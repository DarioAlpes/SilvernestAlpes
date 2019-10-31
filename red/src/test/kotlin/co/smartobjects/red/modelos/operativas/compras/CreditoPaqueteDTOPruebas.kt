package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("CreditoPaqueteDTO")
internal class CreditoPaqueteDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = jsonCreditoPaquetePorDefecto

        const val jsonPorDefectoEsperado = jsonCreditoPaquetePorDefecto

        @JvmField
        val entidadDTOPorDefecto = creditoPaqueteDTOPorDefecto

        private const val jsonConNulos = jsonCreditoPaqueteConNulos

        private val entidadDTOConNulos = creditoPaqueteDTOConNulos

        private val entidadNegocioConNulos = creditoPaqueteNegocioConNulos

        private const val jsonSinNulosConAncestros = jsonCreditoPaqueteSinNulos

        private val entidadDTOSinNulosConAncestros = creditoPaqueteDTOSinNulos

        private val entidadNegocioSinNulosConAncestros = creditoPaqueteNegocioSinNulos
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, CreditoPaqueteDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, CreditoPaqueteDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_con_ancestros_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulosConAncestros, CreditoPaqueteDTO::class.java)
            assertEquals(entidadDTOSinNulosConAncestros, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_id_package()
            {
                val jsonSinCampo = """{ "credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos]}"""
                assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoPaqueteDTO::class.java) })
            }

            @Test
            fun con_id_package_null()
            {
                val jsonConCampoNull = """{ "package-id": null, "credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos]}"""
                assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoPaqueteDTO::class.java) })
            }

            @Test
            fun sin_credits()
            {
                val jsonSinCampo = """{ "package-id": 2}"""
                assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoPaqueteDTO::class.java) })
            }

            @Test
            fun con_credits_null()
            {
                val jsonConCampoNull = """{ "package-id": 2, "credits": null}"""
                assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoPaqueteDTO::class.java) })
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
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = CreditoPaqueteDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = CreditoPaqueteDTO(entidadNegocioSinNulosConAncestros)
                assertEquals(entidadDTOSinNulosConAncestros, entidadDTO)
            }
        }
    }
}