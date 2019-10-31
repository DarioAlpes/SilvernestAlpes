package co.smartobjects.red.modelos.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.operativas.compras.FECHA_DESDE_DEFECTO
import co.smartobjects.red.modelos.operativas.compras.FECHA_DESDE_DEFECTO_STR
import co.smartobjects.red.modelos.operativas.compras.FECHA_HASTA_DEFECTO
import co.smartobjects.red.modelos.operativas.compras.FECHA_HASTA_DEFECTO_STR
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("TransaccionCreditoDTO")
internal class TransaccionCreditoDTOPruebas
{
    companion object
    {
        const val jsonDeserializacionPorDefecto = """
            {
                "transaction-type": "CREDIT",
                "username": "el-usuario",
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "amount": 30.5,
                "device-id": "otro-uuid"
            }
            """

        const val jsonPorDefecto = """
            {
                "transaction-type": "CREDIT",
                "client-id": 0,
                "id": null ,
                "username": "el-usuario",
                "location-id": null,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "amount": 30.5,
                "customer-group-id": null,
                "device-id": "otro-uuid",
                "valid-from": null,
                "valid-until": null
            }
            """

        @JvmField
        val entidadDTOPorDefecto =
                TransaccionCreditoDTO(
                        0,
                        null,
                        "el-usuario",
                        null,
                        70,
                        "código externo fondo",
                        Decimal(30.5),
                        null,
                        "otro-uuid",
                        null,
                        null
                                     )

        @JvmField
        val entidadNegocioPorDefecto =
                Transaccion.Credito(
                        0,
                        null,
                        "el-usuario",
                        null,
                        70,
                        "código externo fondo",
                        Decimal(30.5),
                        null,
                        "otro-uuid",
                        null,
                        null
                                   )

        const val jsonConNulos = """
            {
                "transaction-type": "CREDIT",
                "client-id": 0,
                "id": null ,
                "username": "el-usuario",
                "location-id": null,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "amount": 30.5,
                "customer-group-id": null,
                "device-id": "otro-uuid",
                "valid-from": null,
                "valid-until": null
            }
            """

        @JvmField
        val entidadDTOConNulos =
                TransaccionCreditoDTO(
                        0,
                        null,
                        "el-usuario",
                        null,
                        70,
                        "código externo fondo",
                        Decimal(30.5),
                        null,
                        "otro-uuid",
                        null,
                        null
                                     )

        @JvmField
        val entidadNegocioConNulos =
                Transaccion.Credito(
                        0,
                        null,
                        "el-usuario",
                        null,
                        70,
                        "código externo fondo",
                        Decimal(30.5),
                        null,
                        "otro-uuid",
                        null,
                        null
                                   )

        const val jsonSinNulos = """
            {
                "transaction-type": "CREDIT",
                "client-id": 10,
                "id": 20 ,
                "username": "el-usuario",
                "location-id": 90,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "amount": 30.5,
                "customer-group-id": 100,
                "device-id": "otro-uuid",
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR"
            }
            """

        @JvmField
        val entidadDTOSinNulos =
                TransaccionCreditoDTO(
                        10,
                        20,
                        "el-usuario",
                        90,
                        70,
                        "código externo fondo",
                        Decimal(30.5),
                        100,
                        "otro-uuid",
                        FECHA_DESDE_DEFECTO,
                        FECHA_HASTA_DEFECTO
                                     )

        @JvmField
        val entidadNegocioSinNulos =
                Transaccion.Credito(
                        10,
                        20,
                        "el-usuario",
                        90,
                        70,
                        "código externo fondo",
                        Decimal(30.5),
                        100,
                        "otro-uuid",
                        FECHA_DESDE_DEFECTO,
                        FECHA_HASTA_DEFECTO
                                   )
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, TransaccionCreditoDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, TransaccionCreditoDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, TransaccionCreditoDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_tipo()
            {
                val json = """
                {
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-id": 70,
                    "fund-external-code": "código externo fondo",
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun con_tipo_null()
            {
                val json = """
                {
                    "transaction-type": null,
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-id": 70,
                    "fund-external-code": "código externo fondo",
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun sin_nombre_de_usuario()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "location-id": 90,
                    "fund-id": 70,
                    "fund-external-code": "código externo fondo",
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun con_nombre_de_usuario_null()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "username": null,
                    "location-id": 90,
                    "fund-id": 70,
                    "fund-external-code": "código externo fondo",
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun sin_id_de_fondo()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-external-code": "código externo fondo",
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun con_id_de_fondo_null()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-id": null,
                    "fund-external-code": "código externo fondo",
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun sin_codigo_externo_fondo()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-id": 70,
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun con_codigo_externo_fondo_null()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-id": 70,
                    "fund-external-code": null,
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun sin_cantidad()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-id": 70,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun con_cantidad_null()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-id": 70,
                    "amount": null,
                    "customer-group-id": 100,
                    "device-id": "otro-uuid",
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun sin_id_de_dispositivo()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-id": 70,
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }

            @Test
            fun con_id_de_dispositivo_null()
            {
                val json = """
                {
                    "transaction-type": "CREDIT",
                    "client-id": 10,
                    "id": 20 ,
                    "username": "el-usuario",
                    "location-id": 90,
                    "fund-id": 70,
                    "amount": 30.5,
                    "customer-group-id": 100,
                    "device-id": null,
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, TransaccionCreditoDTO::class.java) }
            }
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun con_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOPorDefecto)

            JSONAssert.assertEquals(jsonPorDefecto, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun con_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOConNulos)

            JSONAssert.assertEquals(jsonConNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun sin_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOSinNulos)

            JSONAssert.assertEquals(jsonSinNulos, entidadSerializada, JSONCompareMode.STRICT)
        }
    }

    @Test
    fun el_tipo_es_CREDITO()
    {
        assertEquals(TransaccionDTO.Tipo.CREDITO, entidadDTOSinNulos.tipo)
    }

    @Nested
    inner class Conversion
    {
        @Nested
        inner class AEntidadDeNegocio
        {
            @Test
            fun con_por_defecto_se_transforma_correctamente()
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
            fun con_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = TransaccionCreditoDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = TransaccionCreditoDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = TransaccionCreditoDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}