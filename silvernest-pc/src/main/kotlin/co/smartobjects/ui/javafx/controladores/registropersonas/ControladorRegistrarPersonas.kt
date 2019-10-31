package co.smartobjects.ui.javafx.controladores.registropersonas

import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.logica.fondos.precios.CalculadorGrupoClientesEnMemoria
import co.smartobjects.logica.personas.CalculadorGrupoEdadEnMemoria
import co.smartobjects.red.clientes.personas.PersonasAPI
import co.smartobjects.red.clientes.personas.PersonasDeUnaCompraAPI
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.ui.javafx.AplicacionPrincipal
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorPrincipal
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorToolbar
import co.smartobjects.ui.javafx.controladores.genericos.ControladorItemEliminable
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.controladores.selecciondecreditos.ControladorSeleccionCreditos
import co.smartobjects.ui.javafx.dependencias.DependenciasBD
import co.smartobjects.ui.javafx.dependencias.agregarDependenciaDePantallaMultiplesUsos
import co.smartobjects.ui.javafx.dependencias.agregarDependenciaDePantallaUnSoloUso
import co.smartobjects.ui.javafx.dependencias.obtenerCondicionalDependenciaDePantalla
import co.smartobjects.ui.javafx.inicializarBindingListaTransformandoANodo
import co.smartobjects.ui.javafx.lectorbarras.LectorBarrasHoneyWellXenon1900
import co.smartobjects.ui.javafx.lectorbarras.ParserDocumento
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.javafx.usarSchedulersEnUI
import co.smartobjects.ui.modelos.registropersonas.ListaPersonasAgregadasUI
import co.smartobjects.ui.modelos.registropersonas.ListaPersonasAgregadasUIConSujetos
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import io.datafx.controller.ViewController
import io.datafx.controller.flow.context.ActionHandler
import io.datafx.controller.flow.context.FXMLViewFlowContext
import io.datafx.controller.flow.context.FlowActionHandler
import io.datafx.controller.flow.context.ViewFlowContext
import io.reactivex.rxjavafx.observers.JavaFxObserver
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@ViewController(value = ControladorRegistrarPersonas.LAYOUT, title = ControladorRegistrarPersonas.TITULO)
class ControladorRegistrarPersonas
{
    companion object
    {
        const val LAYOUT = "/layouts/registropersonas/registrarPersonas.fxml"
        const val TITULO = "REGISTRO DE PERSONAS"
    }

    @FXMLViewFlowContext
    private lateinit var contextoFlujo: ViewFlowContext

    @ActionHandler
    private lateinit var navegadorFlujo: FlowActionHandler


    @FXML
    internal lateinit var contenedorPersonas: VBox
    @FXML
    internal lateinit var dialogoDeEspera: DialogoDeEspera
    @FXML
    internal lateinit var listaFamiliares: ControladorListaFamiliares
    @FXML
    internal lateinit var controladorCrearPersonas: ControladorCrearPersona
    @FXML
    private lateinit var controladorBuscarPorTransaccion: ControladorBuscarPersonasPorNumeroDeTransaccion
    @FXML
    internal lateinit var botonIrAComprarCreditos: JFXButton

