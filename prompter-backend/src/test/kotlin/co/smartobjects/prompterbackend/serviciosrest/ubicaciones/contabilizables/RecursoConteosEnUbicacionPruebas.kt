package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioConteosUbicaciones
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.RecursoUbicaciones
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.ubicaciones.contabilizables.ConteoUbicacionDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.Year
import org.threeten.bp.ZonedDateTime
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import kotlin.test.assertEquals


class RecursoConteosEnUbicacionPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_UBICACION = 2L
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): ConteoUbicacion
    {
        return ConteoUbicacion(
                ID_CLIENTE,
                ID_UBICACION,
                indice.toLong(),
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 1 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO)
                              )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): ConteoUbicacionDTO
    {
        return ConteoUbicacionDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoConteosEnUbicacion

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoConteosEnUbicacion::class.java)

            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)
            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)

            val mockRecursoUbicaciones = mockConDefaultAnswer(RecursoUbicaciones::class.java)
            val mockRecursoUbicacion = mockConDefaultAnswer(RecursoUbicaciones.RecursoUbicacion::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoUbicaciones).`when`(mockRecursoCliente).darRecursoUbicaciones()
            doReturn(mockRecursoUbicacion).`when`(mockRecursoUbicaciones).darRecursosEntidadEspecifica(ID_UBICACION)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoUbicacion).darRecursoConteosEnUbicacion()

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
                val dtoPruebas = darEntidadDTOSegunIndice(ID_UBICACION.toInt())
                val entidadPruebas = darEntidadNegocioSegunIndice(ID_UBICACION.toInt())

                doReturn(ConteoUbicacionDTO(entidadPruebas)).`when`(mockRecursoTodasEntidades).crear(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUbicaciones.RUTA}/$ID_UBICACION/${RecursoConteosEnUbicacion.RUTA}").request()
                    .post(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), ConteoUbicacionDTO::class.java)

                verify(mockRecursoTodasEntidades).crear(dtoPruebas)
            }
        }
    }

    @[Nested DisplayName("Al llamar por código")]
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioConteosUbicaciones::class.java)

        private fun darRecursoTodasEntidadesConManejador(
                manejadorSeguridad: ManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
                                                        ): RecursoConteosEnUbicacion
        {
            return RecursoConteosEnUbicacion(
                    ID_CLIENTE,
                    ID_UBICACION,
                    mockRepositorio,
                    manejadorSeguridad
                                            )
        }

        @[Nested DisplayName("Al crear")]
        inner class Crear
        {
            private lateinit var entidadNegocio: ConteoUbicacion
            private lateinit var entidadDTO: ConteoUbicacionDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(ID_UBICACION.toInt())
                entidadDTO = darEntidadDTOSegunIndice(ID_UBICACION.toInt())
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_crea_la_entidad()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val entidadRetornada = darRecursoTodasEntidadesConManejador().crear(entidadDTO)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun ignora_el_id_de_ubicacion_en_el_dto_y_usa_el_de_la_ruta()
            {
                val entidadDTOConIdUbicacionErroneo = entidadDTO.copy(idUbicacion = ID_UBICACION + 100)
                val entidadDeNegocioErronea = entidadDTOConIdUbicacionErroneo.aEntidadDeNegocio()

                doReturn(entidadNegocio).`when`(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)

                doReturn(entidadDeNegocioErronea).`when`(mockRepositorio).crear(ID_CLIENTE, entidadDeNegocioErronea)

                val entidadRetornada = darRecursoTodasEntidadesConManejador().crear(entidadDTOConIdUbicacionErroneo)

                verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadDeNegocioErronea)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                assertEquals(entidadDTO, entidadRetornada)
            }

            @Test
            fun lanza_excepcion_EntidadReferenciadaNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> {
                    darRecursoTodasEntidadesConManejador().crear(entidadDTO)
                }

                assertEquals(ConteoUbicacionDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> {
                    darRecursoTodasEntidadesConManejador().crear(entidadDTO)
                }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando ConteoUbicacion"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> {
                        darRecursoTodasEntidadesConManejador().crear(entidadDTO)
                    }

                    assertEquals(ConteoUbicacionDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadNegocio)
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Conteo en Ubicación", PermisoBack.Accion.POST)
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
                    doReturn(entidadNegocio)
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
    }
}