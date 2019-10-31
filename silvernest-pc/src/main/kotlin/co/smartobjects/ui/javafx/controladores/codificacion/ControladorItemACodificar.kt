package co.smartobjects.ui.javafx.controladores.codificacion

import co.smartobjects.ui.javafx.comoDineroFormateado
import co.smartobjects.ui.javafx.controladores.codificacion.creditosacodificar.ControladorCreditosACodificarDePersona
import co.smartobjects.ui.javafx.controladores.registropersonas.ControladorInformacionPersonaConGrupo
import co.smartobjects.ui.javafx.hacerEscondible
import co.smartobjects.ui.javafx.usarSchedulersEnUI
import co.smartobjects.ui.modelos.codificacion.ItemACodificarUI
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.reactivex.schedulers.Schedulers
import javafx.animation.Animation
import javafx.animation.Interpolator
import javafx.animation.RotateTransition
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.transform.Rotate
import javafx.util.Duration


class ControladorItemACodificar : VBox()
{
    @FXML
    internal lateinit var raiz: VBox
    @FXML
    internal lateinit var informacionPersonaConGrupo: ControladorInformacionPersonaConGrupo
    @FXML
    internal lateinit var saldo: Label
    @FXML
    internal lateinit var creditosACodificarDePersona: ControladorCreditosACodificarDePersona
    @FXML
    internal lateinit var estadoCodificacion: Label
    @FXML
    internal lateinit var contendorEstadoCodificacion: HBox
    @FXML
    internal lateinit var botonReintentarActivar: JFXButton
    @FXML
    internal lateinit var iconoDeReintentarActivar: FontAwesomeIconView

    private val animacionRotarIconoActivacion: RotateTransition

    private val snackbarErrores: JFXSnackbar by lazy {
        JFXSnackbar(raiz).apply {
            (popupContainer.children.last { it is Group } as Group).children[0].styleClass.add("jfx-snackbar-content-error")
        }
    }

    init
    {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/layouts/codificacion/itemACodificar.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<VBox>()

        animacionRotarIconoActivacion =
                RotateTransition().apply {
                    axis = Rotate.Z_AXIS
                    byAngle = 360.0
                    cycleCount = Animation.INDEFINITE
                    duration = Duration.seconds(1.0)
                    interpolator = Interpolator.LINEAR
                    isAutoReverse = false
                    node = iconoDeReintentarActivar
                }
    }

    fun inicializar(itemACodificarUI: ItemACodificarUI)
    {
        informacionPersonaConGrupo.asignarPersonaConGrupoCliente(itemACodificarUI.creditosACodificar.personaConGrupoCliente)

        saldo.text = itemACodificarUI.totalPagado.comoDineroFormateado(1)

        creditosACodificarDePersona.inicializar(itemACodificarUI.itemsCreditosACodificar)

        itemACodificarUI.estado.usarSchedulersEnUI(Schedulers.io()).subscribe {
            val texto =
                    when (it)
                    {
                        ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar  -> "SIN CODIFICAR"
                        ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag  -> "ESPERANDO TAG"
                        ItemACodificarUI.Estado.Codificando                      -> "CODIFICANDO"
                        is ItemACodificarUI.Estado.EtapaSesionManilla.Codificada -> "CODIFICADA"
                        ItemACodificarUI.Estado.Activando                        -> "ACTIVANDO"
                        ItemACodificarUI.Estado.EtapaSesionManilla.Activada      -> "ACTIVADA"
                    }

            estadoCodificacion.text = texto
        }

        itemACodificarUI.estado.usarSchedulersEnUI(Schedulers.io()).subscribe {
            val estiloAUsar =
                    when (it)
                    {
                        ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar  -> "contendorEstadoCodificacionSinCodificar"
                        ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag  -> "contendorEstadoCodificacionEsperandoTag"
                        ItemACodificarUI.Estado.Codificando                      -> "contendorEstadoCodificacionEsperandoTag"
                        is ItemACodificarUI.Estado.EtapaSesionManilla.Codificada -> "contendorEstadoCodificacionCodificada"
                        ItemACodificarUI.Estado.Activando                        -> "contendorEstadoCodificacionCodificada"
                        ItemACodificarUI.Estado.EtapaSesionManilla.Activada      -> "contendorEstadoCodificacionActivada"
                    }

            with(contendorEstadoCodificacion.styleClass)
            {
                removeAll { true }
                add(estiloAUsar)
            }
        }

        inicializarBotonReintentarActivar(itemACodificarUI)

        itemACodificarUI.mensajesDeError.usarSchedulersEnUI(Schedulers.io()).subscribe {
            if (it.isEmpty())
            {
                snackbarErrores.close()
            }
            else
            {
                snackbarErrores.show(it, 3000)
            }
        }
    }

    private fun inicializarBotonReintentarActivar(itemACodificarUI: ItemACodificarUI)
    {
        botonReintentarActivar.setOnAction { itemACodificarUI.intentarActivarSesion() }

        botonReintentarActivar.hacerEscondible()
        itemACodificarUI.estado.usarSchedulersEnUI(Schedulers.io()).subscribe {
            botonReintentarActivar.isDisable = it !is ItemACodificarUI.Estado.EtapaSesionManilla.Codificada
            botonReintentarActivar.isVisible =
                    it is ItemACodificarUI.Estado.EtapaSesionManilla.Codificada
                    || it === ItemACodificarUI.Estado.Activando

            if (it === ItemACodificarUI.Estado.Activando)
            {
                animacionRotarIconoActivacion.play()
            }
            else
            {
                animacionRotarIconoActivacion.stop()
            }
        }
    }
}