package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.Prohibicion
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

@DisplayName("ProhibicionDePaqueteDTO")
internal class ProhibicionDePaqueteDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = """{ "prohibition-type": "PACKAGE", "prohibition-id": 1 }"""

        const val jsonPorDefectoEsperado = jsonPorDefecto

        @JvmField
        val entidadDTOPorDefecto = ProhibicionDePaqueteDTO(1)

        @JvmField
        val entidadNegocioPorDefecto = Prohibicion.DePaquete(1)
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, ProhibicionDePaqueteDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_tipo_de_prohibicion()
            {
                val json = """{
                "prohibition-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ProhibicionDePaqueteDTO::class.java) }
            }

            @Test
            fun con_tipo_de_prohibicion_null()
            {
                val json = """{
                "prohibition-type": null,
                "prohibition-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ProhibicionDePaqueteDTO::class.java) }
            }

            @Test
            fun sin_id_prohibicion()
            {
                val json = """{
                "prohibition-type": "PACKAGE"
            }"""
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ProhibicionDePaqueteDTO::class.java) }
            }

            @Test
            fun con_id_prohibicion_null()
            {
                val json = """{
                "prohibition-type": "PACKAGE",
                "prohibition-id": null
            }"""
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ProhibicionDePaqueteDTO::class.java) }
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
    fun el_tipo_es_PAQUETE()
    {
        assertEquals(IProhibicionDTO.Tipo.PAQUETE, entidadDTOPorDefecto.tipo)
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
                val entidadDTO = ProhibicionDePaqueteDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }
        }
    }
}