package co.smartobjects.entidades.ubicaciones.contabilizables

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class ConteoUbicacionPruebas
{
    private val fechaDeRealizacion = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = ConteoUbicacion(1, 2, 3, fechaDeRealizacion)

        val fechaNueva = fechaDeRealizacion.plusDays(2)
        val entidadEsperada = ConteoUbicacion(2, 3, 4, fechaNueva)

        val entidadCopiada = entidadInicial.copiar(2, 3, 4, fechaNueva)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Nested
    inner class Instanciacion
    {
        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val entidad = ConteoUbicacion(1, 2, 3, fechaDeRealizacion)

            assertEquals(fechaDeRealizacion, entidad.campoFechaDeRealizacion.valor)
            assertEquals(fechaDeRealizacion, entidad.fechaDeRealizacion)
        }

        @Nested
        inner class NoPermiteInstanciarCon
        {
            @Nested
            inner class FechaDeRealizacion
            {
                @Test
                fun `si_el_id_de_la_zona_horaria_no_es_UTC-5`()
                {
                    val zonaHorariaPorDefectoInvalida = "America/Bogota"
                    val fechaInvalida = fechaDeRealizacion.withZoneSameInstant(ZoneId.of(zonaHorariaPorDefectoInvalida))
                    val excepcion = assertThrows<EntidadMalInicializada> {
                        ConteoUbicacion(1, 2, 3, fechaInvalida)
                    }

                    assertEquals(ConteoUbicacion.Campos.FECHA_REALIZACION, excepcion.nombreDelCampo)
                    assertEquals(ConteoUbicacion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaInvalida.toString(), excepcion.valorUsado)
                    assertTrue(excepcion.cause is EntidadMalInicializada)
                    assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
                    assertEquals(ConteoUbicacion.Campos.FECHA_REALIZACION, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
                    assertEquals(ConteoUbicacion.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
                }

                @Test
                fun es_siempre_mayor_o_igual_a_la_fecha_minima()
                {
                    val fechaInvalida = FECHA_MINIMA_CREACION.minusSeconds(1)
                    val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                        ConteoUbicacion(1, 2, 3, fechaInvalida)
                    }

                    assertEquals(ConteoUbicacion.Campos.FECHA_REALIZACION, excepcion.nombreDelCampo)
                    assertEquals(ConteoUbicacion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals(fechaInvalida.toString(), excepcion.valorUsado)
                    assertEquals(FECHA_MINIMA_CREACION.toString(), excepcion.valorDelLimite)
                    assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
                }
            }
        }
    }
}