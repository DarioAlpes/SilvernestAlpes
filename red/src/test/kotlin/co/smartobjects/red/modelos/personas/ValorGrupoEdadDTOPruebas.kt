package co.smartobjects.red.modelos.personas

import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("ValorGrupoEdadDTO")
internal class ValorGrupoEdadDTOPruebas
{
    companion object
    {
        private const val jsonPorDefecto = """
            {
                "value": "Por Defecto"
            }
            """

        private const val jsonPorDefectoEsperado = """
            {
                "value": "Por Defecto",
                "minimum-age": null,
                "maximum-age": null
            }
            """

        private val paquetePorDefecto = ValorGrupoEdadDTO("Por Defecto", null, null)

        private val paqueteNegocioPorDefecto = ValorGrupoEdad("Por Defecto", null, null)

        private const val jsonConNulos = jsonPorDefectoEsperado

        private val paqueteConNulos = paquetePorDefecto

        private val paqueteNegocioConNulos = paqueteNegocioPorDefecto

        private const val jsonSinNulos = """
            {
                "value": "Por Defecto",
                "minimum-age": 0,
                "maximum-age": 20
            }
            """

        @JvmField
        val paqueteSinNulos = ValorGrupoEdadDTO("Por Defecto", 0, 20)

        @JvmField
        val paqueteNegocioSinNulos = ValorGrupoEdad("Por Defecto", 0, 20)
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, ValorGrupoEdadDTO::class.java)
            assertEquals(paquetePorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, ValorGrupoEdadDTO::class.java)
            assertEquals(paqueteConNulos, entidadDeserializada)
        }

        @Test
        fun json_son_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, ValorGrupoEdadDTO::class.java)
            assertEquals(paqueteSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_valor()
            {
                val jsonSinValor = """
            {
                "minimum-age": 0,
                "maximum-age": 20
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinValor, ValorGrupoEdadDTO::class.java) }
            }

            @Test
            fun con_valor_null()
            {
                val jsonConValorNull = """
            {
                "value": null,
                "minimum-age": 0,
                "maximum-age": 20
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConValorNull, ValorGrupoEdadDTO::class.java) }
            }

        }

        @[Nested DisplayName("no lanza excepción con un json")]
        inner class NoLanzaExcepcion
        {
            @Test
            fun sin_edad_minima()
            {
                val jsonSinNombre = """
            {
                "value": "Por Defecto",
                "maximum-age": 20
            }
            """
                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNombre, ValorGrupoEdadDTO::class.java)
            }

            @Test
            fun sin_edad_maxima()
            {
                val jsonSinNombre = """
            {
                "value": "Por Defecto",
                "minimum-age": 0
            }
            """
                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNombre, ValorGrupoEdadDTO::class.java)
            }
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun de_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(paquetePorDefecto)

            JSONAssert.assertEquals(jsonPorDefectoEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun con_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(paqueteConNulos)

            JSONAssert.assertEquals(jsonConNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun sin_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(paqueteSinNulos)

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
                val entidadDeNegocio = paquetePorDefecto.aEntidadDeNegocio()
                assertEquals(paqueteNegocioPorDefecto, entidadDeNegocio)
            }

            @Test
            fun con_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = paqueteConNulos.aEntidadDeNegocio()
                assertEquals(paqueteNegocioConNulos, entidadDeNegocio)
            }

            @Test
            fun sin_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = paqueteSinNulos.aEntidadDeNegocio()
                assertEquals(paqueteNegocioSinNulos, entidadDeNegocio)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = ValorGrupoEdadDTO(paqueteNegocioPorDefecto)
                assertEquals(paquetePorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = ValorGrupoEdadDTO(paqueteNegocioConNulos)
                assertEquals(paqueteConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = ValorGrupoEdadDTO(paqueteNegocioSinNulos)
                assertEquals(paqueteSinNulos, entidadDTO)
            }
        }
    }
}