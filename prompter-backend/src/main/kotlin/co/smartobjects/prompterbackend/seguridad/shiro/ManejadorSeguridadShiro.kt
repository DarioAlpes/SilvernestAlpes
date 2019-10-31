package co.smartobjects.prompterbackend.seguridad.shiro

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.prompterbackend.excepciones.CredencialesIncorrectas
import co.smartobjects.prompterbackend.excepciones.UsuarioNoAutenticado
import co.smartobjects.prompterbackend.excepciones.UsuarioNoTienePermiso
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authz.AuthorizationException

internal class ManejadorSeguridadShiro : ManejadorSeguridad
{
    override fun darUsuarioDeClienteAutenticado(): Usuario?
    {
        return (SecurityUtils.getSubject().principal as? Usuario.CredencialesGuardadas)?.usuario
    }

    @Throws(UsuarioNoAutenticado::class)
    override fun verificarUsuarioDeClienteEstaAutenticado()
    {
        if (!SecurityUtils.getSubject().isAuthenticated || darUsuarioDeClienteAutenticado() == null)
        {
            throw UsuarioNoAutenticado()
        }
    }

    @Throws(UsuarioNoAutenticado::class, UsuarioNoTienePermiso::class)
    override fun verificarUsuarioDeClienteActualTienePermiso(permiso: PermisoBack)
    {
        verificarUsuarioDeClienteEstaAutenticado()
        val sujeto = SecurityUtils.getSubject()
        try
        {
            sujeto.checkPermission(PermisoShiro(permiso))
        }
        catch (e: AuthorizationException)
        {
            throw UsuarioNoTienePermiso()
        }
    }

    @Throws(CredencialesIncorrectas::class)
    override fun loginUsuarioDeCliente(idCliente: Long, usuario: String, contraseña: CharArray)
    {
        val token = TokenUsuarioContraseñaConPosibleIdCliente(idCliente, UsernamePasswordToken(usuario, contraseña))
        try
        {
            val sujeto = SecurityUtils.getSubject()
            sujeto.login(token)
        }
        catch (e: AuthenticationException)
        {
            throw CredencialesIncorrectas()
        }
        finally
        {
            token.limpiar()
        }
    }

    @Throws(UsuarioNoAutenticado::class, UsuarioNoTienePermiso::class)
    override fun logoutUsuarioDeCliente(idCliente: Long, usuario: String)
    {
        verificarUsuarioDeClienteEstaAutenticado()
        val usuarioActual = darUsuarioDeClienteAutenticado()!!
        when
        {
            usuarioActual.datosUsuario.idCliente != idCliente -> throw UsuarioNoAutenticado()
            usuarioActual.datosUsuario.usuario != usuario     -> throw UsuarioNoTienePermiso()
            else                                              -> SecurityUtils.getSubject().logout()
        }
    }

    override fun darUsuarioGlobalAutenticado(): UsuarioGlobal?
    {
        return (SecurityUtils.getSubject().principal as? UsuarioGlobal.CredencialesGuardadas)?.usuario
    }

    @Throws(UsuarioNoAutenticado::class)
    override fun verificarUsuarioGlobalEstaAutenticado()
    {
        if (!SecurityUtils.getSubject().isAuthenticated || darUsuarioGlobalAutenticado() == null)
        {
            throw UsuarioNoAutenticado()
        }
    }

    @Throws(CredencialesIncorrectas::class)
    override fun loginUsuarioGlobal(usuario: String, contraseña: CharArray)
    {
        val token = TokenUsuarioContraseñaConPosibleIdCliente(null, UsernamePasswordToken(usuario, contraseña))
        try
        {
            val sujeto = SecurityUtils.getSubject()
            sujeto.login(token)
        }
        catch (e: AuthenticationException)
        {
            throw CredencialesIncorrectas()
        }
        finally
        {
            token.limpiar()
        }
    }

    @Throws(UsuarioNoAutenticado::class, UsuarioNoTienePermiso::class)
    override fun logoutUsuarioGlobal(usuario: String)
    {
        verificarUsuarioGlobalEstaAutenticado()
        val usuarioActual = darUsuarioGlobalAutenticado()!!
        when
        {
            usuarioActual.datosUsuario.usuario != usuario     -> throw UsuarioNoTienePermiso()
            else                                              -> SecurityUtils.getSubject().logout()
        }
    }
}