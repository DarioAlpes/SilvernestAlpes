package co.smartobjects.entidades.fondos.precios

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("Precio")
internal class PrecioPruebas
{
    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Precio(Decimal(12.45689), 1L)
        assertEquals(Decimal(12.45689), entidad.campoValor.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Precio(Decimal(12.45689), 1L)
        val entidadCopiada = entidadInicial.copiar(valor = Decimal(20.455))
        val entidadEsperada = Precio(Decimal(20.455), 1L)
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_precio_negativo()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoFueraDeRango::class.java, {
            Precio(Decimal(-12.45689), 1L)
        })

        assertEquals(Precio.Campos.VALOR, excepcion.nombreDelCampo)
        assertEquals(Precio.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(Decimal(-12.45689).toString(), excepcion.valorUsado)
        assertEquals("0", excepcion.valorDelLimite)
        assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
    }
}