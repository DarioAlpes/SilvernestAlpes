package co.smartobjects.red.modelos.ubicaciones.consumibles

import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class ConsumibleEnPuntoDeVentaDTO @JsonCreator internal constructor(
        @get:JsonProperty(PropiedadesJSON.idUbicacion, required = true)
        @param:JsonProperty(PropiedadesJSON.idUbicacion, required = true)
        val idUbicacion: Long,

        @get:JsonProperty(PropiedadesJSON.idConsumible, required = true)
        @param:JsonProperty(PropiedadesJSON.idConsumible, required = true)
        val idConsumible: Long,

        @get:JsonProperty(PropiedadesJSON.codigoExternoConsumible, required = true)
        @param:JsonProperty(PropiedadesJSON.codigoExternoConsumible, required = true)
        val codigoExternoConsumible: String
                                                                        ) :
        EntidadDTO<ConsumibleEnPuntoDeVenta>
{
    internal object PropiedadesJSON
    {
        const val idUbicacion = "location-id"
        const val idConsumible = "consumable-id"
        const val codigoExternoConsumible = "consumable-external-code"
    }

    object CodigosError : CodigosErrorDTO(100000)

    constructor(ubicacion: ConsumibleEnPuntoDeVenta) :
            this(
                    ubicacion.idUbicacion,
                    ubicacion.idConsumible,
                    ubicacion.codigoExternoConsumible
                )

    override fun aEntidadDeNegocio(): ConsumibleEnPuntoDeVenta
    {
        return ConsumibleEnPuntoDeVenta(
                idUbicacion,
                idConsumible,
                codigoExternoConsumible
                                       )
    }
}