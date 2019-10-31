package co.smartobjects.ui.modelos.registropersonas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.logica.fondos.precios.CalculadorGrupoCliente
import co.smartobjects.ui.modelos.ModeloUI
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

interface ListaPersonasAgregadasUI : ModeloUI
{
    val personasActuales: List<PersonaConGrupoCliente>
    val personasRegistradas: Observable<List<PersonaConGrupoCliente>>
    val puedeRegistrarPersonas: Observable<Boolean>


    fun agregarPersonas(personasAAgregar: List<Persona>)
    fun agregarPersona(persona: Persona)
    fun eliminarPersona(persona: Persona)

    override val modelosHijos: List<ModeloUI>
        get() = listOf()
}

class ListaPersonasAgregadasUIConSujetos(private val calculadorGrupoCliente: CalculadorGrupoCliente) : ListaPersonasAgregadasUI
{
    private val personas = linkedMapOf<Long, PersonaConGrupoCliente>()
    private val sujetoPersonasRegistradas = BehaviorSubject.createDefault<LinkedHashMap<Long, PersonaConGrupoCliente>>(personas)

    override val personasRegistradas = sujetoPersonasRegistradas.map { it.values.toList() }!!
    override val puedeRegistrarPersonas = personasRegistradas.map { it.isNotEmpty() }.distinctUntilChanged()!!
    override val observadoresInternos = listOf(sujetoPersonasRegistradas)
    override val personasActuales: List<PersonaConGrupoCliente>
        get() = personas.values.toList()

    override fun agregarPersonas(personasAAgregar: List<Persona>)
    {

        val agregoAlguna =
                personasAAgregar.asSequence().map { intentarAgregarPersona(it) }
                    .fold(false) { acc, pudoAgregar ->
                        acc || pudoAgregar
                    }

        if (agregoAlguna)
        {
            sujetoPersonasRegistradas.onNext(personas)
        }
    }

    override fun agregarPersona(persona: Persona)
    {
        val agregoPersona = intentarAgregarPersona(persona)
        if (agregoPersona)
        {
            sujetoPersonasRegistradas.onNext(personas)
        }
    }

    private fun intentarAgregarPersona(persona: Persona): Boolean
    {
        return if (!personas.containsKey(persona.id!!))
        {
            personas[persona.id!!] = PersonaConGrupoCliente(persona, calculadorGrupoCliente.darGrupoClienteParaPersona(persona))
            true
        }
        else
        {
            false
        }
    }

    override fun eliminarPersona(persona: Persona)
    {
        if (personas[persona.id!!]?.persona == persona)
        {
            personas.remove(persona.id!!)
            sujetoPersonasRegistradas.onNext(personas)
        }
    }
}