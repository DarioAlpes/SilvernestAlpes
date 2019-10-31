package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ImpuestoDTO @JsonCreator internal constructor(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id)
        @param:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.nombre, required = true)
        @param:JsonProperty(PropiedadesJSON.nombre, required = true)
        val nombre: String,

        @get:JsonProperty(PropiedadesJSON.tasa, required = true)
        @param:JsonProperty(PropiedadesJSON.tasa, required = true)
        val tasa: Decimal) : EntidadDTO<Impuesto>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val nombre = "name"
        const val tasa = "rate"
    }

    object CodigosError : CodigosErrorDTO(40100)
    {
        // Errores por campos
        const val NOMBRE_INVALIDO = 40141
        const val TASA_INVALIDA = 40142
    }

    constructor(impuesto: Impuesto) : this(impuesto.idCliente, impuesto.id, impuesto.nombre, impuesto.tasa)

    override fun aEntidadDeNegocio(): Impuesto
    {
        return Impuesto(idCliente, id, nombre, tasa)
    }
}