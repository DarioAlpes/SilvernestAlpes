package co.smartobjects.ui.javafx.controladores.genericos

import co.smartobjects.ui.javafx.inicializarBindingCambiarMensaje
import co.smartobjects.ui.javafx.inicializarBindingCambiarMensajeSegunEstado
import co.smartobjects.ui.javafx.inicializarBindingEsVisible
import co.smartobjects.ui.javafx.inicializarBindingMostrarNodeSegunEstadoAccion
import co.smartobjects.utilidades.Opcional
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXSpinner
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.reactivex.Observable
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label


internal class DialogoDeEspera : JFXDialog()
{
    @FXML
    internal lateinit var labelTituloDialogo: Label

    @FXML
    internal lateinit var spinnerEspera: JFXSpinner

    @FXML
    internal lateinit var iconoError: FontAwesomeIconView

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/genericos/dialogoEspera.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<JFXDialog>()
    }


    fun <T> inicializarSegunEstado(observableEstado: Observable<T>, mensajesAMostrar: Set<InformacionMensajeEspera<T>>)
    {
        mostrarSpinner()

        inicializarBindingMostrarNodeSegunEstadoAccion(observableEstado, mensajesAMostrar.map { it.estadoAMostrar }.toSet())

        labelTituloDialogo.inicializarBindingCambiarMensajeSegunEstado(observableEstado, mensajesAMostrar)
    }

    fun inicializarSegunMultiplesBindings(listaEstados: List<InformacionBindingDialogoEspera<*>>)
    {
        mostrarSpinner()

        val observableDialogoVisible =
                Observable.combineLatest(
                        listaEstados.map { binding ->
                            binding.observableEstado.map<Boolean> { estadoActual ->
                                binding.mensajesAMostrar.any { it.estadoAMostrar == estadoActual }
                            }
                        })
                {
                    it.any { it == true }
                }

        val observableMensaje =
                Observable.combineLatest(
                        listaEstados.map { binding ->
                            binding.observableEstado.map<Opcional<String>> { estadoActual ->
                                Opcional.DeNullable(binding.mensajesAMostrar.firstOrNull { it.estadoAMostrar == estadoActual }?.mensajeAMostrar)
                            }
                        }) {
                    it.map {
                        @Suppress("UNCHECKED_CAST")
                        (it as Opcional<String>)
                    }.firstOrNull {
                        !it.esVacio
                    }?.valor ?: ""
                }

        inicializarBindingEsVisible(observableDialogoVisible)

        labelTituloDialogo.inicializarBindingCambiarMensaje(observableMensaje)
    }

    fun mostrarSpinner()
    {
        spinnerEspera.isVisible = true
        iconoError.isVisible = false
    }

    fun mostrarIconoError()
    {
        spinnerEspera.isVisible = false
        iconoError.isVisible = true
    }

    data class InformacionMensajeEspera<T>(val estadoAMostrar: T, val mensajeAMostrar: String)

    class InformacionBindingDialogoEspera<T>(val observableEstado: Observable<T>, val mensajesAMostrar: List<InformacionMensajeEspera<T>>)
}