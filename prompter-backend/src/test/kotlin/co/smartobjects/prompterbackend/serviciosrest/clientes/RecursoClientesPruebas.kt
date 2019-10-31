package co.smartobjects.prompterbackend.serviciosrest.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.personas.CampoDePersona
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.clientes.RepositorioClientes
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.personas.camposdepersona.ListadoCamposPredeterminados
import co.smartobjects.persistencia.personas.camposdepersona.RepositorioCamposDePersonas
import co.smartobjects.persistencia.usuarios.RepositorioUsuarios
import co.smartobjects.persistencia.usuarios.roles.RepositorioRoles
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.seguridad.GeneradorLlaveNFCCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoRoles
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoUsuarios
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
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
import kotlin.test.assertTrue


@DisplayName("Recurso Clientes")
internal class RecursoClientesPruebas
{
    companion object
    {
        const val ID_ENTIDAD_PRUEBAS = 1L
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Cliente
    {
        return Cliente(
                indice.toLong(),
                "Entidad prueba $indice"
                      )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): ClienteDTO
    {
        return ClienteDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoClientes
        private lateinit var mockRecursoEntidadEspecifica: RecursoClientes.RecursoCliente

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoEntidadEspecifica = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoEntidadEspecifica).`when`(mockRecursoTodasEntidades).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)

            mockearConfiguracionAplicacionJerseyParaNoUsarConfiguracionRepositorios(mockRecursoTodasEntidades)

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
                doReturn(sequenceOf<ClienteDTO>()).`when`(mockRecursoTodasEntidades).darTodas()

                target.path(RecursoClientes.RUTA).request().get(String::class.java)

