package co.smartobjects.entidades.operativas.compras

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.excepciones.RelacionEntreCamposInvalida
import co.smartobjects.mockConDefaultAnswer
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.TemporalAdjusters
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@DisplayName("CreditoFondo")
internal class CreditoFondoPruebas
{
    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val entidadInicial = CreditoFondo(
                1,
                null,
                Decimal(10),
                Decimal(1000),
                Decimal(150),
                fechaActual,
                fechaActual,
                false,
                "Taquilla",
                "Un usuario",
                2,
                6,
                "código externo fondo",
                3,
                "un-uuid-de-dispositivo",
                4,
                5
                                         )
        val otraFecha = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusDays(1)
        val entidadCopiada = entidadInicial.copiar(
                6,
                7,
                Decimal(20),
                Decimal(2000),
                Decimal(250),
                otraFecha,
                otraFecha,
                true,
                "Orbita",
                "Otro usuario",
                8,
                12,
                "código externo fondo nuevo",
                9,
                "otro-uuid-de-dispositivo",
                10,
                11
                                                  )
        val entidadEsperada = CreditoFondo(
                6,
                7,
                Decimal(20),
                Decimal(2000),
                Decimal(250),
                otraFecha,
                otraFecha,
                true,
                "Orbita",
                "Otro usuario",
                8,
                12,
                "código externo fondo nuevo",
                9,
                "otro-uuid-de-dispositivo",
                10,
                11
                                          )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun `El valor pagado sin impuestos se calcula como "valor pagado - valor impuesto pagado"`()
    {
        val valorPagado = Decimal(12345.456879)
        val valorImpuestoPagado = Decimal(1205.4579)
        val entidad = CreditoFondo(
                1,
                null,
                Decimal(5),
                valorPagado,
                valorImpuestoPagado,
                ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                false,
                "Taquilla",
                "Un usuario",
                2,
                6,
                "código externo fondo",
                3,
                "un-uuid-de-dispositivo",
                4,
                5
                                  )

        assertEquals(valorPagado - valorImpuestoPagado, entidad.valorPagadoSinImpuesto)
    }

