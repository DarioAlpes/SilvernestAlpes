package co.smartobjects.ui.modelos

import co.smartobjects.ui.modelos.fechas.FechaUI
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("ListaFiltrableUIConSujetos")
internal class ListaFiltrableUIConSujetosPruebas : PruebasModelosRxBase()
{
    private val itemInexistente = 9999

    @Nested
    inner class ConListaVacia
    {
        private val modelo = ListaFiltrableUIConSujetos<Int>(listOf())

        @Nested
        inner class Items
        {
            @Test
            fun inicializa_con_lista_vacia()
            {
                assertFalse(modelo.items.any())
            }
        }

        @Nested
        inner class ItemsSeleccionados
        {
            private val testItemsSeleccionados = modelo.itemsSeleccionados.test()

            @Test
            fun emite_lista_vacia_al_inicializar()
            {
                testItemsSeleccionados.assertValueCount(1)
                testItemsSeleccionados.assertValue(listOf())
            }

            @Test
            fun emite_lista_vacia_al_llamar_seleccionarTodos()
            {
                modelo.seleccionarTodos()
                testItemsSeleccionados.assertValueCount(2)
                testItemsSeleccionados.assertValueAt(1, listOf())
            }

            @Test
            fun emite_lista_vacia_al_llamar_deseleccionarTodos()
            {
                modelo.deseleccionarTodos()
                testItemsSeleccionados.assertValueCount(2)
                testItemsSeleccionados.assertValueAt(1, listOf())
            }

            @Test
            fun no_emite_nuevos_valores_al_seleccionar_item_inexistente()
            {
                modelo.seleccionarItem(itemInexistente)
                testItemsSeleccionados.assertValueCount(1)
            }

            @Test
            fun no_emite_nuevos_valores_al_deseleccionar_item_inexistente()
            {
                modelo.deseleccionarItem(itemInexistente)
                testItemsSeleccionados.assertValueCount(1)
            }
        }

        @Nested
        inner class TodosLosHabilitadosEstanSeleccionados
        {
            private val testTodosSeleccionados = modelo.todosLosHabilitadosEstanSeleccionados.test()

            @Test
            fun emite_true_al_inicializar()
            {
                testTodosSeleccionados.assertValueCount(1)
                testTodosSeleccionados.assertValue(true)
            }

            @Test
            fun emite_true_al_llamar_seleccionarTodos()
            {
                modelo.seleccionarTodos()
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.assertValueAt(1, true)
            }

            @Test
            fun emite_true_al_llamar_deseleccionarTodos()
            {
                modelo.deseleccionarTodos()
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.assertValueAt(1, true)
            }

            @Test
            fun no_emite_nuevos_valores_al_seleccionar_item_inexistente()
            {
                modelo.seleccionarItem(itemInexistente)
                testTodosSeleccionados.assertValueCount(1)
            }

            @Test
            fun no_emite_nuevos_valores_al_deseleccionar_item_inexistente()
            {
                modelo.deseleccionarItem(itemInexistente)
                testTodosSeleccionados.assertValueCount(1)
            }
        }

        @Nested
        inner class NumeroHabilitadosSeleccionados
        {
            private val testTodosSeleccionados = modelo.numeroHabilitadosSeleccionados.test()

            @Test
            fun emite_el_tamaño_de_la_lista_al_inicializar()
            {
                testTodosSeleccionados.assertValueCount(1)
                testTodosSeleccionados.assertValue(0)
            }

            @Test
            fun emite_true_al_llamar_seleccionarTodos()
            {
                modelo.seleccionarTodos()
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.assertValueAt(0, 0)
            }

            @Test
            fun emite_true_al_llamar_deseleccionarTodos()
            {
                modelo.deseleccionarTodos()
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.assertValueAt(0, 0)
            }
        }

        @Nested
        inner class ItemsFiltrables
        {
            @Test
            fun se_inicializa_con_lista_vacia()
            {
                assertFalse(modelo.itemsFiltrables.any())
            }
        }

        @Test
        fun es_igual_a_otra_si_todas_las_propiedades_no_observables_son_iguales()
        {
            val primeraLista = ListaFiltrableUIConSujetos<FechaUI>(listOf())
            val segundaLista = ListaFiltrableUIConSujetos<FechaUI>(listOf())

            assertEquals(primeraLista, segundaLista)
        }
    }

