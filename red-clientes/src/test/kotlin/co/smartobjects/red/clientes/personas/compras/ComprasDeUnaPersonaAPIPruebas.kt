package co.smartobjects.red.clientes.personas.compras

import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import co.smartobjects.utilidades.Decimal
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


internal class ComprasDeUnaPersonaAPIPruebas : PruebasUsandoServidorMock<ComprasDeUnaPersonaAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val entidadNegocio =
            Compra(
                    1,
                    "Prueba",
                    listOf(CreditoFondo(1, 1, Decimal.DIEZ, Decimal.DIEZ, Decimal.DIEZ, null, null, true, "Prueba", "Prueba", 1, 1, "código externo fondo", 1, "Prueba", null, null)),
                    listOf(),
                    listOf(Pago(Decimal.DIEZ, Pago.MetodoDePago.EFECTIVO, "Prueba")),
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                  )

    private val entidadDTO = CompraDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): ComprasDeUnaPersonaAPI = apiDeComprasDeUnaPersona

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<CompraDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(listOf(entidadNegocio))
        }
            .`when`(mock)
            .haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<CompraDTO>>>())
    }

    @Nested
    inner class Listar
    {
        private val parametrosConsulta =
                ComprasDeUnaPersonaAPI
                    .ParametrosListarRecursoComprasDeUnaPersona(ID_ENTIDAD, ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))
        private val jsonRespuesta = "[" + ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO) + "]"

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_de_una_coleccion_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar(parametrosConsulta)
            }

            verify(mockParser).haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<CompraDTO>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(parametrosConsulta)
            }

            assertEquals("/clients/$ID_CLIENTE/persons/$ID_ENTIDAD/available-purchases?base-datetime=${URLEncoder.encode(parametrosConsulta.fecha.toString(), "UTF-8")}", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }
}
