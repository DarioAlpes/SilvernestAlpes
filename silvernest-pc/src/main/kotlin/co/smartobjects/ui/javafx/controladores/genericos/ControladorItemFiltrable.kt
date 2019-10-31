package co.smartobjects.ui.javafx.controladores.genericos

import co.smartobjects.ui.javafx.bindEstaHabilitado
import co.smartobjects.ui.javafx.inicializarBindingEsVisible
import co.smartobjects.ui.javafx.inicializarBindingSeleccion
import co.smartobjects.ui.modelos.ItemFiltrableUI
import com.jfoenix.controls.JFXCheckBox
import io.reactivex.Observable
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane

internal class ControladorItemFiltrable<T>
{
    companion object
    {
        const val LAYOUT = "/layouts/genericos/itemFiltrable.fxml"
    }

    @FXML
    internal lateinit var raiz: Pane
    @FXML
    internal lateinit var checkSeleccion: JFXCheckBox
    @FXML
    internal lateinit var panelItem: StackPane

    internal fun inicializar(transformarANodo: (T) -> Node, itemFiltrable: ItemFiltrableUI<T>)
    {
        panelItem.children.setAll(transformarANodo(itemFiltrable.item))
        raiz.bindEstaHabilitado(itemFiltrable.habilitado)
        checkSeleccion.inicializarBindingSeleccion(
                itemFiltrable.seleccionado,
                { itemFiltrable.seleccionar() },
                { itemFiltrable.deseleccionar() }
                                                  )
    }

    internal fun inicializar(transformarANodo: (T) -> Node, itemFiltrable: ItemFiltrableUI<T>, observableEsVisible: Observable<Boolean>)
    {
        inicializar(transformarANodo, itemFiltrable)
        checkSeleccion.inicializarBindingEsVisible(observableEsVisible)
    }
}