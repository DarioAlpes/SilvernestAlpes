package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.personas.PersonasDeUnaCompraAPI
import co.smartobjects.ui.modelos.*
import co.smartobjects.utilidades.Opcional
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

interface ProcesoConsultarPersonasPorNumeroTransaccion : ModeloUI
{
    val numeroTransaccionPOS: Observable<Notification<String>>
    val estado: Observable<Estado>
    val errorGlobal: Observable<Opcional<String>>
    val personasConsultadas: Observable<List<Persona>>
    val puedeConsultarPersonas: Observable<Boolean>
    fun intentarConsultarPersonasPorNumeroTransaccion(): ResultadoAccionUI
    fun cambiarNumeroTransaccionPOS(nuevoNumeroTransaccionPOS: String)

    override val modelosHijos: List<ModeloUI>
        get() = listOf()

    enum class Estado
    {
        ESPERANDO_DATOS, CONSULTANDO_PERSONAS
    }
}

class ProcesoConsultarPersonasPorNumeroTransaccionConSujetos(
        private val apiDePersonasDeUnaCompraAPI: PersonasDeUnaCompraAPI,
        private val ioScheduler: Scheduler = Schedulers.io()
                                                            ) : ProcesoConsultarPersonasPorNumeroTransaccion
{
    private val sujetoEstado = BehaviorSubject.createDefault(ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
    private val sujetoNumeroTransaccionPOS = BehaviorSubject.create<Notification<Pago.CampoNumeroTransaccion>>()
    private val sujetPersonasConsultadas = BehaviorSubject.create<List<Persona>>()
    private val sujetoErrorGlobal = BehaviorSubject.createDefault<Opcional<String>>(Opcional.Vacio())

    override val estado = sujetoEstado.hide()!!
    override val numeroTransaccionPOS = sujetoNumeroTransaccionPOS.mapNotificationValorCampo()
    override val errorGlobal = sujetoErrorGlobal.hide()!!
    override val personasConsultadas = sujetPersonasConsultadas.hide()!!
    override val puedeConsultarPersonas =
            Observables.combineLatest(sujetoNumeroTransaccionPOS, estado) { posibleNumeroTransaccionPOS, estado ->
                posibleNumeroTransaccionPOS.isOnNext && estado == ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS
            }.startWith(false).distinctUntilChanged()!!

    override val observadoresInternos: List<Subject<*>> =
            listOf(
                    sujetoEstado,
                    sujetoNumeroTransaccionPOS,
                    sujetPersonasConsultadas,
                    sujetoErrorGlobal
                  )

    override fun intentarConsultarPersonasPorNumeroTransaccion(): ResultadoAccionUI
    {
        return if (sujetoEstado.value == ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
        {
            val numeroTransaccion =
                    try
                    {
                        transformarAEntidadUIEnvolviendoErrores {
                            sujetoNumeroTransaccionPOS.valorDeCampo()
                        }
                    }
                    catch (e: IllegalStateException)
                    {
                        return ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO
                    }
            sujetoErrorGlobal.onNext(Opcional.Vacio())
            sujetoEstado.onNext(ProcesoConsultarPersonasPorNumeroTransaccion.Estado.CONSULTANDO_PERSONAS)
            // La creación y ejecución de este observable se debe poder abstraer, pero quiero esperar a tener otro par de ejemplos antes de hacerlo
            Observable
                .fromCallable { apiDePersonasDeUnaCompraAPI.consultar(numeroTransaccion) }
                .subscribeOn(ioScheduler)
                .subscribe {
                    when (it)
                    {
                        is RespuestaIndividual.Exitosa       ->
                        {
                            sujetPersonasConsultadas.onNext(it.respuesta)
                        }
                        is RespuestaIndividual.Vacia         ->
                        {
                        }
                        is RespuestaIndividual.Error.Timeout ->
                        {
                            sujetoErrorGlobal.onNext(Opcional.De("Timeout contactando el backend"))
                        }
                        is RespuestaIndividual.Error.Red     ->
                        {
                            sujetoErrorGlobal.onNext(Opcional.De("Error contactando el backend"))
                        }
                        is RespuestaIndividual.Error.Back    ->
                        {
                            sujetoErrorGlobal.onNext(Opcional.De("Error en petición: ${it.error.mensaje}"))
                        }
                    }

                    sujetoEstado.onNext(ProcesoConsultarPersonasPorNumeroTransaccion.Estado.ESPERANDO_DATOS)
                }
            ResultadoAccionUI.ACCION_INICIADA
        }
        else
        {
            ResultadoAccionUI.PROCESO_EN_ESTADO_INVALIDO
        }
    }

    override fun cambiarNumeroTransaccionPOS(nuevoNumeroTransaccionPOS: String)
    {
        sujetoNumeroTransaccionPOS.crearYEmitirEntidadNegocioConPosibleError { Pago.CampoNumeroTransaccion(nuevoNumeroTransaccionPOS) }
    }
}