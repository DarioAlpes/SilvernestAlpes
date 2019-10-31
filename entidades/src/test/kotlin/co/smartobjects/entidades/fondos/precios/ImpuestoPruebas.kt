package co.smartobjects.entidades.fondos.precios

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("Impuesto")
internal class ImpuestoPruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = Impuesto(1, 1, "    Prueba    ", Decimal.UNO)
        val entidadConTrim = Impuesto(1, 1, "Prueba", Decimal.UNO)
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Impuesto(1, 1, "Prueba", Decimal.UNO)
        assertEquals("Prueba", entidad.campoNombre.valor)
        assertEquals(Decimal.UNO, entidad.campoTasa.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Impuesto(1, 1, "Prueba", Decimal.UNO)
        val entidadCopiada = entidadInicial.copiar(nombre = "Prueba editada")
        val entidadEsperada = Impuesto(1, 1, "Prueba editada", Decimal.UNO)
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadInicial = Impuesto(1, 1, "Prueba", Decimal.UNO)
        val entidadEsperada = Impuesto(1, 74645, "Prueba", Decimal.UNO)
        val entidadCopiada = entidadInicial.copiarConId(74645)
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Impuesto(1, 1, "", Decimal.UNO)
        }

        assertEquals(Impuesto.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Impuesto.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Impuesto(1, 1, "               ", Decimal.UNO)
        }

        assertEquals(Impuesto.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Impuesto.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_tasa_negativa()
    {
        val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
            Impuesto(1, 1, "Impuesto prueba", Decimal(-1))
        }

        assertEquals(Impuesto.Campos.TASA, excepcion.nombreDelCampo)
        assertEquals(Impuesto.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(Decimal(-1).toString(), excepcion.valorUsado)
        assertEquals("0", excepcion.valorDelLimite)
        assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
    }
}