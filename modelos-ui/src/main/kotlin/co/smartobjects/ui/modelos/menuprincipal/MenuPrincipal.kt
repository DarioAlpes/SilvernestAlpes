package co.smartobjects.ui.modelos.menuprincipal

import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.sincronizadordecontenido.GestorDescargaDeDatos
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.utilidades.Opcional
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


interface MenuPrincipalUI : ModeloUI
{
    val puedeIrARegistrarPersonas: Observable<Boolean>
    val puedeIrAComprarCreditos: Observable<Boolean>
    val dialogoEsperaPorSincronizacionVisible: Observable<Boolean>

    val debeSolicitarUbicacion: Single<Boolean>
    val errorDeDescarga: Maybe<RespuestaVacia>

    val pantallaANavegar: Single<PantallaSeleccionada>

    fun irARegistrarPersonas()
    fun irAComprarCreditos()

    enum class PantallaSeleccionada
    {
        REGISTRAR_PERSONAS, COMPRAR_CREDITOS;
    }
}


class MenuPrincipal
(
        private val idCliente: Long,
        private val posibleContextoDeSesion: Observable<Opcional<ContextoDeSesion>>,
        private val repositorioUbicaciones: RepositorioUbicaciones,
        private val gestorDescargaDeDatos: GestorDescargaDeDatos,
        schedulerBackground: Scheduler = Schedulers.io()
) : MenuPrincipalUI
{
    private val eventosPuedeIrARegistrarPersonas = BehaviorSubject.createDefault(false)
    override val puedeIrARegistrarPersonas = eventosPuedeIrARegistrarPersonas.hide().distinctUntilChanged()!!

    private val eventosPuedeIrAComprarCreditos = BehaviorSubject.createDefault(false)
    override val puedeIrAComprarCreditos = eventosPuedeIrAComprarCreditos.hide().distinctUntilChanged()!!

    override val dialogoEsperaPorSincronizacionVisible = gestorDescargaDeDatos.estaDescargando

    private val eventosCambioUbicaciones = BehaviorSubject.create<MenuPrincipal.CambioBDUbicaciones>()
    override val debeSolicitarUbicacion =
            eventosCambioUbicaciones
                .withLatestFrom(posibleContextoDeSesion)
                .map {
                    when (it.first!!)
                    {
                        MenuPrincipal.CambioBDUbicaciones.NO_CAMBIO -> it.second.esVacio
                        MenuPrincipal.CambioBDUbicaciones.CAMBIO    -> true
                        MenuPrincipal.CambioBDUbicaciones.ERROR     -> false
                    }
                }
                .firstOrError()!!

    private val eventosErrorDeDescarga = PublishSubject.create<RespuestaVacia>()
    override val errorDeDescarga = eventosErrorDeDescarga.firstElement()!!

    private val eventosCambioPantalla = PublishSubject.create<MenuPrincipalUI.PantallaSeleccionada>()
    override val pantallaANavegar = eventosCambioPantalla.hide().firstOrError().doOnSuccess { finalizarProceso() }!!

    override val observadoresInternos: List<Observer<*>> =
            listOf(
                    eventosPuedeIrARegistrarPersonas,
                    eventosPuedeIrAComprarCreditos,
                    eventosErrorDeDescarga,
                    eventosCambioPantalla
                  )
    override val modelosHijos: List<ModeloUI> = emptyList()

    private val disposables = CompositeDisposable()


    init
    {
        Maybe
            .fromCallable { repositorioUbicaciones.listar(idCliente).any() }
            .flatMap { existenUbicaciones ->
                if (existenUbicaciones)
                {
                    eventosPuedeIrARegistrarPersonas.onNext(true)
                    eventosPuedeIrAComprarCreditos.onNext(true)
                    eventosCambioUbicaciones.onNext(CambioBDUbicaciones.NO_CAMBIO)
                    eventosErrorDeDescarga.onComplete()
                    Maybe.empty()
                }
                else
                {
                    gestorDescargaDeDatos.descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                }
            }
            .subscribeOn(schedulerBackground)
            .subscribe {
                var huboRespuestaDeError = false

                for (respuestaVacia in it)
                {
                    if (respuestaVacia === RespuestaVacia.Error.Timeout)
                    {
                        huboRespuestaDeError = true
                        eventosErrorDeDescarga.onNext(RespuestaVacia.Error.Timeout)
                        break
                    }
                    else if (respuestaVacia is RespuestaVacia.Error.Red)
                    {
                        huboRespuestaDeError = true
                        eventosErrorDeDescarga.onNext(respuestaVacia)
                        break
                    }
                    else if (respuestaVacia is RespuestaVacia.Error.Back)
                    {
                        huboRespuestaDeError = true
                        eventosErrorDeDescarga.onNext(respuestaVacia)
                        break
                    }
                }

                if (!huboRespuestaDeError)
                {
                    eventosPuedeIrARegistrarPersonas.onNext(true)
                    eventosPuedeIrAComprarCreditos.onNext(true)
                    eventosCambioUbicaciones.onNext(CambioBDUbicaciones.CAMBIO)
                    eventosErrorDeDescarga.onComplete()
                }
                else
                {
                    eventosCambioUbicaciones.onNext(CambioBDUbicaciones.ERROR)
                }
            }
            .addTo(disposables)
    }

    override fun irARegistrarPersonas()
    {   print("RegistrarPersonas")
        eventosCambioPantalla.onNext(MenuPrincipalUI.PantallaSeleccionada.REGISTRAR_PERSONAS)
    }

    override fun irAComprarCreditos()
    {
        eventosCambioPantalla.onNext(MenuPrincipalUI.PantallaSeleccionada.COMPRAR_CREDITOS)
    }

    override fun finalizarProceso()
    {
        eventosCambioUbicaciones.onComplete()
        disposables.dispose()
        super.finalizarProceso()
    }

    enum class CambioBDUbicaciones
    {
        NO_CAMBIO, CAMBIO, ERROR
    }
}