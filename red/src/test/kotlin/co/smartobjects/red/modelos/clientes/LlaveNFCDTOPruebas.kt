package co.smartobjects.red.modelos.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.fasterxml.jackson.databind.JsonMappingException
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


@DisplayName("LlaveNFCDTO")
internal class LlaveNFCDTOPruebas
{
    companion object
    {
        private const val FECHA_CREACION_DEFECTO_STR = "2000-01-02T03:04:05-05:00[UTC-05:00]"
        private val FECHA_CREACION_DEFECTO = ZonedDateTime.of(LocalDate.of(2000, 1, 2), LocalTime.of(3, 4, 5), ZONA_HORARIA_POR_DEFECTO)
        private const val LLAVE_PRUEBA = "123-789-456"

        private const val jsonSinNulos = """
            {
                "client-id": 1,
                "key": "$LLAVE_PRUEBA",
                "creation-datetime": "$FECHA_CREACION_DEFECTO_STR"
            }
            """

        private val entidadDTOSinNulos = LlaveNFCDTO(1, LLAVE_PRUEBA, FECHA_CREACION_DEFECTO)

        private val entidadNegocioSinNulos = Cliente.LlaveNFC(1, LLAVE_PRUEBA, FECHA_CREACION_DEFECTO)

    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, LlaveNFCDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_llave()
            {
                val json = """
                {
                    "client-id": 1,
                    "creation-datetime": "$FECHA_CREACION_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LlaveNFCDTO::class.java) }
            }

            @Test
            fun con_llave_null()
            {
                val json = """
                {
                    "client-id": 1,
                    "key": null,
                    "creation-datetime": "$FECHA_CREACION_DEFECTO_STR"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LlaveNFCDTO::class.java) }
            }

            @Test
            fun sin_fecha_creacion()
            {
                val json = """
                {
                    "client-id": 1,
                    "key": "$LLAVE_PRUEBA"
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LlaveNFCDTO::class.java) }
            }

            @Test
            fun con_fecha_creacion_null()
            {
                val json = """
                {
                    "client-id": 1,
                    "key": "$LLAVE_PRUEBA",
                    "creation-datetime": null
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LlaveNFCDTO::class.java) }
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
                val entidadDTO = LlaveNFCDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}