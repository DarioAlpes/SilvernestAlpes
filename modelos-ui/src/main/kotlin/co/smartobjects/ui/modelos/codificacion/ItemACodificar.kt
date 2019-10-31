package co.smartobjects.ui.modelos.codificacion

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.tagscodificables.TagConsumos
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.nfc.operacionessobretags.ResultadoLecturaNFC
import co.smartobjects.nfc.utils.comprimir
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.SesionDeManillaAPI
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.pagos.ProcesoPagarUI
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.sumar
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*

interface ItemACodificarUI : ModeloUI
{
    val creditosACodificar: ProcesoPagarUI.CreditosACodificarPorPersona
    val estado: Observable<Estado>
    val mensajesDeError: Observable<String>

    val totalPagado: Decimal
    val itemsCreditosACodificar: List<ItemCreditoACodificar>

    fun habilitarParaCodificacion()

    /**
     * Retorna true si intentó activar la sesión. False si no intentó (e.g. no está codificado el tag)
     */
    fun intentarActivarSesion(): Boolean

    sealed class Estado
    {
        object Codificando : Estado()
        {
            override fun toString() = this::class.java.simpleName!!
        }

        object Activando : Estado()
        {
            override fun toString() = this::class.java.simpleName!!
        }

        sealed class EtapaSesionManilla : Estado()
        {
            object SinCodificar : EtapaSesionManilla()
            {
                override fun toString() = this::class.java.simpleName!!
            }

            object EsperandoTag : EtapaSesionManilla()
            {
                override fun toString() = this::class.java.simpleName!!
            }

            class Codificada(val uuidDelTag: ByteArray) : EtapaSesionManilla()
            {
                override fun equals(other: Any?): Boolean
                {
                    if (this === other) return true
                    if (javaClass != other?.javaClass) return false

                    other as Codificada

                    if (!Arrays.equals(uuidDelTag, other.uuidDelTag)) return false

                    return true
                }

                override fun hashCode(): Int
                {
                    return Arrays.hashCode(uuidDelTag)
                }

                override fun toString() = this::class.java.simpleName!!
            }

            object Activada : EtapaSesionManilla()
            {
                override fun toString() = this::class.java.simpleName!!
            }
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int
        {
            return javaClass.hashCode()
        }
    }

