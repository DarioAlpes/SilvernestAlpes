package co.smartobjects.red.modelos.usuarios

import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DisplayName("ContraseñaUsuarioDTO")
class ContraseñaUsuarioDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacion = """{"password": "123"}"""

        private const val jsonSerializacionEsperado = """{"password": "123"}"""

        private val entidadDTOPorDefecto = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
    }

    @Test
    fun se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = ContraseñaUsuarioDTO(charArrayOf('1', '2', '3'))
        val listaEsperada = setOf(Usuario.CredencialesUsuario.CampoContraseña(charArrayOf('1', '2', '3')))

        assertEquals(listaEsperada, dto.aConjuntoCamposModificables())
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacion, ContraseñaUsuarioDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_contraseña()
            {
                val jsonSinValor = "{}"
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinValor, ContraseñaUsuarioDTO::class.java) }
            }

            @Test
            fun con_contraseña_null()
            {
                val jsonConValorNull = """{"password": null}"""
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConValorNull, ContraseñaUsuarioDTO::class.java) }
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

            JSONAssert.assertEquals(jsonSerializacionEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }
    }

    @Nested
    inner class LimpiarContraseña
    {
        @Test
        fun limpia_el_arreglo_original_y_el_arreglo_interno_correctamente()
        {
            val contraseña = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a')
            val entidadDTO = ContraseñaUsuarioDTO(contraseña)
            entidadDTO.limpiarContraseña()
            assertTrue(contraseña.all { it == '\u0000' })
            assertTrue(entidadDTO.contraseña.all { it == '\u0000' })
        }
    }
}