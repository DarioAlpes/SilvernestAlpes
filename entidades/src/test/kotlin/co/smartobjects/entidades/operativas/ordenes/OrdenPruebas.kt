package co.smartobjects.entidades.operativas.ordenes

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DisplayName("Orden")
internal class OrdenPruebas
{
    private val fechaDeRealizacion = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
    private val fechaDesde = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
    private val fechaHasta = fechaDesde.plusDays(1)
    private val transaccionCredito = Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, fechaHasta)
    private val transaccionDebito = Transaccion.Debito(1, 2, "3", Decimal(4), 5, "6", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Orden(1, 2, 3, listOf(transaccionCredito, transaccionDebito), fechaDeRealizacion)

        val transaccionCreditoNueva = Transaccion.Credito(2, 3, "4", 5, 6, "código externo fondo", Decimal(7), 8, "9", fechaDesde.plusDays(1), fechaHasta.plusDays(1))
        val transaccionDebitoNueva = Transaccion.Debito(2, 3, "4", Decimal(5), 6, "7", ConsumibleEnPuntoDeVenta(8, 9, "código externo fondo"))
        val fechaNueva = fechaDeRealizacion.plusDays(2)
        val entidadEsperada = Orden(2, 3, 4, listOf(transaccionCreditoNueva, transaccionDebitoNueva), fechaNueva)

        val entidadCopiada = entidadInicial.copiar(2, 3, 4, listOf(transaccionCreditoNueva, transaccionDebitoNueva), fechaNueva)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Nested
    inner class Instanciacion
    {
        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val transacciones = listOf(transaccionCredito, transaccionDebito)
            val entidad = Orden(1, 2, 3, transacciones, fechaDeRealizacion)

            assertEquals(transacciones, entidad.campoTransacciones.valor)
            assertEquals(transacciones, entidad.transacciones)
            assertEquals(fechaDeRealizacion, entidad.campoFechaDeRealizacion.valor)
            assertEquals(fechaDeRealizacion, entidad.fechaDeRealizacion)
        }

        @Nested
        inner class NoPermiteInstanciarCon
        {
            @Nested
            inner class Transacciones
            {
                @Test
                fun vacias()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Orden(1, 2, 3, listOf(), fechaDeRealizacion)
                    }

                    assertEquals(Orden.Campos.TRANSACCIONES, excepcion.nombreDelCampo)
                    assertEquals(Orden.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }

            @Nested
            inner class FechaDeRealizacion
            {
                @Test
                fun `el_id_de_la_zona_horaria_es_UTC-5`()
                {
                    val zonaHorariaPorDefectoInvalida = "America/Bogota"
                    val fechaInvalida = fechaDeRealizacion.withZoneSameInstant(ZoneId.of(zonaHorariaPorDefectoInvalida))
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        Orden(1, 2, 3, listOf(transaccionCredito, transaccionDebito), fechaInvalida)
                    }

                    assertEquals(Orden.Campos.FECHA_REALIZACION, excepcion.nombreDelCampo)
                    assertEquals(Orden.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaInvalida.toString(), excepcion.valorUsado)
                    assertTrue(excepcion.cause is EntidadMalInicializada)
                    assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
                    assertEquals(Orden.Campos.FECHA_REALIZACION, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
                    assertEquals(Orden.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
                }

                @Test
                fun es_siempre_mayor_o_igual_a_la_fecha_minima()
                {
                    val fechaInvalida = FECHA_MINIMA_CREACION.minusSeconds(1)
                    val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                        Orden(1, 2, 3, listOf(transaccionCredito, transaccionDebito), fechaInvalida)
                    }

                    assertEquals(Orden.Campos.FECHA_REALIZACION, excepcion.nombreDelCampo)
                    assertEquals(Orden.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaInvalida.toString(), excepcion.valorUsado)
                    assertEquals(FECHA_MINIMA_CREACION.toString(), excepcion.valorDelLimite)
                    assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
                }
            }
        }
    }
}