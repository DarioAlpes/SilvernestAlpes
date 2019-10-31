package co.smartobjects.entidades.fondos.precios

import co.smartobjects.entidades.excepciones.RelacionEntreCamposInvalida
import co.smartobjects.utilidades.Decimal

class PrecioCompleto private constructor(val precioConImpuesto: Decimal, val impuesto: ImpuestoSoloTasa)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = PrecioCompleto::class.java.simpleName
    }

    constructor(precioConImpuesto: Precio, impuesto: ImpuestoSoloTasa) : this(precioConImpuesto.valor, impuesto)
    {
        validarRelacionIdImpuesto(precioConImpuesto, impuesto)
    }

    val precioSinImpuesto = precioConImpuesto / (Decimal.UNO + impuesto.tasaParaCalculos)
    val valorImpuesto = precioConImpuesto - precioSinImpuesto

    private fun validarRelacionIdImpuesto(precio: Precio, impuesto: ImpuestoSoloTasa)
    {
        if (precio.idImpuesto != impuesto.id)
        {
            throw RelacionEntreCamposInvalida(
                    NOMBRE_ENTIDAD,
                    "Id del impuesto en el precio",
                    "Id del impuesto",
                    precio.idImpuesto.toString(),
                    impuesto.id.toString(),
                    RelacionEntreCamposInvalida.Relacion.IGUAL
                                             )
        }
    }

    fun copiar(precioConImpuesto: Precio = Precio(this.precioConImpuesto, this.impuesto.id!!), impuesto: ImpuestoSoloTasa = this.impuesto): PrecioCompleto
    {
        validarRelacionIdImpuesto(precioConImpuesto, impuesto)
        return PrecioCompleto(precioConImpuesto, impuesto)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrecioCompleto

        if (precioConImpuesto != other.precioConImpuesto) return false
        if (impuesto != other.impuesto) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = precioConImpuesto.hashCode()
        result = 31 * result + impuesto.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "PrecioCompleto(precioConImpuesto=$precioConImpuesto, impuesto=$impuesto)"
    }
}