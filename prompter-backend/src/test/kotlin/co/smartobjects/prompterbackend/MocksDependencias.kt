package co.smartobjects.prompterbackend

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.usuarios.RepositorioCredencialesGuardadasUsuario
import co.smartobjects.persistencia.usuariosglobales.RepositorioCredencialesGuardadasUsuarioGlobal
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuariosglobales.RecursoUsuariosGlobales
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn

internal fun mockearConfiguracionAplicacionJerseyParaNoUsarConfiguracionRepositorios(mockRecursoClientes: RecursoClientes)
{
    val mockConfiguracionRepositorios = mockConDefaultAnswer(Dependencias::class.java).also {
        doReturn(mockConDefaultAnswer(ConfiguracionRepositorios::class.java)).`when`(it).configuracionRepositorios
    }

    ConfiguracionAplicacionJersey.DEPENDENCIAS = mockConfiguracionRepositorios

    ConfiguracionAplicacionJersey.RECURSO_CLIENTES = mockRecursoClientes

    ConfiguracionAplicacionJersey.RECURSO_USUARIOS_GLOBALES =
            mockConDefaultAnswer(RecursoUsuariosGlobales::class.java).also {
                doNothing().`when`(it).inicializar()
            }

    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS =
            mockConDefaultAnswer(RepositorioCredencialesGuardadasUsuario::class.java)

    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES =
            mockConDefaultAnswer(RepositorioCredencialesGuardadasUsuarioGlobal::class.java)
}