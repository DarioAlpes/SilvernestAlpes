package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.ReglaDeIdUbicacion
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("ReglaDeIdUbicacionDTO")
internal class ReglaDeIdUbicacionDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = """{ "rule-type": "LOCATION_ID", "restricted-id": 123 }"""

        const val jsonPorDefectoEsperado = jsonPorDefecto

        @JvmField
        val entidadDTOPorDefecto = ReglaDeIdUbicacionDTO(123)

        @JvmField
        val entidadNegocioPorDefecto = ReglaDeIdUbicacion(123)
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, ReglaDeIdUbicacionDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_tipo_de_regla()
            {
                val json = """{
                "restricted-id": 3
            }"""
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReglaDeIdUbicacionDTO::class.java) }
            }

            @Test
            fun con_tipo_de_regla_null()
            {
                val json = """{
                "rule-type": null,
                "restricted-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReglaDeIdUbicacionDTO::class.java) }
            }

            @Test
            fun sin_id_restriccion()
            {
                val json = """{
                "rule-type": "LOCATION_ID"
            }"""
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReglaDeIdUbicacionDTO::class.java) }
            }

            @Test
            fun con_id_restriccion_null()
            {
                val json = """{
                "rule-type": "LOCATION_ID",
                "restricted-id": null
            }"""
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReglaDeIdUbicacionDTO::class.java) }
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
    }

    @Test
    fun el_tipo_es_ID_UBICACION()
    {
        assertEquals(IReglaDTO.Tipo.ID_UBICACION, entidadDTOPorDefecto.tipo)
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
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = ReglaDeIdUbicacionDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }
        }
    }
}