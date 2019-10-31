package co.smartobjects.ui.modelos.carritocreditos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.logica.fondos.CalculadorPuedeAgregarseSegunUnicidad
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.catalogo.Producto
import co.smartobjects.ui.modelos.catalogo.ProductoUI
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoFondo
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoPaquete
import co.smartobjects.utilidades.Decimal
import io.reactivex.Maybe
import io.reactivex.functions.Predicate
import io.reactivex.observers.TestObserver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.doReturn
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class CarritoDeCreditosPruebas : PruebasModelosRxBase()
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
    }

    private val DINERO_CREDITO = Decimal(10_000)

    private val creditosFondoPreIncluidos =
            List(4) {
                val precio = PrecioCompleto(
                        Precio(Decimal(1_000 * (1 + it)), it.toLong()),
                        ImpuestoSoloTasa(1, it.toLong(), Decimal(10 + it))
                                           )
                val cantidadInicial = it + 1
                val idFondo = (it + 1).toLong()
                val idGenerado = (it + 100).toLong().takeIf { it % 2 == 0L }

                CreditoFondoConNombre(
                        "(No Modificable-${if (idGenerado == null) "NO pagado" else "Pagado"}) Fondo $idFondo",
                        CreditoFondo(
                                idCliente = ID_CLIENTE,
                                id = idGenerado,
                                cantidad = Decimal(cantidadInicial),
                                valorPagado = precio.precioConImpuesto * cantidadInicial,
                                valorImpuestoPagado = precio.valorImpuesto * cantidadInicial,
                                validoDesde = null,
                                validoHasta = null,
                                consumido = false,
                                origen = ORIGEN,
                                nombreUsuario = NOMBRE_USUARIO,
                                idPersonaDueña = ID_PERSONA,
                                idFondoComprado = idFondo,
                                codigoExternoFondo = "código externo $idFondo",
                                idImpuestoPagado = precio.impuesto.id!!,
                                idDispositivo = ID_DISPOSITIVO,
                                idUbicacionCompra = ID_UBICACION,
                                idGrupoClientesPersona = ID_GRUPO_CLIENTE
                                    )
                                     )
            }

    private val creditosPaquetePreIncluidos =
            listOf(
                    CreditoPaqueteConNombre(
                            "(No Modificable-Pagado) Paquete 1",
                            1,
                            CreditoPaquete(
                                    1L,
                                    "código externo paquete",
                                    creditosFondoPreIncluidos.asSequence().filter { it.estaPagado }.map { it.creditoAsociado }.toList()
                                          )
                                           )

                  )

    private val creditosPreIncluidos =
            (
                    creditosFondoPreIncluidos.asSequence().map {
                        ItemCredito(
                                it.nombreDeFondo,
                                it.creditoAsociado.idFondoComprado,
                                it.creditoAsociado.codigoExternoFondo,
                                it.precioCompleto,
                                it.creditoAsociado.id,
                                false,
                                it.creditoAsociado.cantidad.valor.toInt(),
                                true,
                                false
                                   )
                    } +
                    creditosPaquetePreIncluidos.asSequence().map {
                        ItemCredito(
                                it.nombreDelPaquete,
                                it.creditoAsociado.idPaquete,
                                it.creditoAsociado.codigoExternoPaquete,
                                it.creditoAsociado.creditosFondos.map { it.idFondoComprado },
                                it.creditoAsociado.creditosFondos.map { it.codigoExternoFondo },
                                it.preciosCompletos,
                                if (it.estaPagado) it.creditoAsociado.creditosFondos.map { it.id!! } else null,
                                false,
                                it.creditoAsociado.creditosFondos.map { it.cantidad },
                                1,
                                true,
                                false
                                   )
                    }
            ).toList()


    private val totalSinImpuestoDePreIncluidos =
            creditosFondoPreIncluidos
                .asSequence()
                .map { it.creditoAsociado.valorPagadoSinImpuesto }
                .plus(creditosPaquetePreIncluidos.asSequence().map { it.creditoAsociado.valorPagadoSinImpuesto })
                .reduce { acc, valorPagado -> acc + valorPagado }

    private val totalImpuestoDePreIncluidos =
            creditosFondoPreIncluidos
                .asSequence()
                .map { it.creditoAsociado.valorImpuestoPagado }
                .plus(creditosPaquetePreIncluidos.asSequence().map { it.creditoAsociado.valorImpuestoPagado })
                .reduce { acc, valorPagado -> acc + valorPagado }


    private val mockDeProveedorDeImagenesDeProductos =
            mockConDefaultAnswer(ProveedorImagenesProductos::class.java)
                .also {
                    doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                        .`when`(it)
                        .darImagen(anyLong(), ArgumentMatchers.anyBoolean())
                }

    private val mockCalculadorPuedeAgregarseSegunUnicidad =
            mockConDefaultAnswer(CalculadorPuedeAgregarseSegunUnicidad::class.java).also {
                doReturn(false).`when`(it).algunoEsUnico(cualquiera())
            }

    private val modelo =
            CarritoDeCreditos(
                    DINERO_CREDITO,
                    creditosFondoPreIncluidos,
                    creditosPaquetePreIncluidos,
                    mockCalculadorPuedeAgregarseSegunUnicidad
                             )

    @Test
    fun los_modelos_hijos_son_al_inicio_solo_los_creditos_pre_incluidos()
    {
        assertEquals(creditosPreIncluidos, modelo.modelosHijos)
    }

    @Test
    fun si_no_hay_ni_fondos_ni_paquetes_pre_incluidos_los_creditos_preincluidos_estan_vacios()
    {
        val modelo =
                CarritoDeCreditos(
                        DINERO_CREDITO,
                        listOf(),
                        listOf(),
                        mockCalculadorPuedeAgregarseSegunUnicidad
                                 )

        assertTrue(modelo.creditosPreIncluidos.isEmpty())
    }

    @Nested
    inner class CreditosAgregados
    {
        private val observableDePrueba = modelo.creditosAgregados.test()!!

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun los_modelos_hijos_son_solo_los_creditos_pre_incluidos()
            {
                assertEquals(creditosPreIncluidos, modelo.modelosHijos)
            }

            @Test
            fun los_creditos_agregados_estan_vacios()
            {
                observableDePrueba.assertEmpty()
            }
        }

        @Nested
        inner class AlAgregar
        {
            @Nested
            inner class UnCreditoDeFondo
            {
                @Test
                fun los_modelos_hijos_se_actualizan_correctamente()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    val creditoEsperado =
                            ItemCredito(
                                    creditoDeReferencia.nombreDeFondo,
                                    creditoDeReferencia.creditoAsociado.idFondoComprado,
                                    creditoDeReferencia.creditoAsociado.codigoExternoFondo,
                                    creditoDeReferencia.precioCompleto,
                                    null,
                                    false,
                                    1,
                                    false,
                                    true
                                       )

                    modelo.agregarAlCarrito(productoAAgregar)

                    assertEquals(creditosPreIncluidos.toMutableList() + creditoEsperado, modelo.modelosHijos)
                }

                @Test
                fun se_actualiza_correctamente()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    val creditoEsperado =
                            ItemCredito(
                                    creditoDeReferencia.nombreDeFondo,
                                    creditoDeReferencia.creditoAsociado.idFondoComprado,
                                    creditoDeReferencia.creditoAsociado.codigoExternoFondo,
                                    creditoDeReferencia.precioCompleto,
                                    null,
                                    false,
                                    1,
                                    false,
                                    true
                                       )

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableDePrueba.assertValuesOnly(listOf(creditoEsperado))
                }

                @Test
                fun al_agregar_dos_o_mas_veces_se_agrega_el_primero_y_los_restantes_solo_suman_a_la_cantidad()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first()
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    val cantidadASumar = 3
                    val creditoEsperado =
                            ItemCredito(
                                    creditoDeReferencia.nombreDeFondo,
                                    creditoDeReferencia.creditoAsociado.idFondoComprado,
                                    creditoDeReferencia.creditoAsociado.codigoExternoFondo,
                                    creditoDeReferencia.precioCompleto,
                                    null,
                                    false,
                                    cantidadASumar,
                                    false,
                                    true
                                       )

                    (1..cantidadASumar).forEach {
                        modelo.agregarAlCarrito(productoAAgregar)
                    }

                    observableDePrueba.assertValuesOnly(listOf(creditoEsperado))
                }

                @Test
                fun al_agregar_un_producto_ya_agregado_como_pre_incluido_agrega_el_producto_y_no_cambia_la_cantidad_del_otro()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first()
                    val cantidadDeCreditoPreIncluido = creditoDeReferencia.creditoAsociado.cantidad
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    val creditoEsperado =
                            ItemCredito(
                                    creditoDeReferencia.nombreDeFondo,
                                    creditoDeReferencia.creditoAsociado.idFondoComprado,
                                    creditoDeReferencia.creditoAsociado.codigoExternoFondo,
                                    creditoDeReferencia.precioCompleto,
                                    null,
                                    false,
                                    1,
                                    false,
                                    true
                                       )

                    modelo.agregarAlCarrito(productoAAgregar)

                    assertEquals(cantidadDeCreditoPreIncluido, creditoDeReferencia.creditoAsociado.cantidad)
                    observableDePrueba.assertValuesOnly(listOf(creditoEsperado))
                }

                @Test
                fun al_cancelar_creditos_agregados_borra_los_creditos_sin_confirmar()
                {
                    val creditoAConfirmar = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo a confirmar")
                    val productoAConfirmar = deCreditoFondoConNombreAProductoFondo(creditoAConfirmar)
                    val creditoQueNoSeConfirma = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo que no se confirma")
                    val productoQueNoSeConfirma = deCreditoFondoConNombreAProductoFondo(creditoQueNoSeConfirma)

                    val creditoConfirmadoEsperado =
                            ItemCredito(
                                    creditoAConfirmar.nombreDeFondo,
                                    creditoAConfirmar.creditoAsociado.idFondoComprado,
                                    creditoAConfirmar.creditoAsociado.codigoExternoFondo,
                                    creditoAConfirmar.precioCompleto,
                                    null,
                                    false,
                                    1,
                                    false,
                                    true
                                       )

                    val creditoQueNoSeConfirmaEsperado =
                            ItemCredito(
                                    creditoQueNoSeConfirma.nombreDeFondo,
                                    creditoQueNoSeConfirma.creditoAsociado.idFondoComprado,
                                    creditoQueNoSeConfirma.creditoAsociado.codigoExternoFondo,
                                    creditoQueNoSeConfirma.precioCompleto,
                                    null,
                                    false,
                                    1,
                                    false,
                                    true
                                       )

                    modelo.agregarAlCarrito(productoAConfirmar)
                    modelo.confirmarCreditosAgregados()
                    modelo.agregarAlCarrito(productoQueNoSeConfirma)
                    modelo.cancelarCreditosAgregados()

                    observableDePrueba.assertValuesOnly(
                            listOf(creditoConfirmadoEsperado),
                            listOf(creditoQueNoSeConfirmaEsperado, creditoConfirmadoEsperado),
                            listOf(creditoConfirmadoEsperado)
                                                       )
                }

                @Test
                fun al_cancelar_un_producto_que_sumo_cantidad_se_revierte_la_cantidad_sumada()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first()
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    val creditoEsperado =
                            ItemCredito(
                                    creditoDeReferencia.nombreDeFondo,
                                    creditoDeReferencia.creditoAsociado.idFondoComprado,
                                    creditoDeReferencia.creditoAsociado.codigoExternoFondo,
                                    creditoDeReferencia.precioCompleto,
                                    null,
                                    false,
                                    1,
                                    false,
                                    false
                                       )

                    modelo.agregarAlCarrito(productoAAgregar)
                    modelo.confirmarCreditosAgregados()

                    (1..3).forEach {
                        modelo.agregarAlCarrito(productoAAgregar)
                    }
                    modelo.cancelarCreditosAgregados()

                    observableDePrueba.assertValuesOnly(listOf(creditoEsperado))
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                private val paquete =
                        mockConDefaultAnswer(Paquete::class.java).also {
                            doReturn(4567L).`when`(it).id
                            doReturn("código externo paquete 457").`when`(it).codigoExterno
                            doReturn("Producto Paquete").`when`(it).nombre
                            doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo", Decimal(13))))
                                .`when`(it)
                                .fondosIncluidos
                        }

                private val productoUI =
                        Producto(
                                ProductoPaquete(
                                        paquete,
                                        listOf("Nombre dummy de prueba"),
                                        listOf(precio)
                                               ),
                                mockDeProveedorDeImagenesDeProductos
                                )

                private val creditoEsperado =
                        ItemCredito(
                                productoUI.nombre,
                                productoUI.idPaquete!!,
                                productoUI.codigoExternoPaquete!!,
                                productoUI.idsFondosAsociados.toList(),
                                productoUI.codigosExternosAsociados,
                                productoUI.preciosDeFondosAsociados,
                                null,
                                false,
                                productoUI.cantidadesFondosEnPaquete!!,
                                1,
                                false,
                                true
                                   )

                @Test
                fun los_modelos_hijos_se_actualizan_correctamente()
                {
                    modelo.agregarAlCarrito(productoUI)

                    assertEquals(creditosPreIncluidos.toMutableList() + creditoEsperado, modelo.modelosHijos)
                }

                @Test
                fun se_actualiza_correctamente()
                {
                    modelo.agregarAlCarrito(productoUI)

                    observableDePrueba.assertValuesOnly(listOf(creditoEsperado))
                }

                @Test
                fun al_agregar_dos_o_mas_veces_se_agrega_el_primero_y_los_restantes_solo_suman_a_la_cantidad()
                {
                    val cantidadASumar = 3
                    val creditoEsperado =
                            ItemCredito(
                                    productoUI.nombre,
                                    productoUI.idPaquete!!,
                                    productoUI.codigoExternoPaquete!!,
                                    productoUI.idsFondosAsociados.toList(),
                                    productoUI.codigosExternosAsociados,
                                    productoUI.preciosDeFondosAsociados,
                                    null,
                                    false,
                                    productoUI.cantidadesFondosEnPaquete!!,
                                    cantidadASumar,
                                    false,
                                    true
                                       )

                    (1..cantidadASumar).forEach {
                        modelo.agregarAlCarrito(productoUI)
                    }

                    observableDePrueba.assertValuesOnly(listOf(creditoEsperado))
                }

                @Test
                fun al_agregar_un_producto_ya_agregado_como_pre_incluido_agrega_el_producto_y_no_cambia_la_cantidad_del_otro()
                {
                    val creditoPaqueteDeReferencia = creditosPaquetePreIncluidos.first()
                    val productoPaqueteAAgregar =
                            Producto(
                                    ProductoPaquete(
                                            mockConDefaultAnswer(Paquete::class.java).also {
                                                doReturn(creditoPaqueteDeReferencia.creditoAsociado.idPaquete)
                                                    .`when`(it)
                                                    .id

                                                doReturn(creditoPaqueteDeReferencia.creditoAsociado.codigoExternoPaquete)
                                                    .`when`(it)
                                                    .codigoExterno

                                                doReturn(creditoPaqueteDeReferencia.nombreDelPaquete)
                                                    .`when`(it)
                                                    .nombre

                                                val fondosIncluidos =
                                                        creditoPaqueteDeReferencia.creditoAsociado.creditosFondos
                                                            .map { Paquete.FondoIncluido(it.idFondoComprado, it.codigoExternoFondo, it.cantidad) }
                                                doReturn(fondosIncluidos)
                                                    .`when`(it)
                                                    .fondosIncluidos
                                            },
                                            listOf("Nombre dummy de prueba"),
                                            creditoPaqueteDeReferencia.preciosCompletos
                                                   ), mockDeProveedorDeImagenesDeProductos
                                    )
                    val creditoEsperado =
                            ItemCredito(
                                    productoPaqueteAAgregar.nombre,
                                    productoPaqueteAAgregar.idPaquete!!,
                                    productoPaqueteAAgregar.codigoExternoPaquete!!,
                                    productoPaqueteAAgregar.idsFondosAsociados.toList(),
                                    productoPaqueteAAgregar.codigosExternosAsociados,
                                    productoPaqueteAAgregar.preciosDeFondosAsociados,
                                    null,
                                    false,
                                    productoPaqueteAAgregar.cantidadesFondosEnPaquete!!,
                                    1,
                                    false,
                                    true
                                       )

                    val mapaObservadoresCantidades =
                            modelo.creditosPreIncluidos.associate { Pair(it.hashCode(), it.cantidad.test()) }


                    modelo.agregarAlCarrito(productoPaqueteAAgregar)


                    val hashCodeBuscado =
                            modelo.creditosPreIncluidos.first {
                                it.nombreProducto == productoPaqueteAAgregar.nombre
                                && it.idFondos == productoPaqueteAAgregar.idsFondosAsociados.toList()
                                && it.esPaquete == productoPaqueteAAgregar.esPaquete
                            }.hashCode()

                    val creditoBuscado = mapaObservadoresCantidades[hashCodeBuscado]!!

                    creditoBuscado.assertResult(1)
                    observableDePrueba.assertValuesOnly(listOf(creditoEsperado))
                }

                @Test
                fun al_cancelar_creditos_agregados_borra_los_creditos_sin_confirmar()
                {
                    val paquete =
                            mockConDefaultAnswer(Paquete::class.java).also {
                                doReturn(789789789L).`when`(it).id
                                doReturn("código externo paquete 7787978").`when`(it).codigoExterno
                                doReturn("Producto Paquete que no se confirma").`when`(it).nombre
                                doReturn(listOf(Paquete.FondoIncluido(487574L, "código externo fondo 459345", Decimal(13))))
                                    .`when`(it)
                                    .fondosIncluidos
                            }

                    val productoQueNoSeConfirma =
                            Producto(
                                    ProductoPaquete(
                                            paquete,
                                            listOf("Paquete que no se confirma"),
                                            listOf(precio)
                                                   ),
                                    mockDeProveedorDeImagenesDeProductos
                                    )

                    val creditoQueNoSeConfirmaEsperado =
                            ItemCredito(
                                    productoQueNoSeConfirma.nombre,
                                    productoQueNoSeConfirma.idPaquete!!,
                                    productoQueNoSeConfirma.codigoExternoPaquete!!,
                                    productoQueNoSeConfirma.idsFondosAsociados.toList(),
                                    productoQueNoSeConfirma.codigosExternosAsociados,
                                    productoQueNoSeConfirma.preciosDeFondosAsociados,
                                    null,
                                    false,
                                    productoQueNoSeConfirma.cantidadesFondosEnPaquete!!,
                                    1,
                                    false,
                                    true
                                       )

                    modelo.agregarAlCarrito(productoUI)
                    modelo.confirmarCreditosAgregados()
                    modelo.agregarAlCarrito(productoQueNoSeConfirma)
                    modelo.cancelarCreditosAgregados()

                    observableDePrueba.assertValuesOnly(
                            listOf(creditoEsperado),
                            listOf(creditoQueNoSeConfirmaEsperado, creditoEsperado),
                            listOf(creditoEsperado)
                                                       )
                }

                @Test
                fun al_cancelar_un_producto_que_sumo_cantidad_se_revierte_la_cantidad_sumada()
                {
                    val creditoEsperado =
                            ItemCredito(
                                    productoUI.nombre,
                                    productoUI.idPaquete!!,
                                    productoUI.codigoExternoPaquete!!,
                                    productoUI.idsFondosAsociados.toList(),
                                    productoUI.codigosExternosAsociados,
                                    productoUI.preciosDeFondosAsociados,
                                    null,
                                    false,
                                    productoUI.cantidadesFondosEnPaquete!!,
                                    1,
                                    false,
                                    true
                                       )

                    modelo.agregarAlCarrito(productoUI)
                    modelo.confirmarCreditosAgregados()

                    (1..3).forEach {
                        modelo.agregarAlCarrito(productoUI)
                    }
                    modelo.cancelarCreditosAgregados()

                    observableDePrueba.assertValuesOnly(listOf(creditoEsperado))
                }
            }
        }

        @Nested
        inner class AlRemover
        {
            @Nested
            inner class UnCreditoDeFondo
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableDePrueba.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableDePrueba.ultimoEmitido().first { it.nombreProducto == creditoDeReferencia.nombreDeFondo }
                }

                @Test
                fun los_modelos_hijos_se_actualizan_correctamente()
                {
                    itemCreditoAgregado.borrar()

                    assertEquals(creditosPreIncluidos, modelo.modelosHijos)
                }

                @Test
                fun se_actualiza_correctamente()
                {
                    itemCreditoAgregado.borrar()

                    observableDePrueba.assertValueAt(1) { it.isEmpty() }
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val paquete =
                            mockConDefaultAnswer(Paquete::class.java).also {
                                doReturn(4567L).`when`(it).id
                                doReturn("código externo paquete 47456").`when`(it).codigoExterno
                                doReturn("Producto Paquete").`when`(it).nombre
                                doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo 34334345", Decimal(13))))
                                    .`when`(it)
                                    .fondosIncluidos
                            }

                    val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                    val productoUI =
                            Producto(
                                    ProductoPaquete(
                                            paquete,
                                            listOf("Nombre dummy de prueba"),
                                            listOf(precio)
                                                   ),
                                    mockDeProveedorDeImagenesDeProductos
                                    )

                    modelo.agregarAlCarrito(productoUI)

                    observableDePrueba.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableDePrueba.ultimoEmitido().first { it.nombreProducto == productoUI.nombre }
                }

                @Test
                fun los_modelos_hijos_se_actualizan_correctamente()
                {
                    itemCreditoAgregado.borrar()

                    assertEquals(creditosPreIncluidos, modelo.modelosHijos)
                }

                @Test
                fun se_actualiza_correctamente()
                {
                    itemCreditoAgregado.borrar()

                    observableDePrueba.assertValueAt(1) { it.isEmpty() }
                }
            }
        }

        @Nested
        inner class AlRemoverCreditosAgregados
        {
            private val creditosAAgregar = List(5) { CreditoFondoConNombre("Fondo $it", crearCreditoFondo(it, 1)) }

            @BeforeEach
            fun agregarCreditos()
            {
                creditosAAgregar.forEach { credito ->
                    modelo.agregarAlCarrito(deCreditoFondoConNombreAProductoFondo(credito))
                }

                observableDePrueba.verificarUltimoValorEmitido(Predicate { it.size == creditosAAgregar.size })
            }

            @Test
            fun se_actualiza_correctamente()
            {
                modelo.removerCreditosAgregados()

                observableDePrueba.verificarUltimoValorEmitido(Predicate { it.isEmpty() })
            }
        }
    }

    @Nested
    inner class CreditosTotales
    {
        private val observableDePrueba = modelo.creditosTotales.test()!!

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun son_iguales_a_los_creditos_pre_incluidos()
            {
                observableDePrueba.assertValuesOnly(creditosPreIncluidos)
            }
        }

        @Nested
        inner class AlAgregar
        {
            @Nested
            inner class UnCreditoDeFondo
            {
                @Test
                fun el_orden_es_primero_el_credito_agregado_y_luego_los_pre_incluidos()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    val creditoEsperado =
                            ItemCredito(
                                    creditoDeReferencia.nombreDeFondo,
                                    creditoDeReferencia.creditoAsociado.idFondoComprado,
                                    creditoDeReferencia.creditoAsociado.codigoExternoFondo,
                                    creditoDeReferencia.precioCompleto,
                                    null,
                                    false,
                                    1,
                                    false,
                                    true
                                       )

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableDePrueba.verificarUltimoValorEmitido(listOf(creditoEsperado) + creditosPreIncluidos)
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private val paquete =
                        mockConDefaultAnswer(Paquete::class.java).also {
                            doReturn(4567L).`when`(it).id
                            doReturn("código externo paquete 636346").`when`(it).codigoExterno
                            doReturn("Producto Paquete").`when`(it).nombre
                            doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo 45768", Decimal(13))))
                                .`when`(it)
                                .fondosIncluidos
                        }

                private val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                private val productoUI =
                        Producto(
                                ProductoPaquete(
                                        paquete,
                                        listOf("Nombre dummy de prueba"),
                                        listOf(precio)
                                               ),
                                mockDeProveedorDeImagenesDeProductos
                                )

                private val creditoEsperado =
                        ItemCredito(
                                productoUI.nombre,
                                productoUI.idPaquete!!,
                                productoUI.codigoExternoPaquete!!,
                                productoUI.idsFondosAsociados.toList(),
                                productoUI.codigosExternosAsociados,
                                productoUI.preciosDeFondosAsociados,
                                null,
                                false,
                                productoUI.cantidadesFondosEnPaquete!!,
                                1,
                                false,
                                true
                                   )

                @Test
                fun el_orden_es_primero_el_credito_agregado_y_luego_los_pre_incluidos()
                {
                    modelo.agregarAlCarrito(productoUI)

                    observableDePrueba.verificarUltimoValorEmitido(listOf(creditoEsperado) + creditosPreIncluidos)
                }
            }
        }

        @Nested
        inner class AlRemover
        {
            private val observableCreditosAgregados = modelo.creditosAgregados.test()!!

            @Nested
            inner class UnCreditoDeFondo
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == creditoDeReferencia.nombreDeFondo }
                }

                @Test
                fun se_actualiza_correctamente()
                {
                    itemCreditoAgregado.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(creditosPreIncluidos)
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val paquete =
                            mockConDefaultAnswer(Paquete::class.java).also {
                                doReturn(4567L).`when`(it).id
                                doReturn("código externo paquete 4523545").`when`(it).codigoExterno
                                doReturn("Producto Paquete").`when`(it).nombre
                                doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo 63636", Decimal(13))))
                                    .`when`(it)
                                    .fondosIncluidos
                            }

                    val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                    val productoUI =
                            Producto(
                                    ProductoPaquete(
                                            paquete,
                                            listOf("Nombre dummy de prueba"),
                                            listOf(precio)
                                                   ),
                                    mockDeProveedorDeImagenesDeProductos
                                    )

                    modelo.agregarAlCarrito(productoUI)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == productoUI.nombre }
                }

                @Test
                fun se_actualiza_correctamente()
                {
                    itemCreditoAgregado.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(creditosPreIncluidos)
                }
            }
        }

        @Nested
        inner class AlRemoverCreditosAgregados
        {
            private val creditosAAgregar = List(5) { CreditoFondoConNombre("Fondo $it", crearCreditoFondo(it, 1)) }

            @BeforeEach
            fun agregarCreditos()
            {
                creditosAAgregar.forEach { credito ->
                    modelo.agregarAlCarrito(deCreditoFondoConNombreAProductoFondo(credito))
                }

                observableDePrueba
                    .verificarUltimoValorEmitido(Predicate {
                        it.size == creditosAAgregar.size + creditosFondoPreIncluidos.size + creditosPaquetePreIncluidos.size
                    })
            }

            @Test
            fun emite_los_creditos_pre_incluidos_unicamente()
            {
                modelo.removerCreditosAgregados()

                observableDePrueba.verificarUltimoValorEmitido(creditosPreIncluidos)
            }

            @Test
            fun reagregar_creditos_anteriormente_removidos_que_habian_sumado_cantidad_entonces_emite_valores_correctos()
            {
                val creditoDeReferencia = creditosAAgregar.first()

                modelo.agregarAlCarrito(deCreditoFondoConNombreAProductoFondo(creditoDeReferencia))

                modelo.removerCreditosAgregados()

                val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                for (i in 0..5)
                {
                    val creditoEsperado =
                            ItemCredito(
                                    creditoDeReferencia.nombreDeFondo,
                                    creditoDeReferencia.creditoAsociado.idFondoComprado,
                                    creditoDeReferencia.creditoAsociado.codigoExternoFondo,
                                    creditoDeReferencia.precioCompleto,
                                    null,
                                    false,
                                    1 + i,
                                    false,
                                    true
                                       )

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableDePrueba.verificarUltimoValorEmitido(listOf(creditoEsperado) + creditosPreIncluidos)
                }
            }
        }
    }

    @Nested
    inner class IdsTotalesDeFondos
    {
        private val observableDePrueba = modelo.idsTotalesDeFondos.test()!!

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun son_iguales_a_los_ids_de_fondos_pre_incluidos()
            {
                val idsEsperados =
                        creditosFondoPreIncluidos.asSequence().map { it.creditoAsociado.idFondoComprado }.toSet()

                observableDePrueba.assertValuesOnly(idsEsperados)
            }
        }

        @Nested
        inner class AlAgregar
        {
            @Nested
            inner class UnCreditoDeFondo
            {
                @Test
                fun se_incluye_en_los_ids_el_del_nuevo_fondo()
                {
                    val nuevoIdFondo = creditosFondoPreIncluidos.size + 1L
                    val idsEsperados =
                            creditosFondoPreIncluidos
                                .asSequence()
                                .map { it.creditoAsociado.idFondoComprado }
                                .plus(nuevoIdFondo)
                                .toSet()

                    val creditoDeReferencia =
                            creditosFondoPreIncluidos
                                .first().let {

                                    val fondoCondIdNuevo = it.creditoAsociado.copiar(idFondoComprado = nuevoIdFondo)
                                    it.copy(nombreDeFondo = "Fondo nuevo", creditoAsociado = fondoCondIdNuevo)
                                }
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)


                    modelo.agregarAlCarrito(productoAAgregar)

                    assertEquals(idsEsperados.size, creditosFondoPreIncluidos.size + 1)
                    observableDePrueba.verificarUltimoValorEmitido(idsEsperados)
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private val idFondoEnPaquete = 1081010212L
                private val idFondoEnPaquete2 = 8503654001L
                private val paquete =
                        mockConDefaultAnswer(Paquete::class.java).also {
                            doReturn(4567L).`when`(it).id
                            doReturn("código externo paquete 345634534").`when`(it).codigoExterno
                            doReturn("Producto Paquete").`when`(it).nombre
                            doReturn(listOf(
                                    Paquete.FondoIncluido(idFondoEnPaquete, "código externo fondo $idFondoEnPaquete", Decimal(13)),
                                    Paquete.FondoIncluido(idFondoEnPaquete2, "código externo fondo $idFondoEnPaquete2", Decimal(13))
                                           ))
                                .`when`(it)
                                .fondosIncluidos
                        }

                private val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                private val productoUI =
                        Producto(
                                ProductoPaquete(
                                        paquete,
                                        listOf("Nombre dummy de prueba"),
                                        listOf(precio, precio)
                                               ),
                                mockDeProveedorDeImagenesDeProductos
                                )

                @Test
                fun se_incluye_en_los_ids_del_paquete()
                {
                    val idsEsperados =
                            creditosFondoPreIncluidos
                                .asSequence()
                                .map { it.creditoAsociado.idFondoComprado }
                                .plus(idFondoEnPaquete)
                                .plus(idFondoEnPaquete2)
                                .toSet()

                    modelo.agregarAlCarrito(productoUI)

                    assertEquals(idsEsperados.size, creditosFondoPreIncluidos.size + 2)
                    observableDePrueba.verificarUltimoValorEmitido(idsEsperados)
                }
            }
        }

        @Nested
        inner class AlRemover
        {
            private val observableCreditosAgregados = modelo.creditosAgregados.test()!!

            @Nested
            inner class UnCreditoDeFondo
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val nuevoIdFondo = creditosFondoPreIncluidos.size + 1L

                    val creditoDeReferencia =
                            creditosFondoPreIncluidos
                                .first().let {

                                    val fondoCondIdNuevo = it.creditoAsociado.copiar(idFondoComprado = nuevoIdFondo)
                                    it.copy(nombreDeFondo = "Fondo nuevo", creditoAsociado = fondoCondIdNuevo)
                                }
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == creditoDeReferencia.nombreDeFondo }
                }

                @Test
                fun se_quita_el_id_del_fondo_correspondiente()
                {
                    val idsEsperados =
                            creditosFondoPreIncluidos.asSequence().map { it.creditoAsociado.idFondoComprado }.toSet()

                    itemCreditoAgregado.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(idsEsperados)

                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val paquete =
                            mockConDefaultAnswer(Paquete::class.java).also {
                                doReturn(4567L).`when`(it).id
                                doReturn("código externo paquete 24699").`when`(it).codigoExterno
                                doReturn("Producto Paquete").`when`(it).nombre
                                doReturn(listOf(
                                        Paquete.FondoIncluido(1081010212L, "código externo fondo 784572", Decimal(13)),
                                        Paquete.FondoIncluido(8503654001L, "código externo fondo 784572", Decimal(13))
                                               ))
                                    .`when`(it)
                                    .fondosIncluidos
                            }

                    val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                    val productoUI =
                            Producto(
                                    ProductoPaquete(
                                            paquete,
                                            listOf("Nombre dummy de prueba"),
                                            listOf(precio, precio)
                                                   ),
                                    mockDeProveedorDeImagenesDeProductos
                                    )

                    modelo.agregarAlCarrito(productoUI)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == productoUI.nombre }
                }

                @Test
                fun se_quita_el_id_del_fondo_correspondiente()
                {
                    val idsEsperados =
                            creditosFondoPreIncluidos.asSequence().map { it.creditoAsociado.idFondoComprado }.toSet()

                    itemCreditoAgregado.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(idsEsperados)
                }
            }
        }

        @Nested
        inner class AlRemoverCreditosAgregados
        {
            private val creditosAAgregar = List(5) { CreditoFondoConNombre("Fondo $it", crearCreditoFondo(it, 1)) }
            private val creditosPaqueteAAgregar = List(5) { CreditoPaqueteConNombre("Paquete $it", 1, crearCreditoPaquete(it + 10, 1)) }

            @BeforeEach
            fun agregarCreditos()
            {
                creditosAAgregar.forEach { credito ->
                    modelo.agregarAlCarrito(deCreditoFondoConNombreAProductoFondo(credito))
                }
                creditosPaqueteAAgregar.forEach { credito ->
                    modelo.agregarAlCarrito(deCreditoPaqueteConNombreAProductoFondo(credito))
                }

                val idsFondosIniciales =
                        creditosPreIncluidos.asSequence().flatMap { it.idFondos.asSequence() } +
                        creditosAAgregar.asSequence().map { it.creditoAsociado.idFondoComprado } +
                        creditosPaqueteAAgregar.asSequence().flatMap { it.creditoAsociado.creditosFondos.asSequence().map { it.idFondoComprado } }

                observableDePrueba.verificarUltimoValorEmitido(idsFondosIniciales.toSet())
            }

            @Test
            fun se_quitan_todos_los_ids_de_los_fondos_agregados()
            {
                val idsEsperados = creditosPreIncluidos.flatMap { it.idFondos }.asSequence().toSet()

                modelo.removerCreditosAgregados()

                observableDePrueba.verificarUltimoValorEmitido(idsEsperados)
            }
        }
    }

    @Nested
    inner class IdsDiferentesDeFondos
    {
        private val observableDePrueba = modelo.idsDiferentesDeFondos.test()!!

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun son_iguales_a_los_ids_de_fondos_pre_incluidos()
            {
                val idsEsperados =
                        creditosFondoPreIncluidos.asSequence().map { it.creditoAsociado.idFondoComprado }.toSet()

                observableDePrueba.assertValuesOnly(idsEsperados)
            }
        }

        @Nested
        inner class AlAgregar
        {
            @Nested
            inner class UnCreditoDeFondo
            {
                @Test
                fun se_incluye_en_los_ids_aquel_del_nuevo_fondo()
                {
                    val nuevoIdFondo = creditosFondoPreIncluidos.size + 1L
                    val idsEsperados =
                            creditosFondoPreIncluidos
                                .asSequence()
                                .map { it.creditoAsociado.idFondoComprado }
                                .plus(nuevoIdFondo)
                                .toSet()

                    val creditoDeReferencia =
                            creditosFondoPreIncluidos
                                .first().let {
                                    val fondoCondIdNuevo = it.creditoAsociado.copiar(idFondoComprado = nuevoIdFondo)
                                    it.copy(nombreDeFondo = "Fondo nuevo", creditoAsociado = fondoCondIdNuevo)
                                }
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)


                    modelo.agregarAlCarrito(productoAAgregar)

                    assertEquals(idsEsperados.size, creditosFondoPreIncluidos.size + 1)
                    observableDePrueba.verificarUltimoValorEmitido(idsEsperados)
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private val paquete =
                        mockConDefaultAnswer(Paquete::class.java).also {
                            doReturn(4567L).`when`(it).id
                            doReturn("código externo paquete 345634534").`when`(it).codigoExterno
                            doReturn("Producto Paquete").`when`(it).nombre
                            doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo 78256", Decimal(13))))
                                .`when`(it)
                                .fondosIncluidos
                        }

                private val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                private val productoUI =
                        Producto(
                                ProductoPaquete(
                                        paquete,
                                        listOf("Nombre dummy de prueba"),
                                        listOf(precio)
                                               ),
                                mockDeProveedorDeImagenesDeProductos
                                )

                @Test
                fun no_se_emite_ningun_valor()
                {
                    modelo.agregarAlCarrito(productoUI)

                    observableDePrueba.assertValueCount(1)
                }
            }
        }

        @Nested
        inner class AlRemover
        {
            private val observableCreditosAgregados = modelo.creditosAgregados.test()!!

            @Nested
            inner class UnCreditoDeFondo
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val nuevoIdFondo = creditosFondoPreIncluidos.size + 1L

                    val creditoDeReferencia =
                            creditosFondoPreIncluidos
                                .first().let {

                                    val fondoCondIdNuevo = it.creditoAsociado.copiar(idFondoComprado = nuevoIdFondo)
                                    it.copy(nombreDeFondo = "Fondo nuevo", creditoAsociado = fondoCondIdNuevo)
                                }
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == creditoDeReferencia.nombreDeFondo }
                }

                @Test
                fun se_quita_el_id_del_fondo_correspondiente()
                {
                    val idsEsperados =
                            creditosFondoPreIncluidos.asSequence().map { it.creditoAsociado.idFondoComprado }.toSet()

                    itemCreditoAgregado.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(idsEsperados)

                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val paquete =
                            mockConDefaultAnswer(Paquete::class.java).also {
                                doReturn(4567L).`when`(it).id
                                doReturn("código externo paquete 24699").`when`(it).codigoExterno
                                doReturn("Producto Paquete").`when`(it).nombre
                                doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo 784572", Decimal(13))))
                                    .`when`(it)
                                    .fondosIncluidos
                            }

                    val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                    val productoUI =
                            Producto(
                                    ProductoPaquete(
                                            paquete,
                                            listOf("Nombre dummy de prueba"),
                                            listOf(precio)
                                                   ),
                                    mockDeProveedorDeImagenesDeProductos
                                    )

                    modelo.agregarAlCarrito(productoUI)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == productoUI.nombre }
                }

                @Test
                fun no_se_emite_nada()
                {
                    itemCreditoAgregado.borrar()

                    observableDePrueba.assertValueCount(1)
                }
            }
        }

        @Nested
        inner class AlRemoverCreditosAgregados
        {
            private val creditosAAgregar = List(5) { CreditoFondoConNombre("Fondo $it", crearCreditoFondo(it, 1)) }

            @BeforeEach
            fun agregarCreditos()
            {
                creditosAAgregar.forEach { credito ->
                    modelo.agregarAlCarrito(deCreditoFondoConNombreAProductoFondo(credito))
                }

                val idsFondosIniciales =
                        (creditosFondoPreIncluidos.asSequence() + creditosAAgregar.asSequence())
                            .map { it.creditoAsociado.idFondoComprado }
                            .toSet()

                observableDePrueba.verificarUltimoValorEmitido(idsFondosIniciales)
            }

            @Test
            fun se_quitan_todos_los_ids_de_los_fondos_agregados()
            {
                val idsEsperados =
                        creditosFondoPreIncluidos.asSequence().map { it.creditoAsociado.idFondoComprado }.toSet()

                modelo.removerCreditosAgregados()

                observableDePrueba.verificarUltimoValorEmitido(idsEsperados)
            }
        }
    }

    @Nested
    inner class IdsDiferentesDePaquetes
    {
        private val observableDePrueba = modelo.idsDiferentesDePaquetes.test()!!

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun son_iguales_a_los_ids_de_paquetes_pre_incluidos()
            {
                val idsEsperados =
                        creditosPaquetePreIncluidos.asSequence().map { it.creditoAsociado.idPaquete }.toSet()

                observableDePrueba.assertValuesOnly(idsEsperados)
            }
        }

        @Nested
        inner class AlAgregar
        {
            @Nested
            inner class UnCreditoDeFondo
            {
                @Test
                fun no_se_emite_ningun_valor()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableDePrueba.assertValueCount(1)
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private val nuevoIdFondo = creditosPaquetePreIncluidos.size + 1L
                private val paquete =
                        mockConDefaultAnswer(Paquete::class.java).also {
                            doReturn(nuevoIdFondo).`when`(it).id
                            doReturn("código externo paquete 367645646").`when`(it).codigoExterno
                            doReturn("Producto Paquete").`when`(it).nombre
                            doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo 692389", Decimal(13))))
                                .`when`(it)
                                .fondosIncluidos
                        }

                private val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                private val productoUI =
                        Producto(
                                ProductoPaquete(
                                        paquete,
                                        listOf("Nombre dummy de prueba"),
                                        listOf(precio)
                                               ),
                                mockDeProveedorDeImagenesDeProductos
                                )

                @Test
                fun se_incluye_en_los_ids_el_del_nuevo_paquete()
                {
                    val idsEsperados =
                            creditosPaquetePreIncluidos
                                .asSequence()
                                .map { it.creditoAsociado.idPaquete }
                                .plus(nuevoIdFondo)
                                .toSet()

                    modelo.agregarAlCarrito(productoUI)

                    observableDePrueba.verificarUltimoValorEmitido(idsEsperados)
                }
            }
        }

        @Nested
        inner class AlRemover
        {
            private val observableCreditosAgregados = modelo.creditosAgregados.test()!!

            @Nested
            inner class UnCreditoDeFondo
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == creditoDeReferencia.nombreDeFondo }
                }

                @Test
                fun no_se_emite_nada()
                {
                    itemCreditoAgregado.borrar()

                    observableDePrueba.assertValueCount(1)
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val paquete =
                            mockConDefaultAnswer(Paquete::class.java).also {
                                doReturn(creditosPaquetePreIncluidos.size + 1L).`when`(it).id
                                doReturn("código externo paquete").`when`(it).codigoExterno
                                doReturn("Producto Paquete").`when`(it).nombre
                                doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo 6723276", Decimal(13))))
                                    .`when`(it)
                                    .fondosIncluidos
                            }

                    val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                    val productoUI =
                            Producto(
                                    ProductoPaquete(
                                            paquete,
                                            listOf("Nombre dummy de prueba"),
                                            listOf(precio)
                                                   ),
                                    mockDeProveedorDeImagenesDeProductos
                                    )

                    modelo.agregarAlCarrito(productoUI)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == productoUI.nombre }
                }

                @Test
                fun se_actualiza_correctamente()
                {
                    val idsEsperados = creditosPaquetePreIncluidos.asSequence().map { it.creditoAsociado.idPaquete }.toSet()

                    itemCreditoAgregado.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(idsEsperados)
                }
            }
        }

        @Nested
        inner class AlRemoverCreditosAgregados
        {
            private val creditosPaqueteAAgregar = List(5) { CreditoPaqueteConNombre("Paquete $it", 1, crearCreditoPaquete(it + 10, 1)) }

            @BeforeEach
            fun agregarCreditos()
            {
                creditosPaqueteAAgregar.forEach { credito ->
                    modelo.agregarAlCarrito(deCreditoPaqueteConNombreAProductoFondo(credito))
                }

                val idsFondosIniciales =
                        creditosPaquetePreIncluidos.asSequence().map { it.creditoAsociado.idPaquete } +
                        creditosPaqueteAAgregar.asSequence().map { it.creditoAsociado.idPaquete }

                observableDePrueba.verificarUltimoValorEmitido(idsFondosIniciales.toSet())
            }

            @Test
            fun se_quitan_todos_los_ids_de_los_paquetes_agregados()
            {
                val idsEsperados = creditosPaquetePreIncluidos.asSequence().map { it.creditoAsociado.idPaquete }.toSet()

                modelo.removerCreditosAgregados()

                observableDePrueba.verificarUltimoValorEmitido(idsEsperados)
            }
        }
    }

    @Nested
    inner class TotalSinImpuesto
    {
        private val observableDePrueba = modelo.totalSinImpuesto.test()

        @Test
        fun emite_valor_correcto_al_suscribirse()
        {
            observableDePrueba.assertValuesOnly(totalSinImpuestoDePreIncluidos)
        }

        @Test
        fun al_agregar_varios_items_se_emiten_valores_correctos()
        {
            val numeroDeItemsAAgregar = 10
            var totalSinImpuestosDeItemsAgregados = totalSinImpuestoDePreIncluidos

            for (i in 1..numeroDeItemsAAgregar)
            {
                val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $i")
                val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                totalSinImpuestosDeItemsAgregados += creditoDeReferencia.precioCompleto.precioSinImpuesto

                modelo.agregarAlCarrito(productoAAgregar)

                observableDePrueba.verificarUltimoValorEmitido(totalSinImpuestosDeItemsAgregados)
            }
        }

        @Nested
        inner class ConItemsAgregados
        {
            private var totalSinImpuestosDeItemsAgregados = totalSinImpuestoDePreIncluidos
            private lateinit var creditosAgregados: List<Pair<Decimal, ItemCreditoUI>>
            private val NUMERO_DE_ITEMS_A_AGREGAR = 5

            @BeforeEach
            fun agregarItems()
            {
                val eventosDeCreditosAgregados = modelo.creditosAgregados.test()
                val creditosAgregadosEnProceso = mutableListOf<Pair<Decimal, ItemCreditoUI>>()

                for (i in 1..NUMERO_DE_ITEMS_A_AGREGAR)
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $i")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    totalSinImpuestosDeItemsAgregados += creditoDeReferencia.precioCompleto.precioSinImpuesto

                    modelo.agregarAlCarrito(productoAAgregar)

                    creditosAgregadosEnProceso
                        .add(
                                0,
                                Pair(
                                        creditoDeReferencia.precioCompleto.precioSinImpuesto,
                                        eventosDeCreditosAgregados.ultimoEmitido().first()
                                    )
                            )
                }

                creditosAgregados = creditosAgregadosEnProceso
            }

            @Test
            fun al_borrar_varios_items_se_emiten_valores_correctos()
            {
                for (creditoAgregado in creditosAgregados.take(NUMERO_DE_ITEMS_A_AGREGAR))
                {
                    totalSinImpuestosDeItemsAgregados -= creditoAgregado.first

                    creditoAgregado.second.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(totalSinImpuestosDeItemsAgregados)
                }
            }

            @Test
            fun al_incrementar_la_cantidad_de_un_item_se_emiten_valores_correctos()
            {
                val cantidadASumar = 7
                val creditoEnModificacion = creditosAgregados.first()

                val valorEsperado = totalSinImpuestosDeItemsAgregados + creditoEnModificacion.first * cantidadASumar

                (1..cantidadASumar).forEach { creditoEnModificacion.second.sumarUno() }

                observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun al_decrementar_la_cantidad_de_un_item_se_emiten_valores_correctos()
            {
                val cantidadASumar = 7
                val creditoEnModificacion = creditosAgregados.first()

                (1..cantidadASumar).forEach { creditoEnModificacion.second.sumarUno() }

                val cantidadARestar = 3

                val valorEsperado = totalSinImpuestosDeItemsAgregados + creditoEnModificacion.first * (cantidadASumar - cantidadARestar)

                (1..cantidadARestar).forEach { creditoEnModificacion.second.restarUno() }

                observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun al_remover_todos_los_creditos_agregados_se_emite_total_sin_impuestos_solo_de_los_pre_incluidos()
            {
                modelo.removerCreditosAgregados()

                observableDePrueba.verificarUltimoValorEmitido(totalSinImpuestoDePreIncluidos)
            }
        }
    }

    @Nested
    inner class TotalImpuesto
    {
        private val observableDePrueba = modelo.impuestoTotal.test()

        @Test
        fun emite_valor_correcto_al_suscribirse()
        {
            observableDePrueba.assertValuesOnly(totalImpuestoDePreIncluidos)
        }

        @Test
        fun al_agregar_varios_items_se_emiten_valores_correctos()
        {
            val numeroDeItemsAAgregar = 10
            var totalImpuestosDeItemsAgregados = totalImpuestoDePreIncluidos

            for (i in 1..numeroDeItemsAAgregar)
            {
                val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $i")
                val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                totalImpuestosDeItemsAgregados += creditoDeReferencia.precioCompleto.valorImpuesto

                modelo.agregarAlCarrito(productoAAgregar)

                observableDePrueba.verificarUltimoValorEmitido(totalImpuestosDeItemsAgregados)
            }
        }

        @Nested
        inner class ConItemsAgregados
        {
            private var totalImpuestosDeItemsAgregados = totalImpuestoDePreIncluidos
            private lateinit var creditosAgregados: List<Pair<Decimal, ItemCreditoUI>>
            private val NUMERO_DE_ITEMS_A_AGREGAR = 5

            @BeforeEach
            fun agregarItems()
            {
                val eventosDeCreditosAgregados = modelo.creditosAgregados.test()
                val creditosAgregadosEnProceso = mutableListOf<Pair<Decimal, ItemCreditoUI>>()

                for (i in 1..NUMERO_DE_ITEMS_A_AGREGAR)
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $i")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    totalImpuestosDeItemsAgregados += creditoDeReferencia.precioCompleto.valorImpuesto

                    modelo.agregarAlCarrito(productoAAgregar)

                    creditosAgregadosEnProceso
                        .add(
                                0,
                                Pair(
                                        creditoDeReferencia.precioCompleto.valorImpuesto,
                                        eventosDeCreditosAgregados.ultimoEmitido().first()
                                    )
                            )
                }

                creditosAgregados = creditosAgregadosEnProceso
            }

            @Test
            fun al_borrar_varios_items_se_emiten_valores_correctos()
            {
                for (creditoAgregado in creditosAgregados.take(NUMERO_DE_ITEMS_A_AGREGAR))
                {
                    totalImpuestosDeItemsAgregados -= creditoAgregado.first

                    creditoAgregado.second.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(totalImpuestosDeItemsAgregados)
                }
            }

            @Test
            fun al_incrementar_la_cantidad_de_un_item_se_emiten_valores_correctos()
            {
                val cantidadASumar = 7
                val creditoEnModificacion = creditosAgregados.first()

                val valorEsperado = totalImpuestosDeItemsAgregados + creditoEnModificacion.first * cantidadASumar

                (1..cantidadASumar).forEach { creditoEnModificacion.second.sumarUno() }

                observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun al_decrementar_la_cantidad_de_un_item_se_emiten_valores_correctos()
            {
                val cantidadASumar = 7
                val creditoEnModificacion = creditosAgregados.first()

                (1..cantidadASumar).forEach { creditoEnModificacion.second.sumarUno() }

                val cantidadARestar = 3

                val valorEsperado = totalImpuestosDeItemsAgregados + creditoEnModificacion.first * (cantidadASumar - cantidadARestar)

                (1..cantidadARestar).forEach { creditoEnModificacion.second.restarUno() }

                observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun al_remover_todos_los_creditos_agregados_se_emite_total_impuestos_solo_de_los_pre_incluidos()
            {
                modelo.removerCreditosAgregados()

                observableDePrueba.verificarUltimoValorEmitido(totalImpuestoDePreIncluidos)
            }
        }
    }

    @Nested
    inner class GranTotal
    {
        private val observableDePrueba = modelo.granTotal.test()

        @Test
        fun emite_valor_correcto_al_suscribirse()
        {
            observableDePrueba.assertValuesOnly(totalSinImpuestoDePreIncluidos + totalImpuestoDePreIncluidos)
        }

        @Test
        fun al_agregar_varios_items_se_emiten_valores_correctos()
        {
            val numeroDeItemsAAgregar = 10
            var granTotalDeItemsAgregados = totalSinImpuestoDePreIncluidos + totalImpuestoDePreIncluidos

            for (i in 1..numeroDeItemsAAgregar)
            {
                val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $i")
                val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                granTotalDeItemsAgregados += creditoDeReferencia.precioCompleto.precioConImpuesto

                modelo.agregarAlCarrito(productoAAgregar)

                observableDePrueba.verificarUltimoValorEmitido(granTotalDeItemsAgregados)
            }
        }

        @Nested
        inner class ConItemsAgregados
        {
            private var granTotalDeItemsAgregados = totalSinImpuestoDePreIncluidos + totalImpuestoDePreIncluidos
            private lateinit var creditosAgregados: List<Pair<Decimal, ItemCreditoUI>>
            private val NUMERO_DE_ITEMS_A_AGREGAR = 5

            @BeforeEach
            fun agregarItems()
            {
                val eventosDeCreditosAgregados = modelo.creditosAgregados.test()
                val creditosAgregadosEnProceso = mutableListOf<Pair<Decimal, ItemCreditoUI>>()

                for (i in 1..NUMERO_DE_ITEMS_A_AGREGAR)
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $i")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    granTotalDeItemsAgregados += creditoDeReferencia.precioCompleto.precioConImpuesto

                    modelo.agregarAlCarrito(productoAAgregar)

                    creditosAgregadosEnProceso
                        .add(
                                0,
                                Pair(
                                        creditoDeReferencia.precioCompleto.precioConImpuesto,
                                        eventosDeCreditosAgregados.ultimoEmitido().first()
                                    )
                            )
                }

                creditosAgregados = creditosAgregadosEnProceso
            }

            @Test
            fun al_borrar_varios_items_se_emiten_valores_correctos()
            {
                for (creditoAgregado in creditosAgregados.take(NUMERO_DE_ITEMS_A_AGREGAR))
                {
                    granTotalDeItemsAgregados -= creditoAgregado.first

                    creditoAgregado.second.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(granTotalDeItemsAgregados)
                }
            }

            @Test
            fun al_incrementar_la_cantidad_de_un_item_se_emiten_valores_correctos()
            {
                val cantidadASumar = 7
                val creditoEnModificacion = creditosAgregados.first()

                val valorEsperado = granTotalDeItemsAgregados + creditoEnModificacion.first * cantidadASumar

                (1..cantidadASumar).forEach { creditoEnModificacion.second.sumarUno() }

                observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun al_decrementar_la_cantidad_de_un_item_se_emiten_valores_correctos()
            {
                val cantidadASumar = 7
                val creditoEnModificacion = creditosAgregados.first()

                (1..cantidadASumar).forEach { creditoEnModificacion.second.sumarUno() }

                val cantidadARestar = 3

                val valorEsperado = granTotalDeItemsAgregados + creditoEnModificacion.first * (cantidadASumar - cantidadARestar)

                (1..cantidadARestar).forEach { creditoEnModificacion.second.restarUno() }

                observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun al_remover_todos_los_creditos_agregados_se_emite_el_gran_total_solo_de_los_pre_incluidos()
            {
                modelo.removerCreditosAgregados()

                observableDePrueba.verificarUltimoValorEmitido(totalSinImpuestoDePreIncluidos + totalImpuestoDePreIncluidos)
            }
        }
    }

    @Nested
    inner class ValorYaPagado
    {
        @Test
        fun el_valor_corresponde_a_la_suma_de_los_precios_con_impuestos_de_los_creditos_pre_incluidos_ya_pagados()
        {
            val valorEsperado =
                    creditosFondoPreIncluidos.asSequence()
                        .filter { it.estaPagado }
                        .map {
                            it.precioCompleto.precioConImpuesto
                        }
                        .plus(
                                creditosPaquetePreIncluidos.asSequence()
                                    .filter { it.estaPagado }
                                    .map { it.precioConImpuestos }
                             )
                        .let { if (it.none()) Decimal.CERO else it.reduce { acc, observable -> acc + observable } }

            assertEquals(valorEsperado, modelo.valorYaPagado)
        }
    }

    @Nested
    inner class Saldo
    {
        private val observableDePrueba = modelo.saldo.test()

        @Test
        fun emite_valor_correcto_al_suscribirse()
        {
            observableDePrueba.assertValuesOnly(
                    totalSinImpuestoDePreIncluidos +
                    totalImpuestoDePreIncluidos -
                    DINERO_CREDITO -
                    modelo.valorYaPagado
                                               )
        }

        @Test
        fun al_agregar_varios_items_se_emiten_valores_correctos()
        {
            val numeroDeItemsAAgregar = 10
            var saldoDeItemsAgregados =
                    totalSinImpuestoDePreIncluidos +
                    totalImpuestoDePreIncluidos -
                    DINERO_CREDITO -
                    modelo.valorYaPagado

            for (i in 1..numeroDeItemsAAgregar)
            {
                val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $i")
                val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                saldoDeItemsAgregados += creditoDeReferencia.precioCompleto.precioConImpuesto

                modelo.agregarAlCarrito(productoAAgregar)

                observableDePrueba.verificarUltimoValorEmitido(saldoDeItemsAgregados)
            }
        }

        @Nested
        inner class ConItemsAgregados
        {
            private var saldoDeItemsAgregados =
                    (totalSinImpuestoDePreIncluidos + totalImpuestoDePreIncluidos) -
                    (DINERO_CREDITO + modelo.valorYaPagado)

            private lateinit var creditosAgregados: List<Pair<Decimal, ItemCreditoUI>>
            private val NUMERO_DE_ITEMS_A_AGREGAR = 5

            @BeforeEach
            fun agregarItems()
            {
                val eventosDeCreditosAgregados = modelo.creditosAgregados.test()
                val creditosAgregadosEnProceso = mutableListOf<Pair<Decimal, ItemCreditoUI>>()

                for (i in 1..NUMERO_DE_ITEMS_A_AGREGAR)
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $i")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    saldoDeItemsAgregados += creditoDeReferencia.precioCompleto.precioConImpuesto

                    modelo.agregarAlCarrito(productoAAgregar)

                    creditosAgregadosEnProceso
                        .add(
                                0,
                                Pair(
                                        creditoDeReferencia.precioCompleto.precioConImpuesto,
                                        eventosDeCreditosAgregados.ultimoEmitido().first()
                                    )
                            )
                }

                creditosAgregados = creditosAgregadosEnProceso
            }

            @Test
            fun al_borrar_varios_items_se_emiten_valores_correctos()
            {
                for (creditoAgregado in creditosAgregados.take(NUMERO_DE_ITEMS_A_AGREGAR))
                {
                    saldoDeItemsAgregados -= creditoAgregado.first

                    creditoAgregado.second.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(saldoDeItemsAgregados)
                }
            }

            @Test
            fun al_incrementar_la_cantidad_de_un_item_se_emiten_valores_correctos()
            {
                val cantidadASumar = 7
                val creditoEnModificacion = creditosAgregados.first()

                val valorEsperado = saldoDeItemsAgregados + creditoEnModificacion.first * cantidadASumar

                (1..cantidadASumar).forEach { creditoEnModificacion.second.sumarUno() }

                observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun al_decrementar_la_cantidad_de_un_item_se_emiten_valores_correctos()
            {
                val cantidadASumar = 7
                val creditoEnModificacion = creditosAgregados.first()

                (1..cantidadASumar).forEach { creditoEnModificacion.second.sumarUno() }

                val cantidadARestar = 3

                val valorEsperado = saldoDeItemsAgregados + creditoEnModificacion.first * (cantidadASumar - cantidadARestar)

                (1..cantidadARestar).forEach { creditoEnModificacion.second.restarUno() }

                observableDePrueba.verificarUltimoValorEmitido(valorEsperado)
            }

            @Test
            fun al_remover_todos_los_creditos_agregados_se_emite_lo_que_falta_por_pagar()
            {
                modelo.removerCreditosAgregados()

                observableDePrueba
                    .verificarUltimoValorEmitido(
                            (totalSinImpuestoDePreIncluidos + totalImpuestoDePreIncluidos) -
                            (DINERO_CREDITO + modelo.valorYaPagado)
                                                )
            }
        }
    }

    @Nested
    inner class HayCreditosSinConfirmar
    {
        private val observableDePrueba = modelo.hayCreditosSinConfirmar.test()!!
        private val observableCreditosAgregados = modelo.creditosAgregados.test()!!

        @Nested
        inner class AlSuscribirse
        {
            @Test
            fun sin_creditos_agregados_emite_false()
            {
                observableDePrueba.assertValuesOnly(false)
            }
        }

        @Nested
        inner class AlAgregar
        {
            @Nested
            inner class UnCreditoDeFondo
            {
                @Test
                fun emite_true()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableDePrueba.assertValuesOnly(false, true)
                }

                @Test
                fun al_agregar_dos_o_mas_veces_se_emite_una_sola_vez_true()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first()
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    (1..3).forEach {
                        modelo.agregarAlCarrito(productoAAgregar)
                    }

                    observableDePrueba.assertValuesOnly(false, true)
                }

                @Test
                fun al_agregar_un_producto_ya_agregado_como_pre_incluido_emite_true()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first()
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableDePrueba.assertValuesOnly(false, true)
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private val paquete =
                        mockConDefaultAnswer(Paquete::class.java).also {
                            doReturn(4567L).`when`(it).id
                            doReturn("código externo paquete").`when`(it).codigoExterno
                            doReturn("Producto Paquete").`when`(it).nombre
                            doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo 42747", Decimal(13))))
                                .`when`(it)
                                .fondosIncluidos
                        }

                private val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                private val productoUI =
                        Producto(
                                ProductoPaquete(
                                        paquete,
                                        listOf("Nombre dummy de prueba"),
                                        listOf(precio)
                                               ),
                                mockDeProveedorDeImagenesDeProductos
                                )

                @Test
                fun emite_true()
                {
                    modelo.agregarAlCarrito(productoUI)

                    observableDePrueba.assertValuesOnly(false, true)
                }

                @Test
                fun al_agregar_dos_o_mas_veces_se_emite_una_sola_vez_true()
                {
                    (1..3).forEach {
                        modelo.agregarAlCarrito(productoUI)
                    }

                    observableDePrueba.assertValuesOnly(false, true)
                }

                @Test
                fun al_agregar_un_producto_ya_agregado_como_pre_incluido_emite_true()
                {
                    modelo.agregarAlCarrito(productoUI)

                    observableDePrueba.assertValuesOnly(false, true)
                }
            }
        }

        @Nested
        inner class AlConfirmarCreditosAgregados
        {
            private lateinit var observablesFaltaConfirmacionItemsCreditoAgregados: MutableList<TestObserver<Boolean>>

            @BeforeEach
            fun agregarItemsYConfirmar()
            {
                observablesFaltaConfirmacionItemsCreditoAgregados =
                        MutableList(2) {
                            val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $it")
                            val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                            modelo.agregarAlCarrito(productoAAgregar)

                            val ultimoEmitido = observableCreditosAgregados.ultimoEmitido()
                            assertEquals(it + 1, ultimoEmitido.size)

                            ultimoEmitido.first { it.nombreProducto == creditoDeReferencia.nombreDeFondo }.faltaConfirmacionAdicion.test()
                        }

                modelo.confirmarCreditosAgregados()
            }

            @Test
            fun emite_false_y_deja_items_marcados_como_confirmados()
            {
                // observableDePrueba.assertValuesOnly(Inicializacion, al agregar, al confirmar)
                observableDePrueba.assertValuesOnly(false, true, false)
                observablesFaltaConfirmacionItemsCreditoAgregados.forEach { it.assertValuesOnly(true, false) }
            }

            @Test
            fun despues_de_volver_a_agregar_items_emite_true_y_luego_false()
            {
                observablesFaltaConfirmacionItemsCreditoAgregados.addAll(
                        MutableList(2) {
                            val contador = it + observablesFaltaConfirmacionItemsCreditoAgregados.size
                            val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $contador")
                            val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                            modelo.agregarAlCarrito(productoAAgregar)

                            val ultimoEmitido = observableCreditosAgregados.ultimoEmitido()
                            assertEquals(contador + 1, ultimoEmitido.size)

                            ultimoEmitido.first { it.nombreProducto == creditoDeReferencia.nombreDeFondo }.faltaConfirmacionAdicion.test()
                        }
                                                                        )

                modelo.confirmarCreditosAgregados()

                //(Inicializacion, al agregar en BeforeEach, al confirmar en BeforeEach, al agregar, al confirmar)
                observableDePrueba.assertValuesOnly(false, true, false, true, false)
                observablesFaltaConfirmacionItemsCreditoAgregados.forEach { it.assertValuesOnly(true, false) }
            }
        }

        @Nested
        inner class AlRemover
        {
            @Nested
            inner class UnCreditoDeFondo
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
                    val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                    modelo.agregarAlCarrito(productoAAgregar)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == creditoDeReferencia.nombreDeFondo }
                }

                @Test
                fun sin_confirmar_emite_false()
                {
                    itemCreditoAgregado.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(false)
                }

                @Test
                fun confirmado_no_emite_nada()
                {
                    modelo.confirmarCreditosAgregados()
                    itemCreditoAgregado.borrar()

                    // observableDePrueba.assertValuesOnly(Inicializacion, al agregar, al confirmar)
                    observableDePrueba.assertValuesOnly(false, true, false)
                }
            }

            @Nested
            inner class UnCreditoDePaquete
            {
                private lateinit var itemCreditoAgregado: ItemCreditoUI

                @BeforeEach
                fun agregarItem()
                {
                    val paquete =
                            mockConDefaultAnswer(Paquete::class.java).also {
                                doReturn(4567L).`when`(it).id
                                doReturn("código externo paquete").`when`(it).codigoExterno
                                doReturn("Producto Paquete").`when`(it).nombre
                                doReturn(listOf(Paquete.FondoIncluido(12336L, "código externo fondo 4523234", Decimal(13))))
                                    .`when`(it)
                                    .fondosIncluidos
                            }

                    val precio = PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal(19)))

                    val productoUI =
                            Producto(
                                    ProductoPaquete(
                                            paquete,
                                            listOf("Nombre dummy de prueba"),
                                            listOf(precio)
                                                   ),
                                    mockDeProveedorDeImagenesDeProductos
                                    )

                    modelo.agregarAlCarrito(productoUI)

                    observableCreditosAgregados.assertValue { it.size == 1 }

                    itemCreditoAgregado = observableCreditosAgregados.ultimoEmitido().first { it.nombreProducto == productoUI.nombre }
                }

                @Test
                fun sin_confirmar_emite_false()
                {
                    itemCreditoAgregado.borrar()

                    observableDePrueba.verificarUltimoValorEmitido(false)
                }

                @Test
                fun confirmado_no_emite_nada()
                {
                    modelo.confirmarCreditosAgregados()
                    itemCreditoAgregado.borrar()

                    // observableDePrueba.assertValuesOnly(Inicializacion, al agregar, al confirmar)
                    observableDePrueba.assertValuesOnly(false, true, false)
                }
            }
        }

        @Nested
        inner class AlCancelarCreditosAgregados
        {
            private lateinit var observablesFaltaConfirmacionItemsCreditoAgregados: MutableList<TestObserver<Boolean>>

            @BeforeEach
            fun agregarItems()
            {
                observablesFaltaConfirmacionItemsCreditoAgregados =
                        MutableList(2) {
                            val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo $it")
                            val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

                            modelo.agregarAlCarrito(productoAAgregar)

                            val ultimoEmitido = observableCreditosAgregados.ultimoEmitido()
                            assertEquals(it + 1, ultimoEmitido.size)

                            ultimoEmitido.first { it.nombreProducto == creditoDeReferencia.nombreDeFondo }.faltaConfirmacionAdicion.test()
                        }
            }

            @Test
            fun sin_confirmar_emite_false()
            {
                modelo.cancelarCreditosAgregados()

                // observableDePrueba.assertValuesOnly(Inicializacion, al agregar, al cancelar)
                observableDePrueba.assertValuesOnly(false, true, false)
            }

            @Test
            fun confirmados_emite_false()
            {
                modelo.confirmarCreditosAgregados()
                modelo.cancelarCreditosAgregados()

                // observableDePrueba.assertValuesOnly(Inicializacion, al agregar, al cancelar)
                observableDePrueba.assertValuesOnly(false, true, false)
            }
        }

        @Nested
        inner class AlRemoverCreditosAgregados
        {
            private val creditosAAgregar = List(5) { CreditoFondoConNombre("Fondo $it", crearCreditoFondo(it, 1)) }
            private val creditosPaqueteAAgregar = List(5) { CreditoPaqueteConNombre("Paquete $it", 1, crearCreditoPaquete(it + 10, 1)) }

            @BeforeEach
            fun agregarCreditos()
            {
                creditosAAgregar.forEach { credito ->
                    modelo.agregarAlCarrito(deCreditoFondoConNombreAProductoFondo(credito))
                }
                creditosPaqueteAAgregar.forEach { credito ->
                    modelo.agregarAlCarrito(deCreditoPaqueteConNombreAProductoFondo(credito))
                }

                observableDePrueba.verificarUltimoValorEmitido(true)
            }

            @Test
            fun emite_false()
            {
                modelo.removerCreditosAgregados()

                observableDePrueba.verificarUltimoValorEmitido(false)
            }
        }
    }

    @Nested
    inner class CreditosAPagar
    {
        private val observableDePrueba = modelo.creditosAProcesar.test()

        @Test
        fun al_suscribirse_no_se_emite_ningun_evento()
        {
            observableDePrueba.assertEmpty()
        }

        @Test
        fun no_permite_si_hay_creditos_sin_confirmar_adicion()
        {
            val creditoDeReferencia =
                    creditosFondoPreIncluidos.first().let {
                        it.copy(
                                nombreDeFondo = "No pagado",
                                creditoAsociado = it.creditoAsociado.copiar(id = null)
                               )
                    }
            val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

            modelo.agregarAlCarrito(productoAAgregar)

            modelo.pagar()

            observableDePrueba.assertEmpty()
        }

        @Test
        fun no_permite_sin_creditos_agregados_y_sin_creditos_preincluidos_pagados()
        {
            val modeloSinCreditosPreincluidos =
                    CarritoDeCreditos(
                            DINERO_CREDITO,
                            listOf(),
                            listOf(),
                            mockCalculadorPuedeAgregarseSegunUnicidad
                                     )

            val observador = modeloSinCreditosPreincluidos.creditosAProcesar.test()

            modeloSinCreditosPreincluidos.pagar()

            observador.assertEmpty()
        }

        @Test
        fun con_creditos_preincluidos_pagados_y_sin_creditos_agregados_emite_entidad_correcta()
        {
            val entidadEsperada =
                    CarritoDeCreditosUI.CreditosAProcesar(
                            listOf(),
                            creditosPreIncluidos.filter { it.estaPagado }
                                                         )

            modelo.pagar()

            observableDePrueba.assertResult(entidadEsperada)
        }

        @Test
        fun al_pagar_se_emite_la_lista_de_creditos_pre_incluidos_sin_pagar_los_agregados_confirmados_y_los_preincluidos_pagados()
        {
            val creditoDeReferencia1 =
                    creditosFondoPreIncluidos.first().let {
                        it.copy(
                                nombreDeFondo = "No pagado 1",
                                creditoAsociado = it.creditoAsociado.copiar(id = null)
                               )
                    }
            val productoAAgregar1 = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia1)

            val creditoDeReferencia2 =
                    creditosFondoPreIncluidos.last().let {
                        it.copy(
                                nombreDeFondo = "No pagado 2",
                                creditoAsociado = it.creditoAsociado.copiar(id = null)
                               )
                    }
            val productoAAgregar2 = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia2)

            modelo.agregarAlCarrito(productoAAgregar1)
            modelo.confirmarCreditosAgregados()
            modelo.agregarAlCarrito(productoAAgregar2)
            modelo.confirmarCreditosAgregados()

            val creditosSinPagarEsperados =
                    listOf(
                            ItemCredito(
                                    creditoDeReferencia2.nombreDeFondo,
                                    creditoDeReferencia2.creditoAsociado.idFondoComprado,
                                    creditoDeReferencia2.creditoAsociado.codigoExternoFondo,
                                    creditoDeReferencia2.precioCompleto,
                                    null,
                                    false,
                                    1,
                                    false,
                                    false
                                       ),
                            ItemCredito(
                                    creditoDeReferencia1.nombreDeFondo,
                                    creditoDeReferencia1.creditoAsociado.idFondoComprado,
                                    creditoDeReferencia1.creditoAsociado.codigoExternoFondo,
                                    creditoDeReferencia1.precioCompleto,
                                    null,
                                    false,
                                    1,
                                    false,
                                    false
                                       )
                          )
                        .asSequence()
                        .plus(creditosPreIncluidos.asSequence().filter { !it.estaPagado })
                        .toList()

            val creditosPagadosEsperados = creditosPreIncluidos.filter { it.estaPagado }

            val resultadoEsperado = CarritoDeCreditosUI.CreditosAProcesar(creditosSinPagarEsperados, creditosPagadosEsperados)


            modelo.pagar()


            observableDePrueba.assertResult(resultadoEsperado)
        }
    }

    @Test
    fun dos_carritos_son_iguales_si_todos_los_valores_no_observables_son_iguales()
    {
        val creditoDeReferencia = creditosFondoPreIncluidos.first().copy(nombreDeFondo = "Fondo nuevo")
        val productoAAgregar = deCreditoFondoConNombreAProductoFondo(creditoDeReferencia)

        val primerCarrito =
                CarritoDeCreditos(
                        DINERO_CREDITO,
                        creditosFondoPreIncluidos,
                        creditosPaquetePreIncluidos,
                        mockCalculadorPuedeAgregarseSegunUnicidad
                                 )

        val segundoCarrito =
                CarritoDeCreditos(
                        DINERO_CREDITO,
                        creditosFondoPreIncluidos,
                        creditosPaquetePreIncluidos,
                        mockCalculadorPuedeAgregarseSegunUnicidad
                                 ).apply { agregarAlCarrito(productoAAgregar) }

        assertEquals(primerCarrito, segundoCarrito)
    }

    private fun deCreditoFondoConNombreAProductoFondo(creditoFondoConNombre: CreditoFondoConNombre)
            : ProductoUI
    {
        return Producto(
                ProductoFondo.SinCategoria(
                        mockConDefaultAnswer(Dinero::class.java).also {
                            doReturn(creditoFondoConNombre.creditoAsociado.idFondoComprado).`when`(it).id
                            doReturn(creditoFondoConNombre.creditoAsociado.codigoExternoFondo).`when`(it).codigoExterno
                            doReturn(creditoFondoConNombre.nombreDeFondo).`when`(it).nombre
                        },
                        creditoFondoConNombre.precioCompleto
                                          ),
                mockDeProveedorDeImagenesDeProductos
                       )
    }

    private fun deCreditoPaqueteConNombreAProductoFondo(creditoPaqueteConNombre: CreditoPaqueteConNombre)
            : ProductoUI
    {
        val paquete =
                mockConDefaultAnswer(Paquete::class.java).also {
                    doReturn(creditoPaqueteConNombre.creditoAsociado.idPaquete).`when`(it).id
                    doReturn("código externo paquete 24699").`when`(it).codigoExterno
                    doReturn(creditoPaqueteConNombre.nombreDelPaquete).`when`(it).nombre

                    val fondosIncluidos = creditoPaqueteConNombre.creditoAsociado.creditosFondos.map {
                        Paquete.FondoIncluido(it.idFondoComprado, it.codigoExternoFondo, it.cantidad)
                    }
                    doReturn(fondosIncluidos)
                        .`when`(it)
                        .fondosIncluidos
                }

        return Producto(
                ProductoPaquete(
                        paquete,
                        creditoPaqueteConNombre.creditoAsociado.creditosFondos.mapIndexed { i, _ -> "Fondo incluido $i" },
                        creditoPaqueteConNombre.preciosCompletos
                               ),
                mockDeProveedorDeImagenesDeProductos
                       )
    }
}