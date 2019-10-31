package co.smartobjects.ui.modelos.consumos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.logica.fondos.CalculadoraDeConsumos
import co.smartobjects.logica.fondos.ProveedorCodigosExternosFondos
import co.smartobjects.logica.fondos.ProveedorNombresYPreciosPorDefectoCompletosFondos
import co.smartobjects.logica.fondos.precios.CalculadorGrupoCliente
import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.nfc.operacionessobretags.ResultadoLecturaNFC
import co.smartobjects.red.clientes.operativas.ordenes.LoteDeOrdenesAPI
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.PersonaPorIdSesionManillaAPI
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.SesionDeManillaAPI
import co.smartobjects.ui.modelos.*
import co.smartobjects.ui.modelos.carritocreditos.ItemCredito
import co.smartobjects.ui.modelos.catalogo.Producto
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoFondo
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoPaquete
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.Opcional
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn

internal class CodificacionDeConsumosPruebas : PruebasModelosRxBase()
{
    private val contextoDeSesion = ContextoDeSesionImpl(1, "Taquilla A", "Pepito Pérez", "123-456-789", 1)

    private val mockProveedorNombresYPreciosPorDefectoCompletosFondos = mockConDefaultAnswer(ProveedorNombresYPreciosPorDefectoCompletosFondos::class.java)
    private val mockProveedorOperacionesNFC = mockConDefaultAnswer(ProveedorOperacionesNFC::class.java).also {
        doReturn(PublishProcessor.create<ResultadoNFC>()).`when`(it).resultadosNFCLeidos
        doReturn(BehaviorProcessor.createDefault(false)).`when`(it).listoParaLectura
        doNothing().`when`(it).permitirLecturaNFC = ArgumentMatchers.anyBoolean()
    }
    private val mockProveedorCodigosExternosFondos = mockConDefaultAnswer(ProveedorCodigosExternosFondos::class.java)
    private val mockCalculadorGrupoCliente = mockConDefaultAnswer(CalculadorGrupoCliente::class.java)
    private val mockCalculadoraDeConsumos = mockConDefaultAnswer(CalculadoraDeConsumos::class.java)
    private val mockApiLoteDeOrdenes = mockConDefaultAnswer(LoteDeOrdenesAPI::class.java)
    private val mockApiPersonaPorIdSesionManilla = mockConDefaultAnswer(PersonaPorIdSesionManillaAPI::class.java)
    private val mockApiSesionDeManilla = mockConDefaultAnswer(SesionDeManillaAPI::class.java)
    private val mockDeProveedorDeImagenesDeProductos =
            mockConDefaultAnswer(ProveedorImagenesProductos::class.java)
                .also {
                    doReturn(Maybe.empty<ProveedorImagenesProductos.ImagenProducto>())
                        .`when`(it)
                        .darImagen(ArgumentMatchers.anyLong(), ArgumentMatchers.anyBoolean())
                }

    private val mockOperacionesCompuestas = OperacionesCompuestasMockeadas.crear()

    @Nested
    inner class AlInstanciar
    {
        private val modelo =
                CodificacionDeConsumos(
                        contextoDeSesion,
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        mockProveedorOperacionesNFC,
                        Single.just(mockProveedorCodigosExternosFondos),
                        Single.just(mockCalculadorGrupoCliente),
                        Single.just(mockCalculadoraDeConsumos),
                        CodificacionDeConsumos.Apis(
                                mockApiLoteDeOrdenes,
                                mockApiPersonaPorIdSesionManilla,
                                mockApiSesionDeManilla
                                                   )
                                      )

        @Test
        fun el_estado_representa_que_el_carrito_esta_vacio()
        {
            modelo.estado.test().assertValue(CodificacionDeConsumosUI.Estado.CON_CARRITO_VACIO)
        }

        @Test
        fun el_mensaje_de_error_no_emite_nada()
        {
            modelo.mensajesDeError.test().assertEmpty()
        }

        @Test
        fun los_creditos_totales_estan_vacios()
        {
            modelo.creditosTotales.test().assertValue(listOf())
        }

        @Test
        fun el_total_sin_impuestos_es_cero()
        {
            modelo.totalSinImpuesto.test().assertValue(Decimal.CERO)
        }

        @Test
        fun el_impuesto_total_es_cero()
        {
            modelo.impuestoTotal.test().assertValue(Decimal.CERO)
        }

        @Test
        fun el_gran_total_es_cero()
        {
            modelo.granTotal.test().assertValue(Decimal.CERO)
        }

        @Test
        fun el_ultimo_resultado_de_consumos_es_vacio()
        {
            modelo.ultimosResultadoDeConsumos.test().assertValue(Opcional.Vacio())
        }
    }

