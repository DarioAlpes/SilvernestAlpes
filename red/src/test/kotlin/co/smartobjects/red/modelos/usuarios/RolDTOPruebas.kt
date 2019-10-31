package co.smartobjects.red.modelos.usuarios

import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.assertEquals

internal const val jsonRolSinNulos = """
            {
                "name": "el_rol",
                "description": "la_descripcion",
                "permissions": [$jsonPermisoBackSinNulos]
            }
            """

@JvmField
internal val entidadDTORolSinNulos = RolDTO(
        "el_rol",
        "la_descripcion",
        listOf(entidadDTOPermisoBackSinNulos)
                                           )

@JvmField
internal val entidadNegocioRolSinNulos = Rol(
        "el_rol",
        "la_descripcion",
        setOf(entidadNegocioPermisoBackSinNulos)
                                            )

@DisplayName("RolDTO")
internal class RolDTOPruebas
{
    companion object
    {
        private const val jsonSinNulos = jsonRolSinNulos

        private val entidadDTOSinNulos = entidadDTORolSinNulos

        private val entidadNegocioSinNulos = entidadNegocioRolSinNulos

    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, RolDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @Test
        fun json_con_permisos_vacios_correctamente()
        {
            val jsonConPermisosVacios = """
            {
                "name": "el_rol",
                "description": "la_descripcion",
                "permissions": []
            }
            """
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConPermisosVacios, RolDTO::class.java)
            assertEquals(entidadDTOSinNulos.copy(permisos = listOf()), entidadDeserializada)
        }

        @Test
        fun json_con_permisos_duplicados_correctamente()
        {
            val jsonConPermisosDuplicados = """
            {
                "name": "el_rol",
                "description": "la_descripcion",
                "permissions": [$jsonPermisoBackSinNulos, $jsonPermisoBackSinNulos]
            }
            """
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConPermisosDuplicados, RolDTO::class.java)
            assertEquals(entidadDTOSinNulos.copy(permisos = listOf(entidadDTOPermisoBackSinNulos, entidadDTOPermisoBackSinNulos)), entidadDeserializada)
        }

        @Test
        fun lanza_excepcion_con_un_json_sin_nombre()
        {
            val jsonConEndpointNull = """
            {
                "description": "la_descripcion",
                "permissions": [$jsonPermisoBackSinNulos]
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, RolDTO::class.java) }
        }

        @Test
        fun lanza_excepcion_con_un_json_con_nombre_null()
        {
            val jsonConEndpointNull = """
            {
                "name": null,
                "description": "la_descripcion",
                "permissions": [$jsonPermisoBackSinNulos]
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, RolDTO::class.java) }
        }

        @Test
        fun lanza_excepcion_con_un_json_sin_descripcion()
        {
            val jsonSinEndpoint = """
            {
                "name": "el_rol",
                "permissions": [$jsonPermisoBackSinNulos]
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinEndpoint, RolDTO::class.java) }
        }

        @Test
        fun lanza_excepcion_con_un_json_con_descripcion_null()
        {
            val jsonConEndpointNull = """
            {
                "name": "el_rol",
                "description": null,
                "permissions": [$jsonPermisoBackSinNulos]
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, RolDTO::class.java) }
        }

        @Test
        fun lanza_excepcion_con_un_json_sin_permisos()
        {
            val jsonSinEndpoint = """
            {
                "name": "el_rol",
                "description": "la_descripcion"
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinEndpoint, RolDTO::class.java) }
        }

        @Test
        fun lanza_excepcion_con_un_json_con_permisos_null()
        {
            val jsonConEndpointNull = """
            {
                "name": "el_rol",
                "description": "la_descripcion",
                "permissions": null
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, RolDTO::class.java) }
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
                val entidadDTO = RolDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}