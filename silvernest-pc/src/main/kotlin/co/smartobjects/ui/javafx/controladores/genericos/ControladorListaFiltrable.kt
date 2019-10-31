package co.smartobjects.ui.javafx.controladores.genericos

import co.smartobjects.ui.javafx.inicializarBindingEsVisible
import co.smartobjects.ui.javafx.inicializarBindingSeleccionarTodos
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.modelos.ListaFiltrableUI
import co.smartobjects.ui.modelos.ListaFiltrableUIConSujetos
import com.jfoenix.controls.JFXCheckBox
import io.reactivex.Observable
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox


internal class ControladorListaFiltrable<T> : VBox()
{
    private lateinit var modeloListaFiltrableSegunItems: ListaFiltrableUI<T>

    @FXML
    internal lateinit var contenedorItems: VBox
    @FXML
    internal lateinit var checkSeleccionarTodos: JFXCheckBox


    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/genericos/listaFiltrable.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()
    }


    internal fun inicializar(
            items: List<T>,
            posibleObservableSonVisibles: Observable<Boolean>? = null,
            transformarANodo: (T) -> Node
                            )
    {
        inicializar(
                Observable.just<ListaFiltrableUI<T>>(ListaFiltrableUIConSujetos(items)),
                posibleObservableSonVisibles,
                transformarANodo
                   )
    }

    internal fun inicializar(
            listaDeItems: Observable<ListaFiltrableUI<T>>,
            posibleObservableSonVisibles: Observable<Boolean>? = null,
            transformarANodo: (T) -> Node
                            )
    {
        listaDeItems.observarEnFx().subscribe { nuevaListaDeItems ->
            modeloListaFiltrableSegunItems = nuevaListaDeItems

            checkSeleccionarTodos.inicializarBindingSeleccionarTodos(nuevaListaDeItems)
            if (posibleObservableSonVisibles != null)
            {
                checkSeleccionarTodos.inicializarBindingEsVisible(posibleObservableSonVisibles)
            }
            contenedorItems.children.setAll(
                    nuevaListaDeItems.itemsFiltrables.map {
                        val loader = FXMLLoader(javaClass.getResource(ControladorItemFiltrable.LAYOUT))
                        val raiz = loader.load<Pane>()
                        val controladorItem = loader.getController<ControladorItemFiltrable<T>>()
                        if (posibleObservableSonVisibles == null)
                        {
                            controladorItem.inicializar(transformarANodo, it)
                        }
                        else
                        {
                            controladorItem.inicializar(transformarANodo, it, posibleObservableSonVisibles)
                        }
                        raiz
                    }
                                           )
        }
    }
}