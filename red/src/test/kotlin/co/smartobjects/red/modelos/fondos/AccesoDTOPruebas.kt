package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Acceso
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

@DisplayName("AccesoDTO")
internal class AccesoDTOPruebas
{
    companion object
    {
        const val jsonDeserializacionPorDefecto = """
            {
                "fund-type": "ACCESS",
                "name": "Por Defecto",
                "available-for-sale": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "location-id": 0
            }
            """

        const val jsonSerializacionPorDefectoEsperado = """
            {
                "fund-type": "ACCESS",
                "client-id": 0,
                "id": null,
                "name": "Por Defecto",
                "available-for-sale": false,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "location-id": 0
            }
            """

        @JvmField
        val entidadDTOPorDefecto = AccesoDTO(
                0,
                null,
                "Por Defecto",
                false,
                false,
                false,
                precioDTOSinNulos,
                "El código externo",
                0)

        @JvmField
        val entidadNegocioPorDefecto = Acceso(
                0,
                null,
                "Por Defecto",
                false,
                false,
                false,
                precioNegocioSinNulos,
                "El código externo",
                0)

        private const val jsonConNulos = """{
            "fund-type": "ACCESS",
            "client-id": 1,
            "id": 2,
            "name": "Entidad prueba",
            "available-for-sale": true,
            "once-per-session": false,
            "unlimited": false,
            "default-price": $jsonPrecioSinNulos,
            "external-code": "El código externo",
            "location-id": 0
        }"""

        private val entidadDTOConNulos = AccesoDTO(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioDTOSinNulos,
                "El código externo",
                0)

        private val entidadNegocioConNulos = Acceso(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioNegocioSinNulos,
                "El código externo",
                0)


        const val jsonSinNulos = """{
            "fund-type": "ACCESS",
            "client-id": 1,
            "id": 2,
            "name": "Entidad prueba",
            "available-for-sale": true,
            "once-per-session": false,
            "unlimited": false,
            "default-price": $jsonPrecioSinNulos,
            "external-code": "El código externo",
            "location-id": 3
        }"""

        val entidadDTOSinNulos = AccesoDTO(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioDTOSinNulos,
                "El código externo",
                3)

        val entidadNegocioSinNulos = Acceso(
                1,
                2,
                "Entidad prueba",
                true,
                false,
                false,
                precioNegocioSinNulos,
                "El código externo",
                3)

    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, AccesoDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, AccesoDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, AccesoDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_nombre()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": null,
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun sin_available_for_sale()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun con_available_for_sale_null()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": null,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun sin_unlimited()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun con_unlimited_null()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": null,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun sin_id_ubicacion()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "unlimited": false
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun con_id_ubicacion_null()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": "El código externo",
                "location-id": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun sin_precio_por_defecto()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "external-code": "El código externo",
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun con_precio_por_defecto_null()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": null,
                "external-code": "El código externo",
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun sin_codigo_externo()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
            }

            @Test
            fun con_codigo_externo_null()
            {
                val json = """{
                "fund-type": "ACCESS",
                "client-id": 1,
                "id": 2,
                "name": "Entidad prueba",
                "available-for-sale": true,
                "once-per-session": false,
                "unlimited": false,
                "default-price": $jsonPrecioSinNulos,
                "external-code": null,
                "location-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, AccesoDTO::class.java) }
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

            JSONAssert.assertEquals(jsonSerializacionPorDefectoEsperado, entidadSerializada, JSONCompareMode.STRICT)
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
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = AccesoDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = AccesoDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = AccesoDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}