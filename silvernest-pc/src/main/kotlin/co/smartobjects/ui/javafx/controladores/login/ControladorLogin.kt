package co.smartobjects.ui.javafx.controladores.login

import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.ui.javafx.*
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorPrincipal
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorToolbar
import co.smartobjects.ui.javafx.controladores.genericos.CampoTextoConSugerencias
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.controladores.menuprincipal.ControladorMenuPrincipal
import co.smartobjects.ui.modelos.login.ProcesoLogin
import co.smartobjects.ui.modelos.login.ProcesoLoginConSujetos
import co.smartobjects.utilidades.Opcional
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXPasswordField
import io.datafx.controller.ViewController
import io.datafx.controller.flow.context.ActionHandler
import io.datafx.controller.flow.context.FXMLViewFlowContext
import io.datafx.controller.flow.context.FlowActionHandler
import io.datafx.controller.flow.context.ViewFlowContext
import io.reactivex.subjects.BehaviorSubject
import javafx.fxml.FXML
import javafx.scene.control.Label
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.coroutines.CoroutineContext


@ViewController(value = ControladorLogin.LAYOUT, title = ControladorLogin.TITULO)
internal class ControladorLogin : CoroutineScope
{
    protected lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.JavaFx

    companion object
    {
        const val LAYOUT = "/layouts/login/inicioDeSesion.fxml"
        const val TITULO = "INICIO DE SESIÓN"
    }

    @FXMLViewFlowContext
    internal lateinit var contextoFlujo: ViewFlowContext

    @ActionHandler
    internal lateinit var navegadorFlujo: FlowActionHandler

    // Se usa proceso inyectado para el caso de pruebas. En ejecución no se debe inyectar nada
    internal lateinit var procesoLogin: ProcesoLogin

    @FXML
    internal lateinit var campoUsuario: CampoTextoConSugerencias
    @FXML
    internal lateinit var campoContraseña: JFXPasswordField
    @FXML
    internal lateinit var botonIniciarSesion: JFXButton
    @FXML
    internal lateinit var labelError: Label
    @FXML
    internal lateinit var dialogoDeEspera: DialogoDeEspera


    @PostConstruct
    fun inicializar()
    {
        job = Job()

        inicializarToolbar()

        val usuarioActual =
                contextoFlujo.applicationContext
                    .getRegisteredObject(AplicacionPrincipal.CTX_EVENTOS_CAMBIO_DE_USUARIO)
                        as BehaviorSubject<Opcional<Usuario>>

        inicializarConDependencias()

        inicializarCampoDeUsuario()

        campoContraseña.inicializarBindingCampoRequerido(
                procesoLogin.credenciales.contraseña,
                { procesoLogin.credenciales.cambiarContraseña(it) },
                { procesoLogin.intentarLogin() }
                                                        )

        botonIniciarSesion.inicializarBindingAccion(procesoLogin.puedeIniciarSesion) { procesoLogin.intentarLogin() }

        val mensajeIniciarSesion =
                DialogoDeEspera
                    .InformacionMensajeEspera(ProcesoLogin.Estado.INICIANDO_SESION, "Iniciando sesión...")

        dialogoDeEspera.inicializarSegunEstado(procesoLogin.estado, setOf(mensajeIniciarSesion))

        labelError.inicializarBindingLabelError(procesoLogin.errorGlobal)

        procesoLogin.usuarioSesionIniciada.observarEnFx().subscribe {
            usuarioActual.onNext(Opcional.De(it))
            navegadorFlujo.navigate(ControladorMenuPrincipal::class.java)
        }
    }

    private fun inicializarToolbar()
    {
        val toolbar = contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_TOOLBAR) as ControladorToolbar

        toolbar.cambiarTitulo(TITULO)
        toolbar.puedeVolver(false)
        toolbar.mostrarMenu(false)
    }

    private fun inicializarConDependencias()
    {
        if (!::procesoLogin.isInitialized)
        {
            val loginApi = contextoFlujo.applicationContext.getRegisteredObject(ManejadorDePeticiones::class.java).apiDeUsuarios
            procesoLogin = ProcesoLoginConSujetos(loginApi)
        }
    }

    private fun inicializarCampoDeUsuario()
    {
        campoUsuario.requestFocus()
        campoUsuario.inicializarBindingCampoRequerido(
                "",
                procesoLogin.credenciales.usuario,
                { procesoLogin.credenciales.cambiarUsuario(it) },
                { procesoLogin.intentarLogin() }
                                                     )

        campoUsuario.callbackClickEnSugerencia = { campoContraseña.requestFocus() }

        launch(Dispatchers.IO) {
            val sugerencias = listOf("Pepito", "Pepita con nombre extra largo para verificar la UI")

            withContext(Dispatchers.JavaFx) { campoUsuario.usarSugerencias(sugerencias) }
        }
    }

    @PreDestroy
    fun liberarRecursos()
    {
        job.cancel()
    }
}