package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals


@DisplayName("NombreGrupoClientesDTO")
internal class NombreGrupoClientesDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacionPorDefecto = """{"name": "Nombre"}"""

        private const val jsonSerializacionPorDefectoEsperado = """{"name": "Nombre"}"""

        private val entidadDTOPorDefecto = NombreGrupoClientesDTO("Nombre")
    }

    @Test
    fun se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = NombreGrupoClientesDTO("Nombre")
        val listaEsperada = setOf(GrupoClientes.CampoNombre("Nombre"))

        assertEquals(listaEsperada, dto.aConjuntoCamposModificables())
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, NombreGrupoClientesDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_valor()
            {
                val jsonSinValor = "{}"
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinValor, NombreGrupoClientesDTO::class.java) }
            }

            @Test
            fun con_valor_null()
            {
                val jsonConValorNull = """{"name": null}"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConValorNull, NombreGrupoClientesDTO::class.java) }
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
    }
}