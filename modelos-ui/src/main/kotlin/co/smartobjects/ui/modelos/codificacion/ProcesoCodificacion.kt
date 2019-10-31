package co.smartobjects.ui.modelos.codificacion

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.SesionDeManillaAPI
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import hu.akarnokd.rxjava2.operators.FlowableTransformers
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


interface ProcesoCodificacionUI : ModeloUI
{
    val itemsACodificar: List<ItemACodificarUI>
    val seActivaronTodasLasSesiones: Single<Unit>
    val numeroDeSesionesActivas: Observable<Int>
    val mensajesDeError: Observable<String>
    fun iniciarCodificacionDesdePrimeroSinCodificar()

    data class SesionDeManillaYCreditosACodificar(
            val sesionDeManilla: SesionDeManilla,
            val creditosACodificar: ProcesoPagarUI.CreditosACodificarPorPersona
                                                 )
}

class ProcesoCodificacion internal constructor
(
        sesionesDeManillaYCreditosACodificar: List<ProcesoCodificacionUI.SesionDeManillaYCreditosACodificar>,
        private val proveedorOperacionesNFC: ProveedorOperacionesNFC,
        apiSesionDeManilla: SesionDeManillaAPI,
        schedulerBackground: Scheduler = Schedulers.io(),
        itemsACodificarDePruebas: List<ItemACodificarUI>? = null
) : ProcesoCodificacionUI
{
    constructor(
            sesionesDeManillaYCreditosACodificar: List<ProcesoCodificacionUI.SesionDeManillaYCreditosACodificar>,
            proveedorOperacionesNFC: ProveedorOperacionesNFC,
            apiSesionDeManilla: SesionDeManillaAPI,
            schedulerBackground: Scheduler = Schedulers.io()
               )
            : this(
            sesionesDeManillaYCreditosACodificar,
            proveedorOperacionesNFC,
            apiSesionDeManilla,
            schedulerBackground,
            null
                  )

    private val resultadosNFCExitosos =
            proveedorOperacionesNFC
                .resultadosNFCLeidos
                .filter { it is ResultadoNFC.Exitoso }
                .cast<ResultadoNFC.Exitoso>()

    override val itemsACodificar: List<ItemACodificarUI> =
            itemsACodificarDePruebas ?: sesionesDeManillaYCreditosACodificar.map {
                ItemACodificar(
                        it.sesionDeManilla,
                        it.creditosACodificar,
                        resultadosNFCExitosos,
                        apiSesionDeManilla,
                        schedulerBackground
                              )
            }

    private val estadosDeItems =
            Observable.combineLatest(itemsACodificar.map { it.estado })
            { emisiones: Array<out Any> ->
                emisiones
                    .asSequence()
                    .map { it as ItemACodificarUI.Estado }
                    .toList()
            }

    override val numeroDeSesionesActivas: Observable<Int> =
            estadosDeItems.map { it.count { it === ItemACodificarUI.Estado.EtapaSesionManilla.Activada } }

    override val seActivaronTodasLasSesiones: Single<Unit> =
            estadosDeItems.map {
                it.all { it === ItemACodificarUI.Estado.EtapaSesionManilla.Activada }
            }.filter { it }.firstOrError().map { Unit }

    private val eventosDeCodificacionEnOrden = PublishSubject.create<Unit>()

    private val eventosDeMensajesError = PublishSubject.create<String>()
    override val mensajesDeError: Observable<String> = eventosDeMensajesError

    override val observadoresInternos: List<Observer<*>> = listOf(eventosDeCodificacionEnOrden, eventosDeMensajesError)
    override val modelosHijos: List<ModeloUI> = itemsACodificar

    private val disposables = CompositeDisposable()

    init
    {
        proveedorOperacionesNFC
            .resultadosNFCLeidos
            .filter { it is ResultadoNFC.Error }
            .cast<ResultadoNFC.Error>()
            .map {
                when (it)
                {
                    is ResultadoNFC.Error.TagNoSoportado ->
                    {
                        "El tag '${it.nombreTag}' no se encuentra soportado en el momento"
                    }
                    ResultadoNFC.Error.ConectandoseAlTag ->
                    {
                        "No fue posible conectarse con el tag"
                    }
                }
            }
            .toObservable()
            .withLatestFrom(estadosDeItems)
            .doOnNext {
                val posicionItemEnCodificacion = it.second.indexOfFirst { it === ItemACodificarUI.Estado.Codificando }
                if (posicionItemEnCodificacion != -1)
                {
                    itemsACodificar[posicionItemEnCodificacion].habilitarParaCodificacion()
                }
            }
            .map { it.first }
            .subscribe(eventosDeMensajesError)

        eventosDeCodificacionEnOrden
            .retenerUltimoYEsperarPorSeñal(proveedorOperacionesNFC.listoParaLectura)
            .withLatestFrom(estadosDeItems)
            .map {
                it.second.indexOfFirst { it === ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar }
            }
            .filter { it != -1 }
            .switchMap {
                itemsACodificar[it].apply { habilitarParaCodificacion() }.estado
            }
            .filter { it is ItemACodificarUI.Estado.EtapaSesionManilla.Codificada }
            .retenerUltimoYEsperarPorSeñal(proveedorOperacionesNFC.listoParaLectura)
            .subscribe {
                eventosDeCodificacionEnOrden.onNext(Unit)
            }.addTo(disposables)
    }

    override fun iniciarCodificacionDesdePrimeroSinCodificar()
    {
        proveedorOperacionesNFC.permitirLecturaNFC = true
        eventosDeCodificacionEnOrden.onNext(Unit)
    }

    override fun finalizarProceso()
    {
        proveedorOperacionesNFC.permitirLecturaNFC = false
        disposables.dispose()
        //        super.finalizarProceso()
    }

    private fun <T> Observable<T>.retenerUltimoYEsperarPorSeñal(señal: Flowable<Boolean>): Observable<T>
    {
        return toFlowable(BackpressureStrategy.LATEST).compose(FlowableTransformers.valve(señal)).toObservable()
    }
}