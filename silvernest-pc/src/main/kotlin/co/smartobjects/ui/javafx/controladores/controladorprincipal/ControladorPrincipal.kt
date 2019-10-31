package co.smartobjects.ui.javafx.controladores.controladorprincipal

import co.smartobjects.ui.javafx.controladores.login.ControladorLogin
import co.smartobjects.ui.javafx.dependencias.DependenciasDePantallas
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujoInterno
import co.smartobjects.ui.javafx.observarEnFx
import io.datafx.controller.ViewController
import io.datafx.controller.flow.Flow
import io.datafx.controller.flow.container.AnimatedFlowContainer
import io.datafx.controller.flow.container.ContainerAnimations
import io.datafx.controller.flow.context.FXMLViewFlowContext
import io.datafx.controller.flow.context.ViewFlowContext
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javafx.fxml.FXML
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.util.Duration
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@ViewController(value = ControladorPrincipal.LAYOUT, title = "Silvernest")
internal class ControladorPrincipal
{
    companion object
    {
        const val LAYOUT = "/layouts/controladorprincipal/controladorPrincipal.fxml"

        const val CTX_PANEL_CONTENIDO = "panel_contenido"
        const val CTX_TOOLBAR = "toolbar"
        const val CTX_CONTENDOR_PRINCIPAL = "contendor_principal"
    }


    @FXMLViewFlowContext
    private lateinit var contextoFlujo: ViewFlowContext

    @FXML
    private lateinit var raiz: BorderPane

    @FXML
    private lateinit var toolbar: ControladorToolbar

    @FXML
    private lateinit var panelContendorPrincipal: StackPane

    @FXML
    internal lateinit var contenidoFlujo: StackPane


    private val disposables = CompositeDisposable()

    @PostConstruct
    fun inicializar()
    {
        val flujoAplicacion = Flow(ControladorLogin::class.java)
        val controladorFlujo = flujoAplicacion.createHandler(contextoFlujo)
        val controladorDeFlujoAcciones = ControladorDeFlujoInterno(controladorFlujo)

        contextoFlujo.register(controladorDeFlujoAcciones)
        contextoFlujo.register(CTX_TOOLBAR, toolbar)
        contextoFlujo.register(CTX_CONTENDOR_PRINCIPAL, panelContendorPrincipal)
        contextoFlujo.register(CTX_PANEL_CONTENIDO, contenidoFlujo)
        contextoFlujo.register(DependenciasDePantallas())

        toolbar.configurarMenu(controladorDeFlujoAcciones)
        toolbar.debeVolverAPantallaAnterior.observarEnFx().subscribe {
            controladorFlujo.navigateBack()
        }.addTo(disposables)

        contenidoFlujo
            .children
            .add(controladorFlujo.start(AnimatedFlowContainer(Duration.millis(300.0), ContainerAnimations.FADE)))
    }

    @PreDestroy
    fun limpiar()
    {
        disposables.dispose()
    }
}