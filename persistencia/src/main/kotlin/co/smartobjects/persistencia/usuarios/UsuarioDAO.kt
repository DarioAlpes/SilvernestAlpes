package co.smartobjects.persistencia.usuarios

import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import sun.util.calendar.LocalGregorianCalendar

@DatabaseTable(tableName = UsuarioDAO.TABLA)
internal data class UsuarioDAO(
        @DatabaseField(columnName = COLUMNA_ID, id = true)
        val usuario: String = "",

        @DatabaseField(columnName = COLUMNA_NOMBRE_COMPLETO, canBeNull = false)
        val nombreCompleto: String = "",

        @DatabaseField(columnName = COLUMNA_EMAIL, canBeNull = false, uniqueCombo = true, uniqueIndexName = "ux_${COLUMNA_EMAIL}_$TABLA")
        val email: String = "",

        @DatabaseField(columnName = COLUMNA_ACTIVO, canBeNull = false)
        val activo: Boolean = false,

        @DatabaseField(columnName = COLUMNA_HASH_CONTRASEÑA, canBeNull = false)
        val hashContraseña: String = "",

        @DatabaseField(columnName = COLUMNA_APELLIDOS, canBeNull = true)
        val apellidos: String = "",

        @DatabaseField(columnName = COLUMNA_TIPO_IDENTIFICAcION, canBeNull = true)
        val tipoIdentificacion: String = "",

        @DatabaseField(columnName = COLUMNA_NUMERO_IDENTIFICAcION, canBeNull = true)
        val numeroIdentificacion: String = "",

        @DatabaseField(columnName = COLUMNA_FECHA_CREACION, canBeNull = false)
        val fechaCreacion: String = "",

        @DatabaseField(columnName = COLUMNA_VIGENCIA_USUARIO, canBeNull = false)
        val vigenciaUsuario: String = "",

        @DatabaseField(columnName = COLUMNA_CECO, canBeNull = false)
        val ceco: String = ""
                              )
{
    companion object
    {
        const val TABLA = "usuario"
        const val COLUMNA_ID = "usuario"
        const val COLUMNA_NOMBRE_COMPLETO = "nombre_completo"
        const val COLUMNA_EMAIL = "email"
        const val COLUMNA_ACTIVO = "activo"
        const val COLUMNA_HASH_CONTRASEÑA = "hash_contraseña"
        const val COLUMNA_APELLIDOS = "apellidos"
        const val COLUMNA_TIPO_IDENTIFICAcION = "tipo_identificacion"
        const val COLUMNA_NUMERO_IDENTIFICAcION = "numero_identificacion"
        const val COLUMNA_FECHA_CREACION = "fecha_creacion"
        const val COLUMNA_VIGENCIA_USUARIO = "vigencia_usuario"
        const val COLUMNA_CECO = "centro_costo"
    }

    constructor(entidadDeNegocio: Usuario.UsuarioParaCreacion, hashContraseña: String, creando: Boolean)
            : this(
            entidadDeNegocio.datosUsuario.usuario,
            entidadDeNegocio.datosUsuario.nombreCompleto,
            entidadDeNegocio.datosUsuario.email,
            if (creando) true else entidadDeNegocio.datosUsuario.activo,
            hashContraseña,
            entidadDeNegocio.datosUsuario.apellidos,
            entidadDeNegocio.datosUsuario.tipoIdentificacion,
            entidadDeNegocio.datosUsuario.numeroIdentificacion,
            entidadDeNegocio.datosUsuario.fechaCreacion,
            entidadDeNegocio.datosUsuario.vigenciaUsuario,
            entidadDeNegocio.datosUsuario.ceco
                  )

    fun aEntidadDeNegocio(idCliente: Long, roles: Set<Rol>): Usuario
    {
        return Usuario(Usuario.DatosUsuario(idCliente, usuario, nombreCompleto, email, activo, apellidos, tipoIdentificacion, numeroIdentificacion, fechaCreacion, vigenciaUsuario, ceco), roles)
    }
}