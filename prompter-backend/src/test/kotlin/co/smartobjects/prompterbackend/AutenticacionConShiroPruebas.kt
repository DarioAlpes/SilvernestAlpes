package co.smartobjects.prompterbackend

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.persistencia.clientes.RepositorioClientes
import co.smartobjects.persistencia.clientes.RepositorioClientesSQL
import co.smartobjects.persistencia.h2.ConfiguracionPersistenciaH2EnMemoria
import co.smartobjects.persistencia.usuarios.RepositorioCredencialesGuardadasUsuario
import co.smartobjects.persistencia.usuarios.RepositorioUsuarios
import co.smartobjects.persistencia.usuarios.RepositorioUsuariosSQL
import co.smartobjects.persistencia.usuarios.roles.RepositorioRoles
import co.smartobjects.persistencia.usuarios.roles.RepositorioRolesSQL
import co.smartobjects.persistencia.usuariosglobales.RepositorioCredencialesGuardadasUsuarioGlobal
import co.smartobjects.persistencia.usuariosglobales.RepositorioUsuariosGlobales
import co.smartobjects.persistencia.usuariosglobales.RepositorioUsuariosGlobalesSQL
import co.smartobjects.prompterbackend.excepciones.CredencialesIncorrectas
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.seguridad.shiro.RealmRepositorioUsuarios
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoUsuarios
import co.smartobjects.prompterbackend.serviciosrest.usuariosglobales.RecursoUsuariosGlobales
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.usuarios.*
import co.smartobjects.red.modelos.usuariosglobales.ContraseñaUsuarioGlobalDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalPatchDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.apache.shiro.SecurityUtils
import org.apache.shiro.mgt.DefaultSecurityManager
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.client.HttpUrlConnectorProvider
import org.junit.jupiter.api.*
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AutenticacionConShiroPruebas
{
    companion object
    {
        internal const val ID_CLIENTE = 1L
        private val REPOSITORIO_CREDENCIALES_ORIGINAL: RepositorioCredencialesGuardadasUsuario
        private val REPOSITORIO_CREDENCIALES_GLOBAL_ORIGINAL: RepositorioCredencialesGuardadasUsuarioGlobal
        private val RECURSO_CLIENTES_ORIGINAL: RecursoClientes
        private val HASH_Y_SAL_PARA_CONTRASEÑA_123 by lazy {
            RealmRepositorioUsuarios.servicioHash.setHashIterations(50)
            RealmRepositorioUsuarios.hasherShiro.calcularHash("123".toCharArray())
        }

        private lateinit var RECURSO_PRUEBAS: RecursoConAutenticacion

        init
        {
            ConfiguracionAplicacionJersey.DEPENDENCIAS = DependenciasImpl(ConfiguracionPersistenciaH2EnMemoria("db_pruebas_backend"))
            ConfiguracionAplicacionJersey.inicializacionPorDefecto()
            REPOSITORIO_CREDENCIALES_ORIGINAL = ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS
            REPOSITORIO_CREDENCIALES_GLOBAL_ORIGINAL = ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES
            RECURSO_CLIENTES_ORIGINAL = ConfiguracionAplicacionJersey.RECURSO_CLIENTES
        }

        @[BeforeAll JvmStatic Suppress("unused")]
        fun agregarRecursoPruebas()
        {
            RECURSO_PRUEBAS = RecursoConAutenticacion()
            ConfiguracionAplicacionJersey.RECURSOS_ADICIONALES = listOf(RECURSO_PRUEBAS)
        }

        @[AfterAll JvmStatic Suppress("unused")]
        fun eliminarRecursoPruebas()
        {
            ConfiguracionAplicacionJersey.RECURSOS_ADICIONALES = listOf()
        }
    }

    private val nombreUsuario = "El usuario"

    private lateinit var server: HttpServer
    private lateinit var target: WebTarget

    private val mockRepositorioCredenciales = mockConDefaultAnswer(RepositorioCredencialesGuardadasUsuario::class.java)
    private val mockRepositorioUsuarios = mockConDefaultAnswer(RepositorioUsuarios::class.java)
    private val mockRepositorioCredencialesGlobales = mockConDefaultAnswer(RepositorioCredencialesGuardadasUsuarioGlobal::class.java)
    private val mockRepositorioUsuariosGlobales = mockConDefaultAnswer(RepositorioUsuariosGlobales::class.java)

    private lateinit var recursoUsuarios: RecursoUsuarios
    private lateinit var recursoUsuariosGlobales: RecursoUsuariosGlobales
    private lateinit var mockRecursoClientes: RecursoClientes

    @[BeforeEach Throws(Exception::class)]
    fun antesDeCadaTest()
    {
        doReturn(true)
            .`when`(mockRepositorioUsuariosGlobales)
            .inicializar()
        recursoUsuarios = RecursoUsuarios(ID_CLIENTE, mockRepositorioUsuarios, RECURSO_CLIENTES_ORIGINAL.manejadorSeguridad)
        recursoUsuariosGlobales = RecursoUsuariosGlobales(mockRepositorioUsuariosGlobales, RECURSO_CLIENTES_ORIGINAL.manejadorSeguridad)

        val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
        mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

        doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
        doReturn(recursoUsuarios).`when`(mockRecursoCliente).darRecursoUsuarios()

        ConfiguracionAplicacionJersey.RECURSO_CLIENTES = mockRecursoClientes
        ConfiguracionAplicacionJersey.RECURSO_USUARIOS_GLOBALES = recursoUsuariosGlobales

        server = PrompterBackend.arrancarServidor()
        target = ClientBuilder.newClient()
            .register(JacksonJaxbJsonProvider().apply {
                setMapper(ConfiguracionJackson.objectMapperDeJackson.apply { registerModule(Jaxrs2TypesModule()) })
            })
            .target(PrompterBackend.BASE_URI)
        RECURSO_PRUEBAS.idCliente = ID_CLIENTE
        RECURSO_PRUEBAS.manejadorSeguridad = RECURSO_CLIENTES_ORIGINAL.manejadorSeguridad
    }

    @[AfterEach Throws(Exception::class)]
    fun despuesDeCadaTest()
    {
        server.shutdownNow()
    }

    @[Nested DisplayName("Usuario normal")]
    inner class ParaUsuarioNormal
    {
        @[Nested DisplayName("Al hacer GET a recurso privado")]
        inner class GETDeRecursoPrivado
        {
            @[Nested DisplayName("Sin autenticación")]
            inner class SinAutenticacion
            {
                @Test
                fun retorna_401()
                {
                    val respuestaRecursoPrivado = target.path("recurso/privado-de-cliente").request().get()
                    assertEquals(401, respuestaRecursoPrivado.status)
                }
            }

            @[Nested DisplayName("Estando autenticado")]
            inner class ConAutenticacion
            {
                @Test
                fun retorna_recurso_correcto()
                {
                    println(HASH_Y_SAL_PARA_CONTRASEÑA_123)
                    doReturn(
                            Usuario.CredencialesGuardadas(
                                    Usuario
                                    (
                                            Usuario.DatosUsuario
                                            (
                                                    RECURSO_PRUEBAS.idCliente, nombreUsuario, "El nombre", "el@email.com", true
                                            ),
                                            setOf
                                            (
                                                    Rol
                                                    (
                                                            "examplerole",
                                                            "rol de ejemplo",
                                                            setOf(RECURSO_PRUEBAS.permisoNecesario)
                                                    )
                                            )
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                         )
                            )
                        .`when`(mockRepositorioCredenciales)
                        .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)

                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales

                    val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target.path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    var requestRecursoPrivado = target.path("recurso/privado-de-cliente").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val mensajeRespuestaRecursoPrivado = requestRecursoPrivado.get<String>(String::class.java)

                    assertEquals(
                            EntidadConAutenticacion(123, "Privado"),
                            ConfiguracionJackson
                                .objectMapperDeJackson
                                .readValue<EntidadConAutenticacion>(mensajeRespuestaRecursoPrivado, EntidadConAutenticacion::class.java)
                                )
                }

                @Test
                fun pero_con_permiso_con_diferente_endpoint_retorna_403()
                {
                    val permisoBack =
                            PermisoBack(
                                    RECURSO_PRUEBAS.permisoNecesario.idCliente,
                                    "otro endpoint",
                                    RECURSO_PRUEBAS.permisoNecesario.accion
                                       )

                    doReturn(
                            Usuario.CredencialesGuardadas(
                                    Usuario
                                    (
                                            Usuario.DatosUsuario
                                            (
                                                    RECURSO_PRUEBAS.idCliente,
                                                    nombreUsuario,
                                                    "El nombre",
                                                    "el@email.com",
                                                    true
                                            ),
                                            setOf(Rol("examplerole", "rol de ejemplo", setOf(permisoBack)))
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                         )
                            )
                        .`when`(mockRepositorioCredenciales)
                        .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                    val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    var requestRecursoPrivado = target.path("recurso/privado-de-cliente").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val respuestaRecursoPrivado = requestRecursoPrivado.get()

                    assertEquals(403, respuestaRecursoPrivado.status)
                }

                @Test
                fun usando_usuario_global_retorna_401()
                {
                    doReturn(
                            UsuarioGlobal.CredencialesGuardadas(
                                    UsuarioGlobal
                                    (
                                            UsuarioGlobal.DatosUsuario
                                            (
                                                    nombreUsuario, "El nombre", "el@email.com", true
                                            )
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                               )
                            )
                        .`when`(mockRepositorioCredencialesGlobales)
                        .buscarPorId(nombreUsuario)

                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = mockRepositorioCredencialesGlobales

                    val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))

                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    var requestRecursoPrivado = target.path("recurso/privado-de-cliente").request()

                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }

                    val respuestaRecursoPrivado = requestRecursoPrivado.get()

                    assertEquals(401, respuestaRecursoPrivado.status)
                }

                @Test
                fun para_permiso_con_diferente_cliente_403()
                {
                    val permisoBack =
                            PermisoBack(
                                    RECURSO_PRUEBAS.permisoNecesario.idCliente + 1,
                                    RECURSO_PRUEBAS.permisoNecesario.endPoint,
                                    RECURSO_PRUEBAS.permisoNecesario.accion
                                       )

                    doReturn(
                            Usuario.CredencialesGuardadas(
                                    Usuario
                                    (
                                            Usuario.DatosUsuario(
                                                    RECURSO_PRUEBAS.idCliente,
                                                    nombreUsuario,
                                                    "El nombre",
                                                    "el@email.com",
                                                    true
                                                                ),
                                            setOf(Rol("examplerole", "rol de ejemplo", setOf(permisoBack)))
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                         )
                            )
                        .`when`(mockRepositorioCredenciales)
                        .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                    val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    var requestRecursoPrivado = target.path("recurso/privado-de-cliente").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val respuestaRecursoPrivado = requestRecursoPrivado.get()

                    assertEquals(403, respuestaRecursoPrivado.status)
                }

                @Test
                fun para_permiso_con_diferente_accion_retorna_403()
                {
                    val permisoBack =
                            PermisoBack(
                                    RECURSO_PRUEBAS.permisoNecesario.idCliente,
                                    RECURSO_PRUEBAS.permisoNecesario.endPoint,
                                    PermisoBack.Accion.DELETE
                                       )
                    doReturn(
                            Usuario.CredencialesGuardadas(
                                    Usuario
                                    (
                                            Usuario.DatosUsuario(
                                                    RECURSO_PRUEBAS.idCliente,
                                                    nombreUsuario,
                                                    "El nombre",
                                                    "el@email.com",
                                                    true
                                                                ),
                                            setOf(Rol("examplerole", "rol de ejemplo", setOf(permisoBack)))
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                         )
                            )
                        .`when`(mockRepositorioCredenciales)
                        .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                    val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    var requestRecursoPrivado = target.path("recurso/privado-de-cliente").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val respuestaRecursoPrivado = requestRecursoPrivado.get()

                    assertEquals(403, respuestaRecursoPrivado.status)
                }

                @Test
                fun despues_de_hacer_logout_retorna_401()
                {
                    doReturn(
                            Usuario.CredencialesGuardadas(
                                    Usuario
                                    (
                                            Usuario.DatosUsuario
                                            (
                                                    RECURSO_PRUEBAS.idCliente, nombreUsuario, "El nombre", "el@email.com", true
                                            ),
                                            setOf
                                            (
                                                    Rol
                                                    (
                                                            "examplerole",
                                                            "rol de ejemplo",
                                                            setOf(RECURSO_PRUEBAS.permisoNecesario)
                                                    )
                                            )
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                         )
                            )
                        .`when`(mockRepositorioCredenciales)
                        .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                    val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    var requestLogout = target.path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/logout").request()
                    respuestaLogin.cookies.values.forEach {
                        requestLogout = requestLogout.cookie(it)
                    }
                    val respuestaLogout = requestLogout.get()
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogout.statusInfo.family)

                    // Se usan a proposito cookies de login para garantizar que son invalidadas
                    var requestRecursoPrivado = target.path("recurso/privado-de-cliente").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val respuestaRecursoPrivado = requestRecursoPrivado.get()
                    assertEquals(401, respuestaRecursoPrivado.status)
                }

                @Test
                fun al_desactivar_usuario_hace_logout_y_retorna_401()
                {
                    val rolConPermisoDePatch =
                            Rol(
                                    "examplerole",
                                    "rol de ejemplo",
                                    setOf(
                                            RECURSO_PRUEBAS.permisoNecesario,
                                            PermisoBack(RECURSO_PRUEBAS.idCliente, "Usuarios", PermisoBack.Accion.PATCH)
                                         )
                               )

                    val datosDeUsuario = Usuario.DatosUsuario(RECURSO_PRUEBAS.idCliente, nombreUsuario, "El nombre", "el@email.com", true)
                    val usuario = Usuario(datosDeUsuario, setOf(rolConPermisoDePatch))

                    doReturn(Usuario.CredencialesGuardadas(usuario, HASH_Y_SAL_PARA_CONTRASEÑA_123))
                        .`when`(mockRepositorioCredenciales)
                        .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)

                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales

                    val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    val entidadPatch = UsuarioPatchDTO(null, false)
                    doNothing()
                        .`when`(mockRepositorioUsuarios)
                        .actualizarCamposIndividuales(
                                RECURSO_PRUEBAS.idCliente,
                                nombreUsuario,
                                mapOf(Usuario.Campos.ACTIVO to Usuario.DatosUsuario.CampoActivo(entidadPatch.activo!!))
                                                     )

                    var peticionPatch =
                            target.path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario")
                                .request()
                                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                    respuestaLogin.cookies.values.forEach {
                        peticionPatch = peticionPatch.cookie(it)
                    }

                    val respuestaPatch = peticionPatch.method("PATCH", Entity.entity(entidadPatch, MediaType.APPLICATION_JSON_TYPE))

                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaPatch.statusInfo.family)

                    // Se usan a proposito cookies de login para garantizar que son invalidadas
                    var requestRecursoPrivado = target.path("recurso/privado-de-cliente").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val respuestaRecursoPrivado = requestRecursoPrivado.get()
                    assertEquals(401, respuestaRecursoPrivado.status)
                }
            }
        }

        @[Nested DisplayName("Al hacer login")]
        inner class Login
        {
            @Test
            fun retorna_usuario_correcto()
            {
                val usuarioEsperado = UsuarioDTO(
                        RECURSO_PRUEBAS.idCliente,
                        nombreUsuario,
                        "El nombre",
                        "el@email.com",
                        listOf
                        (
                                RolDTO
                                (
                                        "examplerole",
                                        "rol de ejemplo",
                                        listOf(PermisoBackDTO(RECURSO_PRUEBAS.permisoNecesario))
                                )
                        ),
                        true
                                                )
                doReturn(
                        Usuario.CredencialesGuardadas(
                                Usuario
                                (
                                        Usuario.DatosUsuario
                                        (
                                                RECURSO_PRUEBAS.idCliente, nombreUsuario, "El nombre", "el@email.com", true
                                        ),
                                        setOf
                                        (
                                                Rol
                                                (
                                                        "examplerole",
                                                        "rol de ejemplo",
                                                        setOf(RECURSO_PRUEBAS.permisoNecesario)
                                                )
                                        )
                                ),
                                HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                     )
                        )
                    .`when`(mockRepositorioCredenciales)
                    .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                val respuestaLogin =
                        target.path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                val usuario = respuestaLogin.readEntity(UsuarioDTO::class.java)
                assertEquals(usuarioEsperado, usuario)
            }

            @Test
            fun con_credenciales_incorrectas_retorna_403_e_intentar_consumir_recurso_privado_de_cliente_posteriormente_retorna_401()
            {
                doReturn(
                        Usuario.CredencialesGuardadas(
                                Usuario
                                (
                                        Usuario.DatosUsuario
                                        (
                                                RECURSO_PRUEBAS.idCliente, nombreUsuario, "El nombre", "el@email.com", true
                                        ),
                                        setOf
                                        (
                                                Rol
                                                (
                                                        "examplerole",
                                                        "rol de ejemplo",
                                                        setOf(RECURSO_PRUEBAS.permisoNecesario)
                                                )
                                        )
                                ),
                                HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                     )
                        )
                    .`when`(mockRepositorioCredenciales)
                    .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3', '4'))
                val respuestaLogin =
                        target
                            .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaLogin.status)

                var requestRecursoPrivado = target.path("recurso/privado-de-cliente").request()
                respuestaLogin.cookies.values.forEach {
                    requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                }
                val respuestaRecursoPrivado = requestRecursoPrivado.get()
                assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaRecursoPrivado.status)
            }

            @Test
            fun con_usuario_inexistente_o_desactivado_retorna_403_e_intentar_consumir_recurso_privado_de_cliente_posteriormente_retorna_401()
            {
                doReturn(null)
                    .`when`(mockRepositorioCredenciales)
                    .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                val respuestaLogin =
                        target.path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaLogin.status)

                var requestRecursoPrivado = target.path("recurso/privado-de-cliente").request()
                respuestaLogin.cookies.values.forEach {
                    requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                }
                val respuestaRecursoPrivado = requestRecursoPrivado.get()
                assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaRecursoPrivado.status)
            }

            @Test
            fun limpia_contraseña_recibida_por_parametro_aun_cuando_falla_la_autenticacion()
            {
                val contraseña = charArrayOf('1', '2', '3')
                val entidadDTO = ContraseñaUsuarioDTO(contraseña)
                val realm = RealmRepositorioUsuarios()
                val securityManager = DefaultSecurityManager(realm)
                SecurityUtils.setSecurityManager(securityManager)
                try
                {
                    recursoUsuarios.darRecursosEntidadEspecifica(nombreUsuario).login(entidadDTO)
                }
                catch (e: CredencialesIncorrectas)
                {
                }

                assertTrue(contraseña.all { it == '\u0000' })
                assertTrue(entidadDTO.contraseña.all { it == '\u0000' })
            }
        }

        @[Nested DisplayName("Al hacer logout")]
        inner class Logout
        {
            @Test
            fun sin_autenticarse_retorna_401()
            {
                val respuestaRecursoPrivado = target.path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/logout").request().get()
                assertEquals(401, respuestaRecursoPrivado.status)
            }

            @Test
            fun con_otro_usuario_retorna_403()
            {
                doReturn(
                        Usuario.CredencialesGuardadas(
                                Usuario
                                (
                                        Usuario.DatosUsuario
                                        (
                                                RECURSO_PRUEBAS.idCliente, nombreUsuario, "El nombre", "el@email.com", true
                                        ),
                                        setOf
                                        (
                                                Rol
                                                (
                                                        "examplerole",
                                                        "rol de ejemplo",
                                                        setOf(RECURSO_PRUEBAS.permisoNecesario)
                                                )
                                        )
                                ),
                                HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                     )
                        )
                    .`when`(mockRepositorioCredenciales)
                    .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                val respuestaLogin =
                        target
                            .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                var requestLogout = target.path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/otro usuario/logout").request()
                respuestaLogin.cookies.values.forEach {
                    requestLogout = requestLogout.cookie(it)
                }
                val respuestaLogout = requestLogout.get()
                assertEquals(403, respuestaLogout.status)
            }

            @Test
            fun con_usuario_global_retorna_401()
            {
                doReturn(
                        UsuarioGlobal.CredencialesGuardadas(
                                UsuarioGlobal
                                (
                                        UsuarioGlobal.DatosUsuario
                                        (
                                                nombreUsuario, "El nombre", "el@email.com", true
                                        )
                                ),
                                HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                           )
                        )
                    .`when`(mockRepositorioCredencialesGlobales)
                    .buscarPorId(nombreUsuario)

                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = mockRepositorioCredencialesGlobales

                val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3'))
                val respuestaLogin =
                        target
                            .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                var requestLogout =
                        target
                            .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/logout")
                            .request()

                respuestaLogin.cookies.values.forEach {
                    requestLogout = requestLogout.cookie(it)
                }

                val respuestaLogout = requestLogout.get()

                assertEquals(401, respuestaLogout.status)
            }

            @Test
            fun con_mismo_usuario_en_otro_cliente_retorna_401()
            {
                doReturn(
                        Usuario.CredencialesGuardadas(
                                Usuario
                                (
                                        Usuario.DatosUsuario
                                        (
                                                RECURSO_PRUEBAS.idCliente, nombreUsuario, "El nombre", "el@email.com", true
                                        ),
                                        setOf
                                        (
                                                Rol
                                                (
                                                        "examplerole",
                                                        "rol de ejemplo",
                                                        setOf(RECURSO_PRUEBAS.permisoNecesario)
                                                )
                                        )
                                ),
                                HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                     )
                        )
                    .`when`(mockRepositorioCredenciales)
                    .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                val respuestaLogin =
                        target
                            .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)

                doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE + 1)
                doReturn(RecursoUsuarios(ID_CLIENTE + 1, mockRepositorioUsuarios, RECURSO_PRUEBAS.manejadorSeguridad)).`when`(mockRecursoCliente).darRecursoUsuarios()

                var requestLogout = target.path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente + 1}/${RecursoUsuarios.RUTA}/$nombreUsuario/logout").request()
                respuestaLogin.cookies.values.forEach {
                    requestLogout = requestLogout.cookie(it)
                }
                val respuestaLogout = requestLogout.get()
                assertEquals(401, respuestaLogout.status)
            }
        }

        @[Nested DisplayName("Servicio de login")]
        inner class ServicioLogin
        {
            @Test
            fun funciona_al_loggearse_con_credenciales_correctas_al_usar_repositorios_reales()
            {
                ConfiguracionAplicacionJersey.RECURSO_CLIENTES = RECURSO_CLIENTES_ORIGINAL
                val repositorioClientes: RepositorioClientes = RepositorioClientesSQL(ConfiguracionAplicacionJersey.DEPENDENCIAS.configuracionRepositorios)
                try
                {
                    repositorioClientes.crearTablaSiNoExiste()
                    val repositorioRoles: RepositorioRoles = RepositorioRolesSQL(ConfiguracionAplicacionJersey.DEPENDENCIAS.configuracionRepositorios)
                    val repositorioUsuarios: RepositorioUsuarios =
                            RepositorioUsuariosSQL(
                                    ConfiguracionAplicacionJersey.DEPENDENCIAS.configuracionRepositorios,
                                    RealmRepositorioUsuarios.hasherShiro
                                                  )

                    val cliente = repositorioClientes.crear(Cliente(null, "Cliente pruebas"))
                    RECURSO_PRUEBAS.idCliente = cliente.id!!
                    RECURSO_PRUEBAS.permisoNecesario = PermisoBack(RECURSO_PRUEBAS.idCliente, RECURSO_PRUEBAS.permisoNecesario.endPoint, RECURSO_PRUEBAS.permisoNecesario.accion)
                    repositorioRoles.inicializarParaCliente(RECURSO_PRUEBAS.idCliente)
                    repositorioUsuarios.inicializarParaCliente(RECURSO_PRUEBAS.idCliente)
                    val rol = repositorioRoles.crear(RECURSO_PRUEBAS.idCliente,
                                                     Rol
                                                     (
                                                             "examplerole",
                                                             "rol de ejemplo",
                                                             setOf(RECURSO_PRUEBAS.permisoNecesario)
                                                     ))
                    val contraseñaCreacion = charArrayOf('1', '2', '3')
                    repositorioUsuarios.crear(
                            RECURSO_PRUEBAS.idCliente,
                            Usuario.UsuarioParaCreacion(
                                    Usuario.DatosUsuario(
                                            RECURSO_PRUEBAS.idCliente,
                                            nombreUsuario,
                                            "El nombre",
                                            "el@email.com",
                                            true
                                                        ),
                                    contraseñaCreacion,
                                    setOf(Rol.RolParaCreacionDeUsuario(rol.nombre))
                                                       )
                                             )
                    // La contraseña se limpia
                    assertTrue(contraseñaCreacion.all { it == '\u0000' })
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = REPOSITORIO_CREDENCIALES_ORIGINAL

                    val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                }
                finally
                {
                    repositorioClientes.limpiar()
                }
            }

            @Test
            fun falla_al_loggearse_con_credenciales_incorrectas_al_usar_repositorios_reales()
            {
                ConfiguracionAplicacionJersey.RECURSO_CLIENTES = RECURSO_CLIENTES_ORIGINAL
                val repositorioClientes: RepositorioClientes = RepositorioClientesSQL(ConfiguracionAplicacionJersey.DEPENDENCIAS.configuracionRepositorios)
                try
                {
                    repositorioClientes.crearTablaSiNoExiste()
                    val repositorioRoles: RepositorioRoles =
                            RepositorioRolesSQL(ConfiguracionAplicacionJersey.DEPENDENCIAS.configuracionRepositorios)
                    val repositorioUsuarios: RepositorioUsuarios =
                            RepositorioUsuariosSQL(
                                    ConfiguracionAplicacionJersey.DEPENDENCIAS.configuracionRepositorios,
                                    RealmRepositorioUsuarios.hasherShiro
                                                  )

                    val cliente = repositorioClientes.crear(Cliente(null, "Cliente pruebas"))
                    RECURSO_PRUEBAS.idCliente = cliente.id!!
                    RECURSO_PRUEBAS.permisoNecesario = PermisoBack(RECURSO_PRUEBAS.idCliente, RECURSO_PRUEBAS.permisoNecesario.endPoint, RECURSO_PRUEBAS.permisoNecesario.accion)
                    repositorioRoles.inicializarParaCliente(RECURSO_PRUEBAS.idCliente)
                    repositorioUsuarios.inicializarParaCliente(RECURSO_PRUEBAS.idCliente)

                    val rol = repositorioRoles.crear(
                            RECURSO_PRUEBAS.idCliente,
                            Rol("examplerole", "rol de ejemplo", setOf(RECURSO_PRUEBAS.permisoNecesario))
                                                    )

                    val contraseñaCreacion = charArrayOf('1', '2', '3')
                    repositorioUsuarios.crear(
                            RECURSO_PRUEBAS.idCliente,
                            Usuario.UsuarioParaCreacion(
                                    Usuario.DatosUsuario(
                                            RECURSO_PRUEBAS.idCliente,
                                            nombreUsuario,
                                            "El nombre",
                                            "el@email.com",
                                            true),
                                    contraseñaCreacion,
                                    setOf(Rol.RolParaCreacionDeUsuario(rol.nombre))
                                                       )
                                             )
                    // La contraseña se limpia
                    assertTrue(contraseñaCreacion.all { it == '\u0000' })
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = REPOSITORIO_CREDENCIALES_ORIGINAL

                    val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3', '4'))
                    val respuestaLogin =
                            target
                                .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaLogin.status)
                }
                finally
                {
                    repositorioClientes.limpiar()
                }
            }
        }
    }

    @[Nested DisplayName("Usuario global")]
    inner class ParaUsuarioGlobal
    {
        @[Nested DisplayName("Al hacer GET a recurso privado")]
        inner class GETDeRecursoPrivado
        {
            @[Nested DisplayName("Sin autenticación")]
            inner class SinAutenticacion
            {
                @Test
                fun retorna_401()
                {
                    val respuestaRecursoPrivado = target.path("recurso/privado-global").request().get()
                    assertEquals(401, respuestaRecursoPrivado.status)
                }
            }

            @[Nested DisplayName("Estando autenticado")]
            inner class ConAutenticacion
            {
                @Test
                fun retorna_recurso_correcto()
                {
                    doReturn(
                            UsuarioGlobal.CredencialesGuardadas(
                                    UsuarioGlobal
                                    (
                                            UsuarioGlobal.DatosUsuario
                                            (
                                                    nombreUsuario, "El nombre", "el@email.com", true
                                            )
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                               )
                            )
                        .`when`(mockRepositorioCredencialesGlobales)
                        .buscarPorId(nombreUsuario)
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = mockRepositorioCredencialesGlobales

                    val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))

                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    var requestRecursoPrivado = target.path("recurso/privado-global").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val mensajeRespuestaRecursoPrivado = requestRecursoPrivado.get<String>(String::class.java)

                    assertEquals(
                            EntidadConAutenticacion(123, "Privado"),
                            ConfiguracionJackson
                                .objectMapperDeJackson
                                .readValue<EntidadConAutenticacion>(mensajeRespuestaRecursoPrivado, EntidadConAutenticacion::class.java)
                                )
                }

                @Test
                fun con_usuario_de_cliente_retorna_401()
                {
                    doReturn(
                            Usuario.CredencialesGuardadas(
                                    Usuario
                                    (
                                            Usuario.DatosUsuario
                                            (
                                                    RECURSO_PRUEBAS.idCliente,
                                                    nombreUsuario,
                                                    "El nombre",
                                                    "el@email.com",
                                                    true
                                            ),
                                            setOf
                                            (
                                                    Rol("examplerole", "rol de ejemplo", setOf(RECURSO_PRUEBAS.permisoNecesario))
                                            )
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                         )
                            )
                        .`when`(mockRepositorioCredenciales)
                        .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)

                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales

                    val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))

                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    var requestRecursoPrivado = target.path("recurso/privado-global").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val respuestaRecursoPrivado = requestRecursoPrivado.get()

                    assertEquals(401, respuestaRecursoPrivado.status)
                }

                @Test
                fun despues_de_logout_retorna_401()
                {
                    doReturn(
                            UsuarioGlobal.CredencialesGuardadas(
                                    UsuarioGlobal
                                    (
                                            UsuarioGlobal.DatosUsuario
                                            (
                                                    nombreUsuario, "El nombre", "el@email.com", true
                                            )
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                               )
                            )
                        .`when`(mockRepositorioCredencialesGlobales)
                        .buscarPorId(nombreUsuario)
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = mockRepositorioCredencialesGlobales
                    val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin = target.path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login").request().post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    var requestLogout = target.path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/logout").request()
                    respuestaLogin.cookies.values.forEach {
                        requestLogout = requestLogout.cookie(it)
                    }
                    val respuestaLogout = requestLogout.get()
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogout.statusInfo.family)

                    // Se usan a proposito cookies de login para garantizar que son invalidadas
                    var requestRecursoPrivado = target.path("recurso/privado-global").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val respuestaRecursoPrivado = requestRecursoPrivado.get()
                    assertEquals(401, respuestaRecursoPrivado.status)
                }

                @Test
                fun al_desactivar_usuario_hace_logout_y_retorna_401()
                {
                    doReturn(
                            UsuarioGlobal.CredencialesGuardadas(
                                    UsuarioGlobal
                                    (
                                            UsuarioGlobal.DatosUsuario
                                            (
                                                    nombreUsuario, "El nombre", "el@email.com", true
                                            )
                                    ),
                                    HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                               )
                            )
                        .`when`(mockRepositorioCredencialesGlobales)
                        .buscarPorId(nombreUsuario)

                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = mockRepositorioCredencialesGlobales

                    val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                    val entidadPatch = UsuarioGlobalPatchDTO(null, false)
                    doNothing()
                        .`when`(mockRepositorioUsuariosGlobales)
                        .actualizarCamposIndividuales(
                                nombreUsuario,
                                mapOf(UsuarioGlobal.Campos.ACTIVO to UsuarioGlobal.DatosUsuario.CampoActivo(entidadPatch.activo!!))
                                                     )

                    var peticionPatch =
                            target.path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario")
                                .request()
                                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                    respuestaLogin.cookies.values.forEach {
                        peticionPatch = peticionPatch.cookie(it)
                    }

                    val respuestaPatch = peticionPatch.method("PATCH", Entity.entity(entidadPatch, MediaType.APPLICATION_JSON_TYPE))

                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaPatch.statusInfo.family)

                    // Se usan a proposito cookies de login para garantizar que son invalidadas
                    var requestRecursoPrivado = target.path("recurso/privado-global").request()
                    respuestaLogin.cookies.values.forEach {
                        requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                    }
                    val respuestaRecursoPrivado = requestRecursoPrivado.get()
                    assertEquals(401, respuestaRecursoPrivado.status)
                }
            }
        }

        @[Nested DisplayName("Al hacer login")]
        inner class Login
        {
            @Test
            fun retorna_usuario_correcto()
            {
                val usuarioEsperado = UsuarioGlobalDTO(UsuarioGlobal(UsuarioGlobal.DatosUsuario(nombreUsuario, "El nombre", "el@email.com", true)))

                doReturn(
                        UsuarioGlobal.CredencialesGuardadas(
                                UsuarioGlobal
                                (
                                        UsuarioGlobal.DatosUsuario
                                        (
                                                nombreUsuario, "El nombre", "el@email.com", true
                                        )
                                ),
                                HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                           )
                        )
                    .`when`(mockRepositorioCredencialesGlobales)
                    .buscarPorId(nombreUsuario)

                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = mockRepositorioCredencialesGlobales

                val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3'))
                val respuestaLogin =
                        target
                            .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))

                assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                val usuario = respuestaLogin.readEntity(UsuarioGlobalDTO::class.java)
                assertEquals(usuarioEsperado, usuario)
            }

            @Test
            fun con_credenciales_incorrectas_retorna_401_e_intentar_consumir_recurso_privado_global_posteriormente_retorna_401()
            {
                doReturn(
                        UsuarioGlobal.CredencialesGuardadas(
                                UsuarioGlobal
                                (
                                        UsuarioGlobal.DatosUsuario
                                        (
                                                nombreUsuario, "El nombre", "el@email.com", true
                                        )
                                ),
                                HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                           )
                        )
                    .`when`(mockRepositorioCredencialesGlobales)
                    .buscarPorId(nombreUsuario)
                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = mockRepositorioCredencialesGlobales
                val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3', '4'))
                val respuestaLogin =
                        target
                            .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaLogin.status)

                var requestRecursoPrivado = target.path("recurso/privado-global").request()
                respuestaLogin.cookies.values.forEach {
                    requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                }
                val respuestaRecursoPrivado = requestRecursoPrivado.get()
                assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaRecursoPrivado.status)
            }

            @Test
            fun con_usuario_inexistente_o_desactivado_retorna_401_e_intentar_consumir_recurso_privado_de_cliente_posteriormente_retorna_401()
            {
                doReturn(null)
                    .`when`(mockRepositorioCredencialesGlobales)
                    .buscarPorId(nombreUsuario)
                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = mockRepositorioCredencialesGlobales
                val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3'))
                val respuestaLogin =
                        target
                            .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaLogin.status)

                var requestRecursoPrivado = target.path("recurso/privado-global").request()
                respuestaLogin.cookies.values.forEach {
                    requestRecursoPrivado = requestRecursoPrivado.cookie(it)
                }
                val respuestaRecursoPrivado = requestRecursoPrivado.get()
                assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaRecursoPrivado.status)
            }

            @Test
            fun limpia_contraseña_recibida_por_parametro_aun_cuando_falla_la_autenticacion()
            {
                val contraseña = charArrayOf('1', '2', '3')
                val entidadDTO = ContraseñaUsuarioGlobalDTO(contraseña)
                val realm = RealmRepositorioUsuarios()
                val securityManager = DefaultSecurityManager(realm)
                SecurityUtils.setSecurityManager(securityManager)
                try
                {
                    recursoUsuariosGlobales.darRecursosEntidadEspecifica(nombreUsuario).login(entidadDTO)
                }
                catch (e: CredencialesIncorrectas)
                {
                }

                assertTrue(contraseña.all { it == '\u0000' })
                assertTrue(entidadDTO.contraseña.all { it == '\u0000' })
            }
        }

        @[Nested DisplayName("Al hacer logout")]
        inner class Logout
        {
            @Test
            fun sin_autenticarse_retorna_401()
            {
                val respuestaRecursoPrivado = target.path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/logout").request().get()
                assertEquals(401, respuestaRecursoPrivado.status)
            }

            @Test
            fun con_otro_usuario_retorna_403()
            {
                doReturn(
                        UsuarioGlobal.CredencialesGuardadas(
                                UsuarioGlobal
                                (
                                        UsuarioGlobal.DatosUsuario
                                        (
                                                nombreUsuario, "El nombre", "el@email.com", true
                                        )
                                ),
                                HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                           )
                        )
                    .`when`(mockRepositorioCredencialesGlobales)
                    .buscarPorId(nombreUsuario)

                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = mockRepositorioCredencialesGlobales

                val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3'))
                val respuestaLogin =
                        target
                            .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))

                assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                var requestLogout = target.path("${RecursoUsuariosGlobales.RUTA}/otro usuario/logout").request()

                respuestaLogin.cookies.values.forEach {
                    requestLogout = requestLogout.cookie(it)
                }

                val respuestaLogout = requestLogout.get()

                assertEquals(403, respuestaLogout.status)
            }

            @Test
            fun con_usuario_de_cliente_retorna_401()
            {
                doReturn(
                        Usuario.CredencialesGuardadas(
                                Usuario
                                (
                                        Usuario.DatosUsuario
                                        (
                                                RECURSO_PRUEBAS.idCliente, nombreUsuario, "El nombre", "el@email.com", true
                                        ),
                                        setOf
                                        (
                                                Rol("examplerole", "rol de ejemplo", setOf(RECURSO_PRUEBAS.permisoNecesario))
                                        )
                                ),
                                HASH_Y_SAL_PARA_CONTRASEÑA_123
                                                     )
                        )
                    .`when`(mockRepositorioCredenciales)
                    .buscarPorId(RECURSO_PRUEBAS.idCliente, nombreUsuario)
                ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS = mockRepositorioCredenciales
                val credenciales = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
                val respuestaLogin =
                        target
                            .path("${RecursoClientes.RUTA}/${RECURSO_PRUEBAS.idCliente}/${RecursoUsuarios.RUTA}/$nombreUsuario/login")
                            .request()
                            .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                var requestLogout = target.path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/logout").request()
                respuestaLogin.cookies.values.forEach {
                    requestLogout = requestLogout.cookie(it)
                }
                val respuestaLogout = requestLogout.get()
                assertEquals(401, respuestaLogout.status)
            }
        }

        @[Nested DisplayName("Servicio de login")]
        inner class ServicioLogin
        {
            @Test
            fun funciona_al_loggearse_con_credenciales_correctas_al_usar_repositorios_reales()
            {
                ConfiguracionAplicacionJersey.RECURSO_CLIENTES = RECURSO_CLIENTES_ORIGINAL
                val repositorioUsuariosGlobales: RepositorioUsuariosGlobales =
                        RepositorioUsuariosGlobalesSQL(
                                ConfiguracionAplicacionJersey.DEPENDENCIAS.configuracionRepositorios,
                                RealmRepositorioUsuarios.hasherShiro
                                                      )
                try
                {
                    repositorioUsuariosGlobales.inicializar()
                    val contraseñaCreacion = charArrayOf('1', '2', '3')
                    repositorioUsuariosGlobales.crear(
                            UsuarioGlobal.UsuarioParaCreacion(
                                    UsuarioGlobal.DatosUsuario(
                                            nombreUsuario,
                                            "El nombre",
                                            "el@email.com",
                                            true
                                                              ),
                                    contraseñaCreacion
                                                             )
                                                     )
                    // La contraseña se limpia
                    assertTrue(contraseñaCreacion.all { it == '\u0000' })
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = REPOSITORIO_CREDENCIALES_GLOBAL_ORIGINAL

                    val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3'))
                    val respuestaLogin =
                            target
                                .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))

                    assertEquals(Response.Status.Family.SUCCESSFUL, respuestaLogin.statusInfo.family)

                }
                finally
                {
                    repositorioUsuariosGlobales.limpiar()
                }
            }

            @Test
            fun falla_al_loggearse_con_credenciales_incorrectas_al_usar_repositorios_reales()
            {
                ConfiguracionAplicacionJersey.RECURSO_CLIENTES = RECURSO_CLIENTES_ORIGINAL
                val repositorioUsuariosGlobales: RepositorioUsuariosGlobales =
                        RepositorioUsuariosGlobalesSQL(
                                ConfiguracionAplicacionJersey.DEPENDENCIAS.configuracionRepositorios,
                                RealmRepositorioUsuarios.hasherShiro
                                                      )
                try
                {
                    repositorioUsuariosGlobales.inicializar()
                    val contraseñaCreacion = charArrayOf('1', '2', '3')
                    repositorioUsuariosGlobales.crear(
                            UsuarioGlobal.UsuarioParaCreacion(
                                    UsuarioGlobal.DatosUsuario(
                                            nombreUsuario,
                                            "El nombre",
                                            "el@email.com",
                                            true
                                                              ),
                                    contraseñaCreacion
                                                             )
                                                     )
                    // La contraseña se limpia
                    assertTrue(contraseñaCreacion.all { it == '\u0000' })
                    ConfiguracionAplicacionJersey.REPOSITORIO_CREDENCIALES_GUARDADAS_GLOBALES = REPOSITORIO_CREDENCIALES_GLOBAL_ORIGINAL

                    val credenciales = ContraseñaUsuarioGlobalDTO(charArrayOf('1', '2', '3', '4'))
                    val respuestaLogin =
                            target
                                .path("${RecursoUsuariosGlobales.RUTA}/$nombreUsuario/login")
                                .request()
                                .post(Entity.entity(credenciales, MediaType.APPLICATION_JSON_TYPE))
                    assertEquals(Response.Status.UNAUTHORIZED.statusCode, respuestaLogin.status)
                }
                finally
                {
                    repositorioUsuariosGlobales.limpiar()
                }
            }
        }
    }
}

