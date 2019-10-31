package co.smartobjects.prompterbackend

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.prompterbackend.excepciones.UsuarioNoAutenticado
import co.smartobjects.prompterbackend.excepciones.UsuarioNoTienePermiso
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow

internal val mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos =
        mockConDefaultAnswer(ManejadorSeguridad::class.java).apply {
            doNothing().`when`(this).verificarUsuarioDeClienteActualTienePermiso(cualquiera())
        }

internal val mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado =
        mockConDefaultAnswer(ManejadorSeguridad::class.java).apply {
            doThrow(UsuarioNoAutenticado()).`when`(this).verificarUsuarioDeClienteActualTienePermiso(cualquiera())
        }

internal fun darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permiso: PermisoBack): ManejadorSeguridad
{
    return mockConDefaultAnswer(ManejadorSeguridad::class.java).apply {
        doThrow(UsuarioNoTienePermiso()).`when`(this).verificarUsuarioDeClienteActualTienePermiso(permiso)
    }
}

internal fun darMockManejadorSeguridadUsuarioDeClienteConPermiso(permiso: PermisoBack): ManejadorSeguridad
{
    return mockConDefaultAnswer(ManejadorSeguridad::class.java).apply {
        doNothing().`when`(this).verificarUsuarioDeClienteActualTienePermiso(permiso)
    }
}

internal val mockManejadorSeguridadUsuarioGlobalSiempreEstaAutenticado =
        mockConDefaultAnswer(ManejadorSeguridad::class.java).apply {
            doNothing().`when`(this).verificarUsuarioGlobalEstaAutenticado()
        }


internal val mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado =
        mockConDefaultAnswer(ManejadorSeguridad::class.java).apply {
            doThrow(UsuarioNoAutenticado()).`when`(this).verificarUsuarioGlobalEstaAutenticado()
        }