package co.smartobjects.ui.javafx.controladores.carritocreditos

import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.comoDineroFormateado
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.javafx.verificarEstilos
import co.smartobjects.ui.modelos.carritocreditos.ItemCreditoUI
import co.smartobjects.utilidades.Decimal
import io.reactivex.Observable
import javafx.event.ActionEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ControladorItemCreditoPruebas : PruebaJavaFXBase()
{
    @Test
    fun al_actualizar_se_conectan_controles_correctamente_el_producto()
    {
        val itemCredito = ControladorItemCredito()

        val nombreProducto = "Nombre producto"
        val cantidad = 7
        val precioTotalInicial = Decimal(22_698)
        val mockCreditoUI =
                mockConDefaultAnswer(ItemCreditoUI::class.java)
                    .also {
                        doReturn(false).`when`(it).estaPagado
                        doReturn(false).`when`(it).noEsModificable
                        doReturn(false).`when`(it).contieneFondoUnico
                        doReturn(Observable.just(false)).`when`(it).faltaConfirmacionAdicion
                        doReturn(nombreProducto).`when`(it).nombreProducto
                        doReturn(precioTotalInicial).`when`(it).precioTotalInicial
                        doReturn(Observable.just(cantidad)).`when`(it).cantidad
                        doNothing().`when`(it).sumarUno()
                        doNothing().`when`(it).restarUno()
                        doNothing().`when`(it).borrar()
                    }

        val precioFormateado = precioTotalInicial.comoDineroFormateado(0)

        itemCredito.actualizar(mockCreditoUI)


        validarEnThreadUI {
            assertEquals(nombreProducto, itemCredito.nombre.text)
            assertEquals("$cantidad X $precioFormateado", itemCredito.precio.text)
            assertEquals("$cantidad", itemCredito.cantidad.text)
        }

        itemCredito.botonDeSumar.fireEvent(ActionEvent())
        verify(mockCreditoUI).sumarUno()

        itemCredito.botonDeRestar.fireEvent(ActionEvent())
        verify(mockCreditoUI).restarUno()

        itemCredito.iconoBorrado.fireEvent(ActionEvent())
        verify(mockCreditoUI).borrar()

        validarEnThreadUI {
            itemCredito.raiz.verificarEstilos("item-lista-carrito-creditos-default", "item-lista-carrito-creditos-activo")
        }
    }

    @Test
    fun si_el_credito_no_es_modificable_los_botones_de_cambio_de_cantidad_y_eliminacion_estan_deshabilitados_y_no_emiten_nada()
    {
        val itemCredito = ControladorItemCredito()

        val mockCreditoUI =
                mockConDefaultAnswer(ItemCreditoUI::class.java)
                    .also {
                        doReturn(false).`when`(it).estaPagado
                        doReturn(true).`when`(it).noEsModificable
                        doReturn(false).`when`(it).contieneFondoUnico
                        doReturn(Observable.just(false)).`when`(it).faltaConfirmacionAdicion
                        doReturn("Nombre producto").`when`(it).nombreProducto
                        doReturn(Decimal(123.456)).`when`(it).precioTotalInicial
                        doReturn(Observable.just(7)).`when`(it).cantidad
                        doNothing().`when`(it).sumarUno()
                        doNothing().`when`(it).restarUno()
                        doNothing().`when`(it).borrar()
                    }


        itemCredito.actualizar(mockCreditoUI)

        itemCredito.botonDeSumar.fireEvent(ActionEvent())
        verify(mockCreditoUI, times(0)).sumarUno()

        itemCredito.botonDeRestar.fireEvent(ActionEvent())
        verify(mockCreditoUI, times(0)).restarUno()

        itemCredito.iconoBorrado.fireEvent(ActionEvent())
        verify(mockCreditoUI, times(0)).borrar()

        validarEnThreadUI {
            assertTrue(itemCredito.botonDeSumar.isDisable)
            assertTrue(itemCredito.botonDeRestar.isDisable)
            assertTrue(itemCredito.iconoBorrado.isDisable)
        }
    }

    @Test
    fun si_al_credito_le_falta_confirmacion_de_adicion_los_botones_de_cambio_de_cantidad_y_eliminacion_estan_deshabilitados_y_toda_la_fila_tiene_estilo_correcto()
    {
        val itemCredito = ControladorItemCredito()

        val mockCreditoUI =
                mockConDefaultAnswer(ItemCreditoUI::class.java)
                    .also {
                        doReturn(false).`when`(it).estaPagado
                        doReturn(true).`when`(it).noEsModificable
                        doReturn(false).`when`(it).contieneFondoUnico
                        doReturn(Observable.just(true)).`when`(it).faltaConfirmacionAdicion
                        doReturn("Nombre producto").`when`(it).nombreProducto
                        doReturn(Decimal(123.456)).`when`(it).precioTotalInicial
                        doReturn(Observable.just(7)).`when`(it).cantidad
                        doNothing().`when`(it).sumarUno()
                        doNothing().`when`(it).restarUno()
                        doNothing().`when`(it).borrar()
                    }


        itemCredito.actualizar(mockCreditoUI)


        itemCredito.botonDeSumar.fireEvent(ActionEvent())
        verify(mockCreditoUI, times(0)).sumarUno()

        itemCredito.botonDeRestar.fireEvent(ActionEvent())
        verify(mockCreditoUI, times(0)).restarUno()

        itemCredito.iconoBorrado.fireEvent(ActionEvent())
        verify(mockCreditoUI, times(0)).borrar()

        validarEnThreadUI {
            assertTrue(itemCredito.iconoBorrado.isDisable)
            assertTrue(itemCredito.botonDeRestar.isDisable)
            assertTrue(itemCredito.botonDeSumar.isDisable)

            itemCredito.raiz.verificarEstilos("item-lista-carrito-creditos-activo", "item-lista-carrito-creditos-default")
            itemCredito.iconoBorrado.verificarEstilos("boton-icono-sin-fondo-activo", "boton-icono-sin-fondo-defecto")
            itemCredito.botonDeRestar.verificarEstilos("boton-icono-activo", "boton-icono-defecto")
            itemCredito.botonDeSumar.verificarEstilos("boton-icono-activo", "boton-icono-defecto")
        }
    }
}