    data class ItemCreditoACodificar(val nombre: String, val valorPagado: Decimal, val cantidad: Int)
}

class ItemACodificar
(
        private val sesionDeManilla: SesionDeManilla,
        override val creditosACodificar: ProcesoPagarUI.CreditosACodificarPorPersona,
        resultadosNFCLeidos: Flowable<ResultadoNFC.Exitoso>,
        private val apiSesionDeManilla: SesionDeManillaAPI,
        schedulerBackground: Scheduler = Schedulers.io()
) : ItemACodificarUI
{
    private var estadoDeEscrituraAlDeshabilitar: ItemACodificarUI.Estado.EtapaSesionManilla = ItemACodificarUI.Estado.EtapaSesionManilla.SinCodificar
    private val eventosDeEstado = BehaviorSubject.createDefault<ItemACodificarUI.Estado>(estadoDeEscrituraAlDeshabilitar)
    override val estado: Observable<ItemACodificarUI.Estado> = eventosDeEstado.distinctUntilChanged()

    private val eventosDeMensajesError = PublishSubject.create<String>()
    override val mensajesDeError: Observable<String> = eventosDeMensajesError

    override val totalPagado = creditosACodificar.creditosFondoTotales.map { it.valorPagado }.sumar()

    override val itemsCreditosACodificar =
            (creditosACodificar.creditosFondoPagados.asSequence().map {
                ItemACodificarUI.ItemCreditoACodificar(
                        it.nombreDeFondo, it.creditoAsociado.valorPagado, it.creditoAsociado.cantidad.aInt()
                                                      )
            } + creditosACodificar.creditosPaquetePagados.asSequence().map {
                ItemACodificarUI.ItemCreditoACodificar(
                        it.nombreDelPaquete, it.creditoAsociado.valorPagado, it.cantidad
                                                      )
            }).toList()

    override val observadoresInternos: List<Observer<*>> = listOf(eventosDeEstado, eventosDeMensajesError)
    override val modelosHijos: List<ModeloUI> = listOf()

    private val disposables = CompositeDisposable()

    init
    {
        resultadosNFCLeidos
            .withLatestFrom(estado.toFlowable(BackpressureStrategy.LATEST))
            .filter { it.second === ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag }
            .map { it.first }
            .subscribeOn(schedulerBackground)
            .subscribe {
                codificar(it)
            }
            .addTo(disposables)
    }

    override fun habilitarParaCodificacion()
    {
        when (estadoDeEscrituraAlDeshabilitar)
        {
            ItemACodificarUI.Estado.EtapaSesionManilla.Activada ->
            {
                eventosDeMensajesError.onNext("La sesión ya se encuentra activa y tiene un tag asociado")
            }
            else                                                ->
            {
                estadoDeEscrituraAlDeshabilitar = ItemACodificarUI.Estado.EtapaSesionManilla.EsperandoTag
                eventosDeEstado.onNext(estadoDeEscrituraAlDeshabilitar)
            }
        }
    }

    private fun codificar(resultadoNFC: ResultadoNFC.Exitoso)
    {
        // Limpiar mensaje anterior
        print("\nSesion actual : "+sesionDeManilla.id)
        eventosDeMensajesError.onNext("")

        eventosDeEstado.onNext(ItemACodificarUI.Estado.Codificando)

        val lecturaNFC = resultadoNFC.operacion.leerTag()
        when (lecturaNFC)
        {
            ResultadoLecturaNFC.TagVacio                 ->
            {
                val tagACodificar = TagConsumos(sesionDeManilla.id!!, creditosACodificar.creditosFondoTotales)
                val codificoCorrectamente = resultadoNFC.operacion.escribirTag(comprimir(tagACodificar.aByteArray()))
                if (codificoCorrectamente)
                {
                    val uuidDelTag = resultadoNFC.operacion.darUID()
                    print("\nSesion de manilla a codificar: "+sesionDeManilla.id)
                    estadoDeEscrituraAlDeshabilitar = ItemACodificarUI.Estado.EtapaSesionManilla.Codificada(uuidDelTag)
                    eventosDeEstado.onNext(estadoDeEscrituraAlDeshabilitar)
                    intentarActivarSesion()
                }
                else
                {
                    eventosDeEstado.onNext(estadoDeEscrituraAlDeshabilitar)
                    eventosDeMensajesError.onNext("Error al intentar escribir en el tag")
                }
            }
            ResultadoLecturaNFC.LlaveDesconocida         ->
            {
                eventosDeEstado.onNext(estadoDeEscrituraAlDeshabilitar)
                eventosDeMensajesError.onNext("El tag está programado con una llave desconocida")
            }
            ResultadoLecturaNFC.SinAutenticacionActivada ->
            {
                eventosDeEstado.onNext(estadoDeEscrituraAlDeshabilitar)
                eventosDeMensajesError.onNext("El tag no tiene autenticación activada")
            }
            is ResultadoLecturaNFC.ErrorDeLectura        ->
            {
                eventosDeEstado.onNext(estadoDeEscrituraAlDeshabilitar)
                eventosDeMensajesError.onNext("Error al leer el tag")
            }
            is ResultadoLecturaNFC.TagLeido              ->
            {
                eventosDeEstado.onNext(estadoDeEscrituraAlDeshabilitar)
                eventosDeMensajesError.onNext("El tag leído ya contiene datos")
                resultadoNFC.operacion.borrarTag()
            }
        }
    }

    override fun intentarActivarSesion(): Boolean
    {
        val estadoActual = estadoDeEscrituraAlDeshabilitar
        val intentoActivar =
                if (estadoActual is ItemACodificarUI.Estado.EtapaSesionManilla.Codificada)
                {
                    eventosDeEstado.onNext(ItemACodificarUI.Estado.Activando)
                    val respuesta =
                            apiSesionDeManilla
                                .actualizarCampos(
                                        sesionDeManilla.id!!,
                                        SesionDeManillaAPI.ParametrosActualizacionParcial.Activacion(estadoActual.uuidDelTag)
                                                 )
                    when (respuesta)
                    {
                        RespuestaVacia.Exitosa       ->
                        {
                            estadoDeEscrituraAlDeshabilitar = ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                        }
                        RespuestaVacia.Error.Timeout ->
                        {
                            eventosDeMensajesError.onNext("Tiempo de espera al servidor agotado. No se pudo activar la sesión asociada con el servidor")
                        }
                        is RespuestaVacia.Error.Red  ->
                        {
                            eventosDeMensajesError.onNext("Hubo un error en la conexión y no fue posible contactar al servidor")
                        }
                        is RespuestaVacia.Error.Back ->
                        {
                            when (respuesta.error.codigoInterno)
                            {
                                SesionDeManillaDTO.CodigosError.SESION_YA_TIENE_TAG_ASOCIADO ->
                                {
                                    estadoDeEscrituraAlDeshabilitar = ItemACodificarUI.Estado.EtapaSesionManilla.Activada
                                    eventosDeMensajesError.onNext("La sesión ya se encuentra activa y tiene un tag asociado")
                                }
                                else                                                         ->
                                {
                                    eventosDeMensajesError.onNext("Error en petición: ${respuesta.error.mensaje}")
                                }
                            }
                        }
                    }

                    true
                }
                else
                {
                    false
                }

        eventosDeEstado.onNext(estadoDeEscrituraAlDeshabilitar)
        return intentoActivar
    }

    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemACodificar

        if (sesionDeManilla != other.sesionDeManilla) return false
        if (creditosACodificar != other.creditosACodificar) return false
        if (estadoDeEscrituraAlDeshabilitar != other.estadoDeEscrituraAlDeshabilitar) return false
        if (eventosDeEstado.value != other.eventosDeEstado.value) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = sesionDeManilla.hashCode()
        result = 31 * result + creditosACodificar.hashCode()
        result = 31 * result + estadoDeEscrituraAlDeshabilitar.hashCode()
        result = 31 * result + eventosDeEstado.value.hashCode()
        return result
    }
}