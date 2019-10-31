package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class PagoDTO @JsonCreator constructor
(
        @get:JsonProperty(PropiedadesJSON.valorPagado, required = true)
        @param:JsonProperty(PropiedadesJSON.valorPagado, required = true)
        val valorPagado: Decimal,

        @get:JsonProperty(PropiedadesJSON.metodoPago, required = true)
        @param:JsonProperty(PropiedadesJSON.metodoPago, required = true)
        val metodoPago: MetodoDePago,

        @get:JsonProperty(PropiedadesJSON.numeroDeTransaccionPOS, required = true)
        @param:JsonProperty(PropiedadesJSON.numeroDeTransaccionPOS, required = true)
        val numeroDeTransaccionPOS: String
) : EntidadDTO<Pago>
{
    internal object PropiedadesJSON
    {
        const val valorPagado = "value"
        const val metodoPago = "method"
        const val numeroDeTransaccionPOS = "pos-transaction-number"
    }

    object CodigosError : CodigosErrorDTO(50300)
    {
        val VALOR_PAGADO_INVALIDO = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
        val NUMERO_TRANSACCION_POS_INVALIDO = VALOR_PAGADO_INVALIDO + 1
    }

    constructor(pago: Pago) : this(pago.valorPagado, MetodoDePago.desdeNegocio(pago.metodoPago), pago.numeroDeTransaccionPOS)

    override fun aEntidadDeNegocio(): Pago
    {
        return Pago(valorPagado, metodoPago.valorEnNegocio, numeroDeTransaccionPOS)
    }

    enum class MetodoDePago(val valorEnNegocio: Pago.MetodoDePago, val valorEnRed: String)
    {
        EFECTIVO(Pago.MetodoDePago.EFECTIVO, "CASH"),
        TARJETA_CREDITO(Pago.MetodoDePago.TARJETA_CREDITO, "CREDIT_CARD"),
        TARJETA_DEBITO(Pago.MetodoDePago.TARJETA_DEBITO, "DEBIT_CARD"),
        TIC(Pago.MetodoDePago.TIC, "TIC");

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Pago.MetodoDePago): MetodoDePago
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }
}