package co.smartobjects.ui.modelos.carritocreditos

import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ItemCreditoPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 1L
        private const val ORIGEN = "Taquilla"
        private const val NOMBRE_USUARIO = "Usuario"
        private const val ID_PERSONA = 1L
        private const val ID_DISPOSITIVO = "x-a-a-s"
        private const val ID_UBICACION = 1L
        private const val ID_GRUPO_CLIENTE = 1L
        private const val NOMBRE_PRODUCTO = "Producto"
        private const val ID_FONDO = 1L
        private const val ID_CREDITO = 100L
        private const val CODIGO_EXTERNO_FONDO = "código externo $ID_FONDO"
        private val PRECIO_PRODUCTO = PrecioCompleto(Precio(Decimal(1_000), 1), ImpuestoSoloTasa(1, 1, Decimal(10)))
    }

    @Test
    fun si_el_id_del_credito_es_nulo_no_esta_pagado()
    {
        val modelo = ItemCredito(
                NOMBRE_PRODUCTO,
                ID_FONDO,
                CODIGO_EXTERNO_FONDO,
                PRECIO_PRODUCTO,
                null,
                false,
                1,
                false,
                false
                                )

        assertFalse(modelo.estaPagado)
    }

    @Test
    fun si_el_id_del_credito_no_es_nulo_esta_pagado()
    {
        val modelo = ItemCredito(
                NOMBRE_PRODUCTO,
                ID_FONDO,
                CODIGO_EXTERNO_FONDO,
                PRECIO_PRODUCTO,
                ID_CREDITO,
                false,
                1,
                false,
                false
                                )

        assertTrue(modelo.estaPagado)
    }

    @Test
    fun si_el_id_del_credito_no_es_nulo_no_es_modificable()
    {
        val modelo = ItemCredito(
                NOMBRE_PRODUCTO,
                ID_FONDO,
                CODIGO_EXTERNO_FONDO,
                PRECIO_PRODUCTO,
                ID_CREDITO,
                false,
                1,
                false,
                false
                                )

        assertTrue(modelo.noEsModificable)
    }

    @Test
    fun es_paquete_si_el_id_de_paquete_no_es_nulo()
    {
        val itemCreditoDeFondo = ItemCredito(
                NOMBRE_PRODUCTO,
                ID_FONDO,
                CODIGO_EXTERNO_FONDO,
                PRECIO_PRODUCTO,
                ID_CREDITO,
                false,
                1,
                false,
                false
                                            )

        assertFalse(itemCreditoDeFondo.esPaquete)

        val itemCreditoDePaquete =
                ItemCredito(
                        NOMBRE_PRODUCTO,
                        1,
                        "código externo paquete",
                        listOf(1L, 2L),
                        listOf("código externo 1", "código externo 2"),
                        listOf(PRECIO_PRODUCTO, PrecioCompleto(Precio(Decimal(2_000), 2), ImpuestoSoloTasa(1, 2, Decimal(15)))),
                        null,
                        false,
                        listOf(Decimal(3), Decimal(7)),
                        1,
                        false,
                        false
                           )

        assertTrue(itemCreditoDePaquete.esPaquete)
    }

    @Test
    fun lanza_excepcion_si_el_numero_de_ids_de_fondos_y_codigos_externos_difieren()
    {
        assertThrows<EntidadMalInicializada> {
            ItemCredito(
                    NOMBRE_PRODUCTO,
                    1,
                    "código externo paquete",
                    listOf(1L),
                    listOf("código externo fondo 1", "código externo fondo 2"),
                    listOf(PRECIO_PRODUCTO),
                    listOf(ID_CREDITO, 200L),
                    false,
                    listOf(Decimal.UNO, Decimal.UNO),
                    1,
                    false,
                    false
                       )
        }
    }

    @Test
    fun lanza_excepcion_si_el_numero_de_ids_de_fondos_y_precios_difieren()
    {
        assertThrows<EntidadMalInicializada> {
            ItemCredito(
                    NOMBRE_PRODUCTO,
                    1,
                    "código externo paquete",
                    listOf(1L),
                    listOf("código externo fondo 1"),
                    listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO),
                    listOf(ID_CREDITO, 200L),
                    false,
                    listOf(Decimal.UNO, Decimal.UNO),
                    1,
                    false,
                    false
                       )
        }
    }

    @Test
    fun lanza_excepcion_si_el_numero_de_ids_creditos_y_precios_difieren()
    {
        assertThrows<EntidadMalInicializada> {
            ItemCredito(
                    NOMBRE_PRODUCTO,
                    1,
                    "código externo paquete",
                    listOf(1L),
                    listOf("código externo fondo 1"),
                    listOf(PRECIO_PRODUCTO),
                    listOf(ID_CREDITO, 200L),
                    false,
                    listOf(Decimal.UNO, Decimal.UNO),
                    1,
                    false,
                    false
                       )
        }
    }

    @Test
    fun el_precio_total_inicial_es_la_suma_de_los_precios_totales_por_la_cantidad_inicial_de_cada_fondo()
    {
        val cantidades = listOf(Decimal(3), Decimal(7))
        val precios =
                listOf(PRECIO_PRODUCTO, PrecioCompleto(Precio(Decimal(2_000), 2), ImpuestoSoloTasa(1, 2, Decimal(15))))

        val preciosEsperados =
                precios
                    .asSequence()
                    .mapIndexed { i, precioCompleto -> precioCompleto.precioConImpuesto * cantidades[i] }
                    .reduce { acc, precioCompleto -> acc + precioCompleto }

        val itemCreditoDePaquete =
                ItemCredito(
                        NOMBRE_PRODUCTO,
                        1,
                        "código externo paquete",
                        listOf(1L, 2L),
                        listOf("código externo 1", "código externo 2"),
                        precios,
                        null,
                        false,
                        cantidades,
                        1,
                        false,
                        false
                           )

        assertEquals(preciosEsperados, itemCreditoDePaquete.precioTotalInicial)
    }

    @Nested
    inner class Nombre
    {
        @Test
        fun el_nombre_es_igual_al_del_producto()
        {
            val modelo = ItemCredito(
                    NOMBRE_PRODUCTO,
                    ID_FONDO,
                    CODIGO_EXTERNO_FONDO,
                    PRECIO_PRODUCTO,
                    null,
                    false,
                    0,
                    false,
                    false
                                    )
            assertEquals(NOMBRE_PRODUCTO, modelo.nombreProducto)
        }
    }

    @Nested
    inner class Cantidad
    {
        private val cantidadInicial = 10

        @Nested
        inner class Modificable
        {
            private val modelo = ItemCredito(
                    NOMBRE_PRODUCTO,
                    ID_FONDO,
                    CODIGO_EXTERNO_FONDO,
                    PRECIO_PRODUCTO,
                    null,
                    false,
                    cantidadInicial,
                    false,
                    false
                                            )
            private val observableDePrueba = modelo.cantidad.test()

            @Test
            fun emite_la_cantidad_inicial_al_suscribirse()
            {
                observableDePrueba.assertValuesOnly(cantidadInicial)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
            {
                with(modelo)
                {
                    finalizarProceso()
                    sumarUno()
                    restarUno()
                }
                observableDePrueba.assertResult(cantidadInicial)
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_borrar_y_queda_como_completado()
            {
                with(modelo)
                {
                    borrar()
                    sumarUno()
                    restarUno()
                }
                observableDePrueba.assertResult(cantidadInicial)
            }

            @Test
            fun al_incrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
            {
                val numeroDeIncrementos = 13

                val valoresEsperados =
                        generateSequence(cantidadInicial) { it + 1 }
                            .take(1 + numeroDeIncrementos)
                            .toList()
                            .toTypedArray()

                (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                observableDePrueba.assertValuesOnly(*valoresEsperados)
            }

            @Test
            fun al_decrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
            {
                val valoresEsperados =
                        generateSequence(cantidadInicial) { it - 1 }
                            .takeWhile { it > 0 }
                            .toList()
                            .toTypedArray()

                (1 until cantidadInicial).forEach { _ -> modelo.restarUno() }

                observableDePrueba.assertValuesOnly(*valoresEsperados)
            }

            @Test
            fun al_decrementar_hasta_cantidad_cero_emite_el_valor_y_se_completa_el_observable()
            {
                val valoresEsperados =
                        generateSequence(cantidadInicial) { it - 1 }
                            .takeWhile { it >= 0 }
                            .toList()
                            .toTypedArray()

                (1..cantidadInicial).forEach { _ -> modelo.restarUno() }

                observableDePrueba.assertResult(*valoresEsperados)
            }

            @Test
            fun al_incrementar_y_decrementar_los_valores_emitidos_son_correctos()
            {
                modelo.sumarUno()
                modelo.restarUno()
                observableDePrueba.assertValuesOnly(cantidadInicial, cantidadInicial + 1, cantidadInicial)
            }

            @Test
            fun al_cambiar_la_cantidad_y_conectar_un_nuevo_observador_se_recibe_el_ultimo_valor_emitido()
            {
                val numeroDeIncrementos = 13

                val valoresEsperados =
                        generateSequence(cantidadInicial) { it + 1 }
                            .take(1 + numeroDeIncrementos)
                            .toList()
                            .toTypedArray()

                (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                observableDePrueba.assertValuesOnly(*valoresEsperados)

                val nuevoObservableDePrueba = modelo.cantidad.test()

                nuevoObservableDePrueba.assertValuesOnly(valoresEsperados.last())
            }

            @Nested
            inner class QueContieneFondoUnico
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        true,
                        cantidadInicial,
                        false,
                        false
                                                )
                private val observableDePrueba = modelo.cantidad.test()

                @Test
                fun no_permite_incrementar_la_cantidad()
                {
                    val numeroDeIncrementos = 13

                    (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                    observableDePrueba.assertValuesOnly(cantidadInicial)
                }
            }
        }

        @Nested
        inner class NoModificable
        {
            private val modelo = ItemCredito(
                    NOMBRE_PRODUCTO,
                    ID_FONDO,
                    CODIGO_EXTERNO_FONDO,
                    PRECIO_PRODUCTO,
                    null,
                    false,
                    cantidadInicial,
                    true,
                    false
                                            )
            private val observableDePrueba = modelo.cantidad.test()

            @Test
            fun emite_la_cantidad_inicial_al_suscribirse_y_el_observable_esta_completado()
            {
                observableDePrueba.assertResult(cantidadInicial)
            }
        }
    }

    @Nested
    inner class PrecioSinImpuestos
    {
        private val cantidadInicial = 10

        @Nested
        inner class ParaUnCredito
        {
            @Nested
            inner class Modificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        cantidadInicial,
                        false,
                        false
                                                )
                private val observableDePrueba = modelo.precioSinImpuesto.test()

                @Test
                fun emite_valor_correcto_al_suscribirse()
                {
                    observableDePrueba.assertValuesOnly(PRECIO_PRODUCTO.precioSinImpuesto * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        finalizarProceso()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(PRECIO_PRODUCTO.precioSinImpuesto * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_borrar_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        borrar()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(PRECIO_PRODUCTO.precioSinImpuesto * cantidadInicial)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val numeroDeIncrementos = 13

                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it + 1 }
                                .map { PRECIO_PRODUCTO.precioSinImpuesto * it }
                                .take(1 + numeroDeIncrementos)
                                .toList()
                                .toTypedArray()

                    (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { PRECIO_PRODUCTO.precioSinImpuesto * it }
                                .takeWhile { it > 0 }
                                .toList()
                                .toTypedArray()

                    (1 until cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_hasta_cantidad_cero_emite_el_valor_y_se_completa_el_observable()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { PRECIO_PRODUCTO.precioSinImpuesto * it }
                                .takeWhile { it >= 0 }
                                .toList()
                                .toTypedArray()

                    (1..cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertResult(*valoresEsperados)
                }

                @Nested
                inner class QueContieneFondoUnico
                {
                    private val modelo = ItemCredito(
                            NOMBRE_PRODUCTO,
                            ID_FONDO,
                            CODIGO_EXTERNO_FONDO,
                            PRECIO_PRODUCTO,
                            null,
                            true,
                            cantidadInicial,
                            false,
                            false
                                                    )
                    private val observableDePrueba = modelo.precioSinImpuesto.test()

                    @Test
                    fun no_cambia_si_se_incrementa_la_cantidad()
                    {
                        val numeroDeIncrementos = 13

                        (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                        observableDePrueba.assertValuesOnly(PRECIO_PRODUCTO.precioSinImpuesto * cantidadInicial)
                    }
                }
            }

            @Nested
            inner class NoModificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        cantidadInicial,
                        true,
                        false
                                                )
                private val observableDePrueba = modelo.precioSinImpuesto.test()

                @Test
                fun emite_cantidad_correcta_al_suscribirse_y_el_observable_esta_completado()
                {
                    observableDePrueba.assertResult(PRECIO_PRODUCTO.precioSinImpuesto * cantidadInicial)
                }
            }
        }

        @Nested
        inner class ParaMultiplesCreditos
        {
            private val ID_FONDO_2 = 2L
            private val CODIGO_EXTERNO_FONDO_2 = "código externo $ID_FONDO_2"
            private val PRECIO_PRODUCTO_2 = PrecioCompleto(Precio(Decimal(2_000), 2), ImpuestoSoloTasa(1, 2, Decimal(15)))
            private val CANTIDAD_CREDITO_1 = Decimal(3)
            private val CANTIDAD_CREDITO_2 = Decimal(7)

            @Nested
            inner class Modificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        1,
                        "código externo paquete",
                        listOf(ID_FONDO, ID_FONDO_2),
                        listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2),
                        listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2),
                        null,
                        false,
                        listOf(CANTIDAD_CREDITO_1, CANTIDAD_CREDITO_2),
                        cantidadInicial,
                        false,
                        false
                                                )

                private val observableDePrueba = modelo.precioSinImpuesto.test()
                private val precioSinImpuestosTotal =
                        PRECIO_PRODUCTO.precioSinImpuesto * CANTIDAD_CREDITO_1 +
                        PRECIO_PRODUCTO_2.precioSinImpuesto * CANTIDAD_CREDITO_2

                @Test
                fun emite_valor_correcto_al_suscribirse()
                {
                    observableDePrueba.assertValuesOnly(precioSinImpuestosTotal * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        finalizarProceso()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(precioSinImpuestosTotal * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_borrar_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        borrar()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(precioSinImpuestosTotal * cantidadInicial)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val numeroDeIncrementos = 13

                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it + 1 }
                                .map { precioSinImpuestosTotal * it }
                                .take(1 + numeroDeIncrementos)
                                .toList()
                                .toTypedArray()

                    (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { precioSinImpuestosTotal * it }
                                .takeWhile { it > 0 }
                                .toList()
                                .toTypedArray()

                    (1 until cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_hasta_cantidad_cero_emite_el_valor_y_se_completa_el_observable()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { precioSinImpuestosTotal * it }
                                .takeWhile { it >= 0 }
                                .toList()
                                .toTypedArray()

                    (1..cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertResult(*valoresEsperados)
                }

                @Nested
                inner class QueContieneFondoUnico
                {
                    private val modelo = ItemCredito(
                            NOMBRE_PRODUCTO,
                            1,
                            "código externo paquete",
                            listOf(ID_FONDO, ID_FONDO_2),
                            listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2),
                            listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2),
                            null,
                            true,
                            listOf(CANTIDAD_CREDITO_1, CANTIDAD_CREDITO_2),
                            cantidadInicial,
                            false,
                            false
                                                    )

                    private val observableDePrueba = modelo.precioSinImpuesto.test()

                    @Test
                    fun no_cambia_si_se_incrementa_la_cantidad()
                    {
                        val numeroDeIncrementos = 13

                        (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                        observableDePrueba.assertValuesOnly(precioSinImpuestosTotal * cantidadInicial)
                    }
                }
            }

            @Nested
            inner class NoModificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        1,
                        "código externo paquete",
                        listOf(ID_FONDO, ID_FONDO_2),
                        listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2),
                        listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2),
                        null,
                        false,
                        listOf(CANTIDAD_CREDITO_1, CANTIDAD_CREDITO_2),
                        cantidadInicial,
                        true,
                        false
                                                )
                private val observableDePrueba = modelo.precioSinImpuesto.test()
                private val precioSinImpuestosTotalInicial =
                        PRECIO_PRODUCTO.precioSinImpuesto * CANTIDAD_CREDITO_1 +
                        PRECIO_PRODUCTO_2.precioSinImpuesto * CANTIDAD_CREDITO_2

                @Test
                fun emite_cantidad_correcta_al_suscribirse_y_el_observable_esta_completado()
                {
                    observableDePrueba.assertResult(precioSinImpuestosTotalInicial * cantidadInicial)
                }
            }
        }
    }

    @Nested
    inner class ValorImpuesto
    {
        private val cantidadInicial = 10

        @Nested
        inner class ParaUnCredito
        {
            @Nested
            inner class Modificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        cantidadInicial,
                        false,
                        false
                                                )
                private val observableDePrueba = modelo.valorImpuesto.test()

                @Test
                fun emite_valor_correcto_al_suscribirse()
                {
                    observableDePrueba.assertValuesOnly(PRECIO_PRODUCTO.valorImpuesto * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        finalizarProceso()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(PRECIO_PRODUCTO.valorImpuesto * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_borrar_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        borrar()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(PRECIO_PRODUCTO.valorImpuesto * cantidadInicial)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val numeroDeIncrementos = 13

                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it + 1 }
                                .map { PRECIO_PRODUCTO.valorImpuesto * it }
                                .take(1 + numeroDeIncrementos)
                                .toList()
                                .toTypedArray()

                    (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { PRECIO_PRODUCTO.valorImpuesto * it }
                                .takeWhile { it > 0 }
                                .toList()
                                .toTypedArray()

                    (1 until cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_hasta_cantidad_cero_emite_el_valor_y_se_completa_el_observable()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { PRECIO_PRODUCTO.valorImpuesto * it }
                                .takeWhile { it >= 0 }
                                .toList()
                                .toTypedArray()

                    (1..cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertResult(*valoresEsperados)
                }

                @Nested
                inner class QueContieneFondoUnico
                {
                    private val modelo = ItemCredito(
                            NOMBRE_PRODUCTO,
                            ID_FONDO,
                            CODIGO_EXTERNO_FONDO,
                            PRECIO_PRODUCTO,
                            null,
                            true,
                            cantidadInicial,
                            false,
                            false
                                                    )
                    private val observableDePrueba = modelo.valorImpuesto.test()

                    @Test
                    fun no_cambia_si_se_incrementa_la_cantidad()
                    {
                        val numeroDeIncrementos = 13

                        (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                        observableDePrueba.assertValuesOnly(PRECIO_PRODUCTO.valorImpuesto * cantidadInicial)
                    }
                }
            }

            @Nested
            inner class NoModificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        cantidadInicial,
                        true,
                        false
                                                )
                private val observableDePrueba = modelo.valorImpuesto.test()

                @Test
                fun emite_cantidad_correcta_al_suscribirse_y_el_observable_esta_completado()
                {
                    observableDePrueba.assertResult(PRECIO_PRODUCTO.valorImpuesto * cantidadInicial)
                }
            }
        }

        @Nested
        inner class ParaMultiplesCreditos
        {
            private val ID_FONDO_2 = 2L
            private val CODIGO_EXTERNO_FONDO_2 = "código externo $ID_FONDO_2"
            private val PRECIO_PRODUCTO_2 = PrecioCompleto(Precio(Decimal(2_000), 2), ImpuestoSoloTasa(1, 2, Decimal(15)))
            private val CANTIDAD_CREDITO_1 = Decimal(3)
            private val CANTIDAD_CREDITO_2 = Decimal(7)

            @Nested
            inner class Modificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        1,
                        "código externo paquete",
                        listOf(ID_FONDO, ID_FONDO_2),
                        listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2),
                        listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2),
                        null,
                        false,
                        listOf(CANTIDAD_CREDITO_1, CANTIDAD_CREDITO_2),
                        cantidadInicial,
                        false,
                        false
                                                )
                private val observableDePrueba = modelo.valorImpuesto.test()
                private val valorImpuestoTotalInicial =
                        PRECIO_PRODUCTO.valorImpuesto * CANTIDAD_CREDITO_1 +
                        PRECIO_PRODUCTO_2.valorImpuesto * CANTIDAD_CREDITO_2

                @Test
                fun emite_valor_correcto_al_suscribirse()
                {
                    observableDePrueba.assertValuesOnly(valorImpuestoTotalInicial * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        finalizarProceso()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(valorImpuestoTotalInicial * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_borrar_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        borrar()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(valorImpuestoTotalInicial * cantidadInicial)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val numeroDeIncrementos = 13

                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it + 1 }
                                .map { valorImpuestoTotalInicial * it }
                                .take(1 + numeroDeIncrementos)
                                .toList()
                                .toTypedArray()

                    (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { valorImpuestoTotalInicial * it }
                                .takeWhile { it > 0 }
                                .toList()
                                .toTypedArray()

                    (1 until cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_hasta_cantidad_cero_emite_el_valor_y_se_completa_el_observable()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { valorImpuestoTotalInicial * it }
                                .takeWhile { it >= 0 }
                                .toList()
                                .toTypedArray()

                    (1..cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertResult(*valoresEsperados)
                }

                @Nested
                inner class QueContieneFondoUnico
                {
                    private val modelo = ItemCredito(
                            NOMBRE_PRODUCTO,
                            1,
                            "código externo paquete",
                            listOf(ID_FONDO, ID_FONDO_2),
                            listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2),
                            listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2),
                            null,
                            true,
                            listOf(CANTIDAD_CREDITO_1, CANTIDAD_CREDITO_2),
                            cantidadInicial,
                            false,
                            false
                                                    )
                    private val observableDePrueba = modelo.valorImpuesto.test()

                    @Test
                    fun no_cambia_si_se_incrementa_la_cantidad()
                    {
                        val numeroDeIncrementos = 13

                        (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                        observableDePrueba.assertValuesOnly(valorImpuestoTotalInicial * cantidadInicial)
                    }
                }
            }

            @Nested
            inner class NoModificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        1,
                        "código externo paquete",
                        listOf(ID_FONDO, ID_FONDO_2),
                        listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2),
                        listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2),
                        null,
                        false,
                        listOf(CANTIDAD_CREDITO_1, CANTIDAD_CREDITO_2),
                        cantidadInicial,
                        true,
                        false
                                                )
                private val observableDePrueba = modelo.valorImpuesto.test()
                private val valorImpuestoTotalInicial =
                        PRECIO_PRODUCTO.valorImpuesto * CANTIDAD_CREDITO_1 +
                        PRECIO_PRODUCTO_2.valorImpuesto * CANTIDAD_CREDITO_2

                @Test
                fun emite_cantidad_correcta_al_suscribirse_y_el_observable_esta_completado()
                {
                    observableDePrueba.assertResult(valorImpuestoTotalInicial * cantidadInicial)
                }
            }
        }
    }

    @Nested
    inner class PrecioConImpuestosTotal
    {
        private val cantidadInicial = 10

        @Nested
        inner class ParaUnCredito
        {
            @Nested
            inner class Modificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        cantidadInicial,
                        false,
                        false
                                                )
                private val observableDePrueba = modelo.precioConImpuestos.test()

                @Test
                fun emite_valor_correcto_al_suscribirse()
                {
                    observableDePrueba.assertValuesOnly(PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        finalizarProceso()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_borrar_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        borrar()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val numeroDeIncrementos = 13

                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it + 1 }
                                .map { PRECIO_PRODUCTO.precioConImpuesto * it }
                                .take(1 + numeroDeIncrementos)
                                .toList()
                                .toTypedArray()

                    (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { PRECIO_PRODUCTO.precioConImpuesto * it }
                                .takeWhile { it > 0 }
                                .toList()
                                .toTypedArray()

                    (1 until cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_hasta_cantidad_cero_emite_el_valor_y_se_completa_el_observable()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { PRECIO_PRODUCTO.precioConImpuesto * it }
                                .takeWhile { it >= 0 }
                                .toList()
                                .toTypedArray()

                    (1..cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertResult(*valoresEsperados)
                }

                @Nested
                inner class QueContieneFondoUnico
                {
                    private val modelo = ItemCredito(
                            NOMBRE_PRODUCTO,
                            ID_FONDO,
                            CODIGO_EXTERNO_FONDO,
                            PRECIO_PRODUCTO,
                            null,
                            true,
                            cantidadInicial,
                            false,
                            false
                                                    )
                    private val observableDePrueba = modelo.precioConImpuestos.test()


                    @Test
                    fun no_cambia_si_se_incrementa_la_cantidad()
                    {
                        val numeroDeIncrementos = 13

                        (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                        observableDePrueba.assertValuesOnly(PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial)
                    }
                }
            }

            @Nested
            inner class NoModificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        cantidadInicial,
                        true,
                        false
                                                )
                private val observableDePrueba = modelo.precioConImpuestos.test()

                @Test
                fun emite_cantidad_correcta_al_suscribirse_y_el_observable_esta_completado()
                {
                    observableDePrueba.assertResult(PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial)
                }
            }
        }

        @Nested
        inner class ParaMultiplesCreditos
        {
            private val ID_FONDO_2 = 2L
            private val CODIGO_EXTERNO_FONDO_2 = "código externo $ID_FONDO_2"
            private val PRECIO_PRODUCTO_2 = PrecioCompleto(Precio(Decimal(2_000), 2), ImpuestoSoloTasa(1, 2, Decimal(15)))
            private val CANTIDAD_CREDITO_1 = Decimal(3)
            private val CANTIDAD_CREDITO_2 = Decimal(7)

            @Nested
            inner class Modificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        1,
                        "código externo paquete",
                        listOf(ID_FONDO, ID_FONDO_2),
                        listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2),
                        listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2),
                        null,
                        false,
                        listOf(CANTIDAD_CREDITO_1, CANTIDAD_CREDITO_2),
                        cantidadInicial,
                        false,
                        false
                                                )
                private val observableDePrueba = modelo.precioConImpuestos.test()
                private val precioTotalDeProductos =
                        PRECIO_PRODUCTO.precioConImpuesto * CANTIDAD_CREDITO_1 +
                        PRECIO_PRODUCTO_2.precioConImpuesto * CANTIDAD_CREDITO_2

                @Test
                fun emite_valor_correcto_al_suscribirse()
                {
                    observableDePrueba.assertValuesOnly(precioTotalDeProductos * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        finalizarProceso()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(precioTotalDeProductos * cantidadInicial)
                }

                @Test
                fun no_emite_mas_eventos_despues_de_llamar_borrar_y_queda_como_completado()
                {
                    with(modelo)
                    {
                        borrar()
                        sumarUno()
                    }
                    observableDePrueba.assertResult(precioTotalDeProductos * cantidadInicial)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val numeroDeIncrementos = 13

                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it + 1 }
                                .map { precioTotalDeProductos * it }
                                .take(1 + numeroDeIncrementos)
                                .toList()
                                .toTypedArray()

                    (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_los_valores_emitidos_son_correctos()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { precioTotalDeProductos * it }
                                .takeWhile { it > 0 }
                                .toList()
                                .toTypedArray()

                    (1 until cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertValuesOnly(*valoresEsperados)
                }

                @Test
                fun al_decrementar_hasta_cantidad_cero_emite_el_valor_y_se_completa_el_observable()
                {
                    val valoresEsperados =
                            generateSequence(cantidadInicial) { it - 1 }
                                .map { precioTotalDeProductos * it }
                                .takeWhile { it >= 0 }
                                .toList()
                                .toTypedArray()

                    (1..cantidadInicial).forEach { _ -> modelo.restarUno() }

                    observableDePrueba.assertResult(*valoresEsperados)
                }

                @Nested
                inner class QueContieneFondoUnico
                {
                    private val modelo = ItemCredito(
                            NOMBRE_PRODUCTO,
                            1,
                            "código externo paquete",
                            listOf(ID_FONDO, ID_FONDO_2),
                            listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2),
                            listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2),
                            null,
                            true,
                            listOf(CANTIDAD_CREDITO_1, CANTIDAD_CREDITO_2),
                            cantidadInicial,
                            false,
                            false
                                                    )
                    private val observableDePrueba = modelo.precioConImpuestos.test()


                    @Test
                    fun no_cambia_si_se_incrementa_la_cantidad()
                    {
                        val numeroDeIncrementos = 13

                        (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                        observableDePrueba.assertValuesOnly(precioTotalDeProductos * cantidadInicial)
                    }
                }
            }

            @Nested
            inner class NoModificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        1,
                        "código externo paquete",
                        listOf(ID_FONDO, ID_FONDO_2),
                        listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2),
                        listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2),
                        listOf(ID_CREDITO, 200L),
                        false,
                        listOf(CANTIDAD_CREDITO_1, CANTIDAD_CREDITO_2),
                        cantidadInicial,
                        true,
                        false
                                                )
                private val observableDePrueba = modelo.precioConImpuestos.test()
                private val precioTotalDeProductos =
                        PRECIO_PRODUCTO.precioConImpuesto * CANTIDAD_CREDITO_1 +
                        PRECIO_PRODUCTO_2.precioConImpuesto * CANTIDAD_CREDITO_2

                @Test
                fun emite_cantidad_correcta_al_suscribirse_y_el_observable_esta_completado()
                {
                    observableDePrueba.assertResult(precioTotalDeProductos * cantidadInicial)
                }
            }
        }
    }

    @Nested
    inner class SeDebeBorrar
    {
        @Nested
        inner class Modificable
        {
            private val modelo = ItemCredito(
                    NOMBRE_PRODUCTO,
                    ID_FONDO,
                    CODIGO_EXTERNO_FONDO,
                    PRECIO_PRODUCTO,
                    null,
                    false,
                    1,
                    false,
                    false
                                            )
            private val observableDePrueba = modelo.seDebeBorrar.test()

            @Test
            fun no_emite_valor_al_suscribirse()
            {
                observableDePrueba.assertEmpty()
            }

            @Test
            fun no_emite_mas_eventos_despues_de_llamar_finalizar_proceso_y_queda_como_completado()
            {
                with(modelo)
                {
                    finalizarProceso()
                    borrar()
                }
                observableDePrueba.assertResult()
            }

            @Test
            fun si_la_cantidad_baja_a_cero_emite_el_hashcode_y_queda_como_completado()
            {
                modelo.restarUno()
                observableDePrueba.assertResult(modelo.hashCode())
            }

            @Test
            fun si_la_cantidad_sube_y_luego_baja_a_cero_no_emite_nada_y_luego_emite_el_hashcode_y_completa()
            {
                modelo.sumarUno()
                observableDePrueba.assertEmpty()
                modelo.restarUno()
                modelo.restarUno()
                observableDePrueba.assertResult(modelo.hashCode())
            }

            @Test
            fun si_se_llama_borrar_emite_el_hashcode_y_completa()
            {
                modelo.borrar()

                observableDePrueba.assertResult(modelo.hashCode())
            }
        }

        @Nested
        inner class NoModificable
        {
            private val modelo = ItemCredito(
                    NOMBRE_PRODUCTO,
                    ID_FONDO,
                    CODIGO_EXTERNO_FONDO,
                    PRECIO_PRODUCTO,
                    null,
                    false,
                    1,
                    true,
                    false
                                            )
            private val observableDePrueba = modelo.seDebeBorrar.test()

            @Test
            fun no_emite_valor_al_suscribirse()
            {
                observableDePrueba.assertEmpty()
            }

            @Test
            fun si_se_llama_borrar_no_emite_nada()
            {
                modelo.borrar()

                observableDePrueba.assertEmpty()
            }
        }
    }

    @Nested
    inner class FaltaConfirmacionAdicion
    {
        @Nested
        inner class Modificable
        {
            @Nested
            inner class SiFaltaConfirmacion
            {
                @Test
                fun al_suscribirse_emite_true_si_falta_confirmacion()
                {
                    val modelo = ItemCredito(
                            NOMBRE_PRODUCTO,
                            ID_FONDO,
                            CODIGO_EXTERNO_FONDO,
                            PRECIO_PRODUCTO,
                            null,
                            false,
                            1,
                            false,
                            true
                                            )
                    val observableDePrueba = modelo.faltaConfirmacionAdicion.test()

                    observableDePrueba.assertValuesOnly(true)
                }

                @Test
                fun al_confirmar_adicion_emite_false()
                {
                    val modelo = ItemCredito(
                            NOMBRE_PRODUCTO,
                            ID_FONDO,
                            CODIGO_EXTERNO_FONDO,
                            PRECIO_PRODUCTO,
                            null,
                            false,
                            1,
                            false,
                            true
                                            )
                    val observableDePrueba = modelo.faltaConfirmacionAdicion.test()

                    modelo.confirmarAdicion()

                    observableDePrueba.assertValuesOnly(true, false)
                }
            }

            @Nested
            inner class SiNoFaltaConfirmacion
            {
                @Test
                fun al_suscribirse_emite_false_si_no_falta_confirmacion()
                {
                    val modelo = ItemCredito(
                            NOMBRE_PRODUCTO,
                            ID_FONDO,
                            CODIGO_EXTERNO_FONDO,
                            PRECIO_PRODUCTO,
                            null,
                            false,
                            1,
                            false,
                            false
                                            )
                    val observableDePrueba = modelo.faltaConfirmacionAdicion.test()

                    observableDePrueba.assertValuesOnly(false)
                }
            }
        }

        @Nested
        inner class NoModificable
        {
            @Test
            fun al_suscribirse_ignora_parametro_de_falta_confirmacion_y_emite_false_y_queda_completado()
            {
                val modeloConConfirmacion = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        1,
                        true,
                        false
                                                       )

                modeloConConfirmacion.faltaConfirmacionAdicion.test().assertResult(false)

                val modeloSinConfirmacion = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        1,
                        true,
                        true
                                                       )

                modeloSinConfirmacion.faltaConfirmacionAdicion.test().assertResult(false)
            }

            @Test
            fun al_confirmar_adicion_no_emite_valores_nuevos_diferentes_al_false_inicial_y_queda_completado()
            {
                val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        1,
                        true,
                        true
                                        )
                val observableDePrueba = modelo.faltaConfirmacionAdicion.test()

                modelo.confirmarAdicion()

                observableDePrueba.assertResult(false)
            }
        }
    }

    @Nested
    inner class ACreditoFondo
    {
        private val cantidadInicial = 5

        @Test
        fun si_es_un_credito_de_un_paquete_retorna_null()
        {
            val modelo = ItemCredito(
                    NOMBRE_PRODUCTO,
                    1,
                    "código externo paquete",
                    listOf(ID_FONDO),
                    listOf(CODIGO_EXTERNO_FONDO),
                    listOf(PRECIO_PRODUCTO),
                    null,
                    false,
                    listOf(Decimal(3)),
                    cantidadInicial,
                    false,
                    false
                                    )

            val entidadObtenida =
                    modelo.aCreditoFondo(
                            ID_CLIENTE,
                            ORIGEN,
                            NOMBRE_USUARIO,
                            ID_PERSONA,
                            ID_DISPOSITIVO,
                            ID_UBICACION,
                            ID_GRUPO_CLIENTE
                                        )

            assertNull(entidadObtenida)
        }

        @Nested
        inner class SinSuscripcionACantidad
        {
            private val modelo = ItemCredito(
                    NOMBRE_PRODUCTO,
                    ID_FONDO,
                    CODIGO_EXTERNO_FONDO,
                    PRECIO_PRODUCTO,
                    null,
                    false,
                    cantidadInicial,
                    false,
                    false
                                            )

            @Test
            fun retorno_una_entidad_por_defecto_compuesta_por_los_valores_inciales()
            {
                val entidadEsperada =
                        CreditoFondo(
                                idCliente = ID_CLIENTE,
                                id = null,
                                cantidad = Decimal(cantidadInicial),
                                valorPagado = PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial,
                                valorImpuestoPagado = PRECIO_PRODUCTO.valorImpuesto * cantidadInicial,
                                validoDesde = null,
                                validoHasta = null,
                                consumido = false,
                                origen = ORIGEN,
                                nombreUsuario = NOMBRE_USUARIO,
                                idPersonaDueña = ID_PERSONA,
                                idFondoComprado = ID_FONDO,
                                codigoExternoFondo = CODIGO_EXTERNO_FONDO,
                                idImpuestoPagado = PRECIO_PRODUCTO.impuesto.id!!,
                                idDispositivo = ID_DISPOSITIVO,
                                idUbicacionCompra = ID_UBICACION,
                                idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                    )

                val entidadObtenida =
                        modelo.aCreditoFondo(
                                ID_CLIENTE,
                                ORIGEN,
                                NOMBRE_USUARIO,
                                ID_PERSONA,
                                ID_DISPOSITIVO,
                                ID_UBICACION,
                                ID_GRUPO_CLIENTE
                                            )

                assertEquals(entidadEsperada, entidadObtenida)
            }

            @Test
            fun si_se_borro_retorna_una_entidad_por_defecto_compuesta_por_los_valores_inciales()
            {
                val entidadEsperada =
                        CreditoFondo(
                                idCliente = ID_CLIENTE,
                                id = null,
                                cantidad = Decimal(cantidadInicial),
                                valorPagado = PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial,
                                valorImpuestoPagado = PRECIO_PRODUCTO.valorImpuesto * cantidadInicial,
                                validoDesde = null,
                                validoHasta = null,
                                consumido = false,
                                origen = ORIGEN,
                                nombreUsuario = NOMBRE_USUARIO,
                                idPersonaDueña = ID_PERSONA,
                                idFondoComprado = ID_FONDO,
                                codigoExternoFondo = CODIGO_EXTERNO_FONDO,
                                idImpuestoPagado = PRECIO_PRODUCTO.impuesto.id!!,
                                idDispositivo = ID_DISPOSITIVO,
                                idUbicacionCompra = ID_UBICACION,
                                idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                    )

                modelo.borrar()

                val entidadObtenida =
                        modelo.aCreditoFondo(
                                ID_CLIENTE,
                                ORIGEN,
                                NOMBRE_USUARIO,
                                ID_PERSONA,
                                ID_DISPOSITIVO,
                                ID_UBICACION,
                                ID_GRUPO_CLIENTE
                                            )

                assertEquals(entidadEsperada, entidadObtenida)
            }
        }

        @Nested
        inner class EstandoSuscritoACantidad
        {
            @Nested
            inner class Modificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        null,
                        false,
                        cantidadInicial,
                        false,
                        false
                                                )

                private val observableDePrueba = modelo.cantidad.test()

                @Test
                fun al_suscribirse_retorno_una_entidad_por_defecto_compuesta_por_los_valores_inciales()
                {
                    val entidadEsperada =
                            CreditoFondo(
                                    idCliente = ID_CLIENTE,
                                    id = null,
                                    cantidad = Decimal(cantidadInicial),
                                    valorPagado = PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial,
                                    valorImpuestoPagado = PRECIO_PRODUCTO.valorImpuesto * cantidadInicial,
                                    validoDesde = null,
                                    validoHasta = null,
                                    consumido = false,
                                    origen = ORIGEN,
                                    nombreUsuario = NOMBRE_USUARIO,
                                    idPersonaDueña = ID_PERSONA,
                                    idFondoComprado = ID_FONDO,
                                    codigoExternoFondo = CODIGO_EXTERNO_FONDO,
                                    idImpuestoPagado = PRECIO_PRODUCTO.impuesto.id!!,
                                    idDispositivo = ID_DISPOSITIVO,
                                    idUbicacionCompra = ID_UBICACION,
                                    idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                        )

                    val entidadObtenida =
                            modelo.aCreditoFondo(
                                    ID_CLIENTE,
                                    ORIGEN,
                                    NOMBRE_USUARIO,
                                    ID_PERSONA,
                                    ID_DISPOSITIVO,
                                    ID_UBICACION,
                                    ID_GRUPO_CLIENTE
                                                )

                    assertEquals(entidadEsperada, entidadObtenida)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_la_entidad_retornada_es_correcta()
                {
                    val numeroDeIncrementos = 13

                    for (i in 0 until numeroDeIncrementos)
                    {
                        val entidadEsperada =
                                CreditoFondo(
                                        idCliente = ID_CLIENTE,
                                        id = null,
                                        cantidad = Decimal(cantidadInicial + i),
                                        valorPagado = PRECIO_PRODUCTO.precioConImpuesto * (cantidadInicial + i),
                                        valorImpuestoPagado = PRECIO_PRODUCTO.valorImpuesto * (cantidadInicial + i),
                                        validoDesde = null,
                                        validoHasta = null,
                                        consumido = false,
                                        origen = ORIGEN,
                                        nombreUsuario = NOMBRE_USUARIO,
                                        idPersonaDueña = ID_PERSONA,
                                        idFondoComprado = ID_FONDO,
                                        codigoExternoFondo = CODIGO_EXTERNO_FONDO,
                                        idImpuestoPagado = PRECIO_PRODUCTO.impuesto.id!!,
                                        idDispositivo = ID_DISPOSITIVO,
                                        idUbicacionCompra = ID_UBICACION,
                                        idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                            )

                        val entidadObtenida =
                                modelo.aCreditoFondo(
                                        ID_CLIENTE,
                                        ORIGEN,
                                        NOMBRE_USUARIO,
                                        ID_PERSONA,
                                        ID_DISPOSITIVO,
                                        ID_UBICACION,
                                        ID_GRUPO_CLIENTE
                                                    )

                        assertEquals(entidadEsperada, entidadObtenida)

                        modelo.sumarUno()
                    }
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_la_entidad_retornada_es_correcta()
                {
                    for (i in 0 until cantidadInicial)
                    {
                        val entidadEsperada =
                                CreditoFondo(
                                        idCliente = ID_CLIENTE,
                                        id = null,
                                        cantidad = Decimal(cantidadInicial - i),
                                        valorPagado = PRECIO_PRODUCTO.precioConImpuesto * (cantidadInicial - i),
                                        valorImpuestoPagado = PRECIO_PRODUCTO.valorImpuesto * (cantidadInicial - i),
                                        validoDesde = null,
                                        validoHasta = null,
                                        consumido = false,
                                        origen = ORIGEN,
                                        nombreUsuario = NOMBRE_USUARIO,
                                        idPersonaDueña = ID_PERSONA,
                                        idFondoComprado = ID_FONDO,
                                        codigoExternoFondo = CODIGO_EXTERNO_FONDO,
                                        idImpuestoPagado = PRECIO_PRODUCTO.impuesto.id!!,
                                        idDispositivo = ID_DISPOSITIVO,
                                        idUbicacionCompra = ID_UBICACION,
                                        idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                            )

                        val entidadObtenida =
                                modelo.aCreditoFondo(
                                        ID_CLIENTE,
                                        ORIGEN,
                                        NOMBRE_USUARIO,
                                        ID_PERSONA,
                                        ID_DISPOSITIVO,
                                        ID_UBICACION,
                                        ID_GRUPO_CLIENTE
                                                    )

                        assertEquals(entidadEsperada, entidadObtenida)

                        modelo.restarUno()
                    }
                }

                @Test
                fun al_cambiar_la_cantidad_y_luego_borrar_la_entidad_retornada_es_correcta()
                {
                    val numeroDeIncrementos = 13

                    val entidadEsperada =
                            CreditoFondo(
                                    idCliente = ID_CLIENTE,
                                    id = null,
                                    cantidad = Decimal(cantidadInicial + numeroDeIncrementos),
                                    valorPagado = PRECIO_PRODUCTO.precioConImpuesto * (cantidadInicial + numeroDeIncrementos),
                                    valorImpuestoPagado = PRECIO_PRODUCTO.valorImpuesto * (cantidadInicial + numeroDeIncrementos),
                                    validoDesde = null,
                                    validoHasta = null,
                                    consumido = false,
                                    origen = ORIGEN,
                                    nombreUsuario = NOMBRE_USUARIO,
                                    idPersonaDueña = ID_PERSONA,
                                    idFondoComprado = ID_FONDO,
                                    codigoExternoFondo = CODIGO_EXTERNO_FONDO,
                                    idImpuestoPagado = PRECIO_PRODUCTO.impuesto.id!!,
                                    idDispositivo = ID_DISPOSITIVO,
                                    idUbicacionCompra = ID_UBICACION,
                                    idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                        )

                    (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                    modelo.borrar()

                    val entidadObtenida =
                            modelo.aCreditoFondo(
                                    ID_CLIENTE,
                                    ORIGEN,
                                    NOMBRE_USUARIO,
                                    ID_PERSONA,
                                    ID_DISPOSITIVO,
                                    ID_UBICACION,
                                    ID_GRUPO_CLIENTE
                                                )

                    assertEquals(entidadEsperada, entidadObtenida)
                }
            }

            @Nested
            inner class NoModificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_FONDO,
                        CODIGO_EXTERNO_FONDO,
                        PRECIO_PRODUCTO,
                        ID_CREDITO,
                        false,
                        cantidadInicial,
                        true,
                        false
                                                )

                private val observableDePrueba = modelo.cantidad.test()

                @Test
                fun al_suscribirse_retorno_una_entidad_por_defecto_compuesta_por_los_valores_inciales()
                {
                    val entidadEsperada =
                            CreditoFondo(
                                    idCliente = ID_CLIENTE,
                                    id = ID_CREDITO,
                                    cantidad = Decimal(cantidadInicial),
                                    valorPagado = PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial,
                                    valorImpuestoPagado = PRECIO_PRODUCTO.valorImpuesto * cantidadInicial,
                                    validoDesde = null,
                                    validoHasta = null,
                                    consumido = false,
                                    origen = ORIGEN,
                                    nombreUsuario = NOMBRE_USUARIO,
                                    idPersonaDueña = ID_PERSONA,
                                    idFondoComprado = ID_FONDO,
                                    codigoExternoFondo = CODIGO_EXTERNO_FONDO,
                                    idImpuestoPagado = PRECIO_PRODUCTO.impuesto.id!!,
                                    idDispositivo = ID_DISPOSITIVO,
                                    idUbicacionCompra = ID_UBICACION,
                                    idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                        )

                    val entidadObtenida =
                            modelo.aCreditoFondo(
                                    ID_CLIENTE,
                                    ORIGEN,
                                    NOMBRE_USUARIO,
                                    ID_PERSONA,
                                    ID_DISPOSITIVO,
                                    ID_UBICACION,
                                    ID_GRUPO_CLIENTE
                                                )

                    assertEquals(entidadEsperada, entidadObtenida)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_la_entidad_retornada_es_constante()
                {
                    val numeroDeIncrementos = 13

                    for (i in 0 until numeroDeIncrementos)
                    {
                        val entidadEsperada =
                                CreditoFondo(
                                        idCliente = ID_CLIENTE,
                                        id = ID_CREDITO,
                                        cantidad = Decimal(cantidadInicial),
                                        valorPagado = PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial,
                                        valorImpuestoPagado = PRECIO_PRODUCTO.valorImpuesto * cantidadInicial,
                                        validoDesde = null,
                                        validoHasta = null,
                                        consumido = false,
                                        origen = ORIGEN,
                                        nombreUsuario = NOMBRE_USUARIO,
                                        idPersonaDueña = ID_PERSONA,
                                        idFondoComprado = ID_FONDO,
                                        codigoExternoFondo = CODIGO_EXTERNO_FONDO,
                                        idImpuestoPagado = PRECIO_PRODUCTO.impuesto.id!!,
                                        idDispositivo = ID_DISPOSITIVO,
                                        idUbicacionCompra = ID_UBICACION,
                                        idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                            )

                        val entidadObtenida =
                                modelo.aCreditoFondo(
                                        ID_CLIENTE,
                                        ORIGEN,
                                        NOMBRE_USUARIO,
                                        ID_PERSONA,
                                        ID_DISPOSITIVO,
                                        ID_UBICACION,
                                        ID_GRUPO_CLIENTE
                                                    )

                        assertEquals(entidadEsperada, entidadObtenida)

                        modelo.sumarUno()
                    }
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_la_entidad_retornada_es_constante()
                {
                    for (i in 0 until cantidadInicial)
                    {
                        val entidadEsperada =
                                CreditoFondo(
                                        idCliente = ID_CLIENTE,
                                        id = ID_CREDITO,
                                        cantidad = Decimal(cantidadInicial),
                                        valorPagado = PRECIO_PRODUCTO.precioConImpuesto * cantidadInicial,
                                        valorImpuestoPagado = PRECIO_PRODUCTO.valorImpuesto * cantidadInicial,
                                        validoDesde = null,
                                        validoHasta = null,
                                        consumido = false,
                                        origen = ORIGEN,
                                        nombreUsuario = NOMBRE_USUARIO,
                                        idPersonaDueña = ID_PERSONA,
                                        idFondoComprado = ID_FONDO,
                                        codigoExternoFondo = CODIGO_EXTERNO_FONDO,
                                        idImpuestoPagado = PRECIO_PRODUCTO.impuesto.id!!,
                                        idDispositivo = ID_DISPOSITIVO,
                                        idUbicacionCompra = ID_UBICACION,
                                        idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                            )

                        val entidadObtenida =
                                modelo.aCreditoFondo(
                                        ID_CLIENTE,
                                        ORIGEN,
                                        NOMBRE_USUARIO,
                                        ID_PERSONA,
                                        ID_DISPOSITIVO,
                                        ID_UBICACION,
                                        ID_GRUPO_CLIENTE
                                                    )

                        assertEquals(entidadEsperada, entidadObtenida)

                        modelo.restarUno()
                    }
                }
            }
        }
    }

    @Nested
    inner class ACreditoPaquete
    {
        private val cantidadInicial = 5
        private val ID_PAQUETE = 1L
        private val CODIGO_EXTERNO_PAQUETE = "código externo paquete"
        private val ID_FONDO_2 = 2L
        private val CODIGO_EXTERNO_FONDO_2 = "código externo $ID_FONDO_2"
        private val PRECIO_PRODUCTO_2 = PrecioCompleto(Precio(Decimal(2_000), 2), ImpuestoSoloTasa(1, 2, Decimal(15)))
        private val idsDeFondos = listOf(ID_FONDO, ID_FONDO_2)
        private val codigosExternosFondos = listOf(CODIGO_EXTERNO_FONDO, CODIGO_EXTERNO_FONDO_2)
        private val preciosDeFondos = listOf(PRECIO_PRODUCTO, PRECIO_PRODUCTO_2)
        private val idsCreditos = listOf(ID_CREDITO, 200L)
        private val cantidadesDeCreditos = listOf(Decimal(3), Decimal(7))


        @Test
        fun si_es_un_credito_de_un_fondo_retorna_null()
        {
            val modelo = ItemCredito(
                    NOMBRE_PRODUCTO,
                    ID_FONDO,
                    CODIGO_EXTERNO_FONDO,
                    PRECIO_PRODUCTO,
                    null,
                    false,
                    cantidadInicial,
                    false,
                    false
                                    )

            val entidadObtenida =
                    modelo.aCreditoPaquete(
                            ID_CLIENTE,
                            ORIGEN,
                            NOMBRE_USUARIO,
                            ID_PERSONA,
                            ID_DISPOSITIVO,
                            ID_UBICACION,
                            ID_GRUPO_CLIENTE
                                          )

            assertNull(entidadObtenida)
        }

        @Nested
        inner class SinSuscripcionACantidad
        {
            private val modelo = ItemCredito(
                    NOMBRE_PRODUCTO,
                    ID_PAQUETE,
                    CODIGO_EXTERNO_PAQUETE,
                    idsDeFondos,
                    codigosExternosFondos,
                    preciosDeFondos,
                    null,
                    false,
                    cantidadesDeCreditos,
                    cantidadInicial,
                    false,
                    false
                                            )

            @Test
            fun retorno_una_entidad_por_defecto_compuesta_por_los_valores_inciales()
            {
                val creditoPaqueteEsperado =
                        CreditoPaquete(
                                ID_PAQUETE,
                                CODIGO_EXTERNO_PAQUETE,
                                idsDeFondos.mapIndexed { index, idFondo ->
                                    val cantidadFinal = cantidadesDeCreditos[index] * cantidadInicial
                                    CreditoFondo(
                                            idCliente = ID_CLIENTE,
                                            id = null,
                                            cantidad = cantidadFinal,
                                            valorPagado = preciosDeFondos[index].precioConImpuesto * cantidadFinal,
                                            valorImpuestoPagado = preciosDeFondos[index].valorImpuesto * cantidadFinal,
                                            validoDesde = null,
                                            validoHasta = null,
                                            consumido = false,
                                            origen = ORIGEN,
                                            nombreUsuario = NOMBRE_USUARIO,
                                            idPersonaDueña = ID_PERSONA,
                                            idFondoComprado = idFondo,
                                            codigoExternoFondo = codigosExternosFondos[index],
                                            idImpuestoPagado = preciosDeFondos[index].impuesto.id!!,
                                            idDispositivo = ID_DISPOSITIVO,
                                            idUbicacionCompra = ID_UBICACION,
                                            idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                                )
                                }
                                      )

                val entidadEsperada = CreditoPaqueteConNombre(NOMBRE_PRODUCTO, cantidadInicial, creditoPaqueteEsperado)

                val entidadObtenida =
                        modelo.aCreditoPaquete(
                                ID_CLIENTE,
                                ORIGEN,
                                NOMBRE_USUARIO,
                                ID_PERSONA,
                                ID_DISPOSITIVO,
                                ID_UBICACION,
                                ID_GRUPO_CLIENTE
                                              )

                assertEquals(entidadEsperada, entidadObtenida)
            }

            @Test
            fun si_se_borro_retorna_una_entidad_por_defecto_compuesta_por_los_valores_inciales()
            {
                val creditoPaqueteEsperado =
                        CreditoPaquete(
                                ID_PAQUETE,
                                CODIGO_EXTERNO_PAQUETE,
                                idsDeFondos.mapIndexed { index, idFondo ->
                                    val cantidadFinal = cantidadesDeCreditos[index] * cantidadInicial
                                    CreditoFondo(
                                            idCliente = ID_CLIENTE,
                                            id = null,
                                            cantidad = cantidadFinal,
                                            valorPagado = preciosDeFondos[index].precioConImpuesto * cantidadFinal,
                                            valorImpuestoPagado = preciosDeFondos[index].valorImpuesto * cantidadFinal,
                                            validoDesde = null,
                                            validoHasta = null,
                                            consumido = false,
                                            origen = ORIGEN,
                                            nombreUsuario = NOMBRE_USUARIO,
                                            idPersonaDueña = ID_PERSONA,
                                            idFondoComprado = idFondo,
                                            codigoExternoFondo = codigosExternosFondos[index],
                                            idImpuestoPagado = preciosDeFondos[index].impuesto.id!!,
                                            idDispositivo = ID_DISPOSITIVO,
                                            idUbicacionCompra = ID_UBICACION,
                                            idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                                )
                                }
                                      )

                val entidadEsperada = CreditoPaqueteConNombre(NOMBRE_PRODUCTO, cantidadInicial, creditoPaqueteEsperado)

                modelo.borrar()

                val entidadObtenida =
                        modelo.aCreditoPaquete(
                                ID_CLIENTE,
                                ORIGEN,
                                NOMBRE_USUARIO,
                                ID_PERSONA,
                                ID_DISPOSITIVO,
                                ID_UBICACION,
                                ID_GRUPO_CLIENTE
                                              )

                assertEquals(entidadEsperada, entidadObtenida)
            }
        }

        @Nested
        inner class EstandoSuscritoACantidad
        {
            @Nested
            inner class Modificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_PAQUETE,
                        CODIGO_EXTERNO_PAQUETE,
                        idsDeFondos,
                        codigosExternosFondos,
                        preciosDeFondos,
                        null,
                        false,
                        cantidadesDeCreditos,
                        cantidadInicial,
                        false,
                        false
                                                )

                private val observableDePrueba = modelo.cantidad.test()

                @Test
                fun al_suscribirse_retorno_una_entidad_por_defecto_compuesta_por_los_valores_inciales()
                {
                    val creditoPaqueteEsperado =
                            CreditoPaquete(
                                    ID_PAQUETE,
                                    CODIGO_EXTERNO_PAQUETE,
                                    idsDeFondos.mapIndexed { index, idFondo ->
                                        val cantidadFinal = cantidadesDeCreditos[index] * cantidadInicial
                                        CreditoFondo(
                                                idCliente = ID_CLIENTE,
                                                id = null,
                                                cantidad = cantidadFinal,
                                                valorPagado = preciosDeFondos[index].precioConImpuesto * cantidadFinal,
                                                valorImpuestoPagado = preciosDeFondos[index].valorImpuesto * cantidadFinal,
                                                validoDesde = null,
                                                validoHasta = null,
                                                consumido = false,
                                                origen = ORIGEN,
                                                nombreUsuario = NOMBRE_USUARIO,
                                                idPersonaDueña = ID_PERSONA,
                                                idFondoComprado = idFondo,
                                                codigoExternoFondo = codigosExternosFondos[index],
                                                idImpuestoPagado = preciosDeFondos[index].impuesto.id!!,
                                                idDispositivo = ID_DISPOSITIVO,
                                                idUbicacionCompra = ID_UBICACION,
                                                idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                                    )
                                    }
                                          )

                    val entidadEsperada = CreditoPaqueteConNombre(NOMBRE_PRODUCTO, cantidadInicial, creditoPaqueteEsperado)

                    val entidadObtenida =
                            modelo.aCreditoPaquete(
                                    ID_CLIENTE,
                                    ORIGEN,
                                    NOMBRE_USUARIO,
                                    ID_PERSONA,
                                    ID_DISPOSITIVO,
                                    ID_UBICACION,
                                    ID_GRUPO_CLIENTE
                                                  )

                    assertEquals(entidadEsperada, entidadObtenida)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_la_entidad_retornada_es_correcta()
                {
                    val numeroDeIncrementos = 13

                    for (i in 0 until numeroDeIncrementos)
                    {
                        val cantidadActual = cantidadInicial + i
                        val creditoPaqueteEsperado =
                                CreditoPaquete(
                                        ID_PAQUETE,
                                        CODIGO_EXTERNO_PAQUETE,
                                        idsDeFondos.mapIndexed { index, idFondo ->
                                            val cantidadFinal = cantidadesDeCreditos[index] * cantidadActual
                                            CreditoFondo(
                                                    idCliente = ID_CLIENTE,
                                                    id = null,
                                                    cantidad = cantidadFinal,
                                                    valorPagado = preciosDeFondos[index].precioConImpuesto * cantidadFinal,
                                                    valorImpuestoPagado = preciosDeFondos[index].valorImpuesto * cantidadFinal,
                                                    validoDesde = null,
                                                    validoHasta = null,
                                                    consumido = false,
                                                    origen = ORIGEN,
                                                    nombreUsuario = NOMBRE_USUARIO,
                                                    idPersonaDueña = ID_PERSONA,
                                                    idFondoComprado = idFondo,
                                                    codigoExternoFondo = codigosExternosFondos[index],
                                                    idImpuestoPagado = preciosDeFondos[index].impuesto.id!!,
                                                    idDispositivo = ID_DISPOSITIVO,
                                                    idUbicacionCompra = ID_UBICACION,
                                                    idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                                        )
                                        }
                                              )

                        val entidadEsperada = CreditoPaqueteConNombre(NOMBRE_PRODUCTO, cantidadActual, creditoPaqueteEsperado)

                        val entidadObtenida =
                                modelo.aCreditoPaquete(
                                        ID_CLIENTE,
                                        ORIGEN,
                                        NOMBRE_USUARIO,
                                        ID_PERSONA,
                                        ID_DISPOSITIVO,
                                        ID_UBICACION,
                                        ID_GRUPO_CLIENTE
                                                      )

                        assertEquals(entidadEsperada, entidadObtenida)

                        modelo.sumarUno()
                    }
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_la_entidad_retornada_es_correcta()
                {
                    for (i in 0 until cantidadInicial)
                    {
                        val cantidadActual = cantidadInicial - i
                        val creditoPaqueteEsperado =
                                CreditoPaquete(
                                        ID_PAQUETE,
                                        CODIGO_EXTERNO_PAQUETE,
                                        idsDeFondos.mapIndexed { index, idFondo ->
                                            val cantidadFinal = cantidadesDeCreditos[index] * cantidadActual
                                            CreditoFondo(
                                                    idCliente = ID_CLIENTE,
                                                    id = null,
                                                    cantidad = cantidadFinal,
                                                    valorPagado = preciosDeFondos[index].precioConImpuesto * cantidadFinal,
                                                    valorImpuestoPagado = preciosDeFondos[index].valorImpuesto * cantidadFinal,
                                                    validoDesde = null,
                                                    validoHasta = null,
                                                    consumido = false,
                                                    origen = ORIGEN,
                                                    nombreUsuario = NOMBRE_USUARIO,
                                                    idPersonaDueña = ID_PERSONA,
                                                    idFondoComprado = idFondo,
                                                    codigoExternoFondo = codigosExternosFondos[index],
                                                    idImpuestoPagado = preciosDeFondos[index].impuesto.id!!,
                                                    idDispositivo = ID_DISPOSITIVO,
                                                    idUbicacionCompra = ID_UBICACION,
                                                    idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                                        )
                                        }
                                              )

                        val entidadEsperada = CreditoPaqueteConNombre(NOMBRE_PRODUCTO, cantidadActual, creditoPaqueteEsperado)

                        val entidadObtenida =
                                modelo.aCreditoPaquete(
                                        ID_CLIENTE,
                                        ORIGEN,
                                        NOMBRE_USUARIO,
                                        ID_PERSONA,
                                        ID_DISPOSITIVO,
                                        ID_UBICACION,
                                        ID_GRUPO_CLIENTE
                                                      )

                        assertEquals(entidadEsperada, entidadObtenida)

                        modelo.restarUno()
                    }
                }

                @Test
                fun al_cambiar_la_cantidad_y_luego_borrar_la_entidad_retornada_es_correcta()
                {
                    val numeroDeIncrementos = 13
                    val cantidadActual = cantidadInicial + numeroDeIncrementos
                    val creditoPaqueteEsperado =
                            CreditoPaquete(
                                    ID_PAQUETE,
                                    CODIGO_EXTERNO_PAQUETE,
                                    idsDeFondos.mapIndexed { index, idFondo ->
                                        val cantidadFinal = cantidadesDeCreditos[index] * cantidadActual
                                        CreditoFondo(
                                                idCliente = ID_CLIENTE,
                                                id = null,
                                                cantidad = cantidadFinal,
                                                valorPagado = preciosDeFondos[index].precioConImpuesto * cantidadFinal,
                                                valorImpuestoPagado = preciosDeFondos[index].valorImpuesto * cantidadFinal,
                                                validoDesde = null,
                                                validoHasta = null,
                                                consumido = false,
                                                origen = ORIGEN,
                                                nombreUsuario = NOMBRE_USUARIO,
                                                idPersonaDueña = ID_PERSONA,
                                                idFondoComprado = idFondo,
                                                codigoExternoFondo = codigosExternosFondos[index],
                                                idImpuestoPagado = preciosDeFondos[index].impuesto.id!!,
                                                idDispositivo = ID_DISPOSITIVO,
                                                idUbicacionCompra = ID_UBICACION,
                                                idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                                    )
                                    }
                                          )

                    val entidadEsperada = CreditoPaqueteConNombre(NOMBRE_PRODUCTO, cantidadActual, creditoPaqueteEsperado)

                    (1..numeroDeIncrementos).forEach { _ -> modelo.sumarUno() }

                    modelo.borrar()

                    val entidadObtenida =
                            modelo.aCreditoPaquete(
                                    ID_CLIENTE,
                                    ORIGEN,
                                    NOMBRE_USUARIO,
                                    ID_PERSONA,
                                    ID_DISPOSITIVO,
                                    ID_UBICACION,
                                    ID_GRUPO_CLIENTE
                                                  )

                    assertEquals(entidadEsperada, entidadObtenida)
                }
            }

            @Nested
            inner class NoModificable
            {
                private val modelo = ItemCredito(
                        NOMBRE_PRODUCTO,
                        ID_PAQUETE,
                        CODIGO_EXTERNO_PAQUETE,
                        idsDeFondos,
                        codigosExternosFondos,
                        preciosDeFondos,
                        idsCreditos,
                        false,
                        cantidadesDeCreditos,
                        cantidadInicial,
                        true,
                        false
                                                )

                private val observableDePrueba = modelo.cantidad.test()

                @Test
                fun al_suscribirse_retorno_una_entidad_por_defecto_compuesta_por_los_valores_inciales()
                {
                    val creditoPaqueteEsperado =
                            CreditoPaquete(
                                    ID_PAQUETE,
                                    CODIGO_EXTERNO_PAQUETE,
                                    idsDeFondos.mapIndexed { index, idFondo ->
                                        val cantidadFinal = cantidadesDeCreditos[index] * cantidadInicial
                                        CreditoFondo(
                                                idCliente = ID_CLIENTE,
                                                id = idsCreditos[index],
                                                cantidad = cantidadFinal,
                                                valorPagado = preciosDeFondos[index].precioConImpuesto * cantidadFinal,
                                                valorImpuestoPagado = preciosDeFondos[index].valorImpuesto * cantidadFinal,
                                                validoDesde = null,
                                                validoHasta = null,
                                                consumido = false,
                                                origen = ORIGEN,
                                                nombreUsuario = NOMBRE_USUARIO,
                                                idPersonaDueña = ID_PERSONA,
                                                idFondoComprado = idFondo,
                                                codigoExternoFondo = codigosExternosFondos[index],
                                                idImpuestoPagado = preciosDeFondos[index].impuesto.id!!,
                                                idDispositivo = ID_DISPOSITIVO,
                                                idUbicacionCompra = ID_UBICACION,
                                                idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                                    )
                                    }
                                          )

                    val entidadEsperada = CreditoPaqueteConNombre(NOMBRE_PRODUCTO, cantidadInicial, creditoPaqueteEsperado)

                    val entidadObtenida =
                            modelo.aCreditoPaquete(
                                    ID_CLIENTE,
                                    ORIGEN,
                                    NOMBRE_USUARIO,
                                    ID_PERSONA,
                                    ID_DISPOSITIVO,
                                    ID_UBICACION,
                                    ID_GRUPO_CLIENTE
                                                  )

                    assertEquals(entidadEsperada, entidadObtenida)
                }

                @Test
                fun al_incrementar_un_numero_arbirario_de_veces_la_entidad_retornada_es_constante()
                {
                    val numeroDeIncrementos = 13

                    for (i in 0 until numeroDeIncrementos)
                    {
                        val creditoPaqueteEsperado =
                                CreditoPaquete(
                                        ID_PAQUETE,
                                        CODIGO_EXTERNO_PAQUETE,
                                        idsDeFondos.mapIndexed { index, idFondo ->
                                            val cantidadFinal = cantidadesDeCreditos[index] * cantidadInicial
                                            CreditoFondo(
                                                    idCliente = ID_CLIENTE,
                                                    id = idsCreditos[index],
                                                    cantidad = cantidadFinal,
                                                    valorPagado = preciosDeFondos[index].precioConImpuesto * cantidadFinal,
                                                    valorImpuestoPagado = preciosDeFondos[index].valorImpuesto * cantidadFinal,
                                                    validoDesde = null,
                                                    validoHasta = null,
                                                    consumido = false,
                                                    origen = ORIGEN,
                                                    nombreUsuario = NOMBRE_USUARIO,
                                                    idPersonaDueña = ID_PERSONA,
                                                    idFondoComprado = idFondo,
                                                    codigoExternoFondo = codigosExternosFondos[index],
                                                    idImpuestoPagado = preciosDeFondos[index].impuesto.id!!,
                                                    idDispositivo = ID_DISPOSITIVO,
                                                    idUbicacionCompra = ID_UBICACION,
                                                    idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                                        )
                                        }
                                              )

                        val entidadEsperada = CreditoPaqueteConNombre(NOMBRE_PRODUCTO, cantidadInicial, creditoPaqueteEsperado)

                        val entidadObtenida =
                                modelo.aCreditoPaquete(
                                        ID_CLIENTE,
                                        ORIGEN,
                                        NOMBRE_USUARIO,
                                        ID_PERSONA,
                                        ID_DISPOSITIVO,
                                        ID_UBICACION,
                                        ID_GRUPO_CLIENTE
                                                      )

                        assertEquals(entidadEsperada, entidadObtenida)

                        modelo.sumarUno()
                    }
                }

                @Test
                fun al_decrementar_un_numero_arbirario_de_veces_la_entidad_retornada_es_constante()
                {
                    for (i in 0 until cantidadInicial)
                    {
                        val creditoPaqueteEsperado =
                                CreditoPaquete(
                                        ID_PAQUETE,
                                        CODIGO_EXTERNO_PAQUETE,
                                        idsDeFondos.mapIndexed { index, idFondo ->
                                            val cantidadFinal = cantidadesDeCreditos[index] * cantidadInicial
                                            CreditoFondo(
                                                    idCliente = ID_CLIENTE,
                                                    id = idsCreditos[index],
                                                    cantidad = cantidadFinal,
                                                    valorPagado = preciosDeFondos[index].precioConImpuesto * cantidadFinal,
                                                    valorImpuestoPagado = preciosDeFondos[index].valorImpuesto * cantidadFinal,
                                                    validoDesde = null,
                                                    validoHasta = null,
                                                    consumido = false,
                                                    origen = ORIGEN,
                                                    nombreUsuario = NOMBRE_USUARIO,
                                                    idPersonaDueña = ID_PERSONA,
                                                    idFondoComprado = idFondo,
                                                    codigoExternoFondo = codigosExternosFondos[index],
                                                    idImpuestoPagado = preciosDeFondos[index].impuesto.id!!,
                                                    idDispositivo = ID_DISPOSITIVO,
                                                    idUbicacionCompra = ID_UBICACION,
                                                    idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                                        )
                                        }
                                              )

                        val entidadEsperada = CreditoPaqueteConNombre(NOMBRE_PRODUCTO, cantidadInicial, creditoPaqueteEsperado)

                        val entidadObtenida =
                                modelo.aCreditoPaquete(
                                        ID_CLIENTE,
                                        ORIGEN,
                                        NOMBRE_USUARIO,
                                        ID_PERSONA,
                                        ID_DISPOSITIVO,
                                        ID_UBICACION,
                                        ID_GRUPO_CLIENTE
                                                      )

                        assertEquals(entidadEsperada, entidadObtenida)

                        modelo.restarUno()
                    }
                }
            }
        }
    }
}