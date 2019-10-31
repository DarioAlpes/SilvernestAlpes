package co.smartobjects.ui.javafx.controladores.registropersonas

import co.smartobjects.red.clientes.personas.PersonasDeUnaCompraAPI
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.inicializarBindingAccion
import co.smartobjects.ui.javafx.inicializarBindingCampoRequerido
import co.smartobjects.ui.javafx.inicializarBindingLabelError
import co.smartobjects.ui.modelos.registropersonas.ProcesoConsultarPersonasPorNumeroTransaccion
import co.smartobjects.ui.modelos.registropersonas.ProcesoConsultarPersonasPorNumeroTransaccionConSujetos
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.VBox

// Constructor para inyectar proceso en pruebas, no se debe usar en ejecución normal
internal class ControladorBuscarPersonasPorNumeroDeTransaccion : VBox()
{
    internal lateinit var procesoConsultarPersonasPorNumeroTransaccion: ProcesoConsultarPersonasPorNumeroTransaccion

    @FXML
    internal lateinit var campoNumeroTransaccion: JFXTextField
    @FXML
    internal lateinit var botonBuscar: JFXButton
    @FXML
    internal lateinit var labelError: Label

    internal val informacionBindingDialogoDeEspera: DialogoDeEspera.InformacionBindingDialogoEspera<ProcesoConsultarPersonasPorNumeroTransaccion.Estado>by lazy {
        val mensajeConsultar = DialogoDeEspera.InformacionMensajeEspera(ProcesoConsultarPersonasPorNumeroTransaccion.Estado.CONSULTANDO_PERSONAS, "Consultando personas ...")
        DialogoDeEspera.InformacionBindingDialogoEspera(procesoConsultarPersonasPorNumeroTransaccion.estado, listOf(mensajeConsultar))
    }

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/registropersonas/buscarPorTransaccion.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()
    }

    fun inicializar(personasDeUnaCompraAPI: PersonasDeUnaCompraAPI)
    {
        procesoConsultarPersonasPorNumeroTransaccion = ProcesoConsultarPersonasPorNumeroTransaccionConSujetos(personasDeUnaCompraAPI)

        campoNumeroTransaccion.inicializarBindingCampoRequerido(
                "",
                procesoConsultarPersonasPorNumeroTransaccion.numeroTransaccionPOS,
                { procesoConsultarPersonasPorNumeroTransaccion.cambiarNumeroTransaccionPOS(it) },
                { procesoConsultarPersonasPorNumeroTransaccion.intentarConsultarPersonasPorNumeroTransaccion() }
                                                               )

        botonBuscar.inicializarBindingAccion(procesoConsultarPersonasPorNumeroTransaccion.puedeConsultarPersonas) {
            procesoConsultarPersonasPorNumeroTransaccion.intentarConsultarPersonasPorNumeroTransaccion()
        }

        labelError.inicializarBindingLabelError(procesoConsultarPersonasPorNumeroTransaccion.errorGlobal)
    }
}