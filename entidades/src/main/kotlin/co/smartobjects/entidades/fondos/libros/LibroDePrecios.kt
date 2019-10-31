package co.smartobjects.entidades.fondos.libros

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoColeccionNoVacio
import co.smartobjects.campos.ValidadorEnCadena

class LibroDePrecios private constructor(
        idCliente: Long,
        id: Long?,
        nombre: String,
        val campoPrecios: CampoPrecios)
    : Libro<LibroDePrecios>(idCliente, id, nombre, Tipo.PRECIOS)
{
    constructor(idCliente: Long, id: Long?, nombre: String, precios: Set<PrecioEnLibro>)
            : this(idCliente, id, nombre, CampoPrecios(precios))

    val precios: Set<PrecioEnLibro> = campoPrecios.valor.toSet()

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = LibroDePrecios::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Libro.Campos.NOMBRE
        @JvmField
        val PRECIOS = LibroDePrecios::precios.name
    }

    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            nombre: String = this.nombre,
            precios: Set<PrecioEnLibro> = this.precios)
            : LibroDePrecios
    {
        return LibroDePrecios(idCliente, id, nombre, precios)
    }

    override fun copiarConId(idNuevo: Long?): LibroDePrecios
    {
        return copiar(id = idNuevo)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is LibroDePrecios) return false
        if (!super.equals(other)) return false

        if (precios != other.precios) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + precios.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "LibroDePrecios(idCliente=$idCliente, id=$id, nombre='$nombre', precios=$precios)"
    }

    class CampoPrecios(valor: Set<PrecioEnLibro>)
        : CampoEntidad<LibroDePrecios, Set<PrecioEnLibro>>(
            valor,
            ValidadorEnCadena(ValidadorCampoColeccionNoVacio()),
            NOMBRE_ENTIDAD,
            Campos.PRECIOS
                                                          )
}