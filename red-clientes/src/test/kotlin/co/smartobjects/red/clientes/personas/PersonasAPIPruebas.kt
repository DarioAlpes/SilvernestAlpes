package co.smartobjects.red.clientes.personas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.personas.PersonaConFamiliaresDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import kotlin.test.assertEquals


internal class PersonasAPIPruebas : PruebasUsandoServidorMock<PersonasAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val personaNegocio = Persona(1, ID_ENTIDAD)
    private val personaDTO = PersonaDTO(personaNegocio)

    override fun ManejadorDePeticiones.extraerApi(): PersonasAPI = apiDePersonas

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java)

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ApiEstandar
    {
        @BeforeAll
        fun resetearMock()
        {
            Mockito.reset(mockParser)

            doAnswer {
                it.getArgument<() -> Response<PersonaDTO>>(0).invoke()
                RespuestaIndividual.Exitosa(personaNegocio)
            }
                .`when`(mockParser)
                .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<PersonaDTO>>())

            doAnswer {
                it.getArgument<() -> Response<Unit>>(0).invoke()
                RespuestaVacia.Exitosa
            }
                .`when`(mockParser)
                .haciaRespuestaVacia(cualquiera())

            doAnswer {
                it.getArgument<() -> Response<PersonaDTO>>(0).invoke()
                RespuestaIndividual.Exitosa(listOf(personaNegocio))
            }
                .`when`(mockParser)
                .haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<PersonaDTO>>>())
        }

        @Nested
        inner class Crear
        {
            private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(personaDTO)

            @Test
            fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
            {
                llamarBackendCon(jsonRespuesta) {
                    api.crear(personaNegocio)
                }

                verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<PersonaDTO>>())
                verifyNoMoreInteractions(mockParser)
            }

            @Test
            fun invoca_url_correcta_usa_POST()
            {
                val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                    api.crear(personaNegocio)
                }

                assertEquals("/clients/$ID_CLIENTE/persons", peticionRealizada.path)
                assertEquals("POST", peticionRealizada.method)
            }
        }

        @Nested
        inner class Listar
        {
            private val jsonRespuesta = "[" + ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(personaDTO) + "]"

            @Test
            fun invoca_el_metodo_de_parseo_de_respuesta_individual_de_una_coleccion_dto()
            {
                llamarBackendCon(jsonRespuesta) {
                    api.consultar()
                }

                verify(mockParser).haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<PersonaDTO>>>())
                verifyNoMoreInteractions(mockParser)
            }

            @Test
            fun invoca_url_correcta_usa_GET()
            {
                val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                    api.consultar()
                }

                assertEquals("/clients/$ID_CLIENTE/persons", peticionRealizada.path)
                assertEquals("GET", peticionRealizada.method)
            }
        }

        @Nested
        inner class Actualizar
        {
            private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(personaDTO)

            @Test
            fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
            {
                llamarBackendCon(jsonRespuesta) {
                    api.actualizar(personaNegocio)
                }

                verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<PersonaDTO>>())
                verifyNoMoreInteractions(mockParser)
            }

            @Test
            fun invoca_url_correcta_usa_PUT()
            {
                val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                    api.actualizar(personaNegocio)
                }

                assertEquals("/clients/$ID_CLIENTE/persons/${personaNegocio.id!!}", peticionRealizada.path)
                assertEquals("PUT", peticionRealizada.method)
            }
        }

        @Nested
        inner class ConsultarUno
        {
            private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(personaDTO)

            @Test
            fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
            {
                llamarBackendCon(jsonRespuesta) {
                    api.consultar(ID_ENTIDAD)
                }

                verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<PersonaDTO>>())
                verifyNoMoreInteractions(mockParser)
            }

            @Test
            fun invoca_url_correcta_usa_GET()
            {
                val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                    api.consultar(ID_ENTIDAD)
                }

                assertEquals("/clients/$ID_CLIENTE/persons/$ID_ENTIDAD", peticionRealizada.path)
                assertEquals("GET", peticionRealizada.method)
            }
        }

        @Nested
        inner class Eliminar
        {
            @Test
            fun invoca_el_metodo_de_parseo_de_respuesta_vacia()
            {
                llamarBackendCon("{}") {
                    api.eliminar(ID_ENTIDAD)
                }

                verify(mockParser).haciaRespuestaVacia(cualquiera())
                verifyNoMoreInteractions(mockParser)
            }

            @Test
            fun invoca_url_correcta_usa_DELETE()
            {
                val peticionRealizada = llamarBackendCon("{}") {
                    api.eliminar(ID_ENTIDAD)
                }

                assertEquals("/clients/$ID_CLIENTE/persons/$ID_ENTIDAD", peticionRealizada.path)
                assertEquals("DELETE", peticionRealizada.method)
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ApiAdicional
    {
        private val personaFamiliaresNegocio = PersonaConFamiliares(Persona(1, 1), setOf())
        private val personaFamiliaresDTO = PersonaConFamiliaresDTO(personaFamiliaresNegocio)

        @BeforeAll
        fun resetearMock()
        {
            Mockito.reset(mockParser)

            doAnswer {
                it.getArgument<() -> Response<PersonaDTO>>(0).invoke()
                RespuestaIndividual.Exitosa(personaFamiliaresNegocio)
            }
                .`when`(mockParser)
                .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<PersonaConFamiliaresDTO>>())
        }

        @Nested
        inner class ConsultarDocumento
        {

            private val parametrosConsulta = DocumentoCompleto(Persona.TipoDocumento.CC, "abc123")
            private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(personaFamiliaresDTO)

            @Test
            fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
            {
                llamarBackendCon(jsonRespuesta) {
                    api.consultarPorDocumento(parametrosConsulta)
                }

                verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<PersonaDTO>>())
                verifyNoMoreInteractions(mockParser)
            }

            @Test
            fun invoca_url_correcta_usa_GET()
            {
                val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                    api.consultarPorDocumento(parametrosConsulta)
                }

                assertEquals("/clients/$ID_CLIENTE/person-by-document?document-number=${parametrosConsulta.numeroDocumento}&document-type=${parametrosConsulta.tipoDocumento.name}", peticionRealizada.path)
                assertEquals("GET", peticionRealizada.method)
            }
        }
    }
}
