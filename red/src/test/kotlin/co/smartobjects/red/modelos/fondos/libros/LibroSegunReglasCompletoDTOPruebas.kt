package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroSegunReglasCompleto
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("LibroSegunReglasCompletoDTO")
internal class LibroSegunReglasCompletoCompletoDTOPruebas
{
    companion object
    {
        const val jsonDeserializacionPorDefecto = """
            {
                "name": "Por Defecto",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """

        const val jsonPorDefecto = """
            {
                "client-id": 0,
                "id": null,
                "name": "Por Defecto",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """

        @JvmField
        val entidadDTOPorDefecto =
                LibroSegunReglasCompletoDTO(
                        0,
                        null,
                        "Por Defecto",
                        LibroDeProhibicionesDTOPruebas.entidadDTOSinNulos,
                        listOf(ReglaDeIdUbicacionDTOPruebas.entidadDTOPorDefecto),
                        listOf(ReglaDeIdGrupoDeClientesDTOPruebas.entidadDTOPorDefecto),
                        listOf(ReglaDeIdPaqueteDTOPruebas.entidadDTOPorDefecto)
                                           )

        @JvmField
        val entidadNegocioPorDefecto =
                LibroSegunReglasCompleto(
                        0,
                        null,
                        "Por Defecto",
                        LibroDeProhibicionesDTOPruebas.entidadNegocioSinNulos,
                        mutableSetOf(ReglaDeIdUbicacionDTOPruebas.entidadNegocioPorDefecto),
                        mutableSetOf(ReglaDeIdGrupoDeClientesDTOPruebas.entidadNegocioPorDefecto),
                        mutableSetOf(ReglaDeIdPaqueteDTOPruebas.entidadNegocioPorDefecto)
                                        )


        const val jsonConNulos = """
            {
                "client-id": 1,
                "id": null,
                "name": "Con nulos",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """

        @JvmField
        val entidadDTOConNulos =
                LibroSegunReglasCompletoDTO(
                        1,
                        null,
                        "Con nulos",
                        LibroDeProhibicionesDTOPruebas.entidadDTOSinNulos,
                        listOf(ReglaDeIdUbicacionDTOPruebas.entidadDTOPorDefecto),
                        listOf(ReglaDeIdGrupoDeClientesDTOPruebas.entidadDTOPorDefecto),
                        listOf(ReglaDeIdPaqueteDTOPruebas.entidadDTOPorDefecto)
                                           )

        @JvmField
        val entidadNegocioConNulos =
                LibroSegunReglasCompleto(
                        1,
                        null,
                        "Con nulos",
                        LibroDeProhibicionesDTOPruebas.entidadNegocioSinNulos,
                        mutableSetOf(ReglaDeIdUbicacionDTOPruebas.entidadNegocioPorDefecto),
                        mutableSetOf(ReglaDeIdGrupoDeClientesDTOPruebas.entidadNegocioPorDefecto),
                        mutableSetOf(ReglaDeIdPaqueteDTOPruebas.entidadNegocioPorDefecto)
                                        )


        const val jsonSinNulos = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """

        @JvmField
        val entidadDTOSinNulos =
                LibroSegunReglasCompletoDTO(
                        1,
                        1,
                        "Sin nulos",
                        LibroDeProhibicionesDTOPruebas.entidadDTOSinNulos,
                        listOf(ReglaDeIdUbicacionDTOPruebas.entidadDTOPorDefecto),
                        listOf(ReglaDeIdGrupoDeClientesDTOPruebas.entidadDTOPorDefecto),
                        listOf(ReglaDeIdPaqueteDTOPruebas.entidadDTOPorDefecto)
                                           )

        @JvmField
        val entidadNegocioSinNulos =
                LibroSegunReglasCompleto(
                        1,
                        1,
                        "Sin nulos",
                        LibroDeProhibicionesDTOPruebas.entidadNegocioSinNulos,
                        mutableSetOf(ReglaDeIdUbicacionDTOPruebas.entidadNegocioPorDefecto),
                        mutableSetOf(ReglaDeIdGrupoDeClientesDTOPruebas.entidadNegocioPorDefecto),
                        mutableSetOf(ReglaDeIdPaqueteDTOPruebas.entidadNegocioPorDefecto)
                                        )
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, LibroSegunReglasCompletoDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, LibroSegunReglasCompletoDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, LibroSegunReglasCompletoDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }


        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_nombre()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": null,
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
            }

            @Test
            fun sin_libro()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
            }

            @Test
            fun con_libro_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "book": null,
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
            }

            @Test
            fun sin_reglas_de_ubicacion()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
            }

            @Test
            fun con_reglas_de_ubicacion_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": null,
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
            }

            @Test
            fun sin_reglas_de_grupos_de_clientes()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
            }

            @Test
            fun con_reglas_de_grupos_de_clientes_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": null,
                "rules-by-package-id": [${ReglaDeIdPaqueteDTOPruebas.jsonPorDefecto}]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
            }

            @Test
            fun sin_reglas_de_paquetes()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}]
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
            }

            @Test
            fun con_reglas_de_paquetes_null()
            {
                val json = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Sin nulos",
                "book": ${LibroDeProhibicionesDTOPruebas.jsonSinNulos},
                "rules-by-location-id": [${ReglaDeIdUbicacionDTOPruebas.jsonPorDefecto}],
                "rules-by-clients-group-id": [${ReglaDeIdGrupoDeClientesDTOPruebas.jsonPorDefecto}],
                "rules-by-package-id": null
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LibroSegunReglasCompletoDTO::class.java) }
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
                val entidadDTO = LibroSegunReglasCompletoDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = LibroSegunReglasCompletoDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = LibroSegunReglasCompletoDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}