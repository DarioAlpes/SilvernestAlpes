package co.smartobjects.ui.javafx.controladores.catalogo

import co.smartobjects.ui.javafx.*
import co.smartobjects.ui.modelos.catalogo.ProductoUI
import com.jfoenix.controls.JFXButton
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import java.net.URL
import java.util.*


internal class ControladorItemProducto : Initializable
{
    @FXML
    internal lateinit var raiz: VBox
    @FXML
    internal lateinit var imagen: ImageView
    @FXML
    internal lateinit var nombre: Label
    @FXML
    internal lateinit var precio: Label
    @FXML
    internal lateinit var botonAgregar: JFXButton

    init
    {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/layouts/catalogo/itemProducto.fxml"))
        fxmlLoader.setController(this)
        fxmlLoader.load<VBox>()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        imagen.isCache = true
        imagen.image = ValoresPorDefecto.imagenPorDefecto
    }

    fun actualizar(
            productoUI: ProductoUI,
            schedulerBackground: Scheduler = Schedulers.io()
                  ): Node
    {
        productoUI.imagen
            .map {
                Image(it.urlImagen, 40.0, 0.0, true, true, false)
            }
            .usarSchedulersEnUI(schedulerBackground)
            .subscribe {
                imagen.image = it
            }

        nombre.text = productoUI.nombre
        //precio.text = productoUI.precioTotal.comoDineroFormateado()
        precio.text = ""

        productoUI.estaSiendoAgregado.usarSchedulersEnUI(schedulerBackground).subscribe {
            productoUI.nombre
            val estiloAUsar = if (it) "jfx-button-agregar-catalogo-activo" else "jfx-button-agregar-catalogo-default"
            botonAgregar.styleClass.removeAll("jfx-button-agregar-catalogo-activo", "jfx-button-agregar-catalogo-default")
            botonAgregar.styleClass.add(estiloAUsar)
        }

        botonAgregar.bindEstaHabilitado(productoUI.estaHabilitado)

        botonAgregar.setOnAction {
            productoUI.agregar()
        }

        if (productoUI.esPaquete)
        {
            Tooltip.install(imagen, inicializarTooltipConTexto(productoUI.nombresFondosAsociados.joinToString(prefix = "\n", separator = "\n")))
        }

        return raiz
    }
}