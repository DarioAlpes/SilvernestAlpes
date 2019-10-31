package co.smartobjects.ui.javafx.controladores.menufiltradofondos

import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosCategorias
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import com.jfoenix.controls.JFXButton
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import javafx.event.ActionEvent
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@DisplayName("ControladorMenuFiltrado")
internal class ControladorMenuFiltradoPruebas : PruebaJavaFXBase()
{
    private val schedulerDePrueba = TestScheduler()

    private val mocksItemsFiltradosUI =
            List(3) { ind: Int ->
                (mockConDefaultAnswer(MenuFiltradoFondosUI.ItemFiltradoFondoUI::class.java) as MenuFiltradoFondosUI.ItemFiltradoFondoUI<PrompterIconosCategorias>)
                    .also {
                        doReturn("Nombre X $ind").`when`(it).nombreFiltrado
                        doReturn(MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso).`when`(it).criterioFiltrado
                        doReturn(PrompterIconosCategorias.PAQUETE).`when`(it).icono
                        doReturn(Observable.just(true)).`when`(it).estado
                        doNothing().`when`(it).cambiarEstado(true)
                        doNothing().`when`(it).activarFiltro()
                    }
            }


    private val mockMenuFiltradoFondosUI =
            (mockConDefaultAnswer(MenuFiltradoFondosUI::class.java) as MenuFiltradoFondosUI<PrompterIconosCategorias>)
                .also {
                    doReturn(Single.just(mocksItemsFiltradosUI))
                        .`when`(it)
                        .filtrosDisponibles
                }

    private val controladorEnPruebas = ControladorMenuFiltrado()


    @BeforeEach
    private fun asignarCamposAControladorEInicializar()
    {
        controladorEnPruebas.raiz = VBox()
        controladorEnPruebas.imagenDeError = ImageView()
        controladorEnPruebas.textoDeError = Label()
        controladorEnPruebas.scrollPane = ScrollPane()
        controladorEnPruebas.listaDeFiltros = VBox()
        controladorEnPruebas.inicializar(mockMenuFiltradoFondosUI, schedulerDePrueba)
    }

    @Nested
    inner class ListaDeFiltros
    {
        @Test
        fun antes_de_recibir_la_lista_se_encuentra_vacia()
        {
            assertTrue(controladorEnPruebas.listaDeFiltros.children.isEmpty())
        }

        @Test
        fun al_recibir_un_error_se_muestra_la_imagen_de_error()
        {
            val mockMenuFiltradoConError =
                    (mockConDefaultAnswer(MenuFiltradoFondosUI::class.java) as MenuFiltradoFondosUI<PrompterIconosCategorias>)
                        .also {
                            doReturn(Single.error<List<MenuFiltradoFondosUI.ItemFiltradoFondoUI<PrompterIconosCategorias>>>(IllegalStateException()))
                                .`when`(it)
                                .filtrosDisponibles
                        }

            controladorEnPruebas.raiz = VBox()
            controladorEnPruebas.imagenDeError = ImageView()
            controladorEnPruebas.textoDeError = Label()
            controladorEnPruebas.listaDeFiltros = VBox()
            controladorEnPruebas.inicializar(mockMenuFiltradoConError, schedulerDePrueba)

            validarEnThreadUI {
                assertTrue(controladorEnPruebas.imagenDeError.isVisible)
                assertTrue(controladorEnPruebas.textoDeError.isVisible)
            }
        }

        @Nested
        inner class AlRecibirFiltrosDisponibles
        {
            @BeforeEach
            fun recibirFiltrosDisponibles()
            {
                schedulerDePrueba.triggerActions()
            }

            @Test
            fun no_se_muestra_la_imagen_de_error()
            {
                validarEnThreadUI {
                    assertFalse(controladorEnPruebas.imagenDeError.isVisible)
                    assertFalse(controladorEnPruebas.textoDeError.isVisible)
                }
            }

            @Test
            fun la_lista_se_inicializa_con_todos_los_filtros_correctamente()
            {
                validarEnThreadUI {
                    val nodosHijos = controladorEnPruebas.listaDeFiltros.childrenUnmodifiable.count {
                        it.id == ControladorItemFiltradoFondo::botonCategoria.name
                    }
                    assertEquals(mocksItemsFiltradosUI.size, nodosHijos)
                }
            }

            @Test
            fun al_hacer_click_en_cada_elemento_se_activa_el_filtro_correspondiente()
            {
                for ((i, nodo) in controladorEnPruebas.listaDeFiltros.childrenUnmodifiable.withIndex())
                {
                    val boton = nodo as JFXButton
                    boton.fireEvent(ActionEvent())
                    verify(mocksItemsFiltradosUI[i]).activarFiltro()
                }
            }
        }
    }
}