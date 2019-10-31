package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles

import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.ubicaciones.consumibles.IdUbicacionConsultaConsumibles
import co.smartobjects.persistencia.ubicaciones.consumibles.ListaConsumiblesEnPuntoDeVentaUbicaciones
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioConsumibleEnPuntoDeVenta
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.RecursoUbicaciones
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.ubicaciones.consumibles.ConsumibleEnPuntoDeVentaDTO
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


@DisplayName("Recurso Consumibles En Punto De Venta")
internal class RecursoConsumiblesEnPuntoDeVentaPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_ENTIDAD_PRUEBAS = 3L
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): ConsumibleEnPuntoDeVenta
    {
        return ConsumibleEnPuntoDeVenta(
                indice.toLong(),
                indice + 2L,
                "código externo fondo $indice"
                                       )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): ConsumibleEnPuntoDeVentaDTO
    {
        return ConsumibleEnPuntoDeVentaDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoConsumiblesEnPuntoDeVenta

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoConsumiblesEnPuntoDeVenta::class.java)

            val mockRecursoUbicacion = mockConDefaultAnswer(RecursoUbicaciones.RecursoUbicacion::class.java)
            val mockRecursoUbicaciones = mockConDefaultAnswer(RecursoUbicaciones::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoUbicaciones).`when`(mockRecursoCliente).darRecursoUbicaciones()
            doReturn(mockRecursoUbicacion).`when`(mockRecursoUbicaciones).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoUbicacion).darRecursoConsumiblesEnPuntoDeVenta()

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
                val dtoPruebas = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                val entidadPruebas = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                doReturn(listOf(ConsumibleEnPuntoDeVentaDTO(entidadPruebas))).`when`(mockRecursoTodasEntidades).crear(listOf(dtoPruebas))

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUbicaciones.RUTA}/$ID_ENTIDAD_PRUEBAS/${RecursoConsumiblesEnPuntoDeVenta.RUTA}")
                    .request()
                    .put(Entity.entity(listOf(dtoPruebas), MediaType.APPLICATION_JSON_TYPE), GenericType.forInstance(arrayListOf(dtoPruebas)))

                verify(mockRecursoTodasEntidades).crear(listOf(dtoPruebas))
            }
        }

        @[Nested DisplayName("Al consultar todos")]
        inner class ConsultarTodos
        {
            @Test
            fun llama_la_funcion_listar()
            {
                doReturn(sequenceOf<ConsumibleEnPuntoDeVentaDTO>()).`when`(mockRecursoTodasEntidades).darTodas()
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoUbicaciones.RUTA}/$ID_ENTIDAD_PRUEBAS/${RecursoConsumiblesEnPuntoDeVenta.RUTA}")
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
        private val mockRepositorio = mockConDefaultAnswer(RepositorioConsumibleEnPuntoDeVenta::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoConsumiblesEnPuntoDeVenta by lazy { RecursoConsumiblesEnPuntoDeVenta(ID_CLIENTE, ID_ENTIDAD_PRUEBAS, mockRepositorio, mockManejadorSeguridad) }

        @Nested
        @DisplayName("Al crear")
        inner class Crear
        {
            private lateinit var entidadNegocio: ConsumibleEnPuntoDeVenta
            private lateinit var entidadDTO: ConsumibleEnPuntoDeVentaDTO
            private lateinit var listaEntidadNegocio: ListaConsumiblesEnPuntoDeVentaUbicaciones

            @BeforeEach
            private fun crearEntidadesDePrueba()
            {
                entidadNegocio = darEntidadNegocioSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                entidadDTO = darEntidadDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                listaEntidadNegocio = ListaConsumiblesEnPuntoDeVentaUbicaciones(setOf(entidadNegocio), entidadNegocio.idUbicacion)
            }

            @Test
            fun retorna_el_dto_correcto_cuando_el_repositorio_crea_la_entidad()
            {
                doReturn(listOf(entidadNegocio))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, listaEntidadNegocio)
                val entidadRetornada = recursoTodasEntidades.crear(listOf(entidadDTO))

                assertEquals(listOf(entidadDTO), entidadRetornada)
                verify(mockRepositorio).crear(ID_CLIENTE, listaEntidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadReferenciadaNoExiste_con_codigo_interno_entidad_referenciada_no_existe_cuando_el_repositorio_lanza_ErrorDeLlaveForanea()
            {
                doThrow(ErrorDeLlaveForanea(0, "Alguien no existe"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, listaEntidadNegocio)

                val errorApi = assertThrows<EntidadReferenciadaNoExiste> { recursoTodasEntidades.crear(listOf(entidadDTO)) }

                assertEquals(ConsumibleEnPuntoDeVentaDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, listaEntidadNegocio)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .crear(ID_CLIENTE, listaEntidadNegocio)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.crear(listOf(entidadDTO)) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).crear(ID_CLIENTE, listaEntidadNegocio)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun nombre_duplicado_cuando_el_repositorio_lanza_ErrorCreacionActualizacionPorDuplicidad()
                {
                    doThrow(ErrorCreacionActualizacionPorDuplicidad("Consumible"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, listaEntidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(listOf(entidadDTO)) }

                    assertEquals(ConsumibleEnPuntoDeVentaDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, listaEntidadNegocio)
                }

                @Test
                fun desconocido_cuando_el_repositorio_lanza_ErrorDeCreacionActualizacionEntidad()
                {
                    doThrow(ErrorDeCreacionActualizacionEntidad("Error creando Ubicacion"))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, listaEntidadNegocio)

                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.crear(listOf(entidadDTO)) }

                    assertEquals(ConsumibleEnPuntoDeVentaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                    verify(mockRepositorio).crear(ID_CLIENTE, listaEntidadNegocio)
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Consumibles En Punto De Venta", PermisoBack.Accion.PUT_TODOS)
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
                    doReturn(listOf(entidadNegocio))
                        .`when`(mockRepositorio)
                        .crear(ID_CLIENTE, listaEntidadNegocio)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.crear(listOf(entidadDTO))
                    verify(mockRepositorio).crear(ID_CLIENTE, listaEntidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.crear(listOf(entidadDTO)) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, listaEntidadNegocio)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.crear(listOf(entidadDTO)) }
                    verify(mockRepositorio, times(0)).crear(ID_CLIENTE, listaEntidadNegocio)
                }
            }
        }

        @Nested
        @DisplayName("Al consultar todos")
        inner class ConsultarTodos
        {
            private val parametros = IdUbicacionConsultaConsumibles(ID_ENTIDAD_PRUEBAS)
            @Test
            fun retorna_una_lista_vacia_de_dtos_cuando_el_repositorio_retorna_una_lista_vacia()
            {
                doReturn(sequenceOf<ConsumibleEnPuntoDeVenta>())
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertTrue(listaRetornada.none())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun retorna_una_lista_de_dtos_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                val listaNegocio = List(5) { darEntidadNegocioSegunIndice(it) }.asSequence()
                val listaDTO = List(5) { darEntidadDTOSegunIndice(it) }
                doReturn(listaNegocio)
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val listaRetornada = recursoTodasEntidades.darTodas()

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Consumible"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.darTodas() }

                assertEquals(ConsumibleEnPuntoDeVentaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.darTodas() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Consumibles En Punto De Venta", PermisoBack.Accion.GET_TODOS)
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
                    doReturn(sequenceOf<ConsumibleEnPuntoDeVenta>())
                        .`when`(mockRepositorio)
                        .listarSegunParametros(ID_CLIENTE, parametros)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.darTodas()
                    verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.darTodas() }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.darTodas() }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }
            }
        }
    }
}