package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.red.ConfiguracionJackson
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("PaquetePatchDTO")
internal class PaquetePatchDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacionPorDefecto = """{}"""

        private const val jsonSerializacionPorDefectoEsperado = """{"name": null, "description": null, "available-for-sale": null}"""

        private val entidadDTOPorDefecto = PaquetePatchDTO(null, null, null)


        private const val jsonDeserializacionConNulos = """{"name": null, "description": null, "available-for-sale": null}"""

        private const val jsonSerializacionConNulosEsperado = jsonDeserializacionConNulos

        private val entidadDTOConNulos = entidadDTOPorDefecto


        private const val jsonDeserializacionConValores = """{"name": "Nombre", "description": "La descripción", "available-for-sale": true}"""

        private const val jsonSerializacionConValoresEsperado = jsonDeserializacionConValores

        private val entidadDTOConValores = PaquetePatchDTO("Nombre", "La descripción", true)
    }

    @Test
    fun si_el_campo_nombre_no_es_nulo_se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = PaquetePatchDTO("Nombre", null, null)
        val listaEsperada = setOf(Paquete.CampoNombre("Nombre"))

        assertEquals(listaEsperada, dto.aConjuntoCamposModificables())
    }

    @Test
    fun si_el_campo_descripcion_no_es_nulo_se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = PaquetePatchDTO(null, "Descripcion", null)
        val listaEsperada = setOf(Paquete.CampoDescripcion("Descripcion"))

        assertEquals(listaEsperada, dto.aConjuntoCamposModificables())
    }

    @Test
    fun si_el_campo_disponible_para_la_venta_no_es_nulo_se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = PaquetePatchDTO(null, null, true)
        val listaEsperada = setOf(Paquete.CampoDisponibleParaLaVenta(true))

        assertEquals(listaEsperada, dto.aConjuntoCamposModificables())
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, PaquetePatchDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_valores_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionConNulos, PaquetePatchDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_con_valores_normales_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionConValores, PaquetePatchDTO::class.java)
            assertEquals(entidadDTOConValores, entidadDeserializada)
        }

        @[Nested DisplayName("no lanza excepción con un json")]
        inner class NoLanzaExcepcion
        {
            @Test
            fun sin_nombre()
            {
                val json = """
                {
                    "description": "La descripción",
                    "available-for-sale": true
                }""".trimIndent()

                ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaquetePatchDTO::class.java)
            }

            @Test
            fun con_nombre_null()
            {
                val json = """
                {
                    "name": null,
                    "description": "La descripción",
                    "available-for-sale": true
                }""".trimIndent()
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaquetePatchDTO::class.java)
            }

            @Test
            fun sin_descripcion()
            {
                val json = """
                {
                    "name": "nombre",
                    "available-for-sale": true
                }""".trimIndent()

                ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaquetePatchDTO::class.java)
            }

            @Test
            fun con_descripcion_null()
            {
                val json = """
                {
                    "name": "nombre",
                    "description": null,
                    "available-for-sale": true
                }""".trimIndent()
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaquetePatchDTO::class.java)
            }

            @Test
            fun sin_disponible_para_la_venta()
            {
                val json = """
                {
                    "name": "nombre",
                    "description": "descripción"
                }""".trimIndent()

                ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaquetePatchDTO::class.java)
            }

            @Test
            fun con_disponible_para_la_venta_null()
            {
                val json = """
                {
                    "name": "nombre",
                    "description": "descripción",
                    "available-for-sale": null
                }""".trimIndent()
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, PaquetePatchDTO::class.java)
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

            JSONAssert.assertEquals(jsonSerializacionPorDefectoEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun de_valores_con_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOConNulos)

            JSONAssert.assertEquals(jsonSerializacionConNulosEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun de_valores_con_valores_normales_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOConValores)

            JSONAssert.assertEquals(jsonSerializacionConValoresEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }
    }
}