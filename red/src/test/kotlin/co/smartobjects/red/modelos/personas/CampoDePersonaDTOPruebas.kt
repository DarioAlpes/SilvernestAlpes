package co.smartobjects.red.modelos.personas

import co.smartobjects.entidades.personas.CampoDePersona
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.util.stream.Stream
import kotlin.test.assertEquals

@DisplayName("CampoDePersonaDTO")
internal class CampoDePersonaDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacionPorDefecto = """
            {
                "field": "FULL NAME",
                "is-required": false
            }
            """

        private const val jsonSerializacionPorDefectoEsperado = """
            {
                "field": "FULL NAME",
                "is-required": false
            }
            """

        private val entidadDTOPorDefecto = CampoDePersonaDTO(
                CampoDePersonaDTO.Predeterminado.NOMBRE_COMPLETO,
                false
                                                            )

        private val entidadNegocioPorDefecto = CampoDePersona(
                CampoDePersona.Predeterminado.NOMBRE_COMPLETO,
                false
                                                             )

    }

    private fun darJsonSegunCampo(campo: String): String
    {
        return """
                {
                    "field": "$campo",
                    "is-required": false
                }
                """
    }

    private fun darEntidadDTOPruebaSegunCampo(campo: CampoDePersonaDTO.Predeterminado): CampoDePersonaDTO
    {
        return CampoDePersonaDTO(campo, false)
    }

    internal class ProveedorCampoPredetermiandoEnJson : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(CampoDePersonaDTO.Predeterminado.NOMBRE_COMPLETO, "FULL NAME"),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.TIPO_DOCUMENTO, "ID TYPE"),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.NUMERO_DOCUMENTO, "ID NUMBER"),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.GENERO, "GENDER"),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.FECHA_NACIMIENTO, "BIRTH DATE"),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.CATEGORIA, "CATEGORY"),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.AFILIACION, "AFFILIATION"),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.ES_ANONIMA, "IS ANONYMOUS"),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.LLAVE_IMAGEN, "THUMBNAIL KEY")
                            )
        }
    }

    internal class ProveedorCampoPredetermiandoNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(CampoDePersonaDTO.Predeterminado.NOMBRE_COMPLETO, CampoDePersona.Predeterminado.NOMBRE_COMPLETO),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.TIPO_DOCUMENTO, CampoDePersona.Predeterminado.TIPO_DOCUMENTO),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.NUMERO_DOCUMENTO, CampoDePersona.Predeterminado.NUMERO_DOCUMENTO),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.GENERO, CampoDePersona.Predeterminado.GENERO),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.FECHA_NACIMIENTO, CampoDePersona.Predeterminado.FECHA_NACIMIENTO),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.CATEGORIA, CampoDePersona.Predeterminado.CATEGORIA),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.AFILIACION, CampoDePersona.Predeterminado.AFILIACION),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.ES_ANONIMA, CampoDePersona.Predeterminado.ES_ANONIMA),
                    Arguments.of(CampoDePersonaDTO.Predeterminado.LLAVE_IMAGEN, CampoDePersona.Predeterminado.LLAVE_IMAGEN)
                            )
        }
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(CampoDePersonaDTOPruebas.jsonDeserializacionPorDefecto, CampoDePersonaDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_campo()
            {
                val jsonSinCampo = """
                {
                    "is-required": false
                }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCampo, CampoDePersonaDTO::class.java) })
            }

            @Test
            fun con_campo_null()
            {
                val jsonConCampoNull = """
                {
                    "field": null,
                    "is-required": false
                }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCampoNull, CampoDePersonaDTO::class.java) })
            }

            @Test
            fun sin_es_requerido()
            {
                val jsonSinEsRequerido = """
                {
                    "field": "FULL NAME"
                }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinEsRequerido, CampoDePersonaDTO::class.java) })
            }

            @Test
            fun con_es_requerido_null()
            {
                val jsonConEsRequeridoNull = """
                {
                    "field": "FULL NAME",
                    "is-required": null
                }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEsRequeridoNull, CampoDePersonaDTO::class.java) })
            }
        }

        @DisplayName("Cuando el campo predeterminado")
        @ParameterizedTest(name = "Es ''{1}'' en el json asigna ''{0}'' en el DTO")
        @ArgumentsSource(ProveedorCampoPredetermiandoEnJson::class)
        fun paraCampo(campo: CampoDePersonaDTO.Predeterminado, tipoJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunCampo(campo)
            val json = darJsonSegunCampo(tipoJson)
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(json, CampoDePersonaDTO::class.java)
            assertEquals(entidadDTO, entidadDeserializada)
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun de_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(CampoDePersonaDTOPruebas.entidadDTOPorDefecto)

            JSONAssert.assertEquals(CampoDePersonaDTOPruebas.jsonSerializacionPorDefectoEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @DisplayName("Cuando el campo predeterminado")
        @ParameterizedTest(name = "Es ''{0}'' en el dto asigna ''{1}'' en el json")
        @ArgumentsSource(ProveedorCampoPredetermiandoEnJson::class)
        fun paraCampo(campo: CampoDePersonaDTO.Predeterminado, tipoJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunCampo(campo)
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)
            val json = darJsonSegunCampo(tipoJson)
            JSONAssert.assertEquals(json, entidadSerializada, JSONCompareMode.STRICT)
        }
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
                val entidadDeNegocio = CampoDePersonaDTOPruebas.entidadDTOPorDefecto.aEntidadDeNegocio()
                assertEquals(entidadNegocioPorDefecto, entidadDeNegocio)
            }

            @DisplayName("Cuando el campo")
            @ParameterizedTest(name = "Es ''{0}'' en el dto asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorCampoPredetermiandoNegocio::class)
            fun paraTipoCampoDePersona(valorEnDTO: CampoDePersonaDTO.Predeterminado, valorEnNegocio: CampoDePersona.Predeterminado)
            {
                val entidadDTO = darEntidadDTOPruebaSegunCampo(valorEnDTO)
                val entidadNegocio = entidadDTO.aEntidadDeNegocio()
                assertEquals(valorEnNegocio, entidadNegocio.campo)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = CampoDePersonaDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @DisplayName("Cuando el Tipo de Ubicación")
            @ParameterizedTest(name = "Es ''{0}'' en negocio asigna ''{1}'' en el dto")
            @ArgumentsSource(ProveedorCampoPredetermiandoNegocio::class)
            fun paraTipoCampoDePersona(valorEnDTO: CampoDePersonaDTO.Predeterminado, valorEnNegocio: CampoDePersona.Predeterminado)
            {
                val entidadNegocio = CampoDePersona(valorEnNegocio, false)
                val entidadDAOCreando = CampoDePersonaDTO(entidadNegocio)
                assertEquals(valorEnDTO, entidadDAOCreando.campo)
            }
        }
    }
}