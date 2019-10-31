package co.smartobjects.ui.javafx.controladores.carritocreditos

import co.smartobjects.ui.javafx.comoDineroFormateado
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosGeneralesView
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.modelos.carritocreditos.ItemCreditoUI
import com.jfoenix.controls.JFXButton
import io.reactivex.rxjavafx.observers.JavaFxObserver
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox

internal class ControladorItemCredito : HBox()
{
    @FXML
    internal lateinit var raiz: HBox
    @FXML
    internal lateinit var nombre: Label
    @FXML
    internal lateinit var precio: Label
    @FXML
    internal lateinit var cantidad: Label
    @FXML
    internal lateinit var botonDeSumar: JFXButton
    @FXML
    internal lateinit var botonDeRestar: JFXButton
    @FXML
    internal lateinit var iconoBorrado: JFXButton
    @FXML
    internal lateinit var iconoPagado: PrompterIconosGeneralesView

    init
    {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/layouts/carritocreditos/itemCredito.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<HBox>()
    }

    fun actualizar(itemCreditoUI: ItemCreditoUI): Node
    {
        nombre.text = itemCreditoUI.nombreProducto

        val precioFormateado = itemCreditoUI.precioTotalInicial.comoDineroFormateado(0)

        val observableDeCantidadConPrecio = itemCreditoUI.cantidad.map { "$it X $precioFormateado" }

        precio.textProperty().bind(JavaFxObserver.toBinding(observableDeCantidadConPrecio))
        cantidad.textProperty().bind(JavaFxObserver.toBinding(itemCreditoUI.cantidad.map { it.toString() }))

        if (itemCreditoUI.noEsModificable)
        {
            botonDeSumar.onAction = null
            botonDeRestar.onAction = null
            iconoBorrado.onAction = null
        }
        else
        {
            if (itemCreditoUI.contieneFondoUnico)
            {
                botonDeSumar.onAction = null
            }
            else
            {
                botonDeSumar.setOnAction {
                    itemCreditoUI.sumarUno()
                }
            }
            botonDeRestar.setOnAction {
                itemCreditoUI.restarUno()
            }
            iconoBorrado.setOnAction {
                itemCreditoUI.borrar()
            }
        }

        val observableParaDeshabilitarBotones = itemCreditoUI.faltaConfirmacionAdicion.map { it || itemCreditoUI.noEsModificable }
        val observableParaDeshabilitarSuma = observableParaDeshabilitarBotones.map { it || itemCreditoUI.contieneFondoUnico }

        botonDeSumar.disableProperty().bind(JavaFxObserver.toBinding(observableParaDeshabilitarSuma))
        botonDeRestar.disableProperty().bind(JavaFxObserver.toBinding(observableParaDeshabilitarBotones))
        iconoBorrado.disableProperty().bind(JavaFxObserver.toBinding(observableParaDeshabilitarBotones))

        itemCreditoUI.faltaConfirmacionAdicion.observarEnFx().subscribe {
            raiz.styleClass.removeAll("item-lista-carrito-creditos-activo", "item-lista-carrito-creditos-default")
            raiz.styleClass.add(if (it) "item-lista-carrito-creditos-activo" else "item-lista-carrito-creditos-default")


            iconoBorrado.styleClass.removeAll("boton-icono-sin-fondo-activo", "boton-icono-sin-fondo-defecto")
            iconoBorrado.styleClass.add(if (it) "boton-icono-sin-fondo-activo" else "boton-icono-sin-fondo-defecto")

            if (!itemCreditoUI.contieneFondoUnico)
            {
                botonDeSumar.styleClass.removeAll("boton-icono-activo", "boton-icono-defecto")
                botonDeSumar.styleClass.add(if (it) "boton-icono-activo" else "boton-icono-defecto")
            }

            botonDeRestar.styleClass.removeAll("boton-icono-activo", "boton-icono-defecto")
            botonDeRestar.styleClass.add(if (it) "boton-icono-activo" else "boton-icono-defecto")
        }

        iconoPagado.isVisible = itemCreditoUI.estaPagado

        return raiz
    }
}