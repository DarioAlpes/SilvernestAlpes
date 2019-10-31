package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.excepciones.RelacionEntreCamposInvalida
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.TemporalAdjusters
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DisplayName("Paquete")
internal class PaquetePruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)

        val entidadEsperada = Paquete(
                1,
                null,
                "Prueba",
                "Descripcion",
                true,
                fechaActual,
                fechaActual,
                listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                "código externo"
                                     )

        val entidadProcesada = Paquete(
                1,
                null,
                "    Prueba     ",
                "Descripcion",
                true,
                fechaActual,
                fechaActual,
                listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                "código externo"
                                      )

        assertEquals(entidadEsperada, entidadProcesada)
    }

    @Test
    fun hace_trim_a_descripcion_correctamente()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val entidadSinTrim = Paquete(
                1,
                null,
                "Prueba",
                "    Descripcion    ",
                true,
                fechaActual,
                fechaActual,
                listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                "código externo"
                                    )
        val entidadConTrim = Paquete(
                1,
                null,
                "Prueba",
                "Descripcion",
                true,
                fechaActual,
                fechaActual,
                listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                "código externo"
                                    )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val entidad = Paquete(
                1,
                null,
                "Prueba",
                "Descripcion",
                true,
                fechaActual,
                fechaActual,
                listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                "código externo"
                             )

        assertEquals("Prueba", entidad.campoNombre.valor)
        assertEquals("Descripcion", entidad.campoDescripcion.valor)
        assertEquals(true, entidad.campoDisponibleParaLaVenta.valor)
        assertEquals(listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)), entidad.campoFondosIncluidos.valor)
        assertEquals(fechaActual, entidad.campoValidoDesde.valor)
        assertEquals(fechaActual, entidad.campoValidoHasta.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val entidadInicial = Paquete(
                1,
                null,
                "Prueba",
                "Descripcion",
                true,
                fechaActual,
                fechaActual,
                listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                "código externo inicial"
                                    )
        val entidadCopiada = entidadInicial.copiar(nombre = "Prueba editada", descripcion = "Descripcion editada", codigoExterno = "código externo final")
        val entidadEsperada = Paquete(
                1,
                null,
                "Prueba editada",
                "Descripcion editada",
                true,
                fechaActual,
                fechaActual,
                listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                "código externo final"
                                     )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val entidadInicial = Paquete(
                1,
                null,
                "Prueba",
                "Descripcion",
                true,
                fechaActual,
                fechaActual,
                listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                "código externo inicial"
                                    )
        val entidadEsperada = Paquete(
                1,
                93893,
                "Prueba",
                "Descripcion",
                true,
                fechaActual,
                fechaActual,
                listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                "código externo inicial"
                                     )

        val entidadCopiada = entidadInicial.copiarConId(93893)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Paquete(
                    1,
                    null,
                    "",
                    "Paquete",
                    true,
                    fechaActual,
                    fechaActual,
                    listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                    "código externo"
                   )
        }

        assertEquals(Paquete.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_solo_espacios_o_tabs()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Paquete(
                    1,
                    null,
                    "              ",
                    "Paquete",
                    true,
                    fechaActual,
                    fechaActual,
                    listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                    "código externo"
                   )
        }

        assertEquals(Paquete.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_descripcion_vacio()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Paquete(
                    1,
                    null,
                    "Paquete",
                    "",
                    true,
                    fechaActual,
                    fechaActual,
                    listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                    "código externo"
                   )
        }

        assertEquals(Paquete.Campos.DESCRIPCION, excepcion.nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_descripcion_con_espacios_o_tabs()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Paquete(
                    1,
                    null,
                    "Paquete",
                    "              ",
                    true,
                    fechaActual,
                    fechaActual,
                    listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                    "código externo"
                   )
        }

        assertEquals(Paquete.Campos.DESCRIPCION, excepcion.nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_sin_fondos_incluidos()
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Paquete(
                    1,
                    null,
                    "Nombre",
                    "Paquete",
                    true,
                    fechaActual,
                    fechaActual,
                    listOf(),
                    "código externo"
                   )
        }

        assertEquals(Paquete.Campos.FONDOS_INCLUIDOS, excepcion.nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun el_id_de_la_zona_horaria_de_la_fecha_de_validez_desde_es_UTC()
    {
        val zonaHorariaPorDefectoInvalida = "America/Bogota"
        val fechaInicial = ZonedDateTime.now(ZoneId.of(zonaHorariaPorDefectoInvalida))
        val fechaFinal = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val excepcion = assertThrows<EntidadMalInicializada> {
            Paquete(
                    1,
                    null,
                    "Nombre",
                    "Paquete",
                    true,
                    fechaInicial,
                    fechaFinal,
                    listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                    "código externo"
                   )
        }

        assertEquals(Paquete.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(fechaInicial.toString(), excepcion.valorUsado)
        assertTrue(excepcion.cause is EntidadMalInicializada)
        assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
        assertEquals(Paquete.Campos.FECHA_VALIDEZ_DESDE, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
    }

    @Test
    fun el_id_de_la_zona_horaria_de_la_fecha_de_validez_hasta_es_UTC()
    {
        val zonaHorariaPorDefectoInvalida = "America/Bogota"
        val fechaInicial = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        val fechaFinal = ZonedDateTime.now(ZoneId.of(zonaHorariaPorDefectoInvalida)).plusDays(1)
        val excepcion = assertThrows<EntidadMalInicializada> {
            Paquete(
                    1,
                    null,
                    "Nombre",
                    "Paquete",
                    true,
                    fechaInicial,
                    fechaFinal,
                    listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                    "código externo"
                   )
        }

        assertEquals(Paquete.Campos.FECHA_VALIDEZ_HASTA, excepcion.nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(fechaFinal.toString(), excepcion.valorUsado)
        assertTrue(excepcion.cause is EntidadMalInicializada)
        assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
        assertEquals(Paquete.Campos.FECHA_VALIDEZ_HASTA, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
    }

    @Test
    fun la_fecha_de_validez_desde_es_siempre_menor_o_igual_a_la_fecha_de_validez_hasta()
    {
        val fechaInicial =
                ZonedDateTime.of(LocalDate.of(2017, 1, 1).with(TemporalAdjusters.firstDayOfYear()),
                                 LocalTime.MIDNIGHT,
                                 ZONA_HORARIA_POR_DEFECTO
                                )
        val fechaFinal = fechaInicial.minusSeconds(1)
        val excepcion = assertThrows<RelacionEntreCamposInvalida> {
            Paquete(
                    1,
                    null,
                    "Nombre",
                    "Paquete",
                    true,
                    fechaInicial,
                    fechaFinal,
                    listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                    "código externo"
                   )
        }

        assertEquals(Paquete.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(Paquete.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampoIzquierdo)
        assertEquals(Paquete.Campos.FECHA_VALIDEZ_HASTA, excepcion.nombreDelCampoDerecho)
        assertEquals(fechaInicial.toString(), excepcion.valorUsadoPorCampoIzquierdo)
        assertEquals(fechaFinal.toString(), excepcion.valorUsadoPorCampoDerecho)
        assertEquals(RelacionEntreCamposInvalida.Relacion.MENOR, excepcion.relacionViolada)
    }

    @Test
    fun la_fecha_de_validez_hasta_es_siempre_mayor_o_igual_a_la_fecha_minima()
    {
        val fechaAUsar = FECHA_MINIMA_CREACION.minusSeconds(1)
        val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
            Paquete(
                    1,
                    null,
                    "Nombre",
                    "Paquete",
                    true,
                    fechaAUsar,
                    fechaAUsar,
                    listOf(Paquete.FondoIncluido(1, "código externo incluido", Decimal.UNO)),
                    "código externo"
                   )
        }

        assertEquals(Paquete.Campos.FECHA_VALIDEZ_DESDE, excepcion.nombreDelCampo)
        assertEquals(Paquete.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(fechaAUsar.toString(), excepcion.valorUsado)
        assertEquals(FECHA_MINIMA_CREACION.toString(), excepcion.valorDelLimite)
        assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
    }
}