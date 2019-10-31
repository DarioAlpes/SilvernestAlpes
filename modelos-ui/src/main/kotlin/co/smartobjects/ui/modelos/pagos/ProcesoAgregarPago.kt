package co.smartobjects.ui.modelos.pagos

import co.smartobjects.entidades.operativas.compras.Pago
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.ResultadoAccionUI
import co.smartobjects.utilidades.Opcional
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

interface ProcesoAgregarPago : ModeloUI
{
    val pago: PagoUI
    val estado: Observable<Estado>
    val errorGlobal: Observable<Opcional<String>>
    val puedeAgregarPago: Observable<Boolean>

    fun intentarAgregarPago(modeloConListaDePagos: ModeloUIConListaDePagos): ResultadoAccionUI
    fun reiniciar()

    override val modelosHijos: List<ModeloUI>
        get() = listOf(pago)

    enum class Estado
    {
        ESPERANDO_DATOS, PAGO_AGREGADO
    }
}

interface ModeloUIConListaDePagos : ModeloUI
{
    fun agregarPago(pago: Pago): Opcional<String>
    fun eliminarPago(pago: Pago): Opcional<String>
}

class ProcesoAgregarPagoConSujetos internal constructor(override val pago: PagoUI) : ProcesoAgregarPago
{
    constructor() : this(PagoUIConSujetos())

    private val sujetoEstado: BehaviorSubject<ProcesoAgregarPago.Estado> = BehaviorSubject.createDefault(ProcesoAgregarPago.Estado.ESPERANDO_DATOS)
    private val sujetoEsPagoCorrecto: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private val sujetoErrorGlobal: BehaviorSubject<Opcional<String>> = BehaviorSubject.createDefault(Opcional.Vacio())

    override val estado: Observable<ProcesoAgregarPago.Estado> = sujetoEstado
    override val errorGlobal: Observable<Opcional<String>> = sujetoErrorGlobal
    override val puedeAgregarPago: Observable<Boolean> = Observables.combineLatest(
            sujetoEsPagoCorrecto,
            estado,
            { esPagoCorrecto, estado -> esPagoCorrecto && estado == ProcesoAgregarPago.Estado.ESPERANDO_DATOS }
                                                                                  )

    override val observadoresInternos: List<Subject<*>> = listOf(sujetoEstado, sujetoEsPagoCorrecto, sujetoErrorGlobal)

    init
    {
        pago.esPagoValido.subscribe(sujetoEsPagoCorrecto)
    }

    override fun intentarAgregarPago(modeloConListaDePagos: ModeloUIConListaDePagos): ResultadoAccionUI
    {
        return if (sujetoEstado.value == ProcesoAgregarPago.Estado.ESPERANDO_DATOS)
        {
            if (sujetoEsPagoCorrecto.value!!)
            {
                val pagoActual =
                        try
                        {
                            pago.aPago()
                        }
                        catch (e: IllegalStateException)
                        {
                            sujetoErrorGlobal.onNext(Opcional.De("Pago inválido"))
                            return ResultadoAccionUI.OBSERVABLES_EN_ESTADO_INVALIDO
                        }

                sujetoErrorGlobal.onNext(Opcional.Vacio())
                val posibleError = modeloConListaDePagos.agregarPago(pagoActual)
                if (!posibleError.esVacio)
                {
                    sujetoErrorGlobal.onNext(posibleError)
                }
                else
                {
                    sujetoEstado.onNext(ProcesoAgregarPago.Estado.PAGO_AGREGADO)
                }
                ResultadoAccionUI.ACCION_INICIADA
            }
            else
            {
                ResultadoAccionUI.MODELO_EN_ESTADO_INVALIDO
            }
        }
        else
        {
            ResultadoAccionUI.PROCESO_EN_ESTADO_INVALIDO
        }
    }

    override fun reiniciar()
    {
        sujetoEstado.onNext(ProcesoAgregarPago.Estado.ESPERANDO_DATOS)
        sujetoEsPagoCorrecto.onNext(false)
        sujetoErrorGlobal.onNext(Opcional.Vacio())
    }
}