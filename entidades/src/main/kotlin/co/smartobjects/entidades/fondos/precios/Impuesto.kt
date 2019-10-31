package co.smartobjects.entidades.fondos.precios

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import co.smartobjects.campos.validadorDecimalMayorACero
import co.smartobjects.entidades.EntidadReferenciable
import co.smartobjects.utilidades.Decimal

class Impuesto private constructor
(
        val idCliente: Long,
        override val id: Long?,
        val campoNombre: CampoNombre,
        val campoTasa: CampoTasa
) : EntidadReferenciable<Long?, Impuesto>
{
    constructor(idCliente: Long, id: Long?, nombre: String, tasa: Decimal) : this(idCliente, id, CampoNombre(nombre), CampoTasa(tasa))

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Impuesto::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Impuesto::nombre.name
        @JvmField
        val TASA = Impuesto::tasa.name
    }

    val nombre = campoNombre.valor
    /**
     * Se expresa en términos porcentuales, por lo que se debe dividir por 100.
     *
     * Ej: Si la tasa es Decimal(19) esto equivale a 19%
     *
     * Ej: Si la tasa es Decimal(0.19) esto equivale a 0.19%
     */
    val tasa = campoTasa.valor

    fun copiar(idCliente: Long = this.idCliente, id: Long? = this.id, nombre: String = this.nombre, tasa: Decimal = this.tasa): Impuesto
    {
        return Impuesto(idCliente, id, nombre, tasa)
    }

    override fun copiarConId(idNuevo: Long?): Impuesto
    {
        return copiar(id = idNuevo)
    }


    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Impuesto

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (tasa != other.tasa) return false
        if (nombre != other.nombre) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + tasa.hashCode()
        result = 31 * result + nombre.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Impuesto(idCliente=$idCliente, id=$id, tasa=$tasa, nombre='$nombre')"
    }

    class CampoNombre(nombre: String) : CampoEntidad<Impuesto, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE)
    class CampoTasa(tasa: Decimal) : CampoEntidad<Impuesto, Decimal>(tasa, validadorDecimalMayorACero, NOMBRE_ENTIDAD, Campos.TASA)
}