package co.smartobjects.entidades.ubicaciones.consumibles

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.validadorDecimalMayorACero
import co.smartobjects.utilidades.Decimal

data class ConsumibleEnPuntoDeVenta(
        val idUbicacion: Long,
        val idConsumible: Long,
        val codigoExternoConsumible: String
                                   )
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = ConsumibleEnPuntoDeVenta::class.java.simpleName
    }

    fun copiar(
            idUbicacion: Long = this.idUbicacion,
            idConsumible: Long = this.idConsumible,
            codigoExternoConsumible: String = this.codigoExternoConsumible
              ): ConsumibleEnPuntoDeVenta
    {
        return ConsumibleEnPuntoDeVenta(idUbicacion, idConsumible, codigoExternoConsumible)
    }
}

class Consumo private constructor
(
        val idUbicacion: Long,
        val idConsumible: Long,
        val campoCantidad: CampoCantidad
)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Consumo::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val CANTIDAD = Consumo::cantidad.name
    }

    val cantidad: Decimal = campoCantidad.valor

    constructor(idUbicacion: Long, idConsumible: Long, cantidad: Decimal) : this(idUbicacion, idConsumible, CampoCantidad(cantidad))

    fun copiar(
            idUbicacion: Long = this.idUbicacion,
            idConsumible: Long = this.idConsumible,
            cantidad: Decimal = this.cantidad
              ): Consumo
    {
        return Consumo(idUbicacion, idConsumible, cantidad)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Consumo

        if (idUbicacion != other.idUbicacion) return false
        if (idConsumible != other.idConsumible) return false
        if (cantidad != other.cantidad) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idUbicacion.hashCode()
        result = 31 * result + idConsumible.hashCode()
        result = 31 * result + cantidad.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Consumo(idUbicacion=$idUbicacion, idConsumible=$idConsumible, cantidad=$cantidad)"
    }


    class CampoCantidad(cantidad: Decimal)
        : CampoEntidad<Consumo, Decimal>(cantidad, validadorDecimalMayorACero, NOMBRE_ENTIDAD, Campos.CANTIDAD)
}

data class ConsumoConNombreConsumible(val nombreConsumible: String, val consumo: Consumo)