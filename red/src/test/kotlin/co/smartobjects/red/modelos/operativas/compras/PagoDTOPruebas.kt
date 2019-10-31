package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.util.stream.Stream
import kotlin.test.assertEquals

@DisplayName("PagoDTO")
internal class PagoDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = jsonPagoPorDefecto

        const val jsonPorDefectoEsperado = jsonPagoPorDefecto

        @JvmField
        val entidadDTOPorDefecto = pagoDTOPorDefecto

        @JvmField
        val entidadNegocioPorDefecto = pagoNegocioPorDefecto

        private const val jsonConNulos = jsonPagoConNulos

        private val entidadDTOConNulos = pagoDTOConNulos

        private val entidadNegocioConNulos = pagoNegocioConNulos

        private const val jsonSinNulos = jsonPagoSinNulos

        private val entidadDTOSinNulos = pagoDTOSinNulos

        private val entidadNegocioSinNulos = pagoNegocioSinNulos
    }

    internal class ProveedorMetodoDePagoDTOConMetodoDePagoEnJson : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PagoDTO.MetodoDePago.EFECTIVO, "CASH"),
                    Arguments.of(PagoDTO.MetodoDePago.TARJETA_CREDITO, "CREDIT_CARD"),
                    Arguments.of(PagoDTO.MetodoDePago.TARJETA_DEBITO, "DEBIT_CARD"),
                    Arguments.of(PagoDTO.MetodoDePago.TIC, "TIC")
                            )
        }
    }

    internal class ProveedorMetodoDePagoDTOConMetodoDePagoNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PagoDTO.MetodoDePago.EFECTIVO, Pago.MetodoDePago.EFECTIVO),
                    Arguments.of(PagoDTO.MetodoDePago.TARJETA_DEBITO, Pago.MetodoDePago.TARJETA_DEBITO),
                    Arguments.of(PagoDTO.MetodoDePago.TARJETA_CREDITO, Pago.MetodoDePago.TARJETA_CREDITO),
                    Arguments.of(PagoDTO.MetodoDePago.TIC, Pago.MetodoDePago.TIC)
                            )
        }
    }

    private fun darEntidadDTOPruebaSegunMetodoDePago(metodoDePagoDTO: PagoDTO.MetodoDePago): PagoDTO
    {
        return PagoDTO(Decimal(100.5), metodoDePagoDTO, "45-6")
    }

    private fun darJsonSegunMetodoPago(metodoDePagoDTO: String): String
    {
        return """{ "value": 100.5, "method": "$metodoDePagoDTO", "pos-transaction-number": "45-6" }"""
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, PagoDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, PagoDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_con_ancestros_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, PagoDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_value()
            {
                val jsonSinCampo = """{ "method": "Efectivo" }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, PagoDTO::class.java) }
            }

            @Test
            fun con_value_null()
            {
                val jsonConCampoNull = """{ "value": null, "method": "Efectivo" }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, PagoDTO::class.java) }
            }

            @Test
            fun sin_method()
            {
                val jsonSinCampo = """{ "value": 100.5 }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, PagoDTO::class.java) }
            }

            @Test
            fun con_method()
            {
                val jsonConCampoNull = """{ "value": 100.5, "method": null }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, PagoDTO::class.java) }
            }
        }

        @DisplayName("Cuando el metodo de pago")
        @ParameterizedTest(name = "Es ''{1}'' en el json asigna ''{0}'' en el DTO")
        @ArgumentsSource(ProveedorMetodoDePagoDTOConMetodoDePagoEnJson::class)
        fun paraMetodoDePago(metodoDePagoDTO: PagoDTO.MetodoDePago, metodoDePagoEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunMetodoDePago(metodoDePagoDTO)
            val json = darJsonSegunMetodoPago(metodoDePagoEnJson)
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(json, PagoDTO::class.java)
            assertEquals(entidadDTO, entidadDeserializada)
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun de_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOPorDefecto)

            JSONAssert.assertEquals(jsonPorDefectoEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun con_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOConNulos)
            JSONAssert.assertEquals(jsonConNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun sin_nulos_con_ancestros_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOSinNulos)
            JSONAssert.assertEquals(jsonSinNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @DisplayName("Cuando el metodo de pago")
        @ParameterizedTest(name = "Es ''{1}'' en el DTO asigna ''{0}'' en el json")
        @ArgumentsSource(ProveedorMetodoDePagoDTOConMetodoDePagoEnJson::class)
        fun paraMetodoDePago(metodoDePagoDTO: PagoDTO.MetodoDePago, metodoDePagoEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunMetodoDePago(metodoDePagoDTO)
            val jsonEsperado = darJsonSegunMetodoPago(metodoDePagoEnJson)

            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

            JSONAssert.assertEquals(jsonEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }
    }

    @Nested
    inner class Conversion
    {
        @Nested
        inner class AEntidadDeNegocio
        {
            @Test
            fun con_valores_por_defecto_se_transforma_correctamente()
            {
                val entidadDeNegocio = entidadDTOPorDefecto.aEntidadDeNegocio()
                assertEquals(entidadNegocioPorDefecto, entidadDeNegocio)
            }

            @Test
            fun con_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = entidadDTOConNulos.aEntidadDeNegocio()
                assertEquals(entidadNegocioConNulos, entidadDeNegocio)
            }

            @Test
            fun sin_nulos_con_ancestros_se_transforma_correctamente()
            {
                val entidadDeNegocio = entidadDTOSinNulos.aEntidadDeNegocio()
                assertEquals(entidadNegocioSinNulos, entidadDeNegocio)
            }

            @DisplayName("Cuando el metodo de pago")
            @ParameterizedTest(name = "Es ''{0}'' en el DTO asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorMetodoDePagoDTOConMetodoDePagoNegocio::class)
            fun paraMetodoDePago(metodoDePagoDTO: PagoDTO.MetodoDePago, metodoDePago: Pago.MetodoDePago)
            {
                val entidadDTO = darEntidadDTOPruebaSegunMetodoDePago(metodoDePagoDTO)
                val entidaComoNegocio = entidadDTO.aEntidadDeNegocio()
                assertEquals(metodoDePago, entidaComoNegocio.metodoPago)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            private fun darEntidadPruebaSegunMetodoDePago(
                    metodoDePagoDTO: Pago.MetodoDePago
                                                         )
                    : Pago
            {
                return Pago(Decimal(100.5), metodoDePagoDTO, "45-6")
            }

            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = PagoDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = PagoDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = PagoDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }

            @DisplayName("Cuando el metodo de pago")
            @ParameterizedTest(name = "Es ''{1}'' en el negocio asigna ''{0}'' en el DTO")
            @ArgumentsSource(ProveedorMetodoDePagoDTOConMetodoDePagoNegocio::class)
            fun paraMetodoDePago(metodoDePagoDTO: PagoDTO.MetodoDePago, metodoDePago: Pago.MetodoDePago)
            {
                val entidadNegocio = darEntidadPruebaSegunMetodoDePago(metodoDePago)
                val entidadComoDTO = PagoDTO(entidadNegocio)
                assertEquals(metodoDePagoDTO, entidadComoDTO.metodoPago)
            }
        }
    }
}