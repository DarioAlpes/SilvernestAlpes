package co.smartobjects.red.clientes.usuarios

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import kotlin.test.assertEquals


internal class PermisosPosiblesAPIPruebas : PruebasUsandoServidorMock<PermisosPosiblesAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = "Prueba"
    }

    private val entidadNegocio = PermisoBack(ID_CLIENTE, ID_ENTIDAD, PermisoBack.Accion.GET_TODOS)
    private val entidadDTO = PermisoBackDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): PermisosPosiblesAPI = apiDePermisosPosibles

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<PermisoBackDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(listOf(entidadNegocio))
        }
            .`when`(mock)
            .haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<PermisoBackDTO>>>())
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

            verify(mockParser).haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<PermisoBackDTO>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            assertEquals("/clients/$ID_CLIENTE/possible-permissions", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }
}
