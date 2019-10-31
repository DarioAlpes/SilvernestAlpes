package co.smartobjects.ui.modelos.codificacion

import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.operativas.reservas.ReservasAPI
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.reservas.ReservaDTO
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import co.smartobjects.utilidades.Opcional
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ProcesoCreacionReservaUI : ModeloUI
{
    val estado: Observable<Estado>
    val mensajesDeError: Observable<String>
    val reservaConNumeroAsignado: Single<Reserva>

    fun intentarCrearActivarYConsultarReserva()

    enum class Estado
    {
        SIN_CREAR, CREANDO, CREADA, ACTIVANDO, ACTIVADA, CONSULTANDO_NUMERO_DE_RESERVA, PROCESO_FINALIZADO
    }
}

class ProcesoCreacionReserva
(
        contextoDeSesion: ContextoDeSesion,
        creditosDeUnaCompra: List<ProcesoPagarUI.CreditosACodificarPorPersona>,
        private val apiDeReservas: ReservasAPI,
        private val schedulerBackground: Scheduler = Schedulers.io()
) : ProcesoCreacionReservaUI
{
    private val reservaACrear =
            Reserva(
                    contextoDeSesion.idCliente,
                    contextoDeSesion.nombreDeUsuario,
                    creditosDeUnaCompra.map {
                        SesionDeManilla(
                                contextoDeSesion.idCliente,
                                null,
                                it.personaConGrupoCliente.persona.id!!,
                                null,
                                null,
                                null,
                                it.creditosFondoTotales.asSequence().map { it.id!! }.toSet()
                                       )
                    }
                   )

    private val eventosDeEstado =
            BehaviorSubject.createDefault<ProcesoCreacionReservaUI.Estado>(ProcesoCreacionReservaUI.Estado.SIN_CREAR)
    override val estado: Observable<ProcesoCreacionReservaUI.Estado> = eventosDeEstado.distinctUntilChanged()

    private val eventosDeMensajesError = PublishSubject.create<String>()
    override val mensajesDeError: Observable<String> = eventosDeMensajesError.distinctUntilChanged()

    private val eventosCreacionDeReserva = PublishSubject.create<Unit>()

    private val eventosConsultaNumeroDeReserva = PublishSubject.create<Reserva>()
    override val reservaConNumeroAsignado: Single<Reserva> = eventosConsultaNumeroDeReserva.firstOrError().cache()

    override val observadoresInternos: List<Observer<*>> =
            listOf(eventosDeEstado, eventosDeMensajesError, eventosCreacionDeReserva, eventosConsultaNumeroDeReserva)
    override val modelosHijos: List<ModeloUI> = listOf()

    private val disposables = CompositeDisposable()

    init
    {
        eventosCreacionDeReserva
            .withLatestFrom(estado)
            .filter {
                it.second === ProcesoCreacionReservaUI.Estado.SIN_CREAR
                || it.second === ProcesoCreacionReservaUI.Estado.CREADA
                || it.second === ProcesoCreacionReservaUI.Estado.ACTIVADA
            }
            .switchMap {
                when (it.second)
                {
                    ProcesoCreacionReservaUI.Estado.SIN_CREAR ->
                    {
                        intentarCrearReserva()
                    }
                    ProcesoCreacionReservaUI.Estado.CREADA    ->
                    {
                        intentarActivarReserva()
                    }
                    ProcesoCreacionReservaUI.Estado.ACTIVADA  ->
                    {
                        consultarReservaParaNumeroDeReserva()
                    }
                    else                                      ->
                    {
                        throw IllegalStateException()
                    }
                }
            }
            .subscribeOn(schedulerBackground)
            .subscribe {
                if (!it.esVacio)
                {
                    eventosCreacionDeReserva.onNext(Unit)
                }
            }
            .addTo(disposables)
    }

    override fun intentarCrearActivarYConsultarReserva()
    {
        eventosCreacionDeReserva.onNext(Unit)
    }

    private fun intentarCrearReserva(): Observable<Opcional<Unit>>
    {
        return cambiarEstadoSegunRespuestaBack<ResultadoCrearReserva>(
                ProcesoCreacionReservaUI.Estado.SIN_CREAR,
                ProcesoCreacionReservaUI.Estado.CREANDO,
                ProcesoCreacionReservaUI.Estado.CREADA
                                                                     )
        {
            when (val respuestaBackend = apiDeReservas.actualizar(reservaACrear.id, reservaACrear))
            {
                is RespuestaIndividual.Exitosa       ->
                {
                    Opcional.De(ResultadoCrearReserva.Creada)
                }
                is RespuestaIndividual.Vacia         ->
                {
                    throw IllegalStateException()
                }
                is RespuestaIndividual.Error.Timeout ->
                {
                    eventosDeMensajesError.onNext("Timeout contactando el backend")
                    Opcional.Vacio()
                }
                is RespuestaIndividual.Error.Red     ->
                {
                    eventosDeMensajesError.onNext("Hubo un error en la conexión y no fue posible contactar al servidor")
                    Opcional.Vacio()
                }
                is RespuestaIndividual.Error.Back    ->
                {
                    when (respuestaBackend.error.codigoInterno)
                    {
                        ReservaDTO.CodigosError.ESTA_MARCADA_CON_CREACION_TERMINADA ->
                        {
                            Opcional.De<ResultadoCrearReserva>(ResultadoCrearReserva.ConsultarPorActivacionPrevia)
                        }
                        ReservaDTO.CodigosError.SESIONES_DE_MANILLAS_INVALIDAS      ->
                        {
                            eventosDeMensajesError.onNext("El estado de las manillas es inconsistente, no se puede crear la reserva")
                            Opcional.Vacio()
                        }
                        else                                                        ->
                        {
                            eventosDeMensajesError.onNext("Error en petición: ${respuestaBackend.error.mensaje}")
                            Opcional.Vacio()
                        }
                    }
                }
            }
        }.doOnNext {
            if (it.valor === ProcesoCreacionReserva.ResultadoCrearReserva.ConsultarPorActivacionPrevia)
            {
                eventosDeEstado.onNext(ProcesoCreacionReservaUI.Estado.ACTIVADA)
            }
        }.map { Opcional.De(Unit) }
    }

    private fun intentarActivarReserva(): Observable<Opcional<Unit>>
    {
        return cambiarEstadoSegunRespuestaBack(
                ProcesoCreacionReservaUI.Estado.CREADA,
                ProcesoCreacionReservaUI.Estado.ACTIVANDO,
                ProcesoCreacionReservaUI.Estado.ACTIVADA
                                              )
        {
            when (val respuestaBackend = apiDeReservas.actualizarCampos(reservaACrear.id, TransaccionEntidadTerminadaDTO(true)))
            {
                RespuestaVacia.Exitosa       ->
                {
                    Opcional.De(Unit)
                }
                RespuestaVacia.Error.Timeout ->
                {
                    eventosDeMensajesError.onNext("Timeout contactando el backend")
                    Opcional.Vacio()
                }
                is RespuestaVacia.Error.Red  ->
                {
                    eventosDeMensajesError.onNext("Hubo un error en la conexión y no fue posible contactar al servidor")
                    Opcional.Vacio()
                }
                is RespuestaVacia.Error.Back ->
                {
                    eventosDeMensajesError.onNext("Error en petición: ${respuestaBackend.error.mensaje}")
                    Opcional.Vacio()
                }
            }
        }
    }

    private fun consultarReservaParaNumeroDeReserva(): Observable<Opcional<Reserva>>
    {
        return cambiarEstadoSegunRespuestaBack(
                ProcesoCreacionReservaUI.Estado.ACTIVADA,
                ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA,
                ProcesoCreacionReservaUI.Estado.PROCESO_FINALIZADO
                                              )
        {
            when (val respuestaBackend = apiDeReservas.consultar(reservaACrear.id))
            {
                is RespuestaIndividual.Exitosa       ->
                {
                    eventosConsultaNumeroDeReserva.onNext(respuestaBackend.respuesta)
                    Opcional.De(respuestaBackend.respuesta)
                }
                is RespuestaIndividual.Vacia         ->
                {
                    throw IllegalStateException()
                }
                is RespuestaIndividual.Error.Timeout ->
                {
                    eventosDeMensajesError.onNext("Timeout contactando el backend")
                    Opcional.Vacio()
                }
                is RespuestaIndividual.Error.Red     ->
                {
                    eventosDeMensajesError.onNext("Hubo un error en la conexión y no fue posible contactar al servidor")
                    Opcional.Vacio()
                }
                is RespuestaIndividual.Error.Back    ->
                {
                    eventosDeMensajesError.onNext("Error en petición: ${respuestaBackend.error.mensaje}")
                    Opcional.Vacio()
                }
            }
        }
    }

    private inline fun <ResultadoExitoso> cambiarEstadoSegunRespuestaBack(
            estadoInicial: ProcesoCreacionReservaUI.Estado,
            estadoIntermedio: ProcesoCreacionReservaUI.Estado,
            estadoExitoso: ProcesoCreacionReservaUI.Estado,
            crossinline llamadoABackend: () -> Opcional<ResultadoExitoso>
                                                                         ): Observable<Opcional<ResultadoExitoso>>
    {
        return Observable.just(Unit)
            .doOnNext {
                eventosDeEstado.onNext(estadoIntermedio)
                eventosDeMensajesError.onNext("")
            }
            .map {
                llamadoABackend()
            }
            .doOnError {
                eventosDeEstado.onNext(estadoInicial)
                throw it
            }
            .doOnNext {
                if (it.esVacio)
                {
                    eventosDeEstado.onNext(estadoInicial)
                }
            }
            .filter { !it.esVacio }
            .doOnNext {
                eventosDeEstado.onNext(estadoExitoso)
            }
            .subscribeOn(schedulerBackground)
    }

    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    private sealed class ResultadoCrearReserva
    {
        object Creada : ResultadoCrearReserva()
        object ConsultarPorActivacionPrevia : ResultadoCrearReserva()
    }
}