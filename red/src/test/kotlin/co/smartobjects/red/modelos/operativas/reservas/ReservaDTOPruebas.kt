package co.smartobjects.red.modelos.operativas.reservas

import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.red.ConfiguracionJackson
import com.fasterxml.jackson.databind.JsonMappingException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.util.*
import kotlin.test.assertEquals


@DisplayName("ReservaDTO")
internal class ReservaDTOPruebas
{
    companion object
    {
        private val UUID_DEFECTO = UUID.randomUUID()
        private const val TIEMPO_CREACION_DEFECTO = 1234567L
        private const val NOMBRE_USUARIO_DEFECTO = "Usuario"
        private val ID_RESERVA_DEFECTO = EntidadTransaccional.PartesId(TIEMPO_CREACION_DEFECTO, NOMBRE_USUARIO_DEFECTO, UUID_DEFECTO).id

        private val jsonDeserializacionPorDefecto = """
            {
                "id": "$ID_RESERVA_DEFECTO",
                "tag-sessions": [
                    ${SesionDeManillaDTOPruebas.jsonDeserializacionPorDefecto}
                ]
            }
            """

        private val jsonPorDefecto = """
            {
                "client-id": 0,
                "id": "$ID_RESERVA_DEFECTO",
                "committed": false,
                "reservation-number": null,
                "tag-sessions": [
                    ${SesionDeManillaDTOPruebas.jsonPorDefecto}
                ]
            }
            """

        private val entidadDTOPorDefecto =
                ReservaDTO(
                        0,
                        ID_RESERVA_DEFECTO,
                        false,
                        null,
                        listOf(
                                SesionDeManillaDTOPruebas.entidadDTOPorDefecto
                              )
                          )

        private val entidadNegocioPorDefecto =
                Reserva(
                        0,
                        NOMBRE_USUARIO_DEFECTO,
                        UUID_DEFECTO,
                        TIEMPO_CREACION_DEFECTO,
                        false,
                        null,
                        listOf(
                                SesionDeManillaDTOPruebas.entidadNegocioPorDefecto
                              )
                       )

        private val jsonConNulos = """
            {
                "client-id": 1,
                "id": "$ID_RESERVA_DEFECTO",
                "committed": true,
                "reservation-number": null,
                "tag-sessions": [
                    ${SesionDeManillaDTOPruebas.jsonConNulos}
                ]
            }
            """

        private val entidadDTOConNulos =
                ReservaDTO(
                        1,
                        ID_RESERVA_DEFECTO,
                        true,
                        null,
                        listOf(
                                SesionDeManillaDTOPruebas.entidadDTOConNulos
                              )
                          )

        private val entidadNegocioConNulos =
                Reserva(
                        1,
                        NOMBRE_USUARIO_DEFECTO,
                        UUID_DEFECTO,
                        TIEMPO_CREACION_DEFECTO,
                        true,
                        null,
                        listOf(
                                SesionDeManillaDTOPruebas.entidadNegocioConNulos
                              )
                       )

        private val jsonSinNulos = """
            {
                "client-id": 1,
                "id": "$ID_RESERVA_DEFECTO",
                "committed": true,
                "reservation-number": 1,
                "tag-sessions": [
                    ${SesionDeManillaDTOPruebas.jsonSinNulos}
                ]
            }
            """

        private val entidadDTOSinNulos =
                ReservaDTO(
                        1,
                        ID_RESERVA_DEFECTO,
                        true,
                        1,
                        listOf(
                                SesionDeManillaDTOPruebas.entidadDTOSinNulos
                              )
                          )

        private val entidadNegocioSinNulos =
                Reserva(
                        1,
                        NOMBRE_USUARIO_DEFECTO,
                        UUID_DEFECTO,
                        TIEMPO_CREACION_DEFECTO,
                        true,
                        1,
                        listOf(
                                SesionDeManillaDTOPruebas.entidadNegocioSinNulos
                              )
                       )
    }


    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, ReservaDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_con_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonConNulos, ReservaDTO::class.java)
            assertEquals(entidadDTOConNulos, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, ReservaDTO::class.java)
            assertEquals(entidadDTOSinNulos, entidadDeserializada)
        }

        @[Nested DisplayName("lanza excepción con un json")]
        inner class LanzaExcepcion
        {
            @Test
            fun sin_id()
            {
                val json = """
                    {
                        "client-id": 1,
                        "committed": true,
                        "reservation-number": 1,
                        "tag-sessions": [
                            ${SesionDeManillaDTOPruebas.jsonSinNulos}
                        ]
                    }
                    """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReservaDTO::class.java) }
            }

            @Test
            fun con_id_null()
            {
                val json = """
                    {
                        "client-id": 1,
                        "id": null,
                        "committed": true,
                        "reservation-number": 1,
                        "tag-sessions": [
                            ${SesionDeManillaDTOPruebas.jsonSinNulos}
                        ]
                    }
                    """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReservaDTO::class.java) }
            }

            @Test
            fun sin_sesiones_de_manilla()
            {
                val json = """
                    {
                        "client-id": 1,
                        "id": "$ID_RESERVA_DEFECTO",
                        "committed": true,
                        "reservation-number": 1
                    }
                    """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReservaDTO::class.java) }
            }

            @Test
            fun con_sesiones_de_manilla_null()
            {
                val json = """
                    {
                        "client-id": 1,
                        "id": "$ID_RESERVA_DEFECTO",
                        "committed": true,
                        "reservation-number": 1,
                        "tag-sessions": null
                    }
                    """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReservaDTO::class.java) }
            }
        }

        @[Nested DisplayName("no lanza excepción con un json")]
        inner class NoLanzaExcepcion
        {
            @Test
            fun sin_tag_creacion_terminada()
            {
                val json = """
                    {
                        "client-id": 1,
                        "id": "$ID_RESERVA_DEFECTO",
                        "reservation-number": 1,
                        "tag-sessions": [
                            ${SesionDeManillaDTOPruebas.jsonSinNulos}
                        ]
                    }
                    """
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReservaDTO::class.java)
            }

            @Test
            fun sin_numero_de_reserva()
            {
                val json = """
                    {
                        "client-id": 1,
                        "id": "$ID_RESERVA_DEFECTO",
                        "committed": true,
                        "tag-sessions": [
                            ${SesionDeManillaDTOPruebas.jsonSinNulos}
                        ]
                    }
                    """
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReservaDTO::class.java)
            }

            @Test
            fun con_numero_de_reserva_null()
            {
                val json = """
                    {
                        "client-id": 1,
                        "id": "$ID_RESERVA_DEFECTO",
                        "committed": true,
                        "reservation-number": null,
                        "tag-sessions": [
                            ${SesionDeManillaDTOPruebas.jsonSinNulos}
                        ]
                    }
                    """
                ConfiguracionJackson.objectMapperDeJackson.readValue(json, ReservaDTO::class.java)
            }
        }
    }

    @Nested
    inner class Serializacion
    {
        @Test
        fun con_valores_por_defecto_a_json_correctamente()
        {
            val entidadSerializada = ConfiguracionJackson.objectMapperDeJackson.writeValueAsString(entidadDTOPorDefecto)

            JSONAssert.assertEquals(jsonPorDefecto, entidadSerializada, JSONCompareMode.STRICT)
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
    }

    @Nested
    inner class Conversion
    {
        @Nested
        inner class AEntidadDeNegocio
        {
            @Test
            fun con_por_defecto_se_transforma_correctamente()
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

            @[Nested DisplayName("falla cuando")]
            inner class FallaCuando
            {
                @Test
                fun el_id_tiene_solo_dos_partes()
                {
                    val id = ID_RESERVA_DEFECTO.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).subList(0, 2).joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())
                    val entidadDTOPruebas = entidadDTOSinNulos.copy(id = id)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        entidadDTOPruebas.aEntidadDeNegocio()
                    }

                    assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun el_id_tiene_mas_de_tres_partes()
                {

                    val id = "$ID_RESERVA_DEFECTO${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}otra-parte"
                    val entidadDTOPruebas = entidadDTOSinNulos.copy(id = id)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        entidadDTOPruebas.aEntidadDeNegocio()
                    }

                    assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun el_tiempo_creacion_es_invalido()
                {
                    val partes = ID_RESERVA_DEFECTO.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
                    partes[0] = "tiempoInvalido"
                    val id = partes.joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())
                    val entidadDTOPruebas = entidadDTOSinNulos.copy(id = id)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        entidadDTOPruebas.aEntidadDeNegocio()
                    }

                    assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun el_uuid_es_invalido()
                {
                    val partes = ID_RESERVA_DEFECTO.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
                    partes[2] = "un-uuid-invalido"
                    val id = partes.joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())
                    val entidadDTOPruebas = entidadDTOSinNulos.copy(id = id)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        entidadDTOPruebas.aEntidadDeNegocio()
                    }

                    assertEquals(EntidadTransaccional.Campos.ID, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }
        }

        @Nested
        inner class DesdeEntidadDeNegocio
        {
            @Test
            fun con_por_defecto_se_construye_correctamente()
            {
                val entidadDTO = ReservaDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun con_nulos_se_construye_correctamente()
            {
                val entidadDTO = ReservaDTO(entidadNegocioConNulos)
                assertEquals(entidadDTOConNulos, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = ReservaDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}