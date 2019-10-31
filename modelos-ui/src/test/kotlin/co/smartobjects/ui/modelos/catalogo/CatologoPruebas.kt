package co.smartobjects.ui.modelos.catalogo

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Entrada
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.logica.fondos.ProveedorCategoriasPadres
import co.smartobjects.logica.fondos.ProveedorNombresYPreciosPorDefectoCompletosFondos
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoFondo
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoPaquete
import co.smartobjects.utilidades.Decimal
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import io.reactivex.observers.TestObserver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.inOrder
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class CatalogoPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_FONDO = 1234L
        private const val CODIGO_EXTERNO_FONDO = "código externo fondo $ID_FONDO"
        private const val ID_PAQUETE = 4567L
        private const val CODIGO_EXTERNO_PAQUETE = "código externo paquete"
        private const val ID_FONDO_INCLUIDO = 8989L
        private const val NOMBRE_FONDO_INCLUIDO = "Fondo incluido de prueba"
    }

    private val mockDeProveedorDeImagenesDeProductos =
            mockConDefaultAnswer(ProveedorImagenesProductos::class.java)
                .also {
                    doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                        .`when`(it)
                        .darImagen(anyLong(), anyBoolean())
                }

    private val mockDePrecio =
            mockConDefaultAnswer(PrecioCompleto::class.java).also {
                doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
            }

    private val fondo = mockConDefaultAnswer(Dinero::class.java).also {
        doReturn(ID_FONDO).`when`(it).id
        doReturn(CODIGO_EXTERNO_FONDO).`when`(it).codigoExterno
        doReturn("Producto Fondo").`when`(it).nombre
    }

    private val paquete = mockConDefaultAnswer(Paquete::class.java).also {
        doReturn(ID_PAQUETE).`when`(it).id
        doReturn(CODIGO_EXTERNO_PAQUETE).`when`(it).codigoExterno
        doReturn("Producto Paquete").`when`(it).nombre
        doReturn(listOf(Paquete.FondoIncluido(ID_FONDO_INCLUIDO, "El código externo", Decimal.UNO)))
            .`when`(it)
            .fondosIncluidos
    }

    private val mockProveedorCategoriasPadres =
            mockConDefaultAnswer(ProveedorCategoriasPadres::class.java)
                .let { Single.just(it) }

    private val mockProveedorNombresYPreciosCompletosFondos =
            mockConDefaultAnswer(ProveedorNombresYPreciosPorDefectoCompletosFondos::class.java)
                .also {
                    doReturn(listOf(NOMBRE_FONDO_INCLUIDO))
                        .`when`(it)
                        .darNombresFondosSegunIds(eqParaKotlin(linkedSetOf(ID_FONDO_INCLUIDO)))

                    // Para un fondo solo
                    doReturn(listOf(mockDePrecio))
                        .`when`(it)
                        .completarPreciosFondos(eqParaKotlin(linkedSetOf(ID_FONDO)))
                    doReturn(mockDePrecio)
                        .`when`(it)
                        .completarPrecioFondo(eqParaKotlin(ID_FONDO))

                    // Para el fondo de un paquete
                    doReturn(listOf(mockDePrecio))
                        .`when`(it)
                        .completarPreciosFondos(eqParaKotlin(linkedSetOf(ID_FONDO_INCLUIDO)))
                }.let {
                    Single.just(it)
                }

    @Test
    fun se_invoca_proveedor_con_argumentos_correctos_al_obtener_imagenes()
    {
        val modelo =
                Catalogo(
                        Single.just(listOf(paquete)),
                        Single.just(listOf<Fondo<*>>(fondo)),
                        mockProveedorCategoriasPadres,
                        mockProveedorNombresYPreciosCompletosFondos,
                        mockDeProveedorDeImagenesDeProductos
                        )

        modelo.catalogoDeProductos.test()

        with(inOrder(mockDeProveedorDeImagenesDeProductos))
        {
            verify(mockDeProveedorDeImagenesDeProductos).darImagen(paquete.id!!, true)
            verify(mockDeProveedorDeImagenesDeProductos).darImagen(fondo.id!!, false)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun si_el_catalogo_de_fondos_y_el_de_catalogo_de_paquetes_emiten_listas_vacias_no_completa_y_no_emite_error()
    {
        val modelo =
                Catalogo(
                        Single.just(listOf()),
                        Single.just(listOf()),
                        mockProveedorCategoriasPadres,
                        mockProveedorNombresYPreciosCompletosFondos,
                        mockDeProveedorDeImagenesDeProductos
                        )

        val valorEsperado =
                CatalogoUI
                    .ResultadoCatalogo(listOf(), null)

        val observable = modelo.catalogoDeProductos.test()

        observable.assertValuesOnly(valorEsperado)
    }

    @Test
    fun si_no_falla_ni_el_catalogo_de_fondos_ni_el_de_catalogo_de_paquetes_emite_ambos_valores_y_no_completa()
    {
        val modelo =
                Catalogo(
                        Single.just(listOf(paquete)),
                        Single.just(listOf(fondo)),
                        mockProveedorCategoriasPadres,
                        mockProveedorNombresYPreciosCompletosFondos,
                        mockDeProveedorDeImagenesDeProductos
                        )

        val valorEsperadoFondo =
                Producto(
                        ProductoFondo.SinCategoria(fondo, mockDePrecio),
                        mockDeProveedorDeImagenesDeProductos
                        )

        val valorEsperadoPaquete =
                Producto(
                        ProductoPaquete(paquete, listOf(NOMBRE_FONDO_INCLUIDO), listOf(mockDePrecio)),
                        mockDeProveedorDeImagenesDeProductos
                        )

        val valorEsperado =
                CatalogoUI
                    .ResultadoCatalogo(listOf(valorEsperadoPaquete, valorEsperadoFondo), null)

        val observable = modelo.catalogoDeProductos.test()

        observable.assertValuesOnly(valorEsperado)
    }

    @Test
    fun si_falla_el_observable_de_catalogo_de_fondos_pero_no_el_de_catalogo_de_paquetes_emite_el_valor_y_el_error_y_no_completa()
    {
        val errorEsperado = NullPointerException()
        val modelo =
                Catalogo(
                        Single.just(listOf(paquete)),
                        Single.error(errorEsperado),
                        mockProveedorCategoriasPadres,
                        mockProveedorNombresYPreciosCompletosFondos,
                        mockDeProveedorDeImagenesDeProductos
                        )

        val productoEsperado =
                Producto(
                        ProductoPaquete(paquete, listOf(NOMBRE_FONDO_INCLUIDO), listOf(mockDePrecio)),
                        mockDeProveedorDeImagenesDeProductos
                        )

        val valorEsperado =
                CatalogoUI
                    .ResultadoCatalogo(listOf(productoEsperado), errorEsperado)

        val observable = modelo.catalogoDeProductos.test()

        observable.assertValuesOnly(valorEsperado)
    }

    @Test
    fun si_falla_el_observable_de_catalogo_de_paquetes_pero_no_el_de_catalogo_de_fondos_emite_el_valor_y_el_error_y_no_completa()
    {
        val errorEsperado = NullPointerException()
        val modelo =
                Catalogo(
                        Single.error(errorEsperado),
                        Single.just(listOf(fondo)),
                        mockProveedorCategoriasPadres,
                        mockProveedorNombresYPreciosCompletosFondos,
                        mockDeProveedorDeImagenesDeProductos
                        )

        val productoEsperado =
                Producto(
                        ProductoFondo.SinCategoria(fondo, mockDePrecio),
                        mockDeProveedorDeImagenesDeProductos
                        )

        val valorEsperado =
                CatalogoUI
                    .ResultadoCatalogo(listOf(productoEsperado), errorEsperado)

        val observable = modelo.catalogoDeProductos.test()

        observable.assertValuesOnly(valorEsperado)
    }

    @Test
    fun si_falla_el_observable_de_catalogo_de_fondos_y_el_de_catalogo_de_paquetes_emite_los_errores_y_termina()
    {
        val errorEsperadoFondo = NullPointerException()
        val errorEsperadoPaquete = IllegalAccessError()
        val modelo =
                Catalogo(
                        Single.error(errorEsperadoPaquete),
                        Single.error(errorEsperadoFondo),
                        mockProveedorCategoriasPadres,
                        mockProveedorNombresYPreciosCompletosFondos,
                        mockDeProveedorDeImagenesDeProductos
                        )

        val erroresEsperados = listOf(errorEsperadoPaquete, errorEsperadoFondo)
        val observable = modelo.catalogoDeProductos.test()

        observable
            .assertSubscribed()
            .assertNoValues()
            .assertError(NoSePudoCargarNingunDatoAsociadoAlCatalogo::class.java)
            .assertError {
                ((it as NoSePudoCargarNingunDatoAsociadoAlCatalogo).excepcion as CompositeException).exceptions == erroresEsperados
            }
            .assertTerminated()
    }

    @Nested
    inner class AlFiltrar
    {
        private val fondoEntrada =
                mockConDefaultAnswer(Entrada::class.java).also {
                    doReturn(ID_FONDO).`when`(it).id
                    doReturn(CODIGO_EXTERNO_FONDO).`when`(it).codigoExterno
                    doReturn("Producto Fondo 2").`when`(it).nombre
                }

        private val productosFondo = listOf(fondo, fondoEntrada)

        private val productosUI =
                listOf<ProductoUI>(
                        Producto(
                                ProductoFondo.SinCategoria(fondo, mockDePrecio),
                                mockDeProveedorDeImagenesDeProductos
                                ),
                        Producto(
                                ProductoFondo.SinCategoria(fondoEntrada, mockDePrecio),
                                mockDeProveedorDeImagenesDeProductos
                                )
                                  )

        private val modelo =
                Catalogo(
                        Single.just(listOf()),
                        Single.just(productosFondo),
                        mockProveedorCategoriasPadres,
                        mockProveedorNombresYPreciosCompletosFondos,
                        mockDeProveedorDeImagenesDeProductos
                        )

        private val resultadoCatalogoInicial =
                CatalogoUI
                    .ResultadoCatalogo(productosUI, null)

        private val observableDePrueba = modelo.catalogoDeProductos.test()

        @Test
        fun por_todos_se_emiten_todos_los_productos_disponibles()
        {
            modelo.filtrarPorCriterio(MenuFiltradoFondosUI.CriterioFiltrado.Todos)

            observableDePrueba.assertValuesOnly(resultadoCatalogoInicial, resultadoCatalogoInicial)
        }

        @Test
        fun por_una_categoria_con_productos_se_emiten_los_productos_disponibles_de_esa_categoria()
        {
            val criterioDeFiltrado = MenuFiltradoFondosUI.CriterioFiltrado.EsDinero
            val resultadoEsperado =
                    CatalogoUI
                        .ResultadoCatalogo(
                                productosUI.filter { it.criteriorDeFiltrado == criterioDeFiltrado },
                                null
                                          )

            modelo.filtrarPorCriterio(criterioDeFiltrado)

            observableDePrueba.assertValuesOnly(resultadoCatalogoInicial, resultadoEsperado)
        }

        @Test
        fun por_una_categoria_de_rango_vacio_se_emite_una_lista_vacia()
        {
            val categoriaFiltrado = MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso
            val resultadoEsperado = CatalogoUI.ResultadoCatalogo(emptyList(), null)

            modelo.filtrarPorCriterio(categoriaFiltrado)

            observableDePrueba.assertValuesOnly(resultadoCatalogoInicial, resultadoEsperado)
        }

        @Test
        fun por_una_categoria_de_rango_vacio_y_luego_por_una_que_si_tiene_productos_se_emite_una_lista_vacia_y_luego_una_de_productos()
        {
            val categoriaFiltradoSinResultados = MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso
            val categoriaFiltradoConResultados = MenuFiltradoFondosUI.CriterioFiltrado.EsDinero
            val resultadoEsperadoRangoVacio = CatalogoUI.ResultadoCatalogo(emptyList(), null)
            val resultadoEsperadoConResultados =
                    CatalogoUI
                        .ResultadoCatalogo(
                                productosUI.filter { it.criteriorDeFiltrado == categoriaFiltradoConResultados },
                                null
                                          )

            modelo.filtrarPorCriterio(categoriaFiltradoSinResultados)
            modelo.filtrarPorCriterio(categoriaFiltradoConResultados)

            observableDePrueba.assertValuesOnly(resultadoCatalogoInicial, resultadoEsperadoRangoVacio, resultadoEsperadoConResultados)
        }

        @Test
        fun por_diferentes_categorias_que_si_tiene_productos_se_emite_listas_correctas()
        {
            val categoriaFiltrado1 = MenuFiltradoFondosUI.CriterioFiltrado.EsDinero
            val resultadoEsperado1 =
                    CatalogoUI
                        .ResultadoCatalogo(
                                productosUI.filter { it.criteriorDeFiltrado == categoriaFiltrado1 },
                                null
                                          )

            val categoriaFiltrado2 = MenuFiltradoFondosUI.CriterioFiltrado.EsEntrada
            val resultadoEsperado2 =
                    CatalogoUI
                        .ResultadoCatalogo(
                                productosUI.filter { it.criteriorDeFiltrado == categoriaFiltrado2 },
                                null
                                          )

            modelo.filtrarPorCriterio(categoriaFiltrado1)
            modelo.filtrarPorCriterio(categoriaFiltrado2)

            observableDePrueba.assertValuesOnly(resultadoCatalogoInicial, resultadoEsperado1, resultadoEsperado2)
        }
    }

    @Nested
    inner class AlMarcarProductoComoEstaSiendoAgregado
    {
        private val listaDePaquetes = listOf(paquete)
        private val listaDeFondos = listOf(fondo, fondo, fondo)

        private val modelo =
                Catalogo(
                        Single.just(listaDePaquetes),
                        Single.just(listaDeFondos),
                        mockProveedorCategoriasPadres,
                        mockProveedorNombresYPreciosCompletosFondos,
                        mockDeProveedorDeImagenesDeProductos
                        )

        private lateinit var productosEmitidos: List<ProductoUI>
        private val observadoresHabilitado = mutableListOf<Pair<ProductoUI, TestObserver<Boolean>>>()

        @BeforeEach
        fun marcarProductoComoEstaSiendoAgregado()
        {
            val observableDePrueba = modelo.catalogoDeProductos.test()
            productosEmitidos = observableDePrueba.ultimoEmitido().catalogo

            productosEmitidos.forEach {
                observadoresHabilitado.add(Pair(it, it.estaHabilitado.test()))
            }

            productosEmitidos.find { it.esPaquete }!!.agregar()
        }

        @Test
        fun se_deshabilitan_los_demas_productos()
        {
            for ((productoEmitido, observador) in observadoresHabilitado)
            {
                if (productoEmitido.esPaquete)
                {
                    assertTrue(observador.ultimoEmitido())
                }
                else
                {
                    assertFalse(observador.ultimoEmitido())
                }
            }
        }

        @Test
        fun al_no_estar_siendo_agregado_se_rehabilitan_los_demas_productos()
        {
            productosEmitidos.find { it.esPaquete }!!.terminarAgregar()

            for ((_, observador) in observadoresHabilitado)
            {
                assertTrue(observador.ultimoEmitido())
            }
        }
    }
}