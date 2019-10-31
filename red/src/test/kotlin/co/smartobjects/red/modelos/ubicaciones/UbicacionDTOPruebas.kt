package co.smartobjects.red.modelos.ubicaciones

import co.smartobjects.entidades.ubicaciones.Ubicacion
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


@DisplayName("UbicacionDTO")
internal class UbicacionDTOPruebas
{
    companion object
    {
        private const val jsonDeserializacionPorDefecto = """
            {
                "name": "Por Defecto",
                "type": "AREA",
                "subtype": "AP",
                "parent-location-id": 0
            }
            """

        private const val jsonSerializacionPorDefectoEsperado = """
            {
                "client-id": 0,
                "id": null,
                "name": "Por Defecto",
                "type": "AREA",
                "subtype": "AP",
                "parent-location-id": 0,
                "ancestors-ids": []
            }
            """

        private val entidadDTOPorDefecto = UbicacionDTO(
                0,
                null,
                "Por Defecto",
                UbicacionDTO.Tipo.AREA,
                UbicacionDTO.Subtipo.AP,
                0,
                listOf()
                                                       )

        private val entidadNegocioPorDefecto = Ubicacion(
                0,
                null,
                "Por Defecto",
                Ubicacion.Tipo.AREA,
                Ubicacion.Subtipo.AP,
                0,
                linkedSetOf()
                                                        )

        private const val jsonConNulos = """
            {
                "client-id": 0,
                "id": null,
                "name": "Con Nulos",
                "type": "AREA",
                "subtype": "AP",
                "parent-location-id": null,
                "ancestors-ids": []
            }
            """

        private val entidadDTOConNulos = UbicacionDTO(
                0,
                null,
                "Con Nulos",
                UbicacionDTO.Tipo.AREA,
                UbicacionDTO.Subtipo.AP,
                null,
                listOf()
                                                     )

        private val entidadNegocioConNulos = Ubicacion(
                0,
                null,
                "Con Nulos",
                Ubicacion.Tipo.AREA,
                Ubicacion.Subtipo.AP,
                null,
                linkedSetOf()
                                                      )


        private const val jsonSinNulos = """
            {
                "client-id": 1,
                "id": 1,
                "name": "Entidad normal",
                "type": "PROPERTY",
                "subtype": "WIRELESS AP",
                "parent-location-id": 8,
                "ancestors-ids": [2,3,4]
            }
            """

        private val entidadDTOSinNulos = UbicacionDTO(
                1,
                1,
                "Entidad normal",
                UbicacionDTO.Tipo.PROPIEDAD,
                UbicacionDTO.Subtipo.AP_INALAMBRICO,
                8,
                listOf(2, 3, 4)
                                                     )

        private val entidadNegocioSinNulos = Ubicacion(
                1,
                1,
                "Entidad normal",
                Ubicacion.Tipo.PROPIEDAD,
                Ubicacion.Subtipo.AP_INALAMBRICO,
                8,
                linkedSetOf<Long>(2, 3, 4)
                                                      )

    }

    private fun darJsonSegunTipoYSubTipoDeUbicacion(tipo: String, subtipo: String): String
    {
        return """
                {
                    "client-id": 1,
                    "id": null,
                    "name": "Ubicacion prueba",
                    "type": "$tipo",
                    "subtype": "$subtipo",
                    "parent-location-id": null,
                    "ancestors-ids": []
                }
                """
    }

    private fun darEntidadDTOPruebaSegunTipoYSubTipoDeUbicacion(tipo: UbicacionDTO.Tipo, subtipo: UbicacionDTO.Subtipo): UbicacionDTO
    {
        return UbicacionDTO(
                1,
                null,
                "Ubicacion prueba",
                tipo,
                subtipo,
                null,
                listOf()
                           )
    }

