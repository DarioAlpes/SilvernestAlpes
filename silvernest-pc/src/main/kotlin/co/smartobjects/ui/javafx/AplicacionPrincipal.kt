package co.smartobjects.ui.javafx

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.nfc.windows.pcsc.GeneradorUIDMaquina
import co.smartobjects.nfc.windows.pcsc.GeneradorUIDMaquinaImpl
import co.smartobjects.nfc.windows.pcsc.ProveedorOperacionesNFCPCSC
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticiones
import co.smartobjects.red.clientes.retrofit.ManejadorDePeticionesRetrofit
import co.smartobjects.sincronizadordecontenido.GestorDescargaDeDatosImpl
import co.smartobjects.sincronizadordecontenido.SincronizadorDeDatos
import co.smartobjects.sincronizadordecontenido.SincronizadorDeDatosImpl
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorPrincipal
import co.smartobjects.ui.javafx.dependencias.DependenciasBD
import co.smartobjects.ui.javafx.dependencias.DependenciasBDImpl
import co.smartobjects.ui.javafx.lectorbarras.LectorBarrasHoneyWellXenon1900
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.ContextoDeSesionImpl
import co.smartobjects.utilidades.Opcional
import io.datafx.controller.flow.Flow
import io.datafx.controller.flow.container.DefaultFlowContainer
import io.datafx.controller.flow.context.FXMLViewFlowContext
import io.datafx.controller.flow.context.ViewFlowContext
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import it.sauronsoftware.junique.AlreadyLockedException
import it.sauronsoftware.junique.JUnique
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.pmw.tinylog.Configurator
import org.pmw.tinylog.Level
import org.pmw.tinylog.Logger
import org.pmw.tinylog.writers.FileWriter
import java.io.File
import java.lang.management.ManagementFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.swing.JOptionPane
import kotlin.system.exitProcess


object InfoEjecucion
{
    @JvmStatic
    val esStandaloneApp = ManagementFactory.getRuntimeMXBean().inputArguments.any { it.contains("Djava.launcher.path") }

    @JvmStatic
    val esDebugMode = ManagementFactory.getRuntimeMXBean().inputArguments.any { it.startsWith("-agentlib") }
}

internal class AplicacionPrincipal : Application()
{
    companion object
    {
        @JvmStatic
        private val ID_APLICACION = AplicacionPrincipal::class.java.canonicalName

        private const val PARAMETRO_OTRAS_INSTANCIAS = "OtraInstanciaActiva"
        const val CTX_STAGE_PRIMARIO = "stagePrimario"
        const val CTX_EVENTOS_CAMBIO_DE_USUARIO = "Eventos Cambio De Usuario"
        const val CTX_EVENTOS_CAMBIO_DE_UBICACION = "Eventos Cambio De Ubicación"
        const val CTX_CONTEXTO_DE_SESION = "Observable Contexto de Sesion"
        const val CTX_LECTOR_BARRAS = "Lector de barras"


//        val CONFIGURACION_AMBIENTE = ConfiguracionAmbiente(1, "http://localhost:80/") // LOCAL
        //val CONFIGURACION_AMBIENTE = ConfiguracionAmbiente(1, "http:18.191.58.98") // DESARROLLO
        //val CONFIGURACION_AMBIENTE = ConfiguracionAmbiente(1, "http:18.221.226.238") // NUEVO DESARROLLO
        //val CONFIGURACION_AMBIENTE = ConfiguracionAmbiente(1, "http:18.189.187.126") // PRUEBAS
        //val CONFIGURACION_AMBIENTE = ConfiguracionAmbiente(1, "http:18.223.68.197") // PRODUCCION
        //val CONFIGURACION_AMBIENTE = ConfiguracionAmbiente(1, "http://dev.back.silvernest.com.co/") // DESARROLLO
        //val CONFIGURACION_AMBIENTE = ConfiguracionAmbiente(1, "http://stage.back.silvernest.com.co/") // PREPRODUCCIÓN
        val CONFIGURACION_AMBIENTE = ConfiguracionAmbiente(1, "https://back.silvernest.cafam.com.co/") // PRODUCCION

        @JvmStatic
        fun main(args: Array<String>)
        {   println("inicio la aplicacion")
            val existeOtraInstancia =
                    try
                    {
                        JUnique.acquireLock(ID_APLICACION)
                        false
                    }
                    catch (exc: AlreadyLockedException)
                    {
                        true
                    }

            if (InfoEjecucion.esStandaloneApp && !InfoEjecucion.esDebugMode)
            {
                configurarLogger()
            }

            Application.launch(AplicacionPrincipal::class.java, "--$PARAMETRO_OTRAS_INSTANCIAS=$existeOtraInstancia")
        }

        @JvmStatic
        fun configurarLogger()
        {
            var configuracion = Configurator.currentConfig()
                .formatPattern("[{level}][{context:usuario_log}][{context:contexto_log}] {date:yyyy-MM-dd HH:mm:ss} {class_name}.{method}:{line} -> {message}")
                .level(Level.DEBUG)

            configuracion = configuracion
                .writer(FileWriter("logs_${DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss").format(LocalDateTime.now())}.logsilvernest", false, true))

            configuracion.activate()
        }
    }

    @FXMLViewFlowContext
    private lateinit var contextoPrincipal: ViewFlowContext

    private val manejadorPeticiones: ManejadorDePeticiones by lazy {
        ManejadorDePeticionesRetrofit(CONFIGURACION_AMBIENTE.idCliente, CONFIGURACION_AMBIENTE.url)
    }

    private val sincronizadorDeDatos by lazy {
        val gestorDeDescargas =
                GestorDescargaDeDatosImpl(
                        CONFIGURACION_AMBIENTE.idCliente,
                        GestorDescargaDeDatosImpl.APIs(
                                manejadorPeticiones.apiDeUbicaciones,
                                manejadorPeticiones.apiDeUbicacionesContabilizables,
                                manejadorPeticiones.apiDeImpuestos,
                                manejadorPeticiones.apiDeAccesos,
                                manejadorPeticiones.apiDeEntradas,
                                manejadorPeticiones.apiDeCategoriasSku,
                                manejadorPeticiones.apiDeSkus,
                                manejadorPeticiones.apiDeMonedas,
                                manejadorPeticiones.apiDePaquetes,
                                manejadorPeticiones.apiDeLibrosSegunReglasCompleto,
                                manejadorPeticiones.apiDeGruposClientes,
                                manejadorPeticiones.apiDeValoresGruposEdad,
                                manejadorPeticiones.apiDeLlavesNFC
                                                      ),
                        GestorDescargaDeDatosImpl.Repositorios(
                                DependenciasBDImpl.repositorioUbicaciones,
                                DependenciasBDImpl.repositorioUbicacionesContabilizables,
                                DependenciasBDImpl.repositorioImpuestos,
                                DependenciasBDImpl.repositorioAccesos,
                                DependenciasBDImpl.repositorioEntradas,
                                DependenciasBDImpl.repositorioCategoriasSkus,
                                DependenciasBDImpl.repositorioSkus,
                                DependenciasBDImpl.repositorioMonedas,
                                DependenciasBDImpl.repositorioPaquetes,
                                DependenciasBDImpl.repositorioLibrosSegunReglas,
                                DependenciasBDImpl.repositorioLibroDePrecios,
                                DependenciasBDImpl.repositorioLibroDeProhibiciones,
                                DependenciasBDImpl.repositorioGrupoClientes,
                                DependenciasBDImpl.repositorioValoresGruposEdad,
                                DependenciasBDImpl.repositorioLlavesNFC
                                                              )
                                         )

        SincronizadorDeDatosImpl(gestorDeDescargas)
    }

    private val generadorUIDMaquina: GeneradorUIDMaquina = GeneradorUIDMaquinaImpl()
    private val eventosCambioDeUsuario = BehaviorSubject.createDefault<Opcional<Usuario>>(Opcional.Vacio())
    private val eventosCambioDeUbicacion = BehaviorSubject.createDefault<Opcional<Ubicacion>>(Opcional.Vacio())
    private val uidMaquinaGenerado =
            Observable
                .fromCallable {
                    Observable.just(Unit).delay(15, TimeUnit.SECONDS)
                        .map {
                            generadorUIDMaquina.generarBloqueante() ?: "Id de máquina por defecto"
                        }
                }
                .flatMap { it }
                .replay(1).refCount()
                .startWith("Id de máquina por defecto")
                .subscribeOn(Schedulers.io())

    private val contextoDeSesion: Observable<Opcional<out ContextoDeSesion>> =
            Observables
                .combineLatest(
                        eventosCambioDeUsuario,
                        eventosCambioDeUbicacion.distinctUntilChanged(),
                        uidMaquinaGenerado
                              )
                { posibleUsuario: Opcional<Usuario>, psoibleUbicacion: Opcional<Ubicacion>, uidMaquina: String ->
                    if (posibleUsuario.esVacio || psoibleUbicacion.esVacio)
                    {
                        Opcional.Vacio<ContextoDeSesion>()
                    }
                    else
                    {
                        val usuario = posibleUsuario.valor
                        val ubicacion = psoibleUbicacion.valor

                        val contextoNuevo =
                                ContextoDeSesionImpl(
                                        CONFIGURACION_AMBIENTE.idCliente,
                                        ubicacion.nombre,
                                        usuario.datosUsuario.usuario,
                                        uidMaquina,
                                        ubicacion.id!!
                                                    )

                        Opcional.De(contextoNuevo)
                    }
                }.distinctUntilChanged().replay(1).refCount()

    private val lectorBarras = LectorBarrasHoneyWellXenon1900()
    private val lectorNFC by lazy {
        ProveedorOperacionesNFCPCSC(CONFIGURACION_AMBIENTE.idCliente, DependenciasBDImpl.repositorioLlavesNFC)
    }


    override fun start(primaryStage: Stage)
    {
        val parametros = parameters.named
        val hayOtraInstancia = parametros[PARAMETRO_OTRAS_INSTANCIAS]!!.toBoolean()

        if (hayOtraInstancia)
        {
            val alert = Alert(
                    Alert.AlertType.ERROR
                    , "Ya existe una instancia corriendo de la aplicación en este usuario u otro usuario." +
                      "\n\nRevisar el 'Administrador de tareas' si no es visible.\nPara ello:" +
                      "\n\n1. Ctrl + Alt + Suprimir (Administrador de tareas)\n2. Ver la pestaña de detalles\n3. Terminar el proceso llamado 'Silvernest.exe'"
                    , ButtonType.OK
                             )
            alert.showAndWait()

            Platform.exit()
        }
        else
        {
            asignarExceptionHandlerGlobal()
            registrarErroresRxJava()

            contextoPrincipal = ViewFlowContext().apply {
                with(applicationContext)
                {
                    register(DependenciasBD::class.java.toString(), DependenciasBDImpl)

                    register(CTX_STAGE_PRIMARIO, primaryStage)

                    GlobalScope.launch(Dispatchers.IO, CoroutineStart.UNDISPATCHED) {
                        DependenciasBDImpl.inicializarTablasNecesariasCliente(CONFIGURACION_AMBIENTE.idCliente)
                    }

                    register(ManejadorDePeticiones::class.java.toString(), manejadorPeticiones)
                    register(SincronizadorDeDatos::class.java.toString(), sincronizadorDeDatos)

                    register(CTX_EVENTOS_CAMBIO_DE_USUARIO, eventosCambioDeUsuario)
                    register(CTX_EVENTOS_CAMBIO_DE_UBICACION, eventosCambioDeUbicacion)
                    register(CTX_CONTEXTO_DE_SESION, contextoDeSesion)
                    register(CTX_LECTOR_BARRAS, lectorBarras)
                    register(ProveedorOperacionesNFCPCSC::class.java.toString(), lectorNFC)
                }
            }

            contextoDeSesion.subscribe {
                println("Nuevo contexto: $it")
            }

            val flujoPrincipal = Flow(ControladorPrincipal::class.java)
            val panelParaEscena = flujoPrincipal.createHandler(contextoPrincipal).start(DefaultFlowContainer())

            // Resolución mínima de Cafam: 1366 x 768
            primaryStage.scene = Scene(panelParaEscena, 800.0, 600.0)
            primaryStage.icons.add(Image(AplicacionPrincipal::class.java.getResource("/imagenes/ic_aplicacion.png").toExternalForm()))
            primaryStage.title = "Silvernest"
            primaryStage.isMaximized = true

            with(primaryStage.scene.stylesheets)
            {
                add(AplicacionPrincipal::class.java.getResource("/css/jfoenix-fonts.css").toExternalForm())
                add(AplicacionPrincipal::class.java.getResource("/css/jfoenix-design.css").toExternalForm())
                add(AplicacionPrincipal::class.java.getResource("/css/estilo.css").toExternalForm())
            }

            primaryStage.show()
        }
    }

    private fun registrarErroresRxJava()
    {
        RxJavaPlugins.setErrorHandler { mostrarDialogoDeError(it.cause) }
    }

    private fun asignarExceptionHandlerGlobal()
    {
        val handlerComun = Thread.UncaughtExceptionHandler { _, e -> mostrarDialogoDeError(e) }
        Thread.setDefaultUncaughtExceptionHandler(handlerComun)
        Thread.currentThread().uncaughtExceptionHandler = handlerComun
    }

    private fun mostrarDialogoDeError(ex: Throwable?)
    {
        if (InfoEjecucion.esStandaloneApp && !InfoEjecucion.esDebugMode)
        {
            Logger.error(ex)
            //JOptionPane.showMessageDialog(null, "El susodicho error"+ex)
            try
            {
                val botonOkEsperar = ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)
                val alert = Alert(
                        Alert.AlertType.ERROR
                        , "Se produjo un error inesperado y la aplicación debe cerrarse.\n${ex?.message}"
                        , botonOkEsperar
                                 ).apply {
                    initStyle(StageStyle.UNDECORATED)
                }

                alert.showAndWait()

                Platform.exit()
            }
            catch (e: Exception)
            {
            }
        }
        else if (ex != null)
        {
            throw ex
        }
    }

    override fun stop()
    {
        Logger.info("Deteniendo")
        lectorBarras.apagarLector()
        DependenciasBDImpl.configuracionRepositorios.limpiarRecursos()
        JUnique.releaseLock(ID_APLICACION)
        exitProcess(0)
    }

    class ConfiguracionAmbiente(val idCliente: Long, _url: String)
    {
        val url =
                if (_url.endsWith('/'))
                {
                    _url
                }
                else
                {
                    "$_url/"
                }
    }
}