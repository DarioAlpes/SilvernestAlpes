package co.smartobjects.ui.javafx.controladores.selecciondecreditos

import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.ui.javafx.AplicacionPrincipal
import co.smartobjects.ui.javafx.controladores.catalogo.ControladorCatalogoProductos
import co.smartobjects.ui.javafx.controladores.codificacion.ControladorCodificacion
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorPrincipal
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorToolbar
import co.smartobjects.ui.javafx.controladores.genericos.DialogoDeEspera
import co.smartobjects.ui.javafx.controladores.menufiltradofondos.ControladorMenuFiltrado
import co.smartobjects.ui.javafx.controladores.menufiltradofondos.ProveedorIconosCategoriasFiltradoJavaFX
import co.smartobjects.ui.javafx.controladores.pagos.ControladorPagarCreditos
import co.smartobjects.ui.javafx.controladores.selecciondecreditos.agrupacioncarritosdecreditos.ControladorAgrupacionPersonasCarritosDeCreditos
import co.smartobjects.ui.javafx.dependencias.DependenciasBD
import co.smartobjects.ui.javafx.dependencias.agregarDependenciaDePantallaMultiplesUsos
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujo
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujoInterno
import co.smartobjects.ui.javafx.dependencias.obtenerDependenciaDePantalla
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosCategorias
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoCompraYSeleccionCreditos
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoCompraYSeleccionCreditosUI
import co.smartobjects.utilidades.Opcional
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.datafx.controller.ViewController
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import kotlin.coroutines.CoroutineContext


internal class ProveedorImagenesProductosImpl
    : ProveedorImagenesProductos
{
    override fun darImagen(idProducto: Long, esPaquete: Boolean): Maybe<ProveedorImagenesProductos.ImagenProducto>
    {
        return Maybe.empty()
    }
}

@ViewController(value = ControladorSeleccionCreditos.LAYOUT, title = ControladorSeleccionCreditos.TITULO)
internal class ControladorSeleccionCreditos : CoroutineScope
{
    companion object
    {
        const val LAYOUT = "/layouts/selecciondecreditos/seleccionDeCreditos.fxml"
        const val TITULO = "ADQUIRIR CRÉDITOS"
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.JavaFx

    @ControladorDeFlujo
    internal lateinit var controladorDeFlujo: ControladorDeFlujoInterno

    internal var schedulerBackground: Scheduler = Schedulers.io()

    @FXML
    internal lateinit var menuDeFiltrado: ControladorMenuFiltrado
    @FXML
    internal lateinit var catalogoDeProductos: ControladorCatalogoProductos
    @FXML
    internal lateinit var agrupacionPersonasCarritosDeCreditos: ControladorAgrupacionPersonasCarritosDeCreditos
    @FXML
    internal lateinit var dialogoDeEspera: DialogoDeEspera


    internal lateinit var procesoCompraYSeleccionCreditos: ProcesoCompraYSeleccionCreditosUI<PrompterIconosCategorias>


    @PostConstruct
    fun inicializar()
    {
        job = Job()

        inicializarToolbar()

        // Se verifica que no esté inicializado para que en las pruebas no se reemplace el mock puesto a mano
        if (!::procesoCompraYSeleccionCreditos.isInitialized)
        {
            val dependencias = controladorDeFlujo.contextoFlujo.obtenerDependenciaDePantalla(Dependencias::class.java)
            val dependenciasBD = controladorDeFlujo.contextoAplicacion.getRegisteredObject(DependenciasBD::class.java) as DependenciasBD
            val dependenciasDeRed = (controladorDeFlujo.contextoAplicacion.getRegisteredObject(ManejadorDePeticiones::class.java) as ManejadorDePeticiones)

            val contextoDeSesion =
                    controladorDeFlujo.contextoAplicacion.getRegisteredObject(AplicacionPrincipal.CTX_CONTEXTO_DE_SESION)
                            as Observable<Opcional<ContextoDeSesion>>

            val repositorios =
                    ProcesoCompraYSeleccionCreditos.Repositorios(
                            dependenciasBD.repositorioImpuestos,
                            dependenciasBD.repositorioCategoriasSkus,
                            dependenciasBD.repositorioPaquetes,
                            dependenciasBD.repositorioFondos,
                            dependenciasBD.repositorioLibrosSegunReglasCompleto
                                                                )

            procesoCompraYSeleccionCreditos =
                    ProcesoCompraYSeleccionCreditos(
                            contextoDeSesion.blockingFirst().valor,
                            repositorios,
                            dependenciasDeRed.apiDeCreditosDeUnaPersona,
                            ProveedorImagenesProductosImpl(),
                            ProveedorIconosCategoriasFiltradoJavaFX(),
                            dependencias.personas
                                                   )
        }

        inicializarDialogoConsultaCreditos(procesoCompraYSeleccionCreditos.estadoConsulta)

        with(procesoCompraYSeleccionCreditos.procesoSeleccionCreditos)
        {
            menuDeFiltrado.inicializar(menuFiltradoFondos, schedulerBackground)
            catalogoDeProductos.inicializar(catalogo, schedulerBackground)
            agrupacionPersonasCarritosDeCreditos.inicializar(agrupacionCarritoDeCreditos, schedulerBackground)

            creditosPorPersonaAProcesar
                .observarEnFx()
                .subscribe { creditos ->
                    if (creditos.all { it.creditosFondoAPagar.isEmpty() && it.creditosPaqueteAPagar.isEmpty() })
                    {
                        val dependenciasDeCodificacion =
                                ControladorCodificacion.Dependencias(creditos.map {
                                    ProcesoPagarUI.CreditosACodificarPorPersona(it.personaConGrupoCliente, it.creditosFondoPagados, it.creditosPaquetePagados)
                                })

                        controladorDeFlujo.contextoFlujo.agregarDependenciaDePantallaMultiplesUsos(dependenciasDeCodificacion)
                        controladorDeFlujo.navigate(ControladorCodificacion::class.java)
                    }
                    else
                    {
                        val dependenciasDeSeleccionDeCreditos =
                                ControladorPagarCreditos.Dependencias(creditos, ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))

                        controladorDeFlujo.contextoFlujo.agregarDependenciaDePantallaMultiplesUsos(dependenciasDeSeleccionDeCreditos)
                        controladorDeFlujo.navigate(ControladorPagarCreditos::class.java)
                    }
                }
        }

        launch {
            delay(230)
            procesoCompraYSeleccionCreditos.consultarComprasEnFecha(LocalDate.now(ZONA_HORARIA_POR_DEFECTO))
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

    private fun inicializarDialogoConsultaCreditos(estadoConsulta: Observable<ProcesoCompraYSeleccionCreditosUI.EstadoConsulta>)
    {
        val mensajeIniciarSesion =
                DialogoDeEspera
                    .InformacionMensajeEspera(ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.CONSULTANDO, "Consultando compras previas...")

        dialogoDeEspera.inicializarSegunEstado(estadoConsulta, setOf(mensajeIniciarSesion))
    }

    @PreDestroy
    fun liberarRecursos()
    {
        job.cancel()
    }

    class Dependencias(val personas: List<PersonaConGrupoCliente>)
}