    internal class ProveedorTipoUbicacionDTOConTipoUbicacionEnJson : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(UbicacionDTO.Tipo.AREA, "AREA"),
                    Arguments.of(UbicacionDTO.Tipo.CIUDAD, "CITY"),
                    Arguments.of(UbicacionDTO.Tipo.PAIS, "COUNTRY"),
                    Arguments.of(UbicacionDTO.Tipo.PROPIEDAD, "PROPERTY", "AP"),
                    Arguments.of(UbicacionDTO.Tipo.PUNTO_DE_CONTACTO, "CONTACT POINT"),
                    Arguments.of(UbicacionDTO.Tipo.PUNTO_DE_INTERES, "POINT OF INTEREST"),
                    Arguments.of(UbicacionDTO.Tipo.REGION, "REGION"),
                    Arguments.of(UbicacionDTO.Tipo.ZONA, "ZONE")
                            )
        }
    }

    internal class ProveedorSubTipoUbicacionDTOConSubTipoUbicacionEnJson : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(UbicacionDTO.Subtipo.AP, "AP"),
                    Arguments.of(UbicacionDTO.Subtipo.AP_INALAMBRICO, "WIRELESS AP"),
                    Arguments.of(UbicacionDTO.Subtipo.AP_RESTRINGIDO, "RESTRICTED AP"),
                    Arguments.of(UbicacionDTO.Subtipo.KIOSKO, "KIOSK"),
                    Arguments.of(UbicacionDTO.Subtipo.POS, "POS"),
                    Arguments.of(UbicacionDTO.Subtipo.POS_SIN_DINERO, "CASHLESS POS")
                            )
        }
    }

    internal class ProveedorTipoUbicacionDTOConTipoNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(UbicacionDTO.Tipo.AREA, Ubicacion.Tipo.AREA),
                    Arguments.of(UbicacionDTO.Tipo.CIUDAD, Ubicacion.Tipo.CIUDAD),
                    Arguments.of(UbicacionDTO.Tipo.PAIS, Ubicacion.Tipo.PAIS),
                    Arguments.of(UbicacionDTO.Tipo.PROPIEDAD, Ubicacion.Tipo.PROPIEDAD),
                    Arguments.of(UbicacionDTO.Tipo.PUNTO_DE_CONTACTO, Ubicacion.Tipo.PUNTO_DE_CONTACTO),
                    Arguments.of(UbicacionDTO.Tipo.PUNTO_DE_INTERES, Ubicacion.Tipo.PUNTO_DE_INTERES),
                    Arguments.of(UbicacionDTO.Tipo.REGION, Ubicacion.Tipo.REGION),
                    Arguments.of(UbicacionDTO.Tipo.ZONA, Ubicacion.Tipo.ZONA)
                            )
        }
    }

    internal class ProveedorSubTipoUbicacionDTOConSubTipoNegocio : ArgumentsProvider
    {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
        {
            return Stream.of(
                    Arguments.of(UbicacionDTO.Subtipo.AP, Ubicacion.Subtipo.AP),
                    Arguments.of(UbicacionDTO.Subtipo.AP_INALAMBRICO, Ubicacion.Subtipo.AP_INALAMBRICO),
                    Arguments.of(UbicacionDTO.Subtipo.AP_RESTRINGIDO, Ubicacion.Subtipo.AP_RESTRINGIDO),
                    Arguments.of(UbicacionDTO.Subtipo.KIOSKO, Ubicacion.Subtipo.KIOSKO),
                    Arguments.of(UbicacionDTO.Subtipo.POS, Ubicacion.Subtipo.POS),
                    Arguments.of(UbicacionDTO.Subtipo.POS_SIN_DINERO, Ubicacion.Subtipo.POS_SIN_DINERO)
                            )
        }
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, UbicacionDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, UbicacionDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, UbicacionDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_nombre()
            {
                val jsonSinNombre = """
                {
                    "client-id": 1,
                    "id": 1,
                    "type": "PROPERTY",
                    "subtype": "WIRELESS AP",
                    "parent-location-id": 8,
                    "ancestors-ids": [2,3,4]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNombre, UbicacionDTO::class.java) }
            }

            @Test
            fun con_nombre_null()
            {
                val jsonConNombreNull = """
                {
                    "client-id": 1,
                    "id": 1,
                    "name": null,
                    "type": "PROPERTY",
                    "subtype": "WIRELESS AP",
                    "parent-location-id": 8,
                    "ancestors-ids": [2,3,4]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNombreNull, UbicacionDTO::class.java) }
            }

            @Test
            fun sin_tipo()
            {
                val jsonSinTipo = """
                {
                    "client-id": 1,
                    "id": 1,
                    "name": "Entidad normal",
                    "subtype": "WIRELESS AP",
                    "parent-location-id": 8,
                    "ancestors-ids": [2,3,4]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinTipo, UbicacionDTO::class.java) }
            }

            @Test
            fun con_tipo_null()
            {
                val jsonConTipoNull = """
                {
                    "client-id": 1,
                    "id": 1,
                    "name": "Entidad normal",
                    "type": null,
                    "subtype": "WIRELESS AP",
                    "parent-location-id": 8,
                    "ancestors-ids": [2,3,4]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConTipoNull, UbicacionDTO::class.java) }
            }

            @Test
            fun con_tipo_incorrecto()
            {
                val jsonConTipoNull = """
                {
                    "client-id": 1,
                    "id": 1,
                    "name": "Entidad normal",
                    "type": "VALOR INCORRECTO",
                    "subtype": "WIRELESS AP",
                    "parent-location-id": 8,
                    "ancestors-ids": [2,3,4]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConTipoNull, UbicacionDTO::class.java) }
            }

            @Test
            fun sin_subtipo()
            {
                val jsonSinSubtipo = """
                {
                    "client-id": 1,
                    "id": 1,
                    "name": "Entidad normal",
                    "type": "PROPERTY",
                    "parent-location-id": 8,
                    "ancestors-ids": [2,3,4]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinSubtipo, UbicacionDTO::class.java) }
            }

            @Test
            fun con_subtipo_null()
            {
                val jsonConSubtipoNull = """
                {
                    "client-id": 1,
                    "id": 1,
                    "name": "Entidad normal",
                    "type": "PROPERTY",
                    "subtype": null,
                    "parent-location-id": 8,
                    "ancestors-ids": [2,3,4]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConSubtipoNull, UbicacionDTO::class.java) }
            }

            @Test
            fun con_subtipo_incorrecto()
            {
                val jsonConSubtipoNull = """
                {
                    "client-id": 1,
                    "id": 1,
                    "name": "Entidad normal",
                    "type": "PROPERTY",
                    "subtype": "VALOR INCORRECTO",
                    "parent-location-id": 8,
                    "ancestors-ids": [2,3,4]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConSubtipoNull, UbicacionDTO::class.java) }
            }

            @Test
            fun sin_id_padre()
            {
                val jsonSinIdPadre = """
                {
                    "client-id": 1,
                    "id": 1,
                    "name": "Entidad normal",
                    "type": "PROPERTY",
                    "subtype": "WIRELESS AP",
                    "ancestors-ids": [2,3,4]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinIdPadre, UbicacionDTO::class.java) }
            }
        }

        @DisplayName("Cuando el Tipo de Ubicación")
        @ParameterizedTest(name = "Es ''{1}'' en el json asigna ''{0}'' en el DTO")
        @ArgumentsSource(ProveedorTipoUbicacionDTOConTipoUbicacionEnJson::class)
        fun paraTipoUbicacion(tipoDTO: UbicacionDTO.Tipo, tipoJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunTipoYSubTipoDeUbicacion(tipoDTO, UbicacionDTO.Subtipo.AP)
            val json = darJsonSegunTipoYSubTipoDeUbicacion(tipoJson, UbicacionDTO.Subtipo.AP.valorEnRed)
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(json, UbicacionDTO::class.java)
            assertEquals(entidadDTO, entidadDeserializada)
        }

        @DisplayName("Cuando el SubTipo de Ubicación")
        @ParameterizedTest(name = "Es ''{1}'' en el json asigna ''{0}'' en el DTO")
        @ArgumentsSource(ProveedorSubTipoUbicacionDTOConSubTipoUbicacionEnJson::class)
        fun paraSubTipoUbicacion(subtipoDTO: UbicacionDTO.Subtipo, subtipoJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunTipoYSubTipoDeUbicacion(UbicacionDTO.Tipo.AREA, subtipoDTO)
            val json = darJsonSegunTipoYSubTipoDeUbicacion(UbicacionDTO.Tipo.AREA.valorEnRed, subtipoJson)
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(json, UbicacionDTO::class.java)
            assertEquals(entidadDTO, entidadDeserializada)
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
        fun con_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOConNulos)
            JSONAssert.assertEquals(jsonConNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @Test
        fun sin_nulos_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOSinNulos)
            JSONAssert.assertEquals(jsonSinNulos, entidadSerializada, JSONCompareMode.STRICT)
        }

        @DisplayName("Cuando el Tipo de Ubicación")
        @ParameterizedTest(name = "Es ''{0}'' en el dto asigna ''{1}'' en el json")
        @ArgumentsSource(ProveedorTipoUbicacionDTOConTipoUbicacionEnJson::class)
        fun paraTipoUbicacion(tipoDTO: UbicacionDTO.Tipo, tipoJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunTipoYSubTipoDeUbicacion(tipoDTO, UbicacionDTO.Subtipo.AP)
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)
            val json = darJsonSegunTipoYSubTipoDeUbicacion(tipoJson, UbicacionDTO.Subtipo.AP.valorEnRed)
            JSONAssert.assertEquals(json, entidadSerializada, JSONCompareMode.STRICT)
        }

        @DisplayName("Cuando el SubTipo de Ubicación")
        @ParameterizedTest(name = "Es ''{0}'' en el dto asigna ''{1}'' en el json")
        @ArgumentsSource(ProveedorSubTipoUbicacionDTOConSubTipoUbicacionEnJson::class)
        fun paraSubTipoUbicacion(subtipoDTO: UbicacionDTO.Subtipo, subtipoJson: String)
        {
            val entidadDTO = darEntidadDTOPruebaSegunTipoYSubTipoDeUbicacion(UbicacionDTO.Tipo.AREA, subtipoDTO)
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTO)
            val json = darJsonSegunTipoYSubTipoDeUbicacion(UbicacionDTO.Tipo.AREA.valorEnRed, subtipoJson)
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
                val entidadDeNegocio = entidadDTOPorDefecto.aEntidadDeNegocio()
                assertEquals(entidadNegocioPorDefecto, entidadDeNegocio)
            }

            @Test
            fun con_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = entidadDTOConNulos.aEntidadDeNegocio()
                assertEquals(entidadNegocioConNulos, entidadDeNegocio)
            }

            @Test
            fun sin_nulos_se_transforma_correctamente()
            {
                val entidadDeNegocio = entidadDTOSinNulos.aEntidadDeNegocio()
                assertEquals(entidadNegocioSinNulos, entidadDeNegocio)
            }

            @DisplayName("Cuando el Tipo de Ubicación")
            @ParameterizedTest(name = "Es ''{0}'' en el dto asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorTipoUbicacionDTOConTipoNegocio::class)
            fun paraTipoUbicacion(tipoDTO: UbicacionDTO.Tipo, tipoNegocio: Ubicacion.Tipo)
            {
                val entidadDTO = darEntidadDTOPruebaSegunTipoYSubTipoDeUbicacion(tipoDTO, UbicacionDTO.Subtipo.AP)
                val entidadNegocio = entidadDTO.aEntidadDeNegocio()
                assertEquals(tipoNegocio, entidadNegocio.tipo)
            }

            @DisplayName("Cuando el SubTipo de Ubicación")
            @ParameterizedTest(name = "Es ''{0}'' en el dto asigna ''{1}'' en negocio")
            @ArgumentsSource(ProveedorSubTipoUbicacionDTOConSubTipoNegocio::class)
            fun paraSubTipoUbicacion(subtipoDTO: UbicacionDTO.Subtipo, subtipoNegocio: Ubicacion.Subtipo)
            {
                val entidadDTO = darEntidadDTOPruebaSegunTipoYSubTipoDeUbicacion(UbicacionDTO.Tipo.AREA, subtipoDTO)
                val entidadNegocio = entidadDTO.aEntidadDeNegocio()
                assertEquals(subtipoNegocio, entidadNegocio.subtipo)
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            private fun darEntidadNegocioPruebaSegunTipoYSubTipoDeUbicacion(tipo: Ubicacion.Tipo, subtipo: Ubicacion.Subtipo): Ubicacion
            {
                return Ubicacion(
                        1,
                        null,
                        "Ubicacion prueba",
                        tipo,
                        subtipo,
                        null,
                        linkedSetOf()
                                )
            }

            @Test
            fun con_valores_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = UbicacionDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = UbicacionDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = UbicacionDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }

            @DisplayName("Cuando el Tipo de Ubicación")
            @ParameterizedTest(name = "Es ''{0}'' en negocio asigna ''{1}'' en el dto")
            @ArgumentsSource(ProveedorTipoUbicacionDTOConTipoNegocio::class)
            fun paraTipoUbicacion(tipoDTO: UbicacionDTO.Tipo, tipoNegocio: Ubicacion.Tipo)
            {
                val entidadNegocio = darEntidadNegocioPruebaSegunTipoYSubTipoDeUbicacion(tipoNegocio, Ubicacion.Subtipo.AP)
                val entidadDAOCreando = UbicacionDTO(entidadNegocio)
                assertEquals(tipoDTO, entidadDAOCreando.tipo)
            }

            @DisplayName("Cuando el SubTipo de Ubicación")
            @ParameterizedTest(name = "Es ''{1}'' en negocio asigna ''{1}'' en el dto")
            @ArgumentsSource(ProveedorSubTipoUbicacionDTOConSubTipoNegocio::class)
            fun paraSubTipoUbicacion(subtipoDTO: UbicacionDTO.Subtipo, subtipoNegocio: Ubicacion.Subtipo)
            {
                val entidadNegocio = darEntidadNegocioPruebaSegunTipoYSubTipoDeUbicacion(Ubicacion.Tipo.AREA, subtipoNegocio)
                val entidadDAOCreando = UbicacionDTO(entidadNegocio)
                assertEquals(subtipoDTO, entidadDAOCreando.subtipo)
            }
        }
    }
}