package co.smartobjects.entidades.clientes

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("Cliente")
internal class ClientePruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = Cliente(1, "    Prueba    ")
        val entidadConTrim = Cliente(1, "Prueba")
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Cliente(1, "Prueba")
        assertEquals("Prueba", entidad.campoNombre.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Cliente(1, "Prueba")
        val entidadCopiada = entidadInicial.copiar(nombre = "Prueba editada")
        val entidadEsperada = Cliente(1, "Prueba editada")
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadInicial = Cliente(1, "Prueba")
        val entidadEsperada = Cliente(34634, "Prueba")
        val entidadCopiada = entidadInicial.copiarConId(34634)
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Cliente(1, "")
        }

        assertEquals(Cliente.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Cliente.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Cliente(1, "             ")
        }

        assertEquals(Cliente.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Cliente.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Nested
    inner class LlaveNFC
    {
        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val llaveDePrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Cliente.LlaveNFC(1, llaveDePrueba)

            assertTrue(llaveDePrueba === entidad.campoLlave.valor)
            assertTrue(llaveDePrueba === entidad.llave)
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.llave))
        }

        @Test
        fun inicializa_correctamente_con_string()
        {
            val llaveDePrueba = "lacontraseña1-"
            val entidad = Cliente.LlaveNFC(1, llaveDePrueba)

            assertTrue(llaveDePrueba.toCharArray().contentEquals(entidad.llave))
        }

        @Test
        fun toString_no_contiene_llave()
        {
            val llaveDePrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Cliente.LlaveNFC(1, llaveDePrueba)

            assertFalse(entidad.toString().contains(Cliente.LlaveNFC.Campos.LLAVE))
            assertFalse(entidad.toString().contains(String(llaveDePrueba)))
        }

        @Test
        fun limpiar_llave_manda_a_0_la_llave_original_el_campo_y_el_atributo()
        {
            val llaveDePrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Cliente.LlaveNFC(1, llaveDePrueba)

            entidad.limpiar()
            assertTrue(llaveDePrueba.all { it == '\u0000' })
            assertTrue(entidad.campoLlave.valor.all { it == '\u0000' })
            assertTrue(entidad.llave.all { it == '\u0000' })
        }

        @Test
        fun copiar_funciona_correctamente_con_parametros_primitivos()
        {
            val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
            val llaveDePrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidadInicial = Cliente.LlaveNFC(1, llaveDePrueba, fechaActual)

            val llaveDePrueba2 = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
            val entidadEsperada = Cliente.LlaveNFC(2, llaveDePrueba2, fechaActual.plusDays(1))

            val entidadCopiada = entidadInicial.copiar(2, llaveDePrueba2, fechaActual.plusDays(1))

            assertEquals(entidadEsperada, entidadCopiada)
        }

        @Test
        fun copiar_manda_a_0_la_llave_original_el_campo_y_el_atributo_cuando_se_envia_nueva_llave()
        {
            val llaveDePrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val llaveDePrueba2 = charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_')
            val entidad = Cliente.LlaveNFC(1, llaveDePrueba)

            val entidad2 = entidad.copiar(1, llaveDePrueba2)

            assertTrue(Arrays.equals(charArrayOf('o', 't', 'r', 'o', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2', '_'), entidad2.llave))
            assertTrue(llaveDePrueba.all { it == '\u0000' })
            assertTrue(entidad.campoLlave.valor.all { it == '\u0000' })
            assertTrue(entidad.llave.all { it == '\u0000' })
        }

        @Test
        fun copiar_manda_a_0_la_llave_original_el_campo_y_el_atributo_cuando_se_envia_nuevo_arreglo_con_la_misma_llave()
        {
            val llaveDePrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val llaveDePrueba2 = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Cliente.LlaveNFC(1, llaveDePrueba)

            val entidad2 = entidad.copiar(1, llaveDePrueba2)

            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.llave))
            assertTrue(llaveDePrueba.all { it == '\u0000' })
            assertTrue(entidad.campoLlave.valor.all { it == '\u0000' })
            assertTrue(entidad.llave.all { it == '\u0000' })
        }

        @Test
        fun copiar_no_cambia_la_llave_original_el_campo_y_el_atributo_cuando_se_envia_el_mismo_arreglo()
        {
            val llaveDePrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Cliente.LlaveNFC(1, llaveDePrueba)

            val entidad2 = entidad.copiar(1, llaveDePrueba)

            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.llave))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), llaveDePrueba))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.campoLlave.valor))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.llave))
        }

        @Test
        fun copiar_no_cambia_la_llave_original_el_campo_y_el_atributo_cuando_no_se_envia_valor()
        {
            val llaveDePrueba = charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-')
            val entidad = Cliente.LlaveNFC(1, llaveDePrueba)

            val entidad2 = entidad.copiar()

            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad2.llave))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), llaveDePrueba))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.campoLlave.valor))
            assertTrue(Arrays.equals(charArrayOf('l', 'a', 'c', 'o', 'n', 't', 'r', 'a', 's', 'e', 'ñ', 'a', '1', '-'), entidad.llave))
        }

        @Test
        fun no_se_permite_instanciar_con_llave_vacia()
        {
            var excepcion = assertThrows<EntidadConCampoVacio> {
                Cliente.LlaveNFC(1, charArrayOf())
            }

            assertEquals(Cliente.LlaveNFC.Campos.LLAVE, excepcion.nombreDelCampo)
            assertEquals(Cliente.LlaveNFC.NOMBRE_ENTIDAD, excepcion.nombreEntidad)

            excepcion = assertThrows<EntidadConCampoVacio> {
                Cliente.LlaveNFC(1, "")
            }

            assertEquals(Cliente.LlaveNFC.Campos.LLAVE, excepcion.nombreDelCampo)
            assertEquals(Cliente.LlaveNFC.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        }

        @Test
        fun no_se_permite_instanciar_si_id_de_la_zona_horaria_de_la_fecha_de_realizacion_es_no_es_la_por_defecto()
        {
            val zonaHorariaPorDefectoInvalida = "America/Bogota"
            val fechaRealizacion = ZonedDateTime.now(ZoneId.of(zonaHorariaPorDefectoInvalida)).plusDays(1)
            val excepcion = assertThrows<EntidadMalInicializada> {
                Cliente.LlaveNFC(1, "asdf", fechaRealizacion)
            }

            assertEquals(Cliente.LlaveNFC.Campos.FECHA_CREACION, excepcion.nombreDelCampo)
            assertEquals(Cliente.LlaveNFC.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
            assertEquals(fechaRealizacion.toString(), excepcion.valorUsado)
            assertTrue(excepcion.cause is EntidadMalInicializada)
            assertEquals(zonaHorariaPorDefectoInvalida, (excepcion.cause as EntidadMalInicializada).valorUsado)
            assertEquals(Cliente.LlaveNFC.Campos.FECHA_CREACION, (excepcion.cause as EntidadMalInicializada).nombreDelCampo)
            assertEquals(Cliente.LlaveNFC.NOMBRE_ENTIDAD, (excepcion.cause as EntidadMalInicializada).nombreEntidad)
        }

        @Test
        fun la_fecha_de_validez_hasta_es_siempre_mayor_o_igual_a_la_fecha_minima()
        {
            val fechaAUsar = FECHA_MINIMA_CREACION.minusSeconds(1)
            val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                Cliente.LlaveNFC(1, "asdf", fechaAUsar)
            }

            assertEquals(Cliente.LlaveNFC.Campos.FECHA_CREACION, excepcion.nombreDelCampo)
            assertEquals(Cliente.LlaveNFC.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
            assertEquals(fechaAUsar.toString(), excepcion.valorUsado)
            assertEquals(FECHA_MINIMA_CREACION.toString(), excepcion.valorDelLimite)
            assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
        }
    }
}