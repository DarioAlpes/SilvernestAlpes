package co.smartobjects.ui.javafx.controladores.controladorprincipal

import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujoInterno
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXPopup
import io.reactivex.subjects.PublishSubject
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.GridPane


internal class ControladorToolbar : GridPane()
{
    @FXML
    internal lateinit var botonVolver: JFXButton

    @FXML
    internal lateinit var titulo: Label

    @FXML
    internal lateinit var botonMenu: JFXButton

    private lateinit var menuToolbar: ControladorMenuToolbar

    private val eventosVolver = PublishSubject.create<Unit>()
    val debeVolverAPantallaAnterior = eventosVolver.hide()!!


    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/controladorprincipal/toolbar.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<GridPane>()


        botonVolver.setOnAction {
            if (botonVolver.isVisible)
            {
                eventosVolver.onNext(Unit)
            }
        }
    }

    fun configurarMenu(controladorFlujo: ControladorDeFlujoInterno)
    {
        menuToolbar = ControladorMenuToolbar(controladorFlujo)

        botonMenu.setOnAction {
            menuToolbar.mostrar(botonMenu, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, -12.0, 15.0)
        }
    }

    fun cambiarTitulo(textoNuevo: String)
    {
        titulo.text = textoNuevo
    }

    fun puedeVolver(mostrar: Boolean)
    {
        botonVolver.isVisible = mostrar
    }

    fun mostrarMenu(mostrar: Boolean)
    {
        botonMenu.isVisible = mostrar
    }

    fun puedeSincronizar(puede: Boolean)
    {
        menuToolbar.botonSincronizar.isVisible = puede
    }

    fun puedeSeleccionarUbicacion(puede: Boolean)
    {
        menuToolbar.botonSeleccionarUbicacion.isVisible = puede
    }
}