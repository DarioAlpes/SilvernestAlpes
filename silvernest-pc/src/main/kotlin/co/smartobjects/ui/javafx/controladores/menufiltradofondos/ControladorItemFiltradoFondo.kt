package co.smartobjects.ui.javafx.controladores.menufiltradofondos

import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosCategorias
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosCategoriasView
import co.smartobjects.ui.javafx.observarEnFx
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.animation.ScaleTransition
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.util.Duration
import java.net.URL
import java.util.*


internal class ControladorItemFiltradoFondo : Initializable
{
    @FXML
    internal lateinit var botonCategoria: JFXButton
    @FXML
    internal lateinit var separadorSuperior: Separator
    @FXML
    internal lateinit var iconoIndicador: FontAwesomeIconView
    @FXML
    internal lateinit var iconoCategoria: PrompterIconosCategoriasView
    @FXML
    internal lateinit var nombreCategoria: Label
    @FXML
    internal lateinit var separadorInferior: Separator

    private lateinit var animacionDeAgrandar: ScaleTransition
    private lateinit var animacionDeReducir: ScaleTransition

    init
    {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/layouts/menufiltradofondos/itemFiltradoFondos.fxml"))
        fxmlLoader.setController(this)
        fxmlLoader.load<JFXButton>()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        animacionDeAgrandar =
                ScaleTransition(Duration.millis(200.0), iconoIndicador).apply {
                    fromX = 0.0
                    fromY = 0.0
                    byX = 1.0
                    byY = 1.0
                    isAutoReverse = false
                }

        animacionDeReducir =
                ScaleTransition(Duration.millis(150.0), iconoIndicador).apply {
                    fromX = 1.0
                    fromY = 1.0
                    toX = 0.0
                    toY = 0.0
                    isAutoReverse = false
                }
    }

    fun crear(item: MenuFiltradoFondosUI.ItemFiltradoFondoUI<PrompterIconosCategorias>, posicion: Posicion): Node
    {
        iconoCategoria.glyphName = item.icono.name
        nombreCategoria.text = item.nombreFiltrado

        botonCategoria.setOnAction {
            item.activarFiltro()
            item.cambiarEstado(true)
        }

        item.estado.observarEnFx().subscribe {
            cambiarEstado(it)
        }

        separadorSuperior.isVisible = true
        separadorInferior.isVisible = true

        when (posicion)
        {
            ControladorItemFiltradoFondo.Posicion.PRIMERO -> separadorSuperior.isVisible = false
            ControladorItemFiltradoFondo.Posicion.INTERNO ->
            {
            }
            ControladorItemFiltradoFondo.Posicion.ULTIMO  -> separadorInferior.isVisible = false
        }

        return botonCategoria
    }

    private fun cambiarEstado(estaActivo: Boolean)
    {
        iconoCategoria.styleClass.removeAll("icono-color-acento", "icono-color-primario")
        iconoCategoria.styleClass.add(if (estaActivo) "icono-color-acento" else "icono-color-primario")

        nombreCategoria.styleClass.removeAll("etiqueta-18px-color-acento-medium", "etiqueta-18px-color-primario-medium")
        nombreCategoria.styleClass.add(if (estaActivo) "etiqueta-18px-color-acento-medium" else "etiqueta-18px-color-primario-medium")

        (if (estaActivo) animacionDeAgrandar else animacionDeReducir).play()
    }

    enum class Posicion
    {
        PRIMERO, INTERNO, ULTIMO
    }
}