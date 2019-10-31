package co.smartobjects.ui.modelos.selecciondecreditos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.precios.*
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.carritocreditos.ItemCredito
import co.smartobjects.ui.modelos.catalogo.CatalogoUI
import co.smartobjects.ui.modelos.catalogo.Producto
import co.smartobjects.ui.modelos.catalogo.ProductoUI
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondos
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.ui.modelos.menufiltrado.ProveedorIconosCategoriasFiltrado
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.AgrupacionPersonasCarritosDeCreditosUI
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.PersonaConCreditosSeleccionados
import co.smartobjects.utilidades.Decimal
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@DisplayName("ProcesoSeleccionCreditos")
internal class ProcesoSeleccionCreditosPersonaPruebas : PruebasModelosRxBase()
{
    companion object
    {
        private const val ID_UBICACION = 1L
        private const val ID_GRUPO_CLIENTE = 1L
    }

    @Test
    fun los_modelos_hijos_son_los_el_menu_de_filtrado_el_catalogo_de_productos_y_la_agrupacion_de_carritos_de_creditos()
    {
        val mockConfiguracionDeSesion = mockConDefaultAnswer(ContextoDeSesion::class.java)

        val mockMenuFiltrado =
                mockConDefaultAnswer(MenuFiltradoFondosUI::class.java).also {
                    doReturn(Single.just(listOf<MenuFiltradoFondosUI.ItemFiltradoFondoUI<*>>()))
                        .`when`(it)
                        .filtrosDisponibles
                    doNothing().`when`(it).finalizarProceso()
                }

        val mockCatalogo =
                mockConDefaultAnswer(CatalogoUI::class.java).also {
                    doReturn(
                            Observable.just(CatalogoUI.ResultadoCatalogo(listOf(), null))
                            )
                        .`when`(it)
                        .catalogoDeProductos
                    doNothing().`when`(it).finalizarProceso()
                }

        val mockCreditos =
                mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java).also {
                    doReturn(Single.just<List<PersonaConCreditosSeleccionados>>(listOf()))
                        .`when`(it)
                        .creditosAProcesar
                }


        val procesoCreado = ProcesoSeleccionCreditos(mockConfiguracionDeSesion, mockMenuFiltrado, mockCatalogo, mockCreditos)

