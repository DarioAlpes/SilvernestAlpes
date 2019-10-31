package co.smartobjects.prompterbackend.serviciosrest.personas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.integraciones.cafam.IntegracionCafam
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.personas.relacionesdepersonas.FiltroRelacionesPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.RepositorioRelacionesPersonas
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.personas.PersonaConFamiliaresDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import com.fasterxml.jackson.datatype.jaxrs.Jaxrs2TypesModule
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.WebTarget
import kotlin.test.assertEquals


@DisplayName("Recurso Personas por documento")
internal class RecursoPersonaPorDocumentoPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val NUMERO_DOCUMENTO_PRUEBAS = "123"
        const val TIPO_DOCUMENTO_PRUEBAS = "CC"
    }

    private fun darPersonaSegunIndice(indice: Int): Persona
    {
        return Persona(
                ID_CLIENTE,
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

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoTodasEntidades: RecursoPersonaPorDocumento

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            mockRecursoTodasEntidades = mockConDefaultAnswer(RecursoPersonaPorDocumento::class.java)

            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoTodasEntidades).`when`(mockRecursoCliente).darRecursoPersonaPorDocumento()

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

        @[Nested DisplayName("Al consultar por documento")]
        inner class ConsultarPorDocumento
        {
            @Test
            fun llama_la_funcion_listar_con_documento_valido()
            {
                doReturn(null).`when`(mockRecursoTodasEntidades).consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, TIPO_DOCUMENTO_PRUEBAS)
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonaPorDocumento.RUTA}")
                    .queryParam(RecursoPersonaPorDocumento.NOMBRE_PARAMETRO_NUMERO_DOCUMENTO, NUMERO_DOCUMENTO_PRUEBAS)
                    .queryParam(RecursoPersonaPorDocumento.NOMBRE_PARAMETRO_TIPO_DOCUMENTO, TIPO_DOCUMENTO_PRUEBAS)
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, TIPO_DOCUMENTO_PRUEBAS)
            }

            @Test
            fun llama_la_funcion_listar_con_numero_documento_null()
            {
                doReturn(null).`when`(mockRecursoTodasEntidades).consultarPorDocumento(null, TIPO_DOCUMENTO_PRUEBAS)
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonaPorDocumento.RUTA}")
                    .queryParam(RecursoPersonaPorDocumento.NOMBRE_PARAMETRO_NUMERO_DOCUMENTO, null)
                    .queryParam(RecursoPersonaPorDocumento.NOMBRE_PARAMETRO_TIPO_DOCUMENTO, TIPO_DOCUMENTO_PRUEBAS)
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).consultarPorDocumento(null, TIPO_DOCUMENTO_PRUEBAS)
            }

            @Test
            fun llama_la_funcion_listar_con_tipo_documento_null()
            {
                doReturn(null).`when`(mockRecursoTodasEntidades).consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, null)
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonasDeUnaCompra.RUTA}")
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonaPorDocumento.RUTA}")
                    .queryParam(RecursoPersonaPorDocumento.NOMBRE_PARAMETRO_NUMERO_DOCUMENTO, NUMERO_DOCUMENTO_PRUEBAS)
                    .queryParam(RecursoPersonaPorDocumento.NOMBRE_PARAMETRO_TIPO_DOCUMENTO, null)
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, null)
            }

            @Test
            fun llama_la_funcion_listar_con_documento_null()
            {
                doReturn(null).`when`(mockRecursoTodasEntidades).consultarPorDocumento(null, null)
                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoPersonaPorDocumento.RUTA}")
                    .queryParam(RecursoPersonaPorDocumento.NOMBRE_PARAMETRO_NUMERO_DOCUMENTO, null)
                    .queryParam(RecursoPersonaPorDocumento.NOMBRE_PARAMETRO_TIPO_DOCUMENTO, null)
                    .request()
                    .get(String::class.java)

                verify(mockRecursoTodasEntidades).consultarPorDocumento(null, null)
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorio = mockConDefaultAnswer(RepositorioRelacionesPersonas::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val mockIntegracionCafam = mockConDefaultAnswer(IntegracionCafam::class.java).also {
            doReturn(null).`when`(it).darInformacionAfiliado(cualquiera())
        }
        private val recursoTodasEntidades: RecursoPersonaPorDocumento by lazy {
            RecursoPersonaPorDocumento(ID_CLIENTE, mockRepositorio, mockManejadorSeguridad, mockIntegracionCafam)
        }

        @Nested
        @DisplayName("Al consultar por documento")
        inner class ConsultarPorDocumento
        {
            @Nested
            inner class CuandoLaIntegracionDeCafamRetornaUnaEntidad
            {
                @DisplayName("Retorna el dto correcto con familiares cuando la integración con CAFAM retorna algo")
                @ParameterizedTest(name = "Cuando el tipo de documento es ''{0}''")
                @EnumSource(Persona.TipoDocumento::class)
                fun retorna_el_dto_correcto_con_familiares_y_no_se_consulta_la_base_de_datos(tipoDocumento: Persona.TipoDocumento)
                {
                    val personaConFamiliaresDesdeIntegracion =
                            PersonaConFamiliares(
                                    darPersonaSegunIndice(0),
                                    setOf(darPersonaSegunIndice(1), darPersonaSegunIndice(2))
                                                )

                    doReturn(personaConFamiliaresDesdeIntegracion).`when`(mockIntegracionCafam).darInformacionAfiliado(cualquiera())

                    val parametros = FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(tipoDocumento, NUMERO_DOCUMENTO_PRUEBAS))

                    val entidadRetornada = recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, tipoDocumento.toString())

                    assertEquals(PersonaConFamiliaresDTO(personaConFamiliaresDesdeIntegracion), entidadRetornada)

                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }
            }

            @Nested
            inner class CuandoLaIntegracionDeCafamRetornaNull
            {
                @DisplayName("Retorna null cuando el gestor de entidades retorna null")
                @ParameterizedTest(name = "Cuando el tipo de documento es ''{0}''")
                @EnumSource(Persona.TipoDocumento::class)
                fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_persona_con_familiares_no_existe_cuando_el_repositorio_retorna_null(tipoDocumento: Persona.TipoDocumento)
                {
                    val parametros = FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(tipoDocumento, NUMERO_DOCUMENTO_PRUEBAS))
                    doReturn(null)
                        .`when`(mockRepositorio)
                        .buscarSegunParametros(ID_CLIENTE, parametros)

                    val errorApi = assertThrows<EntidadNoExiste> {
                        recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, tipoDocumento.toString())
                    }

                    assertEquals(PersonaConFamiliaresDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                    verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
                }

                @DisplayName("Retorna el dto correcto con familiares vacio cuando el gestor de entidades retorna una entidad")
                @ParameterizedTest(name = "Cuando el tipo de documento es ''{0}''")
                @EnumSource(Persona.TipoDocumento::class)
                fun retorna_el_dto_correcto_con_familiares_vacios_cuando_el_repositorio_retorna_una_entidad(tipoDocumento: Persona.TipoDocumento)
                {
                    val parametros = FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(tipoDocumento, NUMERO_DOCUMENTO_PRUEBAS))
                    val entidadNegocio =
                            PersonaConFamiliares(
                                    darPersonaSegunIndice(0),
                                    setOf(darPersonaSegunIndice(1), darPersonaSegunIndice(2))
                                                )

                    doReturn(entidadNegocio)
                        .`when`(mockRepositorio)
                        .buscarSegunParametros(ID_CLIENTE, parametros)

                    val entidadRetornada = recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, tipoDocumento.toString())

                    assertEquals(PersonaConFamiliaresDTO(entidadNegocio), entidadRetornada)
                    verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
                }
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_lanza_ErrorDeConsultaEntidad()
            {
                val parametros = FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(Persona.TipoDocumento.CC, NUMERO_DOCUMENTO_PRUEBAS))
                doThrow(ErrorDeConsultaEntidad("Persona"))
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<ErrorDesconocido> {
                    recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, TIPO_DOCUMENTO_PRUEBAS)
                }

                assertEquals(PersonaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorApi.codigoInterno)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_lanza_EsquemaNoExiste()
            {
                val parametros = FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(Persona.TipoDocumento.CC, NUMERO_DOCUMENTO_PRUEBAS))
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorio)
                    .buscarSegunParametros(ID_CLIENTE, parametros)

                val errorApi = assertThrows<EntidadNoExiste> {
                    recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, TIPO_DOCUMENTO_PRUEBAS)
                }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)
                verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
            }

            @Nested
            @DisplayName("Lanzan EntidadInvalida y código interno")
            inner class LanzanExcepcionEntidadInvalida
            {
                private val parametros = FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(Persona.TipoDocumento.CC, NUMERO_DOCUMENTO_PRUEBAS))

                @Test
                fun documento_invalido_cuando_se_pasa_numero_documento_nulo()
                {
                    val errorApi = assertThrows<EntidadInvalida> {
                        recursoTodasEntidades.consultarPorDocumento(null, TIPO_DOCUMENTO_PRUEBAS)
                    }

                    assertEquals(PersonaDTO.CodigosError.DOCUMENTO_INVALIDO, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun documento_invalido_cuando_se_pasa_tipo_documento_nulo()
                {
                    val errorApi = assertThrows<EntidadInvalida> {
                        recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, null)
                    }

                    assertEquals(PersonaDTO.CodigosError.DOCUMENTO_INVALIDO, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun documento_invalido_cuando_se_pasa_tipo_documento_invalido()
                {
                    val errorApi = assertThrows<EntidadInvalida> {
                        recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, "INEXISTENTE")
                    }

                    assertEquals(PersonaDTO.CodigosError.DOCUMENTO_INVALIDO, errorApi.codigoInterno)
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }
            }

            @Nested
            inner class Permisos
            {
                private val parametros = FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(Persona.TipoDocumento.CC, NUMERO_DOCUMENTO_PRUEBAS))
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "Persona por Documento", PermisoBack.Accion.GET_UNO)
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
                    val respuestaRepositorio =
                            PersonaConFamiliares(
                                    darPersonaSegunIndice(0),
                                    setOf(darPersonaSegunIndice(1), darPersonaSegunIndice(2))
                                                )

                    doReturn(respuestaRepositorio)
                        .`when`(mockRepositorio)
                        .buscarSegunParametros(ID_CLIENTE, parametros)

                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteConPermiso(permisoBuscadoNegocio)

                    recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, TIPO_DOCUMENTO_PRUEBAS)

                    verify(mockRepositorio).buscarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> {
                        recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, TIPO_DOCUMENTO_PRUEBAS)
                    }
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> {
                        recursoTodasEntidades.consultarPorDocumento(NUMERO_DOCUMENTO_PRUEBAS, TIPO_DOCUMENTO_PRUEBAS)
                    }
                    verify(mockRepositorio, times(0)).buscarSegunParametros(ID_CLIENTE, parametros)
                }
            }
        }
    }
}