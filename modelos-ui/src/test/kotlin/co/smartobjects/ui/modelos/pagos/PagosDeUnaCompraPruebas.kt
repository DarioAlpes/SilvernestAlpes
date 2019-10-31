package co.smartobjects.ui.modelos.pagos

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.ui.modelos.PruebasModelosRxBase
import co.smartobjects.ui.modelos.verificarUltimoValorEmitido
import co.smartobjects.utilidades.Decimal
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("PagosDeUnaCompra")
internal class PagosDeUnaCompraPruebas : PruebasModelosRxBase()
{
    @Nested
    inner class AlInicializarConValorPositivo
    {
        private val valorTotalAPagar = Decimal(100)
        private val modelo: PagosDeUnaCompraUI = PagosDeUnaCompra(Observable.just(valorTotalAPagar))

        @Nested
        inner class PagosRegistrados
        {
            private val testPagosRegistrados = modelo.pagosRegistrados.test()

            @Test
            fun emite_lista_vacia_al_inicializar()
            {
                testPagosRegistrados.assertValue(listOf())
                testPagosRegistrados.assertValueCount(1)
            }

            @Nested
            inner class AgregarPago
            {
                @Test
                fun inexistente_emite_lista_con_pago_agregado_en_primera_posicion_y_retorna_empty()
                {
                    val pagosEsperados = mutableListOf<Pago>()
                    val numeroEventos = 10
                    (0 until numeroEventos).forEach {
                        val pago = Pago(Decimal(10 * it), Pago.MetodoDePago.values()[it % Pago.MetodoDePago.values().size], it.toString())
                        pagosEsperados.add(0, pago)
                        assertTrue(modelo.agregarPago(pago).esVacio)
                        testPagosRegistrados.assertValueAt(it + 1) {
                            it.zip(pagosEsperados).all { it.first == it.second }
                        }
                    }
                    testPagosRegistrados.assertValueCount(numeroEventos + 1)
                }
            }

            @Nested
            inner class EliminarPago
            {
                @Test
                fun inexistente_no_emite_nuevo_valor_y_retorna_error_correcto()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "45-6")
                    modelo.agregarPago(pagoExistente)
                    assertEquals("No existe el pago", modelo.eliminarPago(pagoInexistente).valor)
                    testPagosRegistrados.assertValueCount(2)
                }

                @Test
                fun inexistente_con_mismo_numero_de_transaccion_no_emite_nuevo_valor_y_retorna_error_correcto()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "12-3")
                    modelo.agregarPago(pagoExistente)
                    assertEquals("No existe el pago", modelo.eliminarPago(pagoInexistente).valor)
                    testPagosRegistrados.assertValueCount(2)
                }

                @Test
                fun existente_emite_nuevo_valor_con_pago_eliminado_y_retorna_empty()
                {
                    val pago = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    assertTrue(modelo.eliminarPago(pago).esVacio)
                    testPagosRegistrados.assertValueAt(2, listOf())
                    testPagosRegistrados.assertValueCount(3)
                }
            }
        }

        @Nested
        inner class PagosActuales
        {
            @Test
            fun inicia_con_lista_vacia_al_inicializar()
            {
                assertEquals(listOf(), modelo.pagoActuales)
            }

            @Nested
            inner class AgregarPago
            {
                @Test
                fun inexistente_emite_lista_con_pago_agrega_pago_en_ultima_posicion()
                {
                    val pagosEsperados = mutableListOf<Pago>()
                    val numeroEventos = 10
                    (0 until numeroEventos).forEach {
                        val pago = Pago(Decimal(10 * it), Pago.MetodoDePago.values()[it % Pago.MetodoDePago.values().size], it.toString())
                        pagosEsperados.add(pago)
                        modelo.agregarPago(pago)
                        assertTrue(modelo.pagoActuales.zip(pagosEsperados).all { it.first == it.second })
                    }
                    assertEquals(numeroEventos, modelo.pagoActuales.size)
                }
            }

            @Nested
            inner class EliminarPago
            {
                @Test
                fun inexistente_no_cambia_el_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "45-6")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    assertTrue(modelo.pagoActuales.zip(listOf(pagoExistente)).all { it.first == it.second })
                    assertEquals(1, modelo.pagoActuales.size)
                }

                @Test
                fun inexistente_con_mismo_numero_de_transaccion_no_cambia_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "12-3")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    assertTrue(modelo.pagoActuales.zip(listOf(pagoExistente)).all { it.first == it.second })
                    assertEquals(1, modelo.pagoActuales.size)
                }

                @Test
                fun existente_elimina_valor()
                {
                    val pago = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    modelo.eliminarPago(pago)
                    assertEquals(listOf(), modelo.pagoActuales)
                }
            }
        }

        @Nested
        inner class ValorFaltantePorPagar
        {
            private val observadorValorFaltantePorPagar = modelo.valorFaltantePorPagar.test()

            @Test
            fun emite_valor_dado_al_inicializar()
            {
                observadorValorFaltantePorPagar.assertValue(valorTotalAPagar)
                observadorValorFaltantePorPagar.assertValueCount(1)
            }

            @Nested
            inner class AgregarPago
            {
                @Test
                fun inexistente_emite_valor_anterior_menos_valor_para_cada_nuevo_pago()
                {
                    var valorFaltanteEsperado = valorTotalAPagar
                    val numeroEventos = 10
                    (1..numeroEventos).forEach {
                        val valorAPagar = Decimal(10 * it)
                        val pago = Pago(valorAPagar, Pago.MetodoDePago.EFECTIVO, it.toString())
                        valorFaltanteEsperado -= valorAPagar
                        modelo.agregarPago(pago)
                        observadorValorFaltantePorPagar.verificarUltimoValorEmitido(valorFaltanteEsperado)
                    }
                    observadorValorFaltantePorPagar.assertValueCount(numeroEventos + 1)
                }
            }

            @Nested
            inner class EliminarPago
            {
                @Test
                fun inexistente_no_emite_nuevo_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "45-6")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    observadorValorFaltantePorPagar.assertValueCount(2)
                }

                @Test
                fun inexistente_con_mismo_numero_de_transaccion_no_emite_nuevo_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "12-3")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    observadorValorFaltantePorPagar.assertValueCount(2)
                }

                @Test
                fun existente_emite_nuevo_valor_con_pago_eliminado()
                {
                    val pago = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    modelo.eliminarPago(pago)
                    observadorValorFaltantePorPagar.assertValueAt(2, valorTotalAPagar)
                    observadorValorFaltantePorPagar.assertValueCount(3)
                }
            }
        }

        @Nested
        inner class PuedeRegistrarPago
        {
            private val testPuedeRegistrarPago = modelo.puedeRegistrarPago.test()

            @Test
            fun emite_false_al_inicializar()
            {
                testPuedeRegistrarPago.assertValue(false)
                testPuedeRegistrarPago.assertValueCount(1)
            }

            @Nested
            inner class AgregarPago
            {
                @Test
                fun sin_alcanzar_el_valor_inicial_con_un_solo_pago_no_emite_nuevos_valores()
                {
                    val pago = Pago(Decimal(99.9999999), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    testPuedeRegistrarPago.assertValueCount(1)
                }

                @Test
                fun sin_alcanzar_el_valor_inicial_con_multiples_pagos_no_emite_nuevos_valores()
                {
                    val pagoInicial = Pago(Decimal(49.9999999), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoFinal = Pago(Decimal(50), Pago.MetodoDePago.EFECTIVO, "45-6")
                    modelo.agregarPago(pagoInicial)
                    modelo.agregarPago(pagoFinal)
                    testPuedeRegistrarPago.assertValueCount(1)
                }

                @Test
                fun alcanzando_el_valor_inicial_con_un_solo_pago_emite_true()
                {
                    val pago = Pago(valorTotalAPagar, Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    testPuedeRegistrarPago.assertValueAt(1, true)
                    testPuedeRegistrarPago.assertValueCount(2)
                }

                @Test
                fun alcanzando_el_valor_inicial_con_multiples_pagos_emite_true()
                {
                    val pagoInicial = Pago(Decimal(40), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoFinal = Pago(Decimal(60), Pago.MetodoDePago.EFECTIVO, "45-6")
                    modelo.agregarPago(pagoInicial)
                    modelo.agregarPago(pagoFinal)
                    testPuedeRegistrarPago.assertValueAt(1, true)
                    testPuedeRegistrarPago.assertValueCount(2)
                }

                @Test
                fun alcanzando_y_luego_superando_el_valor_inicial_emite_true_una_sola_vez()
                {
                    val pagoInicial = Pago(Decimal(100), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoFinal = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "45-6")
                    modelo.agregarPago(pagoInicial)
                    modelo.agregarPago(pagoFinal)
                    testPuedeRegistrarPago.assertValueAt(1, true)
                    testPuedeRegistrarPago.assertValueCount(2)
                }

                @Test
                fun superando_el_valor_inicial_con_un_solo_emite_true()
                {
                    val pago = Pago(Decimal(110), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    testPuedeRegistrarPago.assertValueAt(1, true)
                    testPuedeRegistrarPago.assertValueCount(2)
                }

                @Test
                fun superando_el_valor_inicial_con_multiples_pagos_emite_true()
                {
                    val pagoInicial = Pago(Decimal(50), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoFinal = Pago(Decimal(60), Pago.MetodoDePago.EFECTIVO, "45-6")
                    modelo.agregarPago(pagoInicial)
                    modelo.agregarPago(pagoFinal)
                    testPuedeRegistrarPago.assertValueAt(1, true)
                    testPuedeRegistrarPago.assertValueCount(2)
                }
            }

            @Nested
            inner class EliminarPago
            {
                @Test
                fun sin_haber_alcanzado_el_valor_inicial_con_un_solo_pago_no_emite_nuevos_valores()
                {
                    val pago = Pago(Decimal(99.9999999), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    modelo.eliminarPago(pago)
                    testPuedeRegistrarPago.assertValueCount(1)
                }

                @Test
                fun sin_haber_alcanzado_el_valor_inicial_con_multiples_pagos_no_emite_nuevos_valores()
                {
                    val pagoInicial = Pago(Decimal(49.9999999), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoFinal = Pago(Decimal(50), Pago.MetodoDePago.EFECTIVO, "45-6")
                    modelo.agregarPago(pagoInicial)
                    modelo.agregarPago(pagoFinal)
                    modelo.eliminarPago(pagoFinal)
                    modelo.eliminarPago(pagoInicial)
                    testPuedeRegistrarPago.assertValueCount(1)
                }

                @Test
                fun despues_de_haber_alcanzando_el_valor_inicial_con_un_solo_pago_emite_false()
                {
                    val pago = Pago(valorTotalAPagar, Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    modelo.eliminarPago(pago)
                    testPuedeRegistrarPago.assertValueAt(2, false)
                    testPuedeRegistrarPago.assertValueCount(3)
                }

                @Test
                fun despues_de_haber_alcanzando_el_valor_inicial_con_multiples_pagos_emite_false()
                {
                    val pagoInicial = Pago(Decimal(40), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoFinal = Pago(Decimal(60), Pago.MetodoDePago.EFECTIVO, "45-6")
                    modelo.agregarPago(pagoInicial)
                    modelo.agregarPago(pagoFinal)
                    modelo.eliminarPago(pagoFinal)
                    modelo.eliminarPago(pagoInicial)
                    testPuedeRegistrarPago.assertValueAt(2, false)
                    testPuedeRegistrarPago.assertValueCount(3)
                }

                @Test
                fun despues_de_haber_alcanzando_y_luego_superado_el_valor_inicial_emite_false_una_sola_vez()
                {
                    val pagoInicial = Pago(Decimal(100), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoFinal = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "45-6")
                    modelo.agregarPago(pagoInicial)
                    modelo.agregarPago(pagoFinal)
                    modelo.eliminarPago(pagoFinal)
                    modelo.eliminarPago(pagoInicial)
                    testPuedeRegistrarPago.assertValueAt(2, false)
                    testPuedeRegistrarPago.assertValueCount(3)
                }

                @Test
                fun despues_de_haber_superado_el_valor_inicial_con_un_solo_pago_emite_false()
                {
                    val pago = Pago(Decimal(110), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    modelo.eliminarPago(pago)
                    testPuedeRegistrarPago.assertValueAt(2, false)
                    testPuedeRegistrarPago.assertValueCount(3)
                }

                @Test
                fun despues_de_haber_superado_el_valor_inicial_con_multiples_pagos_emite_false()
                {
                    val pagoInicial = Pago(Decimal(50), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoFinal = Pago(Decimal(60), Pago.MetodoDePago.EFECTIVO, "45-6")
                    modelo.agregarPago(pagoInicial)
                    modelo.agregarPago(pagoFinal)
                    modelo.eliminarPago(pagoFinal)
                    modelo.eliminarPago(pagoInicial)
                    testPuedeRegistrarPago.assertValueAt(2, false)
                    testPuedeRegistrarPago.assertValueCount(3)
                }
            }
        }

        @Nested
        inner class TotalPagado
        {
            private val observadorTotalPagado = modelo.totalPagado.test()

            @Test
            fun emite_cero_al_inicializar()
            {
                observadorTotalPagado.assertValue(Decimal.CERO)
                observadorTotalPagado.assertValueCount(1)
            }

            @Nested
            inner class AgregarPago
            {
                @Test
                fun inexistente_suma_por_cada_pago_el_valor_pagado()
                {
                    var valorFaltanteEsperado = Decimal.CERO
                    val numeroEventos = 10
                    (1..numeroEventos).forEach {
                        val valorAPagar = Decimal(10 * it)
                        val pago = Pago(valorAPagar, Pago.MetodoDePago.EFECTIVO, it.toString())
                        valorFaltanteEsperado += valorAPagar
                        modelo.agregarPago(pago)
                        observadorTotalPagado.verificarUltimoValorEmitido(valorFaltanteEsperado)
                    }
                    observadorTotalPagado.assertValueCount(numeroEventos + 1)
                }
            }

            @Nested
            inner class EliminarPago
            {
                @Test
                fun inexistente_no_emite_nuevo_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "45-6")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    observadorTotalPagado.assertValueCount(2)
                }

                @Test
                fun inexistente_con_mismo_numero_de_transaccion_no_emite_nuevo_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "12-3")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    observadorTotalPagado.assertValueCount(2)
                }

                @Test
                fun existente_emite_nuevo_valor_con_pago_eliminado()
                {
                    val pago = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    modelo.eliminarPago(pago)
                    observadorTotalPagado.assertValueCount(3)
                    observadorTotalPagado.assertValuesOnly(Decimal.CERO, Decimal(10), Decimal.CERO)
                }
            }
        }
    }

    @Nested
    inner class AlInicializarConValorCero
    {
        // Solo se prueba la inicializacion, los valores emitidos al llamar los metodos ya se probaron al empezar en un valor positivo
        private val valorTotalAPagar = Decimal.CERO
        private val modelo: PagosDeUnaCompraUI = PagosDeUnaCompra(Observable.just(valorTotalAPagar))

        @Nested
        inner class PagosRegistrados
        {
            private val testPagosRegistrados = modelo.pagosRegistrados.test()

            @Test
            fun emite_lista_vacia_al_inicializar()
            {
                testPagosRegistrados.assertValue(listOf())
                testPagosRegistrados.assertValueCount(1)
            }
        }

        @Nested
        inner class PagosActuales
        {
            @Test
            fun inicia_con_lista_vacia_al_inicializar()
            {
                assertEquals(listOf(), modelo.pagoActuales)
            }
        }

        @Nested
        inner class ValorFaltantePorPagar
        {
            private val testValorFaltantePorPagar = modelo.valorFaltantePorPagar.test()

            @Test
            fun emite_cero_al_inicializar()
            {
                testValorFaltantePorPagar.assertValue(Decimal.CERO)
                testValorFaltantePorPagar.assertValueCount(1)
            }
        }

        @Nested
        inner class PuedeRegistrarPago
        {
            private val testPuedeRegistrarPago = modelo.puedeRegistrarPago.test()

            @Test
            fun emite_true_al_inicializar()
            {
                testPuedeRegistrarPago.assertValue(true)
                testPuedeRegistrarPago.assertValueCount(1)
            }
        }

        @Nested
        inner class TotalPagado
        {
            private val observadorTotalPagado = modelo.totalPagado.test()

            @Test
            fun emite_cero_al_inicializar()
            {
                observadorTotalPagado.assertValue(Decimal.CERO)
                observadorTotalPagado.assertValueCount(1)
            }
        }
    }

    @Nested
    inner class AlInicializarConValorNegativo
    {
        // Solo se prueba la inicializacion, los valores emitidos al llamar los metodos ya se probaron al empezar en un valor positivo
        private val valorTotalAPagar = Decimal(-100)
        private val modelo: PagosDeUnaCompraUI = PagosDeUnaCompra(Observable.just(valorTotalAPagar))

        @Nested
        inner class PagosRegistrados
        {
            private val testPagosRegistrados = modelo.pagosRegistrados.test()

            @Test
            fun emite_lista_vacia_al_inicializar()
            {
                testPagosRegistrados.assertValue(listOf())
                testPagosRegistrados.assertValueCount(1)
            }
        }

        @Nested
        inner class PagosActuales
        {
            @Test
            fun inicia_con_lista_vacia_al_inicializar()
            {
                assertEquals(listOf(), modelo.pagoActuales)
            }
        }

        @Nested
        inner class ValorFaltantePorPagar
        {
            private val testValorFaltantePorPagar = modelo.valorFaltantePorPagar.test()

            @Test
            fun emite_valor_dado_al_inicializar()
            {
                testValorFaltantePorPagar.assertValue(valorTotalAPagar)
                testValorFaltantePorPagar.assertValueCount(1)
            }
        }

        @Nested
        inner class PuedeRegistrarPago
        {
            private val testPuedeRegistrarPago = modelo.puedeRegistrarPago.test()

            @Test
            fun emite_true_al_inicializar()
            {
                testPuedeRegistrarPago.assertValue(true)
                testPuedeRegistrarPago.assertValueCount(1)
            }
        }

        @Nested
        inner class TotalPagado
        {
            private val observadorTotalPagado = modelo.totalPagado.test()

            @Test
            fun emite_cero_al_inicializar()
            {
                observadorTotalPagado.assertValue(Decimal.CERO)
                observadorTotalPagado.assertValueCount(1)
            }

            @Nested
            inner class AgregarPago
            {
                @Test
                fun inexistente_suma_por_cada_pago_el_valor_pagado()
                {
                    var valorFaltanteEsperado = Decimal.CERO
                    val numeroEventos = 10
                    (1..numeroEventos).forEach {
                        val valorAPagar = Decimal(10 * it)
                        val pago = Pago(valorAPagar, Pago.MetodoDePago.EFECTIVO, it.toString())
                        valorFaltanteEsperado += valorAPagar
                        modelo.agregarPago(pago)
                        observadorTotalPagado.verificarUltimoValorEmitido(valorFaltanteEsperado)
                    }
                    observadorTotalPagado.assertValueCount(numeroEventos + 1)
                }
            }

            @Nested
            inner class EliminarPago
            {
                @Test
                fun inexistente_no_emite_nuevo_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "45-6")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    observadorTotalPagado.assertValueCount(2)
                }

                @Test
                fun inexistente_con_mismo_numero_de_transaccion_no_emite_nuevo_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "12-3")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    observadorTotalPagado.assertValueCount(2)
                }

                @Test
                fun existente_emite_nuevo_valor_con_pago_eliminado()
                {
                    val pago = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    modelo.eliminarPago(pago)
                    observadorTotalPagado.assertValuesOnly(Decimal.CERO, Decimal(10), Decimal.CERO)
                }
            }
        }
    }

    @Nested
    inner class AlCambiarValorTotalAPagar
    {
        private val eventosCambioTotalAPagar = BehaviorSubject.createDefault(Decimal(100))
        private val modelo: PagosDeUnaCompraUI = PagosDeUnaCompra(eventosCambioTotalAPagar)

        @Nested
        inner class ValorFaltantePorPagar
        {
            private val nuevoValorTotal = Decimal(123456)
            private val observadorValorFaltantePorPagar = modelo.valorFaltantePorPagar.test()

            @BeforeEach
            fun emitirNuevoValor()
            {
                eventosCambioTotalAPagar.onNext(nuevoValorTotal)
            }

            @Test
            fun al_cambiar_de_valor_se_emite_valor_correcto()
            {
                observadorValorFaltantePorPagar.assertValueCount(2)
                observadorValorFaltantePorPagar.assertValuesOnly(Decimal(100), nuevoValorTotal)
            }

            @Nested
            inner class AgregarPago
            {
                @Test
                fun inexistente_emite_valor_anterior_menos_valor_para_cada_nuevo_pago()
                {
                    var valorFaltanteEsperado = nuevoValorTotal
                    val numeroEventos = 10
                    (1..numeroEventos).forEach {
                        val valorAPagar = Decimal(10 * it)
                        val pago = Pago(valorAPagar, Pago.MetodoDePago.EFECTIVO, it.toString())
                        valorFaltanteEsperado -= valorAPagar
                        modelo.agregarPago(pago)
                        observadorValorFaltantePorPagar.verificarUltimoValorEmitido(valorFaltanteEsperado)
                    }
                    observadorValorFaltantePorPagar.assertValueCount(numeroEventos + 2)
                }
            }

            @Nested
            inner class EliminarPago
            {
                @Test
                fun inexistente_no_emite_nuevo_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "45-6")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    observadorValorFaltantePorPagar.assertValueCount(3)
                }

                @Test
                fun inexistente_con_mismo_numero_de_transaccion_no_emite_nuevo_valor()
                {
                    val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "12-3")
                    modelo.agregarPago(pagoExistente)
                    modelo.eliminarPago(pagoInexistente)
                    observadorValorFaltantePorPagar.assertValueCount(3)
                }

                @Test
                fun existente_emite_nuevo_valor_con_pago_eliminado()
                {
                    val pago = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                    modelo.agregarPago(pago)
                    modelo.eliminarPago(pago)
                    observadorValorFaltantePorPagar.assertValueCount(4)
                    observadorValorFaltantePorPagar.verificarUltimoValorEmitido(nuevoValorTotal)
                }
            }
        }

        @Nested
        inner class TotalPagado
        {
            private val nuevoValorTotal = Decimal(123456)
            private val observadorTotalPagado = modelo.totalPagado.test()

            @Nested
            inner class SinPagosPrevios
            {
                @BeforeEach
                fun emitirNuevoValor()
                {
                    eventosCambioTotalAPagar.onNext(nuevoValorTotal)
                }

                @Test
                fun al_cambiar_de_valor_se_emite_valor_correcto()
                {
                    observadorTotalPagado.assertValueCount(1)
                    observadorTotalPagado.assertValuesOnly(Decimal.CERO)
                }

                @Nested
                inner class AgregarPago
                {
                    @Test
                    fun inexistente_suma_por_cada_pago_el_valor_pagado()
                    {
                        var valorFaltanteEsperado = Decimal.CERO
                        val numeroEventos = 10
                        (1..numeroEventos).forEach {
                            val valorAPagar = Decimal(10 * it)
                            val pago = Pago(valorAPagar, Pago.MetodoDePago.EFECTIVO, it.toString())
                            valorFaltanteEsperado += valorAPagar
                            modelo.agregarPago(pago)
                            observadorTotalPagado.verificarUltimoValorEmitido(valorFaltanteEsperado)
                        }
                        observadorTotalPagado.assertValueCount(numeroEventos + 1)
                    }
                }

                @Nested
                inner class EliminarPago
                {
                    @Test
                    fun inexistente_no_emite_nuevo_valor()
                    {
                        val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                        val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "45-6")
                        modelo.agregarPago(pagoExistente)
                        modelo.eliminarPago(pagoInexistente)
                        observadorTotalPagado.assertValueCount(2)
                    }

                    @Test
                    fun inexistente_con_mismo_numero_de_transaccion_no_emite_nuevo_valor()
                    {
                        val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                        val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "12-3")
                        modelo.agregarPago(pagoExistente)
                        modelo.eliminarPago(pagoInexistente)
                        observadorTotalPagado.assertValueCount(2)
                    }

                    @Test
                    fun existente_emite_nuevo_valor_con_pago_eliminado()
                    {
                        val pago = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                        modelo.agregarPago(pago)
                        modelo.eliminarPago(pago)
                        observadorTotalPagado.assertValuesOnly(Decimal.CERO, Decimal(10), Decimal.CERO)
                    }
                }
            }

            @Nested
            inner class ConPagosPrevios
            {
                private val pagoPrevio = Pago(Decimal(102003004), Pago.MetodoDePago.EFECTIVO, "pago previamente hecho")

                @BeforeEach
                fun registrarPagoYEmitirNuevoTotal()
                {
                    modelo.agregarPago(pagoPrevio)
                    eventosCambioTotalAPagar.onNext(nuevoValorTotal)
                }

                @Test
                fun al_cambiar_de_valor_se_emite_valor_correcto()
                {
                    observadorTotalPagado.assertValueCount(2)
                    observadorTotalPagado.assertValuesOnly(Decimal.CERO, pagoPrevio.valorPagado)
                }

                @Nested
                inner class AgregarPago
                {
                    @Test
                    fun inexistente_suma_por_cada_pago_el_valor_pagado()
                    {
                        var valorFaltanteEsperado = pagoPrevio.valorPagado
                        val numeroEventos = 10
                        (1..numeroEventos).forEach {
                            val valorAPagar = Decimal(10 * it)
                            val pago = Pago(valorAPagar, Pago.MetodoDePago.EFECTIVO, it.toString())
                            valorFaltanteEsperado += valorAPagar
                            modelo.agregarPago(pago)
                            observadorTotalPagado.verificarUltimoValorEmitido(valorFaltanteEsperado)
                        }
                        observadorTotalPagado.assertValueCount(numeroEventos + 2)
                    }
                }

                @Nested
                inner class EliminarPago
                {
                    @Test
                    fun inexistente_no_emite_nuevo_valor()
                    {
                        val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                        val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "45-6")
                        modelo.agregarPago(pagoExistente)
                        modelo.eliminarPago(pagoInexistente)
                        observadorTotalPagado.assertValueCount(3)
                    }

                    @Test
                    fun inexistente_con_mismo_numero_de_transaccion_no_emite_nuevo_valor()
                    {
                        val pagoExistente = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                        val pagoInexistente = Pago(Decimal(100), Pago.MetodoDePago.TARJETA_DEBITO, "12-3")
                        modelo.agregarPago(pagoExistente)
                        modelo.eliminarPago(pagoInexistente)
                        observadorTotalPagado.assertValueCount(3)
                    }

                    @Test
                    fun existente_emite_nuevo_valor_con_pago_eliminado()
                    {
                        val pago = Pago(Decimal(10), Pago.MetodoDePago.EFECTIVO, "12-3")
                        modelo.agregarPago(pago)
                        modelo.eliminarPago(pago)
                        observadorTotalPagado.assertValuesOnly(
                                Decimal.CERO,
                                pagoPrevio.valorPagado,
                                pagoPrevio.valorPagado + pago.valorPagado,
                                pagoPrevio.valorPagado
                                                              )
                    }
                }
            }
        }
    }
}