package co.smartobjects.red.clientes

import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.ErrorDePeticion
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.test.assertEquals


internal class ParserRespuestasRetrofitPruebas
{
    @Nested
    inner class HaciaRespuestaVacia
    {
        @Test
        fun al_recibir_una_respuesta_correcta_retorna_respuesta_exitosa()
        {
            val respuestaConError = Response.success(Unit)

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaVacia { respuestaConError }

            assertEquals(RespuestaVacia.Exitosa, parseado)
        }

        @Test
        fun al_recibir_una_respuesta_con_error_body_la_transforma_a_error_de_entidad()
        {
            val codigoDeError = 1
            val mensajeDeError = "El mensaje de error"

            val respuestaConError =
                    Response
                        .error<Unit>(
                                500,
                                ResponseBody.create(
                                        MediaType.parse("application/json"),
                                        """{"internal-code": $codigoDeError,"message": "$mensajeDeError"}"""
                                                   )
                                    )

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaVacia { respuestaConError }

            assertEquals(RespuestaVacia.Error.Back(500, ErrorDePeticion(codigoDeError, mensajeDeError)), parseado)
        }

        @Test
        fun al_lanzar_SocketTimeoutException_retorna_error_de_timeout()
        {
            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaVacia { throw SocketTimeoutException() }

            assertEquals(RespuestaVacia.Error.Timeout, parseado)
        }

        @Test
        fun al_lanzar_IOException_retorna_error_de_red()
        {
            val errorEsperado = IOException("el mensaje")

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaVacia { throw errorEsperado }

            assertEquals(RespuestaVacia.Error.Red(errorEsperado), parseado)
        }
    }

    data class Dummy(val a: String)
    private data class DummyDTO(val a: Int) : EntidadDTO<Dummy>
    {
        override fun aEntidadDeNegocio(): Dummy = Dummy(a.toString())
    }

    @Nested
    inner class HaciaRespuestaIndividualSimple
    {
        private val entidadDTO = DummyDTO(0)

        @Test
        fun al_recibir_una_respuesta_correcta_con_cuerpo_de_mensaje_retorna_entidad_de_negocio_correspondiente()
        {
            val respuesta = Response.success(entidadDTO)

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualSimple { respuesta }

            assertEquals(RespuestaIndividual.Exitosa(entidadDTO), parseado)
        }

        @Test
        fun al_recibir_una_respuesta_correcta_sin_cuerpo_de_mensaje_retorna_respuesta_vacia()
        {
            val respuesta = Response.success<DummyDTO>(null)

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualSimple { respuesta }

            assertEquals(RespuestaIndividual.Vacia(), parseado)
        }

        @Test
        fun al_recibir_una_respuesta_con_error_body_la_transforma_a_error_de_entidad()
        {
            val codigoDeError = 1
            val mensajeDeError = "El mensaje de error"

            val respuestaConError =
                    Response
                        .error<DummyDTO>(
                                500,
                                ResponseBody.create(
                                        MediaType.parse("application/json"),
                                        """{"internal-code": $codigoDeError,"message": "$mensajeDeError"}"""
                                                   )
                                        )

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualSimple { respuestaConError }

            assertEquals(RespuestaIndividual.Error.Back(500, ErrorDePeticion(codigoDeError, mensajeDeError)), parseado)
        }

        @Test
        fun al_lanzar_SocketTimeoutException_retorna_error_de_timeout()
        {
            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualSimple<DummyDTO> { throw SocketTimeoutException() }

            assertEquals(RespuestaIndividual.Error.Timeout(), parseado)
        }

        @Test
        fun al_lanzar_IOException_retorna_error_de_red()
        {
            val errorEsperado = IOException("el mensaje")

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualSimple<DummyDTO> { throw errorEsperado }

            assertEquals(RespuestaIndividual.Error.Red(errorEsperado), parseado)
        }
    }

    @Nested
    inner class HaciaRespuestaIndividualDesdeDTO
    {
        private val entidadNegocio = Dummy("0")
        private val entidadDTO = DummyDTO(0)

        @Test
        fun al_recibir_una_respuesta_correcta_con_cuerpo_de_mensaje_retorna_entidad_de_negocio_correspondiente()
        {
            val respuesta = Response.success(entidadDTO)

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualDesdeDTO { respuesta }

            assertEquals(RespuestaIndividual.Exitosa(entidadNegocio), parseado)
        }

        @Test
        fun al_recibir_una_respuesta_correcta_sin_cuerpo_de_mensaje_retorna_respuesta_vacia()
        {
            val respuesta = Response.success<DummyDTO>(null)

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualDesdeDTO { respuesta }

            assertEquals(RespuestaIndividual.Vacia(), parseado)
        }

        @Test
        fun al_recibir_una_respuesta_con_error_body_la_transforma_a_error_de_entidad()
        {
            val codigoDeError = 1
            val mensajeDeError = "El mensaje de error"

            val respuestaConError =
                    Response
                        .error<DummyDTO>(
                                500,
                                ResponseBody.create(
                                        MediaType.parse("application/json"),
                                        """{"internal-code": $codigoDeError,"message": "$mensajeDeError"}"""
                                                   )
                                        )

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualDesdeDTO { respuestaConError }

            assertEquals(RespuestaIndividual.Error.Back(500, ErrorDePeticion(codigoDeError, mensajeDeError)), parseado)
        }

        @Test
        fun al_lanzar_SocketTimeoutException_retorna_error_de_timeout()
        {
            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualDesdeDTO<Dummy, DummyDTO> { throw SocketTimeoutException() }

            assertEquals(RespuestaIndividual.Error.Timeout(), parseado)
        }

        @Test
        fun al_lanzar_IOException_retorna_error_de_red()
        {
            val errorEsperado = IOException("el mensaje")

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualDesdeDTO<Dummy, DummyDTO> { throw errorEsperado }

            assertEquals(RespuestaIndividual.Error.Red(errorEsperado), parseado)
        }
    }

