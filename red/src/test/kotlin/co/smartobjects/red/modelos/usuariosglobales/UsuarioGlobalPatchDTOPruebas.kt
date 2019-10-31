package co.smartobjects.red.modelos.usuariosglobales

import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.red.ConfiguracionJackson
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DisplayName("UsuarioGlobalPatchDTO")
class UsuarioGlobalPatchDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacionPorDefecto = """{}"""

        private const val jsonSerializacionPorDefectoEsperado = """{"password": null, "active": null}"""

        private val entidadDTOPorDefecto = UsuarioGlobalPatchDTO(null, null)


        private const val jsonDeserializacionConNulos = """{"password": null, "active": null}"""

        private const val jsonSerializacionConNulosEsperado = jsonDeserializacionConNulos

        private val entidadDTOConNulos = entidadDTOPorDefecto

        private const val jsonDeserializacion = """{"password": "123", "active": true}"""

        private const val jsonSerializacionEsperado = jsonDeserializacion

        private val entidadDTOSinNulos = UsuarioGlobalPatchDTO(charArrayOf('1', '2', '3'), true)
    }

    @Test
    fun si_el_campo_contraseña_no_es_nulo_se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = UsuarioGlobalPatchDTO(charArrayOf('1', '2', '3'), null)
        val listaEsperada = setOf(UsuarioGlobal.CredencialesUsuario.CampoContraseña(charArrayOf('1', '2', '3')))

        assertEquals(listaEsperada, dto.aConjuntoCamposModificables())
    }

    @Test
    fun si_el_campo_activo_no_es_nulo_se_convierte_a_lista_de_campos_modificables_correctamente()
    {
        val dto = UsuarioGlobalPatchDTO(null, true)
        val listaEsperada = setOf(UsuarioGlobal.DatosUsuario.CampoActivo(true))

        assertEquals(listaEsperada, dto.aConjuntoCamposModificables())
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, UsuarioGlobalPatchDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_valores_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionConNulos, UsuarioGlobalPatchDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacion, UsuarioGlobalPatchDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("no lanza excepción con un json")]
        inner class NoLanzaExcepcion
        {
            @Test
            fun sin_contraseña()
            {
                val jsonSinValor = """{"active": true}"""

                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinValor, UsuarioGlobalPatchDTO::class.java)
            }

            @Test
            fun con_contraseña_null()
            {
                val jsonConValorNull = """{"password": null, "active": true}"""

                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConValorNull, UsuarioGlobalPatchDTO::class.java)
            }

            @Test
            fun sin_activo()
            {
                val jsonSinValor = """{"password": "123"}"""

                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinValor, UsuarioGlobalPatchDTO::class.java)
            }

            @Test
            fun con_activo_null()
            {
                val jsonConValorNull = """{"password": "123", "active": null}"""

                ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConValorNull, UsuarioGlobalPatchDTO::class.java)
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
        fun valores_no_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOSinNulos)

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
            val entidadDTO = UsuarioGlobalPatchDTO(contraseña, true)
            entidadDTO.limpiarContraseña()
            assertTrue(contraseña.all { it == '\u0000' })
            assertTrue(entidadDTO.contraseña!!.all { it == '\u0000' })
        }
    }
}