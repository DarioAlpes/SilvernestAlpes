package co.smartobjects.entidades.usuarios

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoColeccionNoVacio
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import java.io.Serializable

class Rol private constructor(val campoNombre: CampoNombre, val campoDescripcion: CampoDescripcion, val campoPermisos: CampoPermisos) : Serializable
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Rol::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Rol::nombre.name
        @JvmField
        val PERMISOS = Rol::permisos.name
        @JvmField
        val DESCRIPCION = Rol::descripcion.name
    }

    constructor(nombre: String, descripcion: String, permisos: Set<Permiso>) : this(CampoNombre(nombre), CampoDescripcion(descripcion), CampoPermisos(permisos))

    val nombre: String = campoNombre.valor
    val descripcion: String = campoDescripcion.valor
    val permisos: Set<Permiso> = campoPermisos.valor

    fun copiar(nombre: String = this.nombre, descripcion: String = this.descripcion, permisos: Set<Permiso> = this.permisos): Rol
    {
        return Rol(nombre, descripcion, permisos)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rol

        if (nombre != other.nombre) return false
        if (descripcion != other.descripcion) return false
        if (permisos != other.permisos) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = nombre.hashCode()
        result = 31 * result + descripcion.hashCode()
        result = 31 * result + permisos.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Rol(nombre='$nombre', descripcion='$descripcion', permisos=$permisos)"
    }

    class CampoNombre(nombre: String)
        : CampoEntidad<Rol, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE), Serializable

    class CampoDescripcion(descripcion: String)
        : CampoEntidad<Rol, String>(descripcion, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.DESCRIPCION), Serializable

    class CampoPermisos(permisos: Set<Permiso>)
        : CampoEntidad<Rol, Set<Permiso>>(permisos, ValidadorCampoColeccionNoVacio(), NOMBRE_ENTIDAD, Campos.PERMISOS), Serializable

    class RolParaCreacionDeUsuario private constructor(val campoNombre: CampoNombre)
    {
        companion object
        {
            val NOMBRE_ENTIDAD = RolParaCreacionDeUsuario::class.java.simpleName
        }

        constructor(nombre: String)
                : this(CampoNombre(nombre))

        val nombre: String = campoNombre.valor
        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RolParaCreacionDeUsuario

            if (nombre != other.nombre) return false

            return true
        }

        override fun hashCode(): Int
        {
            return nombre.hashCode()
        }

        override fun toString(): String
        {
            return "RolParaCreacionDeUsuario(nombre='$nombre')"
        }
    }
}