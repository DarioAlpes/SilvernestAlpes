package co.smartobjects.ui.javafx.controladores.codificacion

import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.nfc.windows.pcsc.ProveedorOperacionesNFCPCSC
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.ui.javafx.*
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorPrincipal
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorToolbar
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.controladores.genericos.DialogoPositivoNegativo
import co.smartobjects.ui.javafx.controladores.registropersonas.ControladorRegistrarPersonas
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujo
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujoInterno
import co.smartobjects.ui.javafx.dependencias.eliminarTodasLasDependenciasDePantalla
import co.smartobjects.ui.javafx.dependencias.obtenerDependenciaDePantalla
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.codificacion.*
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import co.smartobjects.utilidades.Opcional
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXScrollPane
import com.jfoenix.controls.JFXSnackbar
import io.datafx.controller.ViewController
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tooltip
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.coroutines.CoroutineContext


@ViewController(value = ControladorCodificacion.LAYOUT, title = ControladorCodificacion.TITULO)
class ControladorCodificacion : CoroutineScope
{
    companion object
    {
        const val LAYOUT = "/layouts/codificacion/codificacionCreditos.fxml"
        const val TITULO = "CODIFICAR CRÉDITOS"
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.JavaFx

    @ControladorDeFlujo
    internal lateinit var controladorDeFlujo: ControladorDeFlujoInterno

    @FXML
    internal lateinit var numeroDeSesionesActivas: Label
    @FXML
    internal lateinit var numeroDeReserva: Label
    @FXML
    internal lateinit var scrollPane: ScrollPane
    @FXML
    internal lateinit var itemsACodificar: TilePane
    @FXML
    internal lateinit var panelTooltipoBotonFinalizar: StackPane
    @FXML
    internal lateinit var botonFinalizar: JFXButton
    @FXML
    internal lateinit var dialogoDeEspera: DialogoDeEspera
    @FXML
    internal lateinit var dialogoLectorNFCDesconectado: DialogoPositivoNegativo


    private val tooltipBotonFinalizar = inicializarTooltipConTexto("Todas las sesiones de la reserva deben estar activadas")

    private val snackbarErrores: JFXSnackbar by lazy {
        val contendorPrincipal = controladorDeFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_CONTENDOR_PRINCIPAL) as StackPane
        JFXSnackbar(contendorPrincipal).apply {
            (popupContainer.children.last { it is Group } as Group).children[0].styleClass.add("jfx-snackbar-content-error")
        }
    }

    internal lateinit var procesoReservaYCodificacion: ProcesoReservaYCodificacionUI

    private val disposables = CompositeDisposable()


    @PostConstruct
    fun inicializar()
    {
        job = Job()

        val dependencias = controladorDeFlujo.contextoFlujo.obtenerDependenciaDePantalla(Dependencias::class.java)

        inicializarToolbar()

        // Se verifica que no esté inicializado para que en las pruebas no se reemplace el mock puesto a mano
        if (!::procesoReservaYCodificacion.isInitialized)
        {
            val contextoAplicacion = controladorDeFlujo.contextoAplicacion

            val contextoDeSesion =
                    contextoAplicacion.getRegisteredObject(AplicacionPrincipal.CTX_CONTEXTO_DE_SESION)
                            as Observable<Opcional<ContextoDeSesion>>

            val dependenciasDeRed = contextoAplicacion.getRegisteredObject(ManejadorDePeticiones::class.java) as ManejadorDePeticiones
            val proveedorOperacionesNFC = contextoAplicacion.getRegisteredObject(ProveedorOperacionesNFCPCSC::class.java) as ProveedorOperacionesNFC

            val dependenciasDelModelo =
                    ProcesoReservaYCodificacion.Dependencias(
                            contextoDeSesion.blockingFirst().valor,
                            dependenciasDeRed.apiDeReservas,
                            dependenciasDeRed.apiDeSesionDeManilla,
                            proveedorOperacionesNFC
                                                            )

            procesoReservaYCodificacion =
                    ProcesoReservaYCodificacion(
                            dependenciasDelModelo,
                            dependencias.creditosPorPersonaAProcesar
                                               )
        }

        inicializarNumeroDeSesionesActivas(dependencias)
        inicializarNumeroDeReserva()
        inicializarGrillaItemsACodificar()
        inicializarBotonFinalizar()
        inicializarDialogosDeEspera()
        inicializarMostradoDeMensajesDeError()

        launch {
            delay(300)
            procesoReservaYCodificacion.procesoCreacionReservaUI.intentarCrearActivarYConsultarReserva()
        }
    }

    private fun inicializarToolbar()
    {
        val toolbar = controladorDeFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_TOOLBAR) as ControladorToolbar

