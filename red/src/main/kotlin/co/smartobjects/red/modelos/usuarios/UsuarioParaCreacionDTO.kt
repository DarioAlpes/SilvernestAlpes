package co.smartobjects.red.modelos.usuarios

import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class UsuarioParaCreacionDTO(
        @get:JsonProperty(UsuarioDTO.PropiedadesJson.ID_CLIENTE)
        val idCliente: Long,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.USUARIO, required = true)
        val usuario: String,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.NOMBRE_COMPLETO, required = true)
        val nombreCompleto: String,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.EMAIL, required = true)
        val email: String,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.CONTRASEÑA, required = true)
        val contraseña: CharArray,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.ROLES, required = true)
        val roles: List<String>,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.ACTIVO, required = true)
        val activo: Boolean,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.APELLIDOS, required = true)
        val apellidos: String,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.TIPO_IDENTIFICACION, required = true)
        val tipoIdentificacion: String,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.NUMERO_IDENTIFICACION, required = true)
        val numeroIdentificacion: String,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.FECHA_CREACION, required = true)
        val fechaCreacion: String,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.VIGENCIA_USUARIO, required = true)
        val vigenciaUsuario: String,

        @get:JsonProperty(UsuarioDTO.PropiedadesJson.CECO, required = true)
        val ceco: String
                                 ) :
        EntidadDTO<Usuario.UsuarioParaCreacion>
{
    @JsonCreator internal constructor(
            usuario: String,
            nombreCompleto: String,
            email: String,
            contraseña: CharArray,
            roles: List<String>,
            activo: Boolean,
            apellidos: String,
            tipoIdentificacion: String,
            numeroIdentificacion: String,
            fechaCreacion: String,
            vigenciaUsuario: String,
            ceco: String)
            : this(0, usuario, nombreCompleto, email, contraseña, roles, activo, apellidos, tipoIdentificacion, numeroIdentificacion, fechaCreacion, vigenciaUsuario, ceco)

    constructor(usuario: Usuario.UsuarioParaCreacion)
            : this(
            usuario.datosUsuario.idCliente,
            usuario.datosUsuario.usuario,
            usuario.datosUsuario.nombreCompleto,
            usuario.datosUsuario.email,
            usuario.contraseña,
            usuario.rolesParaCreacion.map { it.nombre },
            usuario.datosUsuario.activo,
            usuario.datosUsuario.apellidos,
            usuario.datosUsuario.tipoIdentificacion,
            usuario.datosUsuario.numeroIdentificacion,
            usuario.datosUsuario.fechaCreacion,
            usuario.datosUsuario.vigenciaUsuario,
            usuario.datosUsuario.ceco
                  )

    override fun aEntidadDeNegocio(): Usuario.UsuarioParaCreacion
    {
        return Usuario.UsuarioParaCreacion(
                Usuario.DatosUsuario(idCliente, usuario, nombreCompleto, email, activo, apellidos, tipoIdentificacion, numeroIdentificacion, fechaCreacion, vigenciaUsuario, ceco),
                contraseña,
                roles.map { Rol.RolParaCreacionDeUsuario(it) }.toSet()
                                          )
    }

    fun limpiarContraseña()
    {
        contraseña.fill('\u0000')
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is UsuarioParaCreacionDTO) return false

        if (idCliente != other.idCliente) return false
        if (usuario != other.usuario) return false
        if (nombreCompleto != other.nombreCompleto) return false
        if (email != other.email) return false
        if (!Arrays.equals(contraseña, other.contraseña)) return false
        if (roles != other.roles) return false
        if (activo != other.activo) return false
        if (apellidos != other.apellidos) return false
        if (tipoIdentificacion != other.tipoIdentificacion) return false
        if (numeroIdentificacion != other.tipoIdentificacion) return false
        if (fechaCreacion != other.fechaCreacion) return false
        if (vigenciaUsuario != other.vigenciaUsuario) return false
        if (ceco != other.ceco) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + usuario.hashCode()
        result = 31 * result + nombreCompleto.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + Arrays.hashCode(contraseña)
        result = 31 * result + roles.hashCode()
        result = 31 * result + activo.hashCode()
        result = 31 * result + apellidos.hashCode()
        result = 31 * result + tipoIdentificacion.hashCode()
        result = 31 * result + numeroIdentificacion.hashCode()
        result = 31 * result + fechaCreacion.hashCode()
        result = 31 * result + vigenciaUsuario.hashCode()
        result = 31 * result + ceco.hashCode()
        return result
    }
}