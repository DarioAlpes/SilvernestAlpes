package co.smartobjects.ui.javafx.controladores

import co.smartobjects.ui.javafx.inicializarBindingCampoEntero
import co.smartobjects.ui.javafx.inicializarBindingLabelError
import co.smartobjects.ui.modelos.fechas.FechaUI
import co.smartobjects.ui.modelos.mapNotification
import co.smartobjects.utilidades.Opcional
import com.jfoenix.controls.JFXTextField
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.threeten.bp.LocalDate

// Constructor para inyectar proceso en pruebas, no se debe usar en ejecución normal
internal class ControladorFechaSegunCampos : VBox()
{
    @FXML
    internal lateinit var campoDia: JFXTextField
    @FXML
    internal lateinit var campoMes: JFXTextField
    @FXML
    internal lateinit var campoAño: JFXTextField
    @FXML
    internal lateinit var labelError: Label

    private lateinit var fechaInicial: LocalDate

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/fechaSegunCampos.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()


        campoDia.disableProperty().bind(disableProperty())
        campoMes.disableProperty().bind(disableProperty())
        campoAño.disableProperty().bind(disableProperty())
    }


    fun inicializarSegunModeloYNombre(nombreCampo: String, fechaInicial: LocalDate, fechaUI: FechaUI, accion: () -> Unit)
    {
        this.fechaInicial = fechaInicial
        campoDia.promptText = "${campoDia.promptText} $nombreCampo"
        campoMes.promptText = "${campoMes.promptText} $nombreCampo"
        campoAño.promptText = "${campoAño.promptText} $nombreCampo"

        campoDia.inicializarBindingCampoEntero(
                fechaInicial.dayOfMonth.toString(),
                fechaUI.dia.mapNotification { it.toString() },
                { fechaUI.cambiarDia(it) },
                accion ,
                2
                                                        )

        campoMes.inicializarBindingCampoEntero(
                fechaInicial.monthValue.toString(),
                fechaUI.mes.mapNotification { it.toString() },
                { fechaUI.cambiarMes(it) },
                accion,
                2
                                                        )

        campoAño.inicializarBindingCampoEntero(
                fechaInicial.year.toString(),
                fechaUI.año.mapNotification { it.toString() },
                { fechaUI.cambiarAño(it) },
                accion,
                4
                                                        )
        labelError.inicializarBindingLabelError(fechaUI.fecha.skipWhile { !it.isOnError }.map { Opcional.DeNullable(it.error?.message) })
    }

    fun reiniciar()
    {
        campoDia.text = fechaInicial.dayOfMonth.toString()
        campoMes.text = fechaInicial.monthValue.toString()
        campoAño.text = fechaInicial.year.toString()

        labelError.text = ""

        campoDia.resetValidation()
        campoMes.resetValidation()
        campoAño.resetValidation()
    }
}