package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("FondoDisponibleParaLaVentaDTO")
class FondoDisponibleParaLaVentaDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacion = """{"available-for-sale": true}"""

        private const val jsonSerializacionEsperado = """{"available-for-sale": true}"""

        private val entidadDTOPorDefecto = FondoDisponibleParaLaVentaDTO<Dinero>(true)
    }

    @Test
    fun se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = FondoDisponibleParaLaVentaDTO<Dinero>(true)
        val listaEsperada = setOf(Fondo.CampoDisponibleParaLaVenta<Dinero>(true))

        assertEquals(listaEsperada, dto.aConjuntoCamposModificables())
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacion, FondoDisponibleParaLaVentaDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_disponible_para_la_venta()
            {
                val jsonSinValor = "{}"
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinValor, FondoDisponibleParaLaVentaDTO::class.java) }
            }

            @Test
            fun con_disponible_para_la_venta_null()
            {
                val jsonConValorNull = """{"available-for-sale": null}"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConValorNull, FondoDisponibleParaLaVentaDTO::class.java) }
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