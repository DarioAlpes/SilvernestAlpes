package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.PrecioEnLibro
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.fondos.precios.jsonPrecioPorDefecto
import co.smartobjects.red.modelos.fondos.precios.precioDTOPorDefecto
import co.smartobjects.red.modelos.fondos.precios.precioNegocioPorDefecto
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("PrecioDeFondoDTOPruebas")
internal class PrecioDeFondoDTOPruebas
{
    companion object
    {
        const val jsonSinNulos = """{ "price": $jsonPrecioPorDefecto, "fund-id": 1 }"""

        @JvmField
        val entidadDTOSinNulos = LibroDePreciosDTO.PrecioDeFondoDTO(precioDTOPorDefecto, 1)

        @JvmField
        val entidadNegocioSinNulos = PrecioEnLibro(precioNegocioPorDefecto, 1)
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, LibroDePreciosDTO.PrecioDeFondoDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_precio()
            {
                val json = """
            {
                "fund-id": 1
            }
            """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO.PrecioDeFondoDTO::class.java) })
            }

            @Test
            fun con_precio_null()
            {
                val json = """
            {
                "price": null,
                "fund-id": 1
            }
            """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO.PrecioDeFondoDTO::class.java) })
            }

            @Test
            fun sin_id_fondo()
            {
                val json = """
            {
                "price": $jsonPrecioPorDefecto
            }
            """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO.PrecioDeFondoDTO::class.java) })
            }

            @Test
            fun con_id_fondo_null()
            {
                val json = """
            {
                "price": $jsonPrecioPorDefecto,
                "fund-id": null
            }
            """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO.PrecioDeFondoDTO::class.java) })
            }
        }
    }

    @Nested
    inner class Serializacion
    {
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
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = LibroDePreciosDTO.PrecioDeFondoDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}
