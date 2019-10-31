package co.smartobjects.prompterbackend.serviciosrest.personas

import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdad
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.personas.ValorGrupoEdadDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import java.util.*
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import kotlin.test.assertEquals


@DisplayName("Recurso ValorGrupoEdad")
internal class RecursoValoresGrupoEdadPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val VALOR_GRUPO_PRUEBAS = "Grupo de prueba de servicios"
    }

    private fun darEntidadNegocioSegunIndice(indice: Int) = ValorGrupoEdad(VALOR_GRUPO_PRUEBAS, indice, indice + 2)

    private fun darEntidadDTOSegunIndice(indice: Int) = ValorGrupoEdadDTO(darEntidadNegocioSegunIndice(indice))

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoValoresGrupoEdad
        private lateinit var mockRecursoEntidadEspecifica: RecursoValoresGrupoEdad.RecursoValorGrupoEdad

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoEntidadEspecifica = mockConDefaultAnswer(RecursoValoresGrupoEdad.RecursoValorGrupoEdad::class.java)
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoValoresGrupoEdad::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoValoresGrupoEdad()
            doReturn(mockRecursoEntidadEspecifica).`when`(mockRecursoTodasEntidades).darRecursosEntidadEspecifica(VALOR_GRUPO_PRUEBAS)

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
                doReturn(sequenceOf<ValorGrupoEdadDTO>()).`when`(mockRecursoTodasEntidades).darTodas()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoValoresGrupoEdad.RUTA}").request().get(String::class.java)

                verify(mockRecursoTodasEntidades).darTodas()
            }
        }

        @[Nested DisplayName("Al crear")]
        inner class Crear
        {
            @Test
            fun llama_la_funcion_crear_con_dto_correcto()
            {
                val dtoPruebas = darEntidadDTOSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                val entidadPruebas = darEntidadNegocioSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                doReturn(ValorGrupoEdadDTO(entidadPruebas)).`when`(mockRecursoTodasEntidades).crear(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoValoresGrupoEdad.RUTA}").request()
                    .post(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), ValorGrupoEdadDTO::class.java)

                verify(mockRecursoTodasEntidades).crear(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al actualizar")]
        inner class Actualizar
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val dtoPruebas = darEntidadDTOSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                val entidadPruebas = darEntidadNegocioSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                doReturn(ValorGrupoEdadDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoValoresGrupoEdad.RUTA}/$VALOR_GRUPO_PRUEBAS").request()
                    .put(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), ValorGrupoEdadDTO::class.java)

                verify(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al consultar una")]
        inner class ConsultarUna
        {
            @Test
            fun llama_la_funcion_darPorId_con_dto_correcto()
            {
                val entidadPruebas = darEntidadNegocioSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                doReturn(ValorGrupoEdadDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).darPorId()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoValoresGrupoEdad.RUTA}/$VALOR_GRUPO_PRUEBAS").request()
                    .get(ValorGrupoEdadDTO::class.java)

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

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoValoresGrupoEdad.RUTA}/$VALOR_GRUPO_PRUEBAS").request()
                    .delete()

                verify(mockRecursoEntidadEspecifica).eliminarPorId()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioValoresGruposEdad::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoValoresGrupoEdad by lazy {
            RecursoValoresGrupoEdad(ID_CLIENTE, mockRepositorio, mockManejadorSeguridad)
        }
        private val recursoEntidadEspecifica: RecursoValoresGrupoEdad.RecursoValorGrupoEdad by lazy {
            recursoTodasEntidades.darRecursosEntidadEspecifica(VALOR_GRUPO_PRUEBAS)
        }

        @Nested
        @DisplayName("Al crear")
        inner class Crear
        {
            private lateinit var entidadNegocio: ValorGrupoEdad
            private lateinit var entidadDTO: ValorGrupoEdadDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                entidadDTO = darEntidadDTOSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_crea_la_entidad()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val entidadRetornada = recursoTodasEntidades.crear(entidadDTO)

                Assertions.assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.crear(entidadDTO) }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun valor_duplicado_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Entidad cualquiera"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO) }

                    Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun intersecta_otro_grupo_de_edad_cuando_el_repositorio_lanza_ErrorActualizacionViolacionDeRestriccion()
                {
                    doThrow(ErrorCreacionViolacionDeRestriccion("no importa", "no importa", arrayOf("no importa", "no importa")))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO) }

                    Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.INTERSECTA_OTRO_GRUPO_DE_EDAD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("no importa"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO) }

                    Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Nested
                @DisplayName("valor inválido")
                inner class ValorInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_valor_vacio()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(valor = "")) }

                        Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.VALOR_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_dto_tiene_valor_con_solo_espacios_y_tabs()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(valor = "      ")) }

                        Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.VALOR_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("edad mínima superior a la máxima")
                inner class EdadMinimaSuperiorAMaxima
                {
                    @Test
                    fun cuando_el_dto_tiene_fecha_de_nacimiento_posterior_a_la_fecha_de_hoy()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(edadMinima = 10, edadMaxima = 1)) }

                        Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.EDAD_MINIMA_SUPERIOR_A_MAXIMA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Grupos de Edad", PermisoBack.Accion.POST)
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
                        .crear(ID_CLIENTE, entidadNegocio)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.crear(entidadDTO)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.crear(entidadDTO) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.crear(entidadDTO) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
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
                doReturn(sequenceOf<ValorGrupoEdad>())
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val listaRetornada = recursoTodasEntidades.darTodas()

                Assertions.assertTrue(listaRetornada.none())
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
                doThrow(ErrorDeConsultaEntidad("Entidad cualquiera"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.darTodas() }

                Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.darTodas() }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Grupos de Edad", PermisoBack.Accion.GET_TODOS)
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
                    doReturn(sequenceOf<ValorGrupoEdad>())
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
                val entidadNegocio = darEntidadNegocioSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                val entidadDTO = darEntidadDTOSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)

                val entidadRetornada = recursoEntidadEspecifica.darPorId()

                Assertions.assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_correcto_cuando_el_repositorio_retorna_null()
            {
                doReturn(null)
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Entidad cualquiera"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)

                val errorApi = assertThrows<ErrorDesconocido> { recursoEntidadEspecifica.darPorId() }

                Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Grupos de Edad", PermisoBack.Accion.GET_UNO)
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
                    val entidadNegocio = darEntidadNegocioSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.darPorId()
                    verify(mockRepositorio).buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.darPorId() }
                    verify(mockRepositorio, times(0)).buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.darPorId() }
                    verify(mockRepositorio, times(0)).buscarPorId(ID_CLIENTE, VALOR_GRUPO_PRUEBAS)
                }
            }
        }

        @Nested
        @DisplayName("Al actualizar")
        inner class Actualizar
        {
            private lateinit var entidadNegocio: ValorGrupoEdad
            private lateinit var entidadDTO: ValorGrupoEdadDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                entidadDTO = darEntidadDTOSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_actualiza_la_entidad()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTO)

                Assertions.assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
            }

            @Test
            fun permite_actualizar_el_valor_de_un_grupo()
            {
                val entidadDeNegocioNueva = entidadNegocio.copiar(valor = VALOR_GRUPO_PRUEBAS + "bla")
                doReturn(entidadDeNegocioNueva)
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadDeNegocioNueva)

                val nuevaEntidadRetornadaDTO = entidadDTO.copy(valor = entidadDeNegocioNueva.valor)
                val entidadRetornada = recursoEntidadEspecifica.actualizar(nuevaEntidadRetornadaDTO)

                Assertions.assertEquals(nuevaEntidadRetornadaDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadDeNegocioNueva)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_no_existe_cuando_el_repositorio_lanza_EntidadNoExiste()
            {
                doThrow(co.smartobjects.persistencia.excepciones.EntidadNoExiste(Random().nextLong(), "no importa"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_entidad_referenciada_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea(Random().nextLong(), "no importa"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorCambiandoIdDeEntidadReferenciada> {
                    recursoEntidadEspecifica.actualizar(entidadDTO)
                }

                kotlin.test.assertEquals(ValorGrupoEdadDTO.CodigosError.ENTIDAD_REFERENCIADA, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.valor, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun valor_duplicado_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Entidad cualquiera"))
                        .`when`(mockRepositorio)
                        .actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                }

                @Test
                fun intersecta_otro_grupo_de_edad_cuando_el_repositorio_lanza_ErrorActualizacionViolacionDeRestriccion()
                {
                    doThrow(ErrorActualizacionViolacionDeRestriccion("no importa", "no importa", "no importa", arrayOf("no importa", "no importa")))
                        .`when`(mockRepositorio)
                        .actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.INTERSECTA_OTRO_GRUPO_DE_EDAD, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando ValorGrupoEdad"))
                        .`when`(mockRepositorio)
                        .actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                }

                @Nested
                @DisplayName("valor inválido")
                inner class ValorInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_valor_vacio()
                    {
                        val entidadDTOARecibir = entidadDTO.copy(valor = "")
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOARecibir) }

                        Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.VALOR_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_dto_tiene_valor_con_solo_espacios_y_tabs()
                    {
                        val entidadDTOARecibir = entidadDTO.copy(valor = "            ")
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTOARecibir) }

                        Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.VALOR_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("edad mínima superior a la máxima")
                inner class EdadMinimaSuperiorAMaxima
                {
                    @Test
                    fun cuando_el_dto_tiene_fecha_de_nacimiento_posterior_a_la_fecha_de_hoy()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(edadMinima = 10, edadMaxima = 1)) }

                        Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.EDAD_MINIMA_SUPERIOR_A_MAXIMA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Grupos de Edad", PermisoBack.Accion.PUT)
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
                        .actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.actualizar(entidadDTO)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.actualizar(entidadDTO) }
                    verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.actualizar(entidadDTO) }
                    verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, VALOR_GRUPO_PRUEBAS, entidadNegocio)
                }
            }
        }

        @Nested
        @DisplayName("Al eliminar")
        inner class Eliminar
        {
            private lateinit var entidadNegocio: ValorGrupoEdad
            private lateinit var entidadDTO: ValorGrupoEdadDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
                entidadDTO = darEntidadDTOSegunIndice(VALOR_GRUPO_PRUEBAS.hashCode())
            }

            @Test
            fun no_lanza_excepcion_cuando_el_repositorio_retorna_true()
            {
                doReturn(true)
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.valor)

                recursoEntidadEspecifica.eliminarPorId()

                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.valor)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_no_existe_cuando_el_repositorio_retorna_false()
            {
                doReturn(false)
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.valor)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.valor)
            }

            @Test
            fun lanza_excepcion_ErrorEliminandoEntidad_con_codigo_interno_entidad_referenciada_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Error eliminando acceso"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.valor)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad> {
                    recursoEntidadEspecifica.eliminarPorId()
                }

                kotlin.test.assertEquals(ValorGrupoEdadDTO.CodigosError.ENTIDAD_REFERENCIADA, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.valor)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_desconocido_cuando_el_repositorio_lanza_ErrorEliminandoEntidad()
            {
                doThrow(ErrorEliminandoEntidad(1, ValorGrupoEdad.NOMBRE_ENTIDAD))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.valor)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad> {
                    recursoEntidadEspecifica.eliminarPorId()
                }

                Assertions.assertEquals(ValorGrupoEdadDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.valor)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.valor)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.valor)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Grupos de Edad", PermisoBack.Accion.DELETE)
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
                        .eliminarPorId(ID_CLIENTE, entidadNegocio.valor)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.eliminarPorId()
                    verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.valor)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.eliminarPorId() }
                    verify(mockRepositorio, times(0)).eliminarPorId(ID_CLIENTE, entidadNegocio.valor)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.eliminarPorId() }
                    verify(mockRepositorio, times(0)).eliminarPorId(ID_CLIENTE, entidadNegocio.valor)
                }
            }
        }
    }
}