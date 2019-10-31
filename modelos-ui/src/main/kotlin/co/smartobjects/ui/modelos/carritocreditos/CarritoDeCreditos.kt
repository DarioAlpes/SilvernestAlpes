package co.smartobjects.ui.modelos.carritocreditos

import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.logica.fondos.CalculadorPuedeAgregarseSegunUnicidad
import co.smartobjects.ui.modelos.ListaNotificadoraCambios
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.TipoObservableListaNotificadoraCambios
import co.smartobjects.ui.modelos.catalogo.ProductoUI
import co.smartobjects.utilidades.Decimal
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.PublishSubject

interface CarritoDeCreditosInmutableUI : ModeloUI
{
    val creditosTotales: Observable<List<ItemCreditoUI>>
}

interface CarritoDeCreditosUI : ModeloUI, CarritoDeCreditosInmutableUI
{
    val dineroCredito: Decimal
    val creditosPreIncluidos: List<ItemCreditoUI>
    val creditosAgregados: Observable<TipoObservableListaNotificadoraCambios<ItemCreditoUI>>
    val idsTotalesDeFondos: Observable<Set<Long>>
    val idsDiferentesDeFondos: Observable<Set<Long>>
    val idsDiferentesDePaquetes: Observable<Set<Long>>
    val totalSinImpuesto: Observable<Decimal>
    val impuestoTotal: Observable<Decimal>
    val granTotal: Observable<Decimal>
    val valorYaPagado: Decimal
    val saldo: Observable<Decimal>
    val creditosAProcesar: Single<CreditosAProcesar>
    val hayCreditosSinConfirmar: Observable<Boolean>

    fun agregarAlCarrito(producto: ProductoUI)
    fun confirmarCreditosAgregados()
    fun cancelarCreditosAgregados()
    fun pagar()
    fun removerCreditosAgregados()

    data class CreditosAProcesar(val sinPagar: List<ItemCreditoUI>, val pagados: List<ItemCreditoUI>)
    {
        val tieneCreditos = sinPagar.isNotEmpty() || pagados.isNotEmpty()
    }
}

