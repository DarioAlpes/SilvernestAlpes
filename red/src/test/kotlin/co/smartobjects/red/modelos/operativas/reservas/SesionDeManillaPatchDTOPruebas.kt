package co.smartobjects.red.modelos.operativas.reservas

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.red.ConfiguracionJackson
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("SesionDeManillaPatchDTO")
class SesionDeManillaPatchDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacion =
                """{
                        "tag-uuid": "${SesionDeManillaDTOPruebas.TAG_UUID_DEFECTO_STR}",
                        "deactivation-date": "${SesionDeManillaDTOPruebas.FECHA_DESACTIVACION_DEFECTO_STR}"
                   }"""

        private const val jsonSerializacionEsperado = jsonDeserializacion

        private val entidadDTOPorDefecto = SesionDeManillaPatchDTO(SesionDeManillaDTOPruebas.TAG_UUID_DEFECTO, SesionDeManillaDTOPruebas.FECHA_DESACTIVACION_DEFECTO)

        private const val jsonDeserializacionConNulos =
                """{
                        "tag-uuid": "${SesionDeManillaDTOPruebas.TAG_UUID_DEFECTO_STR}",
                        "deactivation-date": null
                   }"""

        private const val jsonSerializacionConNulosEsperado = jsonDeserializacionConNulos

        private val entidadDTOConNulos = SesionDeManillaPatchDTO(SesionDeManillaDTOPruebas.TAG_UUID_DEFECTO, null)
    }

    @Test
    fun si_el_campo_uuid_tag_no_es_nulo_se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = SesionDeManillaPatchDTO(SesionDeManillaDTOPruebas.TAG_UUID_DEFECTO, null)
        val camposEsperados = setOf(SesionDeManilla.CampoUuidTag(SesionDeManillaDTOPruebas.TAG_UUID_DEFECTO))

        assertEquals(camposEsperados, dto.aConjuntoCamposModificables())
    }

    @Test
    fun si_el_campo_fecha_de_desactivacion_no_es_nulo_se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = SesionDeManillaPatchDTO(null, SesionDeManillaDTOPruebas.FECHA_DESACTIVACION_DEFECTO)

        val camposDeserializados = dto.aConjuntoCamposModificables()

        assertEquals(1, camposDeserializados.size)
        assertEquals(SesionDeManillaDTOPruebas.FECHA_DESACTIVACION_DEFECTO, camposDeserializados.first().valor)
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacion, SesionDeManillaPatchDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_valores_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionConNulos, SesionDeManillaPatchDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun de_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOPorDefecto)

            JSONAssert.assertEquals(jsonSerializacionEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun de_valores_con_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOConNulos)

            JSONAssert.assertEquals(jsonSerializacionConNulosEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }
    }
}