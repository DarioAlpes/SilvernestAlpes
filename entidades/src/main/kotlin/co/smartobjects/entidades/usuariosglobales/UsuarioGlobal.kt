package co.smartobjects.entidades.usuariosglobales

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.CampoModificable
import co.smartobjects.campos.ValidadorCampoArregloCharNoVacio
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import java.util.*

class UsuarioGlobal(val datosUsuario: DatosUsuario)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = UsuarioGlobal::class.java.simpleName
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
        val CONTRASEÑA = UsuarioParaCreacion::contraseña.name
        @JvmField
        val ACTIVO = DatosUsuario::activo.name
    }

    fun copiar(datosUsuario: DatosUsuario = this.datosUsuario): UsuarioGlobal
    {
        return UsuarioGlobal(datosUsuario)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UsuarioGlobal

        if (datosUsuario != other.datosUsuario) return false

        return true
    }

    override fun hashCode(): Int
    {
        return datosUsuario.hashCode()
    }

    override fun toString(): String
    {
        return "UsuarioGlobal(datosUsuario=$datosUsuario)"
    }

    class DatosUsuario private constructor(
            val campoUsuario: CampoUsuario,
            val campoNombreCompleto: CampoNombreCompleto,
            val campoEmail: CampoEmail,
            val campoActivo: CampoActivo
                                          )
    {
        constructor(usuario: String, nombreCompleto: String, email: String, activo: Boolean)
                : this(CampoUsuario(usuario), CampoNombreCompleto(nombreCompleto), CampoEmail(email), CampoActivo(activo))

        val usuario: String = campoUsuario.valor
        val nombreCompleto: String = campoNombreCompleto.valor
        val email: String = campoEmail.valor
        val activo: Boolean = campoActivo.valor

        fun copiar(
                usuario: String = this.usuario,
                nombreCompleto: String = this.nombreCompleto,
                email: String = this.email,
                activo: Boolean = this.activo)
                : DatosUsuario
        {
            return DatosUsuario(usuario, nombreCompleto, email, activo)
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (other !is DatosUsuario) return false

            if (activo != other.activo) return false
            if (usuario != other.usuario) return false
            if (nombreCompleto != other.nombreCompleto) return false
            if (email != other.email) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = activo.hashCode()
            result = 31 * result + usuario.hashCode()
            result = 31 * result + nombreCompleto.hashCode()
            result = 31 * result + email.hashCode()
            return result
        }

        override fun toString(): String
        {
            return "DatosUsuario(activo=$activo, usuario='$usuario', nombreCompleto='$nombreCompleto', email='$email')"
        }

        class CampoUsuario(usuario: String)
            : CampoEntidad<UsuarioGlobal, String>(usuario, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.USUARIO)

        class CampoNombreCompleto(nombre: String)
            : CampoEntidad<UsuarioGlobal, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE_COMPLETO)

        class CampoEmail(email: String)
            : CampoEntidad<UsuarioGlobal, String>(email, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.EMAIL)

        class CampoActivo(activo: Boolean)
            : CampoModificable<UsuarioGlobal, Boolean>(activo, null, NOMBRE_ENTIDAD, Campos.ACTIVO)
    }

    class UsuarioParaCreacion private constructor(val datosUsuario: DatosUsuario, val campoContraseña: CredencialesUsuario.CampoContraseña)
    {

        constructor(datosUsuario: DatosUsuario, contraseña: CharArray)
                : this(datosUsuario, CredencialesUsuario.CampoContraseña(contraseña))

        val contraseña: CharArray = campoContraseña.valor

        fun copiar(datosUsuario: DatosUsuario = this.datosUsuario, contraseña: CharArray = this.contraseña): UsuarioParaCreacion
        {
            if (contraseña !== this.contraseña)
            {
                limpiarContraseña()
            }
            return UsuarioParaCreacion(datosUsuario, contraseña)
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
            if (!Arrays.equals(contraseña, other.contraseña)) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = datosUsuario.hashCode()
            result = 31 * result + Arrays.hashCode(contraseña)
            return result
        }

        override fun toString(): String
        {
            return "UsuarioParaCreacion(datosUsuario=$datosUsuario)"
        }
    }

    class CredencialesUsuario private constructor(val campoUsuario: DatosUsuario.CampoUsuario, val campoContraseña: CampoContraseña)
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
            : CampoModificable<UsuarioGlobal, CharArray>(contraseña, ValidadorCampoArregloCharNoVacio(), NOMBRE_ENTIDAD, Campos.CONTRASEÑA)
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

    data class CredencialesGuardadas(val usuario: UsuarioGlobal, val hashContraseña: String)
}