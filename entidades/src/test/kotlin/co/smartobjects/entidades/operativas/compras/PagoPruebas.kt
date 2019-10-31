package co.smartobjects.entidades.operativas.compras

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


@DisplayName("Pago")
internal class PagoPruebas
{
    @Test
    fun hace_trim_a_numero_de_transaccion_correctamente()
    {
        val entidadSinTrim = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "    12-3    ")
        val entidadConTrim = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
        assertEquals(Decimal(10), entidad.campoValorPagado.valor)
        assertEquals("12-3", entidad.campoNumeroDeTransaccionPOS.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
        val entidadCopiada = entidadInicial.copiar(Decimal(20), Pago.MetodoDePago.TARJETA_CREDITO, "45-6")
        val entidadEsperada = Pago(Decimal(20), Pago.MetodoDePago.TARJETA_CREDITO, "45-6")
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_valor_pagado_negativo()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoFueraDeRango::class.java, {
            Pago(Decimal(-1), Pago.MetodoDePago.EFECTIVO, "12-3")
        })

        assertEquals(Pago.Campos.VALOR_PAGADO, excepcion.nombreDelCampo)
        assertEquals(Pago.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
        assertEquals("0", excepcion.valorDelLimite)
    }

    @Test
    fun no_se_permite_instanciar_con_numero_transaccion_vacio()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "")
        })

        assertEquals(Pago.Campos.NUMERO_TRANSACCION_POS, excepcion.nombreDelCampo)
        assertEquals(Pago.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_numero_transaccion_con_solo_espacios_o_tabs()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "              ")
        })

        assertEquals(Pago.Campos.NUMERO_TRANSACCION_POS, excepcion.nombreDelCampo)
        assertEquals(Pago.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }
}