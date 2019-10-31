package co.smartobjects.ui.javafx.controladores.registropersonas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.ui.modelos.registropersonas.ProcesoConsultarFamiliares
import com.jfoenix.controls.JFXButton
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import java.net.URL
import java.util.*

internal class ControladorInformacionFamiliar : Initializable
{
    companion object
    {
        const val LAYOUT = "/layouts/registropersonas/informacionFamiliar.fxml"
    }

    @FXML
    internal lateinit var labelNombreCompleto: Label
    @FXML
    internal lateinit var labelDocumento: Label
    @FXML
    internal lateinit var labelEdad: Label
    @FXML
    internal lateinit var iconoAgregar: JFXButton

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
    }

    internal fun asignarFamiliar(familiar: Persona, procesoConsultarFamiliares: ProcesoConsultarFamiliares)
    {
        labelNombreCompleto.text = familiar.nombreCompleto
        labelDocumento.text = "${familiar.tipoDocumento} ${familiar.numeroDocumento}"
        labelEdad.text = familiar.edad.toString()
        iconoAgregar.setOnAction { procesoConsultarFamiliares.intentarConsultarFamiliarPorDocumento(familiar) }
    }
}