package co.smartobjects.ui.javafx.controladores.genericos

import co.smartobjects.ui.javafx.hacerEscondible
import co.smartobjects.ui.javafx.inicializarBindingEsVisible
import co.smartobjects.ui.javafx.transiciones.TransicionAgitar
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import io.reactivex.Observable
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label


internal class DialogoPositivoNegativo : JFXDialog()
{
    @FXML
    internal lateinit var labelTituloDialogo: Label

    @FXML
    internal lateinit var botonNegativo: JFXButton

    @FXML
    internal lateinit var botonPositivo: JFXButton

    private val animacionAgitarDialogoLectorNFC by lazy { TransicionAgitar(this@DialogoPositivoNegativo) }

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/genericos/dialogoPositivoNegativo.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<JFXDialog>()
    }


    fun inicializar(
            observableVisibilidad: Observable<Boolean>,
            titulo: String,
            configuracionBotonNegativo: ConfiguracionBoton.Negativo?,
            configuracionBotonPositivo: ConfiguracionBoton.Positivo
                   )
    {
        //        observableVisibilidad.observarEnFx().subscribe {
        //            if (it) show() else close()
        //        }
        inicializarBindingEsVisible(observableVisibilidad)

        labelTituloDialogo.text = titulo

        if (configuracionBotonNegativo != null)
        {
            botonNegativo.text = configuracionBotonNegativo.titulo
            botonNegativo.setOnAction { configuracionBotonNegativo.accion() }
        }
        else
        {
            botonNegativo.hacerEscondible()
            botonNegativo.isVisible = false
        }

        botonPositivo.text = configuracionBotonPositivo.titulo
        botonPositivo.setOnAction { configuracionBotonPositivo.accion() }
    }

    fun agitar()
    {
        animacionAgitarDialogoLectorNFC.playFromStart()
    }

    sealed class ConfiguracionBoton(val titulo: String, val accion: () -> Unit)
    {
        class Negativo(titulo: String, accion: () -> Unit) : ConfiguracionBoton(titulo, accion)
        class Positivo(titulo: String, accion: () -> Unit) : ConfiguracionBoton(titulo, accion)
    }
}