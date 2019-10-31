package co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.personas.PersonaDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.threeten.bp.LocalDate
import retrofit2.Response
import kotlin.test.assertEquals


internal class PersonaPorIdSesionManillaAPIPruebas : PruebasUsandoServidorMock<PersonaPorIdSesionManillaAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val entidadNegocio =
            Persona(
                    1,
                    1,
                    "nombre",
                    Persona.TipoDocumento.CC,
                    "123",
                    Persona.Genero.MASCULINO,
                    LocalDate.now(ZONA_HORARIA_POR_DEFECTO),
                    Persona.Categoria.A,
                    Persona.Afiliacion.COTIZANTE,
                    false,
                    "asdfasdf","empresa","0",Persona.Tipo.NO_AFILIADO
                   )

    private val entidadDTO = PersonaDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): PersonaPorIdSesionManillaAPI = apiPersonaPorIdSesionManilla

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<PersonaDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(listOf(entidadNegocio))
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<PersonaDTO>>())
    }

    @Nested
    inner class ConsultarUno
    {
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

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

            assertEquals("/clients/$ID_CLIENTE/tag-sessions/$ID_ENTIDAD/person", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }
}
