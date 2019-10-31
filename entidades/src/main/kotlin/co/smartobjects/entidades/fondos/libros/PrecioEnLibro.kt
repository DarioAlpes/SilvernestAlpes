package co.smartobjects.entidades.fondos.libros

import co.smartobjects.entidades.fondos.precios.Precio

class PrecioEnLibro(val precio: Precio, val idFondo: Long)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = PrecioEnLibro::class.java.simpleName
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrecioEnLibro

        if (precio != other.precio) return false
        if (idFondo != other.idFondo) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = precio.hashCode()
        result = 31 * result + idFondo.hashCode()
        return result
    }

    fun copiar(precio: Precio = this.precio, idFondo: Long = this.idFondo): PrecioEnLibro
    {
        return PrecioEnLibro(precio, idFondo)
    }

    override fun toString(): String
    {
        return "PrecioEnLibro(precio=$precio, idFondo=$idFondo)"
    }
}