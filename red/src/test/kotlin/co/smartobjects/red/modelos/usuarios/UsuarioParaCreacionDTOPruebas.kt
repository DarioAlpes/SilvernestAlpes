package co.smartobjects.red.modelos.usuarios

import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
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

@DisplayName("UsuarioParaCreacionDTO")
internal class UsuarioParaCreacionDTOPruebas
{
    companion object
    {
        private const val jsonSinNulos = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
            """

        private val entidadDTOSinNulos = UsuarioParaCreacionDTO(
                1,
                "el_usuario",
                "el_nombre",
                "el@email.com",
                charArrayOf('1', '2', '3'),
                listOf("un_rol", "otro_rol"),
                true,
                "",
                "",
                "",
                "",
                "",
                ""
                                                               )

        private val entidadNegocioSinNulos = Usuario.UsuarioParaCreacion(
                Usuario.DatosUsuario(1, "el_usuario", "el_nombre", "el@email.com", true,"","","","","",""),
                charArrayOf('1', '2', '3'),
                setOf(Rol.RolParaCreacionDeUsuario("un_rol"), Rol.RolParaCreacionDeUsuario("otro_rol"))
                                                                        )

    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, UsuarioParaCreacionDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @Test
        fun json_con_roles_vacios_correctamente()
        {
            val jsonConPermisosVacios = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "roles": [],
                "active": true
            }
            """
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConPermisosVacios, UsuarioParaCreacionDTO::class.java)
            assertEquals(entidadDTOSinNulos.copy(roles = listOf()), entidadDeserializada)
        }

        @Test
        fun json_con_roles_duplicados_correctamente()
        {
            val jsonConPermisosDuplicados = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "roles": ["un_rol", "un_rol"],
                "active": true
            }
            """
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConPermisosDuplicados, UsuarioParaCreacionDTO::class.java)
            assertEquals(entidadDTOSinNulos.copy(roles = listOf("un_rol", "un_rol")), entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_usuario()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun con_usuario_null()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": null,
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun sin_nombre()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "email": "el@email.com",
                "password": "123",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun con_nombre_null()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": null,
                "email": "el@email.com",
                "password": "123",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun sin_email()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "password": "123",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun con_email_null()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": null,
                "email": null,
                "password": "123",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun sin_contraseña()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun con_contraseña_null()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": null,
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun sin_roles()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun con_roles_null()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "roles": null,
                "active": true
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun sin_activo()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "roles": ["un_rol", "otro_rol"]
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
            }

            @Test
            fun con_activo_null()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "password": "123",
                "roles": ["un_rol", "otro_rol"],
                "active": null
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioParaCreacionDTO::class.java) })
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
                val entidadDTO = UsuarioParaCreacionDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }

            @Test
            fun usa_mismo_arreglo_de_contraseña()
            {
                val entidadDTO = UsuarioParaCreacionDTO(entidadNegocioSinNulos)
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