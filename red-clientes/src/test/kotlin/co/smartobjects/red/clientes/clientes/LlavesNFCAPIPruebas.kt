package co.smartobjects.red.clientes.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.clientes.LlaveNFCDTO
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


internal class LlavesNFCAPIPruebas : PruebasUsandoServidorMock<LlavesNFCAPI>()
{
    private val entidadNegocio = Cliente.LlaveNFC(1, "Prueba")
    private val entidadDTO = LlaveNFCDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): LlavesNFCAPI = apiDeLlavesNFC

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<LlaveNFCDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(entidadNegocio)
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<LlaveNFCDTO>>())

        doAnswer {
            it.getArgument<() -> Response<Unit>>(0).invoke()
            RespuestaVacia.Exitosa
        }
            .`when`(mock)
            .haciaRespuestaVacia(cualquiera())
    }

    @Nested
    inner class Crear
    {
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.crear(entidadNegocio)
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<LlaveNFCDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_POST()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.crear(entidadNegocio)
            }

            assertEquals("/clients/$ID_CLIENTE/nfc-keys", peticionRealizada.path)
            assertEquals("POST", peticionRealizada.method)
        }
    }

    @Nested
    inner class Consultar
    {
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<LlaveNFCDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val fecha = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)

            val request = llamarBackendCon(jsonRespuesta) {
                api.consultar(fecha)
            }

            assertEquals("/clients/$ID_CLIENTE/nfc-keys?base-datetime=" + URLEncoder.encode(fecha.toString(), "UTF-8"), request.path)
            assertEquals("GET", request.method)
        }
    }


    @Nested
    inner class EliminarHastaFechaCorte
    {
        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_vacia()
        {
            llamarBackendCon("{}") {
                api.eliminarHastaFechaCorte(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))
            }

            verify(mockParser).haciaRespuestaVacia(cualquiera())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_DELETE()
        {
            val fecha = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)

            val request = llamarBackendCon("{}") {
                api.eliminarHastaFechaCorte(fecha)
            }

            assertEquals("/clients/$ID_CLIENTE/nfc-keys?base-datetime=" + URLEncoder.encode(fecha.toString(), "UTF-8"), request.path)
            assertEquals("DELETE", request.method)
        }
    }
}
