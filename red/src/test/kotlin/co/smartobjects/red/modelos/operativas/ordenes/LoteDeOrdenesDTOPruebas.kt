package co.smartobjects.red.modelos.operativas.ordenes

import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
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

@DisplayName("LoteDeOrdenesDTO")
internal class LoteDeOrdenesDTOPruebas
{
    companion object
    {
        private val UUID_DEFECTO = UUID.randomUUID()
        private const val TIEMPO_DEFECTO = 1234567L
        private const val NOMBRE_USUARIO_DEFECTO = "Usuario"
        @JvmField
        internal val ID_POR_DEFECTO = EntidadTransaccional.PartesId(TIEMPO_DEFECTO, NOMBRE_USUARIO_DEFECTO, UUID_DEFECTO).id

        private val jsonDeserializacionPorDefecto = """
            {
                "id": "$ID_POR_DEFECTO",
                "committed": false,
                "orders": [${OrdenDTOPruebas.jsonDeserializacionPorDefecto}, ${OrdenDTOPruebas.jsonDeserializacionPorDefecto}]
            }
            """

        private val jsonPorDefecto = """
            {
                "client-id": 0,
                "id": "$ID_POR_DEFECTO",
                "committed": false,
                "orders": [${OrdenDTOPruebas.jsonPorDefecto}, ${OrdenDTOPruebas.jsonPorDefecto}]
            }
            """

        private val entidadDTOPorDefecto =
                LoteDeOrdenesDTO(
                        id = ID_POR_DEFECTO,
                        ordenes = listOf(OrdenDTOPruebas.entidadDTOPorDefecto, OrdenDTOPruebas.entidadDTOPorDefecto)
                                )

        private val entidadNegocioPorDefecto =
                LoteDeOrdenes(
                        0,
                        NOMBRE_USUARIO_DEFECTO,
                        UUID_DEFECTO,
                        TIEMPO_DEFECTO,
                        false,
                        listOf(OrdenDTOPruebas.entidadNegocioPorDefecto, OrdenDTOPruebas.entidadNegocioPorDefecto)
                             )

        private val jsonSinNulos = """
            {
                "client-id": 1,
                "id": "$ID_POR_DEFECTO",
                "committed": true,
                "orders": [${OrdenDTOPruebas.jsonSinNulos}, ${OrdenDTOPruebas.jsonSinNulos}]
            }
            """

        private val entidadDTOSinNulos =
                LoteDeOrdenesDTO(
                        1,
                        ID_POR_DEFECTO,
                        true,
                        listOf(OrdenDTOPruebas.entidadDTOSinNulos, OrdenDTOPruebas.entidadDTOSinNulos)
                                )

        private val entidadNegocioSinNulos =
                LoteDeOrdenes(
                        1,
                        NOMBRE_USUARIO_DEFECTO,
                        UUID_DEFECTO,
                        TIEMPO_DEFECTO,
                        true,
                        listOf(OrdenDTOPruebas.entidadNegocioSinNulos, OrdenDTOPruebas.entidadNegocioSinNulos)
                             )
    }

    @Nested
    inner class Deserializacion
    {
        @Test
        fun json_con_valores_por_defecto_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonDeserializacionPorDefecto, LoteDeOrdenesDTO::class.java)
            assertEquals(entidadDTOPorDefecto, entidadDeserializada)
        }

        @Test
        fun json_sin_nulos_correctamente()
        {
            val entidadDeserializada = ConfiguracionJackson.objectMapperDeJackson.readValue(jsonSinNulos, LoteDeOrdenesDTO::class.java)
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
                    "orders": [${OrdenDTOPruebas.jsonSinNulos}, ${OrdenDTOPruebas.jsonSinNulos}]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LoteDeOrdenesDTO::class.java) }
            }

            @Test
            fun con_id_null()
            {
                val json = """
                {
                    "client-id": 1,
                    "id": null,
                    "committed": true,
                    "orders": [${OrdenDTOPruebas.jsonSinNulos}, ${OrdenDTOPruebas.jsonSinNulos}]
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LoteDeOrdenesDTO::class.java) }
            }

            @Test
            fun sin_ordenes()
            {
                val json = """
                {
                    "client-id": 1,
                    "id": "$ID_POR_DEFECTO",
                    "committed": true
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LoteDeOrdenesDTO::class.java) }
            }

            @Test
            fun con_ordenes_null()
            {
                val json = """
                {
                    "client-id": 1,
                    "id": "$ID_POR_DEFECTO",
                    "committed": true,
                    "orders": null
                }
                """
                assertThrows<JsonMappingException> { ConfiguracionJackson.objectMapperDeJackson.readValue(json, LoteDeOrdenesDTO::class.java) }
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
                    val id = ID_POR_DEFECTO.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).subList(0, 2).joinToString(EntidadTransaccional.SEPARADOR_COMPONENTES_ID.toString())
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

                    val id = "$ID_POR_DEFECTO${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}otra-parte"
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
                    val partes = ID_POR_DEFECTO.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
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
                    val partes = ID_POR_DEFECTO.split(EntidadTransaccional.SEPARADOR_COMPONENTES_ID).toMutableList()
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
                val entidadDTO = LoteDeOrdenesDTO(entidadNegocioPorDefecto)
                assertEquals(entidadDTOPorDefecto, entidadDTO)
            }

            @Test
            fun sin_nulos_se_construye_correctamente()
            {
                val entidadDTO = LoteDeOrdenesDTO(entidadNegocioSinNulos)
                assertEquals(entidadDTOSinNulos, entidadDTO)
            }
        }
    }
}