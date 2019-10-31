package co.smartobjects.red.modelos.ubicaciones.consumibles

import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("ConsumibleEnPuntoDeVentaDTO")
internal class ConsumibleEnPuntoDeVentaDTOPruebas
{
    companion object
    {
        const val jsonDeserializacionPorDefecto = """
            {
                "location-id": 0,
                "consumable-id": 0,
                "consumable-external-code": "código externo consumible"
            }
            """

        const val jsonSerializacionPorDefectoEsperado = jsonDeserializacionPorDefecto

        @JvmField
        val entidadDTOPorDefecto = ConsumibleEnPuntoDeVentaDTO(0, 0, "código externo consumible")

        @JvmField
        val entidadNegocioPorDefecto = ConsumibleEnPuntoDeVenta(0, 0, "código externo consumible")

        private const val jsonConNulos = jsonDeserializacionPorDefecto

        private val entidadDTOConNulos = entidadDTOPorDefecto

        private val entidadNegocioConNulos = entidadNegocioPorDefecto


        private const val jsonSinNulos = """{
                "location-id": 10,
                "consumable-id": 20,
                "consumable-external-code": "código externo consumible"
        }"""

        private val entidadDTOSinNulos = ConsumibleEnPuntoDeVentaDTO(10, 20, "código externo consumible")

        private val entidadNegocioSinNulos = ConsumibleEnPuntoDeVenta(10, 20, "código externo consumible")

    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, ConsumibleEnPuntoDeVentaDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, ConsumibleEnPuntoDeVentaDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, ConsumibleEnPuntoDeVentaDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_id_location()
            {
                val json = """{
                "consumable-id": 20,
                "consumable-external-code": "código externo consumible"
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConsumibleEnPuntoDeVentaDTO::class.java) }
            }

            @Test
            fun con_id_location_null()
            {
                val json = """{
                "location-id": null,
                "consumable-id": 20,
                "consumable-external-code": "código externo consumible"
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConsumibleEnPuntoDeVentaDTO::class.java) }
            }

            @Test
            fun sin_id_consumable()
            {
                val json = """{
                "location-id": 10,
                "consumable-external-code": "código externo consumible"
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConsumibleEnPuntoDeVentaDTO::class.java) }
            }

            @Test
            fun con_id_consumable_null()
            {
                val json = """{
                "location-id": 10,
                "consumable-id": null,
                "consumable-external-code": "código externo consumible"
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConsumibleEnPuntoDeVentaDTO::class.java) }
            }

            @Test
            fun sin_codigo_externo_fondo()
            {
                val json = """{
                "location-id": 10,
                "consumable-id": 20
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConsumibleEnPuntoDeVentaDTO::class.java) }
            }

            @Test
            fun con_codigo_externo_fondo_null()
            {
                val json = """{
                "location-id": 10,
                "consumable-id": 20,
                "consumable-external-code": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConsumibleEnPuntoDeVentaDTO::class.java) }
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
                val entidadDTO = ConsumibleEnPuntoDeVentaDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = ConsumibleEnPuntoDeVentaDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = ConsumibleEnPuntoDeVentaDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}