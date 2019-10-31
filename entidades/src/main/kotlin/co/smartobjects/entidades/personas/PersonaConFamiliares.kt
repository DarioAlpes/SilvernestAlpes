package co.smartobjects.entidades.personas

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorNoPuedeContenerElemento

class PersonaConFamiliares private constructor(
        val persona: Persona,
        val campoFamiliares: CampoFamiliares
                                              )
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = PersonaConFamiliares::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val FAMILIARES = PersonaConFamiliares::familiares.name
    }

    val familiares = campoFamiliares.valor

    constructor(persona: Persona, familiares: Set<Persona>) : this(persona, CampoFamiliares(persona, familiares))

    fun copiar(
            persona: Persona = this.persona,
            familiares: Set<Persona> = this.familiares): PersonaConFamiliares
    {
        return PersonaConFamiliares(persona, familiares)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersonaConFamiliares

        if (persona != other.persona) return false
        if (familiares != other.familiares) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = persona.hashCode()
        result = 31 * result + familiares.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "PersonaConFamiliares(persona=$persona, familiares=$familiares)"
    }


    class CampoFamiliares(persona: Persona, familiares: Set<Persona>)
        : CampoEntidad<PersonaConFamiliares, Set<Persona>>(
            familiares,
            ValidadorNoPuedeContenerElemento(persona),
            NOMBRE_ENTIDAD,
            Campos.FAMILIARES
                                                          )
}