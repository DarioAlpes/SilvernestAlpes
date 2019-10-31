package co.smartobjects.red.modelos.personas

import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class PersonaConFamiliaresDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.persona, required = true)
        @param:JsonProperty(PropiedadesJSON.persona, required = true)
        val persona: PersonaDTO,

        @get:JsonProperty(PropiedadesJSON.familiares)
        @param:JsonProperty(PropiedadesJSON.familiares)
        val familiares: List<PersonaDTO>
) : EntidadDTO<PersonaConFamiliares>
{
    internal object PropiedadesJSON
    {
        const val persona = "person"
        const val familiares = "family"
    }

    object CodigosError : CodigosErrorDTO(10800)

    constructor(personaConFamiliares: PersonaConFamiliares) :
            this(
                    PersonaDTO(personaConFamiliares.persona),
                    personaConFamiliares.familiares.map { PersonaDTO(it) }
                )

    override fun aEntidadDeNegocio(): PersonaConFamiliares
    {
        return PersonaConFamiliares(persona.aEntidadDeNegocio(), familiares.map { it.aEntidadDeNegocio() }.toSet())
    }
}