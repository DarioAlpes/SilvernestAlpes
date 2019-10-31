package co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla

import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.threeten.bp.ZonedDateTime
import retrofit2.Response
import kotlin.test.assertEquals


internal class OrdenesDeUnaSesionDeManillaAPIPruebas : PruebasUsandoServidorMock<OrdenesDeUnaSesionDeManillaAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val entidadNegocio =
            Orden(
                    1,
                    1,
                    1,
                    listOf(Transaccion.Debito(1, 1, "Prueba", 1, 1, "código externo fondo", Decimal.DIEZ, 1, "Prueba")),
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                 )
    private val entidadDTO = listOf(OrdenDTO(entidadNegocio))

    override fun ManejadorDePeticiones.extraerApi(): OrdenesDeUnaSesionDeManillaAPI = apiDeOrdenesDeUnaSesionDeManilla

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<OrdenDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(listOf(entidadNegocio))
        }
            .`when`(mock)
            .haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<OrdenDTO>>>())
    }

    @Nested
    inner class Consultar
    {
        private val jsonRespuesta = "[" + ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO) + "]"

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_de_una_coleccion_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            verify(mockParser).haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<OrdenDTO>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/tag-sessions/$ID_ENTIDAD/orders", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }
}
