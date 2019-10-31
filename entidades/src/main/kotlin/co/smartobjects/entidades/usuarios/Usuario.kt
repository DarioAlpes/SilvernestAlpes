package co.smartobjects.entidades.usuarios

import co.smartobjects.campos.*
import java.io.Serializable
import java.util.*

class Usuario private constructor(
        val datosUsuario: DatosUsuario,
        val campoRoles: CampoRoles
                                 ) : Serializable
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Usuario::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE_COMPLETO = DatosUsuario::nombreCompleto.name
        @JvmField
        val EMAIL = DatosUsuario::email.name
        @JvmField
        val USUARIO = DatosUsuario::usuario.name
        @JvmField
        val ROLES = Usuario::roles.name
        @JvmField
        val CONTRASEÑA = UsuarioParaCreacion::contraseña.name
        @JvmField
        val ACTIVO = DatosUsuario::activo.name
        @JvmField
        val APELLIDOS = DatosUsuario::apellidos.name
        @JvmField
        val TIPO_IDENTIFICACION = DatosUsuario::tipoIdentificacion.name
        @JvmField
        val NUMERO_IDENTIFICACION = DatosUsuario::numeroIdentificacion.name
        @JvmField
        val FECHA_CREACION = DatosUsuario::fechaCreacion.name
        @JvmField
        val VIGENCIA_USUARIO = DatosUsuario::vigenciaUsuario.name
        @JvmField
        val CECO = DatosUsuario::ceco.name
    }

    constructor(datosUsuario: DatosUsuario, roles: Set<Rol>)
            : this(datosUsuario, CampoRoles(roles))

    val roles: Set<Rol> = campoRoles.valor

    fun copiar(datosUsuario: DatosUsuario = this.datosUsuario, roles: Set<Rol> = this.roles): Usuario
    {
        return Usuario(datosUsuario, roles)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Usuario

        if (datosUsuario != other.datosUsuario) return false
        if (roles != other.roles) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = datosUsuario.hashCode()
        result = 31 * result + roles.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Usuario(datosUsuario=$datosUsuario, roles=$roles)"
    }

    class CampoRoles(roles: Set<Rol>)
        : CampoEntidad<Usuario, Set<Rol>>(roles, ValidadorCampoColeccionNoVacio(), NOMBRE_ENTIDAD, Campos.ROLES), Serializable

    class DatosUsuario private constructor(
            val idCliente: Long,
            val campoUsuario: CampoUsuario,
            val campoNombreCompleto: CampoNombreCompleto,
            val campoEmail: CampoEmail,
            val campoActivo: CampoActivo,
            val campoApellidos: CampoApellidos,
            val campoTipoIdentificacion: CampoTipoIdentificacion,
            val campoNumeroIdentificacion: CampoNumeroIdentificacion,
            val campoFechaCreacion: CampoFechaCreacion,
            val campoVigenciaUsuario: CampoVigenciaUsuario,
            val campoCeco: CampoCeco
                                          ) : Serializable
    {
        constructor(idCliente: Long, usuario: String, nombreCompleto: String, email: String, activo: Boolean, apellidos: String, tipoIdentificacion: String, numeroIdentificacion: String , fechaCreacion: String, vigenciaUsuario: String, ceco: String)
                : this(idCliente, CampoUsuario(usuario), CampoNombreCompleto(nombreCompleto), CampoEmail(email), CampoActivo(activo), CampoApellidos(apellidos), CampoTipoIdentificacion(tipoIdentificacion), CampoNumeroIdentificacion(numeroIdentificacion), CampoFechaCreacion(fechaCreacion), CampoVigenciaUsuario(vigenciaUsuario), CampoCeco(ceco))

        val usuario: String = campoUsuario.valor
        val nombreCompleto: String = campoNombreCompleto.valor
        val email: String = campoEmail.valor
        val activo: Boolean = campoActivo.valor
        val apellidos: String = campoApellidos.valor
        val tipoIdentificacion: String = campoTipoIdentificacion.valor
        val numeroIdentificacion: String = campoNumeroIdentificacion.valor
        val fechaCreacion: String = campoFechaCreacion.valor
        val vigenciaUsuario: String = campoVigenciaUsuario.valor
        val ceco: String = campoCeco.valor

        fun copiar(
                idCliente: Long = this.idCliente,
                usuario: String = this.usuario,
                nombreCompleto: String = this.nombreCompleto,
                email: String = this.email,
                activo: Boolean = this.activo,
                apellidos: String = this.apellidos,
                tipoIdentificacion: String = this.tipoIdentificacion,
                numeroIdentificacion: String = this.numeroIdentificacion,
                fechaCreacion: String = this.fechaCreacion,
                vigenciaUsuario: String = this.vigenciaUsuario,
                ceco: String = this.ceco)
                : DatosUsuario
        {
            return DatosUsuario(idCliente, usuario, nombreCompleto, email, activo, apellidos, tipoIdentificacion, numeroIdentificacion, fechaCreacion, vigenciaUsuario, ceco)
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (other !is DatosUsuario) return false

            if (idCliente != other.idCliente) return false
            if (activo != other.activo) return false
            if (usuario != other.usuario) return false
            if (nombreCompleto != other.nombreCompleto) return false
            if (email != other.email) return false
            if (apellidos != other.apellidos) return false
            if (tipoIdentificacion != other.tipoIdentificacion) return false
            if (numeroIdentificacion != other.numeroIdentificacion) return false
            if (fechaCreacion != other.fechaCreacion) return false
            if (vigenciaUsuario != other.vigenciaUsuario) return false
            if (ceco != other.ceco) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = idCliente.hashCode()
            result = 31 * result + activo.hashCode()
            result = 31 * result + usuario.hashCode()
            result = 31 * result + nombreCompleto.hashCode()
            result = 31 * result + email.hashCode()
            result = 31 * result + apellidos.hashCode()
            result = 31 * result + tipoIdentificacion.hashCode()
            result = 31 * result + numeroIdentificacion.hashCode()
            result = 31 * result + fechaCreacion.hashCode()
            result = 31 * result + vigenciaUsuario.hashCode()
            result = 31 * result + ceco.hashCode()
            return result
        }

        override fun toString(): String
        {
            return "DatosUsuario(idCliente=$idCliente, activo=$activo, usuario='$usuario', nombreCompleto='$nombreCompleto', email='$email', apellidos='$apellidos', tipoIdentificacion='$tipoIdentificacion', numeroIdentificacion='$numeroIdentificacion', fechaCreacion='$fechaCreacion', vigenciaUsuario='$vigenciaUsuario', , ceco='$ceco')"
        }

        class CampoUsuario(usuario: String)
            : CampoEntidad<Usuario, String>(usuario, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.USUARIO), Serializable

        class CampoNombreCompleto(nombre: String)
            : CampoEntidad<Usuario, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE_COMPLETO), Serializable

        class CampoEmail(email: String)
            : CampoEntidad<Usuario, String>(email, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.EMAIL), Serializable

        class CampoActivo(activo: Boolean)
            : CampoModificable<Usuario, Boolean>(activo, null, NOMBRE_ENTIDAD, Campos.ACTIVO), Serializable

        class CampoApellidos(apellidos: String)
            : CampoEntidad<Usuario, String>(apellidos, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.APELLIDOS), Serializable

        class CampoTipoIdentificacion(tipoIdentificacion: String)
            : CampoEntidad<Usuario, String>(tipoIdentificacion, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.TIPO_IDENTIFICACION), Serializable

        class CampoNumeroIdentificacion(numeroIdentificacion: String)
            : CampoEntidad<Usuario, String>(numeroIdentificacion, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NUMERO_IDENTIFICACION), Serializable

        class CampoFechaCreacion(fechaCreacion: String)
            : CampoEntidad<Usuario, String>(fechaCreacion, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.FECHA_CREACION), Serializable

        class CampoVigenciaUsuario(vigenciaUsuario: String)
            : CampoEntidad<Usuario, String>(vigenciaUsuario, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.VIGENCIA_USUARIO), Serializable

        class CampoCeco(ceco: String)
            : CampoEntidad<Usuario, String>(ceco, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.CECO), Serializable
    }

    class UsuarioParaCreacion private constructor(val datosUsuario: DatosUsuario, val campoContraseña: CredencialesUsuario.CampoContraseña, val campoRolesParaCreacion: CampoRolesParaCreacion)
    {
        constructor(datosUsuario: DatosUsuario, contraseña: CharArray, rolesParaCreacion: Set<Rol.RolParaCreacionDeUsuario>)
                : this(datosUsuario, CredencialesUsuario.CampoContraseña(contraseña), CampoRolesParaCreacion(rolesParaCreacion))

        val rolesParaCreacion: Set<Rol.RolParaCreacionDeUsuario> = campoRolesParaCreacion.valor
        val contraseña: CharArray = campoContraseña.valor

        fun copiar(datosUsuario: DatosUsuario = this.datosUsuario, contraseña: CharArray = this.contraseña, roles: Set<Rol.RolParaCreacionDeUsuario> = this.rolesParaCreacion): UsuarioParaCreacion
        {
            if (contraseña !== this.contraseña)
            {
                limpiarContraseña()
            }
            return UsuarioParaCreacion(datosUsuario, contraseña, roles)
        }

        fun limpiarContraseña()
        {
            campoContraseña.limpiarValor()
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UsuarioParaCreacion

            if (datosUsuario != other.datosUsuario) return false
            if (rolesParaCreacion != other.rolesParaCreacion) return false
            if (!Arrays.equals(contraseña, other.contraseña)) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = datosUsuario.hashCode()
            result = 31 * result + rolesParaCreacion.hashCode()
            result = 31 * result + Arrays.hashCode(contraseña)
            return result
        }

        override fun toString(): String
        {
            return "UsuarioParaCreacion(datosUsuario=$datosUsuario, rolesParaCreacion=$rolesParaCreacion)"
        }

        class CampoRolesParaCreacion(roles: Set<Rol.RolParaCreacionDeUsuario>)
            : CampoEntidad<Usuario, Set<Rol.RolParaCreacionDeUsuario>>(roles, ValidadorCampoColeccionNoVacio(), NOMBRE_ENTIDAD, Campos.ROLES)
    }

    class CredencialesUsuario private constructor(val campoUsuario: DatosUsuario.CampoUsuario, val campoContraseña: CampoContraseña) : Serializable
    {
        constructor(usuario: String, contraseña: CharArray)
                : this(DatosUsuario.CampoUsuario(usuario), CampoContraseña(contraseña))

        val usuario: String = campoUsuario.valor
        val contraseña: CharArray = campoContraseña.valor

        fun copiar(usuario: String = this.usuario, contraseña: CharArray = this.contraseña): CredencialesUsuario
        {
            if (contraseña !== this.contraseña)
            {
                limpiarContraseña()
            }
            return CredencialesUsuario(usuario, contraseña)
        }

        fun limpiarContraseña()
        {
            campoContraseña.limpiarValor()
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CredencialesUsuario

            if (usuario != other.usuario) return false
            if (!Arrays.equals(contraseña, other.contraseña)) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = usuario.hashCode()
            result = 31 * result + Arrays.hashCode(contraseña)
            return result
        }

        override fun toString(): String
        {
            return "CredencialesUsuario(usuario='$usuario')"
        }

        // El hashcode
        @Suppress("EqualsOrHashCode")
        class CampoContraseña(contraseña: CharArray)
            : CampoModificable<Usuario, CharArray>(contraseña, ValidadorCampoArregloCharNoVacio(), NOMBRE_ENTIDAD, Campos.CONTRASEÑA), Serializable
        {
            fun limpiarValor()
            {
                valor.fill('\u0000')
            }

            override fun equals(other: Any?): Boolean
            {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as CampoContraseña
                if (!Arrays.equals(valor, other.valor)) return false
                if (!Arrays.equals(valor, other.valor)) return false
                if (!Arrays.equals(valor, other.valor)) return false
                return true
            }

            override fun hashCode(): Int
            {
                return Arrays.hashCode(valor)
            }
        }
    }

    data class CredencialesGuardadas(val usuario: Usuario, val hashContraseña: String) : Serializable
    {
        val permisos: Set<Permiso> = usuario.roles.flatMap { it.permisos }.toSet()

        val principalIdParaAutenticacion = usuario.datosUsuario.usuario
    }
}