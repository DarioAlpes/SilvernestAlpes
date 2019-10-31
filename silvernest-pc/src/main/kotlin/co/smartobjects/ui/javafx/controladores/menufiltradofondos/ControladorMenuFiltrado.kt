package co.smartobjects.ui.javafx.controladores.menufiltradofondos

import co.smartobjects.ui.javafx.hacerEscondible
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosCategorias
import co.smartobjects.ui.javafx.usarSchedulersEnUI
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.ui.modelos.menufiltrado.ProveedorIconosCategoriasFiltrado
import com.jfoenix.controls.JFXScrollPane
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox

internal class ProveedorIconosCategoriasFiltradoJavaFX : ProveedorIconosCategoriasFiltrado<PrompterIconosCategorias>
{
    override fun darIcono(criterioDeFiltrado: MenuFiltradoFondosUI.CriterioFiltrado): PrompterIconosCategorias
    {
        return when (criterioDeFiltrado)
        {
            MenuFiltradoFondosUI.CriterioFiltrado.Todos                -> PrompterIconosCategorias.GENERICO
            MenuFiltradoFondosUI.CriterioFiltrado.EsPaquete            -> PrompterIconosCategorias.PAQUETE
            MenuFiltradoFondosUI.CriterioFiltrado.EsEntrada            -> PrompterIconosCategorias.ENTRADA
            MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso             -> PrompterIconosCategorias.ACCESO
            MenuFiltradoFondosUI.CriterioFiltrado.EsDinero             -> PrompterIconosCategorias.DINERO
            is MenuFiltradoFondosUI.CriterioFiltrado.TieneCategoriaSku -> PrompterIconosCategorias.GENERICO
        }
    }
}

internal class ControladorMenuFiltrado : VBox()
{
    @FXML
    internal lateinit var raiz: VBox
    @FXML
    internal lateinit var imagenDeError: ImageView
    @FXML
    internal lateinit var textoDeError: Label
    @FXML
    internal lateinit var scrollPane: ScrollPane
    @FXML
    internal lateinit var listaDeFiltros: VBox


    init
    {
        val loader = FXMLLoader(javaClass.getResource("/layouts/menufiltradofondos/menuDeFiltrado.fxml"))

        loader.setController(this)
        loader.setRoot(this)
        loader.load<VBox>()

        if (scrollPane.content != null)
        {
            JFXScrollPane.smoothScrolling(scrollPane)
        }

        imagenDeError.hacerEscondible()
        textoDeError.hacerEscondible()
    }

    fun inicializar(menuFiltradoFondosUI: MenuFiltradoFondosUI<PrompterIconosCategorias>, schedulerBackground: Scheduler = Schedulers.io())
    {
        menuFiltradoFondosUI
            .filtrosDisponibles
            .usarSchedulersEnUI(schedulerBackground)
            .subscribe(
                    {
                        imagenDeError.isVisible = false
                        textoDeError.isVisible = false

                        listaDeFiltros.children.clear()
                        for ((i, filtro) in it.withIndex())
                        {
                            val controladorItem = ControladorItemFiltradoFondo()

                            val posicion =
                                    when (i)
                                    {
                                        0            -> ControladorItemFiltradoFondo.Posicion.PRIMERO
                                        it.lastIndex -> ControladorItemFiltradoFondo.Posicion.ULTIMO
                                        else         -> ControladorItemFiltradoFondo.Posicion.INTERNO
                                    }

                            listaDeFiltros.children.add(controladorItem.crear(filtro, posicion))
                        }
                    },
                    {
                        imagenDeError.isVisible = true
                        textoDeError.isVisible = true
                    }
                      )
    }
}