    @Nested
    inner class AlAgregarUnProducto
    {
        private val modelo =
                CodificacionDeConsumos(
                        contextoDeSesion,
                        Single.just(mockProveedorNombresYPreciosPorDefectoCompletosFondos),
                        mockProveedorOperacionesNFC,
                        Single.just(mockProveedorCodigosExternosFondos),
                        Single.just(mockCalculadorGrupoCliente),
                        Single.just(mockCalculadoraDeConsumos),
                        CodificacionDeConsumos.Apis(
                                mockApiLoteDeOrdenes,
                                mockApiPersonaPorIdSesionManilla,
                                mockApiSesionDeManilla
                                                   ),
                        Schedulers.trampoline()
                                      )

        private val producto =
                Producto(
                        ProductoFondo.SinCategoria(
                                Dinero(1, 1, "dinero", true, false, false, Precio(Decimal.UNO, 1), "ex-1"),
                                PrecioCompleto(Precio(Decimal.UNO, 1), ImpuestoSoloTasa(1, 1, Decimal.CERO))
                                                  ),
                        mockDeProveedorDeImagenesDeProductos
                        )

        @Test
        fun el_estado_es_esperando_tag()
        {
            val observador = modelo.estado.test()

            modelo.agregarProducto(producto)

            observador.verificarUltimoValorEmitido(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
        }

        @Test
        fun los_creditos_totales_contienen_el_item_agregado()
        {
            val observador = modelo.creditosTotales.test()

            val creditoEsperado =
                    ItemCredito(
                            producto.nombre,
                            producto.idsFondosAsociados.first(),
                            producto.codigosExternosAsociados.first(),
                            producto.preciosDeFondosAsociados.first(),
                            null,
                            false,
                            1,
                            false,
                            true
                               )

            modelo.agregarProducto(producto)

            observador.verificarUltimoValorEmitido(listOf(creditoEsperado))
        }

        @Nested
        inner class MultiplesVeces
        {
            @Test
            fun el_estado_es_siempre_esperando_tag()
            {
                val observador = modelo.estado.test()

                for (i in 0..5)
                {
                    modelo.agregarProducto(producto)
                }

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
            }

            @Test
            fun los_creditos_totales_contienen_el_item_agregado_con_cantidad_incrementada()
            {
                val observador = modelo.creditosTotales.test()

                for (i in 0..5)
                {
                    val creditoEsperado =
                            ItemCredito(
                                    producto.nombre,
                                    producto.idsFondosAsociados.first(),
                                    producto.codigosExternosAsociados.first(),
                                    producto.preciosDeFondosAsociados.first(),
                                    null,
                                    false,
                                    1 + i,
                                    false,
                                    true
                                       )

                    modelo.agregarProducto(producto)

                    observador.verificarUltimoValorEmitido(listOf(creditoEsperado))
                }
            }

            @Test
            fun al_agregar_otr_producto_el_estado_es_siempre_esperando_tag()
            {
                val productoAlternativo =
                        Producto(
                                ProductoFondo.SinCategoria(
                                        Dinero(1, 1, "algo nuevo", true, false, false, Precio(Decimal.DIEZ, 1), "ex-1"),
                                        PrecioCompleto(Precio(Decimal.DIEZ, 1), ImpuestoSoloTasa(1, 1, Decimal.CERO))
                                                          ),
                                mockDeProveedorDeImagenesDeProductos
                                )

                val observador = modelo.estado.test()

                modelo.agregarProducto(producto)
                modelo.agregarProducto(productoAlternativo)

                observador.assertValueCount(2)
                observador.verificarUltimoValorEmitido(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
            }
        }

        @Nested
        inner class LuegoRemoverlo
        {
            private val observadorCreditosTotales = modelo.creditosTotales.test()

            @Test
            fun el_estado_pasa_a_con_carrito_vacio()
            {
                val observador = modelo.estado.test()

                modelo.agregarProducto(producto)

                darUltimoCreditoAgregado().borrar()

                observador.verificarUltimoValorEmitido(CodificacionDeConsumosUI.Estado.CON_CARRITO_VACIO)
            }

            @Test
            fun los_creditos_totales_estan_vacios()
            {
                modelo.agregarProducto(producto)

                darUltimoCreditoAgregado().borrar()

                observadorCreditosTotales.verificarUltimoValorEmitido(listOf())
            }

            private fun darUltimoCreditoAgregado() = observadorCreditosTotales.ultimoEmitido().first()
        }

        @Test
        fun no_permite_agregar_productos_tipo_paquete()
        {
            val observador = modelo.creditosTotales.test()

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

            val productoPaquete =
                    Producto(
                            ProductoPaquete(paquete, listOf("Nombre dummy de prueba"), listOf(precio)),
                            mockDeProveedorDeImagenesDeProductos
                            )

            modelo.agregarProducto(productoPaquete)

            observador.assertValue(listOf())
        }

        @Test
        fun no_hace_nada_si_estaba_codificando()
        {
            val observador = modelo.creditosTotales.test()

            modelo.agregarProducto(producto)

            val creditosEsperados = observador.ultimoEmitido()

            val resultadoNFC = mockProveedorOperacionesNFC.resultadosNFCLeidos as PublishProcessor<ResultadoNFC>

            val productoNuevo =
                    Producto(
                            ProductoFondo.SinCategoria(
                                    Dinero(1, 1, "Nuevo producto", true, false, false, Precio(Decimal.UNO, 1), "ex-1"),
                                    PrecioCompleto(Precio(Decimal.DIEZ, 1), ImpuestoSoloTasa(1, 1, Decimal.DIEZ))
                                                      ),
                            mockDeProveedorDeImagenesDeProductos
                            )

            // Retener lectura del tag para asegurarse que esté en estado codificando
            doAnswer {
                modelo.agregarProducto(productoNuevo)
                observador.verificarUltimoValorEmitido(creditosEsperados)

                ResultadoLecturaNFC.TagVacio
            }.`when`(mockOperacionesCompuestas).leerTag()

            resultadoNFC.onNext(ResultadoNFC.Exitoso(mockOperacionesCompuestas))
        }
    }
}