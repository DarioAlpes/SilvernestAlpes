package co.smartobjects.entidades.ubicaciones.contabilizables

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class UbicacionesContabilizablesPruebas
{
    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = UbicacionesContabilizables(1L, setOf(2, 5, 7, 6, 57))
        val entidadEsperada = UbicacionesContabilizables(2, setOf(345, 7, 45, 2))
        val entidadCopiada = entidadInicial.copiar(2, setOf(345, 7, 45, 2))

        assertEquals(entidadEsperada, entidadCopiada)
    }
}