package co.smartobjects.red.modelos.operativas.ordenes

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class LoteDeOrdenesDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id, required = true)
        @param:JsonProperty(PropiedadesJSON.id, required = true)
        val id: String,

        @get:JsonProperty(TransaccionEntidadTerminadaDTO.PropiedadesJSON.creacionTerminada)
        @param:JsonProperty(TransaccionEntidadTerminadaDTO.PropiedadesJSON.creacionTerminada)
        val creacionTerminada: Boolean = false,

        @get:JsonProperty(PropiedadesJSON.ordenes, required = true)
        @param:JsonProperty(PropiedadesJSON.ordenes, required = true)
        val ordenes: List<OrdenDTO>
) : EntidadDTO<LoteDeOrdenes>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val ordenes = "orders"
    }

    object CodigosError : CodigosErrorDTO(60000)
    {
        @JvmField
        val ORDENES_INVALIDAS = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
    }

    constructor(loteDeOrdenes: LoteDeOrdenes) :
            this(
                    loteDeOrdenes.idCliente,
                    loteDeOrdenes.id,
                    loteDeOrdenes.creacionTerminada,
                    loteDeOrdenes.ordenes.map { OrdenDTO(it) }
                )

    override fun aEntidadDeNegocio(): LoteDeOrdenes
    {
        val partesId = EntidadTransaccional.idAPartes(id)
        return LoteDeOrdenes(
                idCliente,
                partesId.nombreUsuario,
                partesId.uuid,
                partesId.tiempoCreacion,
                creacionTerminada,
                ordenes.map { it.aEntidadDeNegocio() }
                            )
    }
}