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

@DisplayName("UsuarioDTO")
internal class UsuarioDTOPruebas
{
    companion object
    {
        private const val jsonSinNulos = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "roles": [$jsonRolSinNulos],
                "active": true
            }
            """

        private val entidadDTOSinNulos = UsuarioDTO(
                1,
                "el_usuario",
                "el_nombre",
                "el@email.com",
                "",
                "",
                "",
                "",
                "",
                ",",
                listOf(entidadDTORolSinNulos),
                true
                                                   )

        private val entidadNegocioSinNulos = Usuario(
                Usuario.DatosUsuario(1, "el_usuario", "el_nombre", "el@email.com", true,"","","","","",""),
                setOf(entidadNegocioRolSinNulos)
                                                    )

    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, UsuarioDTO::class.java)
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
                "roles": [],
                "active": true
            }
            """
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConPermisosVacios, UsuarioDTO::class.java)
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
                "roles": [$jsonRolSinNulos, $jsonRolSinNulos],
                "active": true
            }
            """
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConPermisosDuplicados, UsuarioDTO::class.java)
            assertEquals(entidadDTOSinNulos.copy(roles = listOf(entidadDTORolSinNulos, entidadDTORolSinNulos)), entidadDeserializada)
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
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
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
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
            }

            @Test
            fun sin_id_cliente()
            {
                val jsonConEndpointNull = """
            {
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
            }

            @Test
            fun con_id_cliente_null()
            {
                val jsonConEndpointNull = """
            {
                "client-id": null,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "email": "el@email.com",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
            }

            @Test
            fun sin_nombre()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "email": "el@email.com",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
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
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
            }

            @Test
            fun sin_email()
            {
                val jsonConEndpointNull = """
            {
                "client-id": 1,
                "username": "el_usuario",
                "full-name": "el_nombre",
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
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
                "roles": ["un_rol", "otro_rol"],
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
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
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
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
                "roles": null,
                "active": true
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
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
                "roles": ["un_rol", "otro_rol"]
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
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
                "roles": ["un_rol", "otro_rol"],
                "active": null
            }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, UsuarioDTO::class.java) }
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
                val entidadDTO = UsuarioDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}