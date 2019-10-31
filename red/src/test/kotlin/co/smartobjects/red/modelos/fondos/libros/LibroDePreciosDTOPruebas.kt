package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.PrecioEnLibro
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.modelos.fondos.precios.*
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("LibroDePreciosDTO")
internal class LibroDePreciosDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacionPorDefecto = """
            {
                "book-type": "PRICES",
                "name": "Por Defecto",
                "prices": [
                        {
                            "price": $jsonPrecioPorDefecto,
                            "fund-id": 1
                        },
                        {
                            "price": $jsonPrecioSinNulos,
                            "fund-id": 2
                        }
                    ]
            }
            """

        private const val jsonPorDefecto = """
            {
                "book-type": "PRICES",
                "client-id": 0,
                "id": null,
                "name": "Por Defecto",
                "prices": [
                        {
                            "price": $jsonPrecioPorDefecto,
                            "fund-id": 1
                        },
                        {
                            "price": $jsonPrecioSinNulos,
                            "fund-id": 2
                        }
                    ]
            }
            """

        private val entidadDTOPorDefecto =
                LibroDePreciosDTO(
                        0,
                        null,
                        "Por Defecto",
                        listOf(
                                LibroDePreciosDTO.PrecioDeFondoDTO(precioDTOPorDefecto, 1),
                                LibroDePreciosDTO.PrecioDeFondoDTO(precioDTOSinNulos, 2)
                              )
                                 )

        private val entidadNegocioPorDefecto =
                LibroDePrecios(
                        0,
                        null,
                        "Por Defecto",
                        setOf(
                                PrecioEnLibro(precioNegocioPorDefecto, 1),
                                PrecioEnLibro(precioNegocioSinNulos, 2)
                             )
                              )

        private const val jsonConNulos = """
            {
                "book-type": "PRICES",
                "client-id": 1,
                "id": null,
                "name": "Con nulos",
                "prices": [
                        {
                            "price": $jsonPrecioPorDefecto,
                            "fund-id": 1
                        },
                        {
                            "price": $jsonPrecioSinNulos,
                            "fund-id": 2
                        }
                    ]
            }
            """

        private val entidadDTOConNulos =
                LibroDePreciosDTO(
                        1,
                        null,
                        "Con nulos",
                        listOf(
                                LibroDePreciosDTO.PrecioDeFondoDTO(precioDTOPorDefecto, 1),
                                LibroDePreciosDTO.PrecioDeFondoDTO(precioDTOSinNulos, 2)
                              )
                                 )

        private val entidadNegocioConNulos =
                LibroDePrecios(
                        1,
                        null,
                        "Con nulos",
                        setOf(
                                PrecioEnLibro(precioNegocioPorDefecto, 1),
                                PrecioEnLibro(precioNegocioSinNulos, 2)
                             )
                              )

        private const val jsonSinNulos = """
            {
                "book-type": "PRICES",
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "prices": [
                        {
                            "price": $jsonPrecioPorDefecto,
                            "fund-id": 1
                        },
                        {
                            "price": $jsonPrecioSinNulos,
                            "fund-id": 2
                        }
                    ]
            }
            """

        private val entidadDTOSinNulos =
                LibroDePreciosDTO(
                        1,
                        1,
                        "Sin nulos",
                        listOf(
                                LibroDePreciosDTO.PrecioDeFondoDTO(precioDTOPorDefecto, 1),
                                LibroDePreciosDTO.PrecioDeFondoDTO(precioDTOSinNulos, 2)
                              )
                                 )

        private val entidadNegocioSinNulos =
                LibroDePrecios(
                        1,
                        1,
                        "Sin nulos",
                        setOf(
                                PrecioEnLibro(precioNegocioPorDefecto, 1),
                                PrecioEnLibro(precioNegocioSinNulos, 2)
                             )
                              )
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, LibroDePreciosDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, LibroDePreciosDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, LibroDePreciosDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_tipo()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "prices": [
                        {
                            "price": $jsonPrecioPorDefecto,
                            "fund-id": 1
                        },
                        {
                            "price": $jsonPrecioSinNulos,
                            "fund-id": 2
                        }
                    ]
            }
            """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO::class.java) }
            }

            @Test
            fun con_tipo_null()
            {
                val json = """
            {
                "book-type": null,
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "prices": [
                        {
                            "price": $jsonPrecioPorDefecto,
                            "fund-id": 1
                        },
                        {
                            "price": $jsonPrecioSinNulos,
                            "fund-id": 2
                        }
                    ]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO::class.java) }
            }

            @Test
            fun sin_nombre()
            {
                val json = """
            {
                "book-type": "PRICES",
                "client-id": 1,
                "id": 1,
                "prices": [
                        {
                            "price": $jsonPrecioPorDefecto,
                            "fund-id": 1
                        },
                        {
                            "price": $jsonPrecioSinNulos,
                            "fund-id": 2
                        }
                    ]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val json = """
            {
                "book-type": "PRICES",
                "client-id": 1,
                "id": 1,
                "name": null,
                "prices": [
                        {
                            "price": $jsonPrecioPorDefecto,
                            "fund-id": 1
                        },
                        {
                            "price": $jsonPrecioSinNulos,
                            "fund-id": 2
                        }
                    ]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO::class.java) }
            }

            @Test
            fun sin_precios()
            {
                val json = """
            {
                "book-type": "PRICES",
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO::class.java) }
            }

            @Test
            fun con_precios_null()
            {
                val json = """
            {
                "book-type": "PRICES",
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "prices": null
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDePreciosDTO::class.java) }
            }
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun con_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOPorDefecto)

            JSONAssert.assertEquals(jsonPorDefecto, entidadSerializada, JSONCompareMode.STRICT)
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

    @Test
    fun el_tipo_es_PRECIOS()
    {
        assertEquals(LibroDTO.Tipo.PRECIOS, entidadDTOSinNulos.tipo)
    }

    @Nested
    inner class Conversion
    {
        @Nested
        inner class AEntidadDeNegocio
        {
            @Test
            fun con_por_defecto_se_transforma_correctamente()
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
            fun con_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = LibroDePreciosDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = LibroDePreciosDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = LibroDePreciosDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}