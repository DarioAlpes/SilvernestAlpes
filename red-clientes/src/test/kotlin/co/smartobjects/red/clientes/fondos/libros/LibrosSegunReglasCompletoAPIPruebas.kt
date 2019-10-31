package co.smartobjects.red.clientes.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.LibroSegunReglasCompleto
import co.smartobjects.entidades.fondos.libros.PrecioEnLibro
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.fondos.libros.LibroSegunReglasCompletoDTO
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import kotlin.test.assertEquals


internal class LibrosSegunReglasCompletoAPIPruebas : PruebasUsandoServidorMock<LibrosSegunReglasCompletoAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }


    private val entidadDTO =
            LibroSegunReglasCompletoDTO(
                    LibroSegunReglasCompleto(
                            1, 1, "Libro de prueba",
                            LibroDePrecios(1, 1, "Libro de precios", setOf(PrecioEnLibro(Precio(Decimal.UNO, 1), 1))),
                            mutableSetOf(),
                            mutableSetOf(),
                            mutableSetOf()
                                            )
                                       )

    override fun ManejadorDePeticiones.extraerApi(): LibrosSegunReglasCompletoAPI = apiDeLibrosSegunReglasCompleto

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<LibroSegunReglasCompletoDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(mockConDefaultAnswer(LibroSegunReglasCompleto::class.java))
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<LibroSegunReglasCompletoDTO>>())

        doAnswer {
            it.getArgument<() -> Response<Unit>>(0).invoke()
            RespuestaIndividual.Exitosa(listOf(mockConDefaultAnswer(LibroSegunReglasCompleto::class.java)))
        }
            .`when`(mock)
            .haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<LibroSegunReglasCompletoDTO>>>())
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

            verify(mockParser).haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<LibroSegunReglasCompletoDTO>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            assertEquals("/clients/$ID_CLIENTE/rules-books-complete", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
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

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<LibroSegunReglasCompletoDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/rules-books-complete/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }
}
