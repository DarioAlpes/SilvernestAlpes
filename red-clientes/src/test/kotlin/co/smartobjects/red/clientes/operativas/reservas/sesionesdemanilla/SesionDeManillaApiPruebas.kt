package co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import kotlin.test.assertEquals


internal class SesionDeManillaApiPruebas : PruebasUsandoServidorMock<SesionDeManillaAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val entidadNegocio = SesionDeManilla(1, 1, 1, null, null, null, setOf(1L))
    private val entidadDTO = SesionDeManillaDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): SesionDeManillaAPI = apiDeSesionDeManilla

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<SesionDeManillaDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(entidadNegocio)
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<SesionDeManillaDTO>>())

        doAnswer {
            it.getArgument<() -> Response<Unit>>(0).invoke()
            RespuestaVacia.Exitosa
        }
            .`when`(mock)
            .haciaRespuestaVacia(cualquiera())
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

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<SesionDeManillaDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/tag-sessions/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }

    @Nested
    inner class ActualizarPorCampos
    {
        private val entidadPatch = SesionDeManillaAPI.ParametrosActualizacionParcial.Activacion(ByteArray(1))

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

            assertEquals("/clients/$ID_CLIENTE/tag-sessions/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("PATCH", peticionRealizada.method)
        }
    }
}
