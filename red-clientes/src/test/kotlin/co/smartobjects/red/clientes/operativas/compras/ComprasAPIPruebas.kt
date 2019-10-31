package co.smartobjects.red.clientes.operativas.compras

import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.PruebasUsandoServidorMock
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.cualquiera
import co.smartobjects.red.clientes.mockConDefaultAnswer
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
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


internal class ComprasAPIPruebas : PruebasUsandoServidorMock<ComprasAPI>()
{
    companion object
    {
        private const val ID_ENTIDAD = "idquenoimporta"
    }

    private val entidadNegocio =
            Compra(
                    1,
                    "Prueba",
                    listOf(CreditoFondo(1, 1, Decimal.DIEZ, Decimal.DIEZ, Decimal.DIEZ, null, null, true, "Prueba", "Prueba", 1, 1, "código externo fondo", 1, "Prueba", null, null)),
                    listOf(),
                    listOf(Pago(Decimal.DIEZ, Pago.MetodoDePago.EFECTIVO, "Prueba")),
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                  )
    private val entidadDTO = CompraDTO(entidadNegocio)

    override fun ManejadorDePeticiones.extraerApi(): ComprasAPI = apiDeCompras

    override val mockParser: ParserRespuestasRetrofit = mockConDefaultAnswer(ParserRespuestasRetrofit::class.java).also { mock ->
        doAnswer {
            it.getArgument<() -> Response<CompraDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(entidadNegocio)
        }
            .`when`(mock)
            .haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<CompraDTO>>())

        doAnswer {
            it.getArgument<() -> Response<Unit>>(0).invoke()
            RespuestaVacia.Exitosa
        }
            .`when`(mock)
            .haciaRespuestaVacia(cualquiera())

        doAnswer {
            it.getArgument<() -> Response<CompraDTO>>(0).invoke()
            RespuestaIndividual.Exitosa(listOf(entidadNegocio))
        }
            .`when`(mock)
            .haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<CompraDTO>>>())
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

            verify(mockParser).haciaRespuestaIndividualColeccionDesdeDTO(cualquiera<() -> Response<List<CompraDTO>>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar()
            }

            assertEquals("/clients/$ID_CLIENTE/purchases", peticionRealizada.path)
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

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<CompraDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_PUT()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.actualizar(ID_ENTIDAD, entidadNegocio)
            }

            assertEquals("/clients/$ID_CLIENTE/purchases/$ID_ENTIDAD", peticionRealizada.path)
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

            verify(mockParser).haciaRespuestaIndividualDesdeDTO(cualquiera<() -> Response<CompraDTO>>())
            verifyNoMoreInteractions(mockParser)
        }

        @Test
        fun invoca_url_correcta_usa_GET()
        {
            val peticionRealizada = llamarBackendCon(jsonRespuesta) {
                api.consultar(ID_ENTIDAD)
            }

            assertEquals("/clients/$ID_CLIENTE/purchases/$ID_ENTIDAD", peticionRealizada.path)
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

            assertEquals("/clients/$ID_CLIENTE/purchases/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("DELETE", peticionRealizada.method)
        }
    }

    @Nested
    inner class ActualizarPorCampos
    {
        private val entidadPatch: TransaccionEntidadTerminadaDTO<Compra> = TransaccionEntidadTerminadaDTO(false)

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

            assertEquals("/clients/$ID_CLIENTE/purchases/$ID_ENTIDAD", peticionRealizada.path)
            assertEquals("PATCH", peticionRealizada.method)
        }
    }
}
