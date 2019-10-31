package co.smartobjects.prompterbackend.seguridad

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.prompterbackend.excepciones.CredencialesIncorrectas
import co.smartobjects.prompterbackend.excepciones.UsuarioNoAutenticado
import co.smartobjects.prompterbackend.excepciones.UsuarioNoTienePermiso

internal interface ManejadorSeguridad
{
    fun darUsuarioDeClienteAutenticado(): Usuario?

    @Throws(UsuarioNoAutenticado::class)
    fun verificarUsuarioDeClienteEstaAutenticado()

    @Throws(UsuarioNoAutenticado::class, UsuarioNoTienePermiso::class)
    fun verificarUsuarioDeClienteActualTienePermiso(permiso: PermisoBack)

    @Throws(CredencialesIncorrectas::class)
    fun loginUsuarioDeCliente(idCliente: Long, usuario: String, contraseña: CharArray)

    @Throws(UsuarioNoAutenticado::class, UsuarioNoTienePermiso::class)
    fun logoutUsuarioDeCliente(idCliente: Long, usuario: String)

    fun darUsuarioGlobalAutenticado(): UsuarioGlobal?

    @Throws(UsuarioNoAutenticado::class)
    fun verificarUsuarioGlobalEstaAutenticado()

    @Throws(CredencialesIncorrectas::class)
    fun loginUsuarioGlobal(usuario: String, contraseña: CharArray)

    @Throws(UsuarioNoAutenticado::class, UsuarioNoTienePermiso::class)
    fun logoutUsuarioGlobal(usuario: String)
}