package co.smartobjects.ui.modelos.contabilizacionubicaciones

import co.smartobjects.entidades.tagscodificables.TagConsumos
import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.nfc.operacionessobretags.ResultadoLecturaNFC
import co.smartobjects.nfc.utils.descomprimir
import co.smartobjects.persistencia.basederepositorios.Listable
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioUbicacionesContabilizables
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.ubicaciones.contabilizables.ConteosEnUbicacionAPI
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.threeten.bp.ZonedDateTime


interface ProcesoContabilizacionUbicacionesUI : ModeloUI
{
    val ubicacionesContabilizables: Single<List<Ubicacion>>
    val conteo: Observable<Int>
    val estado: Observable<Estado>
    val mensajesDeError: Observable<String>

    fun cambiarUbicacion(ubicacion: Ubicacion)

    fun contabilizarManilla(resultadoNFCExitoso: ResultadoNFC.Exitoso)

    sealed class Estado
    {
        object CargandoUbicaciones : Estado()
        object UbicacionesCargadas : Estado()
        sealed class EsperandoTag : Estado()
        {
            object Neutro : EsperandoTag()
            object AnteriorExitoso : EsperandoTag()
            object AnteriorFallido : EsperandoTag()
        }
        object Codificando : Estado()
    }
}

class ProcesoContabilizacionUbicaciones
(
        private val idCliente: Long,
        private val apiConteosEnUbicacion: ConteosEnUbicacionAPI,
        private val repositorioListarUbicaciones: Listable<Ubicacion>,
        private val repositorioUbicacionesContabilizables: RepositorioUbicacionesContabilizables,
        schedulerBackground: Scheduler = Schedulers.io()
) : ProcesoContabilizacionUbicacionesUI
{
    private val eventosContar = PublishSubject.create<Int>()
    override val conteo: Observable<Int> =
            eventosContar
                .startWith(0)
                .scan(0) { acumulado: Int, delta: Int -> acumulado + delta }
                .distinctUntilChanged()

    private val eventosDeEstado =
            BehaviorSubject
                .createDefault<ProcesoContabilizacionUbicacionesUI.Estado>(
                        ProcesoContabilizacionUbicacionesUI.Estado.CargandoUbicaciones
                                                                          )
    override val estado: Observable<ProcesoContabilizacionUbicacionesUI.Estado> =
            eventosDeEstado.distinctUntilChanged().share()

    private val eventosDeMensajesError = PublishSubject.create<String>()
    override val mensajesDeError: Observable<String> = eventosDeMensajesError

    private val eventosCambioDeUbicacion = PublishSubject.create<Ubicacion>()
    private val eventosContabilizarManilla = PublishSubject.create<ResultadoNFC.Exitoso>()

    override val ubicacionesContabilizables: Single<List<Ubicacion>> =
            Singles.zip(
                    Single
                        .fromCallable<Sequence<Ubicacion>> { repositorioListarUbicaciones.listar(idCliente) }
                        .subscribeOn(schedulerBackground),
                    Single
                        .fromCallable<Sequence<Long>> { repositorioUbicacionesContabilizables.listar(idCliente) }
                        .subscribeOn(schedulerBackground)
                       )
            { ubicaciones, ubicacionesContabilizables ->
                val idsContabilizables = ubicacionesContabilizables.toSet()

                ubicaciones.filter { it.id in idsContabilizables }.sortedBy { it.nombre }.toList()
            }
                .doOnSuccess {
                    eventosCambioDeUbicacion.onNext(it.first())
                    eventosDeEstado.onNext(ProcesoContabilizacionUbicacionesUI.Estado.UbicacionesCargadas)
                    eventosDeEstado.onNext(ProcesoContabilizacionUbicacionesUI.Estado.EsperandoTag.Neutro)
                }
                .subscribeOn(schedulerBackground)
                .cache()


    override val observadoresInternos: List<Observer<*>> =
            listOf(eventosContar, eventosDeEstado, eventosDeMensajesError, eventosCambioDeUbicacion)

    override val modelosHijos: List<ModeloUI> = listOf()

    private val compositeDisposable = CompositeDisposable()

    init
    {
        eventosContabilizarManilla
            .withLatestFrom(estado)
            .filter { it.second is ProcesoContabilizacionUbicacionesUI.Estado.EsperandoTag }
            .doOnNext { eventosDeEstado.onNext(ProcesoContabilizacionUbicacionesUI.Estado.Codificando) }
            .map { it.first }
            .withLatestFrom(eventosCambioDeUbicacion)
            .observeOn(schedulerBackground)
            .map {
                val logroContabilizar = contabilizar(it.first, it.second)
                if (logroContabilizar)
                {
                    eventosContar.onNext(1)
                    ProcesoContabilizacionUbicacionesUI.Estado.EsperandoTag.AnteriorExitoso
                }
                else
                {
                    ProcesoContabilizacionUbicacionesUI.Estado.EsperandoTag.AnteriorFallido
                }
            }
            .materialize()
            .subscribeOn(schedulerBackground)
            .subscribe {
                if (it.isOnNext)
                {
                    eventosDeEstado.onNext(it.value!!)
                }
                else
                {
                    eventosDeEstado.onNext(ProcesoContabilizacionUbicacionesUI.Estado.EsperandoTag.Neutro)
                }
            }
            .addTo(compositeDisposable)
    }

    override fun cambiarUbicacion(ubicacion: Ubicacion)
    {
        eventosCambioDeUbicacion.onNext(ubicacion)
    }

    override fun contabilizarManilla(resultadoNFCExitoso: ResultadoNFC.Exitoso)
    {
        eventosContabilizarManilla.onNext(resultadoNFCExitoso)
    }

    private fun contabilizar(resultadoNFC: ResultadoNFC.Exitoso, ubicacionContabilizable: Ubicacion): Boolean
    {
        // Limpiar mensaje anterior
        eventosDeMensajesError.onNext("")

        return when (val lecturaNFC = resultadoNFC.operacion.leerTag())
        {
            is ResultadoLecturaNFC.TagLeido              ->
            {
                val tagConsumosLeido = TagConsumos(descomprimir(lecturaNFC.valor))
                val conteo =
                        ConteoUbicacion(
                                idCliente,
                                ubicacionContabilizable.id!!,
                                tagConsumosLeido.idSesionDeManilla,
                                ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                                       )

                when (val respuesta = apiConteosEnUbicacion.crear(ubicacionContabilizable.id!!, conteo))
                {
                    is RespuestaIndividual.Exitosa       ->
                    {
                        true
                    }
                    is RespuestaIndividual.Vacia         ->
                    {
                        eventosDeMensajesError.onNext("Error al registrar el conteo en el servidor")
                        false
                    }
                    is RespuestaIndividual.Error.Timeout ->
                    {
                        eventosDeMensajesError.onNext("Timeout contactando el backend")
                        false
                    }
                    is RespuestaIndividual.Error.Red     ->
                    {
                        eventosDeMensajesError.onNext("Error contactando el backend")
                        false
                    }
                    is RespuestaIndividual.Error.Back    ->
                    {
                        eventosDeMensajesError.onNext("Error al registrar el conteo en el servidor: ${respuesta.error.mensaje}")
                        false
                    }
                }
            }
            ResultadoLecturaNFC.TagVacio                 ->
            {
                eventosDeMensajesError.onNext("El tag leído está vacío")
                false
            }
            ResultadoLecturaNFC.LlaveDesconocida         ->
            {
                eventosDeMensajesError.onNext("El tag está programado con una llave desconocida")
                false
            }
            ResultadoLecturaNFC.SinAutenticacionActivada ->
            {
                eventosDeMensajesError.onNext("El tag no tiene autenticación activada")
                false
            }
            is ResultadoLecturaNFC.ErrorDeLectura        ->
            {
                eventosDeMensajesError.onNext("Error al leer el tag")
                false
            }
        }
    }

    override fun finalizarProceso()
    {
        compositeDisposable.dispose()
        super.finalizarProceso()
    }
}