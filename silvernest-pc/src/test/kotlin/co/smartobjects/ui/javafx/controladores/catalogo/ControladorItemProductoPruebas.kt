package co.smartobjects.ui.javafx.controladores.catalogo

import co.smartobjects.ui.javafx.PruebaJavaFXBase
import co.smartobjects.ui.javafx.mockConDefaultAnswer
import co.smartobjects.ui.javafx.verificarEstilos
import co.smartobjects.ui.modelos.catalogo.ProductoUI
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.utilidades.Decimal
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.event.ActionEvent
import javafx.scene.control.Tooltip
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import java.text.NumberFormat
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ControladorItemProductoPruebas : PruebaJavaFXBase()
{
    @Test
    fun al_actualizar_se_conectan_controles_correctamente_el_producto()
    {
        val itemProducto = ControladorItemProducto()

        val nombre = "Producto de prueba"
        val precioTotal = 123.456
        val mockProductoUI =
                mockConDefaultAnswer(ProductoUI::class.java)
                    .also {
                        doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>()).`when`(it).imagen
                        doReturn(nombre).`when`(it).nombre
                        doReturn(Decimal(precioTotal)).`when`(it).precioTotal
                        doReturn(Observable.just(false)).`when`(it).estaSiendoAgregado
                        doReturn(Observable.just(true)).`when`(it).estaHabilitado
                        doReturn(false).`when`(it).esPaquete
                        doNothing().`when`(it).agregar()
                    }

        val precioFormateado =
                NumberFormat
                    .getCurrencyInstance(Locale.getDefault())
                    .apply {
                        maximumFractionDigits = 0
                    }
                    .format(precioTotal)

        itemProducto.actualizar(mockProductoUI, Schedulers.trampoline())

        validarEnThreadUI {
            assertEquals(nombre, itemProducto.nombre.text)
            assertEquals(precioFormateado, itemProducto.precio.text)
            itemProducto.botonAgregar.verificarEstilos("jfx-button-agregar-catalogo-default", "jfx-button-agregar-catalogo-activo")
        }

        itemProducto.botonAgregar.fireEvent(ActionEvent())
        verify(mockProductoUI).agregar()
    }

    @Test
    fun si_esta_deshabilitado_el_producto_se_deshabilita_el_boton_de_agregar()
    {
        val itemProducto = ControladorItemProducto()

        val mockProductoUI =
                mockConDefaultAnswer(ProductoUI::class.java)
                    .also {
                        doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>()).`when`(it).imagen
                        doReturn("Producto de prueba").`when`(it).nombre
                        doReturn(Decimal(123.456)).`when`(it).precioTotal
                        doReturn(Observable.just(true)).`when`(it).estaSiendoAgregado
                        doReturn(Observable.just(false)).`when`(it).estaHabilitado
                        doReturn(false).`when`(it).esPaquete
                        doNothing().`when`(it).agregar()
                    }

        itemProducto.actualizar(mockProductoUI, Schedulers.trampoline())

        validarEnThreadUI {
            assertTrue(itemProducto.botonAgregar.isDisable)
        }
    }

    @Test
    fun al_estar_siendo_agregado_usa_el_estilo_correcto()
    {
        val itemProducto = ControladorItemProducto()

        val mockProductoUI =
                mockConDefaultAnswer(ProductoUI::class.java)
                    .also {
                        doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>()).`when`(it).imagen
                        doReturn("Producto de prueba").`when`(it).nombre
                        doReturn(Decimal(123.456)).`when`(it).precioTotal
                        doReturn(Observable.just(true)).`when`(it).estaSiendoAgregado
                        doReturn(Observable.just(true)).`when`(it).estaHabilitado
                        doReturn(false).`when`(it).esPaquete
                        doNothing().`when`(it).agregar()
                    }

        itemProducto.actualizar(mockProductoUI, Schedulers.trampoline())

        validarEnThreadUI {
            itemProducto.botonAgregar.verificarEstilos("jfx-button-agregar-catalogo-activo", "jfx-button-agregar-catalogo-default")
        }
    }

    @Test
    fun si_no_es_paquete_no_agrega_a_la_imagen_tooltip_correcto()
    {
        val itemProducto = ControladorItemProducto()
        val mockProductoUI =
                mockConDefaultAnswer(ProductoUI::class.java)
                    .also {
                        doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>()).`when`(it).imagen
                        doReturn("Producto de prueba").`when`(it).nombre
                        doReturn(Decimal(123.456)).`when`(it).precioTotal
                        doReturn(Observable.just(false)).`when`(it).estaSiendoAgregado
                        doReturn(Observable.just(true)).`when`(it).estaHabilitado
                        doReturn(false).`when`(it).esPaquete
                        doReturn(listOf("A")).`when`(it).nombresFondosAsociados
                        doNothing().`when`(it).agregar()
                    }

        itemProducto.actualizar(mockProductoUI, Schedulers.trampoline())

        val tooltipAgregado = itemProducto.imagen.properties.entries.firstOrNull { it.value is Tooltip }

        assertNull(tooltipAgregado)
    }

    @Test
    fun si_es_paquete_agrega_a_la_imagen_tooltip_correcto()
    {
        val nombresFondos = listOf("A", "B", "C")
        val textoEsperado = nombresFondos.joinToString(prefix = "\n", separator = "\n")

        val itemProducto = ControladorItemProducto()
        val mockProductoUI =
                mockConDefaultAnswer(ProductoUI::class.java)
                    .also {
                        doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>()).`when`(it).imagen
                        doReturn("Producto de prueba").`when`(it).nombre
                        doReturn(Decimal(123.456)).`when`(it).precioTotal
                        doReturn(Observable.just(false)).`when`(it).estaSiendoAgregado
                        doReturn(Observable.just(true)).`when`(it).estaHabilitado
                        doReturn(true).`when`(it).esPaquete
                        doReturn(nombresFondos).`when`(it).nombresFondosAsociados
                        doNothing().`when`(it).agregar()
                    }

        itemProducto.actualizar(mockProductoUI, Schedulers.trampoline())

        val tooltipAgregado = itemProducto.imagen.properties.entries.first { it.value is Tooltip }.let { it.value as Tooltip }

        assertEquals(textoEsperado, tooltipAgregado.text)
    }
}