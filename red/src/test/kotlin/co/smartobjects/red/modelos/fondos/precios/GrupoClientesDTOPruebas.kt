package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("GrupoClientesDTO")
internal class GrupoClientesDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = jsonGrupoClientesPorDefecto

        const val jsonPorDefectoEsperado = jsonGrupoClientesPorDefectoEsperado

        @JvmField
        val entidadDTOPorDefecto = grupoClientesDTOPorDefecto

        private const val jsonConCategoria = jsonGrupoClientesConCategoria

        private val entidadDTOConCategoria = grupoClientesDTOConCategoria

        private val entidadNegocioConCategoria = grupoClientesNegocioConCategoria

        private const val jsonConGrupoEdad = jsonGrupoClientesConGrupoEdad

        private val entidadDTOConGrupoEdad = grupoClientesDTOConGrupoEdad

        private val entidadNegocioConGrupoEdad = grupoClientesNegocioConGrupoEdad

        private const val jsonConGrupoEdadYCategoria = jsonGrupoClientesConGrupoEdadYCategoria

        private val entidadDTOConGrupoEdadYCategoria = grupoClientesDTOConGrupoEdadYCategoria

        private val entidadNegocioConGrupoEdadYCategoria = grupoClientesNegocioConGrupoEdadYCategoria
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, GrupoClientesDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_categoria_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCategoria, GrupoClientesDTO::class.java)
            assertEquals(entidadDTOConCategoria, entidadDeserializada)
        }

        @Test
        fun json_con_grupo_edad_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConGrupoEdad, GrupoClientesDTO::class.java)
            assertEquals(entidadDTOConGrupoEdad, entidadDeserializada)
        }

        @Test
        fun json_con_grupo_edad_y_categoria_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConGrupoEdadYCategoria, GrupoClientesDTO::class.java)
            assertEquals(entidadDTOConGrupoEdadYCategoria, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_field_name()
            {
                val jsonSinFieldName = """{
                "client-id": 1,
                "field-value": "A"
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinFieldName, GrupoClientesDTO::class.java) }
            }

            @Test
            fun con_field_name_null()
            {
                val jsonConFieldNameNull = """{
                "client-id": 1,
                "field-name": null,
                "field-value": "A"
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConFieldNameNull, GrupoClientesDTO::class.java) }
            }

            @Test
            fun sin_field_value()
            {
                val jsonSinFieldValue = """{
                "client-id": 1,
                "field-name": "CATEGORY"
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinFieldValue, GrupoClientesDTO::class.java) }
            }

            @Test
            fun con_field_value_null()
            {
                val jsonConFieldValueNull = """{
                "client-id": 1,
                "field-name": "CATEGORY",
                "field-value": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConFieldValueNull, GrupoClientesDTO::class.java) }
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

            JSONAssert.assertEquals(jsonPorDefectoEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun con_categoria_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOConCategoria)
            JSONAssert.assertEquals(jsonConCategoria, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun con_grupo_edad_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOConGrupoEdad)
            JSONAssert.assertEquals(jsonConGrupoEdad, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun con_grupo_edad_y_categoria_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOConGrupoEdadYCategoria)
            JSONAssert.assertEquals(jsonConGrupoEdadYCategoria, entidadSerializada, JSONCompareMode.STRICT)
        }
    }

    @Nested
    inner class Conversion
    {
        @Nested
        inner class AEntidadDeNegocio
        {
            @Test
            fun con_categoria_se_transforma_correctamente()
            {
                val entidadDeNegocio = entidadDTOConCategoria.aEntidadDeNegocio()
                assertEquals(entidadNegocioConCategoria, entidadDeNegocio)
            }

            @Test
            fun con_grupo_edad_se_transforma_correctamente()
            {
                val entidadDeNegocio = entidadDTOConGrupoEdad.aEntidadDeNegocio()
                assertEquals(entidadNegocioConGrupoEdad, entidadDeNegocio)
            }

            @Test
            fun con_grupo_edad_y_categoria_se_transforma_correctamente()
            {
                val entidadDeNegocio = entidadDTOConGrupoEdadYCategoria.aEntidadDeNegocio()
                assertEquals(entidadNegocioConGrupoEdadYCategoria, entidadDeNegocio)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_categoria_se_construye_correctamente()
            {
                val entidadDTO = GrupoClientesDTO(entidadNegocioConCategoria)
                assertEquals(entidadDTOConCategoria, entidadDTO)
            }

            @Test
            fun con_grupo_edad_se_construye_correctamente()
            {
                val entidadDTO = GrupoClientesDTO(entidadNegocioConGrupoEdad)
                assertEquals(entidadDTOConGrupoEdad, entidadDTO)
            }

            @Test
            fun con_grupo_edad_y_categoria_se_construye_correctamente()
            {
                val entidadDTO = GrupoClientesDTO(entidadNegocioConGrupoEdadYCategoria)
                assertEquals(entidadDTOConGrupoEdadYCategoria, entidadDTO)
            }
        }
    }
}