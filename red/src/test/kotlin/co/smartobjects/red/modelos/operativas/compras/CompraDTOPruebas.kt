package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("CompraDTO")
internal class CompraDTOPruebas
{
    companion object
    {
        private val jsonPorDefecto = jsonCompraPorDefecto

        private val jsonPorDefectoEsperado = jsonCompraPorDefectoEsperado

        private val entidadDTOPorDefecto = compraDTOPorDefecto

        private val jsonConNulos = jsonCompraConNulos

        private val entidadDTOConNulos = compraDTOConNulos

        private val entidadNegocioConNulos = compraNegocioConNulos

        private val jsonSinNulos = jsonCompraSinNulos

        private val entidadDTOSinNulos = compraDTOSinNulos

        private val entidadNegocioSinNulos = compraNegocioSinNulos
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, CompraDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, CompraDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, CompraDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_id()
            {
                val jsonSinCampo = """{
                "client-id": 20,
                "committed": true,
                "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
                "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos],
                "payments": [$jsonPagoSinNulos, $jsonPagoSinNulos],
                "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CompraDTO::class.java) }
            }

            @Test
            fun con_id_null()
            {
                val jsonConCampoNull = """{
                "client-id": 20,
                "id": null,
                "committed": true,
                "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
                "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos],
                "payments": [$jsonPagoSinNulos, $jsonPagoSinNulos],
                "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CompraDTO::class.java) }
            }

            @Test
            fun sin_fund_credits()
            {
                val jsonSinCampo = """{
                "client-id": 20,
                "id": "$ID_COMPRA_DEFECTO",
                "committed": true,
                "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos],
                "payments": [$jsonPagoSinNulos, $jsonPagoSinNulos],
                "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CompraDTO::class.java) }
            }

            @Test
            fun con_fund_credits_null()
            {
                val jsonConCampoNull = """{
                "client-id": 20,
                "id": "$ID_COMPRA_DEFECTO",
                "committed": true,
                "fund-credits": null,
                "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos],
                "payments": [$jsonPagoSinNulos, $jsonPagoSinNulos],
                "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CompraDTO::class.java) }
            }

            @Test
            fun sin_package_credits()
            {
                val jsonSinCampo = """{
                "client-id": 20,
                "id": "$ID_COMPRA_DEFECTO",
                "committed": true,
                "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
                "payments": [$jsonPagoSinNulos, $jsonPagoSinNulos],
                "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CompraDTO::class.java) }
            }

            @Test
            fun con_package_credits_null()
            {
                val jsonConCampoNull = """{
                "client-id": 20,
                "id": "$ID_COMPRA_DEFECTO",
                "committed": true,
                "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
                "package-credits": null,
                "payments": [$jsonPagoSinNulos, $jsonPagoSinNulos],
                "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CompraDTO::class.java) }
            }

            @Test
            fun sin_payments()
            {
                val jsonSinCampo = """{
                "client-id": 20,
                "id": "$ID_COMPRA_DEFECTO",
                "committed": true,
                "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
                "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos],
                "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CompraDTO::class.java) }
            }

            @Test
            fun con_payments_null()
            {
                val jsonConCampoNull = """{
                "client-id": 20,
                "id": "$ID_COMPRA_DEFECTO",
                "committed": true,
                "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
                "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos],
                "payments": null,
                "operation-datetime": "$FECHA_REALIZACION_DEFECTO_STR"
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CompraDTO::class.java) }
            }

            @Test
            fun sin_operation_datetime()
            {
                val jsonSinCampo = """{
                "client-id": 20,
                "id": "$ID_COMPRA_DEFECTO",
                "committed": true,
                "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
                "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos],
                "payments": [$jsonPagoSinNulos, $jsonPagoSinNulos]
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CompraDTO::class.java) }
            }

            @Test
            fun con_operation_datetime_null()
            {
                val jsonConCampoNull = """{
                "client-id": 20,
                "id": "$ID_COMPRA_DEFECTO",
                "committed": true,
                "fund-credits": [$jsonCreditoFondoSinNulos, $jsonCreditoFondoSinNulos],
                "package-credits": [$jsonCreditoPaqueteSinNulos, $jsonCreditoPaqueteSinNulos],
                "payments": [$jsonPagoSinNulos, $jsonPagoSinNulos],
                "operation-datetime": null
            } """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CompraDTO::class.java) }
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

            @[Nested DisplayName("falla cuando")]
            inner class FallaCuando
            {
                @Test
                fun el_id_tiene_solo_dos_partes()
                {
                    val id = ID_COMPRA_DEFECTO.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).subList(0, 2).joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())
                    val entidadDTOPruebas = entidadDTOSinNulos.copy(id = id)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        entidadDTOPruebas.aEntidadDeNegocio()
                    }

                    assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun el_id_tiene_mas_de_tres_partes()
                {

                    val id = "$ID_COMPRA_DEFECTO${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}otra-parte"
                    val entidadDTOPruebas = entidadDTOSinNulos.copy(id = id)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        entidadDTOPruebas.aEntidadDeNegocio()
                    }

                    assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun el_tiempo_creacion_es_invalido()
                {
                    val partes = ID_COMPRA_DEFECTO.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
                    partes[0] = "tiempoInvalido"
                    val id = partes.joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())
                    val entidadDTOPruebas = entidadDTOSinNulos.copy(id = id)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        entidadDTOPruebas.aEntidadDeNegocio()
                    }

                    assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun el_uuid_es_invalido()
                {
                    val partes = ID_COMPRA_DEFECTO.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
                    partes[2] = "un-uuid-invalido"
                    val id = partes.joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())
                    val entidadDTOPruebas = entidadDTOSinNulos.copy(id = id)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        entidadDTOPruebas.aEntidadDeNegocio()
                    }

                    assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = CompraDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = CompraDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}