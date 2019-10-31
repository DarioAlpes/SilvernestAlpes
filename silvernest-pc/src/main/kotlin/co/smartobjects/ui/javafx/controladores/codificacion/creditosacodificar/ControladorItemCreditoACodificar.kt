package co.smartobjects.ui.javafx.controladores.codificacion.creditosacodificar

import co.smartobjects.ui.javafx.comoDineroFormateado
import co.smartobjects.ui.modelos.codificacion.ItemACodificarUI
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox


internal class ControladorItemCreditoACodificar : HBox()
{
    @FXML
    internal lateinit var raiz: HBox
    @FXML
    internal lateinit var nombre: Label
    @FXML
    internal lateinit var precio: Label
    @FXML
    internal lateinit var cantidad: Label

    init
    {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/layouts/codificacion/creditosacodificar/itemCreditoACodificar.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<HBox>()
    }

    fun actualizar(itemCreditoACodificar: ItemACodificarUI.ItemCreditoACodificar): Node
    {
        nombre.text = itemCreditoACodificar.nombre
        precio.text = itemCreditoACodificar.valorPagado.comoDineroFormateado(0)
        cantidad.text = itemCreditoACodificar.cantidad.toString()

        return raiz
    }
}