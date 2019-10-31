package co.smartobjects.entidades.fondos.precios

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.validadorDecimalMayorACero
import co.smartobjects.utilidades.Decimal

class ImpuestoSoloTasa private constructor(
        val idCliente: Long,
        val id: Long?,
        val campoTasa: CampoTasa)
{
    constructor(idCliente: Long, id: Long?, tasa: Decimal) : this(idCliente, id, CampoTasa(tasa))
    constructor(impuesto: Impuesto) : this(impuesto.idCliente, impuesto.id, impuesto.tasa)

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = ImpuestoSoloTasa::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val TASA = ImpuestoSoloTasa::tasa.name
    }

    /**
     * Se expresa en términos porcentuales, por lo que se debe dividir por 100.
     *
     * Ej: Si la tasa es Decimal(19) esto equivale a 19%
     *
     * Ej: Si la tasa es Decimal(0.19) esto equivale a 0.19%
     */
    val tasa = campoTasa.valor
    val tasaParaCalculos = tasa / 100.0

    fun copiar(idCliente: Long = this.idCliente, id: Long? = this.id, tasa: Decimal = this.tasa): ImpuestoSoloTasa
    {
        return ImpuestoSoloTasa(idCliente, id, tasa)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImpuestoSoloTasa

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (tasa != other.tasa) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + tasa.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "ImpuestoSoloTasa(idCliente=$idCliente, id=$id, tasa=$tasa)"
    }

    class CampoTasa(tasa: Decimal) : CampoEntidad<ImpuestoSoloTasa, Decimal>(tasa, validadorDecimalMayorACero, NOMBRE_ENTIDAD, Campos.TASA)
}