class CarritoDeCreditos
(
        override val dineroCredito: Decimal,
        creditosFondoPreIncluidos: List<CreditoFondoConNombre>,
        creditosPaquetePreIncluidos: List<CreditoPaqueteConNombre>,
        private val calculadorPuedeAgregarseSegunUnicidad: CalculadorPuedeAgregarseSegunUnicidad
) : CarritoDeCreditosUI
{
    override val creditosPreIncluidos: List<ItemCreditoUI> =
            (
                    creditosFondoPreIncluidos.asSequence().map {
                        ItemCredito(
                                it.nombreDeFondo,
                                it.creditoAsociado.idFondoComprado,
                                it.creditoAsociado.codigoExternoFondo,
                                it.precioCompleto,
                                it.creditoAsociado.id,
                                calculadorPuedeAgregarseSegunUnicidad.algunoEsUnico(setOf(it.creditoAsociado.idFondoComprado)),
                                it.creditoAsociado.cantidad.valor.toInt(),
                                true,
                                false
                                   )
                    }
                    +
                    creditosPaquetePreIncluidos.asSequence().map {
                        val idsFondos = it.creditoAsociado.creditosFondos.map { it.idFondoComprado }

                        ItemCredito(
                                it.nombreDelPaquete,
                                it.creditoAsociado.idPaquete,
                                it.creditoAsociado.codigoExternoPaquete,
                                idsFondos,
                                it.creditoAsociado.creditosFondos.map { it.codigoExternoFondo },
                                it.preciosCompletos,
                                if (it.estaPagado) it.creditoAsociado.creditosFondos.map { it.id!! } else null,
                                calculadorPuedeAgregarseSegunUnicidad.algunoEsUnico(idsFondos.toSet()),
                                it.creditoAsociado.creditosFondos.map { it.cantidad },
                                1,
                                true,
                                false
                                   )
                    }
            ).toList()

    private val creditosPreIncluidosSinPagar = creditosPreIncluidos.filter { !it.estaPagado }

    private val creditosNuevos = ListaNotificadoraCambios<ItemCreditoUI>()
    override val creditosAgregados = creditosNuevos.observar()
    override val creditosTotales =
            creditosAgregados
                .map {
                    (it.asSequence() + creditosPreIncluidos.asSequence()).toList()
                }
                .startWith(creditosPreIncluidos)!!

    override val idsTotalesDeFondos: Observable<Set<Long>> =
            creditosTotales.map {
                it.asSequence().flatMap { it.idFondos.asSequence() }.toSet()
            }

    override val idsDiferentesDeFondos: Observable<Set<Long>> =
            creditosTotales.map { it.filter { !it.esPaquete } }.distinctUntilChanged().map {
                it.asSequence().flatMap { it.idFondos.asSequence() }.toSet()
            }

    override val idsDiferentesDePaquetes: Observable<Set<Long>> =
            creditosTotales.map { it.filter { it.esPaquete } }.distinctUntilChanged().map {
                it.asSequence().map { it.idPaquete!! }.toSet()
            }

    /*
    total sin impuesto = suma de todos los precios sin impuesto
    impuesto total = suma de precio[i] * (1 + impuesto porcentual[i])
    dinero crédito = valor que había pagado previamente la persona (i.e. a través de Compensar)
    gran total = total + impuesto
    pagado = porcentaje que ha pagado% × gran total
    saldo = gran total - pagado - crédito
     */
    private val totalSinImpuestoPreIncluido =
            creditosFondoPreIncluidos
                .asSequence()
                .map { it.creditoAsociado.valorPagadoSinImpuesto }
                .plus(creditosPaquetePreIncluidos.asSequence().map { it.creditoAsociado.valorPagadoSinImpuesto })
                .fold(Decimal.CERO) { acc, siguiente -> acc + siguiente }

    private val totalImpuestoPreIncluido =
            creditosFondoPreIncluidos
                .asSequence()
                .map { it.creditoAsociado.valorImpuestoPagado }
                .plus(creditosPaquetePreIncluidos.asSequence().map { it.creditoAsociado.valorImpuestoPagado })
                .fold(Decimal.CERO) { acc, siguiente -> acc + siguiente }

    override val totalSinImpuesto: Observable<Decimal> =
            creditosAgregados.sumarPropiedadDecimalListaObservableCreditos(totalSinImpuestoPreIncluido) {
                it.precioSinImpuesto
            }

    override val impuestoTotal: Observable<Decimal> =
            creditosAgregados.sumarPropiedadDecimalListaObservableCreditos(totalImpuestoPreIncluido) {
                it.valorImpuesto
            }

    override val granTotal: Observable<Decimal> =
            creditosAgregados.sumarPropiedadDecimalListaObservableCreditos(
                    totalSinImpuestoPreIncluido + totalImpuestoPreIncluido
                                                                          ) {
                it.precioConImpuestos
            }

    override val valorYaPagado =
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
                .fold(Decimal.CERO) { acc, siguiente -> acc + siguiente }

    override val saldo: Observable<Decimal> = granTotal.map { it - valorYaPagado - dineroCredito }

    private val creditosSinConfirmar = ListaNotificadoraCambios<CreditoAgregado>()
    override val hayCreditosSinConfirmar: Observable<Boolean> =
            creditosAgregados.flatMap {
                // Se usa un observable con false para forzar a que se emita de una un valor
                val confirmaciones = (it.asSequence().map { it.faltaConfirmacionAdicion } + Observable.just(false)).toList()

                Observable.combineLatest(confirmaciones) { emisiones: Array<out Any> ->
                    emisiones.asSequence().map { it as Boolean }.fold(false) { acumulado, siguiente -> acumulado || siguiente }
                }
            }.distinctUntilChanged().startWith(false)

    private val eventosDePago = PublishSubject.create<List<ItemCreditoUI>>()
    private val creditosNoPagados = creditosAgregados.map {
        it.filter { !it.estaPagado } + creditosPreIncluidosSinPagar
    }
    override val creditosAProcesar: Single<CarritoDeCreditosUI.CreditosAProcesar> =
            eventosDePago
                .withLatestFrom(hayCreditosSinConfirmar, creditosNoPagados.startWith(listOf<ItemCreditoUI>()))
                .filter { !it.second }
                .map { CarritoDeCreditosUI.CreditosAProcesar(it.third, creditosPreIncluidos.filter { it.estaPagado }) }
                .filter { it.tieneCreditos }
                .firstOrError()

    override val observadoresInternos: List<Observer<*>> = listOf(eventosDePago)
    override val modelosHijos: List<ModeloUI>
        get() = creditosPreIncluidos + creditosNuevos.snapshotItems

    private val disposables = CompositeDisposable()

    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    override fun agregarAlCarrito(producto: ProductoUI)
    {
        val posibleProductoYaAgregado =
                creditosNuevos.buscar {
                    it.nombreProducto == producto.nombre
                    && it.idFondos == producto.idsFondosAsociados.toList()
                    && it.esPaquete == producto.esPaquete
                    && !it.noEsModificable
                    && !it.estaPagado
                }

        if (posibleProductoYaAgregado != null)
        {
            val creditoSinConfirmarBuscado = CreditoAgregado(posibleProductoYaAgregado)
            val posibleCreditoSinConfirmarYaAgregado =
                    creditosSinConfirmar.buscar { it == creditoSinConfirmarBuscado }

            if (posibleCreditoSinConfirmarYaAgregado == null)
            {
                creditosSinConfirmar.agregarAlInicio(creditoSinConfirmarBuscado)
            }
            else
            {
                posibleCreditoSinConfirmarYaAgregado.sumarUno()
            }

            posibleProductoYaAgregado.sumarUno()
        }
        else
        {
            val algunFondoEsUnico = calculadorPuedeAgregarseSegunUnicidad.algunoEsUnico(producto.idsFondosAsociados)
            val creditoAAgregar =
                    if (producto.esPaquete)
                    {
                        ItemCredito(
                                producto.nombre,
                                producto.idPaquete!!,
                                producto.codigoExternoPaquete!!,
                                producto.idsFondosAsociados.toList(),
                                producto.codigosExternosAsociados,
                                producto.preciosDeFondosAsociados,
                                null,
                                algunFondoEsUnico,
                                producto.cantidadesFondosEnPaquete!!,
                                1,
                                false,
                                true
                                   )
                    }
                    else
                    {
                        ItemCredito(
                                producto.nombre,
                                producto.idsFondosAsociados.first(),
                                producto.codigosExternosAsociados.first(),
                                producto.preciosDeFondosAsociados.first(),
                                null,
                                algunFondoEsUnico,
                                1,
                                false,
                                true
                                   )
                    }


            creditoAAgregar.seDebeBorrar
                .subscribe {
                    creditosNuevos.removerPorHashcode(it)
                }.addTo(disposables)

            creditosNuevos.agregarAlInicio(creditoAAgregar)

            creditosSinConfirmar.agregarAlInicio(CreditoAgregado(creditoAAgregar))
        }
    }

    override fun confirmarCreditosAgregados()
    {
        creditosSinConfirmar.paraCada { it.confirmar() }
        creditosSinConfirmar.limpiar()
    }

    override fun cancelarCreditosAgregados()
    {
        creditosSinConfirmar.paraCada { it.cancelar() }
        creditosSinConfirmar.limpiar()
    }

    override fun pagar()
    {
        eventosDePago.onNext(emptyList())
    }

    override fun removerCreditosAgregados()
    {
        cancelarCreditosAgregados()
        creditosNuevos.limpiar()
    }

    private fun
            Observable<TipoObservableListaNotificadoraCambios<ItemCreditoUI>>.sumarPropiedadDecimalListaObservableCreditos(
            valorPorDefecto: Decimal,
            mapAPropiedad: (ItemCreditoUI) -> Observable<Decimal>)
            : Observable<Decimal>
    {
        return flatMap {
            // Se usa un observable con decimal 0 para forzar a que se emita de una un valor
            val comoDecimal = (it.asSequence().map(mapAPropiedad) + Observable.just(Decimal.CERO)).toList()

            Observable.combineLatest(comoDecimal) { emisiones: Array<out Any> ->
                emisiones.asSequence().map { it as Decimal }.fold(Decimal.CERO) { acumulado, siguiente -> acumulado + siguiente }
            }.map {
                valorPorDefecto + it
            }
        }
            .startWith(valorPorDefecto)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CarritoDeCreditos

        if (dineroCredito != other.dineroCredito) return false
        if (creditosPreIncluidos != other.creditosPreIncluidos) return false
        if (totalSinImpuestoPreIncluido != other.totalSinImpuestoPreIncluido) return false
        if (totalImpuestoPreIncluido != other.totalImpuestoPreIncluido) return false
        if (valorYaPagado != other.valorYaPagado) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = dineroCredito.hashCode()
        result = 31 * result + creditosPreIncluidos.hashCode()
        result = 31 * result + totalSinImpuestoPreIncluido.hashCode()
        result = 31 * result + totalImpuestoPreIncluido.hashCode()
        result = 31 * result + valorYaPagado.hashCode()
        return result
    }

    private data class CreditoAgregado(private val credito: ItemCreditoUI, private var cantidadSumada: Int)
    {
        constructor(credito: ItemCreditoUI) : this(credito, 1)

        fun confirmar()
        {
            credito.confirmarAdicion()
        }

        fun cancelar()
        {
            repeat(cantidadSumada) { credito.restarUno() }
        }

        fun sumarUno()
        {
            cantidadSumada += 1
        }
    }
}