package co.smartobjects.ui.javafx.controladores.carritocreditos

import co.smartobjects.ui.javafx.ModeloDeSeleccionVacioListView
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditosInmutableUI
import co.smartobjects.ui.modelos.carritocreditos.ItemCreditoUI
import javafx.fxml.FXMLLoader
import javafx.scene.control.ContentDisplay
import javafx.scene.control.ListCell
import javafx.scene.control.ListView


internal class CeldaItemCredito : ListCell<ItemCreditoUI>()
{
    private val vista = ControladorItemCredito()

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

    override fun updateItem(item: ItemCreditoUI?, empty: Boolean)
    {
        super.updateItem(item, empty)
        text = null
    }
}

internal class ControladorCarritoDeCreditos : ListView<ItemCreditoUI>()
{
    init
    {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/layouts/carritocreditos/carritoDeCreditos.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<ListView<ItemCreditoUI>>()
    }

    fun inicializar(carritoDeCreditosUIInmutable: CarritoDeCreditosInmutableUI)
    {
        setCellFactory { CeldaItemCredito() }
        selectionModel = ModeloDeSeleccionVacioListView()

        carritoDeCreditosUIInmutable
            .creditosTotales
            .observarEnFx()
            .subscribe {
                items.clear()
                items.addAll(it)
            }
    }
}