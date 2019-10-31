package co.smartobjects.red.modelos.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.Orden
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


@DisplayName("OrdenDTO")
internal class OrdenDTOPruebas
{
    companion object
    {
        private const val FECHA_REALIZACION_DEFECTO_STR = "2000-01-02T03:04:05-05:00[UTC-05:00]"
        private val FECHA_REALIZACION_DEFECTO = ZonedDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)

        const val jsonDeserializacionPorDefecto = """
            {
                "tag-session-id": 3,
                "transactions": [${TransaccionDebitoDTOPruebas.jsonDeserializacionPorDefecto}, ${TransaccionCreditoDTOPruebas.jsonDeserializacionPorDefecto}],
                "order-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
            }
            """

        const val jsonPorDefecto = """
            {
                "client-id": 0,
                "id": null,
                "tag-session-id": 3,
                "transactions": [${TransaccionDebitoDTOPruebas.jsonPorDefecto}, ${TransaccionCreditoDTOPruebas.jsonPorDefecto}],
                "order-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
            }
            """

        @JvmField
        val entidadDTOPorDefecto =
                OrdenDTO(
                        0,
                        null,
                        3,
                        listOf(TransaccionDebitoDTOPruebas.entidadDTOPorDefecto, TransaccionCreditoDTOPruebas.entidadDTOPorDefecto),
                        FECHA_REALIZACION_DEFECTO
                        )

        @JvmField
        val entidadNegocioPorDefecto =
                Orden(
                        0,
                        null,
                        3,
                        listOf(TransaccionDebitoDTOPruebas.entidadNegocioPorDefecto, TransaccionCreditoDTOPruebas.entidadNegocioPorDefecto),
                        FECHA_REALIZACION_DEFECTO
                     )

        private const val jsonConNulos = """
            {
                "client-id": 1,
                "id": null,
                "tag-session-id": 3,
                "transactions": [${TransaccionDebitoDTOPruebas.jsonConNulos}, ${TransaccionCreditoDTOPruebas.jsonConNulos}],
                "order-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
            }
            """

        private val entidadDTOConNulos =
                OrdenDTO(
                        1,
                        null,
                        3,
                        listOf(TransaccionDebitoDTOPruebas.entidadDTOConNulos, TransaccionCreditoDTOPruebas.entidadDTOConNulos),
                        FECHA_REALIZACION_DEFECTO
                        )

        private val entidadNegocioConNulos =
                Orden(
                        1,
                        null,
                        3,
                        listOf(TransaccionDebitoDTOPruebas.entidadNegocioConNulos, TransaccionCreditoDTOPruebas.entidadNegocioConNulos),
                        FECHA_REALIZACION_DEFECTO
                     )

        const val jsonSinNulos = """
            {
                "client-id": 1,
                "id": 2,
                "tag-session-id": 3,
                "transactions": [${TransaccionDebitoDTOPruebas.jsonSinNulos}, ${TransaccionCreditoDTOPruebas.jsonSinNulos}],
                "order-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
            }
            """

        @JvmField
        val entidadDTOSinNulos =
                OrdenDTO(
                        1,
                        2,
                        3,
                        listOf(TransaccionDebitoDTOPruebas.entidadDTOSinNulos, TransaccionCreditoDTOPruebas.entidadDTOSinNulos),
                        FECHA_REALIZACION_DEFECTO
                        )

        @JvmField
        val entidadNegocioSinNulos =
                Orden(
                        1,
                        2,
                        3,
                        listOf(TransaccionDebitoDTOPruebas.entidadNegocioSinNulos, TransaccionCreditoDTOPruebas.entidadNegocioSinNulos),
                        FECHA_REALIZACION_DEFECTO
                     )
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, OrdenDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, OrdenDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, OrdenDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_id_de_sesion_de_manilla()
            {
                val json = """
                {
                    "client-id": 1,
                    "id": 2,
                    "transactions": [${TransaccionDebitoDTOPruebas.jsonSinNulos}, ${TransaccionCreditoDTOPruebas.jsonSinNulos}],
                    "order-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
                }
                """
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, OrdenDTO::class.java) }
            }

            @Test
            fun con_id_de_sesion_de_manilla_null()
            {
                val json = """
                {
                    "client-id": 1,
                    "id": 2,
                    "tag-session-id": null,
                    "transactions": [${TransaccionDebitoDTOPruebas.jsonSinNulos}, ${TransaccionCreditoDTOPruebas.jsonSinNulos}],
                    "order-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, OrdenDTO::class.java) }
            }

            @Test
            fun sin_transacciones()
            {
                val json = """
                {
                    "client-id": 1,
                    "id": 2,
                    "tag-session-id": 3,
                    "order-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, OrdenDTO::class.java) }
            }

            @Test
            fun con_transacciones_null()
            {
                val json = """
                {
                    "client-id": 1,
                    "id": 2,
                    "tag-session-id": 3,
                    "transactions": null,
                    "order-timestamp": "$FECHA_REALIZACION_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, OrdenDTO::class.java) }
            }

            @Test
            fun sin_fecha_de_realizacion()
            {
                val json = """
                {
                    "client-id": 1,
                    "id": 2,
                    "tag-session-id": 3,
                    "transactions": [${TransaccionDebitoDTOPruebas.jsonSinNulos}, ${TransaccionCreditoDTOPruebas.jsonSinNulos}]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, OrdenDTO::class.java) }
            }

            @Test
            fun con_fecha_de_realizacion_null()
            {
                val json = """
                {
                    "client-id": 1,
                    "id": 2,
                    "tag-session-id": 3,
                    "transactions": [${TransaccionDebitoDTOPruebas.jsonSinNulos}, ${TransaccionCreditoDTOPruebas.jsonSinNulos}],
                    "order-timestamp": null
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, OrdenDTO::class.java) }
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
                val entidadDTO = OrdenDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = OrdenDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = OrdenDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}