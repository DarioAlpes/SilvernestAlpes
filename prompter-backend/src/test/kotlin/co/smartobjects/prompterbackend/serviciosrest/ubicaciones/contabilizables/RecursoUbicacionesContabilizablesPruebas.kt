package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioUbicacionesContabilizables
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.ubicaciones.contabilizables.UbicacionesContabilizablesDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.MediaType
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DisplayName("Recurso Ubicaciones Contabilizables")
internal class RecursoUbicacionesContabilizablesPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): UbicacionesContabilizables
    {
        return UbicacionesContabilizables(ID_CLIENTE, List((indice + 1) * 3) { it.toLong() }.toSet())
    }

    private fun darEntidadDTOSegunIndice(indice: Int): UbicacionesContabilizablesDTO
    {
        return UbicacionesContabilizablesDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoUbicacionesContabilizables

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoUbicacionesContabilizables::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoUbicacionesContabilizables()

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

        @[Nested DisplayName("Al crear")]
        inner class Crear
        {
            @Test
            fun llama_la_funcion_crear_con_dto_correcto()
            {
                val dtoPruebas = darEntidadDTOSegunIndice(0)

                doReturn(emptyList<Long>())
                    .`when`(mockRecursoTodasEntidades)
                    .crear(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUbicacionesContabilizables.RUTA}")
                    .request()
                    .put(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), GenericType.forInstance(emptyList<Long>()))

                verify(mockRecursoTodasEntidades).crear(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al consultar todos")]
        inner class ConsultarTodos
        {
            @Test
            fun llama_la_funcion_listar()
            {
                doReturn(emptySequence<Long>()).`when`(mockRecursoTodasEntidades).darTodas()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUbicacionesContabilizables.RUTA}")
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).darTodas()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioUbicacionesContabilizables::class.java)

        private fun darRecursoTodasEntidadesConManejador(manejadorSeguridad: ManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos) =
                RecursoUbicacionesContabilizables(ID_CLIENTE, mockRepositorio, manejadorSeguridad)

        @Nested
        @DisplayName("Al crear")
        inner class Crear
        {
            private lateinit var entidadNegocio: UbicacionesContabilizables
            private lateinit var entidadDTO: UbicacionesContabilizablesDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(0)
                entidadDTO = darEntidadDTOSegunIndice(0)
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_crea_la_entidad()
            {
                val listaRetornada = entidadNegocio.idsUbicaciones.toList()

                doReturn(listaRetornada)
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val entidadRetornada = darRecursoTodasEntidadesConManejador().crear(entidadDTO)

                assertEquals(listaRetornada, entidadRetornada)

                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadReferenciadaNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { darRecursoTodasEntidadesConManejador().crear(entidadDTO) }

                assertEquals(UbicacionesContabilizablesDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { darRecursoTodasEntidadesConManejador().crear(entidadDTO) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun entidad_duplicada_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Consumible"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { darRecursoTodasEntidadesConManejador().crear(entidadDTO) }

                    assertEquals(UbicacionesContabilizablesDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Ubicacion"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { darRecursoTodasEntidadesConManejador().crear(entidadDTO) }

                    assertEquals(UbicacionesContabilizablesDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Ubicaciones Contabilizables", PermisoBack.Accion.PUT_TODOS)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> =
                            RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos)
                                .darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(listOf(entidadNegocio))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    darRecursoTodasEntidadesConManejador(darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio))
                        .crear(entidadDTO)

                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    assertThrows<UsuarioNoAutenticado> {
                        darRecursoTodasEntidadesConManejador(mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado)
                            .crear(entidadDTO)
                    }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    assertThrows<UsuarioNoTienePermiso> {
                        darRecursoTodasEntidadesConManejador(darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio))
                            .crear(entidadDTO)
                    }

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
                doReturn(emptySequence<Long>())
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val listaRetornada = darRecursoTodasEntidadesConManejador().darTodas()

                assertTrue(listaRetornada.none())
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun retorna_una_lista_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                val entidadDeNegocio = darEntidadNegocioSegunIndice(0)
                val listaEsperada = entidadDeNegocio.idsUbicaciones.asSequence()

                doReturn(listaEsperada)
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val listaRetornada = darRecursoTodasEntidadesConManejador().darTodas()

                assertEquals(listaEsperada, listaRetornada)

                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Consumible"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<ErrorDesconocido> { darRecursoTodasEntidadesConManejador().darTodas() }

                assertEquals(UbicacionesContabilizablesDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<EntidadNoExiste> { darRecursoTodasEntidadesConManejador().darTodas() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Ubicaciones Contabilizables", PermisoBack.Accion.GET_TODOS)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(sequenceOf<Long>())
                        .`when`(mockRepositorio)
                        .listar(ID_CLIENTE)

                    darRecursoTodasEntidadesConManejador(darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio))
                        .darTodas()

                    verify(mockRepositorio).listar(ID_CLIENTE)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    assertThrows<UsuarioNoAutenticado> {
                        darRecursoTodasEntidadesConManejador(mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado)
                            .darTodas()
                    }

                    verify(mockRepositorio, times(0)).listar(ID_CLIENTE)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    assertThrows<UsuarioNoTienePermiso> {
                        darRecursoTodasEntidadesConManejador(darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio))
                            .darTodas()
                    }

                    verify(mockRepositorio, times(0)).listar(ID_CLIENTE)
                }
            }
        }
    }
}