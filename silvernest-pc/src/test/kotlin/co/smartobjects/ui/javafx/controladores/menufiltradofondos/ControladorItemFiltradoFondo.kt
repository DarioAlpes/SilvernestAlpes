package co.smartobjects.ui.javafx.controladores.menufiltradofondos

import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.iconosyfonts.PrompterIconosCategorias
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.javafx.verificarEstilos
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondos
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import io.reactivex.Observable
import javafx.event.ActionEvent
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ControladorItemFiltradoFondoPruebas : PruebaJavaFXBase()
{
    @Test
    fun al_crear_un_filtro_al_inicio_se_conectan_controles_correctamente()
    {
        val itemFiltrado = ControladorItemFiltradoFondo()

        val nombre = "Nombre de la categoría"
        val itemFiltradoFondo =
                mockConDefaultAnswer(MenuFiltradoFondosUI.ItemFiltradoFondoUI::class.java).also {
                    doReturn(nombre).`when`(it).nombreFiltrado
                    doReturn(MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso).`when`(it).criterioFiltrado
                    doReturn(PrompterIconosCategorias.GENERICO).`when`(it).icono
                    doReturn(Observable.just(true)).`when`(it).estado
                    doNothing().`when`(it).cambiarEstado(anyBoolean())
                    doNothing().`when`(it).activarFiltro()
                } as MenuFiltradoFondosUI.ItemFiltradoFondoUI<PrompterIconosCategorias>


        itemFiltrado.crear(itemFiltradoFondo, ControladorItemFiltradoFondo.Posicion.PRIMERO)

        validarEnThreadUI {
            assertEquals(nombre, itemFiltrado.nombreCategoria.text)
            assertTrue(itemFiltrado.iconoIndicador.styleClass.contains("icono-color-acento"))
        }

        itemFiltrado.botonCategoria.fireEvent(ActionEvent())
        verify(itemFiltradoFondo).activarFiltro()
        verify(itemFiltradoFondo).cambiarEstado(true)
    }

    @Test
    fun al_crear_un_filtro_al_inicio_se_oculta_separador_superior_y_el_inferior_es_visibles()
    {
        val itemFiltrado = ControladorItemFiltradoFondo()

        val nombre = "Nombre de la categoría"
        val estado = true
        val itemFiltradoFondo =
                MenuFiltradoFondos.ItemFiltradoFondo(
                        nombre,
                        PrompterIconosCategorias.GENERICO,
                        MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso,
                        estado
                                                    )

        itemFiltrado.crear(itemFiltradoFondo, ControladorItemFiltradoFondo.Posicion.PRIMERO)

        validarEnThreadUI {
            assertFalse(itemFiltrado.separadorSuperior.isVisible)
            assertTrue(itemFiltrado.separadorInferior.isVisible)
        }
    }

    @Test
    fun al_crear_un_filtro_al_final_se_oculta_separador_inferior_y_el_superior_es_visibles()
    {
        val itemFiltrado = ControladorItemFiltradoFondo()

        val nombre = "Nombre de la categoría"
        val estado = true
        val itemFiltradoFondo =
                MenuFiltradoFondos.ItemFiltradoFondo(
                        nombre,
                        PrompterIconosCategorias.GENERICO,
                        MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso,
                        estado
                                                    )

        itemFiltrado.crear(itemFiltradoFondo, ControladorItemFiltradoFondo.Posicion.ULTIMO)

        validarEnThreadUI {
            assertFalse(itemFiltrado.separadorInferior.isVisible)
            assertTrue(itemFiltrado.separadorSuperior.isVisible)
        }
    }

    @Test
    fun cuando_esta_activo_usa_el_color_de_acento_para_el_icono_de_categoria_y_el_nombre()
    {
        val itemFiltrado = ControladorItemFiltradoFondo()
        val itemFiltradoFondo =
                mockConDefaultAnswer(MenuFiltradoFondosUI.ItemFiltradoFondoUI::class.java).also {
                    doReturn("Nombre de la categoría").`when`(it).nombreFiltrado
                    doReturn(MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso).`when`(it).criterioFiltrado
                    doReturn(PrompterIconosCategorias.GENERICO).`when`(it).icono
                    doReturn(Observable.just(true)).`when`(it).estado
                } as MenuFiltradoFondosUI.ItemFiltradoFondoUI<PrompterIconosCategorias>


        itemFiltrado.crear(itemFiltradoFondo, ControladorItemFiltradoFondo.Posicion.PRIMERO)

        validarEnThreadUI {
            itemFiltrado.nombreCategoria.verificarEstilos("etiqueta-18px-color-acento-medium", "etiqueta-18px-color-primario-medium")
        }
    }
}