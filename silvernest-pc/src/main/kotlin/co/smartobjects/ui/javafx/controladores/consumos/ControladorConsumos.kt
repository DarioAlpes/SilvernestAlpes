package co.smartobjects.ui.javafx.controladores.consumos

import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.nfc.windows.pcsc.ProveedorOperacionesNFCPCSC
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.ui.javafx.AplicacionPrincipal
import co.smartobjects.ui.javafx.controladores.catalogo.ControladorCatalogoProductos
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorPrincipal
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorToolbar
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.controladores.genericos.DialogoPositivoNegativo
import co.smartobjects.ui.javafx.controladores.menufiltradofondos.ControladorMenuFiltrado
import co.smartobjects.ui.javafx.controladores.menufiltradofondos.ProveedorIconosCategoriasFiltradoJavaFX
import co.smartobjects.ui.javafx.controladores.selecciondecreditos.ProveedorImagenesProductosImpl
import co.smartobjects.ui.javafx.dependencias.DependenciasBD
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujo
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujoInterno
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosCategorias
import co.smartobjects.ui.javafx.usarSchedulersEnUI
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.consumos.CodificacionDeConsumos
import co.smartobjects.ui.modelos.consumos.CodificacionDeConsumosUI
import co.smartobjects.ui.modelos.consumos.ProcesoConsumos
import co.smartobjects.ui.modelos.consumos.ProcesoConsumosUI
import co.smartobjects.utilidades.Opcional
import com.jfoenix.controls.JFXSnackbar
import io.datafx.controller.ViewController
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.layout.StackPane
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@ViewController(value = ControladorConsumos.LAYOUT, title = ControladorConsumos.TITULO)
class ControladorConsumos
{
    companion object
    {
        const val LAYOUT = "/layouts/consumos/consumos.fxml"
        const val TITULO = "REALIZAR CONSUMOS"
    }

    @ControladorDeFlujo
    internal lateinit var controladorDeFlujo: ControladorDeFlujoInterno

    @FXML
    internal lateinit var menuDeFiltrado: ControladorMenuFiltrado
    @FXML
    internal lateinit var catalogoDeProductos: ControladorCatalogoProductos
    @FXML
    internal lateinit var operacionConsumo: ControladorOperacionConsumo
    @FXML
    internal lateinit var dialogoDeEspera: DialogoDeEspera

    @FXML
    internal lateinit var dialogoLectorNFCDesconectado: DialogoPositivoNegativo

    internal lateinit var procesoConsumos: ProcesoConsumosUI<PrompterIconosCategorias>

    internal var schedulerBackground: Scheduler = Schedulers.io()

    private val snackbarErrores: JFXSnackbar by lazy {
        val contendorPrincipal = controladorDeFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_CONTENDOR_PRINCIPAL) as StackPane
        JFXSnackbar(contendorPrincipal).apply {
            (popupContainer.children.last { it is Group } as Group).children[0].styleClass.add("jfx-snackbar-content-error")
        }
    }


    @PostConstruct
    fun inicializar()
    {
        inicializarToolbar()

        // Se verifica que no esté inicializado para que en las pruebas no se reemplace el mock puesto a mano
        if (!::procesoConsumos.isInitialized)
        {
            val dependenciasBD = controladorDeFlujo.contextoAplicacion.getRegisteredObject(DependenciasBD::class.java) as DependenciasBD
            val dependenciasDeRed = controladorDeFlujo.contextoAplicacion.getRegisteredObject(ManejadorDePeticiones::class.java) as ManejadorDePeticiones
            val proveedorOperacionesNFC = controladorDeFlujo.contextoAplicacion.getRegisteredObject(ProveedorOperacionesNFCPCSC::class.java) as ProveedorOperacionesNFC

            val contextoDeSesion =
                    controladorDeFlujo.contextoAplicacion.getRegisteredObject(AplicacionPrincipal.CTX_CONTEXTO_DE_SESION)
                            as Observable<Opcional<ContextoDeSesion>>

            val repositorios =
                    ProcesoConsumos.Repositorios(
                            dependenciasBD.repositorioImpuestos,
                            dependenciasBD.repositorioCategoriasSkus,
                            dependenciasBD.repositorioFondos,
                            dependenciasBD.repositorioGrupoClientes,
                            dependenciasBD.repositorioValoresGruposEdad
                                                )

            val apis =
                    CodificacionDeConsumos.Apis(
                            dependenciasDeRed.apiDeLoteDeOrdenes,
                            dependenciasDeRed.apiPersonaPorIdSesionManilla,
                            dependenciasDeRed.apiDeSesionDeManilla
                                               )

            procesoConsumos =
                    ProcesoConsumos(
                            contextoDeSesion.blockingFirst().valor,
                            repositorios,
                            apis,
                            ProveedorImagenesProductosImpl(),
                            ProveedorIconosCategoriasFiltradoJavaFX(),
                            proveedorOperacionesNFC,
                            schedulerBackground
                                   )
        }

        inicializarDialogoCodificando()
        inicializarDialogoLectorNFCNoConectado()
        inicializarMostradoDeMensajesDeError()

        menuDeFiltrado.inicializar(procesoConsumos.menuFiltradoFondos, schedulerBackground)
        catalogoDeProductos.inicializar(procesoConsumos.catalogo, schedulerBackground)
        operacionConsumo.inicializar(procesoConsumos.codificacionDeConsumos, schedulerBackground)
    }

    private fun inicializarToolbar()
    {
        val toolbar = controladorDeFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_TOOLBAR) as ControladorToolbar

        toolbar.cambiarTitulo(ControladorConsumos.TITULO)
        toolbar.puedeVolver(true)
        toolbar.mostrarMenu(true)
        toolbar.puedeSincronizar(false)
        toolbar.puedeSeleccionarUbicacion(false)
    }

    private fun inicializarDialogoCodificando()
    {
        val mensajeIniciarSesion =
                DialogoDeEspera
                    .InformacionMensajeEspera(CodificacionDeConsumosUI.Estado.CODIFICANDO, "Codificando, no mover el tag...")

        dialogoDeEspera.inicializarSegunEstado(procesoConsumos.codificacionDeConsumos.estado, setOf(mensajeIniciarSesion))
    }

    private fun inicializarDialogoLectorNFCNoConectado()
    {
        val proveedorOperacionesNFC =
                controladorDeFlujo.contextoAplicacion.getRegisteredObject(ProveedorOperacionesNFCPCSC::class.java) as ProveedorOperacionesNFCPCSC

        proveedorOperacionesNFC.intentarConectarseALector()

        dialogoLectorNFCDesconectado
            .inicializar(
                    proveedorOperacionesNFC.hayLectorConectado.map { !it }.toObservable().usarSchedulersEnUI(schedulerBackground),
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
    }

    private fun inicializarMostradoDeMensajesDeError()
    {
        val proveedorOperacionesNFC =
                controladorDeFlujo.contextoAplicacion.getRegisteredObject(ProveedorOperacionesNFCPCSC::class.java) as ProveedorOperacionesNFCPCSC

        val mensajesDeErrorAMostrar =
                procesoConsumos.codificacionDeConsumos.mensajesDeError
                    .mergeWith(proveedorOperacionesNFC.errorLector.map { it.message })

        mensajesDeErrorAMostrar
            .usarSchedulersEnUI(schedulerBackground).subscribe {
                if (it.isEmpty())
                {
                    snackbarErrores.close()
                }
                else
                {
                    snackbarErrores.show(it, 3000L)
                }
            }
    }

    @PreDestroy
    fun liberarRecursos()
    {
        snackbarErrores.close()
        procesoConsumos.finalizarProceso()
    }
}