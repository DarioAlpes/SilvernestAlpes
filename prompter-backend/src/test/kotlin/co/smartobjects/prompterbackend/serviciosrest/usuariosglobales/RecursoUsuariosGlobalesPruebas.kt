package co.smartobjects.prompterbackend.serviciosrest.usuariosglobales

import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.usuariosglobales.RepositorioUsuariosGlobales
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalParaCreacionDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalPatchDTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("Recurso Usuarios Globales")
internal class RecursoUsuariosGlobalesPruebas
{
    companion object
    {
        const val ID_ENTIDAD_PRUEBAS = "usuario_pruebas_1"
        private val CONTRASEÑA_PRUEBAS = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): UsuarioGlobal
    {
        return UsuarioGlobal(
                UsuarioGlobal.DatosUsuario("usuario_pruebas_$indice", "nombre_completo_$indice", "email_$indice.com", indice % 2 == 0)
                            )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): UsuarioGlobalDTO
    {
        return UsuarioGlobalDTO(darEntidadNegocioSegunIndice(indice))
    }

    private fun darEntidadNegocioParaCreacionSegunIndiceYContraseña(indice: Int, contraseña: CharArray): UsuarioGlobal.UsuarioParaCreacion
    {
        return UsuarioGlobal.UsuarioParaCreacion(
                UsuarioGlobal.DatosUsuario("usuario_pruebas_$indice", "nombre_completo_$indice", "email_$indice.com", indice % 2 == 0),
                contraseña
                                                )
    }

    private fun darEntidadDTOParaCreacionSegunIndiceYContraseña(indice: Int, contraseña: CharArray): UsuarioGlobalParaCreacionDTO
    {
        return UsuarioGlobalParaCreacionDTO(darEntidadNegocioParaCreacionSegunIndiceYContraseña(indice, contraseña))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoUsuariosGlobales
        private lateinit var mockRecursoEntidadEspecifica: RecursoUsuariosGlobales.RecursoUsuarioGlobal

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoEntidadEspecifica = mockConDefaultAnswer(RecursoUsuariosGlobales.RecursoUsuarioGlobal::class.java)
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoUsuariosGlobales::class.java)

