package co.smartobjects.ui.modelos.menufiltrado

import co.smartobjects.entidades.fondos.*
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.mockConDefaultAnswer
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoFondo
import co.smartobjects.ui.modelos.ultimoEmitido
import io.reactivex.Single
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MenuFiltradoFondosPruebas : PruebasModelosRxBase()
{
    private val mockIcono = mockConDefaultAnswer(ProveedorIconosCategoriasFiltrado.Icono::class.java)
    private val mockProveedorIconosCategoriasFiltrado =
            object : ProveedorIconosCategoriasFiltrado<ProveedorIconosCategoriasFiltrado.Icono>
            {
                override fun darIcono(criterioDeFiltrado: MenuFiltradoFondosUI.CriterioFiltrado)
                        : ProveedorIconosCategoriasFiltrado.Icono
                {
                    return mockIcono
                }
            }

    private val filtrosDisponiblesMinimos: MutableList<MenuFiltradoFondosUI.ItemFiltradoFondoUI<ProveedorIconosCategoriasFiltrado.Icono>> =
            mutableListOf(
                    MenuFiltradoFondos.ItemFiltradoFondo(
                            "Todos",
                            mockIcono,
                            MenuFiltradoFondosUI.CriterioFiltrado.Todos,
                            true
                                                        ),
                    MenuFiltradoFondos.ItemFiltradoFondo(
                            "Paquetes",
                            mockIcono,
                            MenuFiltradoFondosUI.CriterioFiltrado.EsPaquete,
                            false
                                                        ),
                    MenuFiltradoFondos.ItemFiltradoFondo(
                            "Entradas",
                            mockIcono,
                            MenuFiltradoFondosUI.CriterioFiltrado.EsEntrada,
                            false
                                                        ),
                    MenuFiltradoFondos.ItemFiltradoFondo(
                            "Accesos",
                            mockIcono,
                            MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso,
                            false
                                                        ),
                    MenuFiltradoFondos.ItemFiltradoFondo(
                            "Dinero",
                            mockIcono,
                            MenuFiltradoFondosUI.CriterioFiltrado.EsDinero,
                            false
                                                        )
                         )

    @Test
    fun sin_categorias_los_filtros_disponibles_son_todos_paquetes_entradas_accesos_y_dinero_Todos_esta_seleccionado()
    {
        val modelo: MenuFiltradoFondosUI<ProveedorIconosCategoriasFiltrado.Icono> =
                MenuFiltradoFondos(
                        mockProveedorIconosCategoriasFiltrado,
                        Single.just(emptyList())
                                  )


        val observableDePrueba = modelo.filtrosDisponibles.test()

        observableDePrueba.assertResult(filtrosDisponiblesMinimos)

        assertEquals(filtrosDisponiblesMinimos, modelo.modelosHijos)
    }

    @Test
    fun con_categorias_los_filtros_disponibles_son_todos_paquetes_entradas_accesos_dinero_y_cada_una_de_las_categorías_sku_Todos_esta_seleccionado()
    {
        val categoriasSkuDePrueba = listOf("Uno", "Dos", "Tres").mapIndexed { i, nombreCategoria ->
            mockConDefaultAnswer(CategoriaSku::class.java).also {
                Mockito.doReturn(i.toLong()).`when`(it).id
                Mockito.doReturn(nombreCategoria).`when`(it).nombre
                Mockito.doReturn(linkedSetOf(2L, 3L, 4L)).`when`(it).idsDeAncestros
            }
        }

        val modelo: MenuFiltradoFondosUI<ProveedorIconosCategoriasFiltrado.Icono> =
                MenuFiltradoFondos(
                        mockProveedorIconosCategoriasFiltrado,
                        Single.just(categoriasSkuDePrueba)
                                  )

        val modelosHijosEsperados =
                mutableListOf<MenuFiltradoFondosUI.ItemFiltradoFondoUI<ProveedorIconosCategoriasFiltrado.Icono>>().apply {
                    addAll(filtrosDisponiblesMinimos)
                }

        categoriasSkuDePrueba.forEach {
            modelosHijosEsperados.add(
                    MenuFiltradoFondos.ItemFiltradoFondo(
                            it.nombre,
                            mockIcono,
                            MenuFiltradoFondosUI.CriterioFiltrado.TieneCategoriaSku(it.id, it.idsDeAncestros),
                            false
                                                        )
                                     )
        }

        val observablePrueba = modelo.filtrosDisponibles.test()

        assertEquals(modelosHijosEsperados, modelo.modelosHijos)

        assertEquals(observablePrueba.values().first() as List<ModeloUI>, modelo.modelosHijos)
    }

    @Test
    fun al_activar_un_filtro_se_desactivan_los_demas()
    {
        val modelo: MenuFiltradoFondosUI<ProveedorIconosCategoriasFiltrado.Icono> =
                MenuFiltradoFondos(
                        mockProveedorIconosCategoriasFiltrado,
                        Single.just(emptyList())
                                  )


        val observableFiltrosDisponibles = modelo.filtrosDisponibles.test()

        val filtrosEmitidos = observableFiltrosDisponibles.ultimoEmitido()

        val filtroInicialActivo = filtrosEmitidos.first { it.nombreFiltrado == "Todos" }
        val observableInicialEstado = filtroInicialActivo.estado.test()

        val primerFiltroAActivar = filtrosEmitidos.first { it.nombreFiltrado == "Entradas" }
        val observablePrimeroEstado = primerFiltroAActivar.estado.test()

        val segundoFiltroAActivar = filtrosEmitidos.first { it.nombreFiltrado == "Dinero" }
        val observableSegundoEstado = segundoFiltroAActivar.estado.test()

        observableInicialEstado.assertValuesOnly(true)
        observablePrimeroEstado.assertValuesOnly(false)
        observableSegundoEstado.assertValuesOnly(false)

        primerFiltroAActivar.activarFiltro()

        observableInicialEstado.assertValuesOnly(true, false)
        observablePrimeroEstado.assertValuesOnly(false, true)
        observableSegundoEstado.assertValuesOnly(false)

        segundoFiltroAActivar.activarFiltro()

        observableInicialEstado.assertValuesOnly(true, false)
        observablePrimeroEstado.assertValuesOnly(false, true, false)
        observableSegundoEstado.assertValuesOnly(false, true)
    }

    @Nested
    inner class ItemFiltrado
    {
        private val CRITERIO_FILTRADO = MenuFiltradoFondosUI.CriterioFiltrado.EsDinero
        private val ESTADO_INICIAL = false

        private val modelo: MenuFiltradoFondosUI.ItemFiltradoFondoUI<ProveedorIconosCategoriasFiltrado.Icono> =
                MenuFiltradoFondos.ItemFiltradoFondo(
                        "Criterio",
                        mockConDefaultAnswer(ProveedorIconosCategoriasFiltrado.Icono::class.java),
                        CRITERIO_FILTRADO,
                        ESTADO_INICIAL
                                                    )

        @Nested
        inner class CambiarDeEstado
        {
            private val observableDePrueba = modelo.estado.test()

            @Test
            fun al_suscribirse_emite_el_estado_inicial()
            {
                observableDePrueba.assertValuesOnly(ESTADO_INICIAL)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
            {
                with(modelo)
                {
                    finalizarProceso()
                    cambiarEstado(!ESTADO_INICIAL)
                }
                observableDePrueba.assertResult(ESTADO_INICIAL)
            }

            @Test
            fun solo_se_emite_estado_nuevo_si_es_diferente_al_actual()
            {
                modelo.cambiarEstado(ESTADO_INICIAL)
                observableDePrueba.assertValuesOnly(ESTADO_INICIAL)

                val nuevoEstado = !ESTADO_INICIAL
                modelo.cambiarEstado(nuevoEstado)
                observableDePrueba.assertValuesOnly(ESTADO_INICIAL, nuevoEstado)

                modelo.cambiarEstado(nuevoEstado)
                observableDePrueba.assertValuesOnly(ESTADO_INICIAL, nuevoEstado)

                val nuevoEstado2 = !nuevoEstado
                modelo.cambiarEstado(nuevoEstado2)
                observableDePrueba.assertValuesOnly(ESTADO_INICIAL, nuevoEstado, nuevoEstado2)
            }
        }

        @Nested
        inner class ActivarFiltro
        {
            private val observableDePrueba = modelo.filtroActivado.test()

            @Test
            fun al_suscribirse_no_se_emite_ningun_evento()
            {
                observableDePrueba.assertEmpty()
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
            {
                with(modelo)
                {
                    finalizarProceso()
                    activarFiltro()
                }
                observableDePrueba.assertResult()
            }

            @Test
            fun se_emite_el_criterio_de_filtrado()
            {
                modelo.activarFiltro()

                observableDePrueba.assertValuesOnly(CRITERIO_FILTRADO)
            }

            @Test
            fun cambio_el_estado_a_activo()
            {
                val obseravbleEstado = modelo.estado.test()

                obseravbleEstado.assertValuesOnly(ESTADO_INICIAL)

                modelo.activarFiltro()

                obseravbleEstado.assertValuesOnly(ESTADO_INICIAL, true)
            }
        }
    }

    @Nested
    inner class CriterioFiltrado
    {
        @Test
        fun deProductoFondoACriterioFiltrado_retorna_criterios_correctos_para_cada_tipo_de_producto_de_un_fondo()
        {
            val mockPrecioCompleto = mockConDefaultAnswer(PrecioCompleto::class.java)

            val productoDinero = ProductoFondo.SinCategoria(mockConDefaultAnswer(Dinero::class.java), mockPrecioCompleto)
            val productoEntrada = ProductoFondo.SinCategoria(mockConDefaultAnswer(Entrada::class.java), mockPrecioCompleto)
            val productoAcceso = ProductoFondo.SinCategoria(mockConDefaultAnswer(Acceso::class.java), mockPrecioCompleto)

            val idCategoriaSku = 999L
            val padresCategoriaSku = linkedSetOf(4L, 7L)
            val mockCategoriaSku = mockConDefaultAnswer(CategoriaSku::class.java).also {
                Mockito.doReturn(idCategoriaSku).`when`(it).id
                Mockito.doReturn(padresCategoriaSku).`when`(it).idsDeAncestros
            }

            val productoCategoriaSku = ProductoFondo.ConCategoria(mockCategoriaSku, mockPrecioCompleto)

            val idCategoriaSkuDelSku = 7854L
            val categoriasPadreParaLaDelSku = linkedSetOf(87L, 74L)
            val mockSku = mockConDefaultAnswer(Sku::class.java).also {
                Mockito.doReturn(idCategoriaSkuDelSku).`when`(it).idDeCategoria
            }
            val productoSku = ProductoFondo.ConCategoria(mockSku, mockPrecioCompleto, categoriasPadreParaLaDelSku)

            val convertir = { producto: ProductoFondo ->
                MenuFiltradoFondosUI.CriterioFiltrado.deProductoFondoACriterioFiltrado(producto)
            }

            assertEquals(MenuFiltradoFondosUI.CriterioFiltrado.EsDinero, convertir(productoDinero))
            assertEquals(MenuFiltradoFondosUI.CriterioFiltrado.EsEntrada, convertir(productoEntrada))
            assertEquals(MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso, convertir(productoAcceso))
            assertEquals(
                    MenuFiltradoFondosUI.CriterioFiltrado.TieneCategoriaSku(idCategoriaSku, padresCategoriaSku),
                    convertir(productoCategoriaSku)
                        )
            assertEquals(
                    MenuFiltradoFondosUI.CriterioFiltrado.TieneCategoriaSku(idCategoriaSkuDelSku, categoriasPadreParaLaDelSku),
                    convertir(productoSku)
                        )
        }

        @Test
        fun se_puede_filtrar_por_categoria_sku_usando_una_categoria_padre()
        {
            val idCategoriaPadre = 2L
            val criterioFiltardoDeCategoriaPadre =
                    MenuFiltradoFondosUI
                        .CriterioFiltrado
                        .TieneCategoriaSku(idCategoriaPadre, linkedSetOf())

            val criterioFiltardoDeCategoriaHija =
                    MenuFiltradoFondosUI
                        .CriterioFiltrado
                        .TieneCategoriaSku(1, linkedSetOf(idCategoriaPadre))

            assertTrue(criterioFiltardoDeCategoriaHija.aplicaFiltro(criterioFiltardoDeCategoriaPadre))
        }
    }
}