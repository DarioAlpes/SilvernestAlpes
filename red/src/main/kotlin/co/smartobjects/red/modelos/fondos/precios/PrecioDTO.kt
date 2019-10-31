package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class PrecioDTO @JsonCreator constructor(
        @get:JsonProperty("value", required = true)
        val valor: Decimal,
        @get:JsonProperty("tax-id", required = true)
        val idImpuesto: Long) : EntidadDTO<Precio>
{
    object CodigosError : CodigosErrorDTO(40000)
    {
        // Errores por campos
        const val VALOR_INVALIDO = 40041
    }

    constructor(precio: Precio) :
            this(
                    precio.valor,
                    precio.idImpuesto
                )

    override fun aEntidadDeNegocio(): Precio
    {
        return Precio(
                valor,
                idImpuesto
                     )
    }
}