    @Nested
    inner class HaciaRespuestaIndividualColeccionSimple
    {
        private val entidadesDTO = listOf(DummyDTO(0), DummyDTO(1))

        @Test
        fun al_recibir_una_respuesta_correcta_con_cuerpo_de_mensaje_retorna_entidad_de_negocio_correspondiente()
        {
            val respuesta = Response.success(entidadesDTO)

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionSimple { respuesta }

            assertEquals(RespuestaIndividual.Exitosa(entidadesDTO), parseado)
        }

        @Test
        fun al_recibir_una_respuesta_correcta_sin_cuerpo_de_mensaje_retorna_respuesta_vacia()
        {
            val respuesta = Response.success<List<DummyDTO>>(null)

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionSimple { respuesta }

            assertEquals(RespuestaIndividual.Vacia(), parseado)
        }

        @Test
        fun al_recibir_una_respuesta_con_error_body_la_transforma_a_error_de_entidad()
        {
            val codigoDeError = 1
            val mensajeDeError = "El mensaje de error"

            val respuestaConError =
                    Response
                        .error<List<DummyDTO>>(
                                500,
                                ResponseBody.create(
                                        MediaType.parse("application/json"),
                                        """{"internal-code": $codigoDeError,"message": "$mensajeDeError"}"""
                                                   )
                                              )

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionSimple { respuestaConError }

            assertEquals(RespuestaIndividual.Error.Back(500, ErrorDePeticion(codigoDeError, mensajeDeError)), parseado)
        }

        @Test
        fun al_lanzar_SocketTimeoutException_retorna_error_de_timeout()
        {
            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionSimple<List<DummyDTO>> { throw SocketTimeoutException() }

            assertEquals(RespuestaIndividual.Error.Timeout(), parseado)
        }

        @Test
        fun al_lanzar_IOException_retorna_error_de_red()
        {
            val errorEsperado = IOException("el mensaje")

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionSimple<List<DummyDTO>> { throw errorEsperado }

            assertEquals(RespuestaIndividual.Error.Red(errorEsperado), parseado)
        }
    }

    @Nested
    inner class HaciaRespuestaIndividualColeccionDesdeDTO
    {
        private val entidadesNegocio = listOf(Dummy("0"), Dummy("1"))
        private val entidadesDTO = listOf(DummyDTO(0), DummyDTO(1))

        @Test
        fun al_recibir_una_respuesta_correcta_con_cuerpo_de_mensaje_retorna_entidad_de_negocio_correspondiente()
        {
            val respuesta = Response.success(entidadesDTO)

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionDesdeDTO { respuesta }

            assertEquals(RespuestaIndividual.Exitosa(entidadesNegocio), parseado)
        }

        @Test
        fun al_recibir_una_respuesta_correcta_sin_cuerpo_de_mensaje_retorna_respuesta_vacia()
        {
            val respuesta = Response.success<List<DummyDTO>>(null)

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionDesdeDTO { respuesta }

            assertEquals(RespuestaIndividual.Vacia(), parseado)
        }

        @Test
        fun al_recibir_una_respuesta_con_error_body_la_transforma_a_error_de_entidad()
        {
            val codigoDeError = 1
            val mensajeDeError = "El mensaje de error"

            val respuestaConError =
                    Response
                        .error<List<DummyDTO>>(
                                500,
                                ResponseBody.create(
                                        MediaType.parse("application/json"),
                                        """{"internal-code": $codigoDeError,"message": "$mensajeDeError"}"""
                                                   )
                                              )

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionDesdeDTO { respuestaConError }

            assertEquals(RespuestaIndividual.Error.Back(500, ErrorDePeticion(codigoDeError, mensajeDeError)), parseado)
        }

        @Test
        fun al_lanzar_SocketTimeoutException_retorna_error_de_timeout()
        {
            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionDesdeDTO<Dummy, DummyDTO> { throw SocketTimeoutException() }

            assertEquals(RespuestaIndividual.Error.Timeout(), parseado)
        }

        @Test
        fun al_lanzar_IOException_retorna_error_de_red()
        {
            val errorEsperado = IOException("el mensaje")

            val parser = ParserRespuestasRetrofitImpl()

            val parseado = parser.haciaRespuestaIndividualColeccionDesdeDTO<Dummy, DummyDTO> { throw errorEsperado }

            assertEquals(RespuestaIndividual.Error.Red(errorEsperado), parseado)
        }
    }
}