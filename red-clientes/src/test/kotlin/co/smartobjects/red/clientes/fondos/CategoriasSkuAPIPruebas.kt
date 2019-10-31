package co.smartobjects.red.clientes.fondos

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.fondos.CategoriaSkuDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import retrofit2.Response
import kotlin.test.assertEquals


internal class CategoriasSkuAPIPruebas : PruebasUsandoServidorMock<CategoriasSkuAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = 2L
    }

    private val entidadNegocio = CategoriaSku(1, 1, "Prueba", true, true, true, Precio(Decimal.DIEZ, 1), "código externo", null, linkedSetOf(), null)
    private val entidadDTO = CategoriaSkuDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): CategoriasSkuAPI = apiDeCategoriasSku

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<CategoriaSkuDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(entidadNegocio)
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<CategoriaSkuDTO>>())

        doAnswer {
            it.getArgument<() -> Response<Unit>>(0).invoke()
            RespuestaVacia.Exitosa
        }
            .`when`(mock)
            .haciaRespuestaVacia(cualquiera())

        doAnswer {
            it.getArgument<() -> Response<CategoriaSkuDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(listOf(entidadNegocio))
        }
            .`when`(mock)
            .haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<CategoriaSkuDTO>>>())
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

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<CategoriaSkuDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_POST()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.crear(entidadNegocio)
            }

            assertEquals("/clients/$ID_CLIENTE/sku-categories", peticionRealizada.path)
            assertEquals("POST", peticionRealizada.method)
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

            verify(mockParser).haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<CategoriaSkuDTO>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            assertEquals("/clients/$ID_CLIENTE/sku-categories", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
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

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<CategoriaSkuDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_PUT()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.actualizar(ID_ENTIDAD, entidadNegocio)
            }

            assertEquals("/clients/$ID_CLIENTE/sku-categories/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("PUT", peticionRealizada.method)
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

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<CategoriaSkuDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/sku-categories/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("GET", peticionRealizada.method)
        }
    }

    @Nested
    inner class Eliminar
    {
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

            assertEquals("/clients/$ID_CLIENTE/sku-categories/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("DELETE", peticionRealizada.method)
        }
    }

    @Nested
    inner class ActualizarPorCampos
    {
        private val entidadPatch = FondoDisponibleParaLaVentaDTO<CategoriaSku>(false)

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

            assertEquals("/clients/$ID_CLIENTE/sku-categories/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("PATCH", peticionRealizada.method)
        }
    }
}
