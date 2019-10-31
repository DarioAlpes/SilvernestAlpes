package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.compras.CreditosDeUnaPersona
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("CreditosDeUnaPersonaDTO")
internal class CreditosDeUnaPersonaDTOPruebas
{
    companion object
    {
        private const val ID_CLIENTE = 1L
        private const val ID_PERSONA_POR_DEFECTO = 1001003L

        private const val jsonPorDefecto =
                """{
                    "person-id": $ID_PERSONA_POR_DEFECTO,
                    "fund-credits": [],
                    "package-credits": []
                } """

        private const val jsonPorDefectoEsperado =
                """{
                    "client-id": 0,
                    "person-id": $ID_PERSONA_POR_DEFECTO,
                    "fund-credits": [],
                    "package-credits": []
                } """

        private val entidadDTOPorDefecto = CreditosDeUnaPersonaDTO(0L, ID_PERSONA_POR_DEFECTO, listOf(), listOf())

        private val entidadNegocioPorDefecto = CreditosDeUnaPersona(0L, ID_PERSONA_POR_DEFECTO, listOf(), listOf())

        private const val jsonSinNulos =
                """{
                    "client-id": $ID_CLIENTE,
                    "person-id": $ID_PERSONA_POR_DEFECTO,
                    "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
                    "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos]
                } """

        private val entidadDTOSinNulos =
                CreditosDeUnaPersonaDTO(
                        ID_CLIENTE,
                        ID_PERSONA_POR_DEFECTO,
                        listOf(creditoFondoDTOSinNulos, creditoFondoDTOSinNulos),
                        listOf(creditoPaqueteDTOSinNulos, creditoPaqueteDTOSinNulos)
                                       )

        private val entidadNegocioSinNulos =
                CreditosDeUnaPersona(
                        ID_CLIENTE,
                        ID_PERSONA_POR_DEFECTO,
                        listOf(creditoFondoNegocioSinNulos, creditoFondoNegocioSinNulos),
                        listOf(creditoPaqueteNegocioSinNulos, creditoPaqueteNegocioSinNulos)
                                    )
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, CreditosDeUnaPersonaDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, CreditosDeUnaPersonaDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_id_persona()
            {
                val jsonSinCampo = """{
                        "id": "$ID_CLIENTE",
                        "fund-credits": [],
                        "package-credits": []
                    } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditosDeUnaPersonaDTO::class.java) }
            }

            @Test
            fun con_id_persona_null()
            {
                val jsonConCampoNull = """{
                        "id": "$ID_CLIENTE",
                        "person-id": null,
                        "fund-credits": [],
                        "package-credits": []
                    } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditosDeUnaPersonaDTO::class.java) }
            }

            @Test
            fun sin_fund_credits()
            {
                val jsonSinCampo = """{
                        "id": "$ID_CLIENTE",
                        "person-id": $ID_PERSONA_POR_DEFECTO,
                        "package-credits": []
                    } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditosDeUnaPersonaDTO::class.java) }
            }

            @Test
            fun con_fund_credits_null()
            {
                val jsonConCampoNull = """{
                        "id": "$ID_CLIENTE",
                        "person-id": $ID_PERSONA_POR_DEFECTO,
                        "fund-credits": null,
                        "package-credits": []
                    } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditosDeUnaPersonaDTO::class.java) }
            }

            @Test
            fun sin_package_credits()
            {
                val jsonSinCampo = """{
                        "id": "$ID_CLIENTE",
                        "person-id": $ID_PERSONA_POR_DEFECTO,
                        "fund-credits": []
                    } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditosDeUnaPersonaDTO::class.java) }
            }

            @Test
            fun con_package_credits_null()
            {
                val jsonConCampoNull = """{
                        "id": "$ID_CLIENTE",
                        "person-id": $ID_PERSONA_POR_DEFECTO,
                        "fund-credits": [],
                        "package-credits": null
                    } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditosDeUnaPersonaDTO::class.java) }
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
        fun sin_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOSinNulos)
            JSONAssert.assertEquals(jsonSinNulos, entidadSerializada, JSONCompareMode.STRICT)
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
            fun sin_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = entidadDTOSinNulos.aEntidadDeNegocio()
                assertEquals(entidadNegocioSinNulos, entidadDeNegocio)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = CreditosDeUnaPersonaDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = CreditosDeUnaPersonaDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}