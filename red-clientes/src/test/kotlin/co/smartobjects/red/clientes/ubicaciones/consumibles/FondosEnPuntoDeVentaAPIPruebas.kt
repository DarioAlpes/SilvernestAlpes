package co.smartobjects.red.clientes.ubicaciones.consumibles

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.fondos.FondoDTO
import co.smartobjects.red.modelos.fondos.ListaFondosDTO
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import kotlin.test.assertEquals


internal class FondosEnPuntoDeVentaAPIPruebas : PruebasUsandoServidorMock<FondosEnPuntoDeVentaAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val fondoNegocio = Dinero(1, 1, "fondo prueba", false, false, false, Precio(Decimal.UNO, 1), "asdfasd")
    private val fondoDTO = FondoDTO<Dinero>(fondoNegocio)
    private val listaFondosNegocio = listOf<Fondo<*>>(fondoNegocio)
    private val listaFondosDTO = ListaFondosDTO(listOf(fondoDTO))

    override fun ManejadorDePeticiones.extraerApi(): FondosEnPuntoDeVentaAPI = apiDeFondosEnPuntoDeVenta

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<ListaFondosDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(listaFondosNegocio)
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<ListaFondosDTO>>())
    }

    @Nested
    inner class Consultar
    {
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(listaFondosDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_de_una_coleccion_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<ListaFondosDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/locations/$ID_ENTIDAD/funds", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }
}