    @Nested
    inner class ConListaConItems
    {
        private val item1 = 0
        private val item2 = 1
        private val item3 = 2
        private val listaItems = listOf(item1, item2, item3)
        private val modelo: ListaFiltrableUI<Int> = ListaFiltrableUIConSujetos(listaItems)

        @Nested
        inner class Items
        {
            @Test
            fun inicializa_con_lista_correcta()
            {
                assertEquals(listOf(item1, item2, item3), modelo.items)
            }
        }

        @Nested
        inner class ItemsSeleccionados
        {
            private val testItemsSeleccionados = modelo.itemsSeleccionados.test()

            @Test
            fun emite_lista_con_todos_los_items_al_inicializar()
            {
                testItemsSeleccionados.assertValueCount(1)
                testItemsSeleccionados.assertValue(listOf(item1, item2, item3))
            }

            @Test
            fun emite_lista_vacia_al_llamar_deseleccionarTodos()
            {
                modelo.deseleccionarTodos()
                testItemsSeleccionados.assertValueCount(2)
                testItemsSeleccionados.assertValueAt(1, listOf())
            }

            @Test
            fun emite_lista_con_item_deseleccionado_al_deseleccionar_item_existente_y_seleccionado()
            {
                modelo.deseleccionarItem(item2)
                testItemsSeleccionados.assertValueCount(2)
                testItemsSeleccionados.assertValueAt(1, listOf(item1, item3))
            }

            @Test
            fun emite_lista_solo_con_item_seleccionado_al_seleccionar_item_existente_y_deseleccionado_con_otros_items_deseleccionados()
            {
                modelo.deseleccionarTodos()
                modelo.seleccionarItem(item2)
                testItemsSeleccionados.assertValueCount(3)
                testItemsSeleccionados.assertValueAt(2, listOf(item2))
            }

            @Test
            fun emite_lista_con_todos_los_items_seleccionados_al_seleccionar_item_existente_y_deseleccionado_con_otros_items_seleccionados()
            {
                modelo.deseleccionarItem(item2)
                modelo.seleccionarItem(item2)
                testItemsSeleccionados.assertValueCount(3)
                testItemsSeleccionados.assertValueAt(2, listOf(item1, item2, item3))
            }

            @Test
            fun emite_lista_con_todos_los_items_al_llamar_seleccionarTodos_despues_de_deseleccionar_todos()
            {
                modelo.deseleccionarTodos()
                modelo.seleccionarTodos()
                testItemsSeleccionados.assertValueCount(3)
                testItemsSeleccionados.assertValueAt(2, listOf(item1, item2, item3))
            }

            @Test
            fun emite_lista_con_todos_los_items_seleccionados_al_llamar_seleccionarTodos_con_varios_items_deseleccionados()
            {
                modelo.deseleccionarItem(item1)
                modelo.deseleccionarItem(item3)
                modelo.seleccionarTodos()
                testItemsSeleccionados.assertValueCount(4)
                testItemsSeleccionados.assertValueAt(3, listOf(item1, item2, item3))
            }

            @Test
            fun no_emite_nuevos_valores_al_seleccionar_item_inexistente()
            {
                modelo.seleccionarItem(itemInexistente)
                testItemsSeleccionados.assertValueCount(1)
            }

            @Test
            fun no_emite_nuevos_valores_al_deseleccionar_item_inexistente()
            {
                modelo.deseleccionarItem(itemInexistente)
                testItemsSeleccionados.assertValueCount(1)
            }

            @Test
            fun emite_lista_con_item_deseleccionado_al_deseleccionar_directamente_item_existente_y_seleccionado()
            {
                modelo.itemsFiltrables[1].deseleccionar()
                testItemsSeleccionados.assertValueCount(2)
                testItemsSeleccionados.assertValueAt(1, listOf(item1, item3))
            }

            @Test
            fun emite_lista_solo_con_item_seleccionado_al_seleccionar_directamente_item_existente_y_deseleccionado_con_otros_items_deseleccionados()
            {
                modelo.deseleccionarTodos()
                modelo.itemsFiltrables[1].seleccionar()
                testItemsSeleccionados.assertValueCount(3)
                testItemsSeleccionados.assertValueAt(2, listOf(item2))
            }

            @Test
            fun no_emite_lista_al_deseleccionar_directamente_item_existente_seleccionado_pero_deshabilitado()
            {
                modelo.itemsFiltrables[1].habilitar(false)
                modelo.itemsFiltrables[1].deseleccionar()
                testItemsSeleccionados.assertValue(listOf(item1, item2, item3))
            }

            @Test
            fun emite_lista_vacia_al_seleccionar_directamente_item_existente_deseleccionado_pero_deshabilitado_con_otros_items_deseleccionados()
            {
                modelo.deseleccionarTodos()
                modelo.itemsFiltrables[1].habilitar(false)
                modelo.itemsFiltrables[1].seleccionar()
                testItemsSeleccionados.assertValuesOnly(listOf(item1, item2, item3), listOf())
            }
        }

        @Nested
        inner class TodosLosHabilitadosEstanSeleccionados
        {
            private val testTodosSeleccionados = modelo.todosLosHabilitadosEstanSeleccionados.test()

            @Test
            fun emite_el_tamaño_de_la_lista_al_inicializar()
            {
                testTodosSeleccionados.assertValueCount(1)
                testTodosSeleccionados.assertValue(true)
            }

            @Test
            fun emite_false_al_llamar_deseleccionarTodos()
            {
                modelo.deseleccionarTodos()
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.assertValueAt(1, false)
            }

            @Test
            fun emite_false_al_deseleccionar_item_existente_y_seleccionado()
            {
                modelo.deseleccionarItem(item2)
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.assertValueAt(1, false)
            }

            @Test
            fun emite_false_al_seleccionar_item_existente_y_deseleccionado_con_otros_items_deseleccionados()
            {
                modelo.deseleccionarTodos()
                modelo.seleccionarItem(item2)
                testTodosSeleccionados.assertValueCount(3)
                testTodosSeleccionados.assertValueAt(2, false)
            }

            @Test
            fun emite_true_al_seleccionar_item_existente_y_deseleccionado_con_otros_items_seleccionados()
            {
                modelo.deseleccionarItem(item2)
                modelo.seleccionarItem(item2)
                testTodosSeleccionados.assertValueCount(3)
                testTodosSeleccionados.assertValueAt(2, true)
            }

            @Test
            fun emite_true_al_llamar_seleccionarTodos_despues_de_deseleccionar_todos()
            {
                modelo.deseleccionarTodos()
                modelo.seleccionarTodos()
                testTodosSeleccionados.assertValueCount(3)
                testTodosSeleccionados.assertValueAt(2, true)
            }

            @Test
            fun emite_true_al_llamar_seleccionarTodos_con_varios_items_deseleccionados()
            {
                modelo.deseleccionarItem(item1)
                modelo.deseleccionarItem(item3)
                modelo.seleccionarTodos()
                testTodosSeleccionados.assertValueCount(4)
                testTodosSeleccionados.assertValueAt(3, true)
            }

            @Test
            fun no_emite_nuevos_valores_al_seleccionar_item_inexistente()
            {
                modelo.seleccionarItem(itemInexistente)
                testTodosSeleccionados.assertValueCount(1)
            }

            @Test
            fun no_emite_nuevos_valores_al_deseleccionar_item_inexistente()
            {
                modelo.deseleccionarItem(itemInexistente)
                testTodosSeleccionados.assertValueCount(1)
            }

            @Test
            fun emite_false_al_deseleccionar_directamente_item_existente_y_seleccionado()
            {
                modelo.itemsFiltrables[1].deseleccionar()
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.assertValueAt(1, false)
            }

            @Test
            fun emite_true_al_seleccionar_directamente_item_existente_y_deseleccionado_con_otros_items_seleccionados()
            {
                modelo.deseleccionarItem(item2)
                modelo.itemsFiltrables[1].seleccionar()
                testTodosSeleccionados.assertValueCount(3)
                testTodosSeleccionados.assertValueAt(2, true)
            }

            @Test
            fun emite_true_si_todos_los_items_habilitados_estan_seleccionados()
            {
                fun forzarEmision()
                {
                    modelo.itemsFiltrables[0].deseleccionar()
                    modelo.itemsFiltrables[0].seleccionar()
                }

                modelo.itemsFiltrables[1].deseleccionar()
                modelo.itemsFiltrables[1].habilitar(false)

                forzarEmision()

                testTodosSeleccionados.assertValuesOnly(true, false, false, true)
            }
        }

        @Nested
        inner class NumeroHabilitadosSeleccionados
        {
            private val testTodosSeleccionados = modelo.numeroHabilitadosSeleccionados.test()

            @Test
            fun emite_el_tamaño_de_la_lista_al_inicializar()
            {
                testTodosSeleccionados.assertValueCount(1)
                testTodosSeleccionados.assertValue(listaItems.size)
            }

            @Test
            fun emite_0_al_llamar_deseleccionarTodos()
            {
                modelo.deseleccionarTodos()
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.verificarUltimoValorEmitido(0)
            }

            @Test
            fun emite_tamaño_de_la_lista_menos_uno_al_deseleccionar_item_existente_y_seleccionado()
            {
                modelo.deseleccionarItem(item2)
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.verificarUltimoValorEmitido(listaItems.size - 1)
            }

            @Test
            fun emite_1_al_seleccionar_item_existente_y_deseleccionado_con_otros_items_deseleccionados()
            {
                modelo.deseleccionarTodos()
                modelo.seleccionarItem(item2)
                testTodosSeleccionados.assertValueCount(3)
                testTodosSeleccionados.assertValuesOnly(listaItems.size, 0, 1)
            }

            @Test
            fun emite_tamaño_de_la_lista_al_seleccionar_item_existente_y_deseleccionado_con_otros_items_seleccionados()
            {
                modelo.deseleccionarItem(item2)
                modelo.seleccionarItem(item2)
                testTodosSeleccionados.assertValueCount(3)
                testTodosSeleccionados.assertValuesOnly(listaItems.size, listaItems.size - 1, listaItems.size)
            }

            @Test
            fun emite_tamaño_de_la_lista_al_llamar_seleccionarTodos_despues_de_deseleccionar_todos()
            {
                modelo.deseleccionarTodos()
                modelo.seleccionarTodos()
                testTodosSeleccionados.assertValueCount(3)
                testTodosSeleccionados.assertValuesOnly(listaItems.size, 0, listaItems.size)
            }

            @Test
            fun emite_valores_correctos_al_llamar_seleccionarTodos_con_varios_items_deseleccionados()
            {
                modelo.deseleccionarItem(item1)
                modelo.deseleccionarItem(item3)
                modelo.seleccionarTodos()
                testTodosSeleccionados.assertValueCount(4)
                testTodosSeleccionados.assertValuesOnly(
                        listaItems.size,
                        listaItems.size - 1,
                        listaItems.size - 2,
                        listaItems.size
                                                       )
            }

            @Test
            fun no_emite_nuevos_valores_al_seleccionar_item_inexistente()
            {
                modelo.seleccionarItem(itemInexistente)
                testTodosSeleccionados.assertValueCount(1)
            }

            @Test
            fun no_emite_nuevos_valores_al_deseleccionar_item_inexistente()
            {
                modelo.deseleccionarItem(itemInexistente)
                testTodosSeleccionados.assertValueCount(1)
            }

            @Test
            fun emite_valores_correctos_al_deseleccionar_directamente_item_existente_y_seleccionado()
            {
                modelo.itemsFiltrables[1].deseleccionar()
                testTodosSeleccionados.assertValueCount(2)
                testTodosSeleccionados.assertValuesOnly(listaItems.size, listaItems.size - 1)
            }

            @Test
            fun emite_tamaño_de_lista_al_seleccionar_directamente_item_existente_y_deseleccionado_con_otros_items_seleccionados()
            {
                modelo.deseleccionarItem(item2)
                modelo.itemsFiltrables[1].seleccionar()
                testTodosSeleccionados.assertValueCount(3)
                testTodosSeleccionados.assertValuesOnly(listaItems.size, listaItems.size - 1, listaItems.size)
            }

            @Test
            fun emite_el_numero_de_items_habiiltados_seleccionados()
            {
                fun forzarEmision()
                {
                    modelo.itemsFiltrables[0].deseleccionar()
                    modelo.itemsFiltrables[0].seleccionar()
                }

                modelo.itemsFiltrables[1].habilitar(false)

                forzarEmision()

                testTodosSeleccionados.verificarUltimoValorEmitido(listaItems.size - 1)
            }
        }

        @Nested
        inner class ItemsFiltrables
        {

            @Test
            fun se_inicializa_con_items_correctos()
            {
                assertEquals(listOf(item1, item2, item3), modelo.itemsFiltrables.map(ItemFiltrableUI<Int>::item))
            }

            @Test
            fun se_inicializa_con_todos_los_items_seleccionados()
            {
                assertTrue(modelo.itemsFiltrables.all(ItemFiltrableUI<Int>::estaSeleccionado))
            }

            @Nested
            inner class CuandoEstanSeleccionados
            {
                private val testItemsFiltrablesSeleccionado = modelo.itemsFiltrables.map { it.seleccionado.test() }

                @Test
                fun emite_true_al_inicializar()
                {
                    testItemsFiltrablesSeleccionado.forEach {
                        it.assertValueCount(1)
                        it.assertValue(true)
                    }
                }

                @Test
                fun emite_false_al_llamar_deseleccionarTodos()
                {
                    modelo.deseleccionarTodos()
                    testItemsFiltrablesSeleccionado.forEach {
                        it.assertValueCount(2)
                        it.assertValueAt(1, false)
                    }
                }

                @Test
                fun emite_false_solo_en_observable_correcto_al_deseleccionar_item_existente_y_seleccionado()
                {
                    modelo.deseleccionarItem(item2)
                    testItemsFiltrablesSeleccionado.forEachIndexed { index, testObserver ->
                        if (index == 1)
                        {
                            testObserver.assertValueCount(2)
                            testObserver.assertValueAt(1, false)
                        }
                        else
                        {
                            testObserver.assertValueCount(1)
                        }
                    }
                }

                @Test
                fun emite_true_solo_en_observable_correcto_al_seleccionar_item_existente_y_deseleccionado_con_otros_items_deseleccionados()
                {
                    modelo.deseleccionarTodos()
                    modelo.seleccionarItem(item2)
                    testItemsFiltrablesSeleccionado.forEachIndexed { index, testObserver ->
                        if (index == 1)
                        {
                            testObserver.assertValueCount(3)
                            testObserver.assertValueAt(2, true)
                        }
                        else
                        {
                            testObserver.assertValueCount(2)
                        }
                    }
                }

                @Test
                fun emite_true_en_todos_los_items_al_llamar_seleccionarTodos_despues_de_deseleccionar_todos()
                {
                    modelo.deseleccionarTodos()
                    modelo.seleccionarTodos()
                    testItemsFiltrablesSeleccionado.forEach {
                        it.assertValueCount(3)
                        it.assertValueAt(2, true)
                    }
                }

                @Test
                fun no_emite_nuevos_valores_al_seleccionar_item_inexistente()
                {
                    modelo.seleccionarItem(itemInexistente)
                    testItemsFiltrablesSeleccionado.forEach {
                        it.assertValueCount(1)
                    }
                }

                @Test
                fun no_emite_nuevos_valores_al_deseleccionar_item_inexistente()
                {
                    modelo.deseleccionarItem(itemInexistente)
                    testItemsFiltrablesSeleccionado.forEach {
                        it.assertValueCount(1)
                    }
                }

                @Test
                fun no_emite_nuevos_valores_al_seleccionar_item_ya_seleccionado()
                {
                    modelo.seleccionarTodos()
                    testItemsFiltrablesSeleccionado.forEach {
                        it.assertValueCount(1)
                    }
                }

                @Test
                fun no_emite_nuevos_valores_al_deseleccionar_item_ya_deseleccionado()
                {
                    modelo.deseleccionarTodos()
                    modelo.deseleccionarTodos()
                    testItemsFiltrablesSeleccionado.forEach {
                        it.assertValueCount(2)
                    }
                }

                @Test
                fun emite_false_solo_en_observable_correcto_al_deseleccionar_directamente_item_existente_y_seleccionado()
                {
                    modelo.itemsFiltrables[1].deseleccionar()
                    testItemsFiltrablesSeleccionado.forEachIndexed { index, testObserver ->
                        if (index == 1)
                        {
                            testObserver.assertValueCount(2)
                            testObserver.assertValueAt(1, false)
                        }
                        else
                        {
                            testObserver.assertValueCount(1)
                        }
                    }
                }

                @Test
                fun emite_true_solo_en_observable_correcto_al_seleccionar_directamente_item_existente_y_deseleccionado_con_otros_items_deseleccionados()
                {
                    modelo.deseleccionarTodos()
                    modelo.itemsFiltrables[1].seleccionar()
                    testItemsFiltrablesSeleccionado.forEachIndexed { index, testObserver ->
                        if (index == 1)
                        {
                            testObserver.assertValueCount(3)
                            testObserver.assertValueAt(2, true)
                        }
                        else
                        {
                            testObserver.assertValueCount(2)
                        }
                    }
                }
            }

            @Nested
            inner class Habilitado
            {
                private val observablesEnPrueba = modelo.itemsFiltrables.map { it.habilitado.test() }

                @Test
                fun emite_true_al_inicializar()
                {
                    observablesEnPrueba.forEach {
                        it.assertValuesOnly(true)
                    }
                }

                @Test
                fun al_deshabilitar_un_item_se_emite_false()
                {
                    val posicionItemADeshabilitar = 0

                    modelo.itemsFiltrables[posicionItemADeshabilitar].habilitar(false)

                    observablesEnPrueba.forEachIndexed { i, testObserver ->
                        if (i == posicionItemADeshabilitar)
                        {
                            testObserver.assertValuesOnly(true, false)
                        }
                        else
                        {
                            testObserver.assertValuesOnly(true)
                        }
                    }
                }

                @Test
                fun al_rehabilitar_un_item_se_emite_true()
                {
                    val posicionItemADeshabilitar = 0

                    modelo.itemsFiltrables[posicionItemADeshabilitar].habilitar(false)
                    modelo.itemsFiltrables[posicionItemADeshabilitar].habilitar(true)

                    observablesEnPrueba.forEachIndexed { i, testObserver ->
                        if (i == posicionItemADeshabilitar)
                        {
                            testObserver.assertValuesOnly(true, false, true)
                        }
                        else
                        {
                            testObserver.assertValuesOnly(true)
                        }
                    }
                }
            }
        }

        @Test
        fun es_igual_a_otra_si_todas_las_propiedades_no_observables_son_iguales()
        {
            val primeraLista = ListaFiltrableUIConSujetos(listOf(0, 1, 2))
            val segundaLista = ListaFiltrableUIConSujetos(listOf(0, 1, 2))

            assertEquals(primeraLista, segundaLista)
        }
    }
}