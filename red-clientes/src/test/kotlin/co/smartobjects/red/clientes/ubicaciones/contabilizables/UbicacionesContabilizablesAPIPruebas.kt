package co.smartobjects.red.clientes.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.ubicaciones.contabilizables.UbicacionesContabilizablesDTO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import kotlin.test.assertEquals


internal class UbicacionesContabilizablesAPIPruebas : PruebasUsandoServidorMock<UbicacionesContabilizablesAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val entidadNegocio = UbicacionesContabilizables(ID_CLIENTE, setOf(1L, 2L))
    private val entidadDTO = UbicacionesContabilizablesDTO(entidadNegocio)
    private val entidadNegocioRetornada = listOf(1L, 2L)

    override fun ManejadorDePeticiones.extraerApi(): UbicacionesContabilizablesAPI = apiDeUbicacionesContabilizables

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<List<Long>>>(0).invoke()
            RespuestaIndividual.Exitosa(entidadNegocioRetornada)
        }
            .`when`(mock)
            .haciaRespuestaIndividualColeccionSimple(cualquiera<() -> Response<List<Long>>>())
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

            verify(mockParser).haciaRespuestaIndividualColeccionSimple(cualquiera<() -> Response<List<Long>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_POST()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.crear(entidadNegocio)
            }

            assertEquals("/clients/$ID_CLIENTE/countable-locations", peticionRealizada.path)
            assertEquals("PUT", peticionRealizada.method)
        }
    }

    @Nested
    inner class Consultar
    {
        private val jsonRespuesta = "[" + ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO) + "]"

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_de_una_coleccion_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            verify(mockParser).haciaRespuestaIndividualColeccionSimple(cualquiera<() -> Response<List<Long>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            assertEquals("/clients/$ID_CLIENTE/countable-locations", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }
}