        assertEquals(procesoCreado.modelosHijos, listOf(mockMenuFiltrado, mockCatalogo, mockCreditos))
    }

    @Nested
    inner class FiltradoDeProductos
    {
        private val criterioDeFiltrado = MenuFiltradoFondosUI.CriterioFiltrado.EsDinero
        private val mockPrecio = mockConDefaultAnswer(PrecioCompleto::class.java).also {
            doReturn(Decimal.UNO).`when`(it).precioConImpuesto
        }
        private val productoFondoEsperado: ProductoFondo =
                ProductoFondo.SinCategoria(
                        mockConDefaultAnswer(Dinero::class.java).also {
                            doReturn(1L).`when`(it).id
                            doReturn("código externo fondo 1").`when`(it).codigoExterno
                            doReturn("Producto Fondo").`when`(it).nombre
                        },
                        mockPrecio
                                          )
        private val productoUI =
                Producto(
                        productoFondoEsperado,
                        mockConDefaultAnswer(ProveedorImagenesProductos::class.java).also {
                            doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                                .`when`(it)
                                .darImagen(anyLong(), eq(false))
                        }
                        )

        private val mockCatalogo =
                mockConDefaultAnswer(CatalogoUI::class.java).also {
                    doReturn(Observable.just(CatalogoUI.ResultadoCatalogo(listOf(productoUI), null)))
                        .`when`(it)
                        .catalogoDeProductos

                    doNothing().`when`(it).filtrarPorCriterio(criterioDeFiltrado)
                }

        @BeforeEach
        fun filtrarProductos()
        {
            val itemFiltrado =
                    MenuFiltradoFondos.ItemFiltradoFondo(
                            "Dinero",
                            mockConDefaultAnswer(ProveedorIconosCategoriasFiltrado.Icono::class.java),
                            criterioDeFiltrado,
                            false
                                                        )

            val mockMenuFiltrado = mockConDefaultAnswer(MenuFiltradoFondosUI::class.java).also {
                doReturn(Single.just(listOf(itemFiltrado)))
                    .`when`(it)
                    .filtrosDisponibles
            }

            val mockConfiguracionDeSesion = mockConDefaultAnswer(ContextoDeSesion::class.java).also {
                doReturn(ID_UBICACION).`when`(it).idUbicacion
            }

            val mockAgrupacionPersonasCarritos =
                    mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java).also {
                        doReturn(Single.just<List<PersonaConCreditosSeleccionados>>(listOf()))
                            .`when`(it)
                            .creditosAProcesar

                        doReturn(Observable.just(false)).`when`(it).estaAgregandoProducto
                    }

            ProcesoSeleccionCreditos(
                    mockConfiguracionDeSesion,
                    mockMenuFiltrado,
                    mockCatalogo,
                    mockAgrupacionPersonasCarritos
                                    )


            itemFiltrado.activarFiltro()
        }

        @Test
        fun funciona_correctamente()
        {
            verify(mockCatalogo).filtrarPorCriterio(criterioDeFiltrado)
        }

        @Test
        @Disabled("Toca ver cómo implementar esto")
        fun no_reinicializa_las_suscripciones_de_producto_y_agrupacion_de_personas_creditos()
        {
        }
    }

    @Nested
    inner class SePuedeAgregarUnProducto
    {
        private val ID_PRODUCTO_CON_PRECIO = 1L

        private val mockConfiguracionDeSesion = mockConDefaultAnswer(ContextoDeSesion::class.java).also {
            doReturn(ID_UBICACION).`when`(it).idUbicacion
        }

        private val mockPrecio = mockConDefaultAnswer(PrecioCompleto::class.java).also {
            doReturn(Decimal.UNO).`when`(it).precioConImpuesto
        }


        private val mockMenuFiltrado = mockConDefaultAnswer(MenuFiltradoFondosUI::class.java).also {
            doReturn(Single.just(emptyList<MenuFiltradoFondosUI.ItemFiltradoFondoUI<ProveedorIconosCategoriasFiltrado.Icono>>()))
                .`when`(it)
                .filtrosDisponibles
        }

        private fun darMockDeCatalogo(vararg instanciaProducto: ProductoUI) =
                mockConDefaultAnswer(CatalogoUI::class.java).also {
                    doReturn(
                            Observable.just(CatalogoUI.ResultadoCatalogo(instanciaProducto.toList(), null))
                            )
                        .`when`(it)
                        .catalogoDeProductos
                }

        @Test
        fun fondo_a_los_creditos()
        {
            val productoFondoEsperado =
                    ProductoFondo.SinCategoria(
                            mockConDefaultAnswer(Dinero::class.java).also {
                                doReturn(ID_PRODUCTO_CON_PRECIO).`when`(it).id
                                doReturn("código externo fondo $ID_PRODUCTO_CON_PRECIO").`when`(it).codigoExterno
                                doReturn("Producto Fondo").`when`(it).nombre
                            },
                            mockPrecio
                                              )

            val productoUI =
                    Producto(
                            productoFondoEsperado,
                            mockConDefaultAnswer(ProveedorImagenesProductos::class.java).also {
                                doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                                    .`when`(it)
                                    .darImagen(anyLong(), eq(false))
                            }
                            )

            val mockCatalogo = darMockDeCatalogo(productoUI)

            val mockAgrupacionPersonasCarritos =
                    mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java).also {
                        doNothing().`when`(it).agregarProducto(productoUI)
                        doReturn(Single.just<List<PersonaConCreditosSeleccionados>>(listOf()))
                            .`when`(it)
                            .creditosAProcesar
                        doReturn(Observable.just(false)).`when`(it).estaAgregandoProducto
                    }

            ProcesoSeleccionCreditos(
                    mockConfiguracionDeSesion,
                    mockMenuFiltrado,
                    mockCatalogo,
                    mockAgrupacionPersonasCarritos
                                    )

            productoUI.agregar()

            verify(mockAgrupacionPersonasCarritos).agregarProducto(productoUI)
        }

        @Test
        fun paquete_a_los_creditos()
        {
            val productoPaquete =
                    ProductoPaquete(
                            mockConDefaultAnswer(Paquete::class.java).also {
                                doReturn(147258L).`when`(it).id
                                doReturn("código externo paquete").`when`(it).codigoExterno
                                doReturn("Producto Paquete").`when`(it).nombre
                                doReturn(listOf(Paquete.FondoIncluido(ID_PRODUCTO_CON_PRECIO, "código externo fondo $ID_PRODUCTO_CON_PRECIO", Decimal.UNO))).`when`(it).fondosIncluidos
                            },
                            listOf("Nombre dummy de prueba"),
                            listOf(mockPrecio)
                                   )

            val productoEsperado =
                    Producto(
                            productoPaquete,
                            mockConDefaultAnswer(ProveedorImagenesProductos::class.java).also {
                                doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                                    .`when`(it)
                                    .darImagen(anyLong(), eq(true))
                            }
                            )

            val mockCatalogo = darMockDeCatalogo(productoEsperado)

            val mockAgrupacionPersonasCarritos =
                    mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java).also {
                        doNothing().`when`(it).agregarProducto(productoEsperado)
                        doReturn(Single.just<List<PersonaConCreditosSeleccionados>>(listOf()))
                            .`when`(it)
                            .creditosAProcesar
                        doReturn(Observable.just(false)).`when`(it).estaAgregandoProducto
                    }

            ProcesoSeleccionCreditos(mockConfiguracionDeSesion, mockMenuFiltrado, mockCatalogo, mockAgrupacionPersonasCarritos)

            productoEsperado.agregar()

            verify(mockAgrupacionPersonasCarritos).agregarProducto(productoEsperado)
        }

        @Test
        fun cuando_se_termine_de_agregar_se_le_informa_a_cada_producto()
        {
            val schedulerEstaAgregando = TestScheduler()
            val schedulerQueNoEmite = TestScheduler()
            val productoUI1 = mockConDefaultAnswer(ProductoUI::class.java).also {
                doReturn(Observable.just(it).subscribeOn(schedulerQueNoEmite).observeOn(schedulerQueNoEmite))
                    .`when`(it)
                    .productoAAgregar

                doNothing().`when`(it).terminarAgregar()
                doReturn("asdfasdf").`when`(it).toString()
            }
            val productoUI2 = mockConDefaultAnswer(ProductoUI::class.java).also {
                doReturn(Observable.just(it).subscribeOn(schedulerQueNoEmite).observeOn(schedulerQueNoEmite))
                    .`when`(it)
                    .productoAAgregar
                doNothing().`when`(it).terminarAgregar()
                doReturn("asdfasdf").`when`(it).toString()
            }

            val mockCatalogo = darMockDeCatalogo(productoUI1, productoUI2)

            val mockAgrupacionPersonasCarritos =
                    mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java).also {
                        doReturn(Single.just<List<PersonaConCreditosSeleccionados>>(listOf()))
                            .`when`(it)
                            .creditosAProcesar

                        doReturn(Observable.just(false).subscribeOn(schedulerEstaAgregando).observeOn(schedulerEstaAgregando))
                            .`when`(it)
                            .estaAgregandoProducto
                    }

            ProcesoSeleccionCreditos(
                    mockConfiguracionDeSesion,
                    mockMenuFiltrado,
                    mockCatalogo,
                    mockAgrupacionPersonasCarritos
                                    )

            schedulerEstaAgregando.triggerActions()

            verify(productoUI1).terminarAgregar()
            verify(productoUI2).terminarAgregar()
        }
    }

    @Nested
    inner class CreditosPorPersonaAProcesar
    {
        private val ID_CLIENTE = 1L
        private val ORIGEN = "Caja"
        private val DISPOSITIVO = "Android"
        private val USUARIO = "Usuario"
        private val ID_PERSONA = 987L

        private val ID_FONDO = 1L
        private val CODIGO_EXTERNO_FONDO = "código externo $ID_FONDO"
        private val CANTIDAD_FONDO = 4
        private val IMPUESTO = ImpuestoSoloTasa(1, 1, Decimal(10))
        private val PRECIO_FONDO = PrecioCompleto(Precio(Decimal(100), 1), IMPUESTO)

        private val ID_PAQUETE = 45456L
        private val CODIGO_EXTERNO_PAQUETE = "código externo paquete"
        private val IDS_FONDOS_EN_PAQUETE = listOf(2L, 3L)
        private val CODIGOS_EXTERNOS_FONDOS_EN_PAQUETE = IDS_FONDOS_EN_PAQUETE.map { "código externo $it" }
        private val CANTIDAD_PAQUETE = 1
        private val PRECIOS_PAQUETE = listOf(
                PrecioCompleto(Precio(Decimal(200), 1), IMPUESTO),
                PrecioCompleto(Precio(Decimal(300), 1), IMPUESTO)
                                            )
        private val CANTIDADES_EN_PAQUETE = listOf(Decimal(5), Decimal(13))

        private val grupoDeclientesDePrueba =
                GrupoClientes(
                        ID_GRUPO_CLIENTE,
                        "Grupo de prueba",
                        listOf(SegmentoClientes(1, SegmentoClientes.NombreCampo.CATEGORIA, Persona.Categoria.D.name))
                             )

        private val personaDePrueba = Persona(
                1,
                ID_PERSONA,
                "nombre",
                Persona.TipoDocumento.CC,
                "1234",
                Persona.Genero.MASCULINO,
                LocalDate.now(),
                Persona.Categoria.A,
                Persona.Afiliacion.COTIZANTE,
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                                             )

        private val itemCreditoFondoAPagar =
                ItemCredito(
                        "Producto Fondo A Pagar", ID_FONDO, CODIGO_EXTERNO_FONDO, PRECIO_FONDO, null, false,
                        CANTIDAD_FONDO, false, false
                           )
        private val itemCreditoFondoPagado =
                ItemCredito(
                        "Producto Fondo Pagado", ID_FONDO, CODIGO_EXTERNO_FONDO, PRECIO_FONDO, null, false,
                        CANTIDAD_FONDO, false, false
                           )

        private val itemCreditoPaqueteAPagar =
                ItemCredito(
                        "Producto Paquete A Pagar", ID_PAQUETE, CODIGO_EXTERNO_PAQUETE,
                        IDS_FONDOS_EN_PAQUETE, CODIGOS_EXTERNOS_FONDOS_EN_PAQUETE, PRECIOS_PAQUETE, null,
                        false, CANTIDADES_EN_PAQUETE, CANTIDAD_PAQUETE, false, false
                           )

        private val itemCreditoPaquetePagado =
                ItemCredito(
                        "Producto Paquete Pagado", ID_PAQUETE, CODIGO_EXTERNO_PAQUETE,
                        IDS_FONDOS_EN_PAQUETE, CODIGOS_EXTERNOS_FONDOS_EN_PAQUETE, PRECIOS_PAQUETE, null,
                        false, CANTIDADES_EN_PAQUETE, CANTIDAD_PAQUETE, false, false
                           )

        private val creditosAProcesarPrueba =
                listOf(
                        PersonaConCreditosSeleccionados(personaDePrueba, grupoDeclientesDePrueba, listOf(itemCreditoFondoAPagar), listOf(itemCreditoFondoPagado)),
                        PersonaConCreditosSeleccionados(personaDePrueba, grupoDeclientesDePrueba, listOf(itemCreditoPaqueteAPagar), listOf(itemCreditoPaquetePagado))
                      )

        private lateinit var spyProcesoSeleccionCreditosPersona: ProcesoSeleccionCreditos<*>

        @BeforeEach
        fun inicializarProceso()
        {
            val mockConfiguracionDeSesion = ContextoDeSesionImpl(
                    ID_CLIENTE, ORIGEN, USUARIO, DISPOSITIVO, ID_UBICACION
                                                                )

            val mockMenuFiltrado = mockConDefaultAnswer(MenuFiltradoFondosUI::class.java).also {
                doReturn(Single.just(listOf<MenuFiltradoFondosUI.ItemFiltradoFondoUI<*>>()))
                    .`when`(it)
                    .filtrosDisponibles
                doNothing().`when`(it).finalizarProceso()
            }

            val mockCatalogo =
                    mockConDefaultAnswer(CatalogoUI::class.java).also {
                        doReturn(Observable.just(CatalogoUI.ResultadoCatalogo(listOf(), null)))
                            .`when`(it)
                            .catalogoDeProductos

                        doNothing().`when`(it).finalizarProceso()
                    }

            val mockCreditos = mockConDefaultAnswer(AgrupacionPersonasCarritosDeCreditosUI::class.java).also {
                doReturn(Single.just<List<PersonaConCreditosSeleccionados>>(creditosAProcesarPrueba))
                    .`when`(it)
                    .creditosAProcesar

                doNothing().`when`(it).finalizarProceso()
            }

            spyProcesoSeleccionCreditosPersona =
                    spy(
                            ProcesoSeleccionCreditos(
                                    mockConfiguracionDeSesion,
                                    mockMenuFiltrado,
                                    mockCatalogo,
                                    mockCreditos
                                                    )
                       )
                        .also {
                            doNothing().`when`(it).finalizarProceso()
                        }
        }

        @Test
        fun se_generan_los_resultados_correctos()
        {
            val creditoFondoEsperado =
                    CreditoFondo(
                            ID_CLIENTE,
                            null,
                            Decimal(CANTIDAD_FONDO),
                            PRECIO_FONDO.precioConImpuesto * CANTIDAD_FONDO,
                            PRECIO_FONDO.valorImpuesto * CANTIDAD_FONDO,
                            null,
                            null,
                            false,
                            ORIGEN,
                            USUARIO,
                            ID_PERSONA,
                            ID_FONDO,
                            CODIGO_EXTERNO_FONDO,
                            IMPUESTO.id!!,
                            DISPOSITIVO,
                            ID_UBICACION,
                            ID_GRUPO_CLIENTE
                                )

            val creditoPaqueteEsperado =
                    CreditoPaquete(
                            ID_PAQUETE,
                            CODIGO_EXTERNO_PAQUETE,
                            listOf(
                                    CreditoFondo(
                                            ID_CLIENTE,
                                            null,
                                            CANTIDADES_EN_PAQUETE.first() * CANTIDAD_PAQUETE,
                                            PRECIOS_PAQUETE.first().precioConImpuesto * (CANTIDADES_EN_PAQUETE.first() * CANTIDAD_PAQUETE),
                                            PRECIOS_PAQUETE.first().valorImpuesto * (CANTIDADES_EN_PAQUETE.first() * CANTIDAD_PAQUETE),
                                            null,
                                            null,
                                            false,
                                            ORIGEN,
                                            USUARIO,
                                            ID_PERSONA,
                                            IDS_FONDOS_EN_PAQUETE.first(),
                                            CODIGOS_EXTERNOS_FONDOS_EN_PAQUETE.first(),
                                            IMPUESTO.id!!,
                                            DISPOSITIVO,
                                            ID_UBICACION,
                                            ID_GRUPO_CLIENTE
                                                ),
                                    CreditoFondo(
                                            ID_CLIENTE,
                                            null,
                                            CANTIDADES_EN_PAQUETE.last() * CANTIDAD_PAQUETE,
                                            PRECIOS_PAQUETE.last().precioConImpuesto * (CANTIDADES_EN_PAQUETE.last() * CANTIDAD_PAQUETE),
                                            PRECIOS_PAQUETE.last().valorImpuesto * (CANTIDADES_EN_PAQUETE.last() * CANTIDAD_PAQUETE),
                                            null,
                                            null,
                                            false,
                                            ORIGEN,
                                            USUARIO,
                                            ID_PERSONA,
                                            IDS_FONDOS_EN_PAQUETE.last(),
                                            CODIGOS_EXTERNOS_FONDOS_EN_PAQUETE.last(),
                                            IMPUESTO.id!!,
                                            DISPOSITIVO,
                                            ID_UBICACION,
                                            ID_GRUPO_CLIENTE
                                                )
                                  )
                                  )

            val resultadosEsperados =
                    listOf(
                            ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                    PersonaConGrupoCliente(personaDePrueba, grupoDeclientesDePrueba),
                                    listOf(
                                            CreditoFondoConNombre(
                                                    itemCreditoFondoAPagar.nombreProducto,
                                                    creditoFondoEsperado
                                                                 )
                                          ),
                                    listOf(),
                                    listOf(
                                            CreditoFondoConNombre(
                                                    itemCreditoFondoPagado.nombreProducto,
                                                    creditoFondoEsperado
                                                                 )
                                          ),
                                    listOf()
                                                                                  ),
                            ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                                    PersonaConGrupoCliente(personaDePrueba, grupoDeclientesDePrueba),
                                    listOf(),
                                    listOf(
                                            CreditoPaqueteConNombre(
                                                    itemCreditoPaqueteAPagar.nombreProducto,
                                                    1,
                                                    creditoPaqueteEsperado
                                                                   )
                                          ),
                                    listOf(),
                                    listOf(
                                            CreditoPaqueteConNombre(
                                                    itemCreditoPaquetePagado.nombreProducto,
                                                    1,
                                                    creditoPaqueteEsperado
                                                                   )
                                          )
                                                                                  )
                          )

            val observableDePrueba = spyProcesoSeleccionCreditosPersona.creditosPorPersonaAProcesar.test()

            observableDePrueba.assertResult(resultadosEsperados)
        }

        @Test
        @Disabled("El Spy no está funcionando y no se llama el método. En ejecución normal sí funciona")
        fun al_emitir_se_finaliza_el_proceso()
        {
            val observableDePrueba = spyProcesoSeleccionCreditosPersona.creditosPorPersonaAProcesar.test()
            assertTrue(observableDePrueba.valueCount() > 0)

            verify(spyProcesoSeleccionCreditosPersona).finalizarProceso()
        }
    }
}

internal class CreditosPorPersonaAProcesarPruebas
{
    @Test
    fun los_creditos_fondo_totales_es_la_concatenacion_de_todos_los_creditos_fondo_incluyendo_los_de_los_paquetes()
    {
        val idPersona = 1L
        val creditosFondoAPagar = List(2) { crearCreditoFondo(it + 1, idPersona) }
        val creditosPaqueteAPagar = List(2) { crearCreditoPaquete(it + 10, idPersona) }
        val creditosFondoPagados = List(2) { crearCreditoFondo(it + 100, idPersona) }
        val creditosPaquetePagados = List(2) { crearCreditoPaquete(it + 1000, idPersona) }

        val creditosTotalesEsperados =
                (
                        creditosFondoAPagar.asSequence() +
                        creditosFondoPagados.asSequence() +
                        creditosPaqueteAPagar.asSequence().flatMap { it.creditosFondos.asSequence() } +
                        creditosPaquetePagados.asSequence().flatMap { it.creditosFondos.asSequence() }
                ).toList()

        val entidad = ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar(
                mockConDefaultAnswer(PersonaConGrupoCliente::class.java),
                creditosFondoAPagar.map { CreditoFondoConNombre("no importa", it) },
                creditosPaqueteAPagar.map { CreditoPaqueteConNombre("no importa", 1, it) },
                creditosFondoPagados.map { CreditoFondoConNombre("no importa", it) },
                creditosPaquetePagados.map { CreditoPaqueteConNombre("no importa", 1, it) }
                                                                            )

        assertEquals(creditosTotalesEsperados, entidad.creditosFondoTotales)
    }
}