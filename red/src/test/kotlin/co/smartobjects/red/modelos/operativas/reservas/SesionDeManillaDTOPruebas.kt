package co.smartobjects.red.modelos.operativas.reservas

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals


@DisplayName("SesionDeManillaDTO")
internal class SesionDeManillaDTOPruebas
{
    companion object
    {
        private const val FECHA_ACTIVACION_DEFECTO_STR = "2000-01-02T03:04:05-05:00[UTC-05:00]"
        private val FECHA_ACTIVACION_DEFECTO = ZonedDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)
        const val FECHA_DESACTIVACION_DEFECTO_STR = "2001-02-03T03:04:05-05:00[UTC-05:00]"
        val FECHA_DESACTIVACION_DEFECTO = ZonedDateTime.of(LocalDate.of(2001, 2, 3), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)!!
        const val TAG_UUID_DEFECTO_STR = "QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVo="
        @JvmField
        val TAG_UUID_DEFECTO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toByteArray()

        const val jsonDeserializacionPorDefecto = """
            {
                "person-id": 1,
                "credits-ids-to-encode": [1,2,3]
            }
            """

        const val jsonPorDefecto = """
            {
                "client-id": 0,
                "id": null,
                "person-id": 1,
                "tag-uuid": null,
                "activation-date": null,
                "deactivation-date": null,
                "credits-ids-to-encode":  [1,2,3]
            }
            """

        @JvmField
        val entidadDTOPorDefecto =
                SesionDeManillaDTO(
                        0,
                        null,
                        1,
                        null,
                        null,
                        null,
                        listOf(1, 2, 3)
                                  )

        @JvmField
        val entidadNegocioPorDefecto =
                SesionDeManilla(
                        0,
                        null,
                        1,
                        null,
                        null,
                        null,
                        setOf<Long>(1, 2, 3)
                               )

        const val jsonConNulos = """
            {
                "client-id": 0,
                "id": null,
                "person-id": 1,
                "tag-uuid": null,
                "activation-date": null,
                "deactivation-date": null,
                "credits-ids-to-encode":  [1,2,3]
            }
            """

        @JvmField
        val entidadDTOConNulos =
                SesionDeManillaDTO(
                        0,
                        null,
                        1,
                        null,
                        null,
                        null,
                        listOf(1, 2, 3)
                                  )

        @JvmField
        val entidadNegocioConNulos =
                SesionDeManilla(
                        0,
                        null,
                        1,
                        null,
                        null,
                        null,
                        setOf<Long>(1, 2, 3)
                               )

        const val jsonSinNulos = """
            {
                "client-id": 0,
                "id": 1,
                "person-id": 1,
                "tag-uuid": "$TAG_UUID_DEFECTO_STR",
                "activation-date": "$FECHA_ACTIVACION_DEFECTO_STR",
                "deactivation-date": "$FECHA_DESACTIVACION_DEFECTO_STR",
                "credits-ids-to-encode":  [1,2,3]
            }
            """

        @JvmField
        val entidadDTOSinNulos =
                SesionDeManillaDTO(
                        0,
                        1,
                        1,
                        TAG_UUID_DEFECTO,
                        FECHA_ACTIVACION_DEFECTO,
                        FECHA_DESACTIVACION_DEFECTO,
                        listOf(1, 2, 3)
                                  )

