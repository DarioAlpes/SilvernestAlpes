package co.smartobjects.ui.javafx.controladores.catalogo

import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.modelos.NoSePudoCargarNingunDatoAsociadoAlCatalogo
import co.smartobjects.ui.modelos.catalogo.CatalogoUI
import co.smartobjects.ui.modelos.catalogo.ProductoUI
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.utilidades.Decimal
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import javafx.event.ActionEvent
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ControladorCatalogoProductosPruebas : PruebaJavaFXBase()
{
    private val schedulerDePrueba = TestScheduler()
    private val mockImagenProducto = mockConDefaultAnswer(ProveedorImagenesProductos.ImagenProducto::class.java)
        .also {
            doReturn("http://icons.iconarchive.com/icons/jozef89/origami-birds/72/bird-red-icon.png")
                .`when`(it)
                .urlImagen
        }

    private val mocksProductosUI =
            List(4) { indice ->
                mockConDefaultAnswer(ProductoUI::class.java)
                    .also {
                        doReturn(Maybe.just(mockImagenProducto)).`when`(it).imagen
                        doReturn("Producto $indice").`when`(it).nombre
                        doReturn(Decimal(indice)).`when`(it).precioTotal

                        val criterioDeFiltrado =
                                when (indice)
                                {
                                    0    -> MenuFiltradoFondosUI.CriterioFiltrado.EsDinero
                                    1    -> MenuFiltradoFondosUI.CriterioFiltrado.EsEntrada
                                    2    -> MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso
                                    else -> MenuFiltradoFondosUI.CriterioFiltrado.EsPaquete
                                }

                        doReturn(criterioDeFiltrado).`when`(it).criteriorDeFiltrado
                        doReturn(Observable.just(false)).`when`(it).estaSiendoAgregado
                        doReturn(Observable.just(true)).`when`(it).estaHabilitado
                        doReturn(false).`when`(it).esPaquete
                        doNothing().`when`(it).agregar()
                        doNothing().`when`(it).terminarAgregar()
                    }
            }


    private val controladorEnPruebas = ControladorCatalogoProductos()

    @BeforeEach
    private fun asignarCamposAControladorEInicializar()
    {
        controladorEnPruebas.catalogoDeProductos = TilePane()
        controladorEnPruebas.imagenDeError = ImageView()
        controladorEnPruebas.textoDeError = Label()
    }

    @Nested
    inner class CatalogoDeProductos
    {
        @Test
        fun antes_de_recibir_productos_la_lista_se_encuentra_vacia()
        {
            assertTrue(controladorEnPruebas.catalogoDeProductos.childrenUnmodifiable.isEmpty())
        }

        @Test
        fun al_recibir_un_error_se_muestra_la_imagen_de_error_diciendo_que_hubo_error_al_cargar_productos()
        {
            val mockCatalogoUI =
                    mockConDefaultAnswer(CatalogoUI::class.java)
                        .also {
                            val excepcion = NoSePudoCargarNingunDatoAsociadoAlCatalogo("Error", null)
                            doReturn(Observable.error<CatalogoUI.ResultadoCatalogo>(excepcion))
                                .`when`(it)
                                .catalogoDeProductos
                        }

            controladorEnPruebas.inicializar(mockCatalogoUI, schedulerDePrueba)
            schedulerDePrueba.triggerActions()

            validarEnThreadUI {
                assertTrue(controladorEnPruebas.imagenDeError.isVisible)
                assertTrue(controladorEnPruebas.textoDeError.isVisible)
                assertEquals("ERROR AL CARGAR CATÁLOGO DE PRODUCTOS", controladorEnPruebas.textoDeError.text)
            }
        }

        @Nested
        inner class AlRecibirProductos
        {
            @Nested
            inner class Parciales
            {
                @Test
                fun se_muestra_la_imagen_de_error_diciendo_que_se_produjo_una_carga_parcial()
                {
                    val mockCatalogoUI =
                            mockConDefaultAnswer(CatalogoUI::class.java)
                                .also {
                                    val resultadoDeCatalogo =
                                            CatalogoUI
                                                .ResultadoCatalogo(mocksProductosUI, IllegalStateException("Una excepción cualquiera"))

                                    doReturn(Observable.just(resultadoDeCatalogo))
                                        .`when`(it)
                                        .catalogoDeProductos
                                }

                    controladorEnPruebas.inicializar(mockCatalogoUI, schedulerDePrueba)
                    schedulerDePrueba.triggerActions()

                    validarEnThreadUI {
                        assertTrue(controladorEnPruebas.imagenDeError.isVisible)
                        assertTrue(controladorEnPruebas.textoDeError.isVisible)
                        assertEquals("SE PRODUJO UNA CARGA PARCIAL DE PRODUCTOS", controladorEnPruebas.textoDeError.text)
                    }
                }
            }

            @Nested
            inner class Vacios
            {
                @Test
                fun se_muestra_la_imagen_de_error_diciendo_que_no_hay_datos()
                {
                    val mockCatalogoUI =
                            mockConDefaultAnswer(CatalogoUI::class.java)
                                .also {
                                    val resultadoDeCatalogo =
                                            CatalogoUI
                                                .ResultadoCatalogo(listOf(), null)

                                    doReturn(Observable.just(resultadoDeCatalogo))
                                        .`when`(it)
                                        .catalogoDeProductos
                                }

                    controladorEnPruebas.inicializar(mockCatalogoUI, schedulerDePrueba)
                    schedulerDePrueba.triggerActions()

                    validarEnThreadUI {
                        assertTrue(controladorEnPruebas.imagenDeError.isVisible)
                        assertTrue(controladorEnPruebas.textoDeError.isVisible)
                        assertEquals("NO SE ENCONTRARON PRODUCTOS", controladorEnPruebas.textoDeError.text)
                    }
                }
            }

            @Nested
            inner class Completos
            {
                @BeforeEach
                fun recibirProductos()
                {
                    val mockCatalogoUI =
                            mockConDefaultAnswer(CatalogoUI::class.java)
                                .also {
                                    val resultadoDeCatalogo =
                                            CatalogoUI
                                                .ResultadoCatalogo(mocksProductosUI, null)

                                    doReturn(Observable.just(resultadoDeCatalogo))
                                        .`when`(it)
                                        .catalogoDeProductos
                                }

                    controladorEnPruebas.inicializar(mockCatalogoUI, schedulerDePrueba)
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
                fun al_hacer_click_en_agregar_se_invoca_el_metodo_de_agregar()
                {
                    for ((i, nodo) in controladorEnPruebas.catalogoDeProductos.childrenUnmodifiable.withIndex())
                    {
                        nodo.lookup("#botonAgregar").fireEvent(ActionEvent())
                        verify(mocksProductosUI[i]).agregar()
                    }
                }
            }
        }
    }
}