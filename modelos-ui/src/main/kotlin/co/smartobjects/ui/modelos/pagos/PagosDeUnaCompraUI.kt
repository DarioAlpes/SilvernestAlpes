package co.smartobjects.ui.modelos.pagos

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.Opcional
import co.smartobjects.utilidades.sumar
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface PagosDeUnaCompraUI : ModeloUIConListaDePagos
{
    // Puede ser negativo en caso de que registren más pagos que el valor que calculamos nosotros.
    // Se permite porque nosotros no manejamos los pagos, no queremos restringir más de la cuenta
    val valorFaltantePorPagar: Observable<Decimal>
    val pagoActuales: List<Pago>
    val pagosRegistrados: Observable<List<Pago>>
    val puedeRegistrarPago: Observable<Boolean>
    val totalPagado: Observable<Decimal>

    override val modelosHijos: List<ModeloUI>
        get() = listOf()
}

class PagosDeUnaCompra(valorTotalAPagar: Observable<Decimal>) : PagosDeUnaCompraUI
{
    private val pagos = mutableListOf<Pago>()
    private val eventoPago = PublishSubject.create<Decimal>()
    private val sujetoPagosRegistrados = BehaviorSubject.createDefault(pagos)

    override val valorFaltantePorPagar: Observable<Decimal> =
            valorTotalAPagar.switchMap {
                eventoPago.startWith(Decimal.CERO).scan(it) { acumulado, delta -> acumulado + delta }
            }.distinctUntilChanged()

    override val pagosRegistrados: Observable<List<Pago>> = sujetoPagosRegistrados.map { it.asReversed() }
    override val puedeRegistrarPago: Observable<Boolean> = valorFaltantePorPagar.map { it <= Decimal.CERO }.distinctUntilChanged()
    override val totalPagado: Observable<Decimal> = sujetoPagosRegistrados.map { it.sumar { it.valorPagado } }.distinctUntilChanged()

    override val pagoActuales: List<Pago>
        get() = pagos.toList()

    override val observadoresInternos: List<Observer<*>> = listOf(eventoPago, sujetoPagosRegistrados)

    override fun agregarPago(pago: Pago): Opcional<String>
    {
        pagos += pago
        sujetoPagosRegistrados.onNext(pagos)
        eventoPago.onNext(-pago.valorPagado)

        return Opcional.Vacio()
    }

    override fun eliminarPago(pago: Pago): Opcional<String>
    {
        return if (pagos.contains(pago))
        {
            pagos.remove(pago)
            sujetoPagosRegistrados.onNext(pagos)
            eventoPago.onNext(pago.valorPagado)
            Opcional.Vacio()
        }
        else
        {
            Opcional.De("No existe el pago")
        }
    }
}