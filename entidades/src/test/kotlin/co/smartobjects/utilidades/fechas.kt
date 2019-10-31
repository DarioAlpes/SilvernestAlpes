package co.smartobjects.utilidades

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.TemporalAdjusters
import kotlin.test.assertEquals


@DisplayName("Cliente")
internal class ClientePruebas
{
    @Test
    fun ID_ZONA_HORARIA_POR_DEFECTO_es_una_zona_horaria_valida()
    {
        ZoneId.of(ID_ZONA_HORARIA_POR_DEFECTO)
    }

    @Test
    fun ZONA_HORARIA_POR_DEFECTO_tiene_como_id_a_ID_ZONA_HORARIA_POR_DEFECTO()
    {
        assertEquals(ID_ZONA_HORARIA_POR_DEFECTO, ZONA_HORARIA_POR_DEFECTO.id)
    }

    @Test
    fun FECHA_MINIMA_CREACION_es_igual_al_inicio_del_año_1990()
    {
        assertEquals(
                ZonedDateTime.of(LocalDate.of(1990, 1, 1).with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIDNIGHT, ZONA_HORARIA_POR_DEFECTO),
                FECHA_MINIMA_CREACION
                    )
    }
}