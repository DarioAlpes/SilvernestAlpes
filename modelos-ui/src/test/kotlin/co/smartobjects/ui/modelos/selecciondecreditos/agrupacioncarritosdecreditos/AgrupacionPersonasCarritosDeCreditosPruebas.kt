package co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.logica.fondos.CalculadorPuedeAgregarseSegunUnicidad
import co.smartobjects.logica.fondos.libros.ProveedorDePreciosCompletosYProhibiciones
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditosUI
import co.smartobjects.ui.modelos.carritocreditos.ItemCreditoUI
import co.smartobjects.ui.modelos.catalogo.Producto
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoFondo
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoPaquete
import co.smartobjects.utilidades.Decimal
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class AgrupacionPersonasCarritosDeCreditosPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_UBICACION = 1L
        private const val ID_FONDO_PROHIBIDO = 562319L
        private const val ID_PAQUETE_PROHIBIDO = 198237L
    }

    private val valoresIniciales = listOf(Decimal(2), Decimal(3), Decimal(5), Decimal(7), Decimal(11))
    private val sujetosParaTotalSinImpuesto = valoresIniciales.map { BehaviorSubject.createDefault(it) }
    private val sujetosParaImpuestoTotal = valoresIniciales.map { BehaviorSubject.createDefault(it) }
    private val sujetosParaSaldo = valoresIniciales.map { BehaviorSubject.createDefault(it) }
    private val sujetosBooleanos = List(valoresIniciales.size) { BehaviorSubject.createDefault(false) }
    private val sujetosCreditosAPagar = List(valoresIniciales.size) { PublishSubject.create<CarritoDeCreditosUI.CreditosAProcesar>() }
    private val sujetosIdsTotalesFondosEnCarrito = List(valoresIniciales.size) { BehaviorSubject.createDefault<Set<Long>>(setOf()) }
    private val sujetosIdsFondosEnCarrito = List(valoresIniciales.size) { BehaviorSubject.createDefault<Set<Long>>(setOf()) }
    private val sujetosIdsPaquetesEnCarrito = List(valoresIniciales.size) { BehaviorSubject.createDefault<Set<Long>>(setOf()) }
    private val sujetosCreditosTotales = List(valoresIniciales.size) { BehaviorSubject.createDefault<List<ItemCreditoUI>>(listOf()) }

    private val personasConCarritos =
            List(valoresIniciales.size) { i ->
                val sujetoBooleano = sujetosBooleanos[i]
                val sujetoCreditosAPagar = sujetosCreditosAPagar[i]

                PersonaConCarrito(
                        mockConDefaultAnswer(Persona::class.java).also {
                            doReturn("Persona $i").`when`(it).nombreCompleto
                            doReturn("Persona $i").`when`(it).toString()
                        },
                        GrupoClientes(
                                i.toLong(),
                                "Grupo de persona $i",
                                listOf(SegmentoClientes(null, SegmentoClientes.NombreCampo.CATEGORIA, "A"))
                                     ),
                        mockConDefaultAnswer(CarritoDeCreditosUI::class.java).also {
                            doReturn(sujetosParaTotalSinImpuesto[i]).`when`(it).totalSinImpuesto
                            doReturn(sujetosParaImpuestoTotal[i]).`when`(it).impuestoTotal
                            doReturn(valoresIniciales[i]).`when`(it).valorYaPagado
                            doReturn(sujetosParaSaldo[i]).`when`(it).saldo
                            doReturn(sujetoBooleano).`when`(it).hayCreditosSinConfirmar
                            doReturn(sujetosIdsTotalesFondosEnCarrito[i]).`when`(it).idsTotalesDeFondos
                            doReturn(sujetosIdsFondosEnCarrito[i]).`when`(it).idsDiferentesDeFondos
                            doReturn(sujetosIdsPaquetesEnCarrito[i]).`when`(it).idsDiferentesDePaquetes
                            doReturn(sujetoCreditosAPagar.firstOrError()).`when`(it).creditosAProcesar

                            doReturn(sujetosCreditosTotales[i]).`when`(it).creditosTotales.also {
                                val itemCredito = mockConDefaultAnswer(ItemCreditoUI::class.java).also {
                                    doReturn(true).`when`(it).estaPagado
                                }
                                sujetosCreditosTotales[i].onNext(listOf(itemCredito))
                            }

                            doNothing().`when`(it).confirmarCreditosAgregados()
                            doNothing().`when`(it).cancelarCreditosAgregados()
                            doNothing().`when`(it).pagar()
                            doNothing().`when`(it).finalizarProceso()
                        }
                                 )
            }

    private val listaFiltrable = ListaFiltrableUIConSujetos(personasConCarritos)

    private val mockDePrecioCompletoEncontrado =
            mockConDefaultAnswer(PrecioCompleto::class.java).also {
                doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
            }

    private val indicesPersonasParaProductosProhibidos = setOf(0, 2)
    private val mockProveedorDePreciosCompletosYProhibiciones =
            mockConDefaultAnswer(ProveedorDePreciosCompletosYProhibiciones::class.java).also {
                // Para un fondo solo
                doReturn(listOf(mockDePrecioCompletoEncontrado))
                    .`when`(it)
                    .darPreciosCompletosDeFondos(cualquiera(), cualquiera(), cualquiera(), isNull())

                doReturn(true)
                    .`when`(it)
                    .verificarSiFondoEsVendible(anyLong(), cualquiera(), cualquiera(), cualquiera())

                // Prohibiciones: Personas 1 y 3
                indicesPersonasParaProductosProhibidos.forEach { indiceProhibido ->
                    doReturn(false)
                        .`when`(it)
                        .verificarSiFondoEsVendible(
                                eq(ID_FONDO_PROHIBIDO),
                                cualquiera(),
                                eq(personasConCarritos[indiceProhibido].grupoDeClientes!!.id!!),
                                cualquiera()
                                                   )
                }

                // Para el fondo de un paquete
                doReturn(listOf(mockDePrecioCompletoEncontrado))
                    .`when`(it)
                    .darPreciosCompletosDeFondos(cualquiera(), cualquiera(), cualquiera(), cualquiera())

                doReturn(true)
                    .`when`(it)
                    .verificarSiPaqueteEsVendible(anyLong(), cualquiera(), cualquiera(), cualquiera(), cualquiera())

                // Prohibiciones: Personas 1 y 3
                indicesPersonasParaProductosProhibidos.forEach { indiceProhibido ->
                    doReturn(false)
                        .`when`(it)
                        .verificarSiPaqueteEsVendible(
                                eq(ID_PAQUETE_PROHIBIDO),
                                cualquiera(),
                                eq(personasConCarritos[indiceProhibido].grupoDeClientes!!.id!!),
                                cualquiera(),
                                cualquiera()
                                                     )
                }
            }

    private val mockCalculadorPuedeAgregarseSegunUnicidad =
            mockConDefaultAnswer(CalculadorPuedeAgregarseSegunUnicidad::class.java).also {
                doReturn(true).`when`(it).puedeAgregarFondos(cualquiera(), cualquiera())
            }

    private val modelo =
            AgrupacionPersonasCarritosDeCreditos(
                    listaFiltrable,
                    ID_UBICACION,
                    Single.just(mockProveedorDePreciosCompletosYProhibiciones),
                    Single.just(mockCalculadorPuedeAgregarseSegunUnicidad)
                                                )

    private val modeloSinItemsAnteriores =
            AgrupacionPersonasCarritosDeCreditos(
                    ListaFiltrableUIConSujetos(listOf()),
                    ID_UBICACION,
                    Single.just(mockProveedorDePreciosCompletosYProhibiciones),
                    Single.just(mockCalculadorPuedeAgregarseSegunUnicidad)
                                                )

    private fun darMockProveedorImagenesProducto(idProducto: Long, esPaquete: Boolean) =
            mockConDefaultAnswer(ProveedorImagenesProductos::class.java).also {
                doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                    .`when`(it)
                    .darImagen(eq(idProducto), eq(esPaquete))
            }


    private fun generarNuevaListaDePersonas(): Pair<List<Decimal>, List<PersonaConCarrito>>
    {
        val nuevosValoresIniciales = listOf(Decimal(13), Decimal(17), Decimal(19), Decimal(23), Decimal(29))
        val nuevosValoresParaTotalSinImpuesto = nuevosValoresIniciales.map { BehaviorSubject.createDefault(it) }
        val nuevosValoresParaImpuestoTotal = nuevosValoresIniciales.map { BehaviorSubject.createDefault(it) }
        val nuevosValoresParaSaldo = nuevosValoresIniciales.map { BehaviorSubject.createDefault(it) }
        val nuevosValoresBooleanos = List(nuevosValoresIniciales.size) { BehaviorSubject.createDefault(false) }
        val nuevosValoresCreditosAPagar = List(nuevosValoresIniciales.size) { PublishSubject.create<List<ItemCreditoUI>>() }
        val nuevosValoresIdsTotalesFondosEnCarrito = List(nuevosValoresIniciales.size) { BehaviorSubject.createDefault<Set<Long>>(setOf()) }
        val nuevosValoresIdsFondosEnCarrito = List(nuevosValoresIniciales.size) { BehaviorSubject.createDefault<Set<Long>>(setOf()) }
        val nuevosValoresIdsPaquetesEnCarrito = List(nuevosValoresIniciales.size) { BehaviorSubject.createDefault<Set<Long>>(setOf()) }

        val nuevosItems =
                List(nuevosValoresIniciales.size) { i ->
                    val sujetoBooleano = nuevosValoresBooleanos[i]
                    val sujetoCreditosAPagar = nuevosValoresCreditosAPagar[i]

                    PersonaConCarrito(
                            mockConDefaultAnswer(Persona::class.java).also {
                                doReturn("Persona Nueva $i").`when`(it).nombreCompleto
                                doReturn("Persona Nueva $i").`when`(it).toString()
                            },
                            GrupoClientes(
                                    i.toLong(),
                                    "Grupo de persona Nuevo $i",
                                    listOf(SegmentoClientes(null, SegmentoClientes.NombreCampo.CATEGORIA, "A"))
                                         ),
                            mockConDefaultAnswer(CarritoDeCreditosUI::class.java).also {
                                doReturn(nuevosValoresParaTotalSinImpuesto[i]).`when`(it).totalSinImpuesto
                                doReturn(nuevosValoresParaImpuestoTotal[i]).`when`(it).impuestoTotal
                                doReturn(nuevosValoresIniciales[i]).`when`(it).valorYaPagado
                                doReturn(nuevosValoresParaSaldo[i]).`when`(it).saldo
                                doReturn(sujetoBooleano).`when`(it).hayCreditosSinConfirmar
                                doReturn(nuevosValoresIdsTotalesFondosEnCarrito[i]).`when`(it).idsTotalesDeFondos
                                doReturn(nuevosValoresIdsFondosEnCarrito[i]).`when`(it).idsDiferentesDeFondos
                                doReturn(nuevosValoresIdsPaquetesEnCarrito[i]).`when`(it).idsDiferentesDePaquetes
                                doReturn(sujetoCreditosAPagar.firstOrError()).`when`(it).creditosAProcesar

                                val itemCredito = mockConDefaultAnswer(ItemCreditoUI::class.java).also {
                                    doReturn(true).`when`(it).estaPagado
                                }
                                doReturn(Observable.just(listOf(itemCredito))).`when`(it).creditosTotales

                                doNothing().`when`(it).confirmarCreditosAgregados()
                                doNothing().`when`(it).cancelarCreditosAgregados()
                                doAnswer { sujetoCreditosAPagar.onNext(listOf()) }.`when`(it).pagar()
                                doNothing().`when`(it).finalizarProceso()
                            }
                                     )
                }

        return Pair(nuevosValoresIniciales, nuevosItems)
    }

    private fun generarNuevaListaFiltrableDePersonas(): Pair<List<Decimal>, ListaFiltrableUIConSujetos<PersonaConCarrito>>
    {
        val listado = generarNuevaListaDePersonas()

        return Pair(listado.first, ListaFiltrableUIConSujetos(listado.second))
    }

    private fun mockearCreditosTotales(listadoPersonas: List<PersonaConCarrito>, valores: List<List<ItemCreditoUI>>)
    {
        assert(valores.size == listadoPersonas.size)
        for ((i, personaConCarrito) in listadoPersonas.withIndex())
        {
            doReturn(Observable.just(valores[i]))
                .`when`(personaConCarrito.carritoDeCreditos)
                .creditosTotales
        }
    }


    @Test
    fun cuando_se_emite_la_lista_filtrable_los_modelos_hijos_son_todos_los_carritos_de_creditos_y_lista_filtrable()
    {
        modelo.totalSinImpuesto.test()

        assertEquals(
                personasConCarritos.map { it.carritoDeCreditos } + listaFiltrable,
                modelo.modelosHijos
                    )
    }

    @Nested
    inner class ActualizarItems
    {
        @Test
        fun puede_pasar_de_no_tener_personas_a_tenerlas()
        {
            val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
            val nuevaLista = datosDePruebaNuevos.second

            mockearCreditosTotales(
                    datosDePruebaNuevos.second.items,
                    datosDePruebaNuevos.second.items.map { listOf(mockConDefaultAnswer(ItemCreditoUI::class.java)) })

            val observableDePrueba = modeloSinItemsAnteriores.personasConCarritos.test()

            observableDePrueba.verificarUltimoValorEmitido(ListaFiltrableUIConSujetos(listOf()))

            modeloSinItemsAnteriores.actualizarItems(nuevaLista)

            observableDePrueba.verificarUltimoValorEmitido(nuevaLista)
        }

        @Test
        fun cambia_los_items()
        {
            val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
            val nuevaLista = datosDePruebaNuevos.second

            assertNotEquals(nuevaLista, listaFiltrable)

            val observableDePrueba = modelo.personasConCarritos.test()

            observableDePrueba.verificarUltimoValorEmitido(listaFiltrable)

            modelo.actualizarItems(nuevaLista)

            observableDePrueba.verificarUltimoValorEmitido(nuevaLista)
        }
    }

    @Nested
    inner class TotalSinImpuesto
    {
        private val observableDePrueba = modelo.totalSinImpuesto.test()


        @Test
        fun emite_valor_correcto_al_suscribirse()
        {
            observableDePrueba.assertValuesOnly(valoresIniciales.reduce { acc, decimal -> acc + decimal })
        }

        @Test
        fun al_cambiar_valores_asociados_se_emiten_valores_correctos()
        {
            val indicesACambiar = setOf(0, 2)
            val valoresEsperados = mutableListOf(valoresIniciales.reduce { acc, decimal -> acc + decimal })
            val valoresCambiados = valoresIniciales.mapIndexed { i, valor ->
                if (i in indicesACambiar)
                {
                    valoresEsperados.add(valoresEsperados.last() + 13)
                    valor + 13
                }
                else
                {
                    valor
                }
            }

            sujetosParaTotalSinImpuesto.forEachIndexed { i, sujeto ->
                if (i in indicesACambiar)
                {
                    sujeto.onNext(valoresCambiados[i])
                }
            }

            observableDePrueba.assertValuesOnly(*valoresEsperados.toTypedArray())
        }

        @Test
        fun al_actualizar_items_se_emiten_nuevos_valores_correctos()
        {
            val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
            val valorEsperado = datosDePruebaNuevos.first.reduce { acc, decimal -> acc + decimal }

            modelo.actualizarItems(datosDePruebaNuevos.second)

            observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
        }
    }

    @Nested
    inner class ImpuestoTotal
    {
        private val observableDePrueba = modelo.impuestoTotal.test()


        @Test
        fun emite_valor_correcto_al_suscribirse()
        {
            observableDePrueba.assertValuesOnly(valoresIniciales.reduce { acc, decimal -> acc + decimal })
        }

        @Test
        fun al_cambiar_valores_asociados_se_emiten_valores_correctos()
        {
            val indicesACambiar = setOf(0, 2)
            val valoresEsperados = mutableListOf(valoresIniciales.reduce { acc, decimal -> acc + decimal })
            val valoresCambiados = valoresIniciales.mapIndexed { i, valor ->
                if (i in indicesACambiar)
                {
                    valoresEsperados.add(valoresEsperados.last() + 13)
                    valor + 13
                }
                else
                {
                    valor
                }
            }

            sujetosParaImpuestoTotal.forEachIndexed { i, sujeto ->
                if (i in indicesACambiar)
                {
                    sujeto.onNext(valoresCambiados[i])
                }
            }

            observableDePrueba.assertValuesOnly(*valoresEsperados.toTypedArray())
        }

        @Test
        fun al_actualizar_items_se_emiten_nuevos_valores_correctos()
        {
            val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
            val valorEsperado = datosDePruebaNuevos.first.reduce { acc, decimal -> acc + decimal }

            modelo.actualizarItems(datosDePruebaNuevos.second)

            observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
        }

    }

    @Nested
    inner class ValorYaPagado
    {
        private val observableDePrueba = modelo.valorYaPagado.test()

        @Test
        fun el_valor_ya_pagado_se_calcula_correctamente()
        {
            observableDePrueba.assertValues(valoresIniciales.reduce { acc, decimal -> acc + decimal })
        }

        @Test
        fun al_actualizar_items_se_emiten_nuevos_valores_correctos()
        {
            val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
            val valorEsperado = datosDePruebaNuevos.first.reduce { acc, decimal -> acc + decimal }

            modelo.actualizarItems(datosDePruebaNuevos.second)

            observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
        }
    }

    @Nested
    inner class Saldo
    {
        private val observableDePrueba = modelo.saldo.test()


        @Test
        fun emite_valor_correcto_al_suscribirse()
        {
            observableDePrueba.assertValuesOnly(valoresIniciales.reduce { acc, decimal -> acc + decimal })
        }

        @Test
        fun al_cambiar_valores_asociados_se_emiten_valores_correctos()
        {
            val indicesACambiar = setOf(0, 2)
            val valoresEsperados = mutableListOf(valoresIniciales.reduce { acc, decimal -> acc + decimal })
            val valoresCambiados = valoresIniciales.mapIndexed { i, valor ->
                if (i in indicesACambiar)
                {
                    valoresEsperados.add(valoresEsperados.last() + 13)
                    valor + 13
                }
                else
                {
                    valor
                }
            }

            sujetosParaSaldo.forEachIndexed { i, sujeto ->
                if (i in indicesACambiar)
                {
                    sujeto.onNext(valoresCambiados[i])
                }
            }

            observableDePrueba.assertValuesOnly(*valoresEsperados.toTypedArray())
        }

        @Test
        fun al_actualizar_items_se_emiten_nuevos_valores_correctos()
        {
            val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
            val valorEsperado = datosDePruebaNuevos.first.reduce { acc, decimal -> acc + decimal }

            modelo.actualizarItems(datosDePruebaNuevos.second)

            observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
        }
    }

    @Nested
    inner class PuedePagar
    {
        @Nested
        inner class SinPersonasConCarritos
        {
            private val observador = modeloSinItemsAnteriores.puedePagar.test()

            @Test
            fun no_puede_pagar()
            {
                observador.assertValuesOnly(false)
            }

            @Test
            fun al_actualizar_valores_a_unos_con_creditos_en_todos_los_carritos_puede_pagar()
            {
                val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
                val listaNueva = datosDePruebaNuevos.second

                modeloSinItemsAnteriores.actualizarItems(listaNueva)

                observador.verificarUltimoValorEmitido(true)
            }

            @Test
            fun al_actualizar_valores_a_unos_con_creditos_en_algunos_carritos_no_puede_pagar()
            {
                val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
                val listaNueva = datosDePruebaNuevos.second

                val itemsEnCarritos =
                        listaNueva.items.mapIndexed { index, _ ->
                            if (index % 2 == 0)
                            {
                                listOf(mockConDefaultAnswer(ItemCreditoUI::class.java))
                            }
                            else
                            {
                                listOf()
                            }
                        }

                mockearCreditosTotales(listaNueva.items, itemsEnCarritos)

                modeloSinItemsAnteriores.actualizarItems(listaNueva)

                observador.verificarUltimoValorEmitido(false)
            }

            @Test
            fun si_hay_creditos_sin_confirmar_en_algunos_carritos_no_puede_pagar()
            {
                val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
                val listaNueva = datosDePruebaNuevos.second

                for ((i, personaConCarrito) in listaNueva.items.withIndex())
                {
                    doReturn(Observable.just(i % 2 == 0))
                        .`when`(personaConCarrito.carritoDeCreditos)
                        .hayCreditosSinConfirmar
                }

                modeloSinItemsAnteriores.actualizarItems(listaNueva)

                sujetosBooleanos[1].onNext(true)
                sujetosBooleanos[2].onNext(true)

                observador.verificarUltimoValorEmitido(false)
            }
        }

        @Nested
        inner class ConPersonasConCarritos
        {
            private val observador = modelo.puedePagar.test()

            @Test
            fun con_creditos_en_todos_los_carritos_puede_pagar()
            {
                observador.assertValuesOnly(true)
            }

            @Test
            fun al_actualizar_valores_sin_creditos_en_todos_los_carritos_no_puede_pagar()
            {
                val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
                val listaNueva = datosDePruebaNuevos.second

                mockearCreditosTotales(
                        listaNueva.items,
                        listaNueva.items.map { listOf<ItemCreditoUI>() })

                modelo.actualizarItems(listaNueva)

                observador.verificarUltimoValorEmitido(false)
            }
        }
    }

    @Nested
    inner class TodosLosCreditosEstanPagados
    {
        @Nested
        inner class SinPersonasConCarritos
        {
            private val observador = modeloSinItemsAnteriores.todosLosCreditosEstanPagados.test()

            @Test
            fun emite_false()
            {
                observador.assertValuesOnly(false)
            }

            @Test
            fun al_actualizar_valores_a_unos_con_creditos_en_todos_los_carritos_y_pagados_emite_true()
            {
                val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
                val listaNueva = datosDePruebaNuevos.second

                modeloSinItemsAnteriores.actualizarItems(listaNueva)

                observador.verificarUltimoValorEmitido(true)
            }

            @Test
            fun al_actualizar_valores_a_unos_con_creditos_pagados_en_algunos_carritos_emite_false()
            {
                val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
                val listaNueva = datosDePruebaNuevos.second

                val itemsEnCarritos =
                        listaNueva.items.mapIndexed { index, _ ->
                            listOf(
                                    mockConDefaultAnswer(ItemCreditoUI::class.java).also {
                                        doReturn(index % 2 == 0).`when`(it).estaPagado
                                    }
                                  )
                        }

                mockearCreditosTotales(listaNueva.items, itemsEnCarritos)

                modeloSinItemsAnteriores.actualizarItems(listaNueva)

                observador.verificarUltimoValorEmitido(false)
            }
        }

        @Nested
        inner class ConPersonasConCarritos
        {
            private val observador = modelo.todosLosCreditosEstanPagados.test()

            @Test
            fun con_creditos_pagados_en_todos_los_carritos_emite_true()
            {
                observador.assertValuesOnly(false, true)
            }

            @Test
            fun al_actualizar_valores_sin_creditos_en_todos_los_carritos_emite_false()
            {
                val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()
                val listaNueva = datosDePruebaNuevos.second

                mockearCreditosTotales(
                        listaNueva.items,
                        listaNueva.items.map { listOf<ItemCreditoUI>() })

                modelo.actualizarItems(listaNueva)

                observador.verificarUltimoValorEmitido(false)
            }
        }
    }

    @Nested
    inner class CreditosAPagar
    {
        private val observableDePrueba = modelo.creditosAProcesar.test()

        @Test
        fun no_emite_nada_al_suscribirse()
        {
            observableDePrueba.assertEmpty()
        }

        @Nested
        inner class AlPagar
        {
            @Test
            fun sin_items_no_se_emite_nada()
            {
                modelo.pagar()

                observableDePrueba.assertEmpty()
            }

            @Test
            fun con_items_se_emite_agregacion_correcta()
            {
                val indicesQueTienenCreditosAPagar = setOf(1, 3)
                val valoresEsperados = mutableListOf<PersonaConCreditosSeleccionados>()

                for (i in personasConCarritos.indices)
                {
                    val creditosAPagar =
                            if (i in indicesQueTienenCreditosAPagar)
                            {
                                listOf(mockConDefaultAnswer(ItemCreditoUI::class.java).also {
                                    doReturn("Producto sin pagar $i").`when`(it).nombreProducto
                                    doReturn("Producto sin pagar $i").`when`(it).toString()
                                })
                            }
                            else
                            {
                                listOf()
                            }

                    val creditosPagados =
                            listOf(mockConDefaultAnswer(ItemCreditoUI::class.java).also {
                                doReturn("Producto pagado $i").`when`(it).nombreProducto
                                doReturn("Producto pagado $i").`when`(it).toString()
                            })

                    val creditosAProcesar = CarritoDeCreditosUI.CreditosAProcesar(creditosAPagar, creditosPagados)

                    val resultadoEmitido = PersonaConCreditosSeleccionados(
                            personasConCarritos[i].persona,
                            personasConCarritos[i].grupoDeClientes,
                            creditosAProcesar.sinPagar,
                            creditosAProcesar.pagados
                                                                          )

                    valoresEsperados.add(resultadoEmitido)

                    doAnswer { sujetosCreditosAPagar[i].onNext(creditosAProcesar) }
                        .`when`(personasConCarritos[i].carritoDeCreditos)
                        .pagar()
                }

                modelo.pagar()

                observableDePrueba.assertResult(valoresEsperados)
            }

            @Nested
            inner class AlActualizarItems
            {
                private val nuevosValoresIniciales = listOf(Decimal(13), Decimal(17), Decimal(19), Decimal(23), Decimal(29))
                private val nuevosValoresParaTotalSinImpuesto = nuevosValoresIniciales.map { BehaviorSubject.createDefault(it) }
                private val nuevosValoresParaImpuestoTotal = nuevosValoresIniciales.map { BehaviorSubject.createDefault(it) }
                private val nuevosValoresParaSaldo = nuevosValoresIniciales.map { BehaviorSubject.createDefault(it) }
                private val nuevosValoresBooleanos = List(nuevosValoresIniciales.size) { BehaviorSubject.createDefault(false) }
                private val nuevosValoresCreditosAPagar = List(nuevosValoresIniciales.size) { PublishSubject.create<CarritoDeCreditosUI.CreditosAProcesar>() }
                private val nuevosValoresIdsTotalesFondosEnCarrito = List(nuevosValoresIniciales.size) { BehaviorSubject.createDefault<Set<Long>>(setOf()) }
                private val nuevosValoresIdsFondosEnCarrito = List(nuevosValoresIniciales.size) { BehaviorSubject.createDefault<Set<Long>>(setOf()) }
                private val nuevosValoresIdsPaquetesEnCarrito = List(nuevosValoresIniciales.size) { BehaviorSubject.createDefault<Set<Long>>(setOf()) }

                private val nuevosItems =
                        List(nuevosValoresIniciales.size) { i ->
                            val sujetoBooleano = nuevosValoresBooleanos[i]
                            val sujetoCreditosAPagar = nuevosValoresCreditosAPagar[i]

                            PersonaConCarrito(
                                    mockConDefaultAnswer(Persona::class.java).also {
                                        doReturn("Persona Nueva $i").`when`(it).nombreCompleto
                                        doReturn("Persona Nueva $i").`when`(it).toString()
                                    },
                                    GrupoClientes(
                                            i.toLong(),
                                            "Grupo de persona Nuevo $i",
                                            listOf(SegmentoClientes(null, SegmentoClientes.NombreCampo.CATEGORIA, "A"))
                                                 ),
                                    mockConDefaultAnswer(CarritoDeCreditosUI::class.java).also {
                                        doReturn(nuevosValoresParaTotalSinImpuesto[i]).`when`(it).totalSinImpuesto
                                        doReturn(nuevosValoresParaImpuestoTotal[i]).`when`(it).impuestoTotal
                                        doReturn(nuevosValoresIniciales[i]).`when`(it).valorYaPagado
                                        doReturn(nuevosValoresParaSaldo[i]).`when`(it).saldo
                                        doReturn(sujetoBooleano).`when`(it).hayCreditosSinConfirmar
                                        doReturn(nuevosValoresIdsTotalesFondosEnCarrito[i]).`when`(it).idsTotalesDeFondos
                                        doReturn(nuevosValoresIdsFondosEnCarrito[i]).`when`(it).idsDiferentesDeFondos
                                        doReturn(nuevosValoresIdsPaquetesEnCarrito[i]).`when`(it).idsDiferentesDePaquetes
                                        doReturn(sujetoCreditosAPagar.firstOrError()).`when`(it).creditosAProcesar
                                        doReturn(Observable.just(listOf(mockConDefaultAnswer(ItemCreditoUI::class.java)))).`when`(it).creditosTotales

                                        doNothing().`when`(it).confirmarCreditosAgregados()
                                        doNothing().`when`(it).cancelarCreditosAgregados()
                                        doNothing().`when`(it).pagar()
                                        doNothing().`when`(it).finalizarProceso()
                                    }
                                             )
                        }

                private val nuevaLista = ListaFiltrableUIConSujetos(nuevosItems)


                @Test
                fun sin_items_en_lista_anterior_se_emiten_nuevos_valores_correctos()
                {
                    val observador = modeloSinItemsAnteriores.creditosAProcesar.test()

                    modeloSinItemsAnteriores.actualizarItems(nuevaLista)

                    val indicesQueTienenCreditosAPagar = setOf(1, 3)
                    val valoresEsperados = mutableListOf<PersonaConCreditosSeleccionados>()

                    for (i in personasConCarritos.indices)
                    {
                        val creditosAPagar =
                                if (i in indicesQueTienenCreditosAPagar)
                                {
                                    listOf(mockConDefaultAnswer(ItemCreditoUI::class.java).also {
                                        doReturn("Producto sin pagar $i").`when`(it).nombreProducto
                                        doReturn("Producto sin pagar $i").`when`(it).toString()
                                    })
                                }
                                else
                                {
                                    listOf()
                                }

                        val creditosPagados =
                                listOf(mockConDefaultAnswer(ItemCreditoUI::class.java).also {
                                    doReturn("Producto pagado $i").`when`(it).nombreProducto
                                    doReturn("Producto pagado $i").`when`(it).toString()
                                })

                        val creditosAProcesar = CarritoDeCreditosUI.CreditosAProcesar(creditosAPagar, creditosPagados)

                        val resultadoEmitido = PersonaConCreditosSeleccionados(
                                nuevosItems[i].persona,
                                nuevosItems[i].grupoDeClientes,
                                creditosAProcesar.sinPagar,
                                creditosAProcesar.pagados
                                                                              )

                        valoresEsperados.add(resultadoEmitido)

                        doAnswer { nuevosValoresCreditosAPagar[i].onNext(creditosAProcesar) }
                            .`when`(nuevosItems[i].carritoDeCreditos)
                            .pagar()
                    }

                    modeloSinItemsAnteriores.pagar()

                    observador.assertResult(valoresEsperados)
                }

                @Test
                fun con_items_en_lista_anterior_se_emiten_nuevos_valores_correctos()
                {
                    modelo.actualizarItems(nuevaLista)

                    val indicesQueTienenCreditosAPagar = setOf(1, 3)
                    val valoresEsperados = mutableListOf<PersonaConCreditosSeleccionados>()

                    for (i in personasConCarritos.indices)
                    {
                        val creditosAPagar =
                                if (i in indicesQueTienenCreditosAPagar)
                                {
                                    listOf(mockConDefaultAnswer(ItemCreditoUI::class.java).also {
                                        doReturn("Producto sin pagar $i").`when`(it).nombreProducto
                                        doReturn("Producto sin pagar $i").`when`(it).toString()
                                    })
                                }
                                else
                                {
                                    listOf()
                                }

                        val creditosPagados =
                                listOf(mockConDefaultAnswer(ItemCreditoUI::class.java).also {
                                    doReturn("Producto pagado $i").`when`(it).nombreProducto
                                    doReturn("Producto pagado $i").`when`(it).toString()
                                })

                        val creditosAProcesar = CarritoDeCreditosUI.CreditosAProcesar(creditosAPagar, creditosPagados)

                        val resultadoEmitido = PersonaConCreditosSeleccionados(
                                nuevosItems[i].persona,
                                nuevosItems[i].grupoDeClientes,
                                creditosAProcesar.sinPagar,
                                creditosAProcesar.pagados
                                                                              )

                        valoresEsperados.add(resultadoEmitido)

                        doAnswer { nuevosValoresCreditosAPagar[i].onNext(creditosAProcesar) }
                            .`when`(nuevosItems[i].carritoDeCreditos)
                            .pagar()
                    }

                    modelo.pagar()

                    observableDePrueba.assertResult(valoresEsperados)
                }
            }
        }
    }

    @Nested
    inner class HayCreditosSinConfirmar
    {
        private val observableDePrueba = modelo.hayCreditosSinConfirmar.test()


        @Test
        fun emite_valor_correcto_al_suscribirse()
        {
            observableDePrueba.assertValuesOnly(false)
        }

        @Test
        fun al_cambiar_valores_asociados_se_emiten_valores_correctos_solo_si_cambia_el_valor_agregado()
        {
            sujetosBooleanos[1].onNext(true)
            sujetosBooleanos[2].onNext(true)

            sujetosBooleanos[1].onNext(false)
            sujetosBooleanos[2].onNext(false)

            observableDePrueba.assertValuesOnly(false, true, false)
        }

        @Test
        fun al_actualizar_items_se_emiten_nuevos_valores_correctos()
        {
            val datosDePruebaNuevos = generarNuevaListaFiltrableDePersonas()

            modelo.actualizarItems(datosDePruebaNuevos.second)

            observableDePrueba.assertValuesOnly(false)
        }
    }

    @Nested
    inner class AlAgregarUnProducto
    {
        @Test
        fun funciona_cuando_hay_una_sola_persona()
        {
            val listaNueva = generarNuevaListaDePersonas().second.take(1)
            val carritoDeCreditosAsociado = listaNueva.first().carritoDeCreditos

            modeloSinItemsAnteriores.actualizarItems(ListaFiltrableUIConSujetos(listaNueva))
            doNothing().`when`(carritoDeCreditosAsociado).agregarAlCarrito(cualquiera())

            val mockFondo = mockConDefaultAnswer(Dinero::class.java).also {
                doReturn(1L).`when`(it).id
                doReturn("código externo fondo 1").`when`(it).codigoExterno
                doReturn("Producto Fondo").`when`(it).nombre
            }

            val productoAAgregar =
                    Producto(
                            ProductoFondo.SinCategoria(
                                    mockFondo,
                                    mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                        doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                        doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                    }
                                                      ),
                            darMockProveedorImagenesProducto(1L, false)
                            )

            val productoEsperado =
                    Producto(
                            ProductoFondo.SinCategoria(
                                    mockFondo,
                                    mockDePrecioCompletoEncontrado
                                                      ),
                            darMockProveedorImagenesProducto(1L, false)
                            )


            modeloSinItemsAnteriores.agregarProducto(productoAAgregar)


            verify(carritoDeCreditosAsociado).agregarAlCarrito(productoEsperado)
        }

        @Nested
        inner class SeInvocanMetodosDeVerificacionCorrectosPara
        {
            @Nested
            inner class UnFondo
            {
                private val idFondoAVerificar = 9484001L
                private val mockFondo = mockConDefaultAnswer(Dinero::class.java).also {
                    doReturn(idFondoAVerificar).`when`(it).id
                    doReturn("código externo fondo $idFondoAVerificar").`when`(it).codigoExterno
                    doReturn("Producto Fondo").`when`(it).nombre
                }

                private val productoAAgregar =
                        Producto(
                                ProductoFondo.SinCategoria(
                                        mockFondo,
                                        mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                            doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                            doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                        }
                                                          ),
                                darMockProveedorImagenesProducto(idFondoAVerificar, false)
                                )

                @BeforeEach
                fun mockearLlamadosEnCarritos()
                {
                    for (personaConCarrito in personasConCarritos)
                    {
                        doNothing().`when`(personaConCarrito.carritoDeCreditos).agregarAlCarrito(cualquiera())
                    }
                }

                @Test
                fun con_los_ids_de_fondos_mas_recientes_en_cada_carrito()
                {
                    val nuevosIds = setOf(10082L, 45L, 7544L)
                    for (sujeto in sujetosIdsFondosEnCarrito)
                    {
                        sujeto.onNext(nuevosIds)
                    }

                    modelo.agregarProducto(productoAAgregar)

                    for (personaConCarrito in personasConCarritos)
                    {
                        verify(mockProveedorDePreciosCompletosYProhibiciones)
                            .verificarSiFondoEsVendible(
                                    idFondoAVerificar,
                                    ID_UBICACION,
                                    personaConCarrito.grupoDeClientes?.id,
                                    setOf()
                                                       )
                    }
                }

                @Test
                fun con_los_ids_de_paquetes_mas_recientes_en_cada_carrito()
                {
                    val nuevosIds = setOf(10082L, 45L, 7544L)
                    for (sujeto in sujetosIdsPaquetesEnCarrito)
                    {
                        sujeto.onNext(nuevosIds)
                    }

                    modelo.agregarProducto(productoAAgregar)

                    for (personaConCarrito in personasConCarritos)
                    {
                        verify(mockProveedorDePreciosCompletosYProhibiciones)
                            .verificarSiFondoEsVendible(
                                    idFondoAVerificar,
                                    ID_UBICACION,
                                    personaConCarrito.grupoDeClientes?.id,
                                    nuevosIds
                                                       )
                    }
                }
            }

            @Nested
            inner class UnPaquete
            {
                private val idPaqueteAVerificar = 623453L
                private val paquete = mockConDefaultAnswer(Paquete::class.java).also {
                    doReturn(idPaqueteAVerificar).`when`(it).id
                    doReturn("código externo paquete").`when`(it).codigoExterno
                    doReturn("Producto Paquete Prohibido").`when`(it).nombre
                    doReturn(listOf(Paquete.FondoIncluido(1, "código externo fondo 1", Decimal.UNO))).`when`(it).fondosIncluidos
                }
                private val productoAAgregar =
                        Producto(
                                ProductoPaquete(
                                        paquete,
                                        listOf("Fondo que no importa"),
                                        listOf(mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                            doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                            doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                        })
                                               ),
                                darMockProveedorImagenesProducto(idPaqueteAVerificar, true)
                                )

                @BeforeEach
                fun mockearLlamadosEnCarritos()
                {
                    for (personaConCarrito in personasConCarritos)
                    {
                        doNothing().`when`(personaConCarrito.carritoDeCreditos).agregarAlCarrito(cualquiera())
                    }
                }

                @Test
                fun con_los_ids_de_fondos_mas_recientes_en_cada_carrito()
                {
                    val nuevosIds = setOf(10082L, 45L, 7544L)
                    for (sujeto in sujetosIdsFondosEnCarrito)
                    {
                        sujeto.onNext(nuevosIds)
                    }

                    modelo.agregarProducto(productoAAgregar)

                    for (personaConCarrito in personasConCarritos)
                    {
                        verify(mockProveedorDePreciosCompletosYProhibiciones)
                            .verificarSiPaqueteEsVendible(
                                    idPaqueteAVerificar,
                                    ID_UBICACION,
                                    personaConCarrito.grupoDeClientes?.id,
                                    nuevosIds,
                                    setOf()
                                                         )
                    }
                }

                @Test
                fun con_los_ids_de_paquetes_mas_recientes_en_cada_carrito()
                {
                    val nuevosIds = setOf(10082L, 45L, 7544L)
                    for (sujeto in sujetosIdsPaquetesEnCarrito)
                    {
                        sujeto.onNext(nuevosIds)
                    }

                    modelo.agregarProducto(productoAAgregar)

                    for (personaConCarrito in personasConCarritos)
                    {
                        verify(mockProveedorDePreciosCompletosYProhibiciones)
                            .verificarSiPaqueteEsVendible(
                                    idPaqueteAVerificar,
                                    ID_UBICACION,
                                    personaConCarrito.grupoDeClientes?.id,
                                    setOf(),
                                    nuevosIds
                                                         )
                    }
                }
            }
        }

        @Nested
        inner class NoProhibido
        {
            private val idFondoAAgregar = 71654L
            private val mockFondo = mockConDefaultAnswer(Dinero::class.java).also {
                doReturn(idFondoAAgregar).`when`(it).id
                doReturn("código externo fondo 1").`when`(it).codigoExterno
                doReturn("Producto Fondo").`when`(it).nombre
            }

            private val productoAAgregar =
                    Producto(
                            ProductoFondo.SinCategoria(
                                    mockFondo,
                                    mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                        doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                        doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                    }
                                                      ),
                            darMockProveedorImagenesProducto(idFondoAAgregar, false)
                            )

            @BeforeEach
            fun agregarYConfirmarProducto()
            {
                for (personaConCarrito in personasConCarritos)
                {
                    doNothing().`when`(personaConCarrito.carritoDeCreditos).agregarAlCarrito(cualquiera())
                }

                modelo.agregarProducto(productoAAgregar)
                sujetosIdsTotalesFondosEnCarrito.forEach { it.onNext(productoAAgregar.idsFondosAsociados) }
            }

            @Test
            fun se_agrega_el_producto_a_todas_las_personas()
            {
                val productoEsperado =
                        Producto(
                                ProductoFondo.SinCategoria(
                                        mockFondo,
                                        mockDePrecioCompletoEncontrado
                                                          ),
                                darMockProveedorImagenesProducto(idFondoAAgregar, false)
                                )

                for (personaConCarrito in personasConCarritos)
                {
                    verify(personaConCarrito.carritoDeCreditos).agregarAlCarrito(productoEsperado)
                }
            }

            @Nested
            inner class YCambiarSeleccion
            {
                @Test
                fun deseleccionando_se_cancelan_los_items_agregados()
                {
                    val indicesParaInteractuar = setOf(1, 3)

                    indicesParaInteractuar.forEach {
                        listaFiltrable.deseleccionarItem(personasConCarritos[it])
                    }

                    personasConCarritos.forEachIndexed { i, personaConCarrito ->
                        if (i in indicesParaInteractuar)
                        {
                            verify(personaConCarrito.carritoDeCreditos).cancelarCreditosAgregados()
                        }
                        else
                        {
                            verify(personaConCarrito.carritoDeCreditos, times(0)).cancelarCreditosAgregados()
                        }
                    }
                }

                @Test
                fun seleccionando_se_agregan_los_items()
                {
                    val indicesParaInteractuar = setOf(1, 3)
                    val productoEsperado =
                            Producto(
                                    ProductoFondo.SinCategoria(
                                            mockFondo,
                                            mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                                doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                                doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                            }
                                                              ),
                                    darMockProveedorImagenesProducto(idFondoAAgregar, false)
                                    ).actualizarPreciosAsociados(listOf(mockDePrecioCompletoEncontrado))

                    indicesParaInteractuar.forEach {
                        listaFiltrable.deseleccionarItem(personasConCarritos[it])
                    }

                    indicesParaInteractuar.forEach {
                        listaFiltrable.seleccionarItem(personasConCarritos[it])
                    }

                    personasConCarritos.forEachIndexed { i, personaConCarrito ->
                        if (i in indicesParaInteractuar)
                        {
                            verify(personaConCarrito.carritoDeCreditos, times(2)).agregarAlCarrito(productoEsperado)
                        }
                        else
                        {
                            verify(personaConCarrito.carritoDeCreditos).agregarAlCarrito(productoEsperado)
                        }
                    }
                }

                @Test
                fun multiples_veces_sigue_funcionando_correctamente()
                {
                    val indicesParaInteractuar = setOf(1, 3)
                    val productoEsperado =
                            Producto(
                                    ProductoFondo.SinCategoria(
                                            mockFondo,
                                            mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                                doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                                doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                            }
                                                              ),
                                    darMockProveedorImagenesProducto(idFondoAAgregar, false)
                                    ).actualizarPreciosAsociados(listOf(mockDePrecioCompletoEncontrado))

                    val numeroDeRepeticiones = 5
                    for (repeticion in 1..numeroDeRepeticiones)
                    {
                        indicesParaInteractuar.forEach {
                            listaFiltrable.deseleccionarItem(personasConCarritos[it])
                        }

                        indicesParaInteractuar.forEach {
                            listaFiltrable.seleccionarItem(personasConCarritos[it])
                        }

                        personasConCarritos.forEachIndexed { i, personaConCarrito ->
                            if (i in indicesParaInteractuar)
                            {
                                verify(personaConCarrito.carritoDeCreditos, times(repeticion)).cancelarCreditosAgregados()
                                verify(personaConCarrito.carritoDeCreditos, times(1 + repeticion)).agregarAlCarrito(productoEsperado)
                            }
                            else
                            {
                                verify(personaConCarrito.carritoDeCreditos).agregarAlCarrito(productoEsperado)
                            }
                        }
                    }
                }
            }

            @Nested
            inner class QuePuedeAparecerSoloUnaVez
            {
                @BeforeEach
                fun confirmarCreditoAnteriorYMockearDebeAparecerSoloUnaVez()
                {
                    modelo.confirmarCreditosAgregados()

                    doReturn(false)
                        .`when`(mockCalculadorPuedeAgregarseSegunUnicidad)
                        .puedeAgregarFondos(eqParaKotlin(productoAAgregar.idsFondosAsociados), eqParaKotlin(setOf(idFondoAAgregar)))
                }

                @Test
                fun no_lo_agrega_nuevamente()
                {
                    val productoEsperado =
                            Producto(
                                    ProductoFondo.SinCategoria(
                                            mockFondo,
                                            mockDePrecioCompletoEncontrado
                                                              ),
                                    darMockProveedorImagenesProducto(idFondoAAgregar, false)
                                    )

                    modelo.agregarProducto(productoAAgregar)

                    // Solo se invoca una vez, cuando se agrega antes de cada prueba. Esta última invocación no debería hacer nada
                    for (personaConCarrito in personasConCarritos)
                    {
                        verify(personaConCarrito.carritoDeCreditos).agregarAlCarrito(productoEsperado)
                    }
                }
            }
        }

        @Nested
        inner class Prohibido
        {
            @Nested
            inner class TipoFondo
            {
                private val mockFondoProhibido = mockConDefaultAnswer(Dinero::class.java).also {
                    doReturn(ID_FONDO_PROHIBIDO).`when`(it).id
                    doReturn("código externo fondo prohibido $ID_FONDO_PROHIBIDO").`when`(it).codigoExterno
                    doReturn("Producto Fondo Prohibido").`when`(it).nombre
                }
                private val productoProhibido =
                        Producto(
                                ProductoFondo.SinCategoria(
                                        mockFondoProhibido,
                                        mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                            doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                            doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                        }
                                                          ),
                                darMockProveedorImagenesProducto(ID_FONDO_PROHIBIDO, false)
                                )

                private val productoQueSeAgrega =
                        Producto(
                                ProductoFondo.SinCategoria(
                                        mockFondoProhibido,
                                        mockDePrecioCompletoEncontrado
                                                          ),
                                darMockProveedorImagenesProducto(ID_FONDO_PROHIBIDO, false)
                                )

                private val observadoresDeHabilitado = listaFiltrable.itemsFiltrables.map {
                    it.habilitado.test()
                }

                @BeforeEach
                fun agregarProducto()
                {
                    for (personaConCarrito in personasConCarritos)
                    {
                        doNothing().`when`(personaConCarrito.carritoDeCreditos).agregarAlCarrito(cualquiera())
                    }

                    modelo.agregarProducto(productoProhibido)
                }

                @Test
                fun para_las_personas_a_las_que_no_se_les_puede_vender_se_deshabilitan_y_quedan_desmarcadas()
                {
                    listaFiltrable.itemsFiltrables.forEachIndexed { indice, itemFiltrable ->
                        if (indice in indicesPersonasParaProductosProhibidos)
                        {
                            observadoresDeHabilitado[indice].verificarUltimoValorEmitido(false)
                            assertFalse(itemFiltrable.estaSeleccionado)
                        }
                        else
                        {
                            observadoresDeHabilitado[indice].verificarUltimoValorEmitido(true)
                            assertTrue(itemFiltrable.estaSeleccionado)
                        }
                    }
                }

                @Test
                fun para_las_personas_a_las_que_no_se_les_puede_vender_no_les_agrega_al_carrito_el_producto()
                {
                    listaFiltrable.itemsFiltrables.forEachIndexed { indice, _ ->
                        val carritoEnPrueba = personasConCarritos[indice].carritoDeCreditos
                        if (indice in indicesPersonasParaProductosProhibidos)
                        {
                            verify(carritoEnPrueba, times(0)).agregarAlCarrito(productoQueSeAgrega)
                        }
                        else
                        {
                            verify(carritoEnPrueba).agregarAlCarrito(productoQueSeAgrega)
                        }
                    }
                }
            }

            @Nested
            inner class TipoPaquete
            {
                private val paqueteProhibido = mockConDefaultAnswer(Paquete::class.java).also {
                    doReturn(ID_PAQUETE_PROHIBIDO).`when`(it).id
                    doReturn("código externo paquete").`when`(it).codigoExterno
                    doReturn("Producto Paquete Prohibido").`when`(it).nombre
                    doReturn(listOf(Paquete.FondoIncluido(1, "código externo fondo 1", Decimal.UNO))).`when`(it).fondosIncluidos
                }
                private val productoProhibido =
                        Producto(
                                ProductoPaquete(
                                        paqueteProhibido,
                                        listOf("Fondo que no importa"),
                                        listOf(mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                            doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                            doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                        })
                                               ),
                                darMockProveedorImagenesProducto(ID_PAQUETE_PROHIBIDO, true)
                                )

                private val productoQueSeAgrega =
                        Producto(
                                ProductoPaquete(
                                        paqueteProhibido,
                                        listOf("Fondo que no importa"),
                                        listOf(mockDePrecioCompletoEncontrado)
                                               ),
                                darMockProveedorImagenesProducto(ID_PAQUETE_PROHIBIDO, true)
                                )

                private val observadoresDeHabilitado = listaFiltrable.itemsFiltrables.map { it.habilitado.test() }

                @BeforeEach
                fun agregarProducto()
                {
                    for (personaConCarrito in personasConCarritos)
                    {
                        doNothing().`when`(personaConCarrito.carritoDeCreditos).agregarAlCarrito(cualquiera())
                    }

                    modelo.agregarProducto(productoProhibido)
                }

                @Test
                fun para_las_personas_a_las_que_no_se_les_puede_vender_se_deshabilitan_y_quedan_desmarcadas()
                {
                    listaFiltrable.itemsFiltrables.forEachIndexed { indice, itemFiltrable ->
                        if (indice in indicesPersonasParaProductosProhibidos)
                        {
                            observadoresDeHabilitado[indice].verificarUltimoValorEmitido(false)
                            assertFalse(itemFiltrable.estaSeleccionado)
                        }
                        else
                        {
                            observadoresDeHabilitado[indice].verificarUltimoValorEmitido(true)
                            assertTrue(itemFiltrable.estaSeleccionado)
                        }
                    }
                }

                @Test
                fun para_las_personas_a_las_que_no_se_les_puede_vender_no_les_agrega_al_carrito_el_producto()
                {
                    listaFiltrable.itemsFiltrables.forEachIndexed { indice, _ ->
                        val carritoEnPrueba = personasConCarritos[indice].carritoDeCreditos
                        if (indice in indicesPersonasParaProductosProhibidos)
                        {
                            verify(carritoEnPrueba, times(0)).agregarAlCarrito(productoQueSeAgrega)
                        }
                        else
                        {
                            verify(carritoEnPrueba).agregarAlCarrito(productoQueSeAgrega)
                        }
                    }
                }
            }
        }
    }

    @Nested
    inner class AlConfirmarCreditos
    {
        @BeforeEach
        fun confirmarCreditosAgregados()
        {
            modelo.confirmarCreditosAgregados()
        }

        @Test
        fun se_invoca_confirman_en_todos_los_carritos()
        {
            personasConCarritos.forEach {
                verify(it.carritoDeCreditos).confirmarCreditosAgregados()
            }
        }

        @Test
        fun y_volver_a_agregar_un_producto_se_agrega_correctamente()
        {
            val mockFondo = mockConDefaultAnswer(Dinero::class.java).also {
                doReturn(1L).`when`(it).id
                doReturn("código externo fondo 1").`when`(it).codigoExterno
                doReturn("Producto Fondo").`when`(it).nombre
            }

            val productoAAgregar =
                    Producto(
                            ProductoFondo.SinCategoria(
                                    mockFondo,
                                    mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                        doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                        doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                    }
                                                      ),
                            darMockProveedorImagenesProducto(1L, false)
                            )

            val productoEsperado = productoAAgregar.actualizarPreciosAsociados(listOf(mockDePrecioCompletoEncontrado))

            for (personaConCarrito in personasConCarritos)
            {
                doNothing().`when`(personaConCarrito.carritoDeCreditos).agregarAlCarrito(productoEsperado)
            }

            modelo.agregarProducto(productoAAgregar)

            for (personaConCarrito in personasConCarritos)
            {
                verify(personaConCarrito.carritoDeCreditos).agregarAlCarrito(productoEsperado)
            }
        }
    }

    @Nested
    inner class AlCancelarCreditos
    {
        @Test
        fun se_invoca_cancelan_en_todos_los_carritos()
        {
            modelo.cancelarCreditosAgregados()

            personasConCarritos.forEach {
                verify(it.carritoDeCreditos).cancelarCreditosAgregados()
            }
        }

        @Test
        fun si_no_esta_agregando_un_producto_y_se_deseleccionan_personas_no_se_invoca_cancelar_creditos()
        {
            personasConCarritos.forEach {
                listaFiltrable.deseleccionarItem(it)
                verify(it.carritoDeCreditos, times(0)).cancelarCreditosAgregados()
            }
        }

        @Test
        fun y_volver_a_agregar_un_producto_se_agrega_correctamente()
        {
            val mockFondo = mockConDefaultAnswer(Dinero::class.java).also {
                doReturn(1L).`when`(it).id
                doReturn("código externo fondo 1").`when`(it).codigoExterno
                doReturn("Producto Fondo").`when`(it).nombre
            }

            val productoAAgregar =
                    Producto(
                            ProductoFondo.SinCategoria(
                                    mockFondo,
                                    mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                        doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                        doReturn("Para cuando haya error no lance excepción").`when`(it).toString()
                                    }
                                                      ),
                            darMockProveedorImagenesProducto(1L, false)
                            )

            val productoEsperado = productoAAgregar.actualizarPreciosAsociados(listOf(mockDePrecioCompletoEncontrado))

            for (personaConCarrito in personasConCarritos)
            {
                doNothing().`when`(personaConCarrito.carritoDeCreditos).agregarAlCarrito(productoEsperado)
            }

            modelo.agregarProducto(productoAAgregar)

            for (personaConCarrito in personasConCarritos)
            {
                verify(personaConCarrito.carritoDeCreditos).agregarAlCarrito(productoEsperado)
            }
        }
    }

    @Nested
    inner class EstaAgregandoProducto
    {
        private val fondo = mockConDefaultAnswer(Dinero::class.java).also {
            doReturn(1L).`when`(it).id
            doReturn("código externo fondo 1").`when`(it).codigoExterno
            doReturn("Producto Fondo").`when`(it).nombre
        }
        private val mockProducto =
                Producto(
                        ProductoFondo.SinCategoria(
                                fondo,
                                mockConDefaultAnswer(PrecioCompleto::class.java).also {
                                    doReturn(Decimal.UNO).`when`(it).precioConImpuesto
                                    doReturn("Para que no lance excepción").`when`(it).toString()
                                }
                                                  ),
                        darMockProveedorImagenesProducto(1L, false)
                        )
        private val observableDePrueba = modelo.estaAgregandoProducto.test()

        @Test
        fun al_suscribirse_emite_false()
        {
            observableDePrueba.assertValuesOnly(false)
        }

        @Test
        fun al_agregar_producto_se_emite_true()
        {
            for (personaConCarrito in personasConCarritos)
            {
                doNothing().`when`(personaConCarrito.carritoDeCreditos).agregarAlCarrito(cualquiera())
            }

            modelo.agregarProducto(mockProducto)

            observableDePrueba.assertValuesOnly(false, true)
        }

        @Nested
        inner class ConUnProductoAgregado
        {
            @BeforeEach
            fun agregarProducto()
            {
                for (personaConCarrito in personasConCarritos)
                {
                    doNothing().`when`(personaConCarrito.carritoDeCreditos).agregarAlCarrito(cualquiera())
                }

                modelo.agregarProducto(mockProducto)
            }

            @Test
            fun al_confirmar_se_emite_false()
            {
                modelo.confirmarCreditosAgregados()

                observableDePrueba.assertValuesOnly(false, true, false)
            }

            @Test
            fun al_cancelar_se_emite_false()
            {
                modelo.cancelarCreditosAgregados()

                observableDePrueba.assertValuesOnly(false, true, false)
            }
        }
    }
}