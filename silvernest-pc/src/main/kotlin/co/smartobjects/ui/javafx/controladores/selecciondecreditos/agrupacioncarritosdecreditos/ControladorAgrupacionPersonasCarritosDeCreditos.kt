package co.smartobjects.ui.javafx.controladores.selecciondecreditos.agrupacioncarritosdecreditos

import co.smartobjects.ui.javafx.*
import co.smartobjects.ui.javafx.controladores.genericos.ControladorListaFiltrable
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.AgrupacionPersonasCarritosDeCreditosUI
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.PersonaConCarrito
import com.jfoenix.controls.JFXButton
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox


internal class ControladorAgrupacionPersonasCarritosDeCreditos : VBox()
{
    @FXML
    internal lateinit var listaPersonasCarritosDeCreditos: ControladorListaFiltrable<PersonaConCarrito>
    @FXML
    internal lateinit var total: Label
    @FXML
    internal lateinit var impuesto: Label
    @FXML
    internal lateinit var pagado: Label
    @FXML
    internal lateinit var granTotal: Label
    @FXML
    internal lateinit var contendorBotonesAgregando: HBox
    @FXML
    internal lateinit var botonCancelar: JFXButton
    @FXML
    internal lateinit var botonConfirmar: JFXButton
    @FXML
    internal lateinit var panelTooltipoBotonPagar: StackPane
    @FXML
    internal lateinit var botonPagar: JFXButton

    private val tooltipBotonPagar = inicializarTooltipConTexto("Todas las personas deben tener créditos confirmados")


    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/selecciondecreditos/agrupacioncarritosdecreditos/agrupacionPersonasCarritosDeCreditos.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()
    }

    fun inicializar(agrupacionPersonasCarritoDeCreditos: AgrupacionPersonasCarritosDeCreditosUI, schedulerBackground: Scheduler = Schedulers.io())
    {
        listaPersonasCarritosDeCreditos.inicializar(
                agrupacionPersonasCarritoDeCreditos.personasConCarritos,
                agrupacionPersonasCarritoDeCreditos.estaAgregandoProducto.usarSchedulersEnUI(schedulerBackground)
                                                   ) {
            ControladorItemPersonaConCarrito().inicializar(it)
        }

        agrupacionPersonasCarritoDeCreditos.estaAgregandoProducto.usarSchedulersEnUI(schedulerBackground).subscribe {
            contendorBotonesAgregando.isVisible = it
        }

        botonCancelar.setOnAction { agrupacionPersonasCarritoDeCreditos.cancelarCreditosAgregados() }
        botonConfirmar.setOnAction { agrupacionPersonasCarritoDeCreditos.confirmarCreditosAgregados() }

        inicializarLabelMonetario(total, agrupacionPersonasCarritoDeCreditos.totalSinImpuesto.usarSchedulersEnUI(schedulerBackground))
        inicializarLabelMonetario(impuesto, agrupacionPersonasCarritoDeCreditos.impuestoTotal.usarSchedulersEnUI(schedulerBackground))
        inicializarLabelMonetario(pagado, agrupacionPersonasCarritoDeCreditos.valorYaPagado.usarSchedulersEnUI(schedulerBackground))

        inicializarLabelMonetario(granTotal, agrupacionPersonasCarritoDeCreditos.saldo.usarSchedulersEnUI(schedulerBackground)) // Se muestra es el saldo

        agrupacionPersonasCarritoDeCreditos.todosLosCreditosEstanPagados.usarSchedulersEnUI(schedulerBackground).subscribe {
            botonPagar.text = if (it) "CODIFICAR" else "PAGAR"
        }

        botonPagar.inicializarBindingAccion(agrupacionPersonasCarritoDeCreditos.puedePagar) { agrupacionPersonasCarritoDeCreditos.pagar() }
        agrupacionPersonasCarritoDeCreditos.puedePagar.observarEnFx().subscribe {
            if (it)
            {
                Tooltip.uninstall(panelTooltipoBotonPagar, tooltipBotonPagar)
            }
            else
            {
                Tooltip.install(panelTooltipoBotonPagar, tooltipBotonPagar)
            }
        }
    }
}