                verify(mockRecursoTodasEntidades).darTodas()
            }
        }

        @[Nested DisplayName("Al crear")]
        inner class Crear
        {
            @Test
            fun llama_la_funcion_crear_con_dto_correcto()
            {
                val dtoPruebas = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                val entidadPruebas = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                doReturn(ClienteDTO(entidadPruebas)).`when`(mockRecursoTodasEntidades).crear(dtoPruebas)

                target.path(RecursoClientes.RUTA).request()
                    .post(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), ClienteDTO::class.java)

                verify(mockRecursoTodasEntidades).crear(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al actualizar")]
        inner class Actualizar
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val dtoPruebas = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                val entidadPruebas = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                doReturn(ClienteDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .put(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), ClienteDTO::class.java)

                verify(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al consultar una")]
        inner class ConsultarUna
        {
            @Test
            fun llama_la_funcion_darPorId_con_dto_correcto()
            {
                val entidadPruebas = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                doReturn(ClienteDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).darPorId()

                target.path("${RecursoClientes.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .get(ClienteDTO::class.java)

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

                target.path("${RecursoClientes.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .delete()

                verify(mockRecursoEntidadEspecifica).eliminarPorId()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val LLAVE_NFC_PRUEBA = "123-456-789".toCharArray()

        private val mockGeneradorLlaveNFCCliente = mockConDefaultAnswer(GeneradorLlaveNFCCliente::class.java).also {
            doReturn(LLAVE_NFC_PRUEBA).`when`(it).generarLlave()
        }
        private val mockRepositorioCamposDePersonas = mockConDefaultAnswer(RepositorioCamposDePersonas::class.java).also {
            doReturn(emptyList<CampoDePersona>()).`when`(it).crear(anyLong(), cualquiera())
        }
        private val mockRepositorioRoles = mockConDefaultAnswer(RepositorioRoles::class.java).also {
            doReturn(mockConDefaultAnswer(Rol::class.java)).`when`(it).crear(eq(ID_ENTIDAD_PRUEBAS), cualquiera())
        }
        private val mockRepositorioUsuarios = mockConDefaultAnswer(RepositorioUsuarios::class.java).also {
            doReturn(mockConDefaultAnswer(Usuario::class.java)).`when`(it).crear(eq(ID_ENTIDAD_PRUEBAS), cualquiera())
        }
        private val mockRepositorioLlavesNFC = mockConDefaultAnswer(RepositorioLlavesNFC::class.java).also {
            doReturn(Cliente.LlaveNFC(1, "asdf")).`when`(it).crear(anyLong(), cualquiera())
        }
        private val mockRepositorio = mockConDefaultAnswer(RepositorioClientes::class.java).also {
            doNothing().`when`(it).crearTablaSiNoExiste()
        }

        private val mockDeDependencias = mockConDefaultAnswer(Dependencias::class.java).also {
            doReturn(mockGeneradorLlaveNFCCliente).`when`(it).generadorLlaveNFCCliente
            doReturn(mockRepositorioCamposDePersonas).`when`(it).repositorioCampoDePersonas
            doReturn(mockRepositorioRoles).`when`(it).repositorioRoles
            doReturn(mockRepositorioUsuarios).`when`(it).repositorioUsuarios
            doReturn(mockRepositorioLlavesNFC).`when`(it).repositorioLlavesNFC
            doReturn(mockRepositorio).`when`(it).repositorioClientes
            doReturn(mockManejadorSeguridadUsuarioGlobalSiempreEstaAutenticado).`when`(it).manejadorSeguridad

            doNothing().`when`(it).inicializarTablasNecesariasCliente(ID_ENTIDAD_PRUEBAS)
            doNothing().`when`(mockRepositorio).inicializarConexionAEsquemaDeSerNecesario(ID_ENTIDAD_PRUEBAS)
        }
        private val mockRecursoRoles = mockConDefaultAnswer(RecursoRoles::class.java).also {
            doNothing().`when`(it).crearRolParaConfiguracionInicial()
        }
        private val mockRecurosUsuarios = mockConDefaultAnswer(RecursoUsuarios::class.java).also {
            doNothing().`when`(it).crearUsuarioParaConfiguracionInicial()
        }
        private lateinit var recursoTodasEntidades: RecursoClientes
        private lateinit var recursoEntidadEspecifica: RecursoClientes.RecursoCliente


        @BeforeEach
        fun inicializarRecursoEntidadEspecifica()
        {
            recursoTodasEntidades = spy(RecursoClientes(mockDeDependencias))
            recursoEntidadEspecifica = spy(recursoTodasEntidades.darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS))
        }

        @Nested
        @DisplayName("Al crear")
        inner class Crear
        {
            private lateinit var entidadNegocio: Cliente
            private lateinit var entidadDTO: ClienteDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                entidadDTO = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_crea_la_entidad()
            {
                val entidadConIdNulo = entidadNegocio.copiar(id = null)

                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .crear(entidadConIdNulo)

                val entidadRetornada = recursoTodasEntidades.crear(entidadDTO)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).crear(entidadConIdNulo)
            }

            @Nested
            inner class CreacionDeDatosIniciales
            {
                @BeforeEach
                fun prepararMocks()
                {
                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .crear(entidadNegocio)

                    doReturn(recursoEntidadEspecifica)
                        .`when`(recursoTodasEntidades)
                        .darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)

                    doReturn(mockRecursoRoles)
                        .`when`(recursoEntidadEspecifica)
                        .darRecursoRoles()

                    doReturn(mockRecurosUsuarios)
                        .`when`(recursoEntidadEspecifica)
                        .darRecursoUsuarios()
                }

                @Test
                fun inicializa_tabla_de_clientes_luego_crea_campos_de_persona_luego_rol_luego_usuario_y_luego_llave_nfc()
                {
                    val entidadConIdNulo = entidadNegocio.copiar(id = null)

                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .crear(entidadConIdNulo)

                    recursoTodasEntidades.crear(entidadDTO)

                    val ordenEsperado =
                            inOrder(
                                    mockDeDependencias,
                                    mockRepositorio,
                                    mockRepositorioCamposDePersonas,
                                    mockRecursoRoles,
                                    mockRecurosUsuarios,
                                    mockRepositorioLlavesNFC
                                   )

                    ordenEsperado.verify(mockDeDependencias).inicializarTablasNecesariasCliente(ID_ENTIDAD_PRUEBAS)
                    ordenEsperado.verify(mockRepositorioCamposDePersonas).crear(ID_ENTIDAD_PRUEBAS, ListadoCamposPredeterminados())
                    ordenEsperado.verify(mockRecursoRoles).crearRolParaConfiguracionInicial()
                    ordenEsperado.verify(mockRecurosUsuarios).crearUsuarioParaConfiguracionInicial()
                    ordenEsperado.verify(mockRepositorioLlavesNFC).crear(ID_ENTIDAD_PRUEBAS, Cliente.LlaveNFC(ID_ENTIDAD_PRUEBAS, LLAVE_NFC_PRUEBA))
                }
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                val entidadConIdNulo = entidadNegocio.copiar(id = null)

                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .crear(entidadConIdNulo)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoTodasEntidades.crear(entidadDTO) }

                assertEquals(ClienteDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(entidadConIdNulo)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun nombre_duplicado_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    val entidadConIdNulo = entidadNegocio.copiar(id = null)

                    doThrow(ErrorCreacionActualizacionPorDuplicidad(Cliente.NOMBRE_ENTIDAD))
                        .`when`(mockRepositorio)
                        .crear(entidadConIdNulo)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO) }

                    assertEquals(ClienteDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(entidadConIdNulo)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    val entidadConIdNulo = entidadNegocio.copiar(id = null)

                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Cliente"))
                        .`when`(mockRepositorio)
                        .crear(entidadConIdNulo)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO) }

                    assertEquals(ClienteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(entidadConIdNulo)
                }

                @Nested
                @DisplayName("Nombre inválido")
                inner class NombreInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_nombre_vacio()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(nombre = "")) }

                        assertEquals(ClienteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(entidadNegocio)
                    }

                    @Test
                    fun cuando_el_dto_tiene_nombre_con_solo_espacios_y_tabs()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(nombre = "      ")) }

                        assertEquals(ClienteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(entidadNegocio)
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    val entidadConIdNulo = entidadNegocio.copiar(id = null)

                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .crear(entidadConIdNulo)

                    recursoTodasEntidades.crear(entidadDTO)

                    verify(mockRepositorio).crear(entidadConIdNulo)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    doReturn(mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado)
                        .`when`(mockDeDependencias)
                        .manejadorSeguridad

                    // reinicializar mocks porque se están mockeando las depedencias que son parámetro de recurso de clientes
                    inicializarRecursoEntidadEspecifica()

                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.crear(entidadDTO) }

                    verify(mockRepositorio, times(0)).crear(entidadNegocio)
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
                doReturn(sequenceOf<Cliente>())
                    .`when`(mockRepositorio)
                    .listar()

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertTrue(listaRetornada.none())
                verify(mockRepositorio).listar()
            }

            @Test
            fun retorna_una_lista_de_dtos_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                val listaNegocio = List(5) { darEntidadNegocioSegunIndice(it) }
                val listaDTO = List(5) { darEntidadDTOSegunIndice(it) }
                doReturn(listaNegocio.asSequence())
                    .`when`(mockRepositorio)
                    .listar()

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listar()
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Cliente"))
                    .`when`(mockRepositorio)
                    .listar()

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.darTodas() }

                assertEquals(ClienteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listar()
            }

            @Nested
            inner class Permisos
            {
                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(sequenceOf<Cliente>())
                        .`when`(mockRepositorio)
                        .listar()

                    recursoTodasEntidades.darTodas()

                    verify(mockRepositorio).listar()
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    doReturn(mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado)
                        .`when`(mockDeDependencias)
                        .manejadorSeguridad

                    // reinicializar mocks porque se están mockeando las depedencias que son parámetro de recurso de clientes
                    inicializarRecursoEntidadEspecifica()

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
            fun inicializa_la_conexion_a_la_base_de_datos()
            {
                verify(mockRepositorio).inicializarConexionAEsquemaDeSerNecesario(ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_retorna_un_entidad()
            {
                val entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                val entidadDTO = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
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

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Ubicación"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<ErrorDesconocido> { recursoEntidadEspecifica.darPorId() }

                assertEquals(ClienteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
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

                    recursoEntidadEspecifica.darPorId()

                    verify(mockRepositorio).buscarPorId(ID_ENTIDAD_PRUEBAS)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    doReturn(mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado)
                        .`when`(mockDeDependencias)
                        .manejadorSeguridad

                    // reinicializar mocks porque se están mockeando las depedencias que son parámetro de recurso de clientes
                    inicializarRecursoEntidadEspecifica()

                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.darPorId() }

                    verify(mockRepositorio, times(0)).buscarPorId(ID_ENTIDAD_PRUEBAS)
                }
            }
        }

        @Nested
        @DisplayName("Al actualizar")
        inner class Actualizar
        {
            private lateinit var entidadNegocio: Cliente
            private lateinit var entidadDTO: ClienteDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                entidadDTO = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
            }


            @Test
            fun inicializa_la_conexion_a_la_base_de_datos()
            {
                verify(mockRepositorio).inicializarConexionAEsquemaDeSerNecesario(ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_actualiza_la_entidad()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTO)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)
            }

            @Test
            fun usa_el_id_de_la_ruta_cuando_el_id_de_la_entidad_no_coincide_con_el_de_la_ruta()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTO.copy(id = entidadDTO.id!! + 10))

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(ClienteDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_no_existe_cuando_el_repositorio_lanza_EntidadNoExiste()
            {
                doThrow(co.smartobjects.persistencia.excepciones.EntidadNoExiste(Random().nextLong(), "no importa"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun nombre_duplicado_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Ubicación"))
                        .`when`(mockRepositorio)
                        .actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(ClienteDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Cliente"))
                        .`when`(mockRepositorio)
                        .actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(ClienteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)
                }

                @Nested
                @DisplayName("Nombre inválido")
                inner class NombreInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_nombre_vacio()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(nombre = "")) }

                        assertEquals(ClienteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_dto_tiene_nombre_con_solo_espacios_y_tabs()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO.copy(nombre = "            ")) }

                        assertEquals(ClienteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_ENTIDAD_PRUEBAS, entidadNegocio)
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
                        .actualizar(entidadNegocio.id!!, entidadNegocio)

                    recursoEntidadEspecifica.actualizar(entidadDTO)

                    verify(mockRepositorio).actualizar(entidadNegocio.id!!, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    doReturn(mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado)
                        .`when`(mockDeDependencias)
                        .manejadorSeguridad

                    // reinicializar mocks porque se están mockeando las depedencias que son parámetro de recurso de clientes
                    inicializarRecursoEntidadEspecifica()

                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    verify(mockRepositorio, times(0)).actualizar(entidadNegocio.id!!, entidadNegocio)
                }
            }
        }

        @Nested
        @DisplayName("Al eliminar")
        inner class Eliminar
        {
            private lateinit var entidadNegocio: Cliente
            private lateinit var entidadDTO: ClienteDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                entidadDTO = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
            }

            @Test
            fun inicializa_la_conexion_a_la_base_de_datos()
            {
                verify(mockRepositorio).inicializarConexionAEsquemaDeSerNecesario(ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun no_lanza_excepcion_cuando_el_repositorio_retorna_true()
            {
                doReturn(true)
                    .`when`(mockRepositorio)
                    .eliminarPorId(entidadNegocio.id!!)

                recursoEntidadEspecifica.eliminarPorId()
                verify(mockRepositorio).eliminarPorId(entidadNegocio.id!!)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_no_existe_cuando_el_repositorio_retorna_false()
            {
                doReturn(false)
                    .`when`(mockRepositorio)
                    .eliminarPorId(entidadNegocio.id!!)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(entidadNegocio.id!!)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_desconocido_cuando_el_repositorio_lanza_ErrorEliminandoEntidad()
            {
                doThrow(co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad(1, Cliente.NOMBRE_ENTIDAD))
                    .`when`(mockRepositorio)
                    .eliminarPorId(entidadNegocio.id!!)

                val errorApi = assertThrows<ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(ClienteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(entidadNegocio.id!!)
            }

            @Nested
            inner class Permisos
            {
                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(true)
                        .`when`(mockRepositorio)
                        .eliminarPorId(entidadNegocio.id!!)

                    recursoEntidadEspecifica.eliminarPorId()
                    verify(mockRepositorio).eliminarPorId(entidadNegocio.id!!)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    doReturn(mockManejadorSeguridadUsuarioGlobalNuncaEstaAutenticado)
                        .`when`(mockDeDependencias)
                        .manejadorSeguridad

                    // reinicializar mocks porque se están mockeando las depedencias que son parámetro de recurso de clientes
                    inicializarRecursoEntidadEspecifica()

                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.eliminarPorId() }

                    verify(mockRepositorio, times(0)).eliminarPorId(entidadNegocio.id!!)
                }
            }
        }
    }
}