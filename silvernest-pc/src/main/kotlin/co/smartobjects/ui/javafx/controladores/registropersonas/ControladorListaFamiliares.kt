package co.smartobjects.ui.javafx.controladores.registropersonas

import co.smartobjects.red.clientes.personas.PersonasAPI
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.inicializarBindingLabelError
import co.smartobjects.ui.javafx.inicializarBindingListaTransformandoANodo
import co.smartobjects.ui.modelos.registropersonas.ProcesoConsultarFamiliares
import co.smartobjects.ui.modelos.registropersonas.ProcesoConsultarFamiliaresConSujetos
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

// Constructor para inyectar proceso en pruebas, no se debe usar en ejecución normal
internal class ControladorListaFamiliares : VBox()
{
    // Se usa proceso inyectado para el caso de pruebas. En ejecución no se debe inyectar nada
    internal lateinit var modeloProcesoConsultarFamiliares: ProcesoConsultarFamiliares

    @FXML
    internal lateinit var contenedorFamiliares: VBox
    @FXML
    internal lateinit var labelError: Label

    internal val informacionBindingDialogoDeEspera: DialogoDeEspera.InformacionBindingDialogoEspera<ProcesoConsultarFamiliares.Estado> by lazy {
        DialogoDeEspera
            .InformacionBindingDialogoEspera(
                    modeloProcesoConsultarFamiliares.estado,
                    listOf(
                            DialogoDeEspera
                                .InformacionMensajeEspera(
                                        ProcesoConsultarFamiliares.Estado.CONSULTANDO_FAMILIAR,
                                        "Consultando familiar ..."
                                                         )
                          )
                                            )
    }

    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/registropersonas/listaFamiliares.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()
    }

    fun inicializar(personasApi: PersonasAPI)
    {
        modeloProcesoConsultarFamiliares = ProcesoConsultarFamiliaresConSujetos(personasApi)

        contenedorFamiliares
            .inicializarBindingListaTransformandoANodo(modeloProcesoConsultarFamiliares.familiares) {
                val loaderInformacionFamiliar = FXMLLoader(javaClass.getResource(ControladorInformacionFamiliar.LAYOUT))
                val raizFamiliar = loaderInformacionFamiliar.load<Pane>()
                val controladorInformacionFamiliar = loaderInformacionFamiliar.getController<ControladorInformacionFamiliar>()
                controladorInformacionFamiliar.asignarFamiliar(it, modeloProcesoConsultarFamiliares)
                raizFamiliar
            }

        labelError.inicializarBindingLabelError(modeloProcesoConsultarFamiliares.errorGlobal)
    }
}