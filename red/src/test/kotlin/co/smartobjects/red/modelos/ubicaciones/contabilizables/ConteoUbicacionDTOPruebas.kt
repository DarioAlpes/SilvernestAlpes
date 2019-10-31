package co.smartobjects.red.modelos.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
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


@DisplayName("ConteoUbicacionDTO")
internal class ConteoUbicacionDTOPruebas
{
    companion object
    {
        private const val FECHA_REALIZACION_DEFECTO_STR = "2000-01-02T03:04:05-05:00[UTC-05:00]"
        private val FECHA_REALIZACION_DEFECTO = ZonedDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)

        const val jsonDeserializacionPorDefecto = """
            {
                "location-id": 1,
                "tag-session-id": 2,
                "location-count-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
            }
            """

        const val jsonPorDefecto = """
            {
                "client-id": 0,
                "location-id": 1,
                "tag-session-id": 2,
                "location-count-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
            }
            """

        private val entidadDTOPorDefecto = ConteoUbicacionDTO(0, 1, 2, FECHA_REALIZACION_DEFECTO)

        private val entidadNegocioPorDefecto = ConteoUbicacion(0, 1, 2, FECHA_REALIZACION_DEFECTO)


        const val jsonSinNulos = """
            {
                "client-id": 1,
                "location-id": 1,
                "tag-session-id": 2,
                "location-count-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
            }
            """

        private val entidadDTOSinNulos = ConteoUbicacionDTO(1, 1, 2, FECHA_REALIZACION_DEFECTO)

        private val entidadNegocioSinNulos = ConteoUbicacion(1, 1, 2, FECHA_REALIZACION_DEFECTO)
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(ConteoUbicacionDTOPruebas.jsonDeserializacionPorDefecto, ConteoUbicacionDTO::class.java)
            assertEquals(ConteoUbicacionDTOPruebas.entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(ConteoUbicacionDTOPruebas.jsonSinNulos, ConteoUbicacionDTO::class.java)
            assertEquals(ConteoUbicacionDTOPruebas.entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_id_ubicacion()
            {
                val json = """
                    {
                        "client-id": 1,
                        "tag-session-id": 2,
                        "location-count-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
                    }
                    """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConteoUbicacionDTO::class.java) }
            }

            @Test
            fun con_id_ubicacion_null()
            {
                val json = """
                    {
                        "client-id": 1,
                        "location-id": null,
                        "tag-session-id": 2,
                        "location-count-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
                    }
                    """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConteoUbicacionDTO::class.java) }
            }

            @Test
            fun sin_id_de_sesion_de_manilla()
            {
                val json = """
                    {
                        "client-id": 1,
                        "location-id": 1,
                        "location-count-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
                    }
                    """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConteoUbicacionDTO::class.java) }
            }

            @Test
            fun con_id_de_sesion_de_manilla_null()
            {
                val json = """
                    {
                        "client-id": 1,
                        "location-id": 1,
                        "tag-session-id": null,
                        "location-count-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
                    }
                    """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConteoUbicacionDTO::class.java) }
            }

            @Test
            fun sin_fecha_de_realizacion()
            {
                val json = """
                    {
                        "client-id": 1,
                        "location-id": 1,
                        "tag-session-id": 2
                    }
                    """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConteoUbicacionDTO::class.java) }
            }

            @Test
            fun con_fecha_de_realizacion_null()
            {
                val json = """
                    {
                        "client-id": 1,
                        "location-id": 1,
                        "tag-session-id": 2,
                        "location-count-timestamp": null
                    }
                    """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ConteoUbicacionDTO::class.java) }
            }
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun con_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(ConteoUbicacionDTOPruebas.entidadDTOPorDefecto)

            JSONAssert.assertEquals(ConteoUbicacionDTOPruebas.jsonPorDefecto, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun sin_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(ConteoUbicacionDTOPruebas.entidadDTOSinNulos)

            JSONAssert.assertEquals(ConteoUbicacionDTOPruebas.jsonSinNulos, entidadSerializada, JSONCompareMode.STRICT)
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
                val entidadDeNegocio = ConteoUbicacionDTOPruebas.entidadDTOPorDefecto.aEntidadDeNegocio()
                assertEquals(ConteoUbicacionDTOPruebas.entidadNegocioPorDefecto, entidadDeNegocio)
            }

            @Test
            fun sin_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = ConteoUbicacionDTOPruebas.entidadDTOSinNulos.aEntidadDeNegocio()
                assertEquals(ConteoUbicacionDTOPruebas.entidadNegocioSinNulos, entidadDeNegocio)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = ConteoUbicacionDTO(ConteoUbicacionDTOPruebas.entidadNegocioPorDefecto)
                assertEquals(ConteoUbicacionDTOPruebas.entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = ConteoUbicacionDTO(ConteoUbicacionDTOPruebas.entidadNegocioSinNulos)
                assertEquals(ConteoUbicacionDTOPruebas.entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}