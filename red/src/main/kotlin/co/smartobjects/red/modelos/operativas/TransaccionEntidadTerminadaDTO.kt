package co.smartobjects.red.modelos.operativas

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.red.modelos.EntidadDTOParcial
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class TransaccionEntidadTerminadaDTO<out T : EntidadTransaccional<T>> @JsonCreator constructor(
        @get:JsonProperty(value = PropiedadesJSON.creacionTerminada, required = true)
        @param:JsonProperty(value = PropiedadesJSON.creacionTerminada, required = true)
        val creacionTerminada: Boolean)
    : EntidadDTOParcial<T>
{
    internal object PropiedadesJSON
    {
        const val creacionTerminada = "committed"
    }

    override fun aConjuntoCamposModificables(): Set<CampoModificable<T, *>>
    {
        return setOf(EntidadTransaccional.CampoCreacionTerminada(creacionTerminada))
    }
}