package co.smartobjects.ui.javafx.controladores.genericos

import com.jfoenix.controls.JFXButton
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane

internal class ControladorItemEliminable : HBox()
{
    @FXML
    internal lateinit var panelItem: StackPane
    @FXML
    internal lateinit var botonEliminar: JFXButton


    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/genericos/itemEliminable.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<HBox>()
    }

    internal inline fun inicializar(nodoItem: Node, crossinline accionEliminar: () -> Unit)
    {
        panelItem.children.setAll(nodoItem)
        botonEliminar.setOnAction { accionEliminar() }
    }
}