    private val snackbarErrores: JFXSnackbar by lazy {
        val contendorPrincipal = contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_CONTENDOR_PRINCIPAL) as StackPane
        JFXSnackbar(contendorPrincipal).apply {
            (popupContainer.children.last { it is Group } as Group).children[0].styleClass.add("jfx-snackbar-content-error")
        }
    }


    internal lateinit var modeloListaPersonasAgregadas: ListaPersonasAgregadasUI

    private lateinit var lectorBarras: LectorBarrasHoneyWellXenon1900

    @PostConstruct
    fun inicializar()
    {
        val dependenciasBD = contextoFlujo.applicationContext.getRegisteredObject(DependenciasBD::class.java) as DependenciasBD
        val dependenciasDeRed = contextoFlujo.applicationContext.getRegisteredObject(ManejadorDePeticiones::class.java) as ManejadorDePeticiones
        lectorBarras =
                contextoFlujo.applicationContext.getRegisteredObject(AplicacionPrincipal.CTX_LECTOR_BARRAS)
                        as LectorBarrasHoneyWellXenon1900

        val calculadorGrupoCliente =
                CalculadorGrupoClientesEnMemoria(
                        dependenciasBD.repositorioGrupoClientes.listar(AplicacionPrincipal.CONFIGURACION_AMBIENTE.idCliente).toList(),
                        CalculadorGrupoEdadEnMemoria(
                                dependenciasBD.repositorioValoresGruposEdad.listar(AplicacionPrincipal.CONFIGURACION_AMBIENTE.idCliente).toList()
                                                    )
                                                )
        modeloListaPersonasAgregadas = ListaPersonasAgregadasUIConSujetos(calculadorGrupoCliente)

        val posiblesPersonasAnteriormenteAgregadas =
                contextoFlujo
                    .obtenerCondicionalDependenciaDePantalla(PersonasAnteriores::class.java)
                    ?.personas

        posiblesPersonasAnteriormenteAgregadas?.forEach {
            modeloListaPersonasAgregadas.agregarPersona(it.persona)
        }


        inicializarToolbar()

        inicializarPanelCrearPersona(AplicacionPrincipal.CONFIGURACION_AMBIENTE.idCliente, dependenciasDeRed.apiDePersonas)
        inicializarPanelBuscarPorTransaccion(dependenciasDeRed.apiDePersonasDeUnaCompra)

        listaFamiliares.inicializar(dependenciasDeRed.apiDePersonas)
        listaFamiliares.modeloProcesoConsultarFamiliares.personaConsultada.usarSchedulersEnUI(Schedulers.io()).subscribe {
            modeloListaPersonasAgregadas.agregarPersona(it.persona)
            listaFamiliares.modeloProcesoConsultarFamiliares.agregarPersonaConFamiliares(it)
        }

        contenedorPersonas
            .inicializarBindingListaTransformandoANodo(modeloListaPersonasAgregadas.personasRegistradas.observarEnFx()) {
                val controladorInformacionPersonaConGrupo = ControladorInformacionPersonaConGrupo()
                controladorInformacionPersonaConGrupo.asignarPersonaConGrupoCliente(it)

                ControladorItemEliminable().apply {
                    inicializar(controladorInformacionPersonaConGrupo) {
                        modeloListaPersonasAgregadas.eliminarPersona(it.persona)
                        listaFamiliares.modeloProcesoConsultarFamiliares.eliminarPersonaConFamiliares(it.persona)
                    }
                }
            }

        inicializarBindingDialogoDeEspera()
        inicializarBotonIrAComprarCreditos()
        inicializarLectorDeBarras()
    }

    private fun inicializarToolbar()
    {
        val toolbar = contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_TOOLBAR) as ControladorToolbar

        toolbar.cambiarTitulo(TITULO)
        toolbar.puedeVolver(true)
        toolbar.mostrarMenu(true)
        toolbar.puedeSincronizar(false)
        toolbar.puedeSeleccionarUbicacion(false)
    }

    private fun inicializarPanelCrearPersona(idCliente: Long, personasApi: PersonasAPI)
    {
        controladorCrearPersonas.inicializar(idCliente, personasApi)
        controladorCrearPersonas.procesoCrearPersona.personaCreada.observarEnFx().subscribe {
            modeloListaPersonasAgregadas.agregarPersona(it)
            controladorCrearPersonas.reiniciarFormulario()
            listaFamiliares
                .modeloProcesoConsultarFamiliares
                .agregarPersonaConFamiliares(PersonaConFamiliares(it, controladorCrearPersonas.procesoCrearPersona.familiares))
        }
    }

    private fun inicializarPanelBuscarPorTransaccion(personasDeUnaCompraAPI: PersonasDeUnaCompraAPI)
    {
        /*controladorBuscarPorTransaccion.inicializar(personasDeUnaCompraAPI)
        controladorBuscarPorTransaccion.procesoConsultarPersonasPorNumeroTransaccion.personasConsultadas.observarEnFx().subscribe {
            modeloListaPersonasAgregadas.agregarPersonas(it)
        }*/
    }

    private fun inicializarBindingDialogoDeEspera()
    {
        dialogoDeEspera.inicializarSegunMultiplesBindings(
                listOf(
                        controladorCrearPersonas.informacionBindingDialogoDeEspera,
                        //controladorBuscarPorTransaccion.informacionBindingDialogoDeEspera,
                        listaFamiliares.informacionBindingDialogoDeEspera
                      )
                                                         )
    }

    private fun inicializarBotonIrAComprarCreditos()
    {
        val observableHayPersonasAgregadas = modeloListaPersonasAgregadas.personasRegistradas.map { it.isEmpty() }
        botonIrAComprarCreditos.disableProperty().bind(JavaFxObserver.toBinding(observableHayPersonasAgregadas))
        botonIrAComprarCreditos.setOnAction {

            val dependenciasDeSeleccionDeCreditos =
                    ControladorSeleccionCreditos.Dependencias(modeloListaPersonasAgregadas.personasActuales)

            val personasAnteriores =
                    PersonasAnteriores(modeloListaPersonasAgregadas.personasActuales)

            contextoFlujo.agregarDependenciaDePantallaMultiplesUsos(dependenciasDeSeleccionDeCreditos)
            contextoFlujo.agregarDependenciaDePantallaUnSoloUso(personasAnteriores)
            navegadorFlujo.navigate(ControladorSeleccionCreditos::class.java)
        }
    }

    private fun inicializarLectorDeBarras()
    {
        lectorBarras
            .inicializar()
            .usarSchedulersEnUI(Schedulers.io())
            .subscribe { estado ->
                when (estado)
                {
                    is LectorBarrasHoneyWellXenon1900.EstadoLector.Inicializado  ->
                    {
                        estado.lecturas.subscribe {
                            val personaParseada = ParserDocumento(AplicacionPrincipal.CONFIGURACION_AMBIENTE.idCliente, it).parsearPersona()
                            if (personaParseada != null)
                            {
                                controladorCrearPersonas
                                    .procesoCrearPersona
                                    .consultarPersonaPorDocumentoYCombinarConLecturaBarras(personaParseada)
                            }
                        }
                    }
                    LectorBarrasHoneyWellXenon1900.EstadoLector.PuertoEnUso      ->
                    {
                        snackbarErrores
                            .show(
                                    "El puerto COM del lector de barras se encuentra en uso. Verificar que antes de iniciar el aplicativo no hayan más instancias de 'Silvernest.exe' activas",
                                    5000L
                                 )
                    }
                    LectorBarrasHoneyWellXenon1900.EstadoLector.FalloDesconocido ->
                    {
                        snackbarErrores.fireEvent(JFXSnackbar.SnackbarEvent("No se pudo configurar el lector de código de barras para su uso"))
                    }
                }
            }
    }

    @PreDestroy
    fun liberarRecursos()
    {
        lectorBarras.apagarLector()
        snackbarErrores.close()
    }

    class PersonasAnteriores(val personas: List<PersonaConGrupoCliente>)
}