package co.smartobjects.red.modelos.usuariosglobales

import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("UsuarioGlobalParaCreacionDTO")
internal class UsuarioGlobalParaCreacionDTOPruebas
{
    companion object
    {
        private const val jsonSinNulos = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "active": true
            }
            """

        private val entidadDTOSinNulos = UsuarioGlobalParaCreacionDTO(
                "el_usuario",
                "el_nombre",
                "el@email.com",
                charArrayOf('1', '2', '3'),
                true
                                                                     )

        private val entidadNegocioSinNulos = UsuarioGlobal.UsuarioParaCreacion(
                UsuarioGlobal.DatosUsuario("el_usuario", "el_nombre", "el@email.com", true),
                charArrayOf('1', '2', '3')
                                                                              )

    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, UsuarioGlobalParaCreacionDTO::class.java)
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
                "password": "123",
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
            }

            @Test
            fun con_usuario_null()
            {
                val jsonConEndpointNull = """
            {
                "username": null,
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
            }

            @Test
            fun sin_nombre()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "email": "el@email.com",
                "password": "123",
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
            }

            @Test
            fun con_nombre_null()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": null,
                "email": "el@email.com",
                "password": "123",
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
            }

            @Test
            fun sin_email()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "password": "123",
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
            }

            @Test
            fun con_email_null()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": null,
                "email": null,
                "password": "123",
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
            }

            @Test
            fun sin_contraseña()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
            }

            @Test
            fun con_contraseña_null()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": null,
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
            }

            @Test
            fun sin_activo()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
            }

            @Test
            fun con_activo_null()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "active": null
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioGlobalParaCreacionDTO::class.java) })
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

            @Test
            fun usa_mismo_arreglo_de_contraseña()
            {
                val entidadDeNegocio = entidadDTOSinNulos.aEntidadDeNegocio()
                assertTrue(entidadDeNegocio.contraseña === entidadDTOSinNulos.contraseña)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = UsuarioGlobalParaCreacionDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }

            @Test
            fun usa_mismo_arreglo_de_contraseña()
            {
                val entidadDTO = UsuarioGlobalParaCreacionDTO(entidadNegocioSinNulos)
                assertTrue(entidadDTO.contraseña === entidadNegocioSinNulos.contraseña)
            }
        }
    }

    @Nested
    inner class LimpiarContraseña
    {
        @Test
        fun limpia_el_arreglo_original_y_el_arreglo_interno_correctamente()
        {
            val contraseña = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a')
            val entidadDTO = entidadDTOSinNulos.copy(contraseña = contraseña)
            entidadDTO.limpiarContraseña()
            assertTrue(contraseña.all { it == '\u0000' })
            assertTrue(entidadDTO.contraseña.all { it == '\u0000' })
        }
    }
}