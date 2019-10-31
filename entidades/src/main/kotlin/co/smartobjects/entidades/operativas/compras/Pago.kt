package co.smartobjects.entidades.operativas.compras

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import co.smartobjects.campos.validadorDecimalMayorACero
import co.smartobjects.utilidades.Decimal

class Pago(
        val campoValorPagado: CampoValorPagado,
        val metodoPago: MetodoDePago,
        val campoNumeroDeTransaccionPOS: CampoNumeroTransaccion
          )
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Pago::class.java.simpleName
    }


    object Campos
    {
        @JvmField
        val VALOR_PAGADO = Pago::valorPagado.name
        @JvmField
        val NUMERO_TRANSACCION_POS = Pago::numeroDeTransaccionPOS.name
    }

    constructor(
            valorPagado: Decimal,
            metodoPago: MetodoDePago,
            numeroDeTransaccionPOS: String
               ) : this(
            CampoValorPagado(valorPagado),
            metodoPago,
            CampoNumeroTransaccion(numeroDeTransaccionPOS)
                       )

    val valorPagado: Decimal = campoValorPagado.valor
    val numeroDeTransaccionPOS: String = campoNumeroDeTransaccionPOS.valor

    fun copiar(
            valorPagado: Decimal = this.valorPagado,
            metodoPago: MetodoDePago = this.metodoPago,
            numeroDeTransaccionPOS: String = this.numeroDeTransaccionPOS
              ): Pago
    {
        return Pago(valorPagado, metodoPago, numeroDeTransaccionPOS)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pago

        if (numeroDeTransaccionPOS != other.numeroDeTransaccionPOS) return false
        if (valorPagado != other.valorPagado) return false
        if (metodoPago != other.metodoPago) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = numeroDeTransaccionPOS.hashCode()
        result = 31 * result + valorPagado.hashCode()
        result = 31 * result + metodoPago.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Pago(numeroDeTransaccionPOS=$numeroDeTransaccionPOS, valorPagado=$valorPagado, metodoPago='$metodoPago')"
    }


    enum class MetodoDePago
    {
        EFECTIVO,
        TARJETA_CREDITO,
        TARJETA_DEBITO,
        TIC
    }

    class CampoValorPagado(valorPagado: Decimal) : CampoEntidad<Pago, Decimal>(valorPagado, validadorDecimalMayorACero, NOMBRE_ENTIDAD, Campos.VALOR_PAGADO)
    class CampoNumeroTransaccion(numeroDeTransaccionPOS: String) : CampoEntidad<Pago, String>(numeroDeTransaccionPOS, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NUMERO_TRANSACCION_POS)
}