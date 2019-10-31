package co.smartobjects.red.modelos.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class UbicacionesContabilizablesDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.idsUbicaciones, required = true)
        @param:JsonProperty(PropiedadesJSON.idsUbicaciones, required = true)
        val idsUbicaciones: List<Long>
) : EntidadDTO<UbicacionesContabilizables>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val idsUbicaciones = "locations-ids"
    }

    object CodigosError : CodigosErrorDTO(10600)

    constructor(ubicacionesContabilizables: UbicacionesContabilizables)
            : this(ubicacionesContabilizables.idCliente, ubicacionesContabilizables.idsUbicaciones.toList())


    override fun aEntidadDeNegocio(): UbicacionesContabilizables
    {
        return UbicacionesContabilizables(idCliente, idsUbicaciones.toSet())
    }
}