        @JvmField
        val entidadNegocioSinNulos =
                SesionDeManilla(
                        0,
                        1,
                        1,
                        TAG_UUID_DEFECTO,
                        FECHA_ACTIVACION_DEFECTO,
                        FECHA_DESACTIVACION_DEFECTO,
                        setOf<Long>(1, 2, 3)
                               )
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, SesionDeManillaDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, SesionDeManillaDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, SesionDeManillaDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_id_persona()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": 1,
                        "tag-uuid": "$TAG_UUID_DEFECTO_STR",
                        "activation-date": "$FECHA_ACTIVACION_DEFECTO_STR",
                        "deactivation-date": "$FECHA_DESACTIVACION_DEFECTO_STR",
                        "credits-ids-to-encode": [1,2,3]
                    }
                    """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java) }
            }

            @Test
            fun con_id_persona_null()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": 1,
                        "person-id": null,
                        "tag-uuid": "$TAG_UUID_DEFECTO_STR",
                        "activation-date": "$FECHA_ACTIVACION_DEFECTO_STR",
                        "deactivation-date": "$FECHA_DESACTIVACION_DEFECTO_STR",
                        "credits-ids-to-encode": [1,2,3]
                    }
                    """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java) }
            }

            @Test
            fun sin_creditos_a_codificar()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": 1,
                        "person-id": 1,
                        "tag-uuid": "$TAG_UUID_DEFECTO_STR",
                        "activation-date": "$FECHA_ACTIVACION_DEFECTO_STR",
                        "deactivation-date": "$FECHA_DESACTIVACION_DEFECTO_STR"
                    }
                    """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java) }
            }

            @Test
            fun con_creditos_a_codificar_null()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": 1,
                        "person-id": 1,
                        "tag-uuid": "$TAG_UUID_DEFECTO_STR",
                        "activation-date": "$FECHA_ACTIVACION_DEFECTO_STR",
                        "deactivation-date": "$FECHA_DESACTIVACION_DEFECTO_STR",
                        "credits-ids-to-encode": null
                    }
                    """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java) }
            }
        }

        @[Nested DisplayName("no lanza excepción con un json")]
        inner class NoLanzaExcepcion
        {
            @Test
            fun sin_tag_uuid()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": 1,
                        "person-id": 1,
                        "activation-date": "$FECHA_ACTIVACION_DEFECTO_STR",
                        "deactivation-date": "$FECHA_DESACTIVACION_DEFECTO_STR",
                        "credits-ids-to-encode": [1,2,3]
                    }
                    """
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java)
            }

            @Test
            fun con_tag_uuid_null()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": null,
                        "person-id": 1,
                        "tag-uuid": null,
                        "activation-date": "$FECHA_ACTIVACION_DEFECTO_STR",
                        "deactivation-date": "$FECHA_DESACTIVACION_DEFECTO_STR",
                        "credits-ids-to-encode":  [1,2,3]
                    }
                    """
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java)
            }

            @Test
            fun sin_fecha_de_activacion()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": 1,
                        "person-id": 1,
                        "tag-uuid": "$TAG_UUID_DEFECTO_STR",
                        "deactivation-date": "$FECHA_DESACTIVACION_DEFECTO_STR",
                        "credits-ids-to-encode": [1,2,3]
                    }
                    """
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java)
            }

            @Test
            fun con_fecha_de_activacion_null()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": null,
                        "person-id": 1,
                        "tag-uuid": "$TAG_UUID_DEFECTO_STR",
                        "activation-date": null,
                        "deactivation-date": "$FECHA_DESACTIVACION_DEFECTO_STR",
                        "credits-ids-to-encode":  [1,2,3]
                    }
                    """
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java)
            }

            @Test
            fun sin_fecha_de_desactivacion()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": 1,
                        "person-id": 1,
                        "tag-uuid": "$TAG_UUID_DEFECTO_STR",
                        "activation-date": "$FECHA_ACTIVACION_DEFECTO_STR",
                        "credits-ids-to-encode": [1,2,3]
                    }
                    """
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java)
            }

            @Test
            fun con_fecha_de_desactivacion_null()
            {
                val json = """
                    {
                        "client-id": 0,
                        "id": null,
                        "person-id": 1,
                        "tag-uuid": "$TAG_UUID_DEFECTO_STR",
                        "activation-date": "$FECHA_ACTIVACION_DEFECTO_STR",
                        "deactivation-date": null,
                        "credits-ids-to-encode":  [1,2,3]
                    }
                    """
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, SesionDeManillaDTO::class.java)
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
                val entidadDTO = SesionDeManillaDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = SesionDeManillaDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = SesionDeManillaDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}