package co.smartobjects.ui.modelos.pagos

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoSeleccionCreditosUI
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.sumar
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import kotlin.test.assertEquals


internal class TotalAPagarSegunPersonasPruebas : PruebasModelosRxBase()
{
    @Test
    fun los_modelos_hijos_contienen_solo_la_lista_filtrable()
    {
        val listadoPersonas =
                listOf(
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(crearMockCreditoFondo(13, 17, 19), crearMockCreditoFondo(23, 29, 31)),
                                listOf(crearMockCreditoPaquete(37, 41, 43), crearMockCreditoPaquete(47, 53, 59)),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1))
                                                                              )
                      )

        val modelo = TotalAPagarSegunPersonas(listadoPersonas)

        assertEquals(1, modelo.modelosHijos.size)
        assertEquals(modelo.modelosHijos.first(), modelo.listadoDePersonasConCreditos)
    }

    @Test
    fun el_gran_total_es_la_suma_de_todos_los_valores_pagados_de_los_creditos()
    {
        val listadoPersonas =
                sequenceOf(
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(crearMockCreditoFondo(13, 17, 19), crearMockCreditoFondo(23, 29, 31)),
                                listOf(crearMockCreditoPaquete(37, 41, 43), crearMockCreditoPaquete(47, 53, 59)),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1))
                                                                              ),
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(crearMockCreditoFondo(61, 67, 71), crearMockCreditoFondo(73, 79, 83)),
                                listOf(crearMockCreditoPaquete(89, 97, 101), crearMockCreditoPaquete(103, 107, 109)),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1)))
                          )

        val modelo = TotalAPagarSegunPersonas(listadoPersonas.toList())

        assertEquals(totalizarCreditosAPagar(listadoPersonas, { it.valorPagado }, { it.valorPagado }), modelo.granTotal)
    }

    @Nested
    inner class ConCreditosAPagarDeFondoYPaquete
    {
        // Como precondición para algunas pruebas se asume tamaño de 2
        private val listadoPersonas =
                listOf(
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(crearMockCreditoFondo(13, 17, 19), crearMockCreditoFondo(23, 29, 31)),
                                listOf(crearMockCreditoPaquete(37, 41, 43), crearMockCreditoPaquete(47, 53, 59)),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1))
                                                                              ),
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(crearMockCreditoFondo(61, 67, 71), crearMockCreditoFondo(73, 79, 83)),
                                listOf(crearMockCreditoPaquete(89, 97, 101), crearMockCreditoPaquete(103, 107, 109)),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1)))
                      )

        private val modelo: TotalAPagarSegunPersonasUI = TotalAPagarSegunPersonas(listadoPersonas)

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun a_total_sin_impuesto_se_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }

            @Test
            fun a_impuesto_total_se_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }

            @Test
            fun a_total_se_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }
        }

        @Nested
        inner class AlDeseleccionarPersona
        {
            @Test
            fun total_sin_impuesto_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun impuesto_total_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun total_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }
        }

        @Nested
        inner class AlDeseleccionarYReseleccinoarPersona
        {
            @Test
            fun total_sin_impuesto_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun impuesto_total_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun total_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }
        }
    }

    @Nested
    inner class ConCreditosAPagarDeFondoPeroNoDePaquete
    {
        // Como precondición para algunas pruebas se asume tamaño de 2
        private val listadoPersonas =
                listOf(
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(crearMockCreditoFondo(13, 17, 19), crearMockCreditoFondo(23, 29, 31)),
                                listOf(),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1))
                                                                              ),
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(crearMockCreditoFondo(61, 67, 71), crearMockCreditoFondo(73, 79, 83)),
                                listOf(),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1)))
                      )

        private val modelo: TotalAPagarSegunPersonasUI = TotalAPagarSegunPersonas(listadoPersonas)

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun a_total_sin_impuesto_se_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }

            @Test
            fun a_impuesto_total_se_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }

            @Test
            fun a_total_se_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }
        }

        @Nested
        inner class AlDeseleccionarPersona
        {
            @Test
            fun total_sin_impuesto_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun impuesto_total_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun total_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }
        }

        @Nested
        inner class AlDeseleccionarYReseleccinoarPersona
        {
            @Test
            fun total_sin_impuesto_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun impuesto_total_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun total_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }
        }
    }

    @Nested
    inner class ConCreditosAPagarDePaquetePeroNoDeFondo
    {
        // Como precondición para algunas pruebas se asume tamaño de 2
        private val listadoPersonas =
                listOf(
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(),
                                listOf(crearMockCreditoPaquete(37, 41, 43), crearMockCreditoPaquete(47, 53, 59)),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1))
                                                                              ),
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(),
                                listOf(crearMockCreditoPaquete(89, 97, 101), crearMockCreditoPaquete(103, 107, 109)),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1)))
                      )

        private val modelo: TotalAPagarSegunPersonasUI = TotalAPagarSegunPersonas(listadoPersonas)

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun a_total_sin_impuesto_se_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }

            @Test
            fun a_impuesto_total_se_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }

            @Test
            fun a_total_se_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }
        }

        @Nested
        inner class AlDeseleccionarPersona
        {
            @Test
            fun total_sin_impuesto_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun impuesto_total_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun total_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }
        }

        @Nested
        inner class AlDeseleccionarYReseleccinoarPersona
        {
            @Test
            fun total_sin_impuesto_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun impuesto_total_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun total_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }
        }
    }

    @Nested
    inner class SinCreditosAPagarDeFondoYPaquete
    {
        // Como precondición para algunas pruebas se asume tamaño de 2
        private val listadoPersonas =
                listOf(
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(),
                                listOf(),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1))
                                                                              ),
                        ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                PersonaConGrupoCliente(mockConDefaultAnswer(Persona::class.java), null),
                                listOf(),
                                listOf(),
                                listOf(crearMockCreditoFondo(1, 1, 1), crearMockCreditoFondo(1, 1, 1)),
                                listOf(crearMockCreditoPaquete(1, 1, 1), crearMockCreditoPaquete(1, 1, 1)))
                      )

        private val modelo: TotalAPagarSegunPersonasUI = TotalAPagarSegunPersonas(listadoPersonas)

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun a_total_sin_impuesto_se_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }

            @Test
            fun a_impuesto_total_se_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }

            @Test
            fun a_total_se_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                observador.assertValueCount(1)
                observador.assertValuesOnly(valorEsperado)
            }
        }

        @Nested
        inner class AlDeseleccionarPersona
        {
            @Test
            fun total_sin_impuesto_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun impuesto_total_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun total_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence().take(1),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }
        }

        @Nested
        inner class AlDeseleccionarYReseleccinoarPersona
        {
            @Test
            fun total_sin_impuesto_emite_la_suma_de_los_valores_pagados_sin_impuesto_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagadoSinImpuesto },
                                { it.valorPagadoSinImpuesto }
                                               )

                val observador = modelo.totalSinImpuesto.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun impuesto_total_emite_la_suma_de_los_valores_impuesto_pagado_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorImpuestoPagado },
                                { it.valorImpuestoPagado }
                                               )

                val observador = modelo.impuestoTotal.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun total_emite_la_suma_de_los_valores_pagados_de_todos_los_creditos_a_pagar_de_los_seleccionados()
            {
                val valorEsperado =
                        totalizarCreditosAPagar(
                                listadoPersonas.asSequence(),
                                { it.valorPagado },
                                { it.valorPagado }
                                               )

                val observador = modelo.total.test()

                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].deseleccionar()
                modelo.listadoDePersonasConCreditos.itemsFiltrables[1].seleccionar()

                observador.assertValueCount(3)
                observador.verificarUltimoValorEmitido(valorEsperado)
            }
        }
    }

    private fun totalizarCreditosAPagar(
            lista: Sequence<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>,
            extractorParaFondos: (CreditoFondo) -> Decimal,
            extractorParaPaquetes: (CreditoPaquete) -> Decimal
                                       )
            : Decimal
    {
        return lista.map {
            it.creditosFondoAPagar.asSequence().map { it.creditoAsociado }.sumar(extractorParaFondos) +
            it.creditosPaqueteAPagar.asSequence().map { it.creditoAsociado }.sumar(extractorParaPaquetes)
        }.sumar()
    }

    private fun crearMockCreditoFondo(valorPagadoSinImpuesto: Int, valorImpuestoPagado: Int, valorPagado: Int)
            : CreditoFondoConNombre
    {
        return CreditoFondoConNombre(
                "no importa",
                spy(crearCreditoFondo(10, 1, true)).also {
                    doReturn(Decimal(valorPagadoSinImpuesto)).`when`(it).valorPagadoSinImpuesto
                    doReturn(Decimal(valorImpuestoPagado)).`when`(it).valorImpuestoPagado
                    doReturn(Decimal(valorPagado)).`when`(it).valorPagado
                }
                                    )
    }

    private fun crearMockCreditoPaquete(valorPagadoSinImpuesto: Int, valorImpuestoPagado: Int, valorPagado: Int)
            : CreditoPaqueteConNombre
    {
        return CreditoPaqueteConNombre(
                "no importa",
                1,
                spy(crearCreditoPaquete(30, 1, true)).also {
                    doReturn(Decimal(valorPagadoSinImpuesto)).`when`(it).valorPagadoSinImpuesto
                    doReturn(Decimal(valorImpuestoPagado)).`when`(it).valorImpuestoPagado
                    doReturn(Decimal(valorPagado)).`when`(it).valorPagado
                }
                                      )
    }
}