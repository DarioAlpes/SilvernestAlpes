package co.smartobjects.entidades.operativas.ordenes

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


@DisplayName("LoteDeOrdenes")
internal class LoteDeOrdenesPruebas
{
    private val timestampHoy = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
    private val fechaDesde = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
    private val fechaHasta = fechaDesde.plusDays(1)
    private val transaccionCredito = Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, fechaHasta)
    private val transaccionDebito = Transaccion.Debito(1, 2, "3", Decimal(4), 5, "6", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
    private val orden = Orden(1, 2, 3, listOf(transaccionCredito, transaccionDebito), timestampHoy)

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos_y_no_modifica_datos_iniciales_de_la_transaccion()
    {
        val entidadInicial = LoteDeOrdenes(1, "usuario", listOf(orden))

        val transaccionCreditoNueva = Transaccion.Credito(2, 3, "4", 5, 6, "código externo fondo", Decimal(7), 8, "9", fechaDesde.plusDays(1), fechaHasta.plusDays(1))
        val transaccionDebitoNueva = Transaccion.Debito(2, 3, "4", Decimal(5), 6, "7", ConsumibleEnPuntoDeVenta(8, 9, "código externo fondo"))
        val ordenNueva = Orden(2, 3, 4, listOf(transaccionCreditoNueva, transaccionDebitoNueva), timestampHoy.plusMinutes(1))

        val entidadEsperada = LoteDeOrdenes(2, entidadInicial.nombreUsuario, entidadInicial.uuid, entidadInicial.tiempoCreacion, true, listOf(ordenNueva))

        val entidadCopiada = entidadInicial.copiar(2, entidadEsperada.creacionTerminada, listOf(ordenNueva))

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Nested
    inner class Instanciacion
    {
        @Nested
        inner class NuevaTransaccion
        {
            @Test
            fun asigna_id_unico_segun_tiempo_en_milisegundos_uuid_aleatorio_y_nombre_de_usuario()
            {
                val entidad = LoteDeOrdenes(1, "usuario", listOf(orden))

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
                val entidad = LoteDeOrdenes(1, "usuario", listOf(orden))
                val otraEntidad = LoteDeOrdenes(1, "usuario", listOf(orden))

                // Tecnicamente esta prueba puede fallar si el uuid generado al azar colisiona, pero la probabilidad lo hace imposible en la practica siempre que el uuid se genere aleatoriamente
                assertNotEquals(entidad.uuid, otraEntidad.uuid)
            }
        }

        @Test
        fun transaccion_existente_usa_parametros_dados()
        {
            val usuario = "usuario"
            val uuid = UUID.randomUUID()
            val tiempo = 123456789L
            val entidad = LoteDeOrdenes(99, usuario, uuid, tiempo, true, listOf(orden))

            assertEquals("usuario", entidad.nombreUsuario)
            assertEquals(uuid, entidad.uuid)
            assertEquals(tiempo, entidad.tiempoCreacion)
            assertEquals(true, entidad.creacionTerminada)
            assertEquals(true, entidad.campoCreacionTerminada.valor)
            assertEquals("$tiempo${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$usuario${EntidadTransaccional.SEPARADOR_COMPONENTES_ID}$uuid", entidad.id)
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
                        LoteDeOrdenes(1, "", listOf(orden))
                    }

                    assertEquals(EntidadTransaccional.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_espacios_o_tabs()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        LoteDeOrdenes(1, "  \t\t ", listOf(orden))
                    }

                    assertEquals(EntidadTransaccional.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(EntidadTransaccional.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }

            @Nested
            inner class Ordenes
            {
                @Test
                fun vacias()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        LoteDeOrdenes(1, "usuario", listOf())
                    }

                    assertEquals(LoteDeOrdenes.Campos.ORDENES, excepcion.nombreDelCampo)
                    assertEquals(LoteDeOrdenes.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }
        }
    }
}