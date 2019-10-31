package co.smartobjects.ui.modelos.pagos

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.ui.modelos.*
import co.smartobjects.utilidades.Decimal
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject

interface PagoUI : ModeloUI
{
    val valorPagado: Observable<Notification<Decimal>>
    val metodoPago: Observable<Pago.MetodoDePago>
    val numeroTransaccionPOS: Observable<Notification<String>>
    val esPagoValido: Observable<Boolean>

    fun cambiarValorPagado(nuevoValorPagado: String)
    fun cambiarMetodoPago(nuevoMetodoPago: Pago.MetodoDePago)
    fun cambiarNumeroTransaccionPOS(nuevoNumeroTransaccionPOS: String)
    @Throws(IllegalStateException::class)
    fun aPago(): Pago

    override val modelosHijos: List<ModeloUI>
        get() = listOf()
}

class PagoUIConSujetos : PagoUI
{
    private val sujetoValorPagado = BehaviorSubject.create<Notification<Pago.CampoValorPagado>>()
    private val sujetoMetodoPago = BehaviorSubject.create<Pago.MetodoDePago>()
    private val sujetoNumeroTransaccionPOS = BehaviorSubject.create<Notification<Pago.CampoNumeroTransaccion>>()

    override val valorPagado: Observable<Notification<Decimal>> = sujetoValorPagado.mapNotificationValorCampo()
    override val metodoPago: Observable<Pago.MetodoDePago> = sujetoMetodoPago
    override val numeroTransaccionPOS: Observable<Notification<String>> = sujetoNumeroTransaccionPOS.mapNotificationValorCampo()
    override val esPagoValido: Observable<Boolean> =
            Observables.combineLatest(valorPagado, metodoPago, numeroTransaccionPOS)
            { posiblePago, _, posibleNumeroTransaccionPOS ->
                !posiblePago.isOnError && !posibleNumeroTransaccionPOS.isOnError
            }

    override val observadoresInternos = listOf(sujetoValorPagado, sujetoMetodoPago, sujetoNumeroTransaccionPOS)

    override fun cambiarValorPagado(nuevoValorPagado: String)
    {
        try
        {
            val valor = Decimal(nuevoValorPagado)
            sujetoValorPagado.crearYEmitirEntidadNegocioConPosibleError { Pago.CampoValorPagado(valor) }
        }
        catch (e: NumberFormatException)
        {
            sujetoValorPagado.onNext(Notification.createOnError(Exception("Debe ser numérico")))
        }
    }

    override fun cambiarMetodoPago(nuevoMetodoPago: Pago.MetodoDePago)
    {
        sujetoMetodoPago.onNext(nuevoMetodoPago)
    }

    override fun cambiarNumeroTransaccionPOS(nuevoNumeroTransaccionPOS: String)
    {
        sujetoNumeroTransaccionPOS.crearYEmitirEntidadNegocioConPosibleError { Pago.CampoNumeroTransaccion(nuevoNumeroTransaccionPOS) }
    }

    override fun aPago(): Pago
    {
        return transformarAEntidadUIEnvolviendoErrores {
            Pago(sujetoValorPagado.valorDeCampo(), sujetoMetodoPago.value!!, sujetoNumeroTransaccionPOS.valorDeCampo())
        }
    }
}