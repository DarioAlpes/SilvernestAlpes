package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.CategoriaSku
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

@DisplayName("CategoriaSkuDTO")
internal class CategoriaSkuDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = """
            {
                "fund-type": "SKU-CATEGORY",
                "name": "Por Defecto",
                "available-for-sale": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "parent-category-id": null
            }
            """

        const val jsonPorDefectoEsperado = """
            {
                "fund-type": "SKU-CATEGORY",
                "client-id": 0,
                "id": null,
                "name": "Por Defecto",
                "available-for-sale": false,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }
            """

        @JvmField
        val entidadDTOPorDefecto = CategoriaSkuDTO(
                0,
                null,
                "Por Defecto",
                false,
                false,
                false,
                precioDTOSinNulos,
                "El código externo",
                null,
                listOf(),
                null)

        @JvmField
        val entidadNegocioPorDefecto = CategoriaSku(
                0,
                null,
                "Por Defecto",
                false,
                false,
                false,
                precioNegocioSinNulos,
                "El código externo",
                null,
                linkedSetOf(),
                null)

        private const val jsonConNulos = """{
            "fund-type": "SKU-CATEGORY",
            "client-id": 1,
            "id": 2,
            "name": "Entidad prueba",
            "available-for-sale": true,
            "once-per-session": false,
            "unlimited": false,
            "default-price": $jsonPrecioSinNulos,
            "external-code": "El código externo",
            "parent-category-id": null,
            "ancestors-ids": [],
            "icon-key": null
        }"""

        private val entidadDTOConNulos = CategoriaSkuDTO(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioDTOSinNulos,
                "El código externo",
                null,
                listOf(),
                null)

        private val entidadNegocioConNulos = CategoriaSku(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioNegocioSinNulos,
                "El código externo",
                null,
                linkedSetOf(),
                null)


        const val jsonSinNulosConAncestros = """{
            "fund-type": "SKU-CATEGORY",
            "client-id": 1,
            "id": 2,
            "name": "Entidad prueba",
            "available-for-sale": true,
            "once-per-session": false,
            "unlimited": false,
            "default-price": $jsonPrecioSinNulos,
            "external-code": "El código externo",
            "parent-category-id": 3,
            "ancestors-ids": [1, 2, 3],
            "icon-key": "prueba-llave"
        }"""

        val entidadDTOSinNulosConAncestros = CategoriaSkuDTO(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioDTOSinNulos,
                "El código externo",
                3,
                listOf(1, 2, 3),
                "prueba-llave")

        val entidadNegocioSinNulosConAncestros = CategoriaSku(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioNegocioSinNulos,
                "El código externo",
                3,
                linkedSetOf(1, 2, 3),
                "prueba-llave")

    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, CategoriaSkuDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, CategoriaSkuDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_con_ancestros_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulosConAncestros, CategoriaSkuDTO::class.java)
            assertEquals(entidadDTOSinNulosConAncestros, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_nombre()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": null,
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun sin_available_for_sale()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun con_available_for_sale_null()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": null,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun sin_unlimited()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun con_unlimited_null()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": null,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun sin_id_categoria_padre()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun sin_precio_por_defecto()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "external-code": "El código externo",
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun con_precio_por_defecto_null()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": null,
                "external-code": "El código externo",
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun sin_codigo_externo()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
            }

            @Test
            fun con_codigo_externo_null()
            {
                val json = """{
                "fund-type": "SKU-CATEGORY",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": null,
                "parent-category-id": null,
                "ancestors-ids": [],
                "icon-key": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, CategoriaSkuDTO::class.java) }
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
                val entidadDTO = CategoriaSkuDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = CategoriaSkuDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_con_ancestros_se_construye_correctamente()
            {
                val entidadDTO = CategoriaSkuDTO(entidadNegocioSinNulosConAncestros)
                assertEquals(entidadDTOSinNulosConAncestros, entidadDTO)
            }
        }
    }
}