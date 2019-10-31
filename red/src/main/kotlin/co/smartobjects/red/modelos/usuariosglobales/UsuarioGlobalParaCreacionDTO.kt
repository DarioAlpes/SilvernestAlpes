package co.smartobjects.red.modelos.usuariosglobales

import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class UsuarioGlobalParaCreacionDTO @JsonCreator internal constructor
(
        @get:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.USUARIO, required = true)
        @param:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.USUARIO, required = true)
        val usuario: String,

        @get:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.NOMBRE_COMPLETO, required = true)
        @param:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.NOMBRE_COMPLETO, required = true)
        val nombreCompleto: String,

        @get:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.EMAIL, required = true)
        @param:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.EMAIL, required = true)
        val email: String,

        @get:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.CONTRASEÑA, required = true)
        @param:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.CONTRASEÑA, required = true)
        val contraseña: CharArray,

        @get:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.ACTIVO, required = true)
        @param:JsonProperty(UsuarioGlobalDTO.PropiedadesJSON.ACTIVO, required = true)
        val activo: Boolean
) : EntidadDTO<UsuarioGlobal.UsuarioParaCreacion>
{
    constructor(usuario: UsuarioGlobal.UsuarioParaCreacion)
            : this(
            usuario.datosUsuario.usuario,
            usuario.datosUsuario.nombreCompleto,
            usuario.datosUsuario.email,
            usuario.contraseña,
            usuario.datosUsuario.activo
                  )

    override fun aEntidadDeNegocio(): UsuarioGlobal.UsuarioParaCreacion
    {
        return UsuarioGlobal.UsuarioParaCreacion(
                UsuarioGlobal.DatosUsuario(usuario, nombreCompleto, email, activo),
                contraseña
                                                )
    }

    fun limpiarContraseña()
    {
        contraseña.fill('\u0000')
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is UsuarioGlobalParaCreacionDTO) return false

        if (usuario != other.usuario) return false
        if (nombreCompleto != other.nombreCompleto) return false
        if (email != other.email) return false
        if (!Arrays.equals(contraseña, other.contraseña)) return false
        if (activo != other.activo) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = usuario.hashCode()
        result = 31 * result + nombreCompleto.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + Arrays.hashCode(contraseña)
        result = 31 * result + activo.hashCode()
        return result
    }
}