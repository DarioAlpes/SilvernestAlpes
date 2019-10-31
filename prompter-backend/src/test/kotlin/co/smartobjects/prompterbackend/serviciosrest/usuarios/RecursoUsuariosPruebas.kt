package co.smartobjects.prompterbackend.serviciosrest.usuarios

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.usuarios.RepositorioUsuarios
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.usuarios.*
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@DisplayName("Recurso Usuarios")
internal class RecursoUsuariosPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_ENTIDAD_PRUEBAS = "usuario_pruebas_1"
        private val CONTRASEÑA_PRUEBAS = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Usuario
    {
        return Usuario(
                Usuario.DatosUsuario(ID_CLIENTE, "usuario_pruebas_$indice", "nombre_completo_$indice", "email_$indice.com", indice % 2 == 0),
                setOf(
                        Rol(
                                "rol_pruebas",
                                "descripcion_rol",
                                setOf(
                                        PermisoBack(
                                                ID_CLIENTE,
                                                "endpoint_pruebas",
                                                PermisoBack.Accion.values()[indice % PermisoBack.Accion.values().size]
                                                   )
                                     )
                           )
                     )
                      )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): UsuarioDTO
    {
        return UsuarioDTO(darEntidadNegocioSegunIndice(indice))
    }

    private fun darEntidadNegocioParaCreacionSegunIndiceYContraseña(indice: Int, contraseña: CharArray): Usuario.UsuarioParaCreacion
    {
        return Usuario.UsuarioParaCreacion(
                Usuario.DatosUsuario(ID_CLIENTE, "usuario_pruebas_$indice", "nombre_completo_$indice", "email_$indice.com", indice % 2 == 0),
                contraseña,
                setOf(Rol.RolParaCreacionDeUsuario("rol_pruebas"))
                                          )
    }

    private fun darEntidadDTOParaCreacionSegunIndiceYContraseña(indice: Int, contraseña: CharArray): UsuarioParaCreacionDTO
    {
        return UsuarioParaCreacionDTO(darEntidadNegocioParaCreacionSegunIndiceYContraseña(indice, contraseña))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoUsuarios
        private lateinit var mockRecursoEntidadEspecifica: RecursoUsuarios.RecursoUsuario

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoEntidadEspecifica = mockConDefaultAnswer(RecursoUsuarios.RecursoUsuario::class.java)
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoUsuarios::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoUsuarios()
            doReturn(mockRecursoEntidadEspecifica).`when`(mockRecursoTodasEntidades).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)

            mockearConfiguracionAplicacionJerseyParaNoUsarConfiguracionRepositorios(mockRecursoClientes)

            server = PrompterBackend.arrancarServidor()
            target = ClientBuilder.newClient()
                .register(JacksonJaxbJsonProvider().apply {
                    setMapper(ConfiguracionJackson.objectMapperDeJackson.apply { registerModule(Jaxrs2TypesModule()) })
                })
                .target(PrompterBackend.BASE_URI)
        }

        @[AfterEach Throws(Exception::class)]
        fun despuesDeCadaTest()
        {
            server.shutdownNow()
        }

        @[Nested DisplayName("Al consultar todos")]
        inner class ConsultarTodos
        {
            @Test
            fun llama_la_funcion_darTodas()
            {
                doReturn(sequenceOf<UsuarioDTO>()).`when`(mockRecursoTodasEntidades).darTodas()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUsuarios.RUTA}").request().get(String::class.java)

                verify(mockRecursoTodasEntidades).darTodas()
            }
        }

        @[Nested DisplayName("Al crear")]
        inner class Crear
        {
            @Test
            fun llama_la_funcion_crear_con_dto_correcto()
            {
                val contraseña = CONTRASEÑA_PRUEBAS.copyOf()
                val dtoPruebas = darEntidadDTOParaCreacionSegunIndiceYContraseña(1, contraseña)
                val entidadPruebas = darEntidadNegocioSegunIndice(1)
                doReturn(UsuarioDTO(entidadPruebas)).`when`(mockRecursoTodasEntidades).crear(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUsuarios.RUTA}").request()
                    .post(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), UsuarioDTO::class.java)

                verify(mockRecursoTodasEntidades).crear(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al actualizar")]
        inner class Actualizar
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val contraseña = CONTRASEÑA_PRUEBAS.copyOf()
                val dtoPruebas = darEntidadDTOParaCreacionSegunIndiceYContraseña(1, contraseña)
                val entidadPruebas = darEntidadNegocioSegunIndice(1)
                doReturn(UsuarioDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUsuarios.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .put(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), UsuarioDTO::class.java)

                verify(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al hacer patch")]
        inner class Patch
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val contraseña = CONTRASEÑA_PRUEBAS.copyOf()
                val entidadPatch = UsuarioPatchDTO(contraseña, true)
                doNothing().`when`(mockRecursoEntidadEspecifica).patch(entidadPatch)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUsuarios.RUTA}/$ID_ENTIDAD_PRUEBAS")
                    .request()
                    .patch(entidadPatch, UsuarioPatchDTO::class.java)

                verify(mockRecursoEntidadEspecifica).patch(entidadPatch)
            }
        }

        @[Nested DisplayName("Al consultar una")]
        inner class ConsultarUna
        {
            @Test
            fun llama_la_funcion_darPorId_con_dto_correcto()
            {
                val entidadPruebas = darEntidadNegocioSegunIndice(1)
                doReturn(UsuarioDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).darPorId()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUsuarios.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .get(UsuarioDTO::class.java)

                verify(mockRecursoEntidadEspecifica).darPorId()
            }
        }

        @[Nested DisplayName("Al eliminar")]
        inner class Eliminar
        {
            @Test
            fun llama_la_funcion_eliminarPorId_con_dto_correcto()
            {
                doNothing().`when`(mockRecursoEntidadEspecifica).eliminarPorId()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUsuarios.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .delete()

                verify(mockRecursoEntidadEspecifica).eliminarPorId()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioUsuarios::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoUsuarios by lazy { RecursoUsuarios(ID_CLIENTE, mockRepositorio, mockManejadorSeguridad) }
        private val recursoEntidadEspecifica: RecursoUsuarios.RecursoUsuario by lazy { recursoTodasEntidades.darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS) }


        @Test
        fun al_crear_usuario_para_configuracion_inicial_se_crea_en_el_repositorio_un_usuario_con_el_rol_para_configuracion_inicial_y_credenciales_por_defecto()
        {
            doReturn(mockConDefaultAnswer(Usuario::class.java))
                .`when`(mockRepositorio)
                .crear(ArgumentMatchers.eq(ID_CLIENTE), cualquiera())

            val usuarioEsperado =
                    Usuario.UsuarioParaCreacion(
                            Usuario.DatosUsuario(0, "configuracion_inicial", "Usuario Para Configuración Inicial", "correo_no_existente@smartobjects.co", true),
                            "configuracion_inicial".toCharArray().copyOf(),
                            setOf(Rol.RolParaCreacionDeUsuario(RecursoRoles.NOMBRE_ROL_PARA_CONFIGURACION))
                                               )

            recursoTodasEntidades.crearUsuarioParaConfiguracionInicial()

            verify(mockRepositorio).crear(ID_CLIENTE, usuarioEsperado)
        }

        @Nested
        @DisplayName("Al crear")
        inner class Crear
        {
            private lateinit var contraseñaPrueba: CharArray
            private lateinit var entidadNegocio: Usuario
            private lateinit var entidadNegocioCreacion: Usuario.UsuarioParaCreacion
            private lateinit var entidadDTO: UsuarioDTO
            private lateinit var entidadDTOCreacion: UsuarioParaCreacionDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                contraseñaPrueba = CONTRASEÑA_PRUEBAS.copyOf()
                entidadNegocio = darEntidadNegocioSegunIndice(1)
                entidadNegocioCreacion = darEntidadNegocioParaCreacionSegunIndiceYContraseña(1, contraseñaPrueba)
                entidadDTOCreacion = darEntidadDTOParaCreacionSegunIndiceYContraseña(1, contraseñaPrueba)
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_crea_la_entidad_y_limpia_la_contraseña()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocioCreacion)

                val entidadRetornada = recursoTodasEntidades.crear(entidadDTOCreacion)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocioCreacion)

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea_y_limpia_la_contraseña()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocioCreacion)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoTodasEntidades.crear(entidadDTOCreacion) }

                assertEquals(UsuarioDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocioCreacion)

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste_y_limpia_la_contraseña()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocioCreacion)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.crear(entidadDTOCreacion) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocioCreacion)

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun usuario_duplicado_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad_y_limpia_la_contraseña()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Usuario"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocioCreacion)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion) }

                    assertEquals(UsuarioDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocioCreacion)

                    // Verificar contraseña limpiada
                    assertTrue(contraseñaPrueba.all { it == '\u0000' })
                    assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                    assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad_y_limpia_la_contraseña()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Usuario"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocioCreacion)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion) }

                    assertEquals(UsuarioDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocioCreacion)

                    // Verificar contraseña limpiada
                    assertTrue(contraseñaPrueba.all { it == '\u0000' })
                    assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                    assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                }


                @Nested
                @DisplayName("Usuario inválido")
                inner class UsuarioInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_usuario_vacio_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(usuario = "")) }

                        assertEquals(UsuarioDTO.CodigosError.USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_usuario_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(usuario = "      ")) }

                        assertEquals(UsuarioDTO.CodigosError.USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }

                @Nested
                @DisplayName("Nombre inválido")
                inner class NombreInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_nombre_vacio_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(nombreCompleto = "")) }

                        assertEquals(UsuarioDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_nombre_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(nombreCompleto = "      ")) }

                        assertEquals(UsuarioDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }

                @Nested
                @DisplayName("Email inválido")
                inner class EmailInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_email_vacio_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(email = "")) }

                        assertEquals(UsuarioDTO.CodigosError.EMAIL_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_email_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(email = "      ")) }

                        assertEquals(UsuarioDTO.CodigosError.EMAIL_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }

                @Nested
                @DisplayName("Contraseña inválida")
                inner class ContraseñaInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_contraseña_vacia()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(contraseña = charArrayOf())) }

                        assertEquals(UsuarioDTO.CodigosError.CONTRASEÑA_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)
                    }
                }

                @Nested
                @DisplayName("Roles inválidos")
                inner class RolesInvalidos
                {
                    @Test
                    fun cuando_el_dto_tiene_roles_vacios_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(roles = listOf())) }

                        assertEquals(UsuarioDTO.CodigosError.ROLES_INVALIDOS, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }

                @Nested
                @DisplayName("Rol inválido")
                inner class RolInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_rol_vacio_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(roles = listOf(""))) }

                        assertEquals(RolDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_rol_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(roles = listOf("      "))) }

                        assertEquals(RolDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Usuarios", PermisoBack.Accion.POST)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocioCreacion)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.crear(entidadDTOCreacion)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocioCreacion)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.crear(entidadDTOCreacion) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.crear(entidadDTOCreacion) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocioCreacion)
                }
            }
        }

        @Nested
        @DisplayName("Al consultar todos")
        inner class ConsultarTodos
        {
            @Test
            fun retorna_una_lista_vacia_de_dtos_cuando_el_repositorio_retorna_una_lista_vacia()
            {
                doReturn(sequenceOf<Usuario>())
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertTrue(listaRetornada.none())
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun retorna_una_lista_de_dtos_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                val listaNegocio = List(5) { darEntidadNegocioSegunIndice(it) }.asSequence()
                val listaDTO = List(5) { darEntidadDTOSegunIndice(it) }
                doReturn(listaNegocio)
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Usuario"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.darTodas() }

                assertEquals(UsuarioDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.darTodas() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Usuarios", PermisoBack.Accion.GET_TODOS)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(sequenceOf<Usuario>())
                        .`when`(mockRepositorio)
                        .listar(ID_CLIENTE)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.darTodas()
                    verify(mockRepositorio).listar(ID_CLIENTE)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.darTodas() }
                    verify(mockRepositorio, times(0)).listar(ID_CLIENTE)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.darTodas() }
                    verify(mockRepositorio, times(0)).listar(ID_CLIENTE)
                }
            }
        }

        @Nested
        @DisplayName("Al consultar uno")
        inner class ConsultarUno
        {
            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_retorna_un_entidad()
            {
                val entidadNegocio = darEntidadNegocioSegunIndice(1)
                val entidadDTO = darEntidadDTOSegunIndice(1)
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val entidadRetornada = recursoEntidadEspecifica.darPorId()

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_correcto_cuando_el_repositorio_retorna_null()
            {
                doReturn(null)
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                assertEquals(UsuarioDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Ubicación"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<ErrorDesconocido> { recursoEntidadEspecifica.darPorId() }

                assertEquals(UsuarioDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Usuarios", PermisoBack.Accion.GET_UNO)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    val entidadNegocio = darEntidadNegocioSegunIndice(1)
                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.darPorId()
                    verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.darPorId() }
                    verify(mockRepositorio, times(0)).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.darPorId() }
                    verify(mockRepositorio, times(0)).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                }
            }
        }

        @Nested
        @DisplayName("Al actualizar")
        inner class Actualizar
        {
            private lateinit var contraseñaPrueba: CharArray
            private lateinit var entidadNegocio: Usuario
            private lateinit var entidadNegocioCreacion: Usuario.UsuarioParaCreacion
            private lateinit var entidadDTO: UsuarioDTO
            private lateinit var entidadDTOCreacion: UsuarioParaCreacionDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                contraseñaPrueba = CONTRASEÑA_PRUEBAS.copyOf()
                entidadNegocio = darEntidadNegocioSegunIndice(1)
                entidadNegocioCreacion = darEntidadNegocioParaCreacionSegunIndiceYContraseña(1, contraseñaPrueba)
                entidadDTOCreacion = darEntidadDTOParaCreacionSegunIndiceYContraseña(1, contraseñaPrueba)
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_actualiza_la_entidad_y_limpia_la_contraseña()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTOCreacion)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
            }

            @Test
            fun usa_el_id_de_la_ruta_cuando_el_id_de_la_entidad_no_coincide_con_el_de_la_ruta_y_limpia_la_contraseña()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(usuario = "Otro usuario"))

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea_y_limpia_la_contraseña()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }

                assertEquals(UsuarioDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_no_existe_cuando_el_repositorio_lanza_EntidadNoExiste_y_limpia_la_contraseña()
            {
                doThrow(co.smartobjects.persistencia.excepciones.EntidadNoExiste("Otro usuario", "no importa"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }

                assertEquals(UsuarioDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste_y_limpia_la_contraseña()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun nombre_duplicado_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad_y_limpia_la_contraseña()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Ubicación"))
                        .`when`(mockRepositorio)
                        .actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }

                    assertEquals(UsuarioDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                    // Verificar contraseña limpiada
                    assertTrue(contraseñaPrueba.all { it == '\u0000' })
                    assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                    assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad_y_limpia_la_contraseña()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Usuario"))
                        .`when`(mockRepositorio)
                        .actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }

                    assertEquals(UsuarioDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                    // Verificar contraseña limpiada
                    assertTrue(contraseñaPrueba.all { it == '\u0000' })
                    assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                    assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                }

                @Nested
                @DisplayName("Usuario inválido")
                inner class UsuarioInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_usuario_vacio_y_limpia_la_contraseña()
                    {
                        val recursoEntidadEspecificaUsuarioInvalido = recursoTodasEntidades.darRecursosEntidadEspecifica("")
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecificaUsuarioInvalido.actualizar(entidadDTOCreacion.copy(usuario = "")) }

                        assertEquals(UsuarioDTO.CodigosError.USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, "", entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_usuario_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val recursoEntidadEspecificaUsuarioInvalido = recursoTodasEntidades.darRecursosEntidadEspecifica("            ")
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecificaUsuarioInvalido.actualizar(entidadDTOCreacion.copy(usuario = "            ")) }

                        assertEquals(UsuarioDTO.CodigosError.USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, "            ", entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }

                @Nested
                @DisplayName("Nombre inválido")
                inner class NombreInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_nombre_vacio_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(nombreCompleto = "")) }

                        assertEquals(UsuarioDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_nombre_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(nombreCompleto = "      ")) }

                        assertEquals(UsuarioDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }

                @Nested
                @DisplayName("Email inválido")
                inner class EmailInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_email_vacio_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(email = "")) }

                        assertEquals(UsuarioDTO.CodigosError.EMAIL_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_email_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(email = "      ")) }

                        assertEquals(UsuarioDTO.CodigosError.EMAIL_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }

                @Nested
                @DisplayName("Contraseña inválida")
                inner class ContraseñaInvalida
                {
                    @Test
                    fun cuando_el_dto_tiene_contraseña_vacia()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(contraseña = charArrayOf())) }

                        assertEquals(UsuarioDTO.CodigosError.CONTRASEÑA_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                    }
                }

                @Nested
                @DisplayName("Roles inválidos")
                inner class RolesInvalidos
                {
                    @Test
                    fun cuando_el_dto_tiene_roles_vacios_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(roles = listOf())) }

                        assertEquals(UsuarioDTO.CodigosError.ROLES_INVALIDOS, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }

                @Nested
                @DisplayName("Rol inválido")
                inner class RolInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_rol_vacio_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(roles = listOf(""))) }

                        assertEquals(RolDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_rol_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(roles = listOf("            "))) }

                        assertEquals(RolDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Usuarios", PermisoBack.Accion.PUT)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.actualizar(entidadDTOCreacion)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }
                    verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso_y_esta_logeado_con_usuario_correspondiente_a_la_ruta()
                {
                    doReturn(entidadNegocio)
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }
                    verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso_y_esta_logeado_con_otro_usuario()
                {
                    doReturn(entidadNegocio.copiar(datosUsuario = entidadNegocio.datosUsuario.copiar(usuario = "Otro usuario")))
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }
                    verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso_y_esta_logeado_con_mismo_usuario_pero_en_otro_cliente()
                {
                    doReturn(entidadNegocio.copiar(datosUsuario = entidadNegocio.datosUsuario.copiar(idCliente = ID_CLIENTE + 1)))
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }
                    verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                }
            }
        }

        @Nested
        @DisplayName("Al hacer patch")
        inner class Patch
        {
            private lateinit var contraseñaPrueba: CharArray
            private lateinit var entidadNegocio: Usuario
            private lateinit var entidadDTO: UsuarioDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                contraseñaPrueba = CONTRASEÑA_PRUEBAS.copyOf()
                entidadNegocio = darEntidadNegocioSegunIndice(1)
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Nested
            @DisplayName("Al verificar si toca validar permiso")
            inner class ValidarPermiso
            {
                @Nested
                @DisplayName("para mismo usuario")
                inner class MismoUsuario
                {
                    @BeforeEach
                    fun asignarMismoUsuarioQueElDelRecurso()
                    {
                        val datosUsuarioDuplicados =
                                entidadNegocio.datosUsuario.copiar(
                                        idCliente = ID_CLIENTE,
                                        usuario = ID_ENTIDAD_PRUEBAS
                                                                  )

                        doReturn(entidadNegocio.copiar(datosUsuario = datosUsuarioDuplicados))
                            .`when`(mockManejadorSeguridad)
                            .darUsuarioDeClienteAutenticado()
                    }

                    @Test
                    fun si_solo_envio_contraseña_no_valida_permiso()
                    {
                        val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, null)

                        val retorno =
                                recursoEntidadEspecifica
                                    .seDebeValidarPermiso(
                                            mapOf(
                                                    Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!)
                                                 )
                                                         )

                        assertFalse(retorno)
                    }

                    @Test
                    fun si_envio_algo_adicional_a_contraseña_valida_permiso()
                    {
                        val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, true)

                        val retorno =
                                recursoEntidadEspecifica
                                    .seDebeValidarPermiso(
                                            mapOf(
                                                    Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!),
                                                    Usuario.Campos.ACTIVO to Usuario.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                                 )
                                                         )

                        assertTrue(retorno)
                    }
                }

                @Nested
                @DisplayName("para usuario diferente")
                inner class UsuarioDiferente
                {
                    @BeforeEach
                    fun asignarUsuarioDiferenteAlDelRecurso()
                    {
                        val datosUsuarioDuplicados =
                                entidadNegocio.datosUsuario.copiar(
                                        idCliente = ID_CLIENTE + 1,
                                        usuario = ID_ENTIDAD_PRUEBAS + "asdf"
                                                                  )

                        doReturn(entidadNegocio.copiar(datosUsuario = datosUsuarioDuplicados))
                            .`when`(mockManejadorSeguridad)
                            .darUsuarioDeClienteAutenticado()
                    }

                    @Test
                    fun si_solo_envio_contraseña_valida_permiso()
                    {
                        val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, null)

                        val retorno =
                                recursoEntidadEspecifica
                                    .seDebeValidarPermiso(
                                            mapOf(
                                                    Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!)
                                                 )
                                                         )

                        assertTrue(retorno)
                    }

                    @Test
                    fun si_envio_algo_adicional_a_contraseña_valida_permiso()
                    {
                        val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, true)

                        val retorno =
                                recursoEntidadEspecifica
                                    .seDebeValidarPermiso(
                                            mapOf(
                                                    Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!),
                                                    Usuario.Campos.ACTIVO to Usuario.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                                 )
                                                         )

                        assertTrue(retorno)
                    }
                }
            }

            @Test
            fun funciona_correctamente_cuando_el_repositorio_actualiza_la_entidad_y_limpia_la_contraseña()
            {
                val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, true)

                doNothing()
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.usuario,
                            mapOf(
                                    Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!),
                                    Usuario.Campos.ACTIVO to Usuario.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                 )
                                                 )

                doReturn(entidadNegocio)
                    .`when`(mockManejadorSeguridad)
                    .darUsuarioDeClienteAutenticado()

                recursoEntidadEspecifica.patch(entidadPatch)

                verify(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.usuario,
                            mapOf(
                                    Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!),
                                    Usuario.Campos.ACTIVO to Usuario.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                 )
                                                 )

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadPatch.contraseña!!.all { it == '\u0000' })
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste_y_limpia_la_contraseña()
            {
                val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, null)

                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.usuario,
                            mapOf(Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(contraseñaPrueba))
                                                 )

                doReturn(entidadNegocio)
                    .`when`(mockManejadorSeguridad)
                    .darUsuarioDeClienteAutenticado()

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.patch(entidadPatch) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio)
                    .actualizarCamposIndividuales(
                            ID_CLIENTE,
                            entidadDTO.usuario,
                            mapOf(Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(contraseñaPrueba))
                                                 )

                // Verificar contraseña limpiada
                assertTrue(contraseñaPrueba.all { it == '\u0000' })
                assertTrue(entidadPatch.contraseña!!.all { it == '\u0000' })
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad_y_limpia_la_contraseña()
                {
                    val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, null)

                    doThrow(ErrorDeCreacionActualizacionEntidad("no importa"))
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!)
                                     )
                                                     )

                    doReturn(entidadNegocio)
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(UsuarioDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!)
                                     )
                                                     )

                    // Verificar contraseña limpiada
                    assertTrue(contraseñaPrueba.all { it == '\u0000' })
                    assertTrue(entidadPatch.contraseña!!.all { it == '\u0000' })
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Usuarios", PermisoBack.Accion.PATCH)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion_con_otro_usuario()
                {
                    val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, null)
                    doNothing()
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!)
                                     )
                                                     )
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    doReturn(entidadNegocio.copiar(datosUsuario = entidadNegocio.datosUsuario.copiar(usuario = "Otro usuario")))
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()

                    recursoEntidadEspecifica.patch(entidadPatch)

                    verify(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!)
                                     )
                                                     )
                }

                @Test
                fun el_usuario_puede_cambiar_su_contraseña_cuando_no_tiene_permiso_de_patch_pero_esta_logeado_con_usuario_correspondiente_a_la_ruta()
                {
                    val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, null)
                    doNothing()
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!)
                                     )
                                                     )

                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    doReturn(entidadNegocio)
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()

                    recursoEntidadEspecifica.patch(entidadPatch)

                    verify(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!)
                                     )
                                                     )
                }

                @Test
                fun cuando_el_usuario_no_tiene_permiso_de_patch_e_intenta_activar_su_usuario_se_lanza_UsuarioNoTienePermiso()
                {
                    val entidadPatch = UsuarioPatchDTO(null, true)
                    doNothing()
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.ACTIVO to Usuario.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                     )
                                                     )

                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    doReturn(entidadNegocio)
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()

                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.patch(entidadPatch) }

                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.ACTIVO to Usuario.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                     )
                                                     )
                }

                @Test
                fun cuando_el_usuario_no_tiene_permiso_de_patch_e_intenta_desactivar_su_usuario_se_lanza_UsuarioNoTienePermiso()
                {
                    val entidadPatch = UsuarioPatchDTO(null, false)
                    doNothing()
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.ACTIVO to Usuario.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                     )
                                                     )

                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    doReturn(entidadNegocio)
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()

                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.patch(entidadPatch) }

                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(
                                        Usuario.Campos.ACTIVO to Usuario.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                     )
                                                     )
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, true)
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    doReturn(null)
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()

                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.patch(entidadPatch) }

                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(contraseñaPrueba))
                                                     )
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso_y_esta_logeado_con_usuario_diferente()
                {
                    val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, null)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    doReturn(entidadNegocio.copiar(datosUsuario = entidadNegocio.datosUsuario.copiar(usuario = "Otro usuario")))
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()

                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.patch(entidadPatch) }

                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(contraseñaPrueba))
                                                     )
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso_y_esta_logeado_con_mismo_usuario_en_diferente_cliente()
                {
                    val entidadPatch = UsuarioPatchDTO(contraseñaPrueba, null)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)

                    doReturn(entidadNegocio.copiar(datosUsuario = entidadNegocio.datosUsuario.copiar(idCliente = ID_CLIENTE + 1)))
                        .`when`(mockManejadorSeguridad)
                        .darUsuarioDeClienteAutenticado()

                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.patch(entidadPatch) }

                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                ID_CLIENTE,
                                entidadDTO.usuario,
                                mapOf(Usuario.Campos.CONTRASEÑA to Usuario.CredencialesUsuario.CampoContraseña(contraseñaPrueba))
                                                     )
                }
            }
        }

        @Nested
        @DisplayName("Al eliminar")
        inner class Eliminar
        {
            private lateinit var entidadNegocio: Usuario
            private lateinit var entidadDTO: UsuarioDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(1)
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun no_lanza_excepcion_cuando_el_repositorio_retorna_true()
            {
                doReturn(true)
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)

                recursoEntidadEspecifica.eliminarPorId()
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_no_existe_cuando_el_repositorio_retorna_false()
            {
                doReturn(false)
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(UsuarioDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_entidad_referenciada_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Error eliminando usuario"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)

                val errorApi = assertThrows<ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                kotlin.test.assertEquals(UsuarioDTO.CodigosError.ENTIDAD_REFERENCIADA, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_desconocido_cuando_el_repositorio_lanza_ErrorEliminandoEntidad()
            {
                doThrow(co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad("el_usuario", Usuario.NOMBRE_ENTIDAD))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)

                val errorApi = assertThrows<ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(UsuarioDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Usuarios", PermisoBack.Accion.DELETE)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(true)
                        .`when`(mockRepositorio)
                        .eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.eliminarPorId()
                    verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.eliminarPorId() }
                    verify(mockRepositorio, times(0)).eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.eliminarPorId() }
                    verify(mockRepositorio, times(0)).eliminarPorId(ID_CLIENTE, entidadNegocio.datosUsuario.usuario)
                }
            }
        }
    }
}