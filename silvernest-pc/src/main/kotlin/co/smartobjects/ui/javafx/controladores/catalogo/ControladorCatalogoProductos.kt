package co.smartobjects.ui.javafx.controladores.catalogo

import co.smartobjects.ui.javafx.ValoresPorDefecto
import co.smartobjects.ui.javafx.hacerEscondible
import co.smartobjects.ui.javafx.usarSchedulersEnUI
import co.smartobjects.ui.modelos.catalogo.CatalogoUI
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane

internal class ControladorCatalogoProductos : ScrollPane()
{
    @FXML
    internal lateinit var catalogoDeProductos: TilePane
    @FXML
    internal lateinit var imagenDeError: ImageView
    @FXML
    internal lateinit var textoDeError: Label


    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/catalogo/catalogoDeProductos.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<ScrollPane>()
    }

    fun inicializar(catalogoUI: CatalogoUI, schedulerBackground: Scheduler = Schedulers.io())
    {
        imagenDeError.hacerEscondible()
        textoDeError.hacerEscondible()

        catalogoUI
            .catalogoDeProductos
            .usarSchedulersEnUI(schedulerBackground)
            .subscribe(
                    {
                        esVisibleImagenYTextoErrorSinItems(false)

                        val productosAMostrar = it.catalogo.map { ControladorItemProducto().actualizar(it) }

                        if (it.error != null)
                        {
                            mostrarErrorCargaParcial()
                        }
                        else if (productosAMostrar.isEmpty())
                        {
                            mostrarErrorNoHayProductos()
                        }

                        catalogoDeProductos.children.clear()
                        catalogoDeProductos.children.addAll(productosAMostrar)
                    },
                    {
                        mostrarErrorFatal()
                    }
                      )
    }

    private fun esVisibleImagenYTextoErrorSinItems(mostrar: Boolean)
    {
        imagenDeError.isVisible = mostrar
        textoDeError.isVisible = mostrar
    }

    private fun mostrarErrorFatal()
    {
        esVisibleImagenYTextoErrorSinItems(true)

        imagenDeError.image = ValoresPorDefecto.imagenErrorCargaDatos
        textoDeError.text = "ERROR AL CARGAR CATÁLOGO DE PRODUCTOS"
    }

    private fun mostrarErrorNoHayProductos()
    {
        esVisibleImagenYTextoErrorSinItems(true)

        imagenDeError.image = ValoresPorDefecto.imagenNoHayDatos
        textoDeError.text = "NO SE ENCONTRARON PRODUCTOS"
    }

    private fun mostrarErrorCargaParcial()
    {
        esVisibleImagenYTextoErrorSinItems(true)

        imagenDeError.image = ValoresPorDefecto.imagenCargaParcial
        textoDeError.text = "SE PRODUJO UNA CARGA PARCIAL DE PRODUCTOS"
    }
}