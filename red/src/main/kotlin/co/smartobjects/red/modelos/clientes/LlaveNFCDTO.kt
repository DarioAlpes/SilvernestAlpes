package co.smartobjects.red.modelos.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.ZonedDateTime

data class LlaveNFCDTO @JsonCreator internal constructor(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.llave)
        @param:JsonProperty(PropiedadesJSON.llave)
        val llave: String,

        @get:JsonProperty(value = PropiedadesJSON.fechaCreacion, required = true)
        @param:JsonProperty(value = PropiedadesJSON.fechaCreacion, required = true)
        val fechaCreacion: ZonedDateTime
                                                        ) :
        EntidadDTO<Cliente.LlaveNFC>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val llave = "key"
        const val fechaCreacion = "creation-datetime"
    }

    object CodigosError : CodigosErrorDTO(10500)
    {
        @JvmField
        val FECHA_CONSULTA_INVALIDA = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
    }

    constructor(llaveNFC: Cliente.LlaveNFC) : this(llaveNFC.idCliente, String(llaveNFC.llave), llaveNFC.fechaCreacion)

    override fun aEntidadDeNegocio(): Cliente.LlaveNFC
    {
        return Cliente.LlaveNFC(idCliente, llave, fechaCreacion)
    }
}