package co.smartobjects.ui.javafx.controladores.codificacion.creditosacodificar

import co.smartobjects.ui.javafx.ModeloDeSeleccionVacioListView
import co.smartobjects.ui.modelos.codificacion.ItemACodificarUI
import javafx.fxml.FXMLLoader
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ListCell
import javafx.scene.control.ListView

internal class CeldaItemCredito : ListCell<ItemACodificarUI.ItemCreditoACodificar>()
{
    private val vista = ControladorItemCreditoACodificar()

    init
    {
        itemProperty().addListener { _, _, newValue ->
            if (newValue != null)
            {
                vista.actualizar(newValue)
            }
        }

        emptyProperty().addListener { _, _, estaVacia ->
            graphic = if (estaVacia) null else vista.raiz
        }

        contentDisplay = ContentDisplay.GRAPHIC_ONLY
    }

    override fun updateItem(item: ItemACodificarUI.ItemCreditoACodificar?, empty: Boolean)
    {
        super.updateItem(item, empty)
        text = null
    }
}

internal class ControladorCreditosACodificarDePersona : ListView<ItemACodificarUI.ItemCreditoACodificar>()
{
    init
    {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/layouts/codificacion/creditosacodificar/creditosACodificarDePersona.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<ListView<ItemACodificarUI.ItemCreditoACodificar>>()
    }

    fun inicializar(creditosACodificar: List<ItemACodificarUI.ItemCreditoACodificar>)
    {
        setCellFactory { CeldaItemCredito() }
        selectionModel = ModeloDeSeleccionVacioListView()

        items.clear()
        items.addAll(creditosACodificar)
    }
}