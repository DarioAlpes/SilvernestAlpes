package co.smartobjects.prompterbackend.serviciosrest.personas.creditos

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditosDeUnaPersona
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.operativas.compras.FiltroCreditosPersona
import co.smartobjects.persistencia.operativas.compras.RepositorioCreditosDeUnaPersona
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.personas.RecursoPersonas
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.operativas.compras.CreditosDeUnaPersonaDTO
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
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import kotlin.test.assertEquals


@DisplayName("Recurso Créditos de Una Persona")
internal class RecursoCreditosDeUnaPersonaPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_ENTIDAD_PRUEBAS = 3L
        private val FECHA_PRUEBAS =
                ZonedDateTime.of(
                        LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value, 1, 1),
                        LocalTime.of(5, 5),
                        ZONA_HORARIA_POR_DEFECTO
                                )
        private val FECHA_PRUEBAS_STRING = FECHA_PRUEBAS.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): CreditosDeUnaPersona
    {
        val idPersonaDueña = indice.toLong() + 2

        return CreditosDeUnaPersona(
                ID_CLIENTE,
                idPersonaDueña,
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
                                idPersonaDueña,
                                indice.toLong() + 3,
                                "código externo fondo $indice",
                                indice.toLong() + 4,
                                "uuid-pc",
                                indice.toLong() + 5,
                                indice.toLong() + 6
                        )
                      ),
                listOf()
                                   )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): CreditosDeUnaPersonaDTO
    {
        return CreditosDeUnaPersonaDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoCreditosDeUnaPersona

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoCreditosDeUnaPersona::class.java)

            val mockRecursoPersona = mockConDefaultAnswer(RecursoPersonas.RecursoPersona::class.java)
            val mockRecursoPersonas = mockConDefaultAnswer(RecursoPersonas::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoPersonas).`when`(mockRecursoCliente).darRecursoPersonas()
            doReturn(mockRecursoPersona).`when`(mockRecursoPersonas).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)

            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoPersona).darRecursoCreditosDeUnaPersona()

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

        @[Nested DisplayName("Al consultar una")]
        inner class ConsultarUna
        {
            @Test
            fun llama_la_funcion_buscar_con_fecha_valida()
            {
                doReturn(darEntidadDTOSegunIndice(0))
                    .`when`(mockRecursoTodasEntidades)
                    .buscar(FECHA_PRUEBAS)

                target
                    .path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonas.RUTA}/$ID_ENTIDAD_PRUEBAS/${RecursoCreditosDeUnaPersona.RUTA}")
                    .queryParam(RecursoCreditosDeUnaPersona.NOMBRE_PARAMETRO_TIEMPO_CONSULTA, FECHA_PRUEBAS_STRING)
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).buscar(FECHA_PRUEBAS)
            }

            @Test
            fun llama_la_funcion_buscar_con_fecha_null()
            {
                doReturn(darEntidadDTOSegunIndice(0))
                    .`when`(mockRecursoTodasEntidades)
                    .buscar(null)

                target
                    .path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonas.RUTA}/$ID_ENTIDAD_PRUEBAS/${RecursoCreditosDeUnaPersona.RUTA}")
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).buscar(null)
            }
        }
    }

    @[Nested DisplayName("Al llamar por código")]
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioCreditosDeUnaPersona::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades by lazy {
            RecursoCreditosDeUnaPersona(ID_CLIENTE, ID_ENTIDAD_PRUEBAS, mockRepositorio, mockManejadorSeguridad)
        }

        @[Nested DisplayName("Al consultar una")]
        inner class ConsultarUna
        {
            private val parametros: FiltroCreditosPersona =
                    FiltroCreditosPersona.NoConsumidosValidosParaDia(ID_ENTIDAD_PRUEBAS, FECHA_PRUEBAS)

            private val entidadNegocioVacia =
                    CreditosDeUnaPersona(ID_CLIENTE, parametros.idPersona, emptyList(), emptyList())

            private val entidadDTOVacia =
                    CreditosDeUnaPersonaDTO(entidadNegocioVacia)

            @Test
            fun retorna_una_entidad_dto_con_listas_vacias_cuando_el_repositorio_retorna_una_entidad_con_listas_vacias()
            {
                doReturn(entidadNegocioVacia)
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val entidadRetorna = recursoTodasEntidades.buscar(FECHA_PRUEBAS)

                assertEquals(entidadDTOVacia, entidadRetorna)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun cambia_timezone_de_fecha_a_timezone_por_defecto()
            {
                val fechaConTimeZoneDiferente = FECHA_PRUEBAS.withZoneSameInstant(ZoneId.of("Australia/Sydney"))
                doReturn(entidadNegocioVacia)
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val entidadRetorna = recursoTodasEntidades.buscar(fechaConTimeZoneDiferente)

                assertEquals(entidadDTOVacia, entidadRetorna)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun retorna_una_lista_de_dtos_correcta_cuando_el_repositorio_retorna_una_lista_de_entidades()
            {
                doReturn(darEntidadNegocioSegunIndice(0))
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val entidadRetorna = recursoTodasEntidades.buscar(FECHA_PRUEBAS)

                assertEquals(darEntidadDTOSegunIndice(0), entidadRetorna)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("CreditosDeUnaPersona"))
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.buscar(FECHA_PRUEBAS) }

                assertEquals(CreditosDeUnaPersonaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.buscar(FECHA_PRUEBAS) }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun fecha_consulta_invalida_cuando_se_pasa_fecha_consulta_nula()
                {
                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.buscar(null) }

                    assertEquals(CreditosDeUnaPersonaDTO.CodigosError.FECHA_CONSULTA_INVALIDA, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Créditos de Persona", PermisoBack.Accion.GET_UNO)
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
                    doReturn(darEntidadNegocioSegunIndice(0))
                        .`when`(mockRepositorio)
                        .buscarSegunParametros(ID_CLIENTE, parametros)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.buscar(FECHA_PRUEBAS)
                    verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.buscar(FECHA_PRUEBAS) }
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.buscar(FECHA_PRUEBAS) }
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }
            }
        }
    }
}