package co.smartobjects.red.clientes.personas.creditos

import co.smartobjects.entidades.operativas.compras.CreditosDeUnaPersona
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import co.smartobjects.red.modelos.operativas.compras.CreditosDeUnaPersonaDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.threeten.bp.ZonedDateTime
import retrofit2.Response
import java.net.URLEncoder
import kotlin.test.assertEquals


internal class CreditosDeUnaPersonaAPIPruebas : PruebasUsandoServidorMock<CreditosDeUnaPersonaAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val entidadNegocio = CreditosDeUnaPersona(1, 1, listOf(), listOf())
    private val entidadDTO = CreditosDeUnaPersonaDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): CreditosDeUnaPersonaAPI = apiDeCreditosDeUnaPersona

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<CompraDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(entidadNegocio)
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<CompraDTO>>())
    }

    @Nested
    inner class ConsultarUno
    {
        private val parametrosConsulta =
                CreditosDeUnaPersonaAPI
                    .ParametrosBuscarRecursoCreditosDeUnaPersona(ID_ENTIDAD, ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar(parametrosConsulta)
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<CreditosDeUnaPersonaDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(parametrosConsulta)
            }

            assertEquals("/clients/$ID_CLIENTE/persons/$ID_ENTIDAD/credits?base-datetime=${URLEncoder.encode(parametrosConsulta.fecha.toString(), "UTF-8")}", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }
}
