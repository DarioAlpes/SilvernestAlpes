package co.smartobjects.persistencia.usuarios.roles

import co.smartobjects.entidades.usuarios.Permiso
import co.smartobjects.entidades.usuarios.Rol
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = RolDAO.TABLA)
internal data class RolDAO constructor(
        @DatabaseField(columnName = COLUMNA_ID, id = true)
        val nombre: String = "",

        @DatabaseField(columnName = COLUMNA_DESCRIPCION, canBeNull = false)
        val descripcion: String = ""
                                      )
{
    companion object
    {
        const val TABLA = "rol"
        const val COLUMNA_ID = "nombre_rol"
        const val COLUMNA_DESCRIPCION = "descripcion"
    }

    constructor(entidadDeNegocio: Rol) : this(entidadDeNegocio.nombre, entidadDeNegocio.descripcion)

    fun aEntidadDeNegocio(permisos: Set<Permiso>): Rol
    {
        return Rol(nombre, descripcion, permisos)
    }
}