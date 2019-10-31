package co.smartobjects.ui.modelos

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


@DisplayName("ListaNotificadoraCambios")
internal class ListaNotificadoraCambiosPruebas
{
    @Nested
    inner class Buscar
    {
        @Test
        fun si_el_item_existe_lo_retorna_de_lo_contrario_retorna_null()
        {
            val listaObservable = ListaNotificadoraCambios<Int>()

            listaObservable.agregarAlInicio(0)
            assertEquals(0, listaObservable.buscar { it == 0 })

            listaObservable.agregarAlInicio(10)
            assertEquals(10, listaObservable.buscar { it == 10 })

            assertNull(listaObservable.buscar { it == 456789 })
        }
    }

    private data class ItemParaCada(var valor: Int)
    @Nested
    inner class ParaCada
    {
        @Test
        fun si_el_item_existe_lo_retorna_de_lo_contrario_retorna_null()
        {
            val listaObservable = ListaNotificadoraCambios<ItemParaCada>()

            listaObservable.agregarAlInicio(ItemParaCada(5))
            listaObservable.agregarAlInicio(ItemParaCada(13))

            listaObservable.paraCada { it.valor++ }

            assertNotNull(listaObservable.buscar { it.valor == 6 })
            assertNotNull(listaObservable.buscar { it.valor == 14 })
        }
    }

    @Nested
    inner class Items
    {
        @Test
        fun si_el_item_existe_lo_retorna_de_lo_contrario_retorna_null()
        {
            val listaObservable = ListaNotificadoraCambios<Int>()

            listaObservable.agregarAlInicio(5)
            listaObservable.agregarAlInicio(13)
            listaObservable.agregarAlInicio(17)

            assertEquals(listOf(17, 13, 5), listaObservable.snapshotItems)
        }
    }

    @Nested
    inner class AgregarAlInicio
    {
        @Test
        fun notifica_el_nuevo_estado_de_la_lista()
        {
            val listaObservable = ListaNotificadoraCambios<Int>()
            val observable = listaObservable.observar().test()

            listaObservable.agregarAlInicio(0)
            observable.verificarUltimoValorEmitido(listOf(0))

            listaObservable.agregarAlInicio(1)
            observable.verificarUltimoValorEmitido(listOf(1, 0))

            listaObservable.agregarAlInicio(2)
            observable.verificarUltimoValorEmitido(listOf(2, 1, 0))
        }
    }

    private data class ItemRemoverPorHashcode(val a: Int, val b: String)
    @Nested
    inner class RemoverPorHashcode
    {
        private val listaObservable = ListaNotificadoraCambios<ItemRemoverPorHashcode>()

        @BeforeEach
        fun agregarItems()
        {
            listaObservable.agregarAlInicio(ItemRemoverPorHashcode(0, "0"))
            listaObservable.agregarAlInicio(ItemRemoverPorHashcode(1, "1"))
            listaObservable.agregarAlInicio(ItemRemoverPorHashcode(2, "2"))
        }

        @Test
        fun funciona_correctamente()
        {
            val listaEsperada = listOf(ItemRemoverPorHashcode(2, "2"), ItemRemoverPorHashcode(0, "0"))

            listaObservable.removerPorHashcode(ItemRemoverPorHashcode(1, "1").hashCode())

            assertEquals(listaEsperada, listaObservable.snapshotItems)
        }

        @Test
        fun notifica_el_nuevo_estado_de_la_lista()
        {
            val observable = listaObservable.observar().test()

            val listaEsperada = listOf(ItemRemoverPorHashcode(2, "2"), ItemRemoverPorHashcode(0, "0"))

            listaObservable.removerPorHashcode(ItemRemoverPorHashcode(1, "1").hashCode())

            observable.verificarUltimoValorEmitido(listaEsperada)
        }
    }

    @Nested
    inner class Vaciar
    {
        private val listaObservable = ListaNotificadoraCambios<ItemRemoverPorHashcode>()

        @BeforeEach
        fun agregarItems()
        {
            listaObservable.agregarAlInicio(ItemRemoverPorHashcode(0, "0"))
            listaObservable.agregarAlInicio(ItemRemoverPorHashcode(1, "1"))
            listaObservable.agregarAlInicio(ItemRemoverPorHashcode(2, "2"))
        }

        @Test
        fun funciona_correctamente()
        {
            listaObservable.limpiar()

            assertTrue(listaObservable.snapshotItems.isEmpty())
        }

        @Test
        fun notifica_el_nuevo_estado_de_la_lista()
        {
            val observable = listaObservable.observar().test()

            listaObservable.limpiar()

            observable.verificarUltimoValorEmitido(listOf())
        }
    }

    @Nested
    inner class OperacionesConcurrentes
    {
        @Test
        fun permite_limpiar_mientras_se_itera_sobre_la_lista()
        {
            val listaObservable = ListaNotificadoraCambios<ItemParaCada>()

            listaObservable.agregarAlInicio(ItemParaCada(5))
            listaObservable.agregarAlInicio(ItemParaCada(13))

            listaObservable.paraCada {
                if (it.valor == 5)
                {
                    listaObservable.limpiar()
                }
                it.valor++
            }

            assertTrue(listaObservable.snapshotItems.isEmpty())
        }

        @Test
        fun permite_borrar_mientras_se_itera_sobre_la_lista()
        {
            val listaObservable = ListaNotificadoraCambios<ItemParaCada>()

            val itemaABorrar = ItemParaCada(5)
            val otroItem = ItemParaCada(13)
            listaObservable.agregarAlInicio(itemaABorrar)
            listaObservable.agregarAlInicio(otroItem)

            listaObservable.paraCada {
                if (it.valor == 5)
                {
                    listaObservable.removerPorHashcode(itemaABorrar.hashCode())
                }
                it.valor++
            }

            assertEquals(listOf(otroItem), listaObservable.snapshotItems)
        }

        @Test
        fun permite_agregar_mientras_se_itera_sobre_la_lista()
        {
            val listaObservable = ListaNotificadoraCambios<ItemParaCada>()

            val primerItem = ItemParaCada(5)
            val segundoItem = ItemParaCada(13)
            val itemNuevoAAgregar = ItemParaCada(23)
            listaObservable.agregarAlInicio(primerItem)
            listaObservable.agregarAlInicio(segundoItem)

            listaObservable.paraCada {
                if (it.valor == 5)
                {
                    listaObservable.agregarAlInicio(itemNuevoAAgregar)
                }
                it.valor++
            }

            assertEquals(listOf(itemNuevoAAgregar, segundoItem, primerItem), listaObservable.snapshotItems)
        }
    }

    @Test
    fun dos_listas_son_iguales_si_tienen_los_mismos_elementos_en_el_mismo_orden()
    {
        val primeraLista = ListaNotificadoraCambios<Int>().apply {
            agregarAlInicio(0)
            agregarAlInicio(1)
            agregarAlInicio(2)
        }

        val segundaLista = ListaNotificadoraCambios<Int>().apply {
            agregarAlInicio(0)
            agregarAlInicio(1)
            agregarAlInicio(2)
        }

        assertEquals(primeraLista, segundaLista)
    }
}