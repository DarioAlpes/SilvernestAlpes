package co.smartobjects.red.modelos.personas

import co.smartobjects.entidades.personas.Persona
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
import org.threeten.bp.LocalDate
import java.util.stream.Stream
import kotlin.test.assertEquals


@DisplayName("PersonaDTO")
internal class PersonaDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacionPorDefecto = jsonPersonaPorDefecto

        private const val jsonSerializacionPorDefectoEsperado = jsonPersonaPorDefectoEsperado

        private val entidadDTOPorDefecto = personaDTOPorDefecto

        private val entidadNegocioPorDefecto = personaNegocioPorDefecto

        private const val jsonConNulos = jsonPersonaConNulos

        private val entidadDTOConNulos = personaDTOConNulos

        private val entidadNegocioConNulos = personaNegocioConNulos


        private const val jsonSinNulos = jsonPersonaSinNulos

        private val entidadDTOSinNulos = personaDTOSinNulos

        private val entidadNegocioSinNulos = personaNegocioSinNulos

    }

    private fun darJsonSegunEnumeraciones(tipoDocumento: String? = null, genero: String? = null, categoria: String? = null, afiliacion: String? = null): String
    {
        return """
                {
                    "client-id": 0,
                    "id": 0,
                    "full-name": "Por Defecto",
                    "document-type": "${tipoDocumento ?: "CC"}",
                    "document-number": "Por Defecto",
                    "gender": "${genero ?: "UNKNOWN"}",
                    "birthdate": "2017-05-31",
                    "category": "${categoria ?: "A"}",
                    "affiliation": "${afiliacion ?: "CONTRIBUTOR"}",
                    "is-anonymous": false,
                    "image-key": "llave"
                }
                """
    }

    private fun darEntidadDTOPruebaSegunEnumeraciones(
            tipoDocumento: PersonaDTO.TipoDocumento? = null,
            genero: PersonaDTO.Genero? = null,
            categoria: PersonaDTO.Categoria? = null,
            afiliacion: PersonaDTO.Afiliacion? = null)
            : PersonaDTO
    {
        return PersonaDTO(
                0,
                0,
                "Por Defecto",
                tipoDocumento ?: PersonaDTO.TipoDocumento.CC,
                "Por Defecto",
                genero ?: PersonaDTO.Genero.DESCONOCIDO,
                LocalDate.of(2017, 5, 31),
                categoria ?: PersonaDTO.Categoria.A,
                afiliacion ?: PersonaDTO.Afiliacion.COTIZANTE,
                false,
                "llave",
                "Por Defecto",
                "0",
                PersonaDTO.Tipo.NO_AFILIADO
                         )
    }

    internal class ProveedorTipoDocumentoDTOConTipoDocumentoEnJson : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDTO.TipoDocumento.CC, "CC"),
                    Arguments.of(PersonaDTO.TipoDocumento.CD, "CD"),
                    Arguments.of(PersonaDTO.TipoDocumento.CE, "CE"),
                    Arguments.of(PersonaDTO.TipoDocumento.PA, "PA"),
                    Arguments.of(PersonaDTO.TipoDocumento.RC, "RC"),
                    Arguments.of(PersonaDTO.TipoDocumento.NIT, "NIT"),
                    Arguments.of(PersonaDTO.TipoDocumento.NUIP, "NUIP"),
                    Arguments.of(PersonaDTO.TipoDocumento.TI, "TI")
                            )
        }
    }

    internal class ProveedorGeneroDTOConGeneroEnJson : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDTO.Genero.MASCULINO, "MALE"),
                    Arguments.of(PersonaDTO.Genero.FEMENINO, "FEMALE"),
                    Arguments.of(PersonaDTO.Genero.DESCONOCIDO, "UNKNOWN")
                            )
        }
    }

    internal class ProveedorCategoriaDTOConCategoriaEnJson : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDTO.Categoria.A, "A"),
                    Arguments.of(PersonaDTO.Categoria.B, "B"),
                    Arguments.of(PersonaDTO.Categoria.C, "C"),
                    Arguments.of(PersonaDTO.Categoria.D, "D")
                            )
        }
    }

    internal class ProveedorAfiliacionDTOConAfiliacionEnJson : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDTO.Afiliacion.COTIZANTE, "CONTRIBUTOR"),
                    Arguments.of(PersonaDTO.Afiliacion.BENEFICIARIO, "BENEFICIARY")
                            )
        }
    }


    internal class ProveedorTipoDocumentoDTOConTipoDocumentoNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDTO.TipoDocumento.CC, Persona.TipoDocumento.CC),
                    Arguments.of(PersonaDTO.TipoDocumento.CD, Persona.TipoDocumento.CD),
                    Arguments.of(PersonaDTO.TipoDocumento.CE, Persona.TipoDocumento.CE),
                    Arguments.of(PersonaDTO.TipoDocumento.PA, Persona.TipoDocumento.PA),
                    Arguments.of(PersonaDTO.TipoDocumento.RC, Persona.TipoDocumento.RC),
                    Arguments.of(PersonaDTO.TipoDocumento.NIT, Persona.TipoDocumento.NIT),
                    Arguments.of(PersonaDTO.TipoDocumento.NUIP, Persona.TipoDocumento.NUIP),
                    Arguments.of(PersonaDTO.TipoDocumento.TI, Persona.TipoDocumento.TI)
                            )
        }
    }

    internal class ProveedorGeneroDTOConGeneroNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDTO.Genero.MASCULINO, Persona.Genero.MASCULINO),
                    Arguments.of(PersonaDTO.Genero.FEMENINO, Persona.Genero.FEMENINO),
                    Arguments.of(PersonaDTO.Genero.DESCONOCIDO, Persona.Genero.DESCONOCIDO)
                            )
        }
    }

    internal class ProveedorCategoriaDTOConCategoriaNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDTO.Categoria.A, Persona.Categoria.A),
                    Arguments.of(PersonaDTO.Categoria.B, Persona.Categoria.B),
                    Arguments.of(PersonaDTO.Categoria.C, Persona.Categoria.C),
                    Arguments.of(PersonaDTO.Categoria.D, Persona.Categoria.D)
                            )
        }
    }

    internal class ProveedorAfiliacionDTOConAfiliacionNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(PersonaDTO.Afiliacion.COTIZANTE, Persona.Afiliacion.COTIZANTE),
                    Arguments.of(PersonaDTO.Afiliacion.BENEFICIARIO, Persona.Afiliacion.BENEFICIARIO),
                    Arguments.of(PersonaDTO.Afiliacion.NO_AFILIADO, Persona.Afiliacion.NO_AFILIADO)
                            )
        }
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(PersonaDTOPruebas.jsonDeserializacionPorDefecto, PersonaDTO::class.java)
            assertEquals(PersonaDTOPruebas.entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(PersonaDTOPruebas.jsonConNulos, PersonaDTO::class.java)
            assertEquals(PersonaDTOPruebas.entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(PersonaDTOPruebas.jsonSinNulos, PersonaDTO::class.java)
            assertEquals(PersonaDTOPruebas.entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_nombre_completo()
            {
                val jsonSinNombreCompleto = """
            {
                "client-id": 0,
                "id": 0,
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNombreCompleto, PersonaDTO::class.java) })
            }

            @Test
            fun con_nombre_completo_null()
            {
                val jsonConNombreCompletoNull = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": null,
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNombreCompletoNull, PersonaDTO::class.java) })
            }

            @Test
            fun sin_tipo_de_documento()
            {
                val jsonSinTipoDeDocumento = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinTipoDeDocumento, PersonaDTO::class.java) })
            }

            @Test
            fun con_tipo_de_documento_null()
            {
                val jsonConTipoDeDocumentoNull = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": null,
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConTipoDeDocumentoNull, PersonaDTO::class.java) })
            }

            @Test
            fun sin_numero_de_documento()
            {
                val jsonSinNumeroDeDocumento = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNumeroDeDocumento, PersonaDTO::class.java) })
            }

            @Test
            fun con_numero_de_documento_null()
            {
                val jsonConNumeroDeDocumentoNull = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": null,
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNumeroDeDocumentoNull, PersonaDTO::class.java) })
            }

            @Test
            fun sin_genero()
            {
                val jsonSinGenero = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinGenero, PersonaDTO::class.java) })
            }

            @Test
            fun con_genero_null()
            {
                val jsonConGeneroNull = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": null,
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConGeneroNull, PersonaDTO::class.java) })
            }

            @Test
            fun sin_fecha_de_nacimiento()
            {
                val jsonSinFechaDeNacimiento = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinFechaDeNacimiento, PersonaDTO::class.java) })
            }

            @Test
            fun con_fecha_de_nacimiento_null()
            {
                val jsonConFechaDeNacimientoNull = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": null,
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConFechaDeNacimientoNull, PersonaDTO::class.java) })
            }

            @Test
            fun sin_categoria()
            {
                val jsonSinCategoria = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinCategoria, PersonaDTO::class.java) })
            }

            @Test
            fun con_categoria_null()
            {
                val jsonConCategoriaNull = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": null,
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConCategoriaNull, PersonaDTO::class.java) })
            }

            @Test
            fun sin_afiliacion()
            {
                val jsonSinAfiliacion = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinAfiliacion, PersonaDTO::class.java) })
            }

            @Test
            fun con_afiliacion_null()
            {
                val jsonConAfiliacionNull = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": null,
                "is-anonymous": false,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConAfiliacionNull, PersonaDTO::class.java) })
            }

            @Test
            fun sin_es_anonima()
            {
                val jsonSinEsAnonima = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinEsAnonima, PersonaDTO::class.java) })
            }

            @Test
            fun con_es_anonima_null()
            {
                val jsonConEsAnonimaNull = """
            {
                "client-id": 0,
                "id": 0,
                "full-name": "Por Defecto",
                "document-type": "CC",
                "document-number": "Por Defecto",
                "gender": "UNKNOWN",
                "birthdate": "2017-05-31",
                "category": "A",
                "affiliation": "CONTRIBUTOR",
                "is-anonymous": null,
                "image-key": "llave"
            }
                """
                Assertions.assertThrows(JsonMappingException::class.java, { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConEsAnonimaNull, PersonaDTO::class.java) })
            }
        }

        @DisplayName("Cuando el tipo de documento")
        @ParameterizedTest(name = "Es ''{1}'' en el json asigna ''{0}'' en el DTO")
        @ArgumentsSource(ProveedorTipoDocumentoDTOConTipoDocumentoEnJson::class)
        fun paraTipoDocumento(tipoDeDocumentoDTO: PersonaDTO.TipoDocumento, tipoDeDocumentoEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(tipoDocumento = tipoDeDocumentoDTO)
            val json = darJsonSegunEnumeraciones(tipoDocumento = tipoDeDocumentoEnJson)
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(json, PersonaDTO::class.java)
            assertEquals(entidadDTO, entidadDeserializada)
        }

        @DisplayName("Cuando el género")
        @ParameterizedTest(name = "Es ''{1}'' en el json asigna ''{0}'' en el DTO")
        @ArgumentsSource(ProveedorGeneroDTOConGeneroEnJson::class)
        fun paraGenero(generoDTO: PersonaDTO.Genero, generoEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(genero = generoDTO)
            val json = darJsonSegunEnumeraciones(genero = generoEnJson)
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(json, PersonaDTO::class.java)
            assertEquals(entidadDTO, entidadDeserializada)
        }

        @DisplayName("Cuando la categoría")
        @ParameterizedTest(name = "Es ''{1}'' en el json asigna ''{0}'' en el DTO")
        @ArgumentsSource(ProveedorCategoriaDTOConCategoriaEnJson::class)
        fun paraCategoria(categoriaDTO: PersonaDTO.Categoria, categoriaEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(categoria = categoriaDTO)
            val json = darJsonSegunEnumeraciones(categoria = categoriaEnJson)
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(json, PersonaDTO::class.java)
            assertEquals(entidadDTO, entidadDeserializada)
        }

        @DisplayName("Cuando la afiliación")
        @ParameterizedTest(name = "Es ''{1}'' en el json asigna ''{0}'' en el DTO")
        @ArgumentsSource(ProveedorAfiliacionDTOConAfiliacionEnJson::class)
        fun paraAfiliacion(afiliacionDTO: PersonaDTO.Afiliacion, afiliacionEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(afiliacion = afiliacionDTO)
            val json = darJsonSegunEnumeraciones(afiliacion = afiliacionEnJson)
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(json, PersonaDTO::class.java)
            assertEquals(entidadDTO, entidadDeserializada)
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun de_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(PersonaDTOPruebas.entidadDTOPorDefecto)

            JSONAssert.assertEquals(PersonaDTOPruebas.jsonSerializacionPorDefectoEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun con_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(PersonaDTOPruebas.entidadDTOConNulos)
            JSONAssert.assertEquals(PersonaDTOPruebas.jsonConNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun sin_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(PersonaDTOPruebas.entidadDTOSinNulos)
            JSONAssert.assertEquals(PersonaDTOPruebas.jsonSinNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @DisplayName("Cuando el tipo de documento")
        @ParameterizedTest(name = "Es ''{1}'' en el DTO asigna ''{0}'' en el json")
        @ArgumentsSource(ProveedorTipoDocumentoDTOConTipoDocumentoEnJson::class)
        fun paraTipoDocumento(tipoDeDocumentoDTO: PersonaDTO.TipoDocumento, tipoDeDocumentoEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(tipoDocumento = tipoDeDocumentoDTO)
            val jsonEsperado = darJsonSegunEnumeraciones(tipoDocumento = tipoDeDocumentoEnJson)

            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

            JSONAssert.assertEquals(jsonEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @DisplayName("Cuando el género")
        @ParameterizedTest(name = "Es ''{1}'' en el DTO asigna ''{0}'' en el json")
        @ArgumentsSource(ProveedorGeneroDTOConGeneroEnJson::class)
        fun paraGenero(generoDTO: PersonaDTO.Genero, generoEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(genero = generoDTO)
            val jsonEsperado = darJsonSegunEnumeraciones(genero = generoEnJson)

            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

            JSONAssert.assertEquals(jsonEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @DisplayName("Cuando la categoría")
        @ParameterizedTest(name = "Es ''{1}'' en el DTO asigna ''{0}'' en el json")
        @ArgumentsSource(ProveedorCategoriaDTOConCategoriaEnJson::class)
        fun paraCategoria(categoriaDTO: PersonaDTO.Categoria, categoriaEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(categoria = categoriaDTO)
            val jsonEsperado = darJsonSegunEnumeraciones(categoria = categoriaEnJson)

            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)

            JSONAssert.assertEquals(jsonEsperado, entidadSerializada, JSONCompareMode.STRICT)
        }

        @DisplayName("Cuando la afiliación")
        @ParameterizedTest(name = "Es ''{1}'' en el DTO asigna ''{0}'' en el json")
        @ArgumentsSource(ProveedorAfiliacionDTOConAfiliacionEnJson::class)
        fun paraAfiliacion(afiliacionDTO: PersonaDTO.Afiliacion, afiliacionEnJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(afiliacion = afiliacionDTO)
            val jsonEsperado = darJsonSegunEnumeraciones(afiliacion = afiliacionEnJson)

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
            fun con_valores_por_defecto_se_transforma_correctamente()
            {
                val entidadDeNegocio = PersonaDTOPruebas.entidadDTOPorDefecto.aEntidadDeNegocio()
                assertEquals(PersonaDTOPruebas.entidadNegocioPorDefecto, entidadDeNegocio)
            }

            @Test
            fun con_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = PersonaDTOPruebas.entidadDTOConNulos.aEntidadDeNegocio()
                assertEquals(PersonaDTOPruebas.entidadNegocioConNulos, entidadDeNegocio)
            }

            @Test
            fun sin_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = PersonaDTOPruebas.entidadDTOSinNulos.aEntidadDeNegocio()
                assertEquals(PersonaDTOPruebas.entidadNegocioSinNulos, entidadDeNegocio)
            }

            @DisplayName("Cuando el tipo de documento")
            @ParameterizedTest(name = "Es ''{0}'' en el DTO asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorTipoDocumentoDTOConTipoDocumentoNegocio::class)
            fun paraTipoDocumento(tipoDeDocumentoDTO: PersonaDTO.TipoDocumento, tipoDeDocumento: Persona.TipoDocumento)
            {
                val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(tipoDocumento = tipoDeDocumentoDTO)
                val entidaComoNegocio = entidadDTO.aEntidadDeNegocio()
                assertEquals(tipoDeDocumento, entidaComoNegocio.tipoDocumento)
            }

            @DisplayName("Cuando el género")
            @ParameterizedTest(name = "Es ''{0}'' en el DTO asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorGeneroDTOConGeneroNegocio::class)
            fun paraGenero(generoDTO: PersonaDTO.Genero, genero: Persona.Genero)
            {
                val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(genero = generoDTO)
                val entidaComoNegocio = entidadDTO.aEntidadDeNegocio()
                assertEquals(genero, entidaComoNegocio.genero)
            }

            @DisplayName("Cuando la categoría")
            @ParameterizedTest(name = "Es ''{0}'' en el DTO asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorCategoriaDTOConCategoriaNegocio::class)
            fun paraCategoria(categoriaDTO: PersonaDTO.Categoria, categoria: Persona.Categoria)
            {
                val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(categoria = categoriaDTO)
                val entidaComoNegocio = entidadDTO.aEntidadDeNegocio()
                assertEquals(categoria, entidaComoNegocio.categoria)
            }

            @DisplayName("Cuando la afiliación")
            @ParameterizedTest(name = "Es ''{0}'' en el DTO asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorAfiliacionDTOConAfiliacionNegocio::class)
            fun paraAfiliacion(afiliacionDTO: PersonaDTO.Afiliacion, afiliacion: Persona.Afiliacion)
            {
                val entidadDTO = darEntidadDTOPruebaSegunEnumeraciones(afiliacion = afiliacionDTO)
                val entidaComoNegocio = entidadDTO.aEntidadDeNegocio()
                assertEquals(afiliacion, entidaComoNegocio.afiliacion)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            private fun darEntidadPruebaSegunEnumeraciones(
                    tipoDocumento: Persona.TipoDocumento? = null,
                    genero: Persona.Genero? = null,
                    categoria: Persona.Categoria? = null,
                    afiliacion: Persona.Afiliacion? = null)
                    : Persona
            {
                return Persona(
                        0,
                        0,
                        "Por Defecto",
                        tipoDocumento ?: Persona.TipoDocumento.CC,
                        "Por Defecto",
                        genero ?: Persona.Genero.DESCONOCIDO,
                        LocalDate.of(2017, 5, 31),
                        categoria ?: Persona.Categoria.A,
                        afiliacion ?: Persona.Afiliacion.COTIZANTE,
                        false,
                        "llave",
                        "Por Defecto",
                        "0",
                        Persona.Tipo.NO_AFILIADO
                              )
            }

            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = PersonaDTO(PersonaDTOPruebas.entidadNegocioPorDefecto)
                assertEquals(PersonaDTOPruebas.entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = PersonaDTO(PersonaDTOPruebas.entidadNegocioConNulos)
                assertEquals(PersonaDTOPruebas.entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = PersonaDTO(PersonaDTOPruebas.entidadNegocioSinNulos)
                assertEquals(PersonaDTOPruebas.entidadDTOSinNulos, entidadDTO)
            }

            @DisplayName("Cuando el tipo de documento")
            @ParameterizedTest(name = "Es ''{1}'' en el negocio asigna ''{0}'' en el DTO")
            @ArgumentsSource(ProveedorTipoDocumentoDTOConTipoDocumentoNegocio::class)
            fun paraTipoDocumento(tipoDeDocumentoDTO: PersonaDTO.TipoDocumento, tipoDeDocumento: Persona.TipoDocumento)
            {
                val entidadNegocio = darEntidadPruebaSegunEnumeraciones(tipoDocumento = tipoDeDocumento)
                val entidadComoDTO = PersonaDTO(entidadNegocio)
                assertEquals(tipoDeDocumentoDTO, entidadComoDTO.tipoDocumento)
            }

            @DisplayName("Cuando el género")
            @ParameterizedTest(name = "Es ''{1}'' en el negocio asigna ''{0}'' en el DTO")
            @ArgumentsSource(ProveedorGeneroDTOConGeneroNegocio::class)
            fun paraGenero(generoDTO: PersonaDTO.Genero, genero: Persona.Genero)
            {
                val entidadNegocio = darEntidadPruebaSegunEnumeraciones(genero = genero)
                val entidadComoDTO = PersonaDTO(entidadNegocio)
                assertEquals(generoDTO, entidadComoDTO.genero)
            }

            @DisplayName("Cuando la categoría")
            @ParameterizedTest(name = "Es ''{1}'' en el negocio asigna ''{0}'' en el DTO")
            @ArgumentsSource(ProveedorCategoriaDTOConCategoriaNegocio::class)
            fun paraCategoria(categoriaDTO: PersonaDTO.Categoria, categoria: Persona.Categoria)
            {
                val entidadNegocio = darEntidadPruebaSegunEnumeraciones(categoria = categoria)
                val entidadComoDTO = PersonaDTO(entidadNegocio)
                assertEquals(categoriaDTO, entidadComoDTO.categoria)
            }

            @DisplayName("Cuando la afiliación")
            @ParameterizedTest(name = "Es ''{1}'' en el negocio asigna ''{0}'' en el DTO")
            @ArgumentsSource(ProveedorAfiliacionDTOConAfiliacionNegocio::class)
            fun paraAfiliacion(afiliacionDTO: PersonaDTO.Afiliacion, afiliacion: Persona.Afiliacion)
            {
                val entidadNegocio = darEntidadPruebaSegunEnumeraciones(afiliacion = afiliacion)
                val entidadComoDTO = PersonaDTO(entidadNegocio)
                assertEquals(afiliacionDTO, entidadComoDTO.afiliacion)
            }
        }
    }
}