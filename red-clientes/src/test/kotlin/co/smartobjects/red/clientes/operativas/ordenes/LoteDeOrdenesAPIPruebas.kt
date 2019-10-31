package co.smartobjects.red.clientes.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.clientes.retrofit.operativas.ordenes.LoteDeOrdenesRetrofitAPI
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.ordenes.LoteDeOrdenesDTO
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


internal class LoteDeOrdenesAPIPruebas : PruebasUsandoServidorMock<LoteDeOrdenesAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = "idquenoimporta"
    }

    private val entidadNegocio =
            LoteDeOrdenes(
                    1,
                    "Prueba",
                    listOf(Orden(
                            1,
                            1,
                            1,
                            listOf(Transaccion.Debito(1, 1, "Prueba", 1, 1, "código externo fondo", Decimal.DIEZ, 1, "Prueba")),
                            ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                                ))
                         )

    private val entidadDTO = LoteDeOrdenesDTO(entidadNegocio).ordenes

    override fun ManejadorDePeticiones.extraerApi(): LoteDeOrdenesAPI = apiDeLoteDeOrdenes

    private val mockApiLoteDeOrdenes = mockConDefaultAnswer(LoteDeOrdenesRetrofitAPI::class.java)

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<List<OrdenDTO>>>(0).invoke()
            RespuestaIndividual.Exitosa(entidadNegocio.ordenes)
        }
            .`when`(mock)
            .haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<OrdenDTO>>>())

        doAnswer {
            it.getArgument<() -> Response<LoteDeOrdenesDTO>>(0).invoke()
            RespuestaVacia.Exitosa
        }
            .`when`(mock)
            .haciaRespuestaVacia(cualquiera())
    }

    @Nested
    inner class Actualizar
    {
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.actualizar(ID_ENTIDAD, entidadNegocio)
            }

            verify(mockParser).haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<OrdenDTO>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_PUT()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.actualizar(ID_ENTIDAD, entidadNegocio)
            }

            assertEquals("PUT", peticionRealizada.method)
            assertEquals("/clients/$ID_CLIENTE/orders-batches/$ID_ENTIDAD", peticionRealizada.path)
        }
    }

    @Nested
    inner class ActualizarPorCampos
    {
        private val entidadPatch = TransaccionEntidadTerminadaDTO<LoteDeOrdenes>(true)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_vacia()
        {
            llamarBackendCon("{}") {
                api.actualizarCampos(ID_ENTIDAD, entidadPatch)
            }

            verify(mockParser).haciaRespuestaVacia(cualquiera())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_PATCH()
        {
            val peticionRealizada = llamarBackendCon("{}") {
                api.actualizarCampos(ID_ENTIDAD, entidadPatch)
            }

            assertEquals("PATCH", peticionRealizada.method)
            assertEquals("/clients/$ID_CLIENTE/orders-batches/$ID_ENTIDAD", peticionRealizada.path)
        }
    }
}