            doReturn(mockRecursoEntidadEspecifica).`when`(mockRecursoTodasEntidades).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)

            doNothing().`when`(mockRecursoTodasEntidades).inicializar()

            ConfiguracionAplicacionJersey.RECURSO_USUARIOS_GLOBALES = mockRecursoTodasEntidades

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
                doReturn(sequenceOf<UsuarioGlobalDTO>()).`when`(mockRecursoTodasEntidades).darTodas()

                target.path(RecursoUsuariosGlobales.RUTA).request().get(String::class.java)

                verify(mockRecursoTodasEntidades, times(1)).darTodas()
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
                doReturn(UsuarioGlobalDTO(entidadPruebas)).`when`(mockRecursoTodasEntidades).crear(dtoPruebas)

                target.path(RecursoUsuariosGlobales.RUTA).request()
                    .post(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), UsuarioGlobalDTO::class.java)

                verify(mockRecursoTodasEntidades, times(1)).crear(dtoPruebas)
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
                doReturn(UsuarioGlobalDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)

                target.path("${RecursoUsuariosGlobales.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .put(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), UsuarioGlobalDTO::class.java)

                verify(mockRecursoEntidadEspecifica, times(1)).actualizar(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al hacer patch")]
        inner class Patch
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val contraseña = CONTRASEÑA_PRUEBAS.copyOf()
                val entidadPatch = UsuarioGlobalPatchDTO(contraseña, true)
                doNothing().`when`(mockRecursoEntidadEspecifica).patch(entidadPatch)

                target.path("${RecursoUsuariosGlobales.RUTA}/$ID_ENTIDAD_PRUEBAS")
                    .request()
                    .patch(entidadPatch)

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
                doReturn(UsuarioGlobalDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).darPorId()

                target.path("${RecursoUsuariosGlobales.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .get(UsuarioGlobalDTO::class.java)

                verify(mockRecursoEntidadEspecifica, times(1)).darPorId()
            }
        }

        @[Nested DisplayName("Al eliminar")]
        inner class Eliminar
        {
            @Test
            fun llama_la_funcion_eliminarPorId_con_dto_correcto()
            {
                doNothing().`when`(mockRecursoEntidadEspecifica).eliminarPorId()

                target.path("${RecursoUsuariosGlobales.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .delete()

                verify(mockRecursoEntidadEspecifica, times(1)).eliminarPorId()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioUsuariosGlobales::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalSiempreEstaAutenticado
        private val recursoTodasEntidades: RecursoUsuariosGlobales by lazy {
            spy(RecursoUsuariosGlobales(mockRepositorio, mockManejadorSeguridad))
        }
        private val recursoEntidadEspecifica: RecursoUsuariosGlobales.RecursoUsuarioGlobal by lazy {
            recursoTodasEntidades.darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)
        }

        init
        {
            doReturn(true)
                .`when`(mockRepositorio)
                .inicializar()
        }


        @Nested
        inner class CreacionUsuarioGlobalParaConfiguracionInicial
        {
            private val usuarioACrear =
                    UsuarioGlobal.UsuarioParaCreacion(
                            UsuarioGlobal.DatosUsuario("admin", "Juan Ciga", "juan.ciga@smartobjects.co", true),
                            "admin".toCharArray()
                                                     )

            @BeforeEach
            fun prepararDatos()
            {
                doReturn(mockConDefaultAnswer(UsuarioGlobal::class.java))
                    .`when`(mockRepositorio)
                    .crear(usuarioACrear)
            }

            @Test
            fun al_inicializar_crea_un_usuario_global_para_la_configuracion_inicial_si_no_existe_la_tabla()
            {
                doReturn(false)
                    .`when`(mockRepositorio)
                    .inicializar()

                val ordenEsperado = inOrder(mockRepositorio, recursoTodasEntidades)

                recursoTodasEntidades.inicializar()

                ordenEsperado.verify(mockRepositorio).inicializar()
                ordenEsperado.verify(mockRepositorio).crear(usuarioACrear)
            }

            @Test
            fun al_inicializar_no_crea_un_usuario_global_para_la_configuracion_inicial_si_existe_la_tabla()
            {
                doReturn(true)
                    .`when`(mockRepositorio)
                    .inicializar()

                val ordenEsperado = inOrder(mockRepositorio, recursoTodasEntidades)

                recursoTodasEntidades.inicializar()

                ordenEsperado.verify(mockRepositorio).inicializar()
                ordenEsperado.verify(mockRepositorio, times(0)).crear(usuarioACrear)
            }
        }

        @Nested
        @DisplayName("Al crear")
        inner class Crear
        {
            private lateinit var contraseñaPrueba: CharArray
            private lateinit var entidadNegocio: UsuarioGlobal
            private lateinit var entidadNegocioCreacion: UsuarioGlobal.UsuarioParaCreacion
            private lateinit var entidadDTO: UsuarioGlobalDTO
            private lateinit var entidadDTOCreacion: UsuarioGlobalParaCreacionDTO

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
                    .crear(entidadNegocioCreacion)

                val entidadRetornada = recursoTodasEntidades.crear(entidadDTOCreacion)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).crear(entidadNegocioCreacion)

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
                    .crear(entidadNegocioCreacion)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoTodasEntidades.crear(entidadDTOCreacion) }

                assertEquals(UsuarioGlobalDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(entidadNegocioCreacion)

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
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Usuario global"))
                        .`when`(mockRepositorio)
                        .crear(entidadNegocioCreacion)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion) }

                    assertEquals(UsuarioGlobalDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(entidadNegocioCreacion)

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
                        .crear(entidadNegocioCreacion)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion) }

                    assertEquals(UsuarioGlobalDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(entidadNegocioCreacion)

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

                        assertEquals(UsuarioGlobalDTO.CodigosError.USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_usuario_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(usuario = "      ")) }

                        assertEquals(UsuarioGlobalDTO.CodigosError.USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(entidadNegocioCreacion)

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

                        assertEquals(UsuarioGlobalDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_nombre_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(nombreCompleto = "      ")) }

                        assertEquals(UsuarioGlobalDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(entidadNegocioCreacion)

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

                        assertEquals(UsuarioGlobalDTO.CodigosError.EMAIL_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_email_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(email = "      ")) }

                        assertEquals(UsuarioGlobalDTO.CodigosError.EMAIL_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(entidadNegocioCreacion)

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
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOCreacion.copy(contraseña = charArrayOf())) }

                        assertEquals(UsuarioGlobalDTO.CodigosError.CONTRASEÑA_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(entidadNegocioCreacion)
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .crear(entidadNegocioCreacion)
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalSiempreEstaAutenticado
                    recursoTodasEntidades.crear(entidadDTOCreacion)
                    verify(mockRepositorio).crear(entidadNegocioCreacion)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.crear(entidadDTOCreacion) }
                    verify(mockRepositorio, times(0)).crear(entidadNegocioCreacion)
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
                doReturn(sequenceOf<UsuarioGlobal>())
                    .`when`(mockRepositorio)
                    .listar()

                val listaRetornada = recursoTodasEntidades.darTodas()

                Assertions.assertTrue(listaRetornada.none())
                verify(mockRepositorio).listar()
            }

            @Test
            fun retorna_una_lista_de_dtos_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                val listaNegocio = List(5) { darEntidadNegocioSegunIndice(it) }.asSequence()
                val listaDTO = List(5) { darEntidadDTOSegunIndice(it) }
                doReturn(listaNegocio)
                    .`when`(mockRepositorio)
                    .listar()

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listar()
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Usuario"))
                    .`when`(mockRepositorio)
                    .listar()

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.darTodas() }

                assertEquals(UsuarioGlobalDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listar()
            }

            @Nested
            inner class Permisos
            {
                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(sequenceOf<UsuarioGlobal>())
                        .`when`(mockRepositorio)
                        .listar()
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalSiempreEstaAutenticado
                    recursoTodasEntidades.darTodas()
                    verify(mockRepositorio).listar()
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.darTodas() }
                    verify(mockRepositorio, times(0)).listar()
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
                    .buscarPorId(ID_ENTIDAD_PRUEBAS)

                val entidadRetornada = recursoEntidadEspecifica.darPorId()

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).buscarPorId(ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_correcto_cuando_el_repositorio_retorna_null()
            {
                doReturn(null)
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                assertEquals(UsuarioGlobalDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Ubicación"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<ErrorDesconocido> { recursoEntidadEspecifica.darPorId() }

                assertEquals(UsuarioGlobalDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_ENTIDAD_PRUEBAS)
            }

            @Nested
            inner class Permisos
            {
                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    val entidadNegocio = darEntidadNegocioSegunIndice(1)
                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .buscarPorId(ID_ENTIDAD_PRUEBAS)
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalSiempreEstaAutenticado
                    recursoEntidadEspecifica.darPorId()
                    verify(mockRepositorio).buscarPorId(ID_ENTIDAD_PRUEBAS)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.darPorId() }
                    verify(mockRepositorio, times(0)).buscarPorId(ID_ENTIDAD_PRUEBAS)
                }
            }
        }

        @Nested
        @DisplayName("Al actualizar")
        inner class Actualizar
        {
            private lateinit var contraseñaPrueba: CharArray
            private lateinit var entidadNegocio: UsuarioGlobal
            private lateinit var entidadNegocioCreacion: UsuarioGlobal.UsuarioParaCreacion
            private lateinit var entidadDTO: UsuarioGlobalDTO
            private lateinit var entidadDTOCreacion: UsuarioGlobalParaCreacionDTO

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
                    .actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTOCreacion)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

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
                    .actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(usuario = "Otro usuario"))

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

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
                    .actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }

                assertEquals(UsuarioGlobalDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

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
                    .actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }

                assertEquals(UsuarioGlobalDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

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
                        .actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }

                    assertEquals(UsuarioGlobalDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

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
                        .actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }

                    assertEquals(UsuarioGlobalDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

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

                        assertEquals(UsuarioGlobalDTO.CodigosError.USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar("", entidadNegocioCreacion)

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

                        assertEquals(UsuarioGlobalDTO.CodigosError.USUARIO_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar("            ", entidadNegocioCreacion)

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

                        assertEquals(UsuarioGlobalDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_nombre_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(nombreCompleto = "      ")) }

                        assertEquals(UsuarioGlobalDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

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

                        assertEquals(UsuarioGlobalDTO.CodigosError.EMAIL_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

                        // Verificar contraseña limpiada
                        assertTrue(contraseñaPrueba.all { it == '\u0000' })
                        assertTrue(entidadNegocioCreacion.contraseña.all { it == '\u0000' })
                        assertTrue(entidadDTOCreacion.contraseña.all { it == '\u0000' })
                    }

                    @Test
                    fun cuando_el_dto_tiene_email_con_solo_espacios_y_tabs_y_limpia_la_contraseña()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion.copy(email = "      ")) }

                        assertEquals(UsuarioGlobalDTO.CodigosError.EMAIL_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)

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

                        assertEquals(UsuarioGlobalDTO.CodigosError.CONTRASEÑA_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalSiempreEstaAutenticado
                    recursoEntidadEspecifica.actualizar(entidadDTOCreacion)
                    verify(mockRepositorio).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.actualizar(entidadDTOCreacion) }
                    verify(mockRepositorio, times(0)).actualizar(entidadNegocio.datosUsuario.usuario, entidadNegocioCreacion)
                }
            }
        }

        @Nested
        @DisplayName("Al hacer patch")
        inner class Patch
        {
            private lateinit var contraseñaPrueba: CharArray
            private lateinit var entidadNegocio: UsuarioGlobal
            private lateinit var entidadDTO: UsuarioGlobalDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                contraseñaPrueba = CONTRASEÑA_PRUEBAS.copyOf()
                entidadNegocio = darEntidadNegocioSegunIndice(1)
                entidadDTO = darEntidadDTOSegunIndice(1)
            }

            @Test
            fun funciona_correctamente_cuando_el_repositorio_actualiza_la_entidad_y_limpia_la_contraseña()
            {
                val entidadPatch = UsuarioGlobalPatchDTO(contraseñaPrueba, true)
                doNothing()
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(
                            entidadDTO.usuario,
                            mapOf(
                                    UsuarioGlobal.Campos.CONTRASEÑA to UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPrueba),
                                    UsuarioGlobal.Campos.ACTIVO to UsuarioGlobal.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                 )
                                                 )

                recursoEntidadEspecifica.patch(entidadPatch)

                verify(mockRepositorio)
                    .actualizarCamposIndividuales(
                            entidadDTO.usuario,
                            mapOf(
                                    UsuarioGlobal.Campos.CONTRASEÑA to UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPrueba),
                                    UsuarioGlobal.Campos.ACTIVO to UsuarioGlobal.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                 )
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
                    val entidadPatch = UsuarioGlobalPatchDTO(contraseñaPrueba, true)
                    doThrow(ErrorDeCreacionActualizacionEntidad("no importa"))
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                entidadDTO.usuario,
                                mapOf(
                                        UsuarioGlobal.Campos.CONTRASEÑA to UsuarioGlobal.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!),
                                        UsuarioGlobal.Campos.ACTIVO to UsuarioGlobal.DatosUsuario.CampoActivo(entidadPatch.activo!!)
                                     )
                                                     )

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(UsuarioGlobalDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio)
                        .actualizarCamposIndividuales(
                                entidadDTO.usuario,
                                mapOf(
                                        UsuarioGlobal.Campos.CONTRASEÑA to UsuarioGlobal.CredencialesUsuario.CampoContraseña(entidadPatch.contraseña!!),
                                        UsuarioGlobal.Campos.ACTIVO to UsuarioGlobal.DatosUsuario.CampoActivo(entidadPatch.activo!!)
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
                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    val entidadPatch = UsuarioGlobalPatchDTO(contraseñaPrueba, null)
                    doNothing()
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(
                                entidadDTO.usuario,
                                mapOf(UsuarioGlobal.Campos.CONTRASEÑA to UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPrueba))
                                                     )
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalSiempreEstaAutenticado
                    recursoEntidadEspecifica.patch(entidadPatch)
                    verify(mockRepositorio)
                        .actualizarCamposIndividuales(
                                entidadDTO.usuario,
                                mapOf(UsuarioGlobal.Campos.CONTRASEÑA to UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPrueba))
                                                     )
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    val entidadPatch = UsuarioGlobalPatchDTO(contraseñaPrueba, true)
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.patch(entidadPatch) }
                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(
                                entidadDTO.usuario,
                                mapOf(UsuarioGlobal.Campos.CONTRASEÑA to UsuarioGlobal.CredencialesUsuario.CampoContraseña(contraseñaPrueba))
                                                     )
                }
            }
        }

        @Nested
        @DisplayName("Al eliminar")
        inner class Eliminar
        {
            private lateinit var entidadNegocio: UsuarioGlobal
            private lateinit var entidadDTO: UsuarioGlobalDTO

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
                    .eliminarPorId(entidadNegocio.datosUsuario.usuario)

                recursoEntidadEspecifica.eliminarPorId()
                verify(mockRepositorio).eliminarPorId(entidadNegocio.datosUsuario.usuario)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_no_existe_cuando_el_repositorio_retorna_false()
            {
                doReturn(false)
                    .`when`(mockRepositorio)
                    .eliminarPorId(entidadNegocio.datosUsuario.usuario)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(UsuarioGlobalDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(entidadNegocio.datosUsuario.usuario)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_entidad_referenciada_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Error eliminando usuario"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(entidadNegocio.datosUsuario.usuario)

                val errorApi = assertThrows<ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                kotlin.test.assertEquals(UsuarioGlobalDTO.CodigosError.ENTIDAD_REFERENCIADA, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(entidadNegocio.datosUsuario.usuario)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_desconocido_cuando_el_repositorio_lanza_ErrorEliminandoEntidad()
            {
                doThrow(co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad("el_usuario", UsuarioGlobal.NOMBRE_ENTIDAD))
                    .`when`(mockRepositorio)
                    .eliminarPorId(entidadNegocio.datosUsuario.usuario)

                val errorApi = assertThrows<ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(UsuarioGlobalDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(entidadNegocio.datosUsuario.usuario)
            }

            @Nested
            inner class Permisos
            {
                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(true)
                        .`when`(mockRepositorio)
                        .eliminarPorId(entidadNegocio.datosUsuario.usuario)
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalSiempreEstaAutenticado
                    recursoEntidadEspecifica.eliminarPorId()
                    verify(mockRepositorio).eliminarPorId(entidadNegocio.datosUsuario.usuario)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.eliminarPorId() }
                    verify(mockRepositorio, times(0)).eliminarPorId(entidadNegocio.datosUsuario.usuario)
                }
            }
        }
    }
}