    @Nested
    inner class TasaImpositivaUsada
    {
        @Test
        fun se_calcula_correctamente()
        {
            val tasaEsperada = Decimal(19)
            val valorPagado = Decimal(123.456789)
            val valorImpuestoPagado = Decimal(23.45678991)
            val entidad = CreditoFondo(
                    1,
                    null,
                    Decimal(5),
                    valorPagado,
                    valorImpuestoPagado,
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                    false,
                    "Taquilla",
                    "Un usuario",
                    2,
                    6,
                    "código externo fondo",
                    3,
                    "un-uuid-de-dispositivo",
                    4,
                    5
                                      )

            assertEquals(tasaEsperada, entidad.tasaImpositivaUsada)
        }

        @Test
        fun si_el_valor_pagado_es_cero_la_tasa_impositiva_se_asume_como_cero()
        {
            val tasaEsperada = Decimal.CERO
            val valorPagado = Decimal.CERO
            val valorImpuestoPagado = Decimal.CERO
            val entidad = CreditoFondo(
                    1,
                    null,
                    Decimal(5),
                    valorPagado,
                    valorImpuestoPagado,
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                    ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),
                    false,
                    "Taquilla",
                    "Un usuario",
                    2,
                    6,
                    "código externo fondo",
                    3,
                    "un-uuid-de-dispositivo",
                    4,
                    5
                                      )

            assertEquals(tasaEsperada, entidad.tasaImpositivaUsada)
        }
    }

    @Nested
    inner class Instanciacion
    {
        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val fechaFinal = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusDays(1)
            val entidad = CreditoFondo(
                    1,
                    null,
                    Decimal(10),
                    Decimal(1000),
                    Decimal(150),
                    fechaActual,
                    fechaFinal,
                    false,
                    "Taquilla",
                    "Un usuario",
                    2,
                    6,
                    "código externo fondo",
                    3,
                    "un-uuid-de-dispositivo",
                    4,
                    5
                                      )
            assertEquals(Decimal(10), entidad.campoCantidad.valor)
            assertEquals(Decimal(1000), entidad.campoValorPagado.valor)
            assertEquals(Decimal(150), entidad.campoValorImpuestoPagado.valor)
            assertEquals(fechaActual, entidad.campoValidoDesde?.valor)
            assertEquals(fechaFinal, entidad.campoValidoHasta?.valor)
            assertEquals("Taquilla", entidad.campoOrigen.valor)
            assertEquals("Un usuario", entidad.campoNombreUsuario.valor)
            assertEquals("un-uuid-de-dispositivo", entidad.campoIdDispositivo.valor)
        }

        @Test
        fun permite_instanciar_con_fechas_de_validez_nulas()
        {
            val entidad =
                    CreditoFondo(
                            1,
                            null,
                            Decimal(10),
                            Decimal(1000),
                            Decimal(150),
                            null,
                            null,
                            false,
                            "Taquilla",
                            "Un usuario",
                            2,
                            6,
                            "código externo fondo",
                            3,
                            "un-uuid-de-dispositivo",
                            4,
                            5
                                )

            assertEquals(null, entidad.campoValidoDesde?.valor)
            assertEquals(null, entidad.validoDesde)
            assertEquals(null, entidad.campoValidoHasta?.valor)
            assertEquals(null, entidad.validoHasta)
        }

        @Test
        fun permite_instanciar_con_fechas_de_validez_desde_nula_y_fecha_de_validez_hasta_no_nula()
        {
            val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val entidad =
                    CreditoFondo(
                            1,
                            null,
                            Decimal(10),
                            Decimal(1000),
                            Decimal(150),
                            null,
                            fechaActual,
                            false,
                            "Taquilla",
                            "Un usuario",
                            2,
                            6,
                            "código externo fondo",
                            3,
                            "un-uuid-de-dispositivo",
                            4,
                            5
                                )

            assertEquals(null, entidad.campoValidoDesde?.valor)
            assertEquals(null, entidad.validoDesde)
            assertEquals(fechaActual, entidad.campoValidoHasta?.valor)
            assertEquals(fechaActual, entidad.validoHasta)
        }

        @Test
        fun permite_instanciar_con_fechas_de_validez_hasta_nula_y_fecha_de_validez_desde_no_nula()
        {
            val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val entidad =
                    CreditoFondo(
                            1,
                            null,
                            Decimal(10),
                            Decimal(1000),
                            Decimal(150),
                            fechaActual,
                            null,
                            false,
                            "Taquilla",
                            "Un usuario",
                            2,
                            6,
                            "código externo fondo",
                            3,
                            "un-uuid-de-dispositivo",
                            4,
                            5
                                )

            assertEquals(fechaActual, entidad.campoValidoDesde?.valor)
            assertEquals(fechaActual, entidad.validoDesde)
            assertEquals(null, entidad.campoValidoHasta?.valor)
            assertEquals(null, entidad.validoHasta)
        }

        @Test
        fun hace_trim_a_origen_correctamente()
        {
            val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val entidadSinTrim = CreditoFondo(
                    1,
                    null,
                    Decimal(10),
                    Decimal(1000),
                    Decimal(150),
                    fechaActual,
                    fechaActual,
                    false,
                    "    Taquilla    ",
                    "Un usuario",
                    2,
                    6,
                    "código externo fondo",
                    3,
                    "un-uuid-de-dispositivo",
                    4,
                    5
                                             )
            val entidadConTrim = CreditoFondo(
                    1,
                    null,
                    Decimal(10),
                    Decimal(1000),
                    Decimal(150),
                    fechaActual,
                    fechaActual,
                    false,
                    "Taquilla",
                    "Un usuario",
                    2,
                    6,
                    "código externo fondo",
                    3,
                    "un-uuid-de-dispositivo",
                    4,
                    5
                                             )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun hace_trim_a_nombre_usuario_correctamente()
        {
            val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val entidadSinTrim = CreditoFondo(
                    1,
                    null,
                    Decimal(10),
                    Decimal(1000),
                    Decimal(150),
                    fechaActual,
                    fechaActual,
                    false,
                    "Taquilla",
                    "    Un usuario    ",
                    2,
                    6,
                    "código externo fondo",
                    3,
                    "un-uuid-de-dispositivo",
                    4,
                    5
                                             )
            val entidadConTrim = CreditoFondo(
                    1,
                    null,
                    Decimal(10),
                    Decimal(1000),
                    Decimal(150),
                    fechaActual,
                    fechaActual,
                    false,
                    "Taquilla",
                    "Un usuario",
                    2,
                    6,
                    "código externo fondo",
                    3,
                    "un-uuid-de-dispositivo",
                    4,
                    5
                                             )
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun hace_trim_a_id_de_dispositivo_correctamente()
        {
            val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val entidadSinTrim = CreditoFondo(
                    1,
                    null,
                    Decimal(10),
                    Decimal(1000),
                    Decimal(150),
                    fechaActual,
                    fechaActual,
                    false,
                    "Taquilla",
                    "Un usuario",
                    2,
                    6,
                    "código externo fondo",
                    3,
                    "    un-uuid-de-dispositivo    ",
                    4,
                    5
                                             )
            val entidadConTrim = CreditoFondo(
                    1,
                    null,
                    Decimal(10),
                    Decimal(1000),
                    Decimal(150),
                    fechaActual,
                    fechaActual,
                    false,
                    "Taquilla",
                    "Un usuario",
                    2,
                    6,
                    "código externo fondo",
                    3,
                    "un-uuid-de-dispositivo",
                    4,
                    5
                                             )
            assertEquals(entidadConTrim, entidadSinTrim)
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
                    val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(-1),
                                Decimal(1000),
                                Decimal(150),
                                fechaActual,
                                fechaActual,
                                false,
                                "Taquilla",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.CANTIDAD, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals("0", excepcion.valorDelLimite)
                }
            }

            @Nested
            inner class ValorBasePagado
            {
                @Test
                fun negativo()
                {
                    val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(-1),
                                Decimal(150),
                                fechaActual,
                                fechaActual,
                                false,
                                "Taquilla",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.VALOR_PAGADO, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals("0", excepcion.valorDelLimite)
                }
            }

            @Nested
            inner class ValorImpuestoPagado
            {
                @Test
                fun negativo()
                {
                    val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(-1),
                                fechaActual,
                                fechaActual,
                                false,
                                "Taquilla",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.VALOR_IMPUESTO_PAGADO, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals("0", excepcion.valorDelLimite)
                }
            }

            @Nested
            inner class Origen
            {
                @Test
                fun vacio()
                {
                    val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaActual,
                                fechaActual,
                                false,
                                "",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.ORIGEN, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_espacios_o_tabs()
                {
                    val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaActual,
                                fechaActual,
                                false,
                                "              ",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.ORIGEN, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }

            @Nested
            inner class NombreUsuario
            {
                @Test
                fun vacio()
                {
                    val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaActual,
                                fechaActual,
                                false,
                                "Taquilla",
                                "",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_espacios_o_tabs()
                {
                    val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaActual,
                                fechaActual,
                                false,
                                "Taquilla",
                                "              ",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }

            @Nested
            inner class IdDispositivo
            {
                @Test
                fun vacio()
                {
                    val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaActual,
                                fechaActual,
                                false,
                                "Taquilla",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.ID_DISPOSITIVO, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_espacios_o_tabs()
                {
                    val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaActual,
                                fechaActual,
                                false,
                                "Taquilla",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "              ",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.ID_DISPOSITIVO, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }

            @Nested
            inner class ValidoDesde
            {
                @Test
                fun `tiene_id_de_la_zona_horaria_diferente_a_UTC-5`()
                {
                    val zonaHorariaPorDefectoInvalida = "America/Bogota"
                    val fechaInicial = ZonedDateTime.now(ZoneId.of(zonaHorariaPorDefectoInvalida))
                    val fechaFinal = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaInicial,
                                fechaFinal,
                                false,
                                "Taquilla",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaInicial.toString(), excepcion.valorUsado)
                    assertTrue(excepcion.cause is EntidadMalInicializada)
                    assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
                    assertEquals(CreditoFondo.Campos.FECHA_VALIDEZ_DESDE, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
                }

                @Test
                fun no_es_siempre_menor_o_igual_a_la_fecha_final()
                {
                    val fechaInicial =
                            ZonedDateTime.of(LocalDate.of(2017, 1, 1).with(TemporalAdjusters.firstDayOfYear()),
                                             LocalTime.MIDNIGHT,
                                             ZONA_HORARIA_POR_DEFECTO
                                            )
                    val fechaFinal = fechaInicial.minusSeconds(1)
                    val excepcion = assertThrows<RelacionEntreCamposInvalida> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaInicial,
                                fechaFinal,
                                false,
                                "Taquilla",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(CreditoFondo.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampoIzquierdo)
                    assertEquals(CreditoFondo.Campos.FECHA_VALIDEZ_HASTA, excepcion.nombreDelCampoDerecho)
                    assertEquals(fechaInicial.toString(), excepcion.valorUsadoPorCampoIzquierdo)
                    assertEquals(fechaFinal.toString(), excepcion.valorUsadoPorCampoDerecho)
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
                    val fechaInicial = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                    val fechaFinal = ZonedDateTime.now(ZoneId.of(zonaHorariaPorDefectoInvalida)).plusDays(1)
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaInicial,
                                fechaFinal,
                                false,
                                "Taquilla",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.FECHA_VALIDEZ_HASTA, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaFinal.toString(), excepcion.valorUsado)
                    assertTrue(excepcion.cause is EntidadMalInicializada)
                    assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
                    assertEquals(CreditoFondo.Campos.FECHA_VALIDEZ_HASTA, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
                }

                @Test
                fun no_siempre_mayor_o_igual_a_la_fecha_minima()
                {
                    val fechaAUsar = FECHA_MINIMA_CREACION.minusSeconds(1)
                    val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                        CreditoFondo(
                                1,
                                null,
                                Decimal(10),
                                Decimal(1000),
                                Decimal(150),
                                fechaAUsar,
                                fechaAUsar,
                                false,
                                "Taquilla",
                                "Un usuario",
                                2,
                                6,
                                "código externo fondo",
                                3,
                                "un-uuid-de-dispositivo",
                                4,
                                5
                                    )
                    }

                    assertEquals(CreditoFondo.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampo)
                    assertEquals(CreditoFondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaAUsar.toString(), excepcion.valorUsado)
                    assertEquals(FECHA_MINIMA_CREACION.toString(), excepcion.valorDelLimite)
                    assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
                }
            }
        }
    }
}

@DisplayName("CreditoFondoConNombre")
class CreditoFondoConNombrePruebas
{
    @Nested
    inner class EstaPagado
    {
        @Test
        fun esta_pagado_si_el_credito_asociado_tiene_un_id_diferente_a_nulo()
        {
            val mockCreditoFondo = mockConDefaultAnswer(CreditoFondo::class.java).also {
                Mockito.doReturn(1234L).`when`(it).id
                Mockito.doReturn(Decimal.UNO).`when`(it).valorPagado
                Mockito.doReturn(1L).`when`(it).idCliente
                Mockito.doReturn(1L).`when`(it).idImpuestoPagado
                Mockito.doReturn(Decimal.UNO).`when`(it).tasaImpositivaUsada
            }

            val creditoPagado = CreditoFondoConNombre("nombre", mockCreditoFondo)

            assertTrue(creditoPagado.estaPagado)
        }

        @Test
        fun no_esta_pagado_si_el_credito_asociado_tiene_un_id_diferente_igual_a_nulo()
        {
            val mockCreditoFondo = mockConDefaultAnswer(CreditoFondo::class.java).also {
                Mockito.doReturn(null).`when`(it).id
                Mockito.doReturn(Decimal.UNO).`when`(it).valorPagado
                Mockito.doReturn(1L).`when`(it).idCliente
                Mockito.doReturn(1L).`when`(it).idImpuestoPagado
                Mockito.doReturn(Decimal.UNO).`when`(it).tasaImpositivaUsada
            }

            val creditoNoPagado = CreditoFondoConNombre("nombre", mockCreditoFondo)

            assertFalse(creditoNoPagado.estaPagado)
        }
    }
}