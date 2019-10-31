package co.smartobjects.entidades.fondos.libros

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import co.smartobjects.entidades.EntidadReferenciable

abstract class Libro<TipoLibro : Libro<TipoLibro>> private constructor(
        val idCliente: Long,
        override val id: Long?,
        val campoNombre: CampoNombre<TipoLibro>,
        val tipo: Tipo)
    : EntidadReferenciable<Long?, TipoLibro>
{
    constructor(idCliente: Long, id: Long?, nombre: String, tipo: Tipo) : this(idCliente, id, CampoNombre(nombre), tipo)

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Libro::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Libro<*>::nombre.name
    }

    val nombre = campoNombre.valor

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Libro<*>

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (nombre != other.nombre) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + nombre.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Libro(idCliente=$idCliente, id=$id, nombre='$nombre')"
    }

    class CampoNombre<TipoLibro : Libro<TipoLibro>>(nombre: String)
        : CampoEntidad<TipoLibro, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE)

    enum class Tipo
    {
        PRECIOS, PROHIBICIONES;
    }
}