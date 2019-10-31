package co.smartobjects.entidades.operativas.reservas

import co.smartobjects.entidades.excepciones.EntidadConCampoDuplicado
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("Reserva")
class ReservaPruebas
{
    private val timestampHoy = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos_y_no_modifica_datos_iniciales_de_la_transaccion()
    {
        val sesionDeManillaInicial = SesionDeManilla(
                1,
                null,
                1,
                byteArrayOf(0),
                timestampHoy,
                timestampHoy.plusDays(1),
                setOf<Long>(1, 2, 3)
                                                    )

        val entidadInicial = Reserva(1, "usuario", listOf(sesionDeManillaInicial))

        val sesionDeManillaFinal = SesionDeManilla(
                3,
                1,
                6,
                byteArrayOf(1, 2, 3),
                timestampHoy.plusDays(3),
                timestampHoy.plusDays(5),
                setOf<Long>(4, 5, 7)
                                                  )
        val entidadEsperada = Reserva(99, "usuario", entidadInicial.uuid, entidadInicial.tiempoCreacion, true, 12345, listOf(sesionDeManillaFinal))

        val entidadCopiada = entidadInicial.copiar(99, entidadEsperada.creacionTerminada, 12345, listOf(sesionDeManillaFinal))

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Nested
    inner class Instanciacion
    {
        private val sesionDeManilla =
                SesionDeManilla(
                        1,
                        null,
                        1,
                        byteArrayOf(0),
                        timestampHoy,
                        timestampHoy.plusDays(1),
                        setOf<Long>(1, 2, 3)
                               )

        @Nested
        inner class NuevaTransaccion
        {
            @Test
            fun asigna_id_unico_segun_tiempo_en_milisegundos_uuid_aleatorio_y_nombre_de_usuario()
            {
                val entidad = Reserva(1, "usuario", listOf(sesionDeManilla))

                assertEquals(false, entidad.creacionTerminada)
                assertEquals(false, entidad.campoCreacionTerminada.valor)
                assertEquals("${entidad.tiempoCreacion}${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}${entidad.nombreUsuario}${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}${entidad.uuid}", entidad.id)
                // Se usa el tiempo actual
                assertTrue(entidad.tiempoCreacion <= ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).toInstant().toEpochMilli())
                assertTrue(entidad.tiempoCreacion >= ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).toInstant().toEpochMilli() - 1000)
            }

            @Test
            fun el_uuid_generado_es_aleatorio()
            {
                val entidad = Reserva(1, "usuario", listOf(sesionDeManilla))
                val otraEntidad = Reserva(1, "usuario", listOf(sesionDeManilla))

                // Tecnicamente esta prueba puede fallar si el uuid generado al azar colisiona, pero la probabilidad lo hace imposible en la practica siempre que el uuid se genere aleatoriamente
                assertNotEquals(entidad.uuid, otraEntidad.uuid)
            }

            @Test
            fun el_numero_de_reserva_es_null()
            {
                val entidad = Reserva(1, "usuario", listOf(sesionDeManilla))

                assertNull(entidad.numeroDeReserva)
            }
        }

        @Test
        fun transaccion_existente_usa_parametros_dados()
        {
            val usuario = "usuario"
            val uuid = UUID.randomUUID()
            val tiempo = 123456789L
            val numeroDeReserva = 1234L
            val entidad = Reserva(99, usuario, uuid, tiempo, true, numeroDeReserva, listOf(sesionDeManilla))

            assertEquals("usuario", entidad.nombreUsuario)
            assertEquals(uuid, entidad.uuid)
            assertEquals(tiempo, entidad.tiempoCreacion)
            assertEquals(numeroDeReserva, entidad.numeroDeReserva)
            assertEquals(true, entidad.creacionTerminada)
            assertEquals(true, entidad.campoCreacionTerminada.valor)
            assertEquals("$tiempo${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$usuario${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$uuid", entidad.id)
        }

        @Test
        fun con_un_uuid_de_tag_nulo_en_2_sesiones_diferentes()
        {
            val sesionesDeManillaInvalidas =
                    listOf(
                            SesionDeManilla(
                                    1,
                                    null,
                                    1,
                                    null,
                                    timestampHoy,
                                    timestampHoy.plusDays(1),
                                    setOf<Long>(1, 2, 3)
                                           ),
                            SesionDeManilla(
                                    1,
                                    null,
                                    2,
                                    null,
                                    timestampHoy,
                                    timestampHoy.plusDays(1),
                                    setOf<Long>(1, 2, 3)
                                           )
                          )

            Reserva(1, "asdfasdfasdf", sesionesDeManillaInvalidas)
        }

        @Nested
        inner class NoPermiteInstanciarCon
        {
            @Nested
            inner class NombreUsuario
            {
                @Test
                fun vacio()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Reserva(1, "", listOf(sesionDeManilla))
                    }

                    assertEquals(EntidadTransaccional.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_espacios_o_tabs()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Reserva(1, "   \t\t  ", listOf(sesionDeManilla))
                    }

                    assertEquals(EntidadTransaccional.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }

            @Nested
            inner class SesionesDeManilla
            {
                @Test
                fun vacias()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Reserva(1, "asdfasdfasdf", listOf())
                    }

                    assertEquals(Reserva.Campos.SESIONES_DE_MANILLA, excepcion.nombreDelCampo)
                    assertEquals(Reserva.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_una_misma_persona_en_2_sesiones_diferentes()
                {
                    val personaRepetida = 1L
                    val sesionesDeManillaInvalidas =
                            listOf(
                                    SesionDeManilla(
                                            1,
                                            null,
                                            personaRepetida,
                                            byteArrayOf(1, 2, 3),
                                            timestampHoy,
                                            timestampHoy.plusDays(1),
                                            setOf<Long>(1, 2, 3)
                                                   ),
                                    SesionDeManilla(
                                            1,
                                            null,
                                            personaRepetida,
                                            byteArrayOf(4, 5, 6),
                                            timestampHoy,
                                            timestampHoy.plusDays(1),
                                            setOf<Long>(1, 2, 3)
                                                   )
                                  )

                    val excepcion = assertThrows<EntidadConCampoDuplicado> {
                        Reserva(1, "asdfasdfasdf", sesionesDeManillaInvalidas)
                    }

                    assertEquals(Reserva.Campos.SESIONES_DE_MANILLA, excepcion.nombreDelCampo)
                    assertEquals(Reserva.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_un_misma_uuid_de_tag_en_2_sesiones_diferentes()
                {
                    val uuidRepetido = byteArrayOf(1, 2, 3)
                    val sesionesDeManillaInvalidas =
                            listOf(
                                    SesionDeManilla(
                                            1,
                                            null,
                                            1,
                                            uuidRepetido,
                                            timestampHoy,
                                            timestampHoy.plusDays(1),
                                            setOf<Long>(1, 2, 3)
                                                   ),
                                    SesionDeManilla(
                                            1,
                                            null,
                                            2,
                                            uuidRepetido,
                                            timestampHoy,
                                            timestampHoy.plusDays(1),
                                            setOf<Long>(1, 2, 3)
                                                   )
                                  )

                    val excepcion = assertThrows<EntidadConCampoDuplicado> {
                        Reserva(1, "asdfasdfasdf", sesionesDeManillaInvalidas)
                    }

                    assertEquals(Reserva.Campos.SESIONES_DE_MANILLA, excepcion.nombreDelCampo)
                    assertEquals(Reserva.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }
        }
    }
}