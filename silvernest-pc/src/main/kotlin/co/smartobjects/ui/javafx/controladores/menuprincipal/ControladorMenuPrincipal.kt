package co.smartobjects.ui.javafx.controladores.menuprincipal

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.sincronizadordecontenido.SincronizadorDeDatos
import co.smartobjects.ui.javafx.AplicacionPrincipal
import co.smartobjects.ui.javafx.bindEstaHabilitado
import co.smartobjects.ui.javafx.controladores.consumos.ControladorConsumos
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorPrincipal
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorToolbar
import co.smartobjects.ui.javafx.controladores.genericos.DialogoConListaOpciones
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.controladores.genericos.UbicacionEnLista
import co.smartobjects.ui.javafx.controladores.registropersonas.ControladorRegistrarPersonas
import co.smartobjects.ui.javafx.dependencias.DependenciasBD
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujo
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujoInterno
import co.smartobjects.ui.javafx.dependencias.darDependenciasDePantallas
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.javafx.usarSchedulersEnUI
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.menuprincipal.MenuPrincipal
import co.smartobjects.ui.modelos.menuprincipal.MenuPrincipalUI
import co.smartobjects.ui.modelos.menuprincipal.SeleccionUbicacion
import co.smartobjects.ui.modelos.menuprincipal.SeleccionUbicacionUI
import co.smartobjects.utilidades.Opcional
import io.datafx.controller.ViewController
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxjavafx.observables.JavaFxObservable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javafx.fxml.FXML
import javafx.scene.layout.StackPane
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct


@ViewController(value = ControladorMenuPrincipal.LAYOUT, title = ControladorMenuPrincipal.TITULO)
internal class ControladorMenuPrincipal
{
    companion object
    {
        const val LAYOUT = "/layouts/menuprincipal/menuPrincipal.fxml"
        const val TITULO = "MENÚ PRINCIPAL"
    }

    @ControladorDeFlujo
    internal lateinit var controladorDeFlujo: ControladorDeFlujoInterno

    @FXML
    internal lateinit var botonIrARegistrar: BotonMenuPrincipal
    @FXML
    internal lateinit var botonIrAConsumos: BotonMenuPrincipal

    private val dialogoDeEsperaSincronizacion: DialogoDeEspera = DialogoDeEspera()

    private val dialogoSeleccionUbicaciones: DialogoConListaOpciones<Ubicacion> = DialogoConListaOpciones<Ubicacion>()

    internal lateinit var menuPrincipal: MenuPrincipalUI

    internal lateinit var seleccionUbicacion: SeleccionUbicacionUI


    @PostConstruct
    fun inicializar()
    {
        controladorDeFlujo.contextoFlujo.darDependenciasDePantallas().eliminarTodas()

        inicializarModeloUI()
        inicializarToolbar()
        inicializarDialogoDeEsperaSincronizacion()
        inicializarDialogoSeleccionUbicaciones()

        botonIrARegistrar.bindEstaHabilitado(menuPrincipal.puedeIrARegistrarPersonas)
        botonIrARegistrar.setOnAction {
            menuPrincipal.irARegistrarPersonas()
        }

        botonIrAConsumos.bindEstaHabilitado(menuPrincipal.puedeIrAComprarCreditos)
        botonIrAConsumos.setOnAction {
            menuPrincipal.irAComprarCreditos()
        }

        menuPrincipal
            .pantallaANavegar
            .observarEnFx()
            .subscribe { pantalla ->
                when (pantalla)
                {
                    MenuPrincipalUI.PantallaSeleccionada.REGISTRAR_PERSONAS ->
                    {
                        controladorDeFlujo.navigate(ControladorRegistrarPersonas::class.java)
                    }
                    else                                                    ->
                    {
                        controladorDeFlujo.navigate(ControladorConsumos::class.java)
                    }
                }
            }
    }

    private fun inicializarModeloUI()
    {
        if (!this::menuPrincipal.isInitialized)
        {
            val contextoDeSesion =
                    controladorDeFlujo.contextoAplicacion.getRegisteredObject(AplicacionPrincipal.CTX_CONTEXTO_DE_SESION)
                            as Observable<Opcional<ContextoDeSesion>>

            val repositorioUbicaciones =
                    (controladorDeFlujo.contextoAplicacion.getRegisteredObject(DependenciasBD::class.java) as DependenciasBD)
                        .repositorioUbicaciones

            val gestorDescargaDeDatos =
                    (controladorDeFlujo.contextoAplicacion.getRegisteredObject(SincronizadorDeDatos::class.java) as SincronizadorDeDatos)
                        .gestorDescargaDeDatos

            menuPrincipal =
                    MenuPrincipal(
                            AplicacionPrincipal.CONFIGURACION_AMBIENTE.idCliente,
                            contextoDeSesion,
                            repositorioUbicaciones,
                            gestorDescargaDeDatos
                                 )

            seleccionUbicacion = SeleccionUbicacion(AplicacionPrincipal.CONFIGURACION_AMBIENTE.idCliente, repositorioUbicaciones)
        }
    }

