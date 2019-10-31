package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.personas.PersonasAPI
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.ResultadoAccionUI
import co.smartobjects.ui.modelos.ejecutarAccionEnvolviendoErrores
import co.smartobjects.utilidades.Opcional
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

interface ProcesoConsultarFamiliares : ModeloUI
{
    val familiares: Observable<Iterable<Persona>>
    val estado: Observable<Estado>
    val errorGlobal: Observable<Opcional<String>>
    val personaConsultada: Observable<PersonaConFamiliares>

    fun agregarPersonaConFamiliares(personaConFamiliares: PersonaConFamiliares)
    fun eliminarPersonaConFamiliares(persona: Persona)
    fun intentarConsultarFamiliarPorDocumento(persona: Persona): ResultadoAccionUI

    override val modelosHijos: List<ModeloUI>
        get() = listOf()

    enum class Estado
    {
        ESPERANDO_DATOS, CONSULTANDO_FAMILIAR
    }
}

class ProcesoConsultarFamiliaresConSujetos(
        private val apiPersona: PersonasAPI,
        private val ioScheduler: Scheduler = Schedulers.io()
                                          ) : ProcesoConsultarFamiliares
{
    private val familiaresPorIdPersona: MutableMap<Long, PersonaConFamiliares> = mutableMapOf()

    private val sujetoFamiliaresPorDocumento = BehaviorSubject.createDefault(familiaresPorIdPersona)
    private val sujetoEstado = BehaviorSubject.createDefault(ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
    private val sujetoPersonaConsultada: BehaviorSubject<PersonaConFamiliares> = BehaviorSubject.create()
    private val sujetoErrorGlobal = BehaviorSubject.createDefault<Opcional<String>>(Opcional.Vacio())

    override val estado: Observable<ProcesoConsultarFamiliares.Estado> = sujetoEstado
    override val errorGlobal: Observable<Opcional<String>> = sujetoErrorGlobal
    override val personaConsultada: Observable<PersonaConFamiliares> = sujetoPersonaConsultada
    override val familiares: Observable<Iterable<Persona>> = sujetoFamiliaresPorDocumento.map {
        val familiaresPorDocumento = LinkedHashMap<DocumentoCompleto, Persona>()
        val documentosPersonasAgregadas = ArrayList<DocumentoCompleto>(it.size)
        it.values.forEach {
            documentosPersonasAgregadas.add(DocumentoCompleto(it.persona))
            it.familiares.forEach {
                val documentoCompleto = DocumentoCompleto(it)
                if (!familiaresPorDocumento.containsKey(documentoCompleto))
                {
                    familiaresPorDocumento[DocumentoCompleto(it)] = it
                }
            }
        }
        documentosPersonasAgregadas.forEach {
            familiaresPorDocumento.remove(it)
        }
        familiaresPorDocumento.values
    }

    override val observadoresInternos: List<Subject<*>> = listOf(sujetoEstado, sujetoPersonaConsultada, sujetoFamiliaresPorDocumento, sujetoErrorGlobal)

    override fun intentarConsultarFamiliarPorDocumento(persona: Persona): ResultadoAccionUI
    {
        return if (sujetoEstado.value == ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
        {
            val documentoCompleto = DocumentoCompleto(persona.tipoDocumento, persona.numeroDocumento)
            sujetoErrorGlobal.onNext(Opcional.Vacio())
            sujetoEstado.onNext(ProcesoConsultarFamiliares.Estado.CONSULTANDO_FAMILIAR)
            // La creación y ejecución de este observable se debe poder abstraer, pero quiero esperar a tener otro par de ejemplos antes de hacerlo
            Observable
                .fromCallable { apiPersona.consultarPorDocumento(documentoCompleto) }
                .subscribeOn(ioScheduler)
                .subscribe {
                    when (it)
                    {
                        is RespuestaIndividual.Exitosa       ->
                        {
                            sujetoPersonaConsultada.onNext(it.respuesta)
                        }
                        is RespuestaIndividual.Vacia         ->
                        {
                            sujetoErrorGlobal.onNext(Opcional.De("Error en petición: El familiar debería existir"))
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
                    sujetoEstado.onNext(ProcesoConsultarFamiliares.Estado.ESPERANDO_DATOS)
                }
            ResultadoAccionUI.ACCION_INICIADA
        }
        else
        {
            ResultadoAccionUI.PROCESO_EN_ESTADO_INVALIDO
        }
    }

    @Synchronized
    override fun agregarPersonaConFamiliares(personaConFamiliares: PersonaConFamiliares)
    {
        ejecutarAccionEnvolviendoErrores {
            familiaresPorIdPersona[personaConFamiliares.persona.id!!] = personaConFamiliares
            sujetoFamiliaresPorDocumento.onNext(familiaresPorIdPersona)
        }
    }

    @Synchronized
    override fun eliminarPersonaConFamiliares(persona: Persona)
    {
        ejecutarAccionEnvolviendoErrores {
            familiaresPorIdPersona.remove(persona.id!!)
            sujetoFamiliaresPorDocumento.onNext(familiaresPorIdPersona)
        }
    }
}