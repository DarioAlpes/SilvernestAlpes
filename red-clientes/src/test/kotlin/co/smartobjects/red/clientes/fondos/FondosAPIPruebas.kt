package co.smartobjects.red.clientes.fondos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.fondos.FondoDTO
import co.smartobjects.red.modelos.fondos.ListaFondosDTO
import co.smartobjects.red.modelos.fondos.WrapperDeserilizacionFondoDTO
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import kotlin.test.assertEquals


internal class FondosAPIPruebas : PruebasUsandoServidorMock<FondosAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val fondoNegocio = Dinero(1, 1, "fondo prueba", false, false, false, Precio(Decimal.UNO, 1), "asdfasd")
    private val entidadDTO = FondoDTO<Dinero>(fondoNegocio)

    override fun ManejadorDePeticiones.extraerApi(): FondosAPI = apiDeFondos

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java)

    @BeforeEach
    fun resetearMock()
    {
        Mockito.reset(mockParser)
    }

    @Nested
    inner class Consultar
    {
        private val listaFondosNegocio = listOf<Fondo<*>>(fondoNegocio)
        private val listaFondosDTO = ListaFondosDTO(listOf(entidadDTO))
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(listaFondosDTO)

        @BeforeEach
        fun prepararMock()
        {
            doAnswer {
                it.getArgument<() -> Response<ListaFondosDTO>>(0).invoke()
                RespuestaIndividual.Exitosa(listaFondosNegocio)
            }
                .`when`(mockParser)
                .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<ListaFondosDTO>>())
        }

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<ListaFondosDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            assertEquals("/clients/$ID_CLIENTE/funds", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }

    @Nested
    inner class ConsultarUno
    {
        private val wrapperDeserilizacionFondoDTO = WrapperDeserilizacionFondoDTO(entidadDTO)
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(wrapperDeserilizacionFondoDTO)

        @BeforeEach
        fun prepararMock()
        {
            doAnswer {
                it.getArgument<() -> Response<WrapperDeserilizacionFondoDTO>>(0).invoke()
                RespuestaIndividual.Exitosa(fondoNegocio)
            }
                .`when`(mockParser)
                .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<WrapperDeserilizacionFondoDTO>>())
        }

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<WrapperDeserilizacionFondoDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/funds/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }

    @Nested
    inner class Eliminar
    {
        @BeforeEach
        fun prepararMock()
        {
            doAnswer {
                it.getArgument<() -> Response<Unit>>(0).invoke()
                RespuestaVacia.Exitosa
            }
                .`when`(mockParser)
                .haciaRespuestaVacia(cualquiera())
        }

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

            assertEquals("/clients/$ID_CLIENTE/funds/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("DELETE", peticionRealizada.method)
        }
    }

    /*@Test
    fun test_actualizarPorCamposFondo_invoca_url_correcta_usa_PATCH()
    {
        val id_fondo = 1L
        val json = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(fondoDisponibleParaLaVentaDTO)
        mockServidor.enqueue(MockResponse().setBody(json).setResponseCode(200).addHeader("Content-Type", "application/json"))
        api.actualizarPorCamposIndividuales(ID_CLIENTE, id_fondo, fondoDisponibleParaLaVentaDTO).execute()
        val request = mockServidor.takeRequest()
        assertEquals("/clients/$ID_CLIENTE/funds/$id_fondo", peticionRealizada.path)
        assertEquals("PATCH", peticionRealizada.method)
    }*/
}
