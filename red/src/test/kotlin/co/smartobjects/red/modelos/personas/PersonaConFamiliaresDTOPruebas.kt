package co.smartobjects.red.modelos.personas

import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals


@DisplayName("PersonaConFamiliaresDTO")
internal class PersonaConFamiliaresDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacionPorDefecto = """
            {
                "person": $jsonPersonaPorDefecto,
                "family": []
            }
            """

        private const val jsonSerializacionPorDefectoEsperado = """
            {
                "person": $jsonPersonaPorDefectoEsperado,
                "family": []
            }
            """

        private val entidadDTOPorDefecto = PersonaConFamiliaresDTO(personaDTOPorDefecto, listOf())

        private val entidadNegocioPorDefecto = PersonaConFamiliares(personaNegocioPorDefecto, setOf())

        private const val jsonConNulos = """
            {
                "person": $jsonPersonaConNulos,
                "family": []
            }
            """

        private val entidadDTOConNulos = PersonaConFamiliaresDTO(personaDTOConNulos, listOf())

        private val entidadNegocioConNulos = PersonaConFamiliares(personaNegocioConNulos, setOf())


        private const val jsonSinNulos = """
            {
                "person": $jsonPersonaSinNulos,
                "family": [$jsonPersonaPorDefectoEsperado]
            }
            """

        private val entidadDTOSinNulos = PersonaConFamiliaresDTO(personaDTOSinNulos, listOf(personaDTOPorDefecto))

        private val entidadNegocioSinNulos = PersonaConFamiliares(personaNegocioSinNulos, setOf(personaNegocioPorDefecto))

    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, PersonaConFamiliaresDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, PersonaConFamiliaresDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, PersonaConFamiliaresDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_persona()
            {
                val jsonSinPersona = """
                {
                    "family": []
                }
                    """
                assertThrows<JsonMappingException> {
                    ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinPersona, PersonaConFamiliaresDTO::class.java)
                }
            }

            @Test
            fun con_persona_null()
            {
                val jsonConPersonaNull = """
                {
                    "person": null,
                    "family": []
                }
                """
                assertThrows<JsonMappingException> {
                    ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConPersonaNull, PersonaConFamiliaresDTO::class.java)
                }
            }

            @Test
            fun sin_familia()
            {
                val jsonSinFamilia = """
                {
                    "person": $jsonPersonaSinNulos
                }
                    """
                assertThrows<JsonMappingException> {
                    ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinFamilia, PersonaConFamiliaresDTO::class.java)
                }
            }

            @Test
            fun con_familia_null()
            {
                val jsonConFamiliaNull = """
                {
                    "person": $jsonPersonaSinNulos,
                    "family": null
                }
                """
                assertThrows<JsonMappingException> {
                    ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConFamiliaNull, PersonaConFamiliaresDTO::class.java)
                }
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
                val entidadDTO = PersonaConFamiliaresDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = PersonaConFamiliaresDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = PersonaConFamiliaresDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}