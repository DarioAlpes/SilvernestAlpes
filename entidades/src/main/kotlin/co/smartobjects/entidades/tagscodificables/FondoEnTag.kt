package co.smartobjects.entidades.tagscodificables

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.utilidades.Decimal
import java.nio.ByteBuffer


class FondoEnTag(val idFondoComprado: Long, val cantidad: Decimal) : SerializableABytes()
{
    companion object
    {
        const val TAMAÑO_EN_BYTES = java.lang.Double.BYTES + java.lang.Long.BYTES // número de bytes de cantidad como double + idFondoComprado
    }

    override val tamañoTotalEnBytes: Int = TAMAÑO_EN_BYTES

    constructor(creditoFondo: CreditoFondo) : this(creditoFondo.idFondoComprado, creditoFondo.cantidad)
    @Suppress("UsePropertyAccessSyntax")
    constructor(comoBytes: ByteBuffer) : this(comoBytes.getLong(), Decimal(comoBytes.getDouble()))

    fun copiar(cantidad: Decimal = this.cantidad, idFondoComprado: Long = this.idFondoComprado): FondoEnTag
    {
        return FondoEnTag(idFondoComprado, cantidad)
    }

    override fun escribirComoBytes(buffer: ByteBuffer)
    {
        buffer.putLong(idFondoComprado)
        buffer.putDouble(cantidad.aDouble())
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FondoEnTag

        if (idFondoComprado != other.idFondoComprado) return false
        if (cantidad != other.cantidad) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idFondoComprado.hashCode()
        result = 31 * result + cantidad.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "FondoEnTag(idFondoComprado=$idFondoComprado, cantidad=$cantidad)"
    }
}