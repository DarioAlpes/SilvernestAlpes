package co.smartobjects.persistencia.usuariosglobales

import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = UsuarioGlobalDAO.TABLA)
internal data class UsuarioGlobalDAO(
        @DatabaseField(columnName = COLUMNA_ID, id = true)
        val usuario: String = "",

        @DatabaseField(columnName = COLUMNA_NOMBRE_COMPLETO, canBeNull = false)
        val nombreCompleto: String = "",

        @DatabaseField(columnName = COLUMNA_EMAIL, canBeNull = false, uniqueCombo = true, uniqueIndexName = "ux_${COLUMNA_EMAIL}_$TABLA")
        val email: String = "",

        @DatabaseField(columnName = COLUMNA_ACTIVO, canBeNull = false)
        val activo: Boolean = false,

        @DatabaseField(columnName = COLUMNA_HASH_CONTRASEÑA, canBeNull = false)
        val hashContraseña: String = ""
                                    )
{
    companion object
    {
        const val TABLA = "usuario_global"
        const val COLUMNA_ID = "usuario_global"
        const val COLUMNA_HASH_CONTRASEÑA = "hash_contrasena"
        const val COLUMNA_NOMBRE_COMPLETO = "nombre_completo"
        const val COLUMNA_EMAIL = "email"
        const val COLUMNA_ACTIVO = "activo"
    }

    constructor(entidadDeNegocio: UsuarioGlobal.UsuarioParaCreacion, hashContraseña: String, creando: Boolean)
            : this(
            entidadDeNegocio.datosUsuario.usuario,
            entidadDeNegocio.datosUsuario.nombreCompleto,
            entidadDeNegocio.datosUsuario.email,
            if (creando) true else entidadDeNegocio.datosUsuario.activo,
            hashContraseña
                  )

    fun aEntidadDeNegocio(): UsuarioGlobal
    {
        return UsuarioGlobal(UsuarioGlobal.DatosUsuario(usuario, nombreCompleto, email, activo))
    }
}