    private fun inicializarToolbar()
    {
        val toolbar = controladorDeFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_TOOLBAR) as ControladorToolbar

        toolbar.cambiarTitulo(TITULO)
        toolbar.puedeVolver(false)
        toolbar.mostrarMenu(true)
        toolbar.puedeSincronizar(true)
        toolbar.puedeSeleccionarUbicacion(true)

        Observables.combineLatest(
                menuPrincipal.dialogoEsperaPorSincronizacionVisible,
                JavaFxObservable.valuesOf(dialogoSeleccionUbicaciones.visibleProperty())
                                 )
            .map { !it.first && !it.second }
            .observarEnFx()
            .subscribe { toolbar.mostrarMenu(it) }
    }

    private fun inicializarDialogoDeEsperaSincronizacion()
    {
        with(dialogoDeEsperaSincronizacion)
        {
            dialogContainer = controladorDeFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_CONTENDOR_PRINCIPAL) as StackPane
            mostrarSpinner()
            labelTituloDialogo.text = "Descargando datos..."
        }

        menuPrincipal.dialogoEsperaPorSincronizacionVisible.observarEnFx().subscribe {
            if (it) dialogoDeEsperaSincronizacion.show() else dialogoDeEsperaSincronizacion.close()
        }

        menuPrincipal.errorDeDescarga.subscribe {
            dialogoDeEsperaSincronizacion.mostrarIconoError()

            val mensajeDeError =
                    when (it)
                    {
                        RespuestaVacia.Error.Timeout -> "Tiempo de espera al servidor agotado. No se pudo descargar datos."
                        is RespuestaVacia.Error.Red  -> "Hubo un error en la conexión y no fue posible contactar al servidor"
                        is RespuestaVacia.Error.Back -> "La petición realizada es errónea y no pudo ser procesada.\n${it.error.mensaje}"
                        RespuestaVacia.Exitosa       -> throw IllegalStateException("No se debería enviarse exitoso por acá")
                    }

            dialogoDeEsperaSincronizacion.labelTituloDialogo.text = mensajeDeError

            Single.just(Unit).delay(2700, TimeUnit.MILLISECONDS).subscribe { _ ->
                dialogoDeEsperaSincronizacion.close()
            }
        }
    }

    private fun inicializarDialogoSeleccionUbicaciones()
    {
        with(dialogoSeleccionUbicaciones)
        {
            dialogContainer = controladorDeFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_CONTENDOR_PRINCIPAL) as StackPane
            mostrarSpinner()
            labelTituloDialogo.text = "Cargando ubicaciones..."
        }

        val ubicacionActual =
                controladorDeFlujo.contextoAplicacion
                    .getRegisteredObject(AplicacionPrincipal.CTX_EVENTOS_CAMBIO_DE_UBICACION)
                        as BehaviorSubject<Opcional<Ubicacion>>

        dialogoSeleccionUbicaciones.itemSeleccionado.observarEnFx().subscribe {
            ubicacionActual.onNext(Opcional.De(it))
            dialogoSeleccionUbicaciones.close()
        }

        menuPrincipal.debeSolicitarUbicacion
            .filter { it }
            .usarSchedulersEnUI(Schedulers.io())
            .subscribe {
                seleccionUbicacion.seleccionarUbicacion()
            }

        seleccionUbicacion.puntosDeContactoDisponibles
            .map {
                it.map(::UbicacionEnLista)
            }
            .withLatestFrom(ubicacionActual)
            .observarEnFx()
            .subscribe { (puntosDeContacto, ubicacionActual) ->
                dialogoSeleccionUbicaciones.reemplazarItems(puntosDeContacto, ubicacionActual.valorUOtro(null))
                dialogoSeleccionUbicaciones.mostrarListado()
                dialogoSeleccionUbicaciones.labelTituloDialogo.text = "Seleccione su ubicación"

                dialogoSeleccionUbicaciones.show()
            }
    }
}