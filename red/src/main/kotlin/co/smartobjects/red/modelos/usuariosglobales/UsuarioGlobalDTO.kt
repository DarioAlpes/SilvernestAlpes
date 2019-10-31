package co.smartobjects.red.modelos.usuariosglobales

import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UsuarioGlobalDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.USUARIO, required = true)
        @param:JsonProperty(PropiedadesJSON.USUARIO, required = true)
        val usuario: String,

        @get:JsonProperty(PropiedadesJSON.NOMBRE_COMPLETO, required = true)
        @param:JsonProperty(PropiedadesJSON.NOMBRE_COMPLETO, required = true)
        val nombreCompleto: String,

        @get:JsonProperty(PropiedadesJSON.EMAIL, required = true)
        @param:JsonProperty(PropiedadesJSON.EMAIL, required = true)
        val email: String,

        @get:JsonProperty(PropiedadesJSON.ACTIVO, required = true)
        @param:JsonProperty(PropiedadesJSON.ACTIVO, required = true)
        val activo: Boolean
) : EntidadDTO<UsuarioGlobal>
{
    internal object PropiedadesJSON
    {
        const val ID_CLIENTE = "client-id"
        const val USUARIO = "username"
        const val NOMBRE_COMPLETO = "full-name"
        const val EMAIL = "email"
        const val CONTRASEÑA = "password"
        const val ACTIVO = "active"
    }

    object CodigosError : CodigosErrorDTO(80300)
    {
        val USUARIO_INVALIDO = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
        val NOMBRE_INVALIDO = USUARIO_INVALIDO + 1
        val EMAIL_INVALIDO = NOMBRE_INVALIDO + 1
        val CONTRASEÑA_INVALIDA = EMAIL_INVALIDO + 1
        val ACTIVO_INVALIDO = CONTRASEÑA_INVALIDA + 1
    }

    constructor(usuario: UsuarioGlobal)
            : this(
            usuario.datosUsuario.usuario,
            usuario.datosUsuario.nombreCompleto,
            usuario.datosUsuario.email,
            usuario.datosUsuario.activo
                  )

    override fun aEntidadDeNegocio(): UsuarioGlobal
    {
        return UsuarioGlobal(UsuarioGlobal.DatosUsuario(usuario, nombreCompleto, email, activo))
    }
}