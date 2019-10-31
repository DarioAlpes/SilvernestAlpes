package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("SegmentoClientesDTO")
internal class SegmentoClientesDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = jsonSegmentoClientesPorDefecto

        const val jsonPorDefectoEsperado = jsonSegmentoClientesPorDefectoEsperado

        @JvmField
        val entidadDTOPorDefecto = segmentoClientesDTOPorDefecto

        private const val jsonConCategoria = jsonSegmentoClientesConCategoria

        private val entidadDTOConCategoria = segmentoClientesDTOConCategoria

        private val entidadNegocioConCategoria = segmentoClientesNegocioConCategoria

        private const val jsonConGrupoEdad = jsonSegmentoClientesConGrupoEdad

        private val entidadDTOConGrupoEdad = segmentoClientesDTOConGrupoEdad

        private val entidadNegocioConGrupoEdad = segmentoClientesNegocioConGrupoEdad
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, SegmentoClientesDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_categoria_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCategoria, SegmentoClientesDTO::class.java)
            assertEquals(entidadDTOConCategoria, entidadDeserializada)
        }

        @Test
        fun json_con_grupo_edad_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConGrupoEdad, SegmentoClientesDTO::class.java)
            assertEquals(entidadDTOConGrupoEdad, entidadDeserializada)
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
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinFieldName, SegmentoClientesDTO::class.java) })
            }

            @Test
            fun con_field_name_null()
            {
                val jsonConFieldNameNull = """{
                "client-id": 1,
                "field-name": null,
                "field-value": "A"
            }"""
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConFieldNameNull, SegmentoClientesDTO::class.java) })
            }

            @Test
            fun sin_field_value()
            {
                val jsonSinFieldValue = """{
                "client-id": 1,
                "field-name": "CATEGORY"
            }"""
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinFieldValue, SegmentoClientesDTO::class.java) })
            }

            @Test
            fun con_field_value_null()
            {
                val jsonConFieldValueNull = """{
                "client-id": 1,
                "field-name": "CATEGORY",
                "field-value": null
            }"""
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConFieldValueNull, SegmentoClientesDTO::class.java) })
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
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_categoria_se_construye_correctamente()
            {
                val entidadDTO = SegmentoClientesDTO(entidadNegocioConCategoria)
                assertEquals(entidadDTOConCategoria, entidadDTO)
            }

            @Test
            fun con_grupo_edad_se_construye_correctamente()
            {
                val entidadDTO = SegmentoClientesDTO(entidadNegocioConGrupoEdad)
                assertEquals(entidadDTOConGrupoEdad, entidadDTO)
            }
        }
    }
}