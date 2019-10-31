package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("ImpuestoDTO")
internal class ImpuestoDTOPruebas
{
    companion object
    {
        private const val jsonPorDefecto = """
            {
                "name": "Por Defecto",
                "rate": 0
            }
            """

        private const val jsonPorDefectoEsperado = """
            {
                "client-id": 0,
                "id": null,
                "name": "Por Defecto",
                "rate": 0
            }
            """

        @JvmField
        val impuestoPorDefecto = ImpuestoDTO(0, null, "Por Defecto", Decimal.CERO)

        @JvmField
        val impuestNegocioPorDefecto = Impuesto(0, null, "Por Defecto", Decimal.CERO)

        private const val jsonConNulos = """
            {
                "client-id": 0,
                "id": null,
                "name": "Por Defecto",
                "rate": 10.0
            }
            """

        @JvmField
        val impuestoConNulos = ImpuestoDTO(0, null, "Por Defecto", Decimal.DIEZ)

        @JvmField
        val impuestoNegocioConNulos = Impuesto(0, null, "Por Defecto", Decimal.DIEZ)

        private const val jsonSinNulos = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Por Defecto",
                "rate": 123.456
            }
            """

        @JvmField
        val impuestoSinNulos = ImpuestoDTO(1, 1, "Por Defecto", Decimal(123.456))

        @JvmField
        val impuestoNegocioSinNulos = Impuesto(1, 1, "Por Defecto", Decimal(123.456))
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, ImpuestoDTO::class.java)
            assertEquals(impuestoPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, ImpuestoDTO::class.java)
            assertEquals(impuestoConNulos, entidadDeserializada)
        }

        @Test
        fun json_son_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, ImpuestoDTO::class.java)
            assertEquals(impuestoSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_nombre()
            {
                val jsonSinNombre = """
            {
                "client-id": 1,
                "id": 1,
                "rate": 123.456
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNombre, ImpuestoDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val jsonConNombreNull = """
            {
                "client-id": 1,
                "id": 1,
                "name": null,
                "rate": 123.456
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNombreNull, ImpuestoDTO::class.java) }
            }

            @Test
            fun sin_tasa()
            {
                val jsonSinTasa = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Impuesto 1"
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinTasa, ImpuestoDTO::class.java) }
            }

            @Test
            fun con_tasa_null()
            {
                val jsonConNombreNull = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Impuesto 1",
                "rate": null
            }
            """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNombreNull, ImpuestoDTO::class.java) }
            }
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun de_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(impuestoPorDefecto)

            JSONAssert.assertEquals(jsonPorDefectoEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun con_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(impuestoConNulos)

            JSONAssert.assertEquals(jsonConNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun sin_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(impuestoSinNulos)

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
            fun con_valores_por_defecto_se_transforma_correctamente()
            {
                val entidadDeNegocio = impuestoPorDefecto.aEntidadDeNegocio()
                assertEquals(impuestNegocioPorDefecto, entidadDeNegocio)
            }

            @Test
            fun con_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = impuestoConNulos.aEntidadDeNegocio()
                assertEquals(impuestoNegocioConNulos, entidadDeNegocio)
            }

            @Test
            fun sin_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = impuestoSinNulos.aEntidadDeNegocio()
                assertEquals(impuestoNegocioSinNulos, entidadDeNegocio)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = ImpuestoDTO(impuestNegocioPorDefecto)
                assertEquals(impuestoPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = ImpuestoDTO(impuestoNegocioConNulos)
                assertEquals(impuestoConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = ImpuestoDTO(impuestoNegocioSinNulos)
                assertEquals(impuestoSinNulos, entidadDTO)
            }
        }
    }
}