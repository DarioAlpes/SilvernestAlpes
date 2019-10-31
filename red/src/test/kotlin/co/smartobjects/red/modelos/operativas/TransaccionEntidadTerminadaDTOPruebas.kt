package co.smartobjects.red.modelos.operativas

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("TransaccionEntidadTerminadaDTO")
class TransaccionEntidadTerminadaDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacion = """{"committed": true}"""

        private const val jsonSerializacionEsperado = """{"committed": true}"""

        private val entidadDTOPorDefecto = TransaccionEntidadTerminadaDTO<EntidadTransaccional<*>>(true)
    }

    @Test
    fun se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = TransaccionEntidadTerminadaDTO<EntidadTransaccional<*>>(true)
        val listaEsperada = setOf(EntidadTransaccional.CampoCreacionTerminada<EntidadTransaccional<*>>(true))

        assertEquals(listaEsperada, dto.aConjuntoCamposModificables())
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacion, TransaccionEntidadTerminadaDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_committed()
            {
                val jsonSinValor = "{}"
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinValor, TransaccionEntidadTerminadaDTO::class.java) })
            }

            @Test
            fun con_committed_null()
            {
                val jsonConValorNull = """{"committed": null}"""
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConValorNull, TransaccionEntidadTerminadaDTO::class.java) })
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

            JSONAssert.assertEquals(jsonSerializacionEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }
    }
}