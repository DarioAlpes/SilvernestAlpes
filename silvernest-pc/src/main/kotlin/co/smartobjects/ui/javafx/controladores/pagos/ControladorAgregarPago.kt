package co.smartobjects.ui.javafx.controladores.pagos

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.ui.javafx.*
import co.smartobjects.ui.modelos.mapNotification
import co.smartobjects.ui.modelos.pagos.ModeloUIConListaDePagos
import co.smartobjects.ui.modelos.pagos.ProcesoAgregarPago
import co.smartobjects.ui.modelos.pagos.ProcesoAgregarPagoConSujetos
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTextField
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.StackPane


internal class ControladorAgregarPago : StackPane()
{
    internal var mockProcesoAgregarPago: ProcesoAgregarPago? = null
    internal val procesoAgregarPago: ProcesoAgregarPago by lazy {
        mockProcesoAgregarPago ?: ProcesoAgregarPagoConSujetos()
    }

    @FXML
    internal lateinit var campoNumeroTransaccion: JFXTextField
    @FXML
    internal lateinit var campoValorPagado: JFXTextField
    @FXML
    internal lateinit var grupoOpcionesMedioPago: ToggleGroup
    @FXML
    internal lateinit var botonReiniciar: JFXButton
    @FXML
    internal lateinit var botonAgregarPago: JFXButton
    @FXML
    internal lateinit var labelError: Label


    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/pagos/agregarPago.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<StackPane>()
    }

    fun inicializar(modeloConListaDePagos: ModeloUIConListaDePagos)
    {
        campoValorPagado.inicializarBindingCampoNumericoPositivo(
                "",
                procesoAgregarPago.pago.valorPagado.mapNotification { it.toString() },
                { procesoAgregarPago.pago.cambiarValorPagado(it) },
                { procesoAgregarPago.intentarAgregarPago(modeloConListaDePagos) }
                                                                )

        with(grupoOpcionesMedioPago)
        {
            selectedToggleProperty().addListener { _, _, toggleSeleccionado ->
                toggleSeleccionado?.run {
                    procesoAgregarPago.pago.cambiarMetodoPago(Pago.MetodoDePago.valueOf(userData.toString()))
                }
            }

            selectToggle(toggles.first())
        }

        campoNumeroTransaccion.inicializarBindingCampoRequerido(
                "",
                procesoAgregarPago.pago.numeroTransaccionPOS,
                { procesoAgregarPago.pago.cambiarNumeroTransaccionPOS(it) },
                { procesoAgregarPago.intentarAgregarPago(modeloConListaDePagos) }
                                                               )

        botonReiniciar.setOnAction {
            reiniciarFormulario()
        }

        botonAgregarPago.inicializarBindingAccion(procesoAgregarPago.puedeAgregarPago) {
            procesoAgregarPago.intentarAgregarPago(modeloConListaDePagos)
        }

        labelError.inicializarBindingLabelError(procesoAgregarPago.errorGlobal)

        procesoAgregarPago.estado.filter { it === ProcesoAgregarPago.Estado.PAGO_AGREGADO }.observarEnFx().subscribe {
            reiniciarFormulario()
        }
    }

    private fun reiniciarFormulario()
    {
        campoValorPagado.clear()
        campoValorPagado.resetValidation()

        grupoOpcionesMedioPago.selectToggle(grupoOpcionesMedioPago.toggles.first())

        campoNumeroTransaccion.clear()
        campoNumeroTransaccion.resetValidation()

        labelError.text = ""
    }
}