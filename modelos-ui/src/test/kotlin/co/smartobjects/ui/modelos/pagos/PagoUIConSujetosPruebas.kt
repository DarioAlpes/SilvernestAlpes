package co.smartobjects.ui.modelos.pagos

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.utilidades.Decimal
import io.reactivex.Notification
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("PagoUIConSujetos")
internal class PagoUIConSujetosPruebas : PruebasModelosRxBase()
{
    private val modelo: PagoUI = PagoUIConSujetos()

    @Nested
    inner class CambiarValorPagado
    {
        private val testValorPagado = modelo.valorPagado.test()

        @Test
        fun no_emite_valor_al_inicializar()
        {
            testValorPagado.assertValueCount(0)
        }

        @Test
        fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
        {
            val numeroEventos = 10
            (0 until numeroEventos).forEach {
                modelo.cambiarValorPagado(it.toString())
                testValorPagado.assertValueAt(it, Notification.createOnNext(Decimal(it)))
            }
            testValorPagado.assertValueCount(numeroEventos)
        }

        @Test
        fun con_valor_negativo_emite_error_EntidadConCampoVacio()
        {
            modelo.cambiarValorPagado("-1")
            testValorPagado.assertValue({ it.isOnError })
            testValorPagado.assertValue({ it.error!! is EntidadConCampoFueraDeRango })
            testValorPagado.assertValueCount(1)
        }

        @Test
        fun con_valor_no_numerico_emite_error_con_mensaje_correcto()
        {
            modelo.cambiarValorPagado("asd")
            testValorPagado.assertValue({ it.isOnError })
            testValorPagado.assertValue({ it.error!!.message == "Debe ser numérico" })
            testValorPagado.assertValueCount(1)
        }

        @Test
        fun a_error_y_luego_a_valor_valido_emite_ambos_valores()
        {
            modelo.cambiarValorPagado("-1")
            modelo.cambiarValorPagado("0")
            testValorPagado.assertValueAt(0, { it.isOnError })
            testValorPagado.assertValueAt(0, { it.error!! is EntidadConCampoFueraDeRango })
            testValorPagado.assertValueAt(1, Notification.createOnNext(Decimal(0)))
            testValorPagado.assertValueCount(2)
        }

        @Test
        fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
        {
            modelo.cambiarValorPagado("0")
            modelo.cambiarValorPagado("-1")
            testValorPagado.assertValueAt(0, Notification.createOnNext(Decimal(0)))
            testValorPagado.assertValueAt(1, { it.isOnError })
            testValorPagado.assertValueAt(1, { it.error!! is EntidadConCampoFueraDeRango })
            testValorPagado.assertValueCount(2)
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
        {
            modelo.finalizarProceso()
            modelo.cambiarValorPagado("0")
            modelo.cambiarValorPagado("-1")
            testValorPagado.assertValueCount(0)
        }
    }

    @Nested
    inner class CambiarMetodoPago
    {
        private val testMetodoPago = modelo.metodoPago.test()

        @Test
        fun no_emite_valor_al_inicializar()
        {
            testMetodoPago.assertValueCount(0)
        }

        @Test
        fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
        {
            val numeroEventos = Pago.MetodoDePago.values().size
            Pago.MetodoDePago.values().forEachIndexed { index, metodoDePago ->
                modelo.cambiarMetodoPago(metodoDePago)
                testMetodoPago.assertValueAt(index, metodoDePago)
            }
            testMetodoPago.assertValueCount(numeroEventos)
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
        {
            modelo.finalizarProceso()
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarMetodoPago(Pago.MetodoDePago.TARJETA_DEBITO)
            testMetodoPago.assertValueCount(0)
        }
    }

    @Nested
    inner class CambiarNumeroTransaccion
    {
        private val testNumeroTransaccionPOS = modelo.numeroTransaccionPOS.test()

        @Test
        fun no_emite_valor_al_inicializar()
        {
            testNumeroTransaccionPOS.assertValueCount(0)
        }

        @Test
        fun con_valor_valido_emite_valor_cambiado_una_vez_por_cada_llamado()
        {
            val numeroEventos = 10
            (0 until numeroEventos).forEach {
                val numeroTransaccion = "123-$it"
                modelo.cambiarNumeroTransaccionPOS(numeroTransaccion)
                testNumeroTransaccionPOS.assertValueAt(it, Notification.createOnNext(numeroTransaccion))
            }
            testNumeroTransaccionPOS.assertValueCount(numeroEventos)
        }

        @Test
        fun con_espacios_emite_valor_con_trim()
        {
            modelo.cambiarNumeroTransaccionPOS("        123-4       ")
            testNumeroTransaccionPOS.assertValue(Notification.createOnNext("123-4"))
            testNumeroTransaccionPOS.assertValueCount(1)
        }

        @Test
        fun con_valor_vacio_emite_error_EntidadConCampoVacio()
        {
            modelo.cambiarNumeroTransaccionPOS("")
            testNumeroTransaccionPOS.assertValue({ it.isOnError })
            testNumeroTransaccionPOS.assertValue({ it.error!! is EntidadConCampoVacio })
            testNumeroTransaccionPOS.assertValueCount(1)
        }

        @Test
        fun con_valor_con_espacios_y_tabs_emite_error_EntidadConCampoVacio()
        {
            modelo.cambiarNumeroTransaccionPOS("             ")
            testNumeroTransaccionPOS.assertValue({ it.isOnError })
            testNumeroTransaccionPOS.assertValue({ it.error!! is EntidadConCampoVacio })
            testNumeroTransaccionPOS.assertValueCount(1)
        }

        @Test
        fun a_error_y_luego_a_valor_valido_emite_ambos_valores()
        {
            modelo.cambiarNumeroTransaccionPOS("")
            modelo.cambiarNumeroTransaccionPOS("123-4")
            testNumeroTransaccionPOS.assertValueAt(0, { it.isOnError })
            testNumeroTransaccionPOS.assertValueAt(0, { it.error!! is EntidadConCampoVacio })
            testNumeroTransaccionPOS.assertValueAt(1, Notification.createOnNext("123-4"))
            testNumeroTransaccionPOS.assertValueCount(2)
        }

        @Test
        fun a_valor_valido_y_luego_a_error_emite_ambos_valores()
        {
            modelo.cambiarNumeroTransaccionPOS("123-4")
            modelo.cambiarNumeroTransaccionPOS("")
            testNumeroTransaccionPOS.assertValueAt(0, Notification.createOnNext("123-4"))
            testNumeroTransaccionPOS.assertValueAt(1, { it.isOnError })
            testNumeroTransaccionPOS.assertValueAt(1, { it.error!! is EntidadConCampoVacio })
            testNumeroTransaccionPOS.assertValueCount(2)
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
        {
            modelo.finalizarProceso()
            modelo.cambiarNumeroTransaccionPOS("123-4")
            modelo.cambiarNumeroTransaccionPOS("")
            testNumeroTransaccionPOS.assertValueCount(0)
        }
    }

    @Nested
    inner class EsPagoValido
    {
        private val testEsPagoValido = modelo.esPagoValido.test()

        @Test
        fun no_emite_evento_al_cambiar_otros_valores_si_no_se_cambia_valor_pagado()
        {
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarNumeroTransaccionPOS("123-4")
            testEsPagoValido.assertValueCount(0)
        }

        @Test
        fun no_emite_evento_al_cambiar_otros_valores_si_no_se_cambia_metodo_pago()
        {
            modelo.cambiarValorPagado("1")
            modelo.cambiarNumeroTransaccionPOS("123-4")
            testEsPagoValido.assertValueCount(0)
        }

        @Test
        fun no_emite_evento_al_cambiar_otros_valores_si_no_se_cambia_numero_transaccion()
        {
            modelo.cambiarValorPagado("1")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            testEsPagoValido.assertValueCount(0)
        }

        @Test
        fun emite_evento_true_al_cambiar_todos_los_valores_a_valores_validos()
        {
            modelo.cambiarValorPagado("1")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarNumeroTransaccionPOS("123-4")
            testEsPagoValido.assertValue(true)
            testEsPagoValido.assertValueCount(1)
        }

        @Test
        fun emite_evento_false_al_cambiar_otros_campos_a_valor_valido_y_valor_pagado_a_valor_invalido()
        {
            modelo.cambiarValorPagado("-1")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarNumeroTransaccionPOS("123-4")
            testEsPagoValido.assertValue(false)
            testEsPagoValido.assertValueCount(1)
        }

        @Test
        fun emite_evento_false_al_cambiar_otros_campos_a_valor_valido_y_numero_transaccion_a_valor_invalido()
        {
            modelo.cambiarValorPagado("1")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarNumeroTransaccionPOS("")
            testEsPagoValido.assertValue(false)
            testEsPagoValido.assertValueCount(1)
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso()
        {
            modelo.finalizarProceso()
            modelo.cambiarValorPagado("1")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarNumeroTransaccionPOS("123-4")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.TARJETA_DEBITO)
            modelo.cambiarValorPagado("-1")
            modelo.cambiarNumeroTransaccionPOS("")
            testEsPagoValido.assertValueCount(0)
        }
    }

    @Nested
    inner class APago
    {
        @Test
        fun retorna_pago_correcto_al_asignar_valores_validos()
        {
            modelo.cambiarValorPagado("1")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarNumeroTransaccionPOS("123-4")
            val pago = modelo.aPago()
            val pagoEsperado = Pago(Decimal(1), Pago.MetodoDePago.EFECTIVO,"123-4")
            Assertions.assertEquals(pagoEsperado, pago)
        }

        @Test
        fun lanza_IllegalStateException_si_no_se_ha_asignado_ningun_valor()
        {
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aPago() })
        }

        @Test
        fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_valor_pagado()
        {
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarNumeroTransaccionPOS("123-4")
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aPago() })
        }

        @Test
        fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_metodo_pago()
        {
            modelo.cambiarValorPagado("1")
            modelo.cambiarNumeroTransaccionPOS("123-4")
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aPago() })
        }

        @Test
        fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_sin_asignar_numero_transaccion()
        {
            modelo.cambiarValorPagado("1")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aPago() })
        }

        @Test
        fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_con_valor_pagado_invalido()
        {
            modelo.cambiarValorPagado("-1")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarNumeroTransaccionPOS("123-4")
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aPago() })
        }

        @Test
        fun lanza_IllegalStateException_con_todos_los_campos_validos_pero_con_numero_transaccion_invalido()
        {
            modelo.cambiarValorPagado("1")
            modelo.cambiarMetodoPago(Pago.MetodoDePago.EFECTIVO)
            modelo.cambiarNumeroTransaccionPOS("")
            Assertions.assertThrows(IllegalStateException::class.java, { modelo.aPago() })
        }
    }
}