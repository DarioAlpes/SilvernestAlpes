package co.smartobjects.prompterbackend.serviciosrest.personas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.operativas.compras.NumeroTransaccionPago
import co.smartobjects.persistencia.operativas.compras.RepositorioPersonasDeUnaCompra
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.operativas.compras.PagoDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget


@DisplayName("Recurso Personas de una Compra")
internal class RecursoPersonasDeUnaCompraPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val NUMERO_TRANSACCION_PRUEBAS = "123"

    }

    private fun darEntidadNegocioSegunIndice(indice: Int): Persona
    {
        return Persona(
                RecursoPersonasPruebas.ID_CLIENTE,
                indice.toLong(),
                "Entidad prueba $indice",
                Persona.TipoDocumento.values()[indice % Persona.TipoDocumento.values().size],
                "Documento $indice",
                Persona.Genero.values()[indice % Persona.Genero.values().size],
                LocalDate.of(1980 + (indice % (LocalDate.now().year - 1980)), 1, 1),
                Persona.Categoria.values()[indice % Persona.Categoria.values().size],
                Persona.Afiliacion.values()[indice % Persona.Afiliacion.values().size],
                indice % 2 == 0,
                "Llave $indice"
                      )
    }

    private fun darEntidadDTOSegunIndice(indice: Int): PersonaDTO
    {
        return PersonaDTO(darEntidadNegocioSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoPersonasDeUnaCompra

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoPersonasDeUnaCompra::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoPersonasDeUnaCompra()

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
            fun llama_la_funcion_listar_con_numero_transaccion_valido()
            {
                doReturn(sequenceOf<PersonaDTO>()).`when`(mockRecursoTodasEntidades).listar(NUMERO_TRANSACCION_PRUEBAS)
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonasDeUnaCompra.RUTA}")
                    .queryParam(RecursoPersonasDeUnaCompra.NOMBRE_PARAMETRO_NUMERO_TRANSACCION, NUMERO_TRANSACCION_PRUEBAS)
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).listar(NUMERO_TRANSACCION_PRUEBAS)
            }

            @Test
            fun llama_la_funcion_listar_con_numero_transaccion_null()
            {
                doReturn(sequenceOf<PersonaDTO>()).`when`(mockRecursoTodasEntidades).listar(null)
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonasDeUnaCompra.RUTA}").request().get(String::class.java)

                verify(mockRecursoTodasEntidades).listar(null)
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioPersonasDeUnaCompra::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoTodasEntidades: RecursoPersonasDeUnaCompra by lazy { RecursoPersonasDeUnaCompra(ID_CLIENTE, mockRepositorio, mockManejadorSeguridad) }

        @Nested
        @DisplayName("Al consultar todos")
        inner class ConsultarTodos
        {
            private val parametros = NumeroTransaccionPago(NUMERO_TRANSACCION_PRUEBAS)
            @Test
            fun retorna_una_lista_vacia_de_dtos_cuando_el_repositorio_retorna_una_lista_vacia()
            {
                doReturn(sequenceOf<Persona>())
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val listaRetornada = recursoTodasEntidades.listar(NUMERO_TRANSACCION_PRUEBAS)

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

                val listaRetornada = recursoTodasEntidades.listar(NUMERO_TRANSACCION_PRUEBAS)

                Assertions.assertEquals(listaDTO, listaRetornada.toList())
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Persona"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorDesconocido> { recursoTodasEntidades.listar(NUMERO_TRANSACCION_PRUEBAS) }

                Assertions.assertEquals(PersonaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .listarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExiste> { recursoTodasEntidades.listar(NUMERO_TRANSACCION_PRUEBAS) }

                Assertions.assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                @Test
                fun numero_transaccion_invalido_cuando_se_pasa_numero_transaccion_nulo()
                {
                    val errorApi = assertThrows<EntidadInvalida> { recursoTodasEntidades.listar(null) }

                    Assertions.assertEquals(PagoDTO.CodigosError.NUMERO_TRANSACCION_POS_INVALIDO, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Personas de una Compra", PermisoBack.Accion.GET_TODOS)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    Assertions.assertEquals(1, numeroPermisos)
                }

                @Test
                fun se_ejecuta_la_operacion_de_bd_cuando_manejador_de_seguridad_no_lanza_excepcion()
                {
                    doReturn(sequenceOf<Persona>())
                        .`when`(mockRepositorio)
                        .listarSegunParametros(ID_CLIENTE, parametros)
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)
                    recursoTodasEntidades.listar(NUMERO_TRANSACCION_PRUEBAS)
                    verify(mockRepositorio).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoTodasEntidades.listar(NUMERO_TRANSACCION_PRUEBAS) }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoTodasEntidades.listar(NUMERO_TRANSACCION_PRUEBAS) }
                    verify(mockRepositorio, times(0)).listarSegunParametros(ID_CLIENTE, parametros)
                }
            }
        }
    }
}