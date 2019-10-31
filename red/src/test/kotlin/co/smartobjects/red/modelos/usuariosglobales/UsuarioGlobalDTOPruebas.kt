package co.smartobjects.red.modelos.usuariosglobales

import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

@DisplayName("UsuarioGlobalDTO")
internal class UsuarioGlobalDTOPruebas
{
    companion object
    {
        private const val jsonSinNulos = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "active": true
            }
            """

        private val entidadDTOSinNulos = UsuarioGlobalDTO(
                "el_usuario",
                "el_nombre",
                "el@email.com",
                true
                                                         )

        private val entidadNegocioSinNulos = UsuarioGlobal(
                UsuarioGlobal.DatosUsuario("el_usuario", "el_nombre", "el@email.com", true)
                                                          )

    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, UsuarioGlobalDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_usuario()
            {
                val jsonConEndpointNull = """
            {
                "full-name": "el_nombre",
                "email": "el@email.com",
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalDTO::class.java) }
            }

            @Test
            fun con_usuario_null()
            {
                val jsonConEndpointNull = """
            {
                "username": null,
                "full-name": "el_nombre",
                "email": "el@email.com",
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalDTO::class.java) }
            }

            @Test
            fun sin_nombre()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "email": "el@email.com",
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": null,
                "email": "el@email.com",
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalDTO::class.java) }
            }

            @Test
            fun sin_email()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalDTO::class.java) }
            }

            @Test
            fun con_email_null()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": null,
                "email": null,
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalDTO::class.java) }
            }

            @Test
            fun sin_activo()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com"
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalDTO::class.java) }
            }

            @Test
            fun con_activo_null()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": null,
                "email": "el@email.com",
                "active": null
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalDTO::class.java) }
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
                val entidadDTO = UsuarioGlobalDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}