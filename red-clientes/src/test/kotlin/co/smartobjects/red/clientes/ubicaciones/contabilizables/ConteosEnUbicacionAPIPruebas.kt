package co.smartobjects.red.clientes.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.ubicaciones.contabilizables.ConteoUbicacionDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.threeten.bp.ZonedDateTime
import retrofit2.Response
import kotlin.test.assertEquals


internal class ConteosEnUbicacionAPIPruebas : PruebasUsandoServidorMock<ConteosEnUbicacionAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val entidadNegocio = ConteoUbicacion(1, 1, 1, ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))
    private val entidadDTO = ConteoUbicacionDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): ConteosEnUbicacionAPI = apiDeConteosEnUbicacion

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<ConteoUbicacionDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(entidadNegocio)
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<ConteoUbicacionDTO>>())
    }

    @Nested
    inner class Crear
    {
        private val jsonRespuesta = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

        @Test
        fun invoca_el_metodo_de_parseo_de_respuesta_individual_dto()
        {
            llamarBackendCon(jsonRespuesta) {
                api.crear(ID_ENTIDAD, entidadNegocio)
            }

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<ConteoUbicacionDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_POST()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.crear(ID_ENTIDAD, entidadNegocio)
            }

            assertEquals("/clients/$ID_CLIENTE/locations/$ID_ENTIDAD/count", peticionRealizada.path)
            assertEquals("POST", peticionRealizada.method)
        }
    }
}
