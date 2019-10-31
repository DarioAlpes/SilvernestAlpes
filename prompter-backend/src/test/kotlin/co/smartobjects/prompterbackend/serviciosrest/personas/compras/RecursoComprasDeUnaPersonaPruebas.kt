package co.smartobjects.prompterbackend.serviciosrest.personas.compras

import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.operativas.compras.FiltroComprasPersonaConCreditosPresentesOFuturos
import co.smartobjects.persistencia.operativas.compras.RepositorioComprasDeUnaPersona
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.personas.RecursoPersonas
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import kotlin.test.assertEquals


@DisplayName("Recurso Compras de Una Persona")
internal class RecursoComprasDeUnaPersonaPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_ENTIDAD_PRUEBAS = 3L
        private val UUID_PRUEBAS: UUID = UUID.randomUUID()
        private val FECHA_PRUEBAS = ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO)
        private val FECHA_PRUEBAS_STRING = FECHA_PRUEBAS.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Compra
    {
        return Compra(
                ID_CLIENTE,
                "Un usuario",
                UUID_PRUEBAS,
                indice.toLong(),
                false,
                listOf(
                        CreditoFondo
                        (
                                ID_CLIENTE,
                                null,
                                Decimal.DIEZ,
                                Decimal.DIEZ,
                                Decimal.DIEZ,
                                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 2 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                                false,
                                "Taquilla",
                                "Un usuario",
                                indice.toLong() + 2,
                                indice.toLong() + 3,
                                "código externo fondo $indice",
                                indice.toLong() + 4,
                                "uuid-pc",
                                indice.toLong() + 5,
                                indice.toLong() + 6
                        )
                      ),
                listOf(),
                listOf(
                        Pago(Decimal.DIEZ, Pago.MetodoDePago.EFECTIVO, indice.toString())
                      ),
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 1 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO)
                     )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): CompraDTO
    {
        return CompraDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoComprasDeUnaPersona

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoComprasDeUnaPersona::class.java)

            val mockRecursoPersona = mockConDefaultAnswer(RecursoPersonas.RecursoPersona::class.java)
            val mockRecursoPersonas = mockConDefaultAnswer(RecursoPersonas::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoPersonas).`when`(mockRecursoCliente).darRecursoPersonas()
            doReturn(mockRecursoPersona).`when`(mockRecursoPersonas).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoPersona).darRecursoComprasDeUnaPersona()

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
            fun llama_la_funcion_listar_con_fecha_valida()
            {
                doReturn(sequenceOf<CompraDTO>()).`when`(mockRecursoTodasEntidades).listar(FECHA_PRUEBAS)
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonas.RUTA}/$ID_ENTIDAD_PRUEBAS/${RecursoComprasDeUnaPersona.RUTA}")
                    .queryParam(RecursoComprasDeUnaPersona.NOMBRE_PARAMETRO_TIEMPO_CONSULTA, FECHA_PRUEBAS_STRING)
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).listar(FECHA_PRUEBAS)
            }

            @Test
            fun llama_la_funcion_listar_con_fecha_null()
            {
                doReturn(sequenceOf<CompraDTO>()).`when`(mockRecursoTodasEntidades).listar(null)
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonas.RUTA}/$ID_ENTIDAD_PRUEBAS/${RecursoComprasDeUnaPersona.RUTA}").request().get(String::class.java)

                verify(mockRecursoTodasEntidades).listar(null)
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioComprasDeUnaPersona::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoComprasDeUnaPersona by lazy { RecursoComprasDeUnaPersona(ID_CLIENTE, ID_ENTIDAD_PRUEBAS, mockRepositorio, mockManejadorSeguridad) }

        @Nested
        @DisplayName("Al consultar todos")
        inner class ConsultarTodos
        {
            private val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(ID_ENTIDAD_PRUEBAS, FECHA_PRUEBAS)
            @Test
            fun retorna_una_lista_vacia_de_dtos_cuando_el_repositorio_retorna_una_lista_vacia()
            {
                doReturn(sequenceOf<Compra>())
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val listaRetornada = recursoTodasEntidades.listar(FECHA_PRUEBAS)

                Assertions.assertTrue(listaRetornada.none())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun cambia_timezone_de_fecha_a_timezone_por_defecto_cuando_se_envia_otro_timezone()
            {
                val fechaConTimeZoneDiferente = FECHA_PRUEBAS.withZoneSameInstant(ZoneId.of("Australia/Sydney"))
                doReturn(sequenceOf<Compra>())
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val listaRetornada = recursoTodasEntidades.listar(fechaConTimeZoneDiferente)

                Assertions.assertTrue(listaRetornada.none())
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

                val listaRetornada = recursoTodasEntidades.listar(FECHA_PRUEBAS)

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Compra"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.listar(FECHA_PRUEBAS) }

                Assertions.assertEquals(CompraDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.listar(FECHA_PRUEBAS) }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun fecha_consulta_invalida_cuando_se_pasa_fecha_consulta_nula()
                {
                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.listar(null) }

                    Assertions.assertEquals(CompraDTO.CodigosError.FECHA_CONSULTA_INVALIDA, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Compras de Persona", PermisoBack.Accion.GET_TODOS)
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
                    doReturn(sequenceOf<Compra>())
                        .`when`(mockRepositorio)
                        .listarSegunParametros(ID_CLIENTE, parametros)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.listar(FECHA_PRUEBAS)
                    verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.listar(FECHA_PRUEBAS) }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.listar(FECHA_PRUEBAS) }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }
            }
        }
    }
}