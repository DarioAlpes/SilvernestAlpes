package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("CreditoDTO")
internal class CreditoFondoDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = jsonCreditoFondoPorDefecto

        const val jsonPorDefectoEsperado = jsonCreditoFondoPorDefectoEsperado

        @JvmField
        val entidadDTOPorDefecto = creditoFondoDTOPorDefecto

        @JvmField
        val entidadNegocioPorDefecto = creditoFondoNegocioPorDefecto

        private const val jsonConNulos = jsonCreditoFondoConNulos

        private val entidadDTOConNulos = creditoFondoDTOConNulos

        private val entidadNegocioConNulos = creditoFondoNegocioConNulos

        private const val jsonSinNulosConAncestros = jsonCreditoFondoSinNulos

        private val entidadDTOSinNulosConAncestros = creditoFondoDTOSinNulos

        private val entidadNegocioSinNulosConAncestros = creditoFondoNegocioSinNulos
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, CreditoFondoDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, CreditoFondoDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_con_ancestros_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulosConAncestros, CreditoFondoDTO::class.java)
            assertEquals(entidadDTOSinNulosConAncestros, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_amount()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-external-code": "código externo fondo",
                "fund-id": 70,
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_amount_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": null,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_base_price()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_base_price_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": null,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_tax_paid()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_tax_paid_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": null,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_source()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_source_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": null,
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_username()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_username_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": null,
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_id_person()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_id_person_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": null,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_id_fund()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_id_fund_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": null,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_codigo_externo_fondo()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_codigo_externo_fondo_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": null,
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_id_tax()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_id_tax_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": null,
                "device-id": "otro-uuid",
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_id_device()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun con_id_device_null()
            {
                val jsonConCampoNull = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": null,
                "location-id": 90,
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_id_location()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "customer-group-id": 100
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }

            @Test
            fun sin_id_customer_group()
            {
                val jsonSinCampo = """{
                "client-id": 10,
                "id": 20 ,
                "amount": 30.5,
                "price-paid": 40.5,
                "tax-paid": 50.5,
                "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                "consumed": true,
                "source": "Orbita",
                "username": "el-usuario",
                "person-id": 60,
                "fund-id": 70,
                "fund-external-code": "código externo fondo",
                "tax-id": 80,
                "device-id": "otro-uuid",
                "location-id": 90
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java) }
            }
        }

        @[Nested DisplayName("no lanza excepción con un json")]
        inner class NoLanzaExcepcion
        {
            @Test
            fun sin_valid_from()
            {
                val jsonSinCampo = """{
                    "client-id": 10,
                    "id": 20 ,
                    "amount": 30.5,
                    "price-paid": 40.5,
                    "tax-paid": 50.5,
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                    "consumed": true,
                    "source": "Orbita",
                    "username": "el-usuario",
                    "person-id": 60,
                    "fund-id": 70,
                    "fund-external-code": "código externo fondo",
                    "tax-id": 80,
                    "device-id": "otro-uuid",
                    "location-id": 90,
                    "customer-group-id": 100
                } """

                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java)
            }

            @Test
            fun con_valid_from_null()
            {
                val jsonConCampoNull = """{
                    "client-id": 10,
                    "id": 20 ,
                    "amount": 30.5,
                    "price-paid": 40.5,
                    "tax-paid": 50.5,
                    "valid-from": null,
                    "valid-until": "$FECHA_HASTA_DEFECTO_STR",
                    "consumed": true,
                    "source": "Orbita",
                    "username": "el-usuario",
                    "person-id": 60,
                    "fund-id": 70,
                    "fund-external-code": "código externo fondo",
                    "tax-id": 80,
                    "device-id": "otro-uuid",
                    "location-id": 90,
                    "customer-group-id": 100
                } """

                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java)
            }

            @Test
            fun sin_valid_until()
            {
                val jsonSinCampo = """{
                    "client-id": 10,
                    "id": 20 ,
                    "amount": 30.5,
                    "price-paid": 40.5,
                    "tax-paid": 50.5,
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "consumed": true,
                    "source": "Orbita",
                    "username": "el-usuario",
                    "person-id": 60,
                    "fund-id": 70,
                    "fund-external-code": "código externo fondo",
                    "tax-id": 80,
                    "device-id": "otro-uuid",
                    "location-id": 90,
                    "customer-group-id": 100
                } """

                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CreditoFondoDTO::class.java)
            }

            @Test
            fun con_valid_until_null()
            {
                val jsonConCampoNull = """{
                    "client-id": 10,
                    "id": 20 ,
                    "amount": 30.5,
                    "price-paid": 40.5,
                    "tax-paid": 50.5,
                    "valid-from": "$FECHA_DESDE_DEFECTO_STR",
                    "valid-until": null,
                    "consumed": true,
                    "source": "Orbita",
                    "username": "el-usuario",
                    "person-id": 60,
                    "fund-id": 70,
                    "fund-external-code": "código externo fondo",
                    "tax-id": 80,
                    "device-id": "otro-uuid",
                    "location-id": 90,
                    "customer-group-id": 100
                } """

                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CreditoFondoDTO::class.java)
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
                val entidadDTO = CreditoFondoDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = CreditoFondoDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = CreditoFondoDTO(entidadNegocioSinNulosConAncestros)
                assertEquals(entidadDTOSinNulosConAncestros, entidadDTO)
            }
        }
    }
}