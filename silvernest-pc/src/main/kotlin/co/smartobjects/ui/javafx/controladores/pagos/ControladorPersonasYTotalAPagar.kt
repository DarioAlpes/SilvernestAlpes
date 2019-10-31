package co.smartobjects.ui.javafx.controladores.pagos

import co.smartobjects.ui.javafx.comoDineroFormateado
import co.smartobjects.ui.javafx.controladores.genericos.ControladorListaFiltrable
import co.smartobjects.ui.javafx.controladores.registropersonas.ControladorInformacionPersonaConGrupo
import co.smartobjects.ui.javafx.inicializarLabelMonetario
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.javafx.usarSchedulersEnUI
import co.smartobjects.ui.modelos.pagos.TotalAPagarSegunPersonasUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoSeleccionCreditosUI
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.VBox

// Constructor para inyectar proceso en pruebas, no se debe usar en ejecución normal
internal class ControladorPersonasYTotalAPagar : VBox()
{
    @FXML
    internal lateinit var listaPersonas: ControladorListaFiltrable<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>
    @FXML
    internal lateinit var totalSinImpuestos: Label
    @FXML
    internal lateinit var impuesto: Label
    @FXML
    internal lateinit var total: Label
    @FXML
    internal lateinit var granTotal: Label
    @FXML
    internal lateinit var labelPersonasSeleccionadas: Label


    private val schedulerBackground: Scheduler = Schedulers.io()

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/pagos/personasYTotalAPagar.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()
    }

    fun inicializar(totalAPagarSegunPersonas: TotalAPagarSegunPersonasUI)
    {
        val listadoPersonas = Observable.just(totalAPagarSegunPersonas.listadoDePersonasConCreditos)
        listaPersonas.inicializar(listadoPersonas, Observable.just(true)) {
            ControladorInformacionPersonaConGrupo().apply {
                asignarPersonaConGrupoCliente(it.personaConGrupoCliente)
            }
        }

        inicializarLabelMonetario(totalSinImpuestos, totalAPagarSegunPersonas.totalSinImpuesto.usarSchedulersEnUI(schedulerBackground))
        inicializarLabelMonetario(impuesto, totalAPagarSegunPersonas.impuestoTotal.usarSchedulersEnUI(schedulerBackground))
        inicializarLabelMonetario(total, totalAPagarSegunPersonas.total.usarSchedulersEnUI(schedulerBackground))

        granTotal.text = totalAPagarSegunPersonas.granTotal.comoDineroFormateado(numeroEspacios = 1)

        totalAPagarSegunPersonas.listadoDePersonasConCreditos.numeroHabilitadosSeleccionados.observarEnFx().subscribe {
            labelPersonasSeleccionadas.text = "$it PERSONAS SELECCIONADAS"
        }
    }
}