@Path("recurso")
internal class RecursoConAutenticacion
{
    internal var idCliente = AutenticacionConShiroPruebas.ID_CLIENTE
    internal var permisoNecesario = PermisoBack(idCliente, "elEndpoint", PermisoBack.Accion.PUT)
    internal lateinit var manejadorSeguridad: ManejadorSeguridad

    @[GET Path("privado-de-cliente") Produces(MediaType.APPLICATION_JSON)]
    fun darRecursoPrivadoDeCliente(): EntidadConAutenticacion
    {
        manejadorSeguridad.verificarUsuarioDeClienteActualTienePermiso(permisoNecesario)
        return EntidadConAutenticacion(123, "Privado")
    }

    @[GET Path("privado-global") Produces(MediaType.APPLICATION_JSON)]
    fun darRecursoPrivadoGlobal(): EntidadConAutenticacion
    {
        manejadorSeguridad.verificarUsuarioGlobalEstaAutenticado()
        return EntidadConAutenticacion(123, "Privado")
    }
}

internal data class EntidadConAutenticacion(
        @get:JsonProperty("id")
        val id: Long?,

        @get:JsonProperty("nombre", required = true)
        val nombre: String
                                           )
{
    @Suppress("unused")
    @JsonCreator
    internal constructor(nombre: String) : this(null, nombre)
}