        toolbar.cambiarTitulo(TITULO)
        toolbar.puedeVolver(true)
        toolbar.mostrarMenu(true)
        toolbar.puedeSincronizar(false)
        toolbar.puedeSeleccionarUbicacion(false)
    }

    private fun inicializarGrillaItemsACodificar()
    {
        if (scrollPane.content != null)
        {
            JFXScrollPane.smoothScrolling(scrollPane)
        }

        procesoReservaYCodificacion
            .procesoCodificacionUI
            .usarSchedulersEnUI(Schedulers.io())
            .subscribe { procesoCodificacion ->

                val productosAMostrar = procesoCodificacion.itemsACodificar.map { itemACodificarUI ->
                    ControladorItemACodificar().apply {
                        inicializar(itemACodificarUI)
                    }.also { nodo ->
                        itemACodificarUI.estado.usarSchedulersEnUI(Schedulers.io()).subscribe { estado ->
                            if (estado === ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag)
                            {
                                hacerScrollHastaItemACodificar(nodo)
                            }
                        }.addTo(disposables)
                    }
                }

                itemsACodificar.children.clear()
                itemsACodificar.children.addAll(productosAMostrar)

            }
            .addTo(disposables)
    }

    private fun inicializarNumeroDeSesionesActivas(dependencias: Dependencias)
    {
        numeroDeSesionesActivas.text = "0 / ${dependencias.creditosPorPersonaAProcesar.size}"
        procesoReservaYCodificacion
            .procesoCodificacionUI
            .observarEnFx()
            .subscribe { procesoCodificacion ->
                val mensaje = procesoCodificacion.numeroDeSesionesActivas.map { "$it / ${dependencias.creditosPorPersonaAProcesar.size}" }
                numeroDeSesionesActivas.inicializarBindingCambiarMensaje(mensaje)
            }
            .addTo(disposables)
    }

    private fun inicializarNumeroDeReserva()
    {
        procesoReservaYCodificacion.procesoCreacionReservaUI
            .reservaConNumeroAsignado
            .observarEnFx()
            .subscribe { reserva ->
                numeroDeReserva.text = reserva.numeroDeReserva.toString()
            }
    }

    private fun inicializarBotonFinalizar()
    {
        val estaActivoElBoton =
                procesoReservaYCodificacion
                    .procesoCodificacionUI
                    .observarEnFx()
                    .toObservable()
                    .switchMap {
                        it.seActivaronTodasLasSesiones.toObservable()
                    }
                    .map { true }
                    .startWith(true)

        botonFinalizar.inicializarBindingAccion(estaActivoElBoton) {
            controladorDeFlujo.contextoFlujo.eliminarTodasLasDependenciasDePantalla()
            controladorDeFlujo.navegarHaciaAtrasAPantalla(ControladorRegistrarPersonas::class)
        }
        estaActivoElBoton.observarEnFx().subscribe {
            if (it)
            {
                Tooltip.uninstall(panelTooltipoBotonFinalizar, tooltipBotonFinalizar)
            }
            else
            {
                Tooltip.install(panelTooltipoBotonFinalizar, tooltipBotonFinalizar)
            }
        }
    }

    private fun inicializarDialogosDeEspera()
    {
        val proveedorOperacionesNFC =
                controladorDeFlujo.contextoAplicacion.getRegisteredObject(ProveedorOperacionesNFCPCSC::class.java) as ProveedorOperacionesNFCPCSC

        proveedorOperacionesNFC.intentarConectarseALector()

        dialogoLectorNFCDesconectado
            .inicializar(
                    proveedorOperacionesNFC.hayLectorConectado.map { !it }.toObservable().usarSchedulersEnUI(Schedulers.io()),
                    "Lector NFC no detectado",
                    DialogoPositivoNegativo.ConfiguracionBoton.Negativo("Regresar") {
                        controladorDeFlujo.navigateBack<Unit>()
                    },
                    DialogoPositivoNegativo.ConfiguracionBoton.Positivo("Reintentar detección") {
                        val inicializoLector = proveedorOperacionesNFC.intentarConectarseALector()

                        if (!inicializoLector)
                        {
                            dialogoLectorNFCDesconectado.agitar()
                        }
                    }
                        )

        val mensajesDeDialogoDeEspera =
                setOf(
                        DialogoDeEspera.InformacionMensajeEspera(ProcesoCreacionReservaUI.Estado.CREANDO, "Creando reserva..."),
                        DialogoDeEspera.InformacionMensajeEspera(ProcesoCreacionReservaUI.Estado.ACTIVANDO, "Activando reserva..."),
                        DialogoDeEspera.InformacionMensajeEspera(ProcesoCreacionReservaUI.Estado.CONSULTANDO_NUMERO_DE_RESERVA, "Consultando número de reserva...")
                     )

        dialogoDeEspera.inicializarSegunEstado(procesoReservaYCodificacion.procesoCreacionReservaUI.estado, mensajesDeDialogoDeEspera)
    }

    private fun inicializarMostradoDeMensajesDeError()
    {
        val proveedorOperacionesNFC =
                controladorDeFlujo.contextoAplicacion.getRegisteredObject(ProveedorOperacionesNFCPCSC::class.java) as ProveedorOperacionesNFCPCSC

        val mensajesDeCreacionDeReserva = procesoReservaYCodificacion.procesoCreacionReservaUI.mensajesDeError
        val mensajesDeCodificacion = procesoReservaYCodificacion.procesoCodificacionUI.toObservable().flatMap { it.mensajesDeError }
        val mensajesDeErrorDeLector = proveedorOperacionesNFC.errorLector.map { it.message }

        mensajesDeCreacionDeReserva.mergeWith(mensajesDeCodificacion).mergeWith(mensajesDeErrorDeLector)
            .usarSchedulersEnUI(Schedulers.io()).subscribe {
                if (it.isEmpty())
                {
                    snackbarErrores.close()
                }
                else
                {
                    snackbarErrores.show(it, 5000L)
                }
            }
    }

    private fun hacerScrollHastaItemACodificar(nodo: Node)
    {
        val ancho = scrollPane.content.boundsInLocal.width
        val alto = scrollPane.content.boundsInLocal.height

        scrollPane.hvalue = nodo.boundsInParent.maxX / ancho
        scrollPane.vvalue = nodo.boundsInParent.maxY / alto
    }

    @PreDestroy
    fun liberarRecursos()
    {
        job.cancel()
        disposables.dispose()
        procesoReservaYCodificacion.finalizarProceso()
    }

    class Dependencias(val creditosPorPersonaAProcesar: List<ProcesoPagarUI.CreditosACodificarPorPersona>)
}