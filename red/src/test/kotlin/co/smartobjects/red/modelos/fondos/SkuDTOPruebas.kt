package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Sku
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.fondos.precios.jsonPrecioSinNulos
import co.smartobjects.red.modelos.fondos.precios.precioDTOSinNulos
import co.smartobjects.red.modelos.fondos.precios.precioNegocioSinNulos
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("SkuDTO")
internal class SkuDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = """
            {
                "fund-type": "SKU",
                "name": "Por Defecto",
                "available-for-sale": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 0
            }
            """

        const val jsonPorDefectoEsperado = """
            {
                "fund-type": "SKU",
                "client-id": 0,
                "id": null,
                "name": "Por Defecto",
                "available-for-sale": false,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 0,
                "image-key": null
            }
            """

        @JvmField
        val entidadDTOPorDefecto = SkuDTO(
                0,
                null,
                "Por Defecto",
                false,
                false,
                false,
                precioDTOSinNulos,
                "El código externo",
                0,
                null)

        @JvmField
        val entidadNegocioPorDefecto = Sku(
                0,
                null,
                "Por Defecto",
                false,
                false,
                false,
                precioNegocioSinNulos,
                "El código externo",
                0,
                null)

        private const val jsonConNulos = """
            {
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 0,
                "image-key": null
            }"""

        private val entidadDTOConNulos = SkuDTO(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioDTOSinNulos,
                "El código externo",
                0,
                null)

        private val entidadNegocioConNulos = Sku(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioNegocioSinNulos,
                "El código externo",
                0,
                null)


        const val jsonSinNulos = """
            {
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 3,
                "image-key": "prueba-llave"
            }"""

        val entidadDTOSinNulos = SkuDTO(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioDTOSinNulos,
                "El código externo",
                3,
                "prueba-llave")

        val entidadNegocioSinNulos = Sku(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioNegocioSinNulos,
                "El código externo",
                3,
                "prueba-llave")

    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, SkuDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, SkuDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, SkuDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }


        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_nombre()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": null,
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun sin_available_for_sale()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun con_available_for_sale_null()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": null,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun sin_unlimited()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun con_unlimited_null()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": null,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun sin_id_categoria()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun con_id_categoria_null()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "category-id": null,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun sin_precio_por_defecto()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "external-code": "El código externo",
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun con_precio_por_defecto_null()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": null,
                "external-code": "El código externo",
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun sin_codigo_externo()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
            }

            @Test
            fun con_codigo_externo_null()
            {
                val json = """{
                "fund-type": "SKU",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": null,
                "category-id": 3,
                "image-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SkuDTO::class.java) }
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
            fun con_valores_por_defecto_se_construye_correctamente_desde_entidad()
            {
                val entidadDTO = SkuDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente_desde_entidad()
            {
                val entidadDTO = SkuDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente_desde_entidad()
            {
                val entidadDTO = SkuDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}