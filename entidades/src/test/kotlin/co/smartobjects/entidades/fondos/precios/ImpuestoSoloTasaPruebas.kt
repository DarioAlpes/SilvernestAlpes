package co.smartobjects.entidades.fondos.precios

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("ImpuestoSoloTasa")
internal class ImpuestoSoloTasaPruebas
{
    @Test
    fun se_instancia_correctamente_usando_un_impuesto()
    {
        val impuesto = Impuesto(1, 1, "nombre", Decimal.UNO)
        val entidad = ImpuestoSoloTasa(impuesto)

        assertEquals(Decimal.UNO, entidad.campoTasa.valor)
        assertEquals(impuesto.tasa, entidad.tasa)
        assertEquals(impuesto.id, entidad.id)
        assertEquals(impuesto.idCliente, entidad.idCliente)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = ImpuestoSoloTasa(1, 1, Decimal.UNO)

        assertEquals(Decimal.UNO, entidad.campoTasa.valor)
    }

    @Test
    fun `La tasaParaCalculos es la tasa ÷ 100`()
    {
        val entidad = ImpuestoSoloTasa(1, 1, Decimal(123546))

        assertEquals(Decimal(123546.0 / 100.0), entidad.tasaParaCalculos)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = ImpuestoSoloTasa(1, 1, Decimal.UNO)
        val entidadCopiada = entidadInicial.copiar(tasa = Decimal.DIEZ)
        val entidadEsperada = ImpuestoSoloTasa(1, 1, Decimal.DIEZ)
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_tasa_negativa()
    {
        val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
            ImpuestoSoloTasa(1, 1, Decimal(-1))
        }

        assertEquals(ImpuestoSoloTasa.Campos.TASA, excepcion.nombreDelCampo)
        assertEquals(ImpuestoSoloTasa.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals(Decimal(-1).toString(), excepcion.valorUsado)
        assertEquals("0", excepcion.valorDelLimite)
        assertEquals(EntidadConCampoFueraDeRango.Limite.INFERIOR, excepcion.limiteSobrepasado)
    }
}