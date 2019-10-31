package co.smartobjects.prompterbackend.serviciosrest.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.fondos.PaqueteDTO
import co.smartobjects.red.modelos.fondos.PaquetePatchDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.threeten.bp.*
import java.util.*
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DisplayName("Recurso Paquetes")
internal class RecursoPaquetesPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_ENTIDAD_PRUEBAS = 3L
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Paquete
    {
        return Paquete(
                ID_CLIENTE,
                indice.toLong(),
                "Entidad prueba $indice",
                "Entidad prueba $indice",
                indice % 2 == 0,
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 1 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 1 + indice, 1, 2), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                List(indice + 1) {
                    Paquete.FondoIncluido(
                            indice.toLong() + 10 * it,
                            "El código externo $it",
                            Decimal(1000) * indice * it
                                         )
                },
                "El código externo $indice"
                      )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): PaqueteDTO
    {
        return PaqueteDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoPaquetes
        private lateinit var mockRecursoEntidadEspecifica: RecursoPaquetes.RecursoPaquete

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoEntidadEspecifica = mockConDefaultAnswer(RecursoPaquetes.RecursoPaquete::class.java)
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoPaquetes::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoPaquetes()
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
                doReturn(sequenceOf<PaqueteDTO>()).`when`(mockRecursoTodasEntidades).darTodas()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPaquetes.RUTA}").request().get(String::class.java)

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
                doReturn(PaqueteDTO(entidadPruebas)).`when`(mockRecursoTodasEntidades).crear(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPaquetes.RUTA}").request()
                    .post(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), PaqueteDTO::class.java)

                verify(mockRecursoTodasEntidades).crear(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al consultar una")]
        inner class ConsultarUna
        {
            @Test
            fun llama_la_funcion_darPorId_con_dto_correcto()
            {
                val entidadPruebas = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                doReturn(PaqueteDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).darPorId()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPaquetes.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .get(PaqueteDTO::class.java)

                verify(mockRecursoEntidadEspecifica).darPorId()
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
                doReturn(PaqueteDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPaquetes.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .put(Entity.entity(dtoPruebas, MediaType.APPLICATION_JSON_TYPE), PaqueteDTO::class.java)

                verify(mockRecursoEntidadEspecifica).actualizar(dtoPruebas)
            }
        }

        @[Nested DisplayName("Al hacer patch")]
        inner class Patch
        {
            @Test
            fun llama_la_funcion_actualizar_con_dto_correcto()
            {
                val dtoPruebas = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                val entidadPatch = PaquetePatchDTO(dtoPruebas.nombre, dtoPruebas.descripcion, dtoPruebas.disponibleParaLaVenta)
                doNothing().`when`(mockRecursoEntidadEspecifica).patch(entidadPatch)

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPaquetes.RUTA}/$ID_ENTIDAD_PRUEBAS")
                    .request()
                    .patch(dtoPruebas, PaquetePatchDTO::class.java)

                verify(mockRecursoEntidadEspecifica).patch(entidadPatch)
            }
        }

        @[Nested DisplayName("Al eliminar")]
        inner class Eliminar
        {
            @Test
            fun llama_la_funcion_eliminarPorId_con_dto_correcto()
            {
                doNothing().`when`(mockRecursoEntidadEspecifica).eliminarPorId()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPaquetes.RUTA}/$ID_ENTIDAD_PRUEBAS").request()
                    .delete()

                verify(mockRecursoEntidadEspecifica).eliminarPorId()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioPaquetes::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoPaquetes by lazy { RecursoPaquetes(ID_CLIENTE, mockRepositorio, mockManejadorSeguridad) }
        private val recursoEntidadEspecifica: RecursoPaquetes.RecursoPaquete by lazy { recursoTodasEntidades.darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS) }

        @Nested
        @DisplayName("Al crear")
        inner class Crear
        {
            private lateinit var entidadNegocio: Paquete
            private lateinit var entidadDTO: PaqueteDTO

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
                    .crear(ID_CLIENTE, entidadConIdNulo)

                val entidadRetornada = recursoTodasEntidades.crear(entidadDTO)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadConIdNulo)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                val entidadConIdNulo = entidadNegocio.copiar(id = null)

                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadConIdNulo)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoTodasEntidades.crear(entidadDTO) }

                assertEquals(PaqueteDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadConIdNulo)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                val entidadConIdNulo = entidadNegocio.copiar(id = null)

                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, entidadConIdNulo)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.crear(entidadDTO) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, entidadConIdNulo)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun nombre_duplicado_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    val entidadConIdNulo = entidadNegocio.copiar(id = null)

                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Paquete"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadConIdNulo)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO) }

                    assertEquals(PaqueteDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadConIdNulo)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    val entidadConIdNulo = entidadNegocio.copiar(id = null)

                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Sku"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadConIdNulo)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO) }

                    assertEquals(PaqueteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, entidadConIdNulo)
                }

                @Nested
                @DisplayName("Nombre inválido")
                inner class NombreInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_nombre_vacio()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(nombre = "")) }

                        assertEquals(PaqueteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_dto_tiene_nombre_con_solo_espacios_y_tabs()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(nombre = "      ")) }

                        assertEquals(PaqueteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Descripción invalida")
                inner class DescripcionInvalida
                {
                    @Test
                    fun cuando_el_dto_tiene_vacio()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(descripcion = "")) }

                        assertEquals(PaqueteDTO.CodigosError.DESCRIPCION_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_dto_tiene_solo_espacios_y_tabs()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(descripcion = "      ")) }

                        assertEquals(PaqueteDTO.CodigosError.DESCRIPCION_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Fecha desde invalida")
                inner class FechaDesdeInvalida
                {
                    @Test
                    fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                    {
                        val entidadDTOErronea = entidadDTO.copy(validoDesde = entidadDTO.validoDesde.withZoneSameInstant(ZoneId.of("America/Bogota")))
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOErronea) }

                        assertEquals(PaqueteDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun cuando_en_el_dto_es_inferior_a_la_fecha_minima_creacion_paquete()
                    {
                        val fechaErronea = FECHA_MINIMA_CREACION.minusNanos(1)
                        val entidadDTOErronea = entidadDTO.copy(validoDesde = fechaErronea)
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOErronea) }

                        assertEquals(PaqueteDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }

                    @Test
                    fun cuando_en_el_dto_es_superior_a_la_fecha_final()
                    {
                        val fechaErronea = entidadDTO.validoHasta.plusDays(10)
                        val entidadDTOErronea = entidadDTO.copy(validoDesde = fechaErronea)
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOErronea) }

                        assertEquals(PaqueteDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Fecha Hasta invalida")
                inner class FechaHastaInvalida
                {
                    @Test
                    fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                    {
                        val entidadDTOErronea = entidadDTO.copy(validoHasta = entidadDTO.validoHasta.withZoneSameInstant(ZoneId.of("America/Bogota")))
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTOErronea) }

                        assertEquals(PaqueteDTO.CodigosError.FECHA_VALIDEZ_HASTA_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Fondos incluidos inválidos")
                inner class FondosIncluidosInvalidos
                {
                    @Test
                    fun cuando_en_el_dto_estan_vacios()
                    {
                        val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(entidadDTO.copy(fondosIncluidos = listOf())) }

                        assertEquals(PaqueteDTO.CodigosError.FONDOS_INCLUIDOS_INVALIDOS, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).crear(ID_CLIENTE, entidadNegocio)
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Paquetes", PermisoBack.Accion.POST)
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
                    val entidadConIdNulo = entidadNegocio.copiar(id = null)

                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, entidadConIdNulo)

                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)

                    recursoTodasEntidades.crear(entidadDTO)

                    verify(mockRepositorio).crear(ID_CLIENTE, entidadConIdNulo)
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
                doReturn(sequenceOf<Paquete>())
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertTrue(listaRetornada.none())
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun retorna_una_lista_de_dtos_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                val listaNegocio = List(5) { darEntidadNegocioSegunIndice(it) }
                val listaDTO = List(5) { darEntidadDTOSegunIndice(it) }
                doReturn(listaNegocio.asSequence())
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listar(ID_CLIENTE)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Paquete"))
                    .`when`(mockRepositorio)
                    .listar(ID_CLIENTE)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.darTodas() }

                assertEquals(PaqueteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
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
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Paquetes", PermisoBack.Accion.GET_TODOS)
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
                    doReturn(sequenceOf<Paquete>())
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
                val entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                val entidadDTO = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
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

                assertEquals(PaqueteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Paquete"))
                    .`when`(mockRepositorio)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<ErrorDesconocido> { recursoEntidadEspecifica.darPorId() }

                assertEquals(PaqueteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
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
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Paquetes", PermisoBack.Accion.GET_UNO)
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
                    val entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
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
            private lateinit var entidadNegocio: Paquete
            private lateinit var entidadDTO: PaqueteDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                entidadDTO = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_actualiza_la_entidad()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTO)

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
            }

            @Test
            fun usa_el_id_de_la_ruta_cuando_el_id_de_la_entidad_no_coincide_con_el_de_la_ruta()
            {
                doReturn(entidadNegocio)
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)

                val entidadRetornada = recursoEntidadEspecifica.actualizar(entidadDTO.copy(id = entidadDTO.id!! + 10))

                assertEquals(entidadDTO, entidadRetornada)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(PaqueteDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_no_existe_cuando_el_repositorio_lanza_EntidadNoExiste()
            {
                doThrow(co.smartobjects.persistencia.excepciones.EntidadNoExiste(Random().nextLong(), "no importa"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(PaqueteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun nombre_duplicado_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Paquete"))
                        .`when`(mockRepositorio)
                        .actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(PaqueteDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Paquete"))
                        .`when`(mockRepositorio)
                        .actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.actualizar(entidadDTO) }

                    assertEquals(PaqueteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                }

                @Nested
                @DisplayName("Nombre inválido")
                inner class NombreInvalido
                {
                    @Test
                    fun cuando_el_dto_tiene_nombre_vacio()
                    {
                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTO.copy(nombre = ""))
                        }

                        assertEquals(PaqueteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_dto_tiene_nombre_con_solo_espacios_y_tabs()
                    {
                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTO.copy(nombre = "            "))
                        }

                        assertEquals(PaqueteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Descripción invalida")
                inner class DescripcionInvalida
                {
                    @Test
                    fun cuando_el_dto_tiene_vacio()
                    {
                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTO.copy(descripcion = ""))
                        }

                        assertEquals(PaqueteDTO.CodigosError.DESCRIPCION_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    }

                    @Test
                    fun cuando_el_dto_tiene_solo_espacios_y_tabs()
                    {
                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTO.copy(descripcion = "      "))
                        }

                        assertEquals(PaqueteDTO.CodigosError.DESCRIPCION_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Fecha desde invalida")
                inner class FechaDesdeInvalida
                {
                    @Test
                    fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                    {
                        val entidadDTOErronea = entidadDTO.copy(validoDesde = entidadDTO.validoDesde.withZoneSameInstant(ZoneId.of("America/Bogota")))

                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTOErronea)
                        }

                        assertEquals(PaqueteDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    }

                    @Test
                    fun cuando_en_el_dto_es_inferior_a_la_fecha_minima_creacion_paquete()
                    {
                        val fechaErronea = FECHA_MINIMA_CREACION.minusNanos(1)
                        val entidadDTOErronea = entidadDTO.copy(validoDesde = fechaErronea)

                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTOErronea)
                        }

                        assertEquals(PaqueteDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    }

                    @Test
                    fun cuando_en_el_dto_es_superior_a_la_fecha_final()
                    {
                        val fechaErronea = entidadDTO.validoHasta.plusDays(10)
                        val entidadDTOErronea = entidadDTO.copy(validoDesde = fechaErronea)

                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTOErronea)
                        }

                        assertEquals(PaqueteDTO.CodigosError.FECHA_VALIDEZ_DESDE_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Fecha Hasta invalida")
                inner class FechaHastaInvalida
                {
                    @Test
                    fun cuando_el_dto_tiene_una_zona_horaria_diferente_a_la_por_defecto()
                    {
                        val entidadDTOErronea = entidadDTO.copy(validoHasta = entidadDTO.validoHasta.withZoneSameInstant(ZoneId.of("America/Bogota")))

                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTOErronea)
                        }

                        assertEquals(PaqueteDTO.CodigosError.FECHA_VALIDEZ_HASTA_INVALIDA, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    }
                }

                @Nested
                @DisplayName("Fondos incluidos inválidos")
                inner class FondosIncluidosInvalidos
                {
                    @Test
                    fun cuando_en_el_dto_estan_vacios()
                    {
                        val errorApi = assertThrows<EntidadInvalida> {
                            recursoEntidadEspecifica.actualizar(entidadDTO.copy(fondosIncluidos = listOf()))
                        }

                        assertEquals(PaqueteDTO.CodigosError.FONDOS_INCLUIDOS_INVALIDOS, errorApi.codigoInterno)
                        verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    }
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Paquetes", PermisoBack.Accion.PUT)
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
                        .actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.actualizar(entidadDTO)
                    verify(mockRepositorio).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.actualizar(entidadDTO) }
                    verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.actualizar(entidadDTO) }
                    verify(mockRepositorio, times(0)).actualizar(ID_CLIENTE, entidadNegocio.id!!, entidadNegocio)
                }
            }
        }

        @Nested
        @DisplayName("Al hacer patch")
        inner class Patch
        {
            private lateinit var entidadNegocio: Paquete
            private lateinit var entidadDTO: PaqueteDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                entidadDTO = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
            }

            @Test
            fun funciona_correctamente_cuando_el_repositorio_actualiza_la_entidad()
            {
                val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, entidadDTO.descripcion, entidadDTO.disponibleParaLaVenta)

                val nombreNuevo = Paquete.CampoNombre(entidadDTO.nombre)
                val descripcionNuevo = Paquete.CampoDescripcion(entidadDTO.descripcion)
                val disponibleParaLaVentaNuevo = Paquete.CampoDisponibleParaLaVenta(entidadDTO.disponibleParaLaVenta)

                val camposAActualizar =
                        mapOf<String, CampoModificable<Paquete, *>>(
                                nombreNuevo.nombreCampo to nombreNuevo,
                                descripcionNuevo.nombreCampo to descripcionNuevo,
                                disponibleParaLaVentaNuevo.nombreCampo to disponibleParaLaVentaNuevo
                                                                   )

                doNothing()
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)

                recursoEntidadEspecifica.patch(entidadPatch)

                verify(mockRepositorio).actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
            }

            @Test
            fun si_el_nombre_es_nulo_no_se_agrega_campo_de_nombre_a_la_lista_de_campos_por_actualizar()
            {
                val entidadPatch = PaquetePatchDTO(null, entidadDTO.descripcion, entidadDTO.disponibleParaLaVenta)

                val descripcionNuevo = Paquete.CampoDescripcion(entidadDTO.descripcion)
                val disponibleParaLaVentaNuevo = Paquete.CampoDisponibleParaLaVenta(entidadDTO.disponibleParaLaVenta)

                val camposAActualizar =
                        mapOf<String, CampoModificable<Paquete, *>>(
                                descripcionNuevo.nombreCampo to descripcionNuevo,
                                disponibleParaLaVentaNuevo.nombreCampo to disponibleParaLaVentaNuevo
                                                                   )

                doNothing()
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)

                recursoEntidadEspecifica.patch(entidadPatch)

                verify(mockRepositorio).actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
            }

            @Test
            fun si_el_descripcion_es_nulo_no_se_agrega_campo_de_descripcion_a_la_lista_de_campos_por_actualizar()
            {
                val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, null, entidadDTO.disponibleParaLaVenta)

                val nombreNuevo = Paquete.CampoNombre(entidadDTO.nombre)
                val disponibleParaLaVentaNuevo = Paquete.CampoDisponibleParaLaVenta(entidadDTO.disponibleParaLaVenta)

                val camposAActualizar =
                        mapOf<String, CampoModificable<Paquete, *>>(
                                nombreNuevo.nombreCampo to nombreNuevo,
                                disponibleParaLaVentaNuevo.nombreCampo to disponibleParaLaVentaNuevo
                                                                   )

                doNothing()
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)

                recursoEntidadEspecifica.patch(entidadPatch)

                verify(mockRepositorio).actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
            }

            @Test
            fun si_el_disponible_para_la_venta_es_nulo_no_se_agrega_campo_de_disponible_para_la_venta_a_la_lista_de_campos_por_actualizar()
            {
                val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, entidadDTO.descripcion, null)

                val nombreNuevo = Paquete.CampoNombre(entidadDTO.nombre)
                val descripcionNuevo = Paquete.CampoDescripcion(entidadDTO.descripcion)

                val camposAActualizar =
                        mapOf<String, CampoModificable<Paquete, *>>(
                                nombreNuevo.nombreCampo to nombreNuevo,
                                descripcionNuevo.nombreCampo to descripcionNuevo
                                                                   )

                doNothing()
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)

                recursoEntidadEspecifica.patch(entidadPatch)

                verify(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, entidadDTO.descripcion, null)

                val nombreNuevo = Paquete.CampoNombre(entidadDTO.nombre)
                val descripcionNuevo = Paquete.CampoDescripcion(entidadDTO.descripcion)

                val camposAActualizar =
                        mapOf<String, CampoModificable<Paquete, *>>(
                                nombreNuevo.nombreCampo to nombreNuevo,
                                descripcionNuevo.nombreCampo to descripcionNuevo
                                                                   )

                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.patch(entidadPatch) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio)
                    .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                private lateinit var camposAActualizar: Map<String, CampoModificable<Paquete, *>>

                @BeforeEach
                private fun crearEntidadesDePrueba()
                {
                    val nombreNuevo = Paquete.CampoNombre(entidadDTO.nombre)
                    val descripcionNuevo = Paquete.CampoDescripcion(entidadDTO.descripcion)
                    val disponibleParaLaVentaNuevo = Paquete.CampoDisponibleParaLaVenta(entidadDTO.disponibleParaLaVenta)

                    camposAActualizar =
                            mapOf<String, CampoModificable<Paquete, *>>(
                                    nombreNuevo.nombreCampo to nombreNuevo,
                                    descripcionNuevo.nombreCampo to descripcionNuevo,
                                    disponibleParaLaVentaNuevo.nombreCampo to disponibleParaLaVentaNuevo
                                                                       )
                }

                @Test
                fun nombre_invalido_cuando_el_nombre_recibido_es_vacio()
                {
                    val entidadPatch = PaquetePatchDTO("", entidadDTO.descripcion, entidadDTO.disponibleParaLaVenta)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(PaqueteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
                }

                @Test
                fun nombre_invalido_cuando_el_nombre_recibido_tiene_espacios_o_tabs()
                {
                    val entidadPatch = PaquetePatchDTO("    \t    \t", entidadDTO.descripcion, entidadDTO.disponibleParaLaVenta)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(PaqueteDTO.CodigosError.NOMBRE_INVALIDO, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
                }

                @Test
                fun descripcion_invalida_cuando_la_descripcion_recibida_es_vacio()
                {
                    val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, "", entidadDTO.disponibleParaLaVenta)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(PaqueteDTO.CodigosError.DESCRIPCION_INVALIDA, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
                }

                @Test
                fun descripcion_invalida_cuando_la_descripcion_recibida_tiene_espacios_o_tabs()
                {
                    val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, "    \t    \t", entidadDTO.disponibleParaLaVenta)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(PaqueteDTO.CodigosError.DESCRIPCION_INVALIDA, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0))
                        .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, entidadDTO.descripcion, entidadDTO.disponibleParaLaVenta)

                    doThrow(ErrorDeCreacionActualizacionEntidad("no importa"))
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)

                    val errorApi = assertThrows<EntidadInvalida> { recursoEntidadEspecifica.patch(entidadPatch) }

                    assertEquals(PaqueteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Paquetes", PermisoBack.Accion.PATCH)
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
                    val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, entidadDTO.descripcion, entidadDTO.disponibleParaLaVenta)

                    val nombreNuevo = Paquete.CampoNombre(entidadDTO.nombre)
                    val descripcionNuevo = Paquete.CampoDescripcion(entidadDTO.descripcion)
                    val disponibleParaLaVentaNuevo = Paquete.CampoDisponibleParaLaVenta(entidadDTO.disponibleParaLaVenta)

                    val camposAActualizar =
                            mapOf<String, CampoModificable<Paquete, *>>(
                                    nombreNuevo.nombreCampo to nombreNuevo,
                                    descripcionNuevo.nombreCampo to descripcionNuevo,
                                    disponibleParaLaVentaNuevo.nombreCampo to disponibleParaLaVentaNuevo
                                                                       )

                    doNothing()
                        .`when`(mockRepositorio)
                        .actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.patch(entidadPatch)
                    verify(mockRepositorio).actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, entidadDTO.descripcion, entidadDTO.disponibleParaLaVenta)

                    val nombreNuevo = Paquete.CampoNombre(entidadDTO.nombre)
                    val descripcionNuevo = Paquete.CampoDescripcion(entidadDTO.descripcion)
                    val disponibleParaLaVentaNuevo = Paquete.CampoDisponibleParaLaVenta(entidadDTO.disponibleParaLaVenta)

                    val camposAActualizar =
                            mapOf<String, CampoModificable<Paquete, *>>(
                                    nombreNuevo.nombreCampo to nombreNuevo,
                                    descripcionNuevo.nombreCampo to descripcionNuevo,
                                    disponibleParaLaVentaNuevo.nombreCampo to disponibleParaLaVentaNuevo
                                                                       )

                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.patch(entidadPatch) }

                    verify(mockRepositorio, times(0)).actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    val entidadPatch = PaquetePatchDTO(entidadDTO.nombre, entidadDTO.descripcion, entidadDTO.disponibleParaLaVenta)

                    val nombreNuevo = Paquete.CampoNombre(entidadDTO.nombre)
                    val descripcionNuevo = Paquete.CampoDescripcion(entidadDTO.descripcion)
                    val disponibleParaLaVentaNuevo = Paquete.CampoDisponibleParaLaVenta(entidadDTO.disponibleParaLaVenta)

                    val camposAActualizar =
                            mapOf<String, CampoModificable<Paquete, *>>(
                                    nombreNuevo.nombreCampo to nombreNuevo,
                                    descripcionNuevo.nombreCampo to descripcionNuevo,
                                    disponibleParaLaVentaNuevo.nombreCampo to disponibleParaLaVentaNuevo
                                                                       )

                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.patch(entidadPatch) }

                    verify(mockRepositorio, times(0)).actualizarCamposIndividuales(ID_CLIENTE, entidadDTO.id!!, camposAActualizar)
                }
            }
        }

        @Nested
        @DisplayName("Al eliminar")
        inner class Eliminar
        {
            private lateinit var entidadNegocio: Paquete
            private lateinit var entidadDTO: PaqueteDTO

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                entidadDTO = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
            }

            @Test
            fun no_lanza_excepcion_cuando_el_repositorio_retorna_true()
            {
                doReturn(true)
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)

                recursoEntidadEspecifica.eliminarPorId()
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_no_existe_cuando_el_repositorio_retorna_false()
            {
                doReturn(false)
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(PaqueteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_entidad_referenciada_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Error eliminando acceso"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(PaqueteDTO.CodigosError.ENTIDAD_REFERENCIADA, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)
            }

            @Test
            fun lanza_excepcion_con_codigo_interno_desconocido_cuando_el_repositorio_lanza_ErrorEliminandoEntidad()
            {
                doThrow(ErrorEliminandoEntidad(1, "Nombre entidad"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)

                val errorApi = assertThrows<co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(PaqueteDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.eliminarPorId() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Paquetes", PermisoBack.Accion.DELETE)
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
                        .eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoEntidadEspecifica.eliminarPorId()
                    verify(mockRepositorio).eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.eliminarPorId() }
                    verify(mockRepositorio, times(0)).eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.eliminarPorId() }
                    verify(mockRepositorio, times(0)).eliminarPorId(ID_CLIENTE, entidadNegocio.id!!)
                }
            }
        }
    }
}