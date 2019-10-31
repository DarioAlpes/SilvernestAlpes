package co.smartobjects.ui.modelos.pagos

import co.smartobjects.entidades.operativas.compras.*
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.logica.fondos.ProveedorNombresYPreciosPorDefectoCompletosFondos
import co.smartobjects.logica.fondos.libros.MapeadorReglasANombresRestricciones
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.operativas.compras.ComprasAPI
import co.smartobjects.red.modelos.ErrorDePeticion
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoSeleccionCreditosUI
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.mockito.Mockito.*
import org.threeten.bp.ZonedDateTime
import java.io.IOException
import kotlin.test.assertEquals


internal class ProcesoPagarPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_CLIENTE = 1L
        private const val NOMBRE_USUARIO = "un usuario"
    }

    private val mockContextoDeSesion = mockConDefaultAnswer(ContextoDeSesion::class.java).also {
        doReturn(ID_CLIENTE).`when`(it).idCliente
        doReturn(NOMBRE_USUARIO).`when`(it).nombreDeUsuario
    }
    private val mockTotalAPagarSegunPersonas = mockConDefaultAnswer(TotalAPagarSegunPersonasUI::class.java).also {
        doNothing().`when`(it).finalizarProceso()
    }
    private val mockPagosDeUnaCompra = mockConDefaultAnswer(PagosDeUnaCompraUI::class.java).also {
        doNothing().`when`(it).finalizarProceso()
    }
    private val mockApiCompras = mockConDefaultAnswer(ComprasAPI::class.java)
    private val mockMapeadorReglasANombresRestricciones = mockConDefaultAnswer(MapeadorReglasANombresRestricciones::class.java)
    private val mockProveedorNombresYPreciosPorDefectoCompletosFondos = mockConDefaultAnswer(ProveedorNombresYPreciosPorDefectoCompletosFondos::class.java)

    @Nested
    inner class AlInicializar
    {
        @BeforeEach
        fun mockearDependencias()
        {
            doReturn(Observable.just(Decimal.DIEZ)).`when`(mockPagosDeUnaCompra).totalPagado
            doReturn(Decimal.DIEZ).`when`(mockTotalAPagarSegunPersonas).granTotal
        }

        @Test
        fun el_estado_es_sin_crear_compra()
        {
            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                    Schedulers.trampoline())

            val observadorDePrueba = modelo.estado.test()

            observadorDePrueba.assertValue(ProcesoPagarUI.Estado.SIN_CREAR_COMPRA)
        }

        @Test
        fun el_estado_el_mensaje_de_error_no_ha_emitido()
        {
            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                    Schedulers.trampoline())

            val observadorDePrueba = modelo.mensajesDeError.test()

            observadorDePrueba.assertEmpty()
        }

        @Test
        fun el_resumen_de_creditos_fondo_a_pagar_emitido_es_correcto()
        {
            val idsFondosComprasVsReglasDummiesAplicadas =
                    mapOf(
                            100L to listOf("Regla 1A", "Regla 1B"),
                            200L to listOf("Regla 2A"),
                            300L to listOf("Regla 3A", "Regla 3B", "Regla 3C")
                         )

            val creditosAProcesar =
                    listOf(
                            crearCreditosAProcesarSoloConCreditosFondosAPagar(
                                    listOf(
                                            CreditoFondoConNombre(
                                                    "Ubicacion 100 Cant. 2 Conjunto de Reglas 1",
                                                    crearCreditoFondo(1, 1).copiar(idUbicacionCompra = 100)
                                                                 ),
                                            CreditoFondoConNombre(
                                                    "Ubicacion 200 Cant. 8 Conjunto de Reglas 2",
                                                    crearCreditoFondo(7, 1).copiar(idUbicacionCompra = 200)
                                                                 )
                                          )
                                                                             ),

                            crearCreditosAProcesarSoloConCreditosFondosAPagar(
                                    listOf(
                                            CreditoFondoConNombre(
                                                    "Ubicacion 100 Cant. 2 Conjunto de Reglas 1",
                                                    crearCreditoFondo(1, 1).copiar(idUbicacionCompra = 100)
                                                                 ),
                                            CreditoFondoConNombre(
                                                    "Ubicacion 300 Cant. 11 Conjunto de Reglas 3",
                                                    crearCreditoFondo(10, 1).copiar(idUbicacionCompra = 300)
                                                                 )
                                          )
                                                                             )
                          )

            val mockListadoPersonasConCreditos =
                    mockConDefaultAnswer(ListaFiltrableUI::class.java).also {
                        doReturn(creditosAProcesar).`when`(it).items
                    } as ListaFiltrableUI<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>

            doReturn(mockListadoPersonasConCreditos).`when`(mockTotalAPagarSegunPersonas).listadoDePersonasConCreditos

            doAnswer {
                idsFondosComprasVsReglasDummiesAplicadas[it.getArgument<Long>(1)]!!
            }.`when`(mockMapeadorReglasANombresRestricciones).mapear(anyLong(), cualquiera(), cualquiera(), cualquiera())

            val valoresEsperados =
                    listOf(
                            ProcesoPagarUI.ResumenPagoProducto(Decimal(4), "Ubicacion 100 Cant. 2 Conjunto de Reglas 1", idsFondosComprasVsReglasDummiesAplicadas[100]!!),
                            ProcesoPagarUI.ResumenPagoProducto(Decimal(8), "Ubicacion 200 Cant. 8 Conjunto de Reglas 2", idsFondosComprasVsReglasDummiesAplicadas[200]!!),
                            ProcesoPagarUI.ResumenPagoProducto(Decimal(11), "Ubicacion 300 Cant. 11 Conjunto de Reglas 3", idsFondosComprasVsReglasDummiesAplicadas[300]!!)
                          )

            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                    Schedulers.trampoline())

            val observadorDePrueba = modelo.resumenDeCreditosAPagar.test()

            observadorDePrueba.assertResult(valoresEsperados)
        }

        @Test
        fun el_resumen_de_creditos_paquete_a_pagar_emitido_es_correcto()
        {
            val idsPaquetesVsReglasDummiesAplicadas =
                    mapOf(
                            100L to listOf("Regla 1A", "Regla 1B"),
                            200L to listOf("Regla 2A"),
                            300L to listOf("Regla 3A", "Regla 3B", "Regla 3C")
                         )

            val idsFondoVsNombre = mutableMapOf<Long, String>()

            val creditosAProcesar =
                    listOf(
                            crearCreditosAProcesarSoloConCreditosPaqueteAPagar(
                                    listOf(
                                            CreditoPaqueteConNombre(
                                                    "Ubicación 100 Conjunto de Reglas 1",
                                                    1,
                                                    crearCreditoPaquete(1, 1).actualizarIdUbicacionCreditos(100).also {
                                                        it.creditosFondos.forEachIndexed { i, creditoFondo ->
                                                            idsFondoVsNombre[creditoFondo.idFondoComprado] = "Fondo $i Paquete 1 Reglas 1"
                                                        }
                                                    }
                                                                   ),
                                            CreditoPaqueteConNombre(
                                                    "Ubicación 200 Conjunto de Reglas 2",
                                                    1,
                                                    crearCreditoPaquete(5, 1).actualizarIdUbicacionCreditos(200).also {
                                                        it.creditosFondos.forEachIndexed { i, creditoFondo ->
                                                            idsFondoVsNombre[creditoFondo.idFondoComprado] = "Fondo $i Paquete 5 Reglas 2"
                                                        }
                                                    }
                                                                   )
                                          )
                                                                              ),

                            crearCreditosAProcesarSoloConCreditosPaqueteAPagar(
                                    listOf(
                                            CreditoPaqueteConNombre(
                                                    "Ubicación 100 Conjunto de Reglas 1",
                                                    1,
                                                    crearCreditoPaquete(1, 1).actualizarIdUbicacionCreditos(100).also {
                                                        it.creditosFondos.forEachIndexed { i, creditoFondo ->
                                                            idsFondoVsNombre[creditoFondo.idFondoComprado] = "Fondo $i Paquete 1 Reglas 1"
                                                        }
                                                    }
                                                                   ),
                                            CreditoPaqueteConNombre(
                                                    "Ubicación 300 Conjunto de Reglas 3",
                                                    1,
                                                    crearCreditoPaquete(7, 1).actualizarIdUbicacionCreditos(300).also {
                                                        it.creditosFondos.forEachIndexed { i, creditoFondo ->
                                                            idsFondoVsNombre[creditoFondo.idFondoComprado] = "Fondo $i Paquete 7 Reglas 3"
                                                        }
                                                    }
                                                                   )
                                          )
                                                                              )
                          )

            doAnswer { idsFondoVsNombre[it.getArgument<Long>(0)] }
                .`when`(mockProveedorNombresYPreciosPorDefectoCompletosFondos)
                .darNombreFondoSegunId(anyLong())

            val mockListadoPersonasConCreditos =
                    mockConDefaultAnswer(ListaFiltrableUI::class.java).also {
                        doReturn(creditosAProcesar).`when`(it).items
                    } as ListaFiltrableUI<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>

            doReturn(mockListadoPersonasConCreditos).`when`(mockTotalAPagarSegunPersonas).listadoDePersonasConCreditos

            doAnswer {
                idsPaquetesVsReglasDummiesAplicadas[it.getArgument<Long>(1)]!!
            }.`when`(mockMapeadorReglasANombresRestricciones).mapear(anyLong(), cualquiera(), cualquiera(), cualquiera())

            val creditosPaquetePrimeraPersona = creditosAProcesar.first().creditosPaqueteAPagar
            val creditosPaqueteSegundaPersona = creditosAProcesar.last().creditosPaqueteAPagar

            val valoresEsperados =
                    listOf(
                            ProcesoPagarUI.ResumenPagoProducto(
                                    creditosPaquetePrimeraPersona.first().creditoAsociado.creditosFondos.first().cantidad * 2,
                                    "Fondo 0 Paquete 1 Reglas 1",
                                    idsPaquetesVsReglasDummiesAplicadas[100]!!
                                                              ),
                            ProcesoPagarUI.ResumenPagoProducto(
                                    creditosPaquetePrimeraPersona.first().creditoAsociado.creditosFondos.last().cantidad * 2,
                                    "Fondo 1 Paquete 1 Reglas 1",
                                    idsPaquetesVsReglasDummiesAplicadas[100]!!
                                                              ),
                            ProcesoPagarUI.ResumenPagoProducto(
                                    creditosPaquetePrimeraPersona.last().creditoAsociado.creditosFondos.first().cantidad,
                                    "Fondo 0 Paquete 5 Reglas 2",
                                    idsPaquetesVsReglasDummiesAplicadas[200]!!
                                                              ),
                            ProcesoPagarUI.ResumenPagoProducto(
                                    creditosPaquetePrimeraPersona.last().creditoAsociado.creditosFondos.last().cantidad,
                                    "Fondo 1 Paquete 5 Reglas 2",
                                    idsPaquetesVsReglasDummiesAplicadas[200]!!
                                                              ),
                            ProcesoPagarUI.ResumenPagoProducto(
                                    creditosPaqueteSegundaPersona.last().creditoAsociado.creditosFondos.first().cantidad,
                                    "Fondo 0 Paquete 7 Reglas 3",
                                    idsPaquetesVsReglasDummiesAplicadas[300]!!
                                                              ),
                            ProcesoPagarUI.ResumenPagoProducto(
                                    creditosPaqueteSegundaPersona.last().creditoAsociado.creditosFondos.last().cantidad,
                                    "Fondo 1 Paquete 7 Reglas 3",
                                    idsPaquetesVsReglasDummiesAplicadas[300]!!
                                                              )
                          )

            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                    Schedulers.trampoline())

            val observadorDePrueba = modelo.resumenDeCreditosAPagar.test()

            observadorDePrueba.assertResult(valoresEsperados)
        }

        private fun crearCreditosAProcesarSoloConCreditosFondosAPagar(creditosFondoAPagar: List<CreditoFondoConNombre>)
                : ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar
        {
            return ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                    mockConDefaultAnswer(PersonaConGrupoCliente::class.java),
                    creditosFondoAPagar,
                    listOf(),
                    listOf(),
                    listOf()
                                                                         )
        }

        private fun crearCreditosAProcesarSoloConCreditosPaqueteAPagar(creditosPaqueteAPagar: List<CreditoPaqueteConNombre>)
                : ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar
        {
            return ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                    mockConDefaultAnswer(PersonaConGrupoCliente::class.java),
                    listOf(),
                    creditosPaqueteAPagar,
                    listOf(),
                    listOf()
                                                                         )
        }

        private fun CreditoPaquete.actualizarIdUbicacionCreditos(idUbicacion: Long): CreditoPaquete
        {
            return copiar(creditosFondos = creditosFondos.map { it.copiar(idUbicacionCompra = idUbicacion) })
        }
    }

    @Nested
    inner class PuedePagar
    {
        @BeforeEach
        fun mockearApi()
        {
            val mockCompra = mockConDefaultAnswer(Compra::class.java).also {
                doReturn("id que no importa").`when`(it).id
            }

            doReturn(RespuestaIndividual.Exitosa(mockCompra))
                .`when`(mockApiCompras)
                .actualizar(anyString(), cualquiera())

            doReturn(RespuestaVacia.Error.Timeout)
                .`when`(mockApiCompras)
                .actualizarCampos(anyString(), cualquiera())
        }

        @Test
        fun emite_true_si_el_total_pagado_es_igual_al_total_a_pagar()
        {
            doReturn(Observable.just(Decimal.DIEZ)).`when`(mockPagosDeUnaCompra).totalPagado
            doReturn(Decimal.DIEZ).`when`(mockTotalAPagarSegunPersonas).granTotal

            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos)
                                     )
            val observadorPrueba = modelo.puedePagar.test()

            observadorPrueba.assertValue(true)
        }

        @Test
        fun emite_true_si_el_total_pagado_es_mayor_al_total_a_pagar()
        {
            val granTotal = Decimal.DIEZ
            doReturn(Observable.just(granTotal * 10)).`when`(mockPagosDeUnaCompra).totalPagado
            doReturn(granTotal).`when`(mockTotalAPagarSegunPersonas).granTotal

            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos)
                                     )
            val observadorPrueba = modelo.puedePagar.test()

            observadorPrueba.assertValue(true)
        }

        @Test
        fun emite_false_si_el_total_pagado_es_menor_al_total_a_pagar()
        {
            val totalPagado = Decimal.UNO
            doReturn(Observable.just(totalPagado)).`when`(mockPagosDeUnaCompra).totalPagado
            doReturn(totalPagado * 10).`when`(mockTotalAPagarSegunPersonas).granTotal

            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos)
                                     )
            val observadorPrueba = modelo.puedePagar.test()

            observadorPrueba.assertValue(false)
        }

        @Test
        fun emite_valor_correcto_cuando_cambia_el_total_pagado()
        {
            val eventosTotalPagado = BehaviorSubject.createDefault(Decimal.UNO)
            doReturn(eventosTotalPagado.hide()).`when`(mockPagosDeUnaCompra).totalPagado
            doReturn(Decimal.UNO).`when`(mockTotalAPagarSegunPersonas).granTotal

            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos)
                                     )
            val observadorPrueba = modelo.puedePagar.test()

            observadorPrueba.assertValue(true)

            eventosTotalPagado.onNext(Decimal.DIEZ)
            observadorPrueba.assertValue(true)

            eventosTotalPagado.onNext(Decimal.CERO)
            observadorPrueba.assertValuesOnly(true, false)

            eventosTotalPagado.onNext(Decimal.DIEZ)
            observadorPrueba.assertValuesOnly(true, false, true)
        }

        @Nested
        inner class AlEstarPagando
        {
            @Test
            fun si_no_confirmo_la_compra_emite_true_luego_false_y_finalmente_true()
            {
                doReturn(Observable.just(Decimal.UNO)).`when`(mockPagosDeUnaCompra).totalPagado
                doReturn(Decimal.UNO).`when`(mockTotalAPagarSegunPersonas).granTotal

                val mockListadoPersonas = mockConDefaultAnswer(ListaFiltrableUI::class.java).also {
                    val creditoFondo = mockConDefaultAnswer(CreditoFondoConNombre::class.java).also {
                        doReturn(mockConDefaultAnswer(CreditoFondo::class.java)).`when`(it).creditoAsociado
                    }
                    val creditoPaquete = mockConDefaultAnswer(CreditoPaqueteConNombre::class.java).also {
                        val mockCreditoPaquete = mockConDefaultAnswer(CreditoPaquete::class.java).also {
                            doReturn(listOf(creditoFondo))
                                .`when`(it)
                                .creditosFondos
                        }
                        doReturn(mockCreditoPaquete).`when`(it).creditoAsociado
                    }

                    doReturn(
                            listOf(
                                    ProcesoSeleccionCreditosUI
                                        .CreditosPorPersonaAProcesar(
                                                mockConDefaultAnswer(PersonaConGrupoCliente::class.java),
                                                listOf(creditoFondo),
                                                listOf(creditoPaquete),
                                                listOf(),
                                                listOf()
                                                                    )
                                  )
                            )
                        .`when`(it).items
                }
                doReturn(mockListadoPersonas).`when`(mockTotalAPagarSegunPersonas).listadoDePersonasConCreditos
                doReturn(listOf(mockConDefaultAnswer(Pago::class.java))).`when`(mockPagosDeUnaCompra).pagoActuales

                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())
                val observadorPrueba = modelo.puedePagar.test()

                observadorPrueba.assertValue(true)

                modelo.pagar()

                observadorPrueba.assertValuesOnly(true, false, true)
            }

            @Test
            fun si_confirmo_la_compra_emite_true_luego_false_y_finalmente_true()
            {
                doReturn(Observable.just(Decimal.UNO)).`when`(mockPagosDeUnaCompra).totalPagado
                doReturn(Decimal.UNO).`when`(mockTotalAPagarSegunPersonas).granTotal

                val mockListadoPersonas = mockConDefaultAnswer(ListaFiltrableUI::class.java).also {
                    val creditoFondo = mockConDefaultAnswer(CreditoFondoConNombre::class.java).also {
                        doReturn(mockConDefaultAnswer(CreditoFondo::class.java)).`when`(it).creditoAsociado
                    }
                    val creditoPaquete = mockConDefaultAnswer(CreditoPaqueteConNombre::class.java).also {
                        val mockCreditoPaquete = mockConDefaultAnswer(CreditoPaquete::class.java).also {
                            doReturn(listOf(creditoFondo))
                                .`when`(it)
                                .creditosFondos
                        }
                        doReturn(mockCreditoPaquete).`when`(it).creditoAsociado
                    }

                    doReturn(
                            listOf(
                                    ProcesoSeleccionCreditosUI
                                        .CreditosPorPersonaAProcesar(
                                                mockConDefaultAnswer(PersonaConGrupoCliente::class.java),
                                                listOf(creditoFondo),
                                                listOf(creditoPaquete),
                                                listOf(),
                                                listOf()
                                                                    )
                                  )
                            )
                        .`when`(it).items
                }
                doReturn(mockListadoPersonas).`when`(mockTotalAPagarSegunPersonas).listadoDePersonasConCreditos
                doReturn(listOf(mockConDefaultAnswer(Pago::class.java))).`when`(mockPagosDeUnaCompra).pagoActuales

                doReturn(RespuestaVacia.Exitosa)
                    .`when`(mockApiCompras)
                    .actualizarCampos(anyString(), cualquiera())

                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())
                val observadorPrueba = modelo.puedePagar.test()

                observadorPrueba.assertValue(true)

                modelo.pagar()

                observadorPrueba.assertValues(true, false, true)
            }
        }
    }

    @Nested
    inner class AlPagarSinCompraCreada
    {
        @BeforeEach
        fun mockearDependencias()
        {
            doReturn(Observable.just(Decimal.UNO)).`when`(mockPagosDeUnaCompra).totalPagado
            doReturn(Decimal.UNO).`when`(mockTotalAPagarSegunPersonas).granTotal
        }

        @Test
        fun invoca_el_api_de_compras_correctamente()
        {
            val creditosAPagar = crearCrearMocksCreditosAPagar()
            val mockListadoPersonas = mockConDefaultAnswer(ListaFiltrableUI::class.java).also {
                doReturn(
                        listOf(
                                ProcesoSeleccionCreditosUI
                                    .CreditosPorPersonaAProcesar(
                                            mockConDefaultAnswer(PersonaConGrupoCliente::class.java),
                                            creditosAPagar.first,
                                            creditosAPagar.second,
                                            listOf(),
                                            listOf()
                                                                )
                              )
                        )
                    .`when`(it).items
            }
            doReturn(mockListadoPersonas).`when`(mockTotalAPagarSegunPersonas).listadoDePersonasConCreditos

            val pagos = listOf(Pago(Decimal.UNO, Pago.MetodoDePago.TARJETA_DEBITO, "asdf"))
            doReturn(pagos).`when`(mockPagosDeUnaCompra).pagoActuales

            val mockCompra = mockConDefaultAnswer(Compra::class.java).also {
                doReturn("id que no importa").`when`(it).id
            }
            doReturn(RespuestaIndividual.Exitosa(mockCompra))
                .`when`(mockApiCompras)
                .actualizar(anyString(), cualquiera())

            doReturn(RespuestaVacia.Exitosa)
                .`when`(mockApiCompras)
                .actualizarCampos(anyString(), cualquiera())

            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                    Schedulers.trampoline())

            modelo.pagar()


            verify(mockApiCompras).actualizar(
                    anyString(),
                    argCumpleQue(ArgumentMatcher {
                        assertEquals(ID_CLIENTE, it.idCliente)
                        assertEquals(NOMBRE_USUARIO, it.nombreUsuario)
                        assertEquals(creditosAPagar.first.map { it.creditoAsociado }, it.creditosFondos)
                        assertEquals(creditosAPagar.second.map { it.creditoAsociado }, it.creditosPaquetes)
                        assertEquals(pagos, it.pagos)
                        true
                    })
                                             )
        }

        @Test
        fun se_limpia_el_mensaje_de_error()
        {
            val mockCompra = mockConDefaultAnswer(Compra::class.java).also {
                doReturn("asasdf 123234").`when`(it).id
            }

            crearMocksDeModelosUIInternosYApiDePrueba(RespuestaIndividual.Exitosa(mockCompra))
            doReturn(RespuestaVacia.Exitosa)
                .`when`(mockApiCompras)
                .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))

            val modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                    Schedulers.trampoline())

            val observadorDePrueba = modelo.mensajesDeError.test()

            modelo.pagar()

            observadorDePrueba.assertValue("")
        }

        @Nested
        inner class YRespuestaExitosa
        {
            private val ID_COMPRA = "asdfasdf"

            @BeforeEach
            fun mockearRespuesta()
            {
                val mockCompra = mockConDefaultAnswer(Compra::class.java).also {
                    doReturn(ID_COMPRA).`when`(it).id
                }

                crearMocksDeModelosUIInternosYApiDePrueba(RespuestaIndividual.Exitosa(mockCompra))
            }

            @Test
            fun el_mensaje_de_error_no_emite_valores_nuevos()
            {
                doReturn(RespuestaVacia.Exitosa)
                    .`when`(mockApiCompras)
                    .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))

                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())

                val observadorDePrueba = modelo.mensajesDeError.test()

                modelo.pagar()

                observadorDePrueba.assertValue("")
            }

            @Test
            fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_y_finalmente_a_compra_creada()
            {
                doReturn(RespuestaVacia.Exitosa)
                    .`when`(mockApiCompras)
                    .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))

                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())

                val observadorDePrueba = modelo.estado.test()

                modelo.pagar()

                observadorDePrueba.assertValueAt(0, ProcesoPagarUI.Estado.SIN_CREAR_COMPRA)
                observadorDePrueba.assertValueAt(1, ProcesoPagarUI.Estado.CREANDO_COMPRA)
                observadorDePrueba.assertValueAt(2, ProcesoPagarUI.Estado.COMPRA_CREADA)
            }

            @Test
            fun invoca_el_api_de_compras_para_confirmacion_correctamente()
            {
                doReturn(RespuestaVacia.Exitosa)
                    .`when`(mockApiCompras)
                    .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))

                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())

                modelo.pagar()

                with(inOrder(mockApiCompras))
                {
                    verify(mockApiCompras).actualizar(anyString(), cualquiera())
                    verify(mockApiCompras).actualizarCampos(eqParaKotlin(ID_COMPRA), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    verifyNoMoreInteractions()
                }
            }

            @Nested
            inner class CuandoLaRespuestaDeConfirmarEs
            {
                @Nested
                inner class Exitosa
                {
                    @BeforeEach
                    fun mockearRespuesta()
                    {
                        doReturn(RespuestaVacia.Exitosa)
                            .`when`(mockApiCompras)
                            .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    }

                    @Test
                    fun el_mensaje_de_error_no_emite_valores_nuevos()
                    {
                        val modelo = ProcesoPagar(
                                mockContextoDeSesion,
                                mockTotalAPagarSegunPersonas,
                                mockPagosDeUnaCompra,
                                mockApiCompras,
                                Single.just(mockMapeadorReglasANombresRestricciones),
                                Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                Schedulers.trampoline())

                        val observadorDePrueba = modelo.mensajesDeError.test()

                        modelo.pagar()

                        observadorDePrueba.assertValue("")
                    }

                    @Test
                    fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_a_compra_creada_a_confirmando_compra_y_finalmente_a_compra_confirmada()
                    {
                        val modelo = ProcesoPagar(
                                mockContextoDeSesion,
                                mockTotalAPagarSegunPersonas,
                                mockPagosDeUnaCompra,
                                mockApiCompras,
                                Single.just(mockMapeadorReglasANombresRestricciones),
                                Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                Schedulers.trampoline())

                        val observadorDePrueba = modelo.estado.test()

                        modelo.pagar()

                        observadorDePrueba.assertValues(
                                ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                                ProcesoPagarUI.Estado.CREANDO_COMPRA,
                                ProcesoPagarUI.Estado.COMPRA_CREADA,
                                ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                                ProcesoPagarUI.Estado.COMPRA_CONFIRMADA,
                                ProcesoPagarUI.Estado.PROCESO_COMPLETADO
                                                       )
                    }
                }

                @Nested
                inner class ErrorTimeout
                {
                    @BeforeEach
                    fun mockearRespuesta()
                    {
                        doReturn(RespuestaVacia.Error.Timeout)
                            .`when`(mockApiCompras)
                            .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    }

                    @Test
                    fun el_mensaje_de_error_informa_que_se_produjo_un_error_al_intentar_crear_la_compra_y_muestra_el_error()
                    {
                        val modelo = ProcesoPagar(
                                mockContextoDeSesion,
                                mockTotalAPagarSegunPersonas,
                                mockPagosDeUnaCompra,
                                mockApiCompras,
                                Single.just(mockMapeadorReglasANombresRestricciones),
                                Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                Schedulers.trampoline())

                        val observadorDePrueba = modelo.mensajesDeError.test()

                        modelo.pagar()

                        observadorDePrueba.assertValues("", "Timeout contactando el backend")
                    }

                    @Test
                    fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_a_compra_creada_a_confirmando_comrpa_y_finalmente_a_compra_creada()
                    {
                        val modelo = ProcesoPagar(
                                mockContextoDeSesion,
                                mockTotalAPagarSegunPersonas,
                                mockPagosDeUnaCompra,
                                mockApiCompras,
                                Single.just(mockMapeadorReglasANombresRestricciones),
                                Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                Schedulers.trampoline())

                        val observadorDePrueba = modelo.estado.test()

                        modelo.pagar()

                        observadorDePrueba.assertValues(
                                ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                                ProcesoPagarUI.Estado.CREANDO_COMPRA,
                                ProcesoPagarUI.Estado.COMPRA_CREADA,
                                ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                                ProcesoPagarUI.Estado.COMPRA_CREADA
                                                       )
                    }
                }

                @Nested
                inner class ErrorRed
                {
                    @BeforeEach
                    fun mockearRespuesta()
                    {
                        doReturn(RespuestaVacia.Error.Red(IOException("Error de red")))
                            .`when`(mockApiCompras)
                            .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    }

                    @Test
                    fun el_mensaje_de_error_informa_que_se_produjo_un_error_al_intentar_crear_la_compra_y_muestra_el_error()
                    {
                        val modelo = ProcesoPagar(
                                mockContextoDeSesion,
                                mockTotalAPagarSegunPersonas,
                                mockPagosDeUnaCompra,
                                mockApiCompras,
                                Single.just(mockMapeadorReglasANombresRestricciones),
                                Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                Schedulers.trampoline())

                        val observadorDePrueba = modelo.mensajesDeError.test()

                        modelo.pagar()

                        observadorDePrueba.assertValues("", "Error contactando el backend")
                    }

                    @Test
                    fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_a_compra_creada_a_confirmando_comrpa_y_finalmente_a_compra_creada()
                    {
                        val modelo = ProcesoPagar(
                                mockContextoDeSesion,
                                mockTotalAPagarSegunPersonas,
                                mockPagosDeUnaCompra,
                                mockApiCompras,
                                Single.just(mockMapeadorReglasANombresRestricciones),
                                Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                Schedulers.trampoline())

                        val observadorDePrueba = modelo.estado.test()

                        modelo.pagar()

                        observadorDePrueba.assertValues(
                                ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                                ProcesoPagarUI.Estado.CREANDO_COMPRA,
                                ProcesoPagarUI.Estado.COMPRA_CREADA,
                                ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                                ProcesoPagarUI.Estado.COMPRA_CREADA
                                                       )
                    }
                }

                @Nested
                inner class ErrorBack
                {
                    @Nested
                    inner class ConCodigoEntidadNoExiste
                    {
                        @BeforeEach
                        fun mockearRespuesta()
                        {
                            doReturn(
                                    RespuestaVacia.Error.Back(
                                            404,
                                            ErrorDePeticion(CompraDTO.CodigosError.NO_EXISTE, "no importa")
                                                             )
                                    )
                                .`when`(mockApiCompras)
                                .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                        }

                        @Test
                        fun el_mensaje_de_error_informa_que_la_compra_se_encuentra_confirmada()
                        {
                            val modelo = ProcesoPagar(
                                    mockContextoDeSesion,
                                    mockTotalAPagarSegunPersonas,
                                    mockPagosDeUnaCompra,
                                    mockApiCompras,
                                    Single.just(mockMapeadorReglasANombresRestricciones),
                                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                    Schedulers.trampoline())

                            val observadorDePrueba = modelo.mensajesDeError.test()

                            modelo.pagar()

                            observadorDePrueba.assertValues("", "La compra a confirmar no existe")
                        }

                        @Test
                        fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_a_compra_creada_a_confirmando_comrpa_y_finalmente_a_compra_creada()
                        {
                            val modelo = ProcesoPagar(
                                    mockContextoDeSesion,
                                    mockTotalAPagarSegunPersonas,
                                    mockPagosDeUnaCompra,
                                    mockApiCompras,
                                    Single.just(mockMapeadorReglasANombresRestricciones),
                                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                    Schedulers.trampoline())

                            val observadorDePrueba = modelo.estado.test()

                            modelo.pagar()

                            observadorDePrueba.assertValues(
                                    ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                                    ProcesoPagarUI.Estado.CREANDO_COMPRA,
                                    ProcesoPagarUI.Estado.COMPRA_CREADA,
                                    ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                                    ProcesoPagarUI.Estado.COMPRA_CREADA
                                                           )
                        }
                    }

                    @Nested
                    inner class ConCodigoNoControlado
                    {
                        @BeforeEach
                        fun mockearRespuesta()
                        {
                            doReturn(
                                    RespuestaVacia.Error.Back(
                                            400,
                                            ErrorDePeticion(1, "no es controlado")
                                                             )
                                    )
                                .`when`(mockApiCompras)
                                .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                        }

                        @Test
                        fun el_mensaje_de_error_informa_que_se_produjo_un_error_al_intentar_crear_la_compra_y_muestra_el_error()
                        {
                            val modelo = ProcesoPagar(
                                    mockContextoDeSesion,
                                    mockTotalAPagarSegunPersonas,
                                    mockPagosDeUnaCompra,
                                    mockApiCompras,
                                    Single.just(mockMapeadorReglasANombresRestricciones),
                                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                    Schedulers.trampoline())

                            val observadorDePrueba = modelo.mensajesDeError.test()

                            modelo.pagar()

                            observadorDePrueba.assertValues("", "Error en petición: no es controlado")
                        }

                        @Test
                        fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_a_compra_creada_a_confirmando_comrpa_y_finalmente_a_compra_creada()
                        {
                            val modelo = ProcesoPagar(
                                    mockContextoDeSesion,
                                    mockTotalAPagarSegunPersonas,
                                    mockPagosDeUnaCompra,
                                    mockApiCompras,
                                    Single.just(mockMapeadorReglasANombresRestricciones),
                                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                                    Schedulers.trampoline())

                            val observadorDePrueba = modelo.estado.test()

                            modelo.pagar()

                            observadorDePrueba.assertValues(
                                    ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                                    ProcesoPagarUI.Estado.CREANDO_COMPRA,
                                    ProcesoPagarUI.Estado.COMPRA_CREADA,
                                    ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                                    ProcesoPagarUI.Estado.COMPRA_CREADA
                                                           )
                        }
                    }
                }
            }
        }

        @Nested
        inner class YRespuestaVacia
        {
            @BeforeEach
            fun mockearRespuesta()
            {
                crearMocksDeModelosUIInternosYApiDePrueba(RespuestaIndividual.Vacia())
            }

            @Test
            fun lanza_excepcion_IllegalStateException_y_no_emite_mensaje_de_error_nuevos()
            {
                erroresEsperados.add(ErrorEsperado(IllegalStateException()))

                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())

                val observadorDePrueba = modelo.mensajesDeError.test()

                modelo.pagar()

                observadorDePrueba.assertValue("")
            }

            @Test
            fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_y_lanza_excepcion_IllegalStateException()
            {
                erroresEsperados.add(ErrorEsperado(IllegalStateException()))

                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())

                val observadorDePrueba = modelo.estado.test()

                modelo.pagar()

                observadorDePrueba.assertValues(ProcesoPagarUI.Estado.SIN_CREAR_COMPRA, ProcesoPagarUI.Estado.CREANDO_COMPRA)
            }
        }

        @Nested
        inner class YRespuestaErrorTimeout
        {
            @BeforeEach
            fun mockearRespuesta()
            {
                crearMocksDeModelosUIInternosYApiDePrueba(RespuestaIndividual.Error.Timeout())
            }

            @Test
            fun el_mensaje_de_error_informa_que_se_produjo_un_error_al_intentar_crear_la_compra_y_muestra_el_error()
            {
                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())

                val observadorDePrueba = modelo.mensajesDeError.test()

                modelo.pagar()

                observadorDePrueba.assertValues("", "Timeout contactando el backend")
            }

            @Test
            fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_y_finalmente_a_sin_crear_compra()
            {
                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())

                val observadorDePrueba = modelo.estado.test()

                modelo.pagar()

                observadorDePrueba.assertValues(
                        ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                        ProcesoPagarUI.Estado.CREANDO_COMPRA,
                        ProcesoPagarUI.Estado.SIN_CREAR_COMPRA
                                               )
            }
        }

        @Nested
        inner class YRespuestaErrorRed
        {
            @BeforeEach
            fun mockearRespuesta()
            {
                crearMocksDeModelosUIInternosYApiDePrueba(RespuestaIndividual.Error.Red(IOException("Error de red")))
            }

            @Test
            fun el_mensaje_de_error_informa_que_se_produjo_un_error_al_intentar_crear_la_compra_y_muestra_el_error()
            {
                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())

                val observadorDePrueba = modelo.mensajesDeError.test()

                modelo.pagar()

                observadorDePrueba.assertValues("", "Error contactando el backend")
            }

            @Test
            fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_y_finalmente_a_sin_crear_compra()
            {
                val modelo = ProcesoPagar(
                        mockContextoDeSesion,
                        mockTotalAPagarSegunPersonas,
                        mockPagosDeUnaCompra,
                        mockApiCompras,
                        Single.just(mockMapeadorReglasANombresRestricciones),
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        Schedulers.trampoline())

                val observadorDePrueba = modelo.estado.test()

                modelo.pagar()

                observadorDePrueba.assertValues(
                        ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                        ProcesoPagarUI.Estado.CREANDO_COMPRA,
                        ProcesoPagarUI.Estado.SIN_CREAR_COMPRA
                                               )
            }
        }

        @Nested
        inner class YRespuestaErrorBack
        {
            @Nested
            inner class ConCodigoCompraDuplicadaEnBD
            {
                @BeforeEach
                fun mockearRespuesta()
                {
                    crearMocksDeModelosUIInternosYApiDePrueba(
                            RespuestaIndividual.Error.Back(
                                    400,
                                    ErrorDePeticion(CompraDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD, "no importa")
                                                          )
                                                             )
                }

                @Test
                fun el_mensaje_de_error_informa_que_la_compra_se_encuentra_confirmada()
                {
                    val modelo = ProcesoPagar(
                            mockContextoDeSesion,
                            mockTotalAPagarSegunPersonas,
                            mockPagosDeUnaCompra,
                            mockApiCompras,
                            Single.just(mockMapeadorReglasANombresRestricciones),
                            Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                            Schedulers.trampoline())

                    val observadorDePrueba = modelo.mensajesDeError.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues("", "La compra ya fue confirmada")
                }

                @Test
                fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_y_finalmente_a_compra_confirmada()
                {
                    val modelo = ProcesoPagar(
                            mockContextoDeSesion,
                            mockTotalAPagarSegunPersonas,
                            mockPagosDeUnaCompra,
                            mockApiCompras,
                            Single.just(mockMapeadorReglasANombresRestricciones),
                            Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                            Schedulers.trampoline())

                    val observadorDePrueba = modelo.estado.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues(
                            ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                            ProcesoPagarUI.Estado.CREANDO_COMPRA,
                            ProcesoPagarUI.Estado.COMPRA_CONFIRMADA
                                                   )
                }
            }

            @Nested
            inner class ConCodigoPagosConNumeroDeTransaccionPosRepetidos
            {
                @BeforeEach
                fun mockearRespuesta()
                {
                    crearMocksDeModelosUIInternosYApiDePrueba(
                            RespuestaIndividual.Error.Back(
                                    400,
                                    ErrorDePeticion(CompraDTO.CodigosError.PAGOS_CON_NUMERO_DE_TRANSACCION_POS_REPETIDOS, "no importa")
                                                          )
                                                             )
                }

                @Test
                fun el_mensaje_de_error_informa_que_la_compra_contiene_pagos_con_numeros_de_transaccion_repetidos()
                {
                    val modelo = ProcesoPagar(
                            mockContextoDeSesion,
                            mockTotalAPagarSegunPersonas,
                            mockPagosDeUnaCompra,
                            mockApiCompras,
                            Single.just(mockMapeadorReglasANombresRestricciones),
                            Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                            Schedulers.trampoline())

                    val observadorDePrueba = modelo.mensajesDeError.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues("", "La compra contiene pagos con números de transacción usados anteriormente")
                }

                @Test
                fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_y_finalmente_a_sin_crear_compra()
                {
                    val modelo = ProcesoPagar(
                            mockContextoDeSesion,
                            mockTotalAPagarSegunPersonas,
                            mockPagosDeUnaCompra,
                            mockApiCompras,
                            Single.just(mockMapeadorReglasANombresRestricciones),
                            Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                            Schedulers.trampoline())

                    val observadorDePrueba = modelo.estado.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues(
                            ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                            ProcesoPagarUI.Estado.CREANDO_COMPRA,
                            ProcesoPagarUI.Estado.SIN_CREAR_COMPRA
                                                   )
                }
            }

            @Nested
            inner class ConCodigoEntidadReferenciadaNoExiste
            {
                @BeforeEach
                fun mockearRespuesta()
                {
                    crearMocksDeModelosUIInternosYApiDePrueba(
                            RespuestaIndividual.Error.Back(
                                    400,
                                    ErrorDePeticion(CompraDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, "no importa")
                                                          )
                                                             )
                }

                @Test
                fun el_mensaje_de_error_informa_que_la_compra_se_encuentra_confirmada()
                {
                    val modelo = ProcesoPagar(
                            mockContextoDeSesion,
                            mockTotalAPagarSegunPersonas,
                            mockPagosDeUnaCompra,
                            mockApiCompras,
                            Single.just(mockMapeadorReglasANombresRestricciones),
                            Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                            Schedulers.trampoline())

                    val observadorDePrueba = modelo.mensajesDeError.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues("", "Algún dato prerequisito para la compra no existe")
                }

                @Test
                fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_y_finalmente_a_compra_confirmada()
                {
                    val modelo = ProcesoPagar(
                            mockContextoDeSesion,
                            mockTotalAPagarSegunPersonas,
                            mockPagosDeUnaCompra,
                            mockApiCompras,
                            Single.just(mockMapeadorReglasANombresRestricciones),
                            Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                            Schedulers.trampoline())

                    val observadorDePrueba = modelo.estado.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues(
                            ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                            ProcesoPagarUI.Estado.CREANDO_COMPRA,
                            ProcesoPagarUI.Estado.SIN_CREAR_COMPRA
                                                   )
                }
            }

            @Nested
            inner class ConCodigoNoControlado
            {
                @BeforeEach
                fun mockearRespuesta()
                {
                    crearMocksDeModelosUIInternosYApiDePrueba(
                            RespuestaIndividual.Error.Back(
                                    400,
                                    ErrorDePeticion(1, "no es controlado")
                                                          )
                                                             )
                }

                @Test
                fun el_mensaje_de_error_informa_que_se_produjo_un_error_al_intentar_crear_la_compra_y_muestra_el_error()
                {
                    val modelo = ProcesoPagar(
                            mockContextoDeSesion,
                            mockTotalAPagarSegunPersonas,
                            mockPagosDeUnaCompra,
                            mockApiCompras,
                            Single.just(mockMapeadorReglasANombresRestricciones),
                            Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                            Schedulers.trampoline())

                    val observadorDePrueba = modelo.mensajesDeError.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues("", "Error en petición: no es controlado")
                }

                @Test
                fun el_estado_pasa_de_sin_crear_compra_a_creando_compra_y_finalmente_a_sin_crear_compra()
                {
                    val modelo = ProcesoPagar(
                            mockContextoDeSesion,
                            mockTotalAPagarSegunPersonas,
                            mockPagosDeUnaCompra,
                            mockApiCompras,
                            Single.just(mockMapeadorReglasANombresRestricciones),
                            Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                            Schedulers.trampoline())

                    val observadorDePrueba = modelo.estado.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues(
                            ProcesoPagarUI.Estado.SIN_CREAR_COMPRA,
                            ProcesoPagarUI.Estado.CREANDO_COMPRA,
                            ProcesoPagarUI.Estado.SIN_CREAR_COMPRA
                                                   )
                }
            }
        }
    }

    @Nested
    inner class AlPagarConCompraCreada
    {
        private val ID_COMPRA = "asdfasdf"
        private lateinit var modelo: ProcesoPagarUI
        private lateinit var observadorDeEstado: TestObserver<ProcesoPagarUI.Estado>

        private val creditosAProcesar = List(4) {
            val idPersona = it.toLong()
            ProcesoSeleccionCreditosUI
                .CreditosPorPersonaAProcesar(
                        mockConDefaultAnswer(PersonaConGrupoCliente::class.java).also {
                            val mockPersona = mockConDefaultAnswer(Persona::class.java).also {
                                doReturn(idPersona).`when`(it).id
                            }
                            doReturn(mockPersona).`when`(it).persona
                        },
                        List(3) { CreditoFondoConNombre("Crédito Fondo A Pagar $it", crearCreditoFondoSegunIndice(it, idPersona)) },
                        List(2) { CreditoPaqueteConNombre("Crédito Paquete A Pagar ${(it + 1) * 100}", it + 13, crearCreditoPaqueteSegunIndice((it + 1) * 100, idPersona)) },
                        List(1) { CreditoFondoConNombre("Crédito Fondo Pagado $it", crearCreditoFondoSegunIndice(it, idPersona)) },
                        List(1) { CreditoPaqueteConNombre("Crédito Paquete Pagado ${(it + 1) * 100}", 1, crearCreditoPaqueteSegunIndice((it + 1) * 100, idPersona)) }
                                            )
        }

        private val creditosFondosDevueltosPorBackend =
                creditosAProcesar.asSequence()
                    .flatMap { it.creditosFondoAPagar.asSequence() }
                    .mapIndexed { index, creditoFondo -> creditoFondo.creditoAsociado.copiar(id = index.toLong()) }
                    .toList()

        private val creditosPaqueteDevueltosPorBackend =
                creditosAProcesar.asSequence()
                    .flatMap { it.creditosPaqueteAPagar.asSequence() }
                    .mapIndexed { indiceCreditoPaquete, creditoPaquete ->

                        val creditosDelPaqueteConIds =
                                creditoPaquete
                                    .creditoAsociado
                                    .creditosFondos
                                    .mapIndexed { j, creditoFondo ->
                                        val idCredito = ((indiceCreditoPaquete + 1) * creditosFondosDevueltosPorBackend.size + j).toLong()
                                        creditoFondo.copiar(id = idCredito)
                                    }

                        creditoPaquete.creditoAsociado.copiar(creditosFondos = creditosDelPaqueteConIds)
                    }
                    .toList()

        @BeforeEach
        fun mockearCompraCreada()
        {
            doReturn(Observable.just(Decimal.UNO)).`when`(mockPagosDeUnaCompra).totalPagado
            doReturn(Decimal.UNO).`when`(mockTotalAPagarSegunPersonas).granTotal

            val mockCompra = mockConDefaultAnswer(Compra::class.java).also {
                doReturn(ID_COMPRA).`when`(it).id
                doReturn(creditosFondosDevueltosPorBackend).`when`(it).creditosFondos
                doReturn(creditosPaqueteDevueltosPorBackend).`when`(it).creditosPaquetes
            }

            crearMocksDeModelosUIInternosYApiDePrueba(RespuestaIndividual.Exitosa(mockCompra))
            val mockListadoPersonas = mockConDefaultAnswer(ListaFiltrableUI::class.java).also {
                doReturn(creditosAProcesar).`when`(it).items
            }
            doReturn(mockListadoPersonas).`when`(mockTotalAPagarSegunPersonas).listadoDePersonasConCreditos

            // Se forza a que falle la confirmación para poder probar cuando se tienen la compra creada en memoria (o eventualmente BD)
            doReturn(RespuestaVacia.Error.Timeout)
                .`when`(mockApiCompras)
                .actualizarCampos(anyString(), cualquiera())

            modelo = ProcesoPagar(
                    mockContextoDeSesion,
                    mockTotalAPagarSegunPersonas,
                    mockPagosDeUnaCompra,
                    mockApiCompras,
                    Single.just(mockMapeadorReglasANombresRestricciones),
                    Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                    Schedulers.trampoline())

            modelo.pagar()

            // Observar después de pagar para registrar solo los eventos del reintento. Los eventos anteriores se probaron
            // cuando se paga sin una compra creada
            observadorDeEstado = modelo.estado.test()
        }

        @Test
        fun invoca_el_api_de_compras_para_confirmacion_correctamente()
        {
            doReturn(RespuestaVacia.Exitosa)
                .`when`(mockApiCompras)
                .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))

            modelo.pagar()

            with(inOrder(mockApiCompras))
            {
                verify(mockApiCompras).actualizar(anyString(), cualquiera())
                verify(mockApiCompras, times(2)).actualizarCampos(eqParaKotlin(ID_COMPRA), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                verifyNoMoreInteractions()
            }
        }

        @Nested
        inner class CuandoLaRespuestaDeConfirmarEs
        {
            @Nested
            inner class Exitosa
            {
                @BeforeEach
                fun mockearRespuesta()
                {
                    doReturn(RespuestaVacia.Exitosa)
                        .`when`(mockApiCompras)
                        .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                }

                @Test
                fun el_mensaje_de_error_no_emite_valores_nuevos()
                {
                    val observadorDePrueba = modelo.mensajesDeError.test()

                    modelo.pagar()

                    observadorDePrueba.assertValue("")
                }

                @Test
                fun el_estado_pasa_de_compra_creada_a_confirmando_compra_luego_compra_confirmada_y_finalmente_proceso_completado()
                {
                    modelo.pagar()

                    observadorDeEstado.assertValues(
                            ProcesoPagarUI.Estado.COMPRA_CREADA,
                            ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                            ProcesoPagarUI.Estado.COMPRA_CONFIRMADA,
                            ProcesoPagarUI.Estado.PROCESO_COMPLETADO
                                                   )
                }

                @Test
                fun los_creditos_a_codificar_emitidos_son_los_pagados_y_los_devueltos_por_el_backend_y_son_correctos_para_cada_persona()
                {
                    val creditosAProcesarPorPersona = creditosAProcesar.associateBy { it.personaConGrupoCliente.persona.id!! }
                    val idFondoVsNombre =
                            creditosAProcesar
                                .flatMap { it.creditosFondoAPagar }
                                .associateBy({ it.creditoAsociado.idFondoComprado }, { it.nombreDeFondo })
                    val idPaqueteVsCreditoPaqueteConNombre =
                            creditosAProcesar
                                .flatMap { it.creditosPaqueteAPagar }
                                .associateBy { it.creditoAsociado.idPaquete }

                    val creditosFondoPorPersona = creditosFondosDevueltosPorBackend.groupBy { it.idPersonaDueña }
                    val creditosPaquetePorPersona =
                            creditosPaqueteDevueltosPorBackend
                                .asSequence()
                                .map { Pair(it.creditosFondos.first().idPersonaDueña, it) }
                                .groupBy({ it.first }, { it.second })


                    val creditosEsperados =
                            creditosAProcesar.mapIndexed { idMockPersona, creditosPorPersonaAProcesar ->
                                val idPersona = idMockPersona.toLong()
                                ProcesoPagarUI.CreditosACodificarPorPersona(
                                        creditosPorPersonaAProcesar.personaConGrupoCliente,
                                        creditosAProcesarPorPersona[idPersona]!!.creditosFondoPagados +
                                        creditosFondoPorPersona[idPersona]!!.map {
                                            CreditoFondoConNombre(idFondoVsNombre[it.idFondoComprado]!!, it)
                                        },
                                        creditosAProcesarPorPersona[idPersona]!!.creditosPaquetePagados +
                                        creditosPaquetePorPersona[idPersona]!!.map {
                                            CreditoPaqueteConNombre(
                                                    idPaqueteVsCreditoPaqueteConNombre[it.idPaquete]!!.nombreDelPaquete,
                                                    idPaqueteVsCreditoPaqueteConNombre[it.idPaquete]!!.cantidad,
                                                    it
                                                                   )
                                        }
                                                                           )
                            }

                    val observadorDePrueba = modelo.creditosACodificar.test()

                    modelo.pagar()

                    observadorDePrueba.assertResult(creditosEsperados)
                }
            }

            @Nested
            inner class ErrorTimeout
            {
                @BeforeEach
                fun mockearRespuesta()
                {
                    doReturn(RespuestaVacia.Error.Timeout)
                        .`when`(mockApiCompras)
                        .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                }

                @Test
                fun el_mensaje_de_error_informa_que_se_produjo_un_error_al_intentar_crear_la_compra_y_muestra_el_error()
                {
                    val observadorDePrueba = modelo.mensajesDeError.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues("", "Timeout contactando el backend")
                }

                @Test
                fun el_estado_pasa_de_compra_creada_a_confirmando_compra_y_finalmente_vuelve_a_compra_creada()
                {
                    modelo.pagar()

                    observadorDeEstado.assertValues(
                            ProcesoPagarUI.Estado.COMPRA_CREADA,
                            ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                            ProcesoPagarUI.Estado.COMPRA_CREADA
                                                   )
                }

                @Test
                fun los_creditos_a_codificar_no_emite()
                {
                    val observadorDePrueba = modelo.creditosACodificar.test()

                    modelo.pagar()

                    observadorDePrueba.assertEmpty()
                }
            }

            @Nested
            inner class ErrorRed
            {
                @BeforeEach
                fun mockearRespuesta()
                {
                    doReturn(RespuestaVacia.Error.Red(IOException("Error de red")))
                        .`when`(mockApiCompras)
                        .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                }

                @Test
                fun el_mensaje_de_error_informa_que_se_produjo_un_error_al_intentar_crear_la_compra_y_muestra_el_error()
                {
                    val observadorDePrueba = modelo.mensajesDeError.test()

                    modelo.pagar()

                    observadorDePrueba.assertValues("", "Error contactando el backend")
                }

                @Test
                fun el_estado_pasa_de_compra_creada_a_confirmando_compra_y_finalmente_vuelve_a_compra_creada()
                {
                    modelo.pagar()

                    observadorDeEstado.assertValues(
                            ProcesoPagarUI.Estado.COMPRA_CREADA,
                            ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                            ProcesoPagarUI.Estado.COMPRA_CREADA
                                                   )
                }

                @Test
                fun los_creditos_a_codificar_no_emite()
                {
                    val observadorDePrueba = modelo.creditosACodificar.test()

                    modelo.pagar()

                    observadorDePrueba.assertEmpty()
                }
            }

            @Nested
            inner class ErrorBack
            {
                @Nested
                inner class ConCodigoEntidadNoExiste
                {
                    @BeforeEach
                    fun mockearRespuesta()
                    {
                        doReturn(
                                RespuestaVacia.Error.Back(
                                        404,
                                        ErrorDePeticion(CompraDTO.CodigosError.NO_EXISTE, "no importa")
                                                         )
                                )
                            .`when`(mockApiCompras)
                            .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    }

                    @Test
                    fun el_mensaje_de_error_informa_que_la_compra_se_encuentra_confirmada()
                    {
                        val observadorDePrueba = modelo.mensajesDeError.test()

                        modelo.pagar()

                        observadorDePrueba.assertValues("", "La compra a confirmar no existe")
                    }

                    @Test
                    fun el_estado_pasa_de_compra_creada_a_confirmando_compra_y_finalmente_vuelve_a_compra_creada()
                    {
                        modelo.pagar()

                        observadorDeEstado.assertValues(
                                ProcesoPagarUI.Estado.COMPRA_CREADA,
                                ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                                ProcesoPagarUI.Estado.COMPRA_CREADA
                                                       )
                    }

                    @Test
                    fun los_creditos_a_codificar_no_emite()
                    {
                        val observadorDePrueba = modelo.creditosACodificar.test()

                        modelo.pagar()

                        observadorDePrueba.assertEmpty()
                    }
                }

                @Nested
                inner class ConCodigoNoControlado
                {
                    @BeforeEach
                    fun mockearRespuesta()
                    {
                        doReturn(
                                RespuestaVacia.Error.Back(
                                        400,
                                        ErrorDePeticion(1, "no es controlado")
                                                         )
                                )
                            .`when`(mockApiCompras)
                            .actualizarCampos(anyString(), eqParaKotlin(TransaccionEntidadTerminadaDTO(true)))
                    }

                    @Test
                    fun el_mensaje_de_error_informa_que_se_produjo_un_error_al_intentar_crear_la_compra_y_muestra_el_error()
                    {
                        val observadorDePrueba = modelo.mensajesDeError.test()

                        modelo.pagar()

                        observadorDePrueba.assertValues("", "Error en petición: no es controlado")
                    }

                    @Test
                    fun el_estado_pasa_de_compra_creada_a_confirmando_compra_y_finalmente_vuelve_a_compra_creada()
                    {
                        modelo.pagar()

                        observadorDeEstado.assertValues(
                                ProcesoPagarUI.Estado.COMPRA_CREADA,
                                ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA,
                                ProcesoPagarUI.Estado.COMPRA_CREADA
                                                       )
                    }

                    @Test
                    fun los_creditos_a_codificar_no_emite()
                    {
                        val observadorDePrueba = modelo.creditosACodificar.test()

                        modelo.pagar()

                        observadorDePrueba.assertEmpty()
                    }
                }
            }
        }
    }

    private fun crearMocksDeModelosUIInternosYApiDePrueba(mockRespuestaApi: RespuestaIndividual<Compra>)
    {
        val creditosAPagar = crearCrearMocksCreditosAPagar()
        val mockListadoPersonas = mockConDefaultAnswer(ListaFiltrableUI::class.java).also {
            doReturn(
                    listOf(
                            ProcesoSeleccionCreditosUI
                                .CreditosPorPersonaAProcesar(
                                        mockConDefaultAnswer(PersonaConGrupoCliente::class.java),
                                        creditosAPagar.first,
                                        creditosAPagar.second,
                                        listOf(),
                                        listOf()
                                                            )
                          )
                    )
                .`when`(it).items
        }
        doReturn(mockListadoPersonas).`when`(mockTotalAPagarSegunPersonas).listadoDePersonasConCreditos

        val pagos = listOf(Pago(Decimal.UNO, Pago.MetodoDePago.TARJETA_DEBITO, "asdf"))
        doReturn(pagos).`when`(mockPagosDeUnaCompra).pagoActuales

        doReturn(mockRespuestaApi)
            .`when`(mockApiCompras)
            .actualizar(anyString(), cualquiera())
    }

    private fun crearCrearMocksCreditosAPagar(): Pair<List<CreditoFondoConNombre>, List<CreditoPaqueteConNombre>>
    {
        return Pair(
                listOf(CreditoFondoConNombre("Crédito Fondo 0", crearCreditoFondoSegunIndice(0, 1))),
                listOf(CreditoPaqueteConNombre("Crédito Paquete 100", 1, crearCreditoPaqueteSegunIndice(100, 1)))
                   )
    }

    private fun crearCreditoFondoSegunIndice(indice: Int, idPersonaDueña: Long): CreditoFondo
    {
        val fechaActual = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        return CreditoFondo(
                indice.toLong(),
                indice.toLong() + 1,
                Decimal(indice * 10),
                Decimal(indice * 1000),
                Decimal(indice * 150),
                fechaActual.plusDays(indice.toLong()),
                fechaActual.plusDays(indice.toLong()),
                false,
                "Taquilla",
                "Un usuario",
                idPersonaDueña,
                indice.toLong() * 6,
                "código externo fondo  $indice",
                indice.toLong() * 3,
                "un-uuid-de-dispositivo $indice",
                indice.toLong() * 4,
                indice.toLong() * 5
                           )

    }

    private fun crearCreditoPaqueteSegunIndice(indice: Int, idPersonaDueña: Long): CreditoPaquete
    {
        return CreditoPaquete(
                indice.toLong(),
                "código externo paquete $indice",
                (indice..2 + indice).map {
                    crearCreditoFondoSegunIndice(it, idPersonaDueña)
                }
                             )

    }
}