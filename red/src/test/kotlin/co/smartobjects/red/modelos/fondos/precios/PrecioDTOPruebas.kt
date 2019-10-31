package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("PrecioDTO")
internal class PrecioDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = jsonPrecioPorDefecto

        const val jsonPorDefectoEsperado = jsonPrecioPorDefecto

        @JvmField
        val entidadDTOPorDefecto = precioDTOPorDefecto

        @JvmField
        val entidadNegocioPorDefecto = precioNegocioPorDefecto

        private const val jsonConNulos = jsonPrecioConNulos

        private val entidadDTOConNulos = precioDTOConNulos

        private val entidadNegocioConNulos = precioNegocioConNulos

        private const val jsonSinNulosConAncestros = jsonPrecioSinNulos

        private val entidadDTOSinNulosConAncestros = precioDTOSinNulos

        private val entidadNegocioSinNulosConAncestros = precioNegocioSinNulos
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, PrecioDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, PrecioDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_con_ancestros_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulosConAncestros, PrecioDTO::class.java)
            assertEquals(entidadDTOSinNulosConAncestros, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_value()
            {
                val jsonSinValue = """{
                "tax-id": 4
            }"""
                assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinValue, PrecioDTO::class.java) })
            }

            @Test
            fun con_value_null()
            {
                val jsonConValueNull = """{
                "value": null,
                "tax-id": 4
            }"""
                assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConValueNull, PrecioDTO::class.java) })
            }

            @Test
            fun sin_id_tax()
            {
                val jsonSinValue = """{
                "value": 100.5
            }"""
                assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinValue, PrecioDTO::class.java) })
            }

            @Test
            fun con_id_tax_null()
            {
                val jsonConValueNull = """{
                "value": 100.5,
                "tax-id": null
            }"""
                assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConValueNull, PrecioDTO::class.java) })
            }
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
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOSinNulosConAncestros)
            JSONAssert.assertEquals(jsonSinNulosConAncestros, entidadSerializada, JSONCompareMode.STRICT)
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
                val entidadDeNegocio = entidadDTOSinNulosConAncestros.aEntidadDeNegocio()
                assertEquals(entidadNegocioSinNulosConAncestros, entidadDeNegocio)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = PrecioDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = PrecioDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = PrecioDTO(entidadNegocioSinNulosConAncestros)
                assertEquals(entidadDTOSinNulosConAncestros, entidadDTO)
            }
        }
    }
}