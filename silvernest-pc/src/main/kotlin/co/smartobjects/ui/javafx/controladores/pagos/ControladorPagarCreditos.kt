package co.smartobjects.ui.javafx.controladores.pagos

import co.smartobjects.logica.fondos.ProveedorNombresYPreciosPorDefectoCompletosFondos
import co.smartobjects.logica.fondos.ProveedorNombresYPreciosPorDefectoPorDefectoCompletosEnMemoria
import co.smartobjects.logica.fondos.libros.BuscadorReglasDePreciosAplicables
import co.smartobjects.logica.fondos.libros.MapeadorReglasANombresRestricciones
import co.smartobjects.logica.fondos.libros.MapeadorReglasANombresRestriccionesEnMemoria
import co.smartobjects.logica.fondos.libros.ProveedorDePreciosCompletosYProhibicionesEnMemoria
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.ui.javafx.*
import co.smartobjects.ui.javafx.controladores.codificacion.ControladorCodificacion
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorPrincipal
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorToolbar
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.dependencias.DependenciasBD
import co.smartobjects.ui.javafx.dependencias.agregarDependenciaDePantallaMultiplesUsos
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujo
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujoInterno
import co.smartobjects.ui.javafx.dependencias.obtenerDependenciaDePantalla
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.pagos.ProcesoPagar
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoSeleccionCreditosUI
import co.smartobjects.utilidades.Opcional
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import io.datafx.controller.ViewController
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.scene.Group
import javafx.scene.control.Tooltip
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.threeten.bp.ZonedDateTime
import javax.annotation.PostConstruct

@ViewController(value = ControladorPagarCreditos.LAYOUT, title = ControladorPagarCreditos.TITULO)
internal class ControladorPagarCreditos
{
    companion object
    {
        const val LAYOUT = "/layouts/pagos/pagarCreditos.fxml"
        const val TITULO = "PAGAR CRÉDITOS"
    }

    @ControladorDeFlujo
    internal lateinit var controladorDeFlujo: ControladorDeFlujoInterno
    @FXML
    internal lateinit var personasYTotalAPagar: ControladorPersonasYTotalAPagar
    @FXML
    internal lateinit var panelTooltipoBotonPagar: StackPane
    @FXML
    internal lateinit var botonPagar: JFXButton
    @FXML
    internal lateinit var pagosDeUnaCompra: ControladorPagosDeUnaCompra
    @FXML
    internal lateinit var contendorItemsResumen: VBox
    @FXML
    internal lateinit var dialogoDeEspera: DialogoDeEspera


    internal lateinit var procesoPagar: ProcesoPagarUI

    private val tooltipBotonPagar = inicializarTooltipConTexto("El total de pagos debe ser igual o mayor al Gran Total")
    private val snackbarErrores: JFXSnackbar by lazy {
        val contendorPrincipal = controladorDeFlujo.contextoFlujo.getRegisteredObject(ControladorPrincipal.CTX_CONTENDOR_PRINCIPAL) as StackPane
        JFXSnackbar(contendorPrincipal).apply {
            (popupContainer.children.last { it is Group } as Group).children[0].styleClass.add("jfx-snackbar-content-error")
        }
    }

    @PostConstruct
    fun inicializar()
    {
        val dependencias = controladorDeFlujo.contextoFlujo.obtenerDependenciaDePantalla(Dependencias::class.java)

        inicializarToolbar()

        // Se verifica que no esté inicializado para que en las pruebas no se reemplace el mock puesto a mano
        if (!::procesoPagar.isInitialized)
        {
            val contextoDeSesion =
                    controladorDeFlujo.contextoAplicacion.getRegisteredObject(AplicacionPrincipal.CTX_CONTEXTO_DE_SESION)
                            as Observable<Opcional<ContextoDeSesion>>

            val dependenciasDeRed = (controladorDeFlujo.contextoAplicacion.getRegisteredObject(ManejadorDePeticiones::class.java) as ManejadorDePeticiones)
            val dependenciasBD = controladorDeFlujo.contextoAplicacion.getRegisteredObject(DependenciasBD::class.java) as DependenciasBD

            val contextoDeSesionActual = contextoDeSesion.blockingFirst().valor
            procesoPagar =
                    ProcesoPagar(
                            contextoDeSesionActual,
                            dependenciasDeRed.apiDeCompras,
                            dependencias.creditosAProcesar,
                            instanciarMapeadorReglasANombreRestriccion(contextoDeSesionActual.idCliente, dependenciasBD),
                            instanciarProveedorNombresYPreciosPorDefectoCompletosFondos(contextoDeSesionActual.idCliente, dependenciasBD)
                                )
        }

        personasYTotalAPagar.inicializar(procesoPagar.totalAPagarSegunPersonas)
        pagosDeUnaCompra.inicializar(procesoPagar.pagosDeUnaCompra, dependencias.fechaDeCompra)

        inicializarBotonDePagar()
        inicializarDialogoDeEspera()
        inicializarManejoDeErrores()

        procesoPagar.creditosACodificar.observarEnFx().subscribe { creditosAProcesar ->

            val dependenciasDeCodificacion = ControladorCodificacion.Dependencias(creditosAProcesar)

            controladorDeFlujo.contextoFlujo.agregarDependenciaDePantallaMultiplesUsos(dependenciasDeCodificacion)
            controladorDeFlujo.navigate(ControladorCodificacion::class.java)
        }

        procesoPagar.resumenDeCreditosAPagar.usarSchedulersEnUI(Schedulers.io()).subscribe { resumenesDePago ->
            val controladoresItemResumenPago =
                    resumenesDePago.map { ControladorItemResumenPago().apply { asignarResumenPagoProducto(it) } }

            contendorItemsResumen.children.setAll(controladoresItemResumenPago)
        }
    }

    private fun inicializarBotonDePagar()
    {
        botonPagar.inicializarBindingAccion(procesoPagar.puedePagar) {
            procesoPagar.pagar()
        }
        procesoPagar.puedePagar.observarEnFx().subscribe {
            if (it)
            {
                Tooltip.uninstall(panelTooltipoBotonPagar, tooltipBotonPagar)
            }
            else
            {
                Tooltip.install(panelTooltipoBotonPagar, tooltipBotonPagar)
            }
        }

        procesoPagar.estado.observarEnFx().subscribe {
            botonPagar.text =
                    when (it!!)
                    {
                        ProcesoPagarUI.Estado.SIN_CREAR_COMPRA   -> "PAGAR"
                        ProcesoPagarUI.Estado.CREANDO_COMPRA     -> "PAGAR"
                        ProcesoPagarUI.Estado.COMPRA_CREADA      -> "CONFIRMAR"
                        ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA -> "CONFIRMAR"
                        ProcesoPagarUI.Estado.COMPRA_CONFIRMADA  -> "COMPRA CONFIRMADA"
                        ProcesoPagarUI.Estado.PROCESO_COMPLETADO -> "PROCESO COMPLETADO"
                    }
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

    private fun inicializarDialogoDeEspera()
    {
        val mensajesDeDialogoDeEspera =
                setOf(
                        DialogoDeEspera.InformacionMensajeEspera(ProcesoPagarUI.Estado.CREANDO_COMPRA, "Creando compra..."),
                        DialogoDeEspera.InformacionMensajeEspera(ProcesoPagarUI.Estado.COMPRA_CREADA, "¡Compra creada!"),
                        DialogoDeEspera.InformacionMensajeEspera(ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA, "Confirmando compra..."),
                        DialogoDeEspera.InformacionMensajeEspera(ProcesoPagarUI.Estado.COMPRA_CONFIRMADA, "¡Compra confirmada!")
                     )

        dialogoDeEspera.inicializarSegunEstado(procesoPagar.estado, mensajesDeDialogoDeEspera)
    }

    private fun inicializarManejoDeErrores()
    {
        procesoPagar.mensajesDeError.observarEnFx().subscribe {
            if (it.isNotBlank())
            {
                snackbarErrores.show(it, 5000L)
            }
        }
    }

    private fun instanciarMapeadorReglasANombreRestriccion(idCliente: Long, dependenciasBD: DependenciasBD)
            : Single<MapeadorReglasANombresRestricciones>
    {
        val fondos =
                Single
                    .fromCallable { dependenciasBD.repositorioFondos.listar(idCliente) }
                    .cache()
                    .subscribeOn(Schedulers.io())

        val impuestos =
                Single
                    .fromCallable { dependenciasBD.repositorioImpuestos.listar(idCliente) }
                    .cache()
                    .subscribeOn(Schedulers.io())

        val buscadorReglasDePreciosAplicables =
                Singles
                    .zip(
                            Single
                                .fromCallable { dependenciasBD.repositorioLibrosSegunReglasCompleto.listar(idCliente) }
                                .cache()
                                .subscribeOn(Schedulers.io()),
                            fondos,
                            impuestos
                        )
                    .map<BuscadorReglasDePreciosAplicables> {
                        ProveedorDePreciosCompletosYProhibicionesEnMemoria(it.first, it.second, it.third)
                    }
                    .subscribeOn(Schedulers.io())

        val ubicaciones =
                Single
                    .fromCallable { dependenciasBD.repositorioUbicaciones.listar(idCliente) }
                    .cache()
                    .subscribeOn(Schedulers.io())

        val gruposDeClientes =
                Single
                    .fromCallable { dependenciasBD.repositorioGrupoClientes.listar(idCliente) }
                    .cache()
                    .subscribeOn(Schedulers.io())

        val paquetes =
                Single
                    .fromCallable { dependenciasBD.repositorioPaquetes.listar(idCliente) }
                    .cache()
                    .subscribeOn(Schedulers.io())

        return Singles
            .zip(
                    buscadorReglasDePreciosAplicables,
                    ubicaciones,
                    gruposDeClientes,
                    paquetes
                )
            { resBuscadorReglasDePreciosAplicables, resUbicaciones, resGruposDeClientes, resPaquetes ->
                MapeadorReglasANombresRestriccionesEnMemoria(
                        resBuscadorReglasDePreciosAplicables,
                        resUbicaciones.toList(),
                        resGruposDeClientes.toList(),
                        resPaquetes.toList()
                                                            )
                        as MapeadorReglasANombresRestricciones
            }
            .subscribeOn(Schedulers.io())
    }

    private fun instanciarProveedorNombresYPreciosPorDefectoCompletosFondos(idCliente: Long, dependenciasBD: DependenciasBD): Single<ProveedorNombresYPreciosPorDefectoCompletosFondos>
    {
        val fondos =
                Single
                    .fromCallable { dependenciasBD.repositorioFondos.listar(idCliente) }
                    .cache()
                    .subscribeOn(Schedulers.io())

        val impuestos =
                Single
                    .fromCallable { dependenciasBD.repositorioImpuestos.listar(idCliente) }
                    .cache()
                    .subscribeOn(Schedulers.io())

        return Singles
            .zip(fondos, impuestos)
            .map<ProveedorNombresYPreciosPorDefectoCompletosFondos> {
                ProveedorNombresYPreciosPorDefectoPorDefectoCompletosEnMemoria(it.first, it.second)
            }
            .cache()
            .subscribeOn(Schedulers.io())
    }

    class Dependencias(
            val creditosAProcesar: List<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>,
            val fechaDeCompra: ZonedDateTime
                      )
}