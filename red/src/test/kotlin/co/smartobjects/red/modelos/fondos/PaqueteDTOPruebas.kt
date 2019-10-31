package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals


@DisplayName("PaqueteDTO")
internal class PaqueteDTOPruebas
{
    companion object
    {
        private const val FECHA_DESDE_DEFECTO_STR = "2000-01-02T03:04:05-05:00[UTC-05:00]"
        private val FECHA_DESDE_DEFECTO = ZonedDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)
        private const val FECHA_HASTA_DEFECTO_STR = "2001-02-03T03:04:05-05:00[UTC-05:00]"
        private val FECHA_HASTA_DEFECTO = ZonedDateTime.of(LocalDate.of(2001, 2, 3), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)

        const val jsonPorDefectoDeserializacion = """
            {
                "client-id": 0,
                "id": 0,
                "name": "Por Defecto",
                "description": "descripción",
                "available-for-sale": false,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [
                        {
                            "fund-id": 1,
                            "fund-external-code": "El código externo",
                            "amount": 1
                        }
                    ],
                "external-code": "El código externo"
            }
            """

        const val jsonPorDefectoSerializacion = """
            {
                "client-id": 0,
                "id": 0,
                "name": "Por Defecto",
                "description": "descripción",
                "available-for-sale": false,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [
                        {
                            "fund-id": 1,
                            "fund-external-code": "El código externo",
                            "amount": 1
                        }
                    ],
                "external-code": "El código externo"
            }
            """

        @JvmField
        val paquetePorDefecto =
                PaqueteDTO(
                        0,
                        0,
                        "Por Defecto",
                        "descripción",
                        false,
                        FECHA_DESDE_DEFECTO,
                        FECHA_HASTA_DEFECTO,
                        listOf(
                                PaqueteDTO.FondoIncluidoDTO(1, "El código externo", Decimal.UNO)
                              ),
                        "El código externo"
                          )

        @JvmField
        val paqueteNegocioPorDefecto =
                Paquete(
                        0,
                        0,
                        "Por Defecto",
                        "descripción",
                        false,
                        FECHA_DESDE_DEFECTO,
                        FECHA_HASTA_DEFECTO,
                        listOf(
                                Paquete.FondoIncluido(1, "El código externo", Decimal.UNO)
                              ),
                        "El código externo"
                       )

        const val jsonConNulos = """
            {
                "client-id": 1,
                "id": null,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [
                        {
                            "fund-id": 1,
                            "fund-external-code": "El código externo",
                            "amount": 1
                        }
                    ],
                "external-code": "El código externo"
            }
            """

        @JvmField
        val paqueteConNulos =
                PaqueteDTO(
                        1,
                        null,
                        "Paquete 1",
                        "Paquete",
                        true,
                        FECHA_DESDE_DEFECTO,
                        FECHA_HASTA_DEFECTO,
                        listOf(
                                PaqueteDTO.FondoIncluidoDTO(1, "El código externo", Decimal.UNO)
                              ),
                        "El código externo"
                          )

        @JvmField
        val paqueteNegocioConNulos =
                Paquete(
                        1,
                        null,
                        "Paquete 1",
                        "Paquete",
                        true,
                        FECHA_DESDE_DEFECTO,
                        FECHA_HASTA_DEFECTO,
                        listOf(
                                Paquete.FondoIncluido(1, "El código externo", Decimal.UNO)
                              ),
                        "El código externo"
                       )

        const val jsonSinNulos = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [
                        {
                            "fund-id": 1,
                            "fund-external-code": "El código externo 1",
                            "amount": 1
                        },
                        {
                            "fund-id": 2,
                            "fund-external-code": "El código externo 2",
                            "amount": 1
                        },
                        {
                            "fund-id": 3,
                            "fund-external-code": "El código externo 3",
                            "amount": 1
                        }
                    ],
                "external-code": "El código externo"
            }
            """

        @JvmField
        val paqueteSinNulos =
                PaqueteDTO(
                        1,
                        1,
                        "Paquete 1",
                        "Paquete",
                        true,
                        FECHA_DESDE_DEFECTO,
                        FECHA_HASTA_DEFECTO,
                        listOf(
                                PaqueteDTO.FondoIncluidoDTO(1, "El código externo 1", Decimal.UNO),
                                PaqueteDTO.FondoIncluidoDTO(2, "El código externo 2", Decimal.UNO),
                                PaqueteDTO.FondoIncluidoDTO(3, "El código externo 3", Decimal.UNO)
                              ),
                        "El código externo"
                          )

        @JvmField
        val paqueteNegocioSinNulos =
                Paquete(
                        1,
                        1,
                        "Paquete 1",
                        "Paquete",
                        true,
                        FECHA_DESDE_DEFECTO,
                        FECHA_HASTA_DEFECTO,
                        listOf(
                                Paquete.FondoIncluido(1, "El código externo 1", Decimal.UNO),
                                Paquete.FondoIncluido(2, "El código externo 2", Decimal.UNO),
                                Paquete.FondoIncluido(3, "El código externo 3", Decimal.UNO)
                              ),
                        "El código externo"
                       )
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefectoDeserializacion, PaqueteDTO::class.java)
            assertEquals(paquetePorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, PaqueteDTO::class.java)
            assertEquals(paqueteConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, PaqueteDTO::class.java)
            assertEquals(paqueteSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_nombre()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": null,
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun sin_descripcion()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun con_descripcion_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": null,
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun sin_disponibleParaLaVenta()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun con_disponibleParaLaVenta_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": null,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun sin_validoDesde()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun con_validoDesde_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": null,
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun sin_validoHasta()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun con_validoHasta_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": null,
                "included-funds": [],
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun sin_fondos_incluidos()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun con_fondos_incluidos_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": null,
                "external-code": "El código externo"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun sin_codigo_externo()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": []
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }

            @Test
            fun con_codigo_externo_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Paquete 1",
                "description": "Paquete",
                "available-for-sale": true,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "included-funds": [],
                "external-code": null
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaqueteDTO::class.java) }
            }
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun de_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(paquetePorDefecto)

            JSONAssert.assertEquals(jsonPorDefectoSerializacion, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun con_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(paqueteConNulos)

            JSONAssert.assertEquals(jsonConNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun sin_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(paqueteSinNulos)

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
                val entidadDeNegocio = paquetePorDefecto.aEntidadDeNegocio()
                assertEquals(paqueteNegocioPorDefecto, entidadDeNegocio)
            }

            @Test
            fun con_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = paqueteConNulos.aEntidadDeNegocio()
                assertEquals(paqueteNegocioConNulos, entidadDeNegocio)
            }

            @Test
            fun sin_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = paqueteSinNulos.aEntidadDeNegocio()
                assertEquals(paqueteNegocioSinNulos, entidadDeNegocio)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = PaqueteDTO(paqueteNegocioPorDefecto)
                assertEquals(paquetePorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = PaqueteDTO(paqueteNegocioConNulos)
                assertEquals(paqueteConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = PaqueteDTO(paqueteNegocioSinNulos)
                assertEquals(paqueteSinNulos, entidadDTO)
            }
        }
    }
}