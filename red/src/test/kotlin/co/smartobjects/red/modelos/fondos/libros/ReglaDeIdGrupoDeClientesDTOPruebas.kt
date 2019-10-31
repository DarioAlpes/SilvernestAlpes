package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.ReglaDeIdGrupoDeClientes
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("ReglaDeIdGrupoDeClientesDTO")
internal class ReglaDeIdGrupoDeClientesDTOPruebas
{
    companion object
    {
        const val jsonPorDefecto = """{ "rule-type": "CLIENTS_GROUP_ID", "restricted-id": 123 }"""

        const val jsonPorDefectoEsperado = jsonPorDefecto

        @JvmField
        val entidadDTOPorDefecto = ReglaDeIdGrupoDeClientesDTO(123)

        @JvmField
        val entidadNegocioPorDefecto = ReglaDeIdGrupoDeClientes(123)
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonPorDefecto, ReglaDeIdGrupoDeClientesDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_tipo_de_regla()
            {
                val json = """{
                "restricted-id": 3
            }"""
                assertThrows<MismatchedInputException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReglaDeIdGrupoDeClientesDTO::class.java) }
            }

            @Test
            fun con_tipo_de_regla_null()
            {
                val json = """{
                "rule-type": null,
                "restricted-id": 3
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReglaDeIdGrupoDeClientesDTO::class.java) }
            }

            @Test
            fun sin_id_restriccion()
            {
                val json = """{
                "rule-type": "CLIENTS_GROUP_ID"
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReglaDeIdGrupoDeClientesDTO::class.java) }
            }

            @Test
            fun con_id_restriccion_null()
            {
                val json = """{
                "rule-type": "CLIENTS_GROUP_ID",
                "restricted-id": null
            }"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReglaDeIdGrupoDeClientesDTO::class.java) }
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
    }

    @Test
    fun el_tipo_es_ID_GRUPO_DE_CLIENTES()
    {
        assertEquals(IReglaDTO.Tipo.ID_GRUPO_DE_CLIENTES, entidadDTOPorDefecto.tipo)
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
                val entidadDeNegocio = entidadDTOPorDefecto.aEntidadDeNegocio()
                assertEquals(entidadNegocioPorDefecto, entidadDeNegocio)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = ReglaDeIdGrupoDeClientesDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }
        }
    }
}