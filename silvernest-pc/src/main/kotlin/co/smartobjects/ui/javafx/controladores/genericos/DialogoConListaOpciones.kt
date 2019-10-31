package co.smartobjects.ui.javafx.controladores.genericos

import co.smartobjects.entidades.ubicaciones.Ubicacion
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXSpinner
import io.reactivex.subjects.PublishSubject
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.input.MouseButton


internal class DialogoConListaOpciones<TipoItem> : JFXDialog()
{
    @FXML
    internal lateinit var labelTituloDialogo: Label

    @FXML
    internal lateinit var spinnerEspera: JFXSpinner

    @FXML
    internal lateinit var listado: JFXListView<TextoEnLista<TipoItem>>

    private var eventosItemSeleccionado = PublishSubject.create<TipoItem>()
    val itemSeleccionado = eventosItemSeleccionado.hide()

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/genericos/dialogoConListaOpciones.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<JFXDialog>()

        listado.setOnMouseClicked {
            if (it.button == MouseButton.PRIMARY)
            {
                val posibleItem = listado.selectionModel?.selectedItem?.item

                if (posibleItem != null)
                {
                    eventosItemSeleccionado.onNext(posibleItem)
                }
            }
        }
    }

    fun mostrarSpinner()
    {
        spinnerEspera.isVisible = true
        listado.isVisible = false
    }

    fun mostrarListado()
    {
        spinnerEspera.isVisible = false
        listado.isVisible = true
    }

    fun reemplazarItems(nuevosItems: List<TextoEnLista<TipoItem>>, itemSeleccionado: TipoItem? = null)
    {
        listado.items.clear()
        listado.items.addAll(nuevosItems)
        if (itemSeleccionado != null)
        {
            val indiceSeleccionado = nuevosItems.indexOfFirst { it.item == itemSeleccionado }
            if (indiceSeleccionado != -1)
            {
                listado.selectionModel.select(indiceSeleccionado)
            }
        }
    }

    abstract class TextoEnLista<T>
    {
        abstract val item: T
        abstract fun darTextoParaLista(): String
        override fun toString(): String = darTextoParaLista()
    }
}

internal class UbicacionEnLista(override val item: Ubicacion) : DialogoConListaOpciones.TextoEnLista<Ubicacion>()
{
    override fun darTextoParaLista() = item.nombre
}