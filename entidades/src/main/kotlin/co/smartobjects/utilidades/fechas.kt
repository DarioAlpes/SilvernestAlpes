package co.smartobjects.utilidades

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAdjusters

const val ID_ZONA_HORARIA_POR_DEFECTO = "UTC-05:00"

@JvmField
val ZONA_HORARIA_POR_DEFECTO = ZoneId.of(ID_ZONA_HORARIA_POR_DEFECTO)!!

@JvmField
val FECHA_MINIMA_CREACION =
        ZonedDateTime.of(
                LocalDate.of(1990, 1, 1).with(TemporalAdjusters.firstDayOfYear()),
                LocalTime.MIDNIGHT,
                ZONA_HORARIA_POR_DEFECTO
                        )!!

fun formatearComoFechaConMesCompleto(fecha: ZonedDateTime): String = fecha.format(DateTimeFormatter.ofPattern("dd/MMMM/yyyy"))
fun formatearComoFechaHoraConMesCompleto(fecha: ZonedDateTime): String = fecha.format(DateTimeFormatter.ofPattern("dd/MMMM/yyyy KK:mm:ss a"))