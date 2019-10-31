package co.smartobjects.ui.modelos.catalogo

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.ui.modelos.mockConDefaultAnswer
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoFondo
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoPaquete
import co.smartobjects.utilidades.Decimal
import io.reactivex.Maybe
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doReturn
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue


internal class ProductoPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_FONDO = 123L
        private const val CODIGO_EXTERNO_FONDO = "código externo $ID_FONDO"
    }

    private val mockFondoDinero = mockConDefaultAnswer(Dinero::class.java).also {
        doReturn(ID_FONDO).`when`(it).id
        doReturn(CODIGO_EXTERNO_FONDO).`when`(it).codigoExterno
        doReturn("Producto Fondo").`when`(it).nombre
    }

    private val ID_PAQUETE = 47258L
    private val CODIGO_EXTERNO_PAQUETE = "código externo paquete"
    private val paquete = mockConDefaultAnswer(Paquete::class.java).also {
        doReturn(ID_PAQUETE).`when`(it).id
        doReturn(CODIGO_EXTERNO_PAQUETE).`when`(it).codigoExterno
        doReturn("Producto Paquete").`when`(it).nombre
        doReturn(listOf(Paquete.FondoIncluido(ID_FONDO, "El código externo", Decimal(100))))
            .`when`(it)
            .fondosIncluidos
    }

    private val mockPrecio = mockConDefaultAnswer(PrecioCompleto::class.java).also {
        doReturn(Decimal.UNO).`when`(it).precioConImpuesto
        doReturn(Decimal.UNO.toString()).`when`(it).toString()
    }

    private val mockDeProveedorDeImagenesDeProductos =
            mockConDefaultAnswer(ProveedorImagenesProductos::class.java)
                .also {
                    doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                        .`when`(it)
                        .darImagen(anyLong(), anyBoolean())
                }

    @Nested
    inner class Instanciacion
    {
        @Test
        fun para_un_producto_de_fondo_se_inicializa_correctamente()
        {
            val productoFondoEsperado = ProductoFondo.SinCategoria(mockFondoDinero, mockPrecio)

            val imagen = Maybe.empty<ProveedorImagenesProductos.ImagenProducto>()

            val productoInstanciado =
                    Producto(
                            productoFondoEsperado,
                            mockConDefaultAnswer(ProveedorImagenesProductos::class.java).also {
                                doReturn(imagen)
                                    .`when`(it)
                                    .darImagen(eq(ID_FONDO), eq(false))
                            }
                            )

            assertEquals(imagen, productoInstanciado.imagen)
            assertEquals("Producto Fondo", productoInstanciado.nombre)
            assertEquals(MenuFiltradoFondosUI.CriterioFiltrado.EsDinero, productoInstanciado.criteriorDeFiltrado)
            assertNull(productoInstanciado.idPaquete)
            assertEquals(linkedSetOf(ID_FONDO), productoInstanciado.idsFondosAsociados)
            assertEquals(listOf("Producto Fondo"), productoInstanciado.nombresFondosAsociados)
            assertNull(productoInstanciado.cantidadesFondosEnPaquete)
            assertEquals(listOf(mockPrecio), productoInstanciado.preciosDeFondosAsociados)
            assertFalse(productoInstanciado.esPaquete)
        }

        @Test
        fun para_un_producto_de_paquete_se_inicializa_correctamente()
        {
            val productoFondoEsperado =
                    ProductoPaquete(
                            paquete,
                            listOf("Nombre fondo de prueba"),
                            listOf(mockPrecio)
                                   )

            val imagen = Maybe.empty<ProveedorImagenesProductos.ImagenProducto>()

            val productoInstanciado =
                    Producto(
                            productoFondoEsperado,
                            mockConDefaultAnswer(ProveedorImagenesProductos::class.java).also {
                                doReturn(imagen)
                                    .`when`(it)
                                    .darImagen(eq(ID_PAQUETE), eq(true))
                            }
                            )

            assertEquals(imagen, productoInstanciado.imagen)
            assertEquals("Producto Paquete", productoInstanciado.nombre)
            assertEquals(MenuFiltradoFondosUI.CriterioFiltrado.EsPaquete, productoInstanciado.criteriorDeFiltrado)
            assertEquals((ID_PAQUETE), productoInstanciado.idPaquete)
            assertEquals(linkedSetOf(ID_FONDO), productoInstanciado.idsFondosAsociados)
            assertEquals(listOf("Nombre fondo de prueba"), productoInstanciado.nombresFondosAsociados)
            assertEquals(listOf(paquete.fondosIncluidos.first().cantidad), productoInstanciado.cantidadesFondosEnPaquete)
            assertEquals(listOf(mockPrecio), productoInstanciado.preciosDeFondosAsociados)
            assertTrue(productoInstanciado.esPaquete)
        }
    }

    @Nested
    inner class ImagenAsociada
    {
        @Test
        fun apenas_esta_disponible_la_imagen_se_emite()
        {
            val schedulerPrueba = TestScheduler()
            val mockImagenProducto = mockConDefaultAnswer(ProveedorImagenesProductos.ImagenProducto::class.java).also {
                doReturn("").`when`(it).toString()
            }

            val mockDeProveedorDeImagenesDeProductosConResultado =
                    mockConDefaultAnswer(ProveedorImagenesProductos::class.java)
                        .also {
                            doReturn(Maybe.just(mockImagenProducto))
                                .`when`(it)
                                .darImagen(anyLong(), anyBoolean())
                        }

            val productoFondo = ProductoFondo.SinCategoria(mockFondoDinero, mockPrecio)

            val modelo =
                    Producto(
                            productoFondo,
                            mockDeProveedorDeImagenesDeProductosConResultado
                            )

            val observableDePrueba =
                    modelo.imagen
                        .subscribeOn(schedulerPrueba)
                        .observeOn(schedulerPrueba)
                        .test()

            observableDePrueba.assertEmpty()

            schedulerPrueba.triggerActions()

            observableDePrueba.assertResult(mockImagenProducto)
        }
    }

    @Nested
    inner class PrecioTotal
    {
        @Test
        fun es_la_suma_de_los_precios_con_impuesto_de_los_fondos_asociados()
        {
            val mockPrecio1 = mockConDefaultAnswer(PrecioCompleto::class.java).also {
                doReturn(Decimal(23)).`when`(it).precioConImpuesto
            }

            val mockPrecio2 = mockConDefaultAnswer(PrecioCompleto::class.java).also {
                doReturn(Decimal(17)).`when`(it).precioConImpuesto
            }

            val productoPaquete =
                    ProductoPaquete(
                            paquete,
                            listOf("Nombre fondo de prueba 1", "Nombre fondo de prueba 2"),
                            listOf(mockPrecio1, mockPrecio2)
                                   )

            val modelo =
                    Producto(
                            productoPaquete,
                            mockDeProveedorDeImagenesDeProductos
                            )

            assertEquals(mockPrecio1.precioConImpuesto + mockPrecio2.precioConImpuesto, modelo.precioTotal)
        }
    }

    @Nested
    inner class EstaSiendoAgregado
    {
        private val fondo = mockConDefaultAnswer(Dinero::class.java).also {
            doReturn(123L).`when`(it).id
            doReturn("código externo 123").`when`(it).codigoExterno
            doReturn("Producto Fondo").`when`(it).nombre
        }

        private val modelo =
                Producto(
                        ProductoFondo.SinCategoria(
                                fondo,
                                mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                    doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                }
                                                  ),
                        mockDeProveedorDeImagenesDeProductos
                        )

        private val observableDePrueba = modelo.estaSiendoAgregado.test()!!

        @Test
        fun al_suscribirse_emite_false()
        {
            observableDePrueba.assertValuesOnly(false)
        }

        @Nested
        inner class AlAgregar
        {
            @Test
            fun se_emite_true()
            {
                modelo.agregar()

                observableDePrueba.assertValuesOnly(false, true)
            }

            @Test
            fun solo_emite_si_no_esta_ya_agregando()
            {
                modelo.agregar()
                observableDePrueba.assertValuesOnly(false, true)
                modelo.agregar()
                observableDePrueba.assertValuesOnly(false, true)
            }

            @Test
            fun solo_emite_si_esta_habilitado()
            {
                modelo.habilitar(false)

                modelo.agregar()
                observableDePrueba.assertValuesOnly(false)

                modelo.habilitar(true)
                modelo.agregar()
                observableDePrueba.assertValuesOnly(false, true)
            }
        }

        @Nested
        inner class AlTerminarAgregar
        {
            @Test
            fun solo_emite_si_esta_agregando()
            {
                modelo.terminarAgregar()

                observableDePrueba.assertValuesOnly(false)
            }

            @Test
            fun si_estaba_agregando_se_emite_false()
            {
                modelo.agregar()
                modelo.terminarAgregar()

                observableDePrueba.assertValuesOnly(false, true, false)
            }

            @Test
            fun solo_emite_si_esta_habilitado()
            {
                modelo.agregar()

                modelo.habilitar(false)

                modelo.terminarAgregar()
                observableDePrueba.assertValuesOnly(false, true)

                modelo.habilitar(true)
                modelo.terminarAgregar()
                observableDePrueba.assertValuesOnly(false, true, false)
            }
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
        {
            modelo.agregar()
            observableDePrueba.assertValuesOnly(false, true)

            modelo.finalizarProceso()

            modelo.agregar()
            observableDePrueba.assertResult(false, true)
        }
    }

    @Nested
    inner class EstaHabilitado
    {
        private val fondo = mockConDefaultAnswer(Dinero::class.java).also {
            doReturn(123L).`when`(it).id
            doReturn("código externo 123").`when`(it).codigoExterno
            doReturn("Producto Fondo").`when`(it).nombre
        }

        private val modelo =
                Producto(
                        ProductoFondo.SinCategoria(
                                fondo,
                                mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                    doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                }
                                                  ),
                        mockDeProveedorDeImagenesDeProductos
                        )

        private val observableDePrueba = modelo.estaHabilitado.test()!!

        @Test
        fun al_suscribirse_emite_true()
        {
            observableDePrueba.assertValuesOnly(true)
        }

        @Test
        fun cambia_a_false_cuando_se_deshabilita()
        {
            modelo.habilitar(false)

            observableDePrueba.assertValuesOnly(true, false)
        }

        @Test
        fun cambia_a_true_cuando_se_habilita()
        {
            modelo.habilitar(false)
            observableDePrueba.assertValuesOnly(true, false)
            modelo.habilitar(true)
            observableDePrueba.assertValuesOnly(true, false, true)
        }

        @Test
        fun solo_emite_valores_cuando_hay_un_cambio()
        {
            modelo.habilitar(false)
            observableDePrueba.assertValuesOnly(true, false)
            modelo.habilitar(false)
            observableDePrueba.assertValuesOnly(true, false)
        }

        @Test
        fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
        {
            modelo.habilitar(false)
            observableDePrueba.assertValuesOnly(true, false)

            modelo.finalizarProceso()

            modelo.habilitar(true)
            observableDePrueba.assertResult(true, false)
        }
    }

    @Nested
    inner class ProductoAAgregar
    {
        private val mockFondo = mockConDefaultAnswer(Dinero::class.java).also {
            doReturn(123L).`when`(it).id
            doReturn("código externo 123").`when`(it).codigoExterno
            doReturn("Producto Fondo").`when`(it).nombre
        }

        private val modelo =
                Producto(
                        ProductoFondo.SinCategoria(
                                mockFondo,
                                mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                    doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                    doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                }
                                                  ),
                        mockDeProveedorDeImagenesDeProductos
                        )

        private val observableDePrueba = modelo.productoAAgregar.test()!!

        @Test
        fun no_emite_ningun_producto_al_suscrbirse()
        {
            observableDePrueba.assertEmpty()
        }

        @Test
        fun solo_emite_si_esta_habilitado()
        {
            modelo.habilitar(false)
            modelo.agregar()
            observableDePrueba.assertEmpty()

            modelo.habilitar(true)
            modelo.agregar()
            observableDePrueba.assertValuesOnly(modelo)
        }

        @Nested
        inner class SiEstaSiendoAgregado
        {
            @BeforeEach
            fun fijarEstaSiendoAgregado()
            {
                modelo.agregar()
            }

            @Test
            fun emite_el_producto()
            {
                observableDePrueba.assertValuesOnly(modelo)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
            {
                observableDePrueba.assertValuesOnly(modelo)

                modelo.finalizarProceso()

                modelo.agregar()
                observableDePrueba.assertResult(modelo)
            }
        }
    }

    @Test
    fun se_pueden_actualizar_los_precios_correctamente()
    {
        val precioCompletoEsperado = mockConDefaultAnswer(PrecioCompleto::class.java).also {
            doReturn(Decimal(1.24543636)).`when`(it).precioConImpuesto
            doReturn(Decimal(1.24543636).toString()).`when`(it).toString()
        }

        val productoInstanciado =
                Producto(
                        ProductoFondo.SinCategoria(mockFondoDinero, mockPrecio),
                        mockConDefaultAnswer(ProveedorImagenesProductos::class.java).also {
                            doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                                .`when`(it)
                                .darImagen(eq(ID_FONDO), eq(false))
                            doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                        }
                        )

        val productoActualizado = productoInstanciado.actualizarPreciosAsociados(listOf(precioCompletoEsperado))

        assertEquals(precioCompletoEsperado, productoActualizado.preciosDeFondosAsociados.first())
    }
}