package co.smartobjects.ui.javafx.controladores.controladorprincipal

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.sincronizadordecontenido.SincronizadorDeDatos
import co.smartobjects.ui.javafx.AplicacionPrincipal
import co.smartobjects.ui.javafx.controladores.genericos.DialogoConListaOpciones
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.controladores.genericos.UbicacionEnLista
import co.smartobjects.ui.javafx.controladores.login.ControladorLogin
import co.smartobjects.ui.javafx.dependencias.DependenciasBD
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujoInterno
import co.smartobjects.ui.javafx.hacerEscondible
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.javafx.usarSchedulersEnUI
import co.smartobjects.ui.modelos.cerrarsesion.ProcesoCerrarSesion
import co.smartobjects.ui.modelos.cerrarsesion.ProcesoCerrarSesionUI
import co.smartobjects.ui.modelos.menuprincipal.SeleccionUbicacion
import co.smartobjects.ui.modelos.menuprincipal.SeleccionUbicacionUI
import co.smartobjects.utilidades.Opcional
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXPopup
import com.jfoenix.controls.JFXSnackbar
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import java.util.concurrent.TimeUnit


internal class ControladorMenuToolbar
(
        private val controladorFlujo: ControladorDeFlujoInterno
) : VBox()
{
    @FXML
    internal lateinit var botonSincronizar: JFXButton

    @FXML
    internal lateinit var botonSeleccionarUbicacion: JFXButton

    @FXML
    internal lateinit var botonCerrarSesion: JFXButton


    private val dependenciasDeRed = controladorFlujo.contextoAplicacion.getRegisteredObject(ManejadorDePeticiones::class.java)
    private val sincronizadorDeDatos = controladorFlujo.contextoAplicacion.getRegisteredObject(SincronizadorDeDatos::class.java) as SincronizadorDeDatos
    private val contendorPrincipal =
            controladorFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_CONTENDOR_PRINCIPAL)
                    as StackPane

    private val dialogoDeEsperaCierreSesion = DialogoDeEspera().apply {
        dialogContainer = contendorPrincipal
        mostrarSpinner()
    }

    private val dialogoSeleccionUbicaciones: DialogoConListaOpciones<Ubicacion> = DialogoConListaOpciones<Ubicacion>()

    private val snackbarInformacion: JFXSnackbar =
            JFXSnackbar(contendorPrincipal).apply {
                (popupContainer.children.last { it is Group } as Group).children[0].styleClass.add("snackbar-informativo")
            }

    private val toolbarPopup: JFXPopup

    internal lateinit var procesoCerrarSesion: ProcesoCerrarSesionUI

    internal lateinit var seleccionUbicacion: SeleccionUbicacionUI


    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/controladorprincipal/menuToolbar.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()

        toolbarPopup = JFXPopup(this)

        inicializarProcesos()
        inicializarBotonSincronizarDatos()
        inicializarBotonSeleccionarUbicacion()
        inicializarBotonCerrarSesion()
    }

    private fun inicializarProcesos()
    {
        // Se verifica que no esté inicializado para que en las pruebas no se reemplace el mock puesto a mano
        if (!::procesoCerrarSesion.isInitialized)
        {
            @Suppress("UNCHECKED_CAST")
            procesoCerrarSesion =
                    ProcesoCerrarSesion(
                            dependenciasDeRed.apiDeUsuarios,
                            controladorFlujo.contextoAplicacion
                                .getRegisteredObject(AplicacionPrincipal.CTX_EVENTOS_CAMBIO_DE_USUARIO)
                                    as BehaviorSubject<Opcional<Usuario>>
                                       )
        }

        procesoCerrarSesion.estado.observarEnFx().subscribe {
            when (it)
            {
                ProcesoCerrarSesionUI.Estado.Esperando                        ->
                {
                    dialogoDeEsperaCierreSesion.close()
                }
                ProcesoCerrarSesionUI.Estado.CerrandoSesion                   ->
                {
                    dialogoDeEsperaCierreSesion.labelTituloDialogo.text = "Cerrando sesión..."
                    dialogoDeEsperaCierreSesion.show()
                }
                ProcesoCerrarSesionUI.Estado.Resultado.SesionCerrada          ->
                {
                    dialogoDeEsperaCierreSesion.close()
                    controladorFlujo.navigate(ControladorLogin::class.java)
                }
                is ProcesoCerrarSesionUI.Estado.Resultado.ErrorCerrandoSesion ->
                {
                    dialogoDeEsperaCierreSesion.mostrarIconoError()
                    dialogoDeEsperaCierreSesion.labelTituloDialogo.text = it.mensaje
                    Single.just(Unit).delay(2700, TimeUnit.MILLISECONDS).subscribe { _ ->
                        procesoCerrarSesion.reiniciarEstado()
                    }
                }
            }
        }

        if (!::seleccionUbicacion.isInitialized)
        {
            val repositorioUbicaciones =
                    (controladorFlujo.contextoAplicacion.getRegisteredObject(DependenciasBD::class.java) as DependenciasBD)
                        .repositorioUbicaciones

            seleccionUbicacion = SeleccionUbicacion(AplicacionPrincipal.CONFIGURACION_AMBIENTE.idCliente, repositorioUbicaciones)
        }

        inicializarDialogoSeleccionUbicaciones()
    }

    private fun inicializarDialogoSeleccionUbicaciones()
    {
        with(dialogoSeleccionUbicaciones)
        {
            dialogContainer = controladorFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_CONTENDOR_PRINCIPAL) as StackPane
            mostrarSpinner()
            labelTituloDialogo.text = "Cargando ubicaciones..."
        }

        val ubicacionActual =
                controladorFlujo.contextoAplicacion
                    .getRegisteredObject(AplicacionPrincipal.CTX_EVENTOS_CAMBIO_DE_UBICACION) as BehaviorSubject<Opcional<Ubicacion>>

        dialogoSeleccionUbicaciones.itemSeleccionado.observarEnFx().subscribe {
            ubicacionActual.onNext(Opcional.De(it))
            dialogoSeleccionUbicaciones.close()
        }

        seleccionUbicacion.puntosDeContactoDisponibles
            .map {
                it.map(::UbicacionEnLista)
            }
            .withLatestFrom(ubicacionActual)
            .observarEnFx()
            .subscribe { (puntosDeContacto, ubicacionActual) ->
                with(dialogoSeleccionUbicaciones)
                {
                    reemplazarItems(puntosDeContacto, ubicacionActual.valorUOtro(null))
                    mostrarListado()
                    labelTituloDialogo.text = "Seleccione su ubicación"
                    show()
                }
            }
    }

    private fun inicializarBotonSincronizarDatos()
    {
        with(botonSincronizar)
        {
            hacerEscondible()
            isVisible = false
            setOnAction {
                toolbarPopup.hide()
                sincronizadorDeDatos
                    .gestorDescargaDeDatos
                    .descargarYAlmacenarDatosIndependientesDeLaUbicacion()
                    .usarSchedulersEnUI(Schedulers.io())
                    .subscribeBy(
                            onSuccess = { snackbarInformacion.show("Datos sincronizados correctamente", 1400L) },
                            onComplete = { println("Ya estaba sincronizando") }
                                )
            }
        }
    }

    private fun inicializarBotonSeleccionarUbicacion()
    {
        with(botonSeleccionarUbicacion)
        {
            hacerEscondible()
            isVisible = false
            setOnAction {
                toolbarPopup.hide()

                seleccionUbicacion.seleccionarUbicacion()
            }
        }
    }

    private fun inicializarBotonCerrarSesion()
    {
        botonCerrarSesion.setOnAction {
            toolbarPopup.hide()
            procesoCerrarSesion.cerrarSesion()
        }
    }

    fun mostrar(
            anclaje: Node,
            vAlign: JFXPopup.PopupVPosition,
            hAlign: JFXPopup.PopupHPosition,
            initOffsetX: Double,
            initOffsetY: Double
               )
    {
        toolbarPopup.show(anclaje, vAlign, hAlign, initOffsetX, initOffsetY)
    }
}