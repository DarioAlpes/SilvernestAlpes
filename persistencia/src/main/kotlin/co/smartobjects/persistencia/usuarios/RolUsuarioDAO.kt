package co.smartobjects.persistencia.usuarios

import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.persistencia.usuarios.roles.RolDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = RolUsuarioDAO.TABLA)
internal data class RolUsuarioDAO constructor(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,
        @DatabaseField(columnName = COLUMNA_ID_ROL, foreign = true, uniqueCombo = true, columnDefinition = "VARCHAR NOT NULL REFERENCES ${RolDAO.TABLA}(${RolDAO.COLUMNA_ID})")
        val rolDAO: RolDAO = RolDAO(),
        @DatabaseField(columnName = COLUMNA_ID_USUARIO, foreign = true, uniqueCombo = true, columnDefinition = "VARCHAR NOT NULL REFERENCES ${UsuarioDAO.TABLA}(${UsuarioDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val usuarioDAO: UsuarioDAO = UsuarioDAO()
                                             )
{
    companion object
    {
        const val TABLA = "rol_usuario"
        const val COLUMNA_ID = "id_dummy_$TABLA"
        const val COLUMNA_ID_ROL = "fk_${TABLA}_${RolDAO.TABLA}"
        const val COLUMNA_ID_USUARIO = "fk_${TABLA}_${UsuarioDAO.TABLA}"
    }

    constructor(usuario: String, entidadDeNegocio: Rol.RolParaCreacionDeUsuario)
            : this(
            null,
            RolDAO(nombre = entidadDeNegocio.nombre),
            UsuarioDAO(usuario = usuario)
                  )
}