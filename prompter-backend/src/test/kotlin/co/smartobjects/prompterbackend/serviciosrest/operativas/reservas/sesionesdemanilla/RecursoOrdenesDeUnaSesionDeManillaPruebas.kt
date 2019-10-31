package co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla

import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.operativas.ordenes.IdSesionDeManillaParaConsultaOrdenes
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesDeUnaSesionDeManilla
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.excepciones.ErrorDesconocido
import co.smartobjects.prompterbackend.excepciones.UsuarioNoAutenticado
import co.smartobjects.prompterbackend.excepciones.UsuarioNoTienePermiso
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.utilidades.Decimal
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
import javax.ws.rs.client.WebTarget
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DisplayName("Recurso órdenes de una sesión de manilla")
internal class RecursoOrdenesDeUnaSesionDeManillaPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_ENTIDAD_PRUEBAS = 3L
    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Orden
    {
        return Orden(
                ID_CLIENTE,
                indice.toLong(),
                indice.toLong(),
                listOf(
                        Transaccion.Debito(
                                ID_CLIENTE,
                                indice.toLong(),
                                "usuario",
                                Decimal.UNO,
                                indice.toLong(),
                                "dispositivo",
                                ConsumibleEnPuntoDeVenta(indice.toLong(), indice.toLong(), "código externo fondo $indice")
                                          )
                      ),
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 1 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO)
                    )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): OrdenDTO
    {
        return OrdenDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoOrdenesDeUnaSesionDeManilla

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoOrdenesDeUnaSesionDeManilla::class.java)

            val mockRecursoSesionDeManilla = mockConDefaultAnswer(RecursoSesionesDeManilla.RecursoSesionDeManilla::class.java)
            val mockRecursoSesionesDeManilla = mockConDefaultAnswer(RecursoSesionesDeManilla::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoSesionesDeManilla).`when`(mockRecursoCliente).darRecursoSesionesDeManilla()
            doReturn(mockRecursoSesionDeManilla).`when`(mockRecursoSesionesDeManilla).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoSesionDeManilla).darRecursoOrdenesDeUnaSesionDeManilla()

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
                doReturn(sequenceOf<OrdenDTO>()).`when`(mockRecursoTodasEntidades).listar()
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoSesionesDeManilla.RUTA}/$ID_ENTIDAD_PRUEBAS/${RecursoOrdenesDeUnaSesionDeManilla.RUTA}")
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).listar()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioOrdenesDeUnaSesionDeManilla::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoOrdenesDeUnaSesionDeManilla by lazy {
            RecursoOrdenesDeUnaSesionDeManilla(ID_CLIENTE, ID_ENTIDAD_PRUEBAS, mockRepositorio, mockManejadorSeguridad)
        }

        @Nested
        @DisplayName("Al consultar todos")
        inner class ConsultarTodos
        {
            private val parametros = IdSesionDeManillaParaConsultaOrdenes(ID_ENTIDAD_PRUEBAS)
            @Test
            fun retorna_una_lista_vacia_de_dtos_cuando_el_repositorio_retorna_una_lista_vacia()
            {
                doReturn(emptySequence<Orden>())
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val listaRetornada = recursoTodasEntidades.listar()

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

                val listaRetornada = recursoTodasEntidades.listar()

                assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Orden"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.listar() }

                assertEquals(OrdenDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.listar() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "OrdenesDeUnaSesionDeManilla", PermisoBack.Accion.GET_TODOS)
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
                    doReturn(emptySequence<Orden>())
                        .`when`(mockRepositorio)
                        .listarSegunParametros(ID_CLIENTE, parametros)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.listar()
                    verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.listar() }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.listar() }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }
            }
        }
    }
}