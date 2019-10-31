package co.smartobjects.prompterbackend.seguridad.shiro

import co.smartobjects.entidades.usuarios.Permiso
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.persistencia.usuarios.Hasher
import co.smartobjects.persistencia.usuarios.RepositorioCredencialesGuardadasUsuario
import co.smartobjects.persistencia.usuariosglobales.RepositorioCredencialesGuardadasUsuarioGlobal
import org.apache.shiro.authc.Account
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.credential.DefaultPasswordService
import org.apache.shiro.authc.credential.PasswordMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.Permission
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.ConfigurableHashService
import org.apache.shiro.crypto.hash.DefaultHashService
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.util.ByteSource

private val NOMBRE_REALM_REPOSITORIO_USUARIOS = RealmRepositorioUsuarios::class.java.simpleName


internal class RealmRepositorioUsuarios : AuthorizingRealm()
{
    companion object
    {
        internal lateinit var servicioHash: ConfigurableHashService
        internal lateinit var servicioHashPassword: DefaultPasswordService
        internal val hasherShiro = object : Hasher
        {
            override fun calcularHash(entrada: CharArray): String
            {   println("\ncalcularHash: "+entrada)
                return servicioHashPassword.hashFormat.format(servicioHashPassword.hashPassword(entrada))
            }
        }
        internal lateinit var repositorioCredencialesUsuarioGuardadas: RepositorioCredencialesGuardadasUsuario
        internal lateinit var repositorioCredencialesUsuariosGlobalesGuardadas: RepositorioCredencialesGuardadasUsuarioGlobal
    }

    init
    {
        servicioHash = DefaultHashService().apply {
            hashIterations = 500000
            hashAlgorithmName = Sha512Hash.ALGORITHM_NAME
            isGeneratePublicSalt = true
            randomNumberGenerator = SecureRandomNumberGenerator().apply {
                defaultNextBytesSize = 32
            }
        }

        servicioHashPassword = DefaultPasswordService().apply {
            hashService = servicioHash
            hashFormat = Shiro1CryptFormat()
        }
        credentialsMatcher = PasswordMatcher().apply {
            passwordService = servicioHashPassword
        }
        name = NOMBRE_REALM_REPOSITORIO_USUARIOS
    }

    @Suppress("unused")
    fun setSaltPrivada(salt: ByteSource)
    {
        servicioHash.setPrivateSalt(salt)
    }

    override fun doGetAuthenticationInfo(token: AuthenticationToken?): AuthenticationInfo?
    {
        val tokenConIdCliente = token as? TokenUsuarioContraseñaConPosibleIdCliente

        return tokenConIdCliente?.run {
            if (idCliente != null)
            {
                repositorioCredencialesUsuarioGuardadas.buscarPorId(idCliente, tokenUsuario.username)?.run {
                    UsuarioDeClienteShiro(this)
                }
            }
            else
            {
                repositorioCredencialesUsuariosGlobalesGuardadas.buscarPorId(tokenUsuario.username)?.run {
                    UsuarioGlobalShiro(this)
                }
            }
        }
    }

    override fun doGetAuthorizationInfo(principals: PrincipalCollection?): AuthorizationInfo?
    {
        val credenciales = principals?.firstOrNull()
        return when (credenciales)
        {
            is Usuario.CredencialesGuardadas       -> UsuarioDeClienteShiro(credenciales)
            is UsuarioGlobal.CredencialesGuardadas -> UsuarioGlobalShiro(credenciales)
            else                                   -> null
        }
    }

    override fun getAuthenticationTokenClass(): Class<*>
    {
        return TokenUsuarioContraseñaConPosibleIdCliente::class.java
    }
}

private class UsuarioDeClienteShiro(private val credenciales: Usuario.CredencialesGuardadas) : Account
{
    override fun getCredentials(): Any
    {
        return credenciales.hashContraseña
    }

    override fun getRoles(): MutableCollection<String>
    {
        return credenciales.usuario.roles.map { it.nombre }.toMutableSet()
    }

    override fun getObjectPermissions(): MutableCollection<Permission>
    {
        return credenciales.permisos.map { PermisoShiro(it) }.toMutableSet()
    }

    override fun getPrincipals(): PrincipalCollection
    {
        return SimplePrincipalCollection(credenciales, NOMBRE_REALM_REPOSITORIO_USUARIOS)
    }

    override fun getStringPermissions(): MutableCollection<String>
    {
        return objectPermissions.map { it.toString() }.toMutableSet()
    }
}

private class UsuarioGlobalShiro(private val credenciales: UsuarioGlobal.CredencialesGuardadas) : Account
{
    override fun getCredentials(): Any
    {
        return credenciales.hashContraseña
    }

    override fun getRoles(): MutableCollection<String>
    {
        // Por ahora usuarios globales no tiene permisos ni roles, quedan quemados en código
        return mutableSetOf()
    }

    override fun getObjectPermissions(): MutableCollection<Permission>
    {
        // Por ahora usuarios globales no tiene permisos ni roles, quedan quemados en código
        return mutableSetOf()
    }

    override fun getPrincipals(): PrincipalCollection
    {
        return SimplePrincipalCollection(credenciales, NOMBRE_REALM_REPOSITORIO_USUARIOS)
    }

    override fun getStringPermissions(): MutableCollection<String>
    {
        return objectPermissions.map { it.toString() }.toMutableSet()
    }
}

internal data class PermisoShiro(private val permiso: Permiso) : Permission
{
    override fun implies(p: Permission?): Boolean
    {
        return this == p
    }

    override fun toString(): String
    {
        return permiso.aStringDePermiso()
    }
}

internal data class TokenUsuarioContraseñaConPosibleIdCliente(val idCliente: Long?, val tokenUsuario: UsernamePasswordToken)
    : AuthenticationToken by tokenUsuario
{
    fun limpiar()
    {   //val newPass = RealmRepositorioUsuarios.hasherShiro.calcularHash(pass)
        //println("\n pass: "+pass+" newPass: "+newPass)
        tokenUsuario.clear()
    }
}