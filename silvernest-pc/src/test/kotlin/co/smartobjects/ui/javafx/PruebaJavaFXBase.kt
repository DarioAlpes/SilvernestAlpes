package co.smartobjects.ui.javafx

import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.sincronizadordecontenido.GestorDescargaDeDatos
import co.smartobjects.sincronizadordecontenido.SincronizadorDeDatos
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorPrincipal
import co.smartobjects.ui.javafx.controladores.controladorprincipal.ControladorToolbar
import co.smartobjects.ui.javafx.dependencias.DependenciasBD
import co.smartobjects.ui.javafx.dependencias.DependenciasDePantallas
import co.smartobjects.ui.javafx.dependencias.controladordeflujointerno.ControladorDeFlujoInterno
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosCategorias
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosGenerales
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosMenuPrincipal
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.utilidades.Opcional
import io.datafx.controller.flow.Flow
import io.datafx.controller.flow.FlowHandler
import io.datafx.controller.flow.context.ViewFlowContext
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.layout.StackPane
import javafx.scene.text.Font
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import java.util.concurrent.CountDownLatch
import javax.swing.SwingUtilities

internal abstract class PruebaJavaFXBase
{
    companion object
    {
        init
        {
            Font.loadFont(AplicacionPrincipal::class.java.getResource("/fonts/${PrompterIconosCategorias.NOMBRE_FAMILIA_FONT}.ttf").toExternalForm(), 14.0)
            Font.loadFont(AplicacionPrincipal::class.java.getResource("/fonts/${PrompterIconosGenerales.NOMBRE_FAMILIA_FONT}.ttf").toExternalForm(), 14.0)
            Font.loadFont(AplicacionPrincipal::class.java.getResource("/fonts/${PrompterIconosMenuPrincipal.NOMBRE_FAMILIA_FONT}.ttf").toExternalForm(), 14.0)
        }

        // Hay que forzar inicialización de javafx, ver https://stackoverflow.com/questions/18429422/basic-junit-test-for-javafx-8
        @[JvmStatic BeforeAll Suppress("unused")]
        fun inicializarJavaFX()
        {
            val latch = CountDownLatch(1)

            SwingUtilities.invokeLater {
                JFXPanel()

                latch.countDown()
            }
            latch.await()
        }

        /**
         * Para probar acciones de UI ejecutadas en Platform.runLater
         */
        fun validarEnThreadUI(prueba: () -> Unit)
        {
            var excepcion: Throwable? = null
            val latch = CountDownLatch(1)
            Platform.runLater {
                try
                {
                    prueba()
                }
                catch (e: Throwable)
                {
                    excepcion = e
                }
                finally
                {
                    latch.countDown()
                }
            }
            latch.await()
            if (excepcion != null)
            {
                throw excepcion!!
            }
        }

        fun <T> cualquiera(): T
        {
            ArgumentMatchers.any<T>()
            @Suppress("UNCHECKED_CAST")
            return null as T
        }

        fun <T : Any> eqParaKotlin(value: T): T = ArgumentMatchers.eq(value) ?: value
    }

    private val errores = mutableListOf<Throwable>()

    protected val mockToolbar = mockConDefaultAnswer(ControladorToolbar::class.java).also {
        doNothing().`when`(it).cambiarTitulo(ArgumentMatchers.anyString())
        doNothing().`when`(it).puedeVolver(ArgumentMatchers.anyBoolean())
        doNothing().`when`(it).puedeSincronizar(ArgumentMatchers.anyBoolean())
        doNothing().`when`(it).mostrarMenu(ArgumentMatchers.anyBoolean())
    }
    internal val eventosCambioDeUsuario = BehaviorSubject.createDefault<Opcional<Usuario>>(Opcional.Vacio())

    private val mockRepositorios = mockConDefaultAnswer(DependenciasBD::class.java).also {
        doReturn(mockConDefaultAnswer(RepositorioUbicaciones::class.java)).`when`(it).repositorioUbicaciones
    }

    private val mockSincronizadorDeDatos = mockConDefaultAnswer(SincronizadorDeDatos::class.java).also {
        val mockGestorDescarga = mockConDefaultAnswer(GestorDescargaDeDatos::class.java)
        doReturn(mockGestorDescarga).`when`(it).gestorDescargaDeDatos
    }

    protected fun darMockContextoFlujo(): ViewFlowContext
    {
        return ViewFlowContext().apply {
            applicationContext.register(AplicacionPrincipal.CTX_STAGE_PRIMARIO, Stage())
            applicationContext.register(AplicacionPrincipal.CTX_EVENTOS_CAMBIO_DE_USUARIO, eventosCambioDeUsuario)
            applicationContext.register(DependenciasBD::class.java.toString(), mockRepositorios)
            applicationContext.register(SincronizadorDeDatos::class.java.toString(), mockSincronizadorDeDatos)
            applicationContext.register(
                    AplicacionPrincipal.CTX_CONTEXTO_DE_SESION,
                    Observable.just(Opcional.De(mockConDefaultAnswer(ContextoDeSesion::class.java)))
                                       )

            register(ControladorPrincipal.CTX_TOOLBAR, mockToolbar)
            register(ControladorPrincipal.CTX_CONTENDOR_PRINCIPAL, StackPane())
            register(DependenciasDePantallas())
        }
    }

    protected fun darMockControladorDeFlujoInterno(): ControladorDeFlujoInterno
    {
        val contextoFlujo = darMockContextoFlujo()

        return spy(ControladorDeFlujoInterno(FlowHandler(mockConDefaultAnswer(Flow::class.java), contextoFlujo))).also {
            doNothing().`when`(it).navigate(cualquiera<Class<*>>())
        }
    }

    @BeforeEach
    fun registrarErrores()
    {
        RxJavaPlugins.setErrorHandler { errores.add(it) }
    }

    @AfterEach
    fun revisarErrores()
    {
        Assertions.assertTrue(errores.isEmpty(), "Erores: $errores")
    }
}