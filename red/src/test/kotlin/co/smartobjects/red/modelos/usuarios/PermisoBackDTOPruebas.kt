package co.smartobjects.red.modelos.usuarios

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.util.stream.Stream
import kotlin.test.assertEquals

internal const val jsonPermisoBackSinNulos = """
            {
                "client-id": 1,
                "endpoint": "rol",
                "action": "PUT"
            }
            """

@JvmField
internal val entidadDTOPermisoBackSinNulos = PermisoBackDTO(
        1,
        "rol",
        PermisoBackDTO.AccionDTO.PUT
                                                           )

@JvmField
internal val entidadNegocioPermisoBackSinNulos = PermisoBack(
        1,
        "rol",
        PermisoBack.Accion.PUT
                                                            )

@DisplayName("PermisoBackDTO")
internal class PermisoBackDTOPruebas
{
    companion object
    {
        private const val jsonSinNulos = jsonPermisoBackSinNulos

        private val entidadDTOSinNulos = entidadDTOPermisoBackSinNulos

        private val entidadNegocioSinNulos = entidadNegocioPermisoBackSinNulos

    }

    private fun darJsonSegunAccionString(accion: String): String
    {
        return """
                {
                    "client-id": 1,
                    "endpoint": "rol",
                    "action": "$accion"
                }
                """
    }

    private fun darEntidadDTOPruebaSegunEnumeracion(
            accion: PermisoBackDTO.AccionDTO
                                                   )
            : PermisoBackDTO
    {
        return PermisoBackDTO(
                1,
                "rol",
                accion
                             )
    }

    internal class ProveedorAccionDTOConAccionEnJson : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PermisoBackDTO.AccionDTO.GET_TODOS, "GET_ALL"),
                    Arguments.of(PermisoBackDTO.AccionDTO.GET_UNO, "GET_ONE"),
                    Arguments.of(PermisoBackDTO.AccionDTO.PATCH, "PATCH"),
                    Arguments.of(PermisoBackDTO.AccionDTO.PUT, "PUT"),
                    Arguments.of(PermisoBackDTO.AccionDTO.POST, "POST"),
                    Arguments.of(PermisoBackDTO.AccionDTO.DELETE, "DELETE")
                            )
        }
    }


    internal class ProveedorAccionDTOConAccionNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PermisoBackDTO.AccionDTO.GET_TODOS, PermisoBack.Accion.GET_TODOS),
                    Arguments.of(PermisoBackDTO.AccionDTO.GET_UNO, PermisoBack.Accion.GET_UNO),
                    Arguments.of(PermisoBackDTO.AccionDTO.PATCH, PermisoBack.Accion.PATCH),
                    Arguments.of(PermisoBackDTO.AccionDTO.PUT, PermisoBack.Accion.PUT),
                    Arguments.of(PermisoBackDTO.AccionDTO.POST, PermisoBack.Accion.POST),
                    Arguments.of(PermisoBackDTO.AccionDTO.DELETE, PermisoBack.Accion.DELETE)
                            )
        }
    }

    @Nested
    inner class Deserializacion
    {

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(PermisoBackDTOPruebas.jsonSinNulos, PermisoBackDTO::class.java)
            assertEquals(PermisoBackDTOPruebas.entidadDTOSinNulos, entidadDeserializada)
        }

        @Test
        fun lanza_excepcion_con_un_json_sin_endpoint()
        {
            val jsonSinEndpoint = """
            {
                "client-id": 1,
                "action": "PUT"
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinEndpoint, PermisoBackDTO::class.java) }
        }

        @Test
        fun lanza_excepcion_con_un_json_con_endpoint_null()
        {
            val jsonConEndpointNull = """
            {
                "client-id": 1,
                "endpoint": null,
                "action": "PUT"
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, PermisoBackDTO::class.java) }
        }

        @Test
        fun lanza_excepcion_con_un_json_sin_accion()
        {
            val jsonSinEndpoint = """
            {
                "client-id": 1,
                "endpoint": "rol"
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinEndpoint, PermisoBackDTO::class.java) }
        }

        @Test
        fun lanza_excepcion_con_un_json_con_accion_null()
        {
            val jsonConEndpointNull = """
            {
                "client-id": 1,
                "endpoint": "rol",
                "action": null
            }
                """
            assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEndpointNull, PermisoBackDTO::class.java) }
        }

        @DisplayName("Cuando la acción")
        @ParameterizedTest(name = "Es ''{1}'' en el json asigna ''{0}'' en el DTO")
        @ArgumentsSource(ProveedorAccionDTOConAccionEnJson::class)
        fun paraAccion(accionDTO: PermisoBackDTO.AccionDTO, accionEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeracion(accionDTO)
            val json = darJsonSegunAccionString(accionEnJson)
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(json, PermisoBackDTO::class.java)
            assertEquals(entidadDTO, entidadDeserializada)
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun sin_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(PermisoBackDTOPruebas.entidadDTOSinNulos)
            JSONAssert.assertEquals(PermisoBackDTOPruebas.jsonSinNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @DisplayName("Cuando la acción")
        @ParameterizedTest(name = "Es ''{1}'' en el DTO asigna ''{0}'' en el json")
        @ArgumentsSource(ProveedorAccionDTOConAccionEnJson::class)
        fun paraTipoDocumento(accionDTO: PermisoBackDTO.AccionDTO, accionEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeracion(accionDTO)
            val jsonEsperado = darJsonSegunAccionString(accionEnJson)

            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

            JSONAssert.assertEquals(jsonEsperado, entidadSerializada, JSONCompareMode.STRICT)
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
                val entidadDeNegocio = PermisoBackDTOPruebas.entidadDTOSinNulos.aEntidadDeNegocio()
                assertEquals(PermisoBackDTOPruebas.entidadNegocioSinNulos, entidadDeNegocio)
            }

            @DisplayName("Cuando la acción")
            @ParameterizedTest(name = "Es ''{0}'' en el DTO asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorAccionDTOConAccionNegocio::class)
            fun paraTipoDocumento(accionDTO: PermisoBackDTO.AccionDTO, accion: PermisoBack.Accion)
            {
                val entidadDTO = darEntidadDTOPruebaSegunEnumeracion(accionDTO)
                val entidaComoNegocio = entidadDTO.aEntidadDeNegocio()
                assertEquals(accion, entidaComoNegocio.accion)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            private fun darEntidadPruebaSegunEnumeracion(
                    accion: PermisoBack.Accion
                                                        )
                    : PermisoBack
            {
                return PermisoBack(
                        1,
                        "rol",
                        accion
                                  )
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = PermisoBackDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }

            @DisplayName("Cuando el tipo de documentoCuando la acción")
            @ParameterizedTest(name = "Es ''{1}'' en el negocio asigna ''{0}'' en el DTO")
            @ArgumentsSource(ProveedorAccionDTOConAccionNegocio::class)
            fun paraTipoDocumento(accionDTO: PermisoBackDTO.AccionDTO, accion: PermisoBack.Accion)
            {
                val entidadNegocio = darEntidadPruebaSegunEnumeracion(accion)
                val entidadComoDTO = PermisoBackDTO(entidadNegocio)
                assertEquals(accionDTO, entidadComoDTO.accion)
            }
        }
    }
}