package co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.operativas.reservas.RepositorioDeSesionDeManilla
import co.smartobjects.persistencia.personas.RepositorioPersonas
import co.smartobjects.prompterbackend.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.excepciones.ErrorDesconocido
import co.smartobjects.prompterbackend.excepciones.UsuarioNoAutenticado
import co.smartobjects.prompterbackend.excepciones.UsuarioNoTienePermiso
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoClientes
import co.smartobjects.prompterbackend.serviciosrest.personas.RecursoPersonasPruebas
import co.smartobjects.prompterbackend.serviciosrest.usuarios.RecursoPermisosPosibles
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.clientes.ClienteDTO
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
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
import javax.ws.rs.client.WebTarget
import kotlin.test.assertEquals

class RecursoPersonaPorIdSesionManillaPruebas
{
    companion object
    {
        const val ID_CLIENTE = 1L
        const val ID_ENTIDAD_PRUEBAS = 3L
        private val UUID_TAG_DE_PRUEBA = byteArrayOf(1, 2, 3)
    }

    private fun darSesionDeManillaSegunIndice(indice: Int): SesionDeManilla
    {
        return SesionDeManilla(
                ID_CLIENTE,
                indice.toLong(),
                indice.toLong(),
                UUID_TAG_DE_PRUEBA,
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                ZonedDateTime.of(LocalDate.of(Year.now(ZONA_HORARIA_POR_DEFECTO).value + 2 + indice, 1, 1), LocalTime.of(5, 5), ZONA_HORARIA_POR_DEFECTO),
                setOf<Long>(1, 2, 3)
                              )
    }

    private fun darPersonaSegunIndice(indice: Int): Persona
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

    private fun darPersonaDTOSegunIndice(indice: Int): PersonaDTO
    {
        return PersonaDTO(darPersonaSegunIndice(indice))
    }

    @[Nested DisplayName("Al llamar por red")]
    inner class Red
    {
        private lateinit var server: HttpServer
        private lateinit var target: WebTarget
        private lateinit var mockRecursoEntidadEspecifica: RecursoPersonaPorIdSesionManilla

        @[BeforeEach Throws(Exception::class)]
        fun antesDeCadaTest()
        {
            val mockRecursoCliente = mockConDefaultAnswer(RecursoClientes.RecursoCliente::class.java)
            val mockRecursoClientes = mockConDefaultAnswer(RecursoClientes::class.java)

            val mockRecursoSesionDeManilla = mockConDefaultAnswer(RecursoSesionesDeManilla.RecursoSesionDeManilla::class.java)
            val mockRecursoSesionesDeManilla = mockConDefaultAnswer(RecursoSesionesDeManilla::class.java)

            mockRecursoEntidadEspecifica = mockConDefaultAnswer(RecursoPersonaPorIdSesionManilla::class.java)

            doReturn(mockRecursoCliente).`when`(mockRecursoClientes).darRecursosEntidadEspecifica(ID_CLIENTE)
            doReturn(mockRecursoSesionesDeManilla).`when`(mockRecursoCliente).darRecursoSesionesDeManilla()
            doReturn(mockRecursoSesionDeManilla).`when`(mockRecursoSesionesDeManilla).darRecursosEntidadEspecifica(ID_ENTIDAD_PRUEBAS)
            doReturn(mockRecursoEntidadEspecifica).`when`(mockRecursoSesionDeManilla).darRecursoPersonaPorIdSesionManilla()

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
            fun llama_la_funcion_darPorId_con_dto_correcto()
            {
                val entidadPruebas = darPersonaSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                doReturn(PersonaDTO(entidadPruebas)).`when`(mockRecursoEntidadEspecifica).darPorId()

                target.path("${RecursoClientes.RUTA}/$ID_CLIENTE/${RecursoSesionesDeManilla.RUTA}/$ID_ENTIDAD_PRUEBAS/${RecursoPersonaPorIdSesionManilla.RUTA}").request()
                    .get(PersonaDTO::class.java)

                verify(mockRecursoEntidadEspecifica).darPorId()
            }
        }
    }

    @Nested
    @DisplayName("Al llamar por código")
    inner class Codigo
    {
        private val mockRepositorioSesionesDeManilla = mockConDefaultAnswer(RepositorioDeSesionDeManilla::class.java)
        private val mockRepositorioPersonas = mockConDefaultAnswer(RepositorioPersonas::class.java)
        private var mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteSiempreTienePermisos
        private val recursoEntidadEspecifica: RecursoPersonaPorIdSesionManilla by lazy {
            RecursoPersonaPorIdSesionManilla(
                    ID_CLIENTE,
                    ID_ENTIDAD_PRUEBAS,
                    mockRepositorioSesionesDeManilla,
                    mockRepositorioPersonas,
                    mockManejadorSeguridad
                                            )
        }

        @Nested
        @DisplayName("Al consultar uno")
        inner class ConsultarUno
        {
            @Test
            fun si_la_sesion_de_manilla_existe_retorna_dto_correcto()
            {
                val sesionDeManilla = darSesionDeManillaSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                val persona = darPersonaSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())
                val personaDTO = darPersonaDTOSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())

                doReturn(sesionDeManilla)
                    .`when`(mockRepositorioSesionesDeManilla)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                doReturn(persona)
                    .`when`(mockRepositorioPersonas)
                    .buscarPorId(ID_CLIENTE, sesionDeManilla.idPersona)

                val entidadRetornada = recursoEntidadEspecifica.darPorId()

                assertEquals(personaDTO, entidadRetornada)

                verify(mockRepositorioSesionesDeManilla).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                verify(mockRepositorioPersonas).buscarPorId(ID_CLIENTE, sesionDeManilla.idPersona)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_correcto_cuando_el_repositorio_de_sesiones_de_manilla_retorna_null()
            {
                doReturn(null)
                    .`when`(mockRepositorioSesionesDeManilla)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                assertEquals(SesionDeManillaDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)

                verify(mockRepositorioSesionesDeManilla).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                verify(mockRepositorioPersonas, never()).buscarPorId(anyLong(), anyLong())
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_correcto_cuando_el_repositorio_de_sesiones_de_manilla_lanza_ErrorDeConsultaEntidad()
            {
                doThrow(ErrorDeConsultaEntidad("Sesión de manilla"))
                    .`when`(mockRepositorioSesionesDeManilla)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorBDParaSesionDeManilla = assertThrows<ErrorDesconocido> { recursoEntidadEspecifica.darPorId() }

                assertEquals(SesionDeManillaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorBDParaSesionDeManilla.codigoInterno)

                verify(mockRepositorioSesionesDeManilla).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Test
            fun lanza_excepcion_ErrorDesconocido_con_codigo_interno_de_sesion_de_manilla_cuando_el_repositorio_de_personas_lanza_ErrorDeConsultaEntidad()
            {
                val sesionDeManilla = darSesionDeManillaSegunIndice(ID_ENTIDAD_PRUEBAS.toInt())

                doReturn(sesionDeManilla)
                    .`when`(mockRepositorioSesionesDeManilla)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                doThrow(ErrorDeConsultaEntidad("Persona"))
                    .`when`(mockRepositorioPersonas)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorBDParaSesionDeManilla = assertThrows<ErrorDesconocido> { recursoEntidadEspecifica.darPorId() }

                assertEquals(SesionDeManillaDTO.CodigosError.ERROR_DE_BD_DESCONOCIDO, errorBDParaSesionDeManilla.codigoInterno)

                verify(mockRepositorioSesionesDeManilla).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                verify(mockRepositorioPersonas).buscarPorId(ID_CLIENTE, sesionDeManilla.idPersona)
            }

            @Test
            fun lanza_excepcion_EntidadNoExiste_con_codigo_interno_cliente_no_existe_cuando_el_repositorio_de_sesiones_de_manilla_lanza_EsquemaNoExiste()
            {
                doThrow(EsquemaNoExiste("Algun esquema"))
                    .`when`(mockRepositorioSesionesDeManilla)
                    .buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)

                val errorApi = assertThrows<EntidadNoExiste> { recursoEntidadEspecifica.darPorId() }

                assertEquals(ClienteDTO.CodigosError.NO_EXISTE, errorApi.codigoInterno)

                verify(mockRepositorioSesionesDeManilla).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
            }

            @Nested
            inner class Permisos
            {
                // Se usa el nombre del permiso directamente en lugar de usar la constante para garantizar que falle si es cambiado (Este valor se escribe en BD)
                private val permisoBuscadoNegocio = PermisoBack(ID_CLIENTE, "PersonaDeUnaSesionDeManilla", PermisoBack.Accion.GET_UNO)
                private val permisoBuscadoDTO = PermisoBackDTO(permisoBuscadoNegocio)

                @Test
                fun el_servicio_de_permisos_posibles_retorna_el_permiso_correspondiente_una_vez()
                {
                    val permisos: Sequence<PermisoBackDTO> = RecursoPermisosPosibles(ID_CLIENTE, mockManejadorSeguridad).darTodas()
                    val numeroPermisos = permisos.count { it == permisoBuscadoDTO }
                    assertEquals(1, numeroPermisos)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoAutenticado()
                {
                    mockManejadorSeguridad = mockManejadorSeguridadUsuarioDeClienteNuncaEstaAutenticado
                    assertThrows<UsuarioNoAutenticado> { recursoEntidadEspecifica.darPorId() }
                    verify(mockRepositorioSesionesDeManilla, never()).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                    verify(mockRepositorioPersonas, never()).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                }

                @Test
                fun no_intenta_ejecutar_la_operacion_de_bd_cuando_manejador_de_seguridad_lanza_excepcion_UsuarioNoTienePermiso()
                {
                    mockManejadorSeguridad = darMockManejadorSeguridadUsuarioDeClienteSinPermiso(permisoBuscadoNegocio)
                    assertThrows<UsuarioNoTienePermiso> { recursoEntidadEspecifica.darPorId() }
                    verify(mockRepositorioSesionesDeManilla, never()).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                    verify(mockRepositorioPersonas, never()).buscarPorId(ID_CLIENTE, ID_ENTIDAD_PRUEBAS)
                }
            }
        }
    }
}