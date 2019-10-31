package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
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

@DisplayName("LibroDeProhibicionesDTO")
internal class LibroDeProhibicionesDTOPruebas
{
    companion object
    {
        const val jsonDeserializacionPorDefecto = """
            {
                "book-type": "PROHIBITIONS",
                "name": "Por Defecto",
                "funds-prohibitions": [
                        ${ProhibicionDeFondoDTOPruebas.jsonPorDefecto}
                    ],
                "packages-prohibitions": [
                        ${ProhibicionDePaqueteDTOPruebas.jsonPorDefecto}
                    ]
            }
            """

        const val jsonPorDefecto = """
            {
                "book-type": "PROHIBITIONS",
                "client-id": 0,
                "id": null,
                "name": "Por Defecto",
                "funds-prohibitions": [
                        ${ProhibicionDeFondoDTOPruebas.jsonPorDefecto}
                    ],
                "packages-prohibitions": [
                        ${ProhibicionDePaqueteDTOPruebas.jsonPorDefecto}
                    ]
            }
            """

        @JvmField
        val entidadDTOPorDefecto =
                LibroDeProhibicionesDTO(
                        0,
                        null,
                        "Por Defecto",
                        listOf(ProhibicionDeFondoDTOPruebas.entidadDTOPorDefecto),
                        listOf(ProhibicionDePaqueteDTOPruebas.entidadDTOPorDefecto)
                                       )

        @JvmField
        val entidadNegocioPorDefecto =
                LibroDeProhibiciones(
                        0,
                        null,
                        "Por Defecto",
                        setOf(ProhibicionDeFondoDTOPruebas.entidadNegocioPorDefecto),
                        setOf(ProhibicionDePaqueteDTOPruebas.entidadNegocioPorDefecto)
                                    )

        const val jsonConNulos = """
            {
                "book-type": "PROHIBITIONS",
                "client-id": 1,
                "id": null,
                "name": "Con nulos",
                "funds-prohibitions": [
                        ${ProhibicionDeFondoDTOPruebas.jsonPorDefecto}
                    ],
                "packages-prohibitions": [
                        ${ProhibicionDePaqueteDTOPruebas.jsonPorDefecto}
                    ]
            }
            """

        @JvmField
        val entidadDTOConNulos =
                LibroDeProhibicionesDTO(
                        1,
                        null,
                        "Con nulos",
                        listOf(ProhibicionDeFondoDTOPruebas.entidadDTOPorDefecto),
                        listOf(ProhibicionDePaqueteDTOPruebas.entidadDTOPorDefecto)
                                       )

        @JvmField
        val entidadNegocioConNulos =
                LibroDeProhibiciones(
                        1,
                        null,
                        "Con nulos",
                        setOf(ProhibicionDeFondoDTOPruebas.entidadNegocioPorDefecto),
                        setOf(ProhibicionDePaqueteDTOPruebas.entidadNegocioPorDefecto)
                                    )

        const val jsonSinNulos = """
            {
                "book-type": "PROHIBITIONS",
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "funds-prohibitions": [
                        ${ProhibicionDeFondoDTOPruebas.jsonPorDefecto}
                    ],
                "packages-prohibitions": [
                        ${ProhibicionDePaqueteDTOPruebas.jsonPorDefecto}
                    ]
            }
            """

        @JvmField
        val entidadDTOSinNulos =
                LibroDeProhibicionesDTO(
                        1,
                        1,
                        "Sin nulos",
                        listOf(ProhibicionDeFondoDTOPruebas.entidadDTOPorDefecto),
                        listOf(ProhibicionDePaqueteDTOPruebas.entidadDTOPorDefecto)
                                       )

        @JvmField
        val entidadNegocioSinNulos =
                LibroDeProhibiciones(
                        1,
                        1,
                        "Sin nulos",
                        setOf(ProhibicionDeFondoDTOPruebas.entidadNegocioPorDefecto),
                        setOf(ProhibicionDePaqueteDTOPruebas.entidadNegocioPorDefecto)
                                    )
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, LibroDeProhibicionesDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, LibroDeProhibicionesDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, LibroDeProhibicionesDTO::class.java)
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
                "funds-prohibitions": [
                        { "prohibition-type": "FUND", "prohibition-id": 1 }
                    ],
                "packages-prohibitions": [
                        { "prohibition-type": "PACKAGE", "prohibition-id": 2 }
                    ]
            }
            """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDeProhibicionesDTO::class.java) }
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
                "funds-prohibitions": [
                        { "prohibition-type": "FUND", "prohibition-id": 1 }
                    ],
                "packages-prohibitions": [
                        { "prohibition-type": "PACKAGE", "prohibition-id": 2 }
                    ]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDeProhibicionesDTO::class.java) }
            }

            @Test
            fun sin_nombre()
            {
                val json = """
            {
                "book-type": "PROHIBITIONS",
                "client-id": 1,
                "id": 1,
                "funds-prohibitions": [
                        { "prohibition-type": "FUND", "prohibition-id": 1 }
                    ],
                "packages-prohibitions": [
                        { "prohibition-type": "PACKAGE", "prohibition-id": 2 }
                    ]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDeProhibicionesDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val json = """
            {
                "book-type": "PROHIBITIONS",
                "client-id": 1,
                "id": 1,
                "name": null,
                "funds-prohibitions": [
                        { "prohibition-type": "FUND", "prohibition-id": 1 }
                    ],
                "packages-prohibitions": [
                        { "prohibition-type": "PACKAGE", "prohibition-id": 2 }
                    ]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDeProhibicionesDTO::class.java) }
            }

            @Test
            fun sin_prohibiciones_de_fondos()
            {
                val json = """
            {
                "book-type": "PROHIBITIONS",
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "packages-prohibitions": [
                        { "prohibition-type": "PACKAGE", "prohibition-id": 2 }
                    ]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDeProhibicionesDTO::class.java) }
            }

            @Test
            fun con_prohibiciones_de_fondos_null()
            {
                val json = """
            {
                "book-type": "PROHIBITIONS",
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "funds-prohibitions": null,
                "packages-prohibitions": [
                        { "prohibition-type": "PACKAGE", "prohibition-id": 2 }
                    ]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDeProhibicionesDTO::class.java) }
            }

            @Test
            fun sin_prohibiciones_de_paquetes()
            {
                val json = """
            {
                "book-type": "PROHIBITIONS",
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "funds-prohibitions": [
                        { "prohibition-type": "FUND", "prohibition-id": 1 }
                    ]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDeProhibicionesDTO::class.java) }
            }

            @Test
            fun con_prohibiciones_de_paquetes_null()
            {
                val json = """
            {
                "book-type": "PROHIBITIONS",
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "funds-prohibitions": [
                        { "prohibition-type": "FUND", "prohibition-id": 1 }
                    ],
                "packages-prohibitions": null
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroDeProhibicionesDTO::class.java) }
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
    fun el_tipo_es_PROHIBITIONS()
    {
        assertEquals(LibroDTO.Tipo.PROHIBICIONES, entidadDTOSinNulos.tipo)
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
                val entidadDTO = LibroDeProhibicionesDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = LibroDeProhibicionesDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = LibroDeProhibicionesDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}