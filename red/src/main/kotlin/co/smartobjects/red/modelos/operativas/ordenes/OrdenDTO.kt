package co.smartobjects.red.modelos.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.ZonedDateTime

data class OrdenDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id)
        @param:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.idSesionDeManilla, required = true)
        @param:JsonProperty(PropiedadesJSON.idSesionDeManilla, required = true)
        val idSesionDeManilla: Long,

        @get:JsonProperty(PropiedadesJSON.transacciones, required = true)
        @param:JsonProperty(PropiedadesJSON.transacciones, required = true)
        val transacciones: List<ITransaccionDTO>,

        @get:JsonProperty(PropiedadesJSON.fechaDeRealizacion, required = true)
        @param:JsonProperty(PropiedadesJSON.fechaDeRealizacion, required = true)
        val fechaDeRealizacion: ZonedDateTime
) : EntidadDTO<Orden>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val idSesionDeManilla = "tag-session-id"
        const val transacciones = "transactions"
        const val fechaDeRealizacion = "order-timestamp"
    }

    object CodigosError : CodigosErrorDTO(61000)
    {
        @JvmField
        val ESTA_MARCADA_CON_CREACION_TERMINADA = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1

        @JvmField
        val TRANSACCIONES_VACIAS = ESTA_MARCADA_CON_CREACION_TERMINADA + 1

        @JvmField
        val FECHA_DE_REALIZACION_INVALIDA = TRANSACCIONES_VACIAS + 1
    }

    constructor(orden: Orden)
            : this(
            orden.idCliente,
            orden.id,
            orden.idSesionDeManilla,
            orden.transacciones.map { ITransaccionDTO.aITransaccionDTO(it) },
            orden.fechaDeRealizacion
                  )

    override fun aEntidadDeNegocio(): Orden
    {
        return Orden(idCliente, id, idSesionDeManilla, transacciones.map { it.aEntidadDeNegocio() }, fechaDeRealizacion)
    }
}