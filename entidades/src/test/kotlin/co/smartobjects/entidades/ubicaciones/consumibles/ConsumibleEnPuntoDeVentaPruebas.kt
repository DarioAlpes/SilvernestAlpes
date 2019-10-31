package co.smartobjects.entidades.ubicaciones.consumibles

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


@DisplayName("Consumible En Punto De Venta")
internal class ConsumibleEnPuntoDeVentaPruebas
{
    @Test
    fun se_instancia_correctamente()
    {
        ConsumibleEnPuntoDeVenta(1, 2, "código externo fondo")
    }
}

@DisplayName("Consumo")
internal class ConsumoPruebas
{
    @Test
    fun el_id_del_consumible_es_correcto()
    {
        val idConsumibleEsperado = 1234L
        val consumo = Consumo(1, idConsumibleEsperado, Decimal.DIEZ)

        assertEquals(idConsumibleEsperado, consumo.idConsumible)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Consumo(1, 2, Decimal(1))

        val entidadEsperada = Consumo(1234, 346, Decimal(9875))

        val entidadCopiada = entidadInicial.copiar(1234, 346, Decimal(9875))

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_cantidad_negativa()
    {
        val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
            Consumo(1, 2, Decimal(-0.01))
        }

        assertEquals(Consumo.Campos.CANTIDAD, excepcion.nombreDelCampo)
        assertEquals(Consumo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals("0", excepcion.valorDelLimite)
    }
}