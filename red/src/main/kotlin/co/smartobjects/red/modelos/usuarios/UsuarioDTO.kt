package co.smartobjects.red.modelos.usuarios

import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UsuarioDTO @JsonCreator constructor
(
        @get:JsonProperty(PropiedadesJson.ID_CLIENTE, required = true)
        @param:JsonProperty(PropiedadesJson.ID_CLIENTE, required = true)
        val idCliente: Long,

        @get:JsonProperty(PropiedadesJson.USUARIO, required = true)
        @param:JsonProperty(PropiedadesJson.USUARIO, required = true)
        val usuario: String,

        @get:JsonProperty(PropiedadesJson.NOMBRE_COMPLETO, required = true)
        @param:JsonProperty(PropiedadesJson.NOMBRE_COMPLETO, required = true)
        val nombreCompleto: String,

        @get:JsonProperty(PropiedadesJson.EMAIL, required = true)
        @param:JsonProperty(PropiedadesJson.EMAIL, required = true)
        val email: String,

        @get:JsonProperty(PropiedadesJson.APELLIDOS, required = true)
        @param:JsonProperty(PropiedadesJson.APELLIDOS, required = true)
        val apellidos: String,

        @get:JsonProperty(PropiedadesJson.TIPO_IDENTIFICACION, required = true)
        @param:JsonProperty(PropiedadesJson.TIPO_IDENTIFICACION, required = true)
        val tipoIdentifificacion: String,

        @get:JsonProperty(PropiedadesJson.NUMERO_IDENTIFICACION, required = true)
        @param:JsonProperty(PropiedadesJson.NUMERO_IDENTIFICACION, required = true)
        val numeroIdentifificacion: String,

        @get:JsonProperty(PropiedadesJson.FECHA_CREACION, required = true)
        @param:JsonProperty(PropiedadesJson.FECHA_CREACION, required = true)
        val fechaCreacion: String,

        @get:JsonProperty(PropiedadesJson.VIGENCIA_USUARIO, required = true)
        @param:JsonProperty(PropiedadesJson.VIGENCIA_USUARIO, required = true)
        val vigenciaUsuario: String,

        @get:JsonProperty(PropiedadesJson.CECO, required = true)
        @param:JsonProperty(PropiedadesJson.CECO, required = true)
        val ceco: String,

        @get:JsonProperty(PropiedadesJson.ROLES, required = true)
        @param:JsonProperty(PropiedadesJson.ROLES, required = true)
        val roles: List<RolDTO>,

        @get:JsonProperty(PropiedadesJson.ACTIVO, required = true)
        @param:JsonProperty(PropiedadesJson.ACTIVO, required = true)
        val activo: Boolean



) : EntidadDTO<Usuario>
{
    internal object PropiedadesJson
    {
        const val ID_CLIENTE = "client-id"
        const val USUARIO = "username"
        const val NOMBRE_COMPLETO = "full-name"
        const val EMAIL = "email"
        const val ROLES = "roles"
        const val CONTRASEÑA = "password"
        const val ACTIVO = "active"
        const val APELLIDOS = "last-name"
        const val TIPO_IDENTIFICACION = "document-type"
        const val NUMERO_IDENTIFICACION = "document-number"
        const val FECHA_CREACION = "creation-date"
        const val VIGENCIA_USUARIO = "validity-user"
        const val CECO = "ceco"
    }

    object CodigosError : CodigosErrorDTO(80000)
    {
        val USUARIO_INVALIDO = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
        val NOMBRE_INVALIDO = USUARIO_INVALIDO + 1
        val EMAIL_INVALIDO = NOMBRE_INVALIDO + 1
        val CONTRASEÑA_INVALIDA = EMAIL_INVALIDO + 1
        val ROLES_INVALIDOS = CONTRASEÑA_INVALIDA + 1
        val ACTIVO_INVALIDO = ROLES_INVALIDOS + 1
        val APELLIDO_INVALIDO = ACTIVO_INVALIDO + 1
        val TIPO_IDENTIFICACION_INVALIDO = APELLIDO_INVALIDO + 1
        val NUMERO_IDENTIFICACION_INVALIDO = TIPO_IDENTIFICACION_INVALIDO + 1
        val FECHA_CREACION_INVALIDA = NUMERO_IDENTIFICACION_INVALIDO + 1
        val VIGENCIA_USUARIO = FECHA_CREACION_INVALIDA + 1
        val CECO = VIGENCIA_USUARIO + 1
    }

    constructor(usuario: Usuario)
            : this(
            usuario.datosUsuario.idCliente,
            usuario.datosUsuario.usuario,
            usuario.datosUsuario.nombreCompleto,
            usuario.datosUsuario.email,
            usuario.datosUsuario.apellidos,
            usuario.datosUsuario.tipoIdentificacion,
            usuario.datosUsuario.numeroIdentificacion,
            usuario.datosUsuario.fechaCreacion,
            usuario.datosUsuario.vigenciaUsuario,
            usuario.datosUsuario.ceco,
            usuario.roles.map { RolDTO(it) },
            usuario.datosUsuario.activo
            
                  )

    override fun aEntidadDeNegocio(): Usuario
    {
        return Usuario(
                Usuario.DatosUsuario(idCliente, usuario, nombreCompleto, email, activo, apellidos, tipoIdentifificacion, numeroIdentifificacion, fechaCreacion, vigenciaUsuario, ceco),
                roles.map { it.aEntidadDeNegocio() }.toSet()
                      )
    }
}