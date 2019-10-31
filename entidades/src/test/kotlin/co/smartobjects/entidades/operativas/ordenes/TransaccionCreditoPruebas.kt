package co.smartobjects.entidades.operativas.ordenes

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.excepciones.RelacionEntreCamposInvalida
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

@DisplayName("Transaccion.Credito")
internal class TransaccionCreditoPruebas
{
    private val fechaDesde = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
    private val fechaHasta = fechaDesde.plusDays(1)

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, fechaHasta)

        val entidadEsperada = Transaccion.Credito(2, 3, "4", 5, 6, "código externo fondo nuevo", Decimal(7), 8, "9", fechaDesde.plusDays(1), fechaHasta.plusDays(1))

        val entidadCopiada = entidadInicial.copiar(2, 3, "4", 5, 6, "código externo fondo nuevo", Decimal(7), 8, "9", fechaDesde.plusDays(1), fechaHasta.plusDays(1))

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Nested
    inner class Instanciacion
    {
        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val nombreUsuario = "3"
            val cantidad = Decimal(6)
            val idDispositivo = "8"
            val entidad = Transaccion.Credito(1, 2, nombreUsuario, 4, 5, "código externo fondo", cantidad, 7, idDispositivo, fechaDesde, fechaHasta)

            assertEquals(nombreUsuario, entidad.campoNombreUsuario.valor)
            assertEquals(nombreUsuario, entidad.nombreUsuario)
            assertEquals(cantidad, entidad.campoCantidad.valor)
            assertEquals(cantidad, entidad.cantidad)
            assertEquals(idDispositivo, entidad.campoIdDispositivo.valor)
            assertEquals(idDispositivo, entidad.idDispositivo)
            assertEquals(fechaDesde, entidad.campoValidoDesde?.valor)
            assertEquals(fechaDesde, entidad.validoDesde)
            assertEquals(fechaHasta, entidad.campoValidoHasta?.valor)
            assertEquals(fechaHasta, entidad.validoHasta)
        }

        @Test
        fun hace_trim_a_nombre_usuario_correctamente()
        {
            val entidadSinTrim = Transaccion.Credito(1, 2, "   3  ", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, fechaHasta)
            val entidadConTrim = Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, fechaHasta)
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun hace_trim_a_id_de_dispositivo_correctamente()
        {
            val entidadSinTrim = Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "    8    ", fechaDesde, fechaHasta)
            val entidadConTrim = Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, fechaHasta)
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun permite_instanciar_con_fechas_de_validez_nulas()
        {
            val entidad = Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", null, null)

            assertEquals(null, entidad.campoValidoDesde?.valor)
            assertEquals(null, entidad.validoDesde)
            assertEquals(null, entidad.campoValidoHasta?.valor)
            assertEquals(null, entidad.validoHasta)
        }

        @Test
        fun permite_instanciar_con_fechas_de_validez_desde_nula_y_fecha_de_validez_hasta_no_nula()
        {
            val entidad = Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", null, fechaHasta)

            assertEquals(null, entidad.campoValidoDesde?.valor)
            assertEquals(null, entidad.validoDesde)
            assertEquals(fechaHasta, entidad.campoValidoHasta?.valor)
            assertEquals(fechaHasta, entidad.validoHasta)
        }

        @Test
        fun permite_instanciar_con_fechas_de_validez_hasta_nula_y_fecha_de_validez_desde_no_nula()
        {
            val entidad = Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, null)

            assertEquals(fechaDesde, entidad.campoValidoDesde?.valor)
            assertEquals(fechaDesde, entidad.validoDesde)
            assertEquals(null, entidad.campoValidoHasta?.valor)
            assertEquals(null, entidad.validoHasta)
        }

        @Nested
        inner class FallaSi
        {
            @Nested
            inner class Cantidad
            {
                @Test
                fun negativa()
                {
                    val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                        Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(-999), 7, "8", fechaDesde, fechaHasta)
                    }

                    assertEquals(Transaccion.Campos.CANTIDAD, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals("0", excepcion.valorDelLimite)
                }
            }

            @Nested
            inner class NombreDeUsuario
            {
                @Test
                fun vacio()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Transaccion.Credito(1, 2, "", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, fechaHasta)
                    }

                    assertEquals(Transaccion.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_espacios_o_tabs()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Transaccion.Credito(1, 2, "   \t\t", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, fechaHasta)
                    }

                    assertEquals(Transaccion.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }

            @Nested
            inner class IdDeDispositivo
            {
                @Test
                fun vacio()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "", fechaDesde, fechaHasta)
                    }

                    assertEquals(Transaccion.Campos.ID_DISPOSITIVO, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_espacios_o_tabs()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "  \t \t", fechaDesde, fechaHasta)
                    }

                    assertEquals(Transaccion.Campos.ID_DISPOSITIVO, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }

            @Nested
            inner class ValidoDesde
            {
                @Test
                fun `tiene_id_de_la_zona_horaria_diferente_a_UTC-5`()
                {
                    val zonaHorariaPorDefectoInvalida = "America/Bogota"
                    val fechaInvalida = fechaDesde.withZoneSameInstant(ZoneId.of(zonaHorariaPorDefectoInvalida))
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaInvalida, fechaHasta)
                    }

                    assertEquals(Transaccion.Credito.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.Credito.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaInvalida.toString(), excepcion.valorUsado)
                    assertTrue(excepcion.cause is EntidadMalInicializada)
                    assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
                    assertEquals(Transaccion.Credito.Campos.FECHA_VALIDEZ_DESDE, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
                    assertEquals(Transaccion.Credito.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
                }

                @Test
                fun si_no_es_siempre_mayor_o_igual_a_la_fecha_minima()
                {
                    val fechaInvalida = FECHA_MINIMA_CREACION.minusSeconds(1)
                    val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                        Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaInvalida, fechaHasta)
                    }

                    assertEquals(Transaccion.Credito.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.Credito.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaInvalida.toString(), excepcion.valorUsado)
                    assertEquals(FECHA_MINIMA_CREACION.toString(), excepcion.valorDelLimite)
                    assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
                }

                @Test
                fun si_no_es_siempre_menor_o_igual_a_la_fecha_de_validez_hasta()
                {
                    val fechaInvalida = fechaDesde.plusDays(100)
                    val excepcion = assertThrows<RelacionEntreCamposInvalida> {
                        Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaInvalida, fechaHasta)
                    }

                    assertEquals(Transaccion.Credito.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.Credito.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(Transaccion.Credito.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampoIzquierdo)
                    assertEquals(Transaccion.Credito.Campos.FECHA_VALIDEZ_HASTA, excepcion.nombreDelCampoDerecho)
                    assertEquals(fechaInvalida.toString(), excepcion.valorUsadoPorCampoIzquierdo)
                    assertEquals(fechaHasta.toString(), excepcion.valorUsadoPorCampoDerecho)
                    assertEquals(RelacionEntreCamposInvalida.Relacion.MENOR, excepcion.relacionViolada)
                }
            }

            @Nested
            inner class ValidoHasta
            {
                @Test
                fun `tiene_id_de_la_zona_horaria_diferente_a_UTC-5`()
                {
                    val zonaHorariaPorDefectoInvalida = "America/Bogota"
                    val fechaInvalida = fechaHasta.withZoneSameInstant(ZoneId.of(zonaHorariaPorDefectoInvalida))
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        Transaccion.Credito(1, 2, "3", 4, 5, "código externo fondo", Decimal(6), 7, "8", fechaDesde, fechaInvalida)
                    }

                    assertEquals(Transaccion.Credito.Campos.FECHA_VALIDEZ_HASTA, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.Credito.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaInvalida.toString(), excepcion.valorUsado)
                    assertTrue(excepcion.cause is EntidadMalInicializada)
                    assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
                    assertEquals(Transaccion.Credito.Campos.FECHA_VALIDEZ_HASTA, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
                    assertEquals(Transaccion.Credito.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
                }
            }
        }
    }
}