package co.smartobjects.entidades.fondos.precios

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.validadorDecimalMayorACero
import co.smartobjects.utilidades.Decimal

class Precio private constructor(val campoValor: CampoValor, val idImpuesto: Long)
{
    constructor(valor: Decimal, idImpuesto: Long) : this(CampoValor(valor), idImpuesto)

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Precio::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val VALOR = Precio::valor.name
    }

    val valor = campoValor.valor

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Precio

        if (valor != other.valor) return false
        if (idImpuesto != other.idImpuesto) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = valor.hashCode()
        result = 31 * result + idImpuesto.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Precio(idImpuesto=$idImpuesto, valor=$valor)"
    }

    fun copiar(valor: Decimal = this.valor, idImpuesto: Long = this.idImpuesto): Precio
    {
        return Precio(valor, idImpuesto)
    }

    class CampoValor(valor: Decimal) : CampoEntidad<Precio, Decimal>(valor, validadorDecimalMayorACero, NOMBRE_ENTIDAD, Campos.VALOR)
}