@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.ui.modelos

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import hu.akarnokd.rxjava2.operators.FlowableTransformers
import io.reactivex.BackpressureStrategy
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.threeten.bp.DateTimeException
import java.lang.ref.WeakReference


internal inline fun <T> Subject<Notification<T>>.crearYEmitirEntidadNegocioConPosibleError(crearEntidadConError: () -> T)
{
    try
    {
        onNext(Notification.createOnNext(crearEntidadConError()))
    }
    catch (e: EntidadMalInicializada)
    {
        onNext(Notification.createOnError(e))
    }
}

internal inline fun <T : CampoEntidad<*, R>, R> Observable<Notification<T>>.mapNotificationValorCampo(): Observable<Notification<R>>
{
    return mapNotification { it.valor }
}

inline fun <T, R> Observable<Notification<T>>.mapNotification(crossinline transformacion: (T) -> R): Observable<Notification<R>>
{
    return map {
        when
        {
            it.isOnNext  -> Notification.createOnNext(transformacion(it.value!!))
            it.isOnError -> Notification.createOnError(it.error!!)
            else         -> Notification.createOnComplete()
        }
    }
}

internal inline fun <T, N : Notification<C>, C : CampoEntidad<*, T>> BehaviorSubject<N>.valorDeCampo(): T
{
    return value!!.value!!.valor
}

@Throws(IllegalStateException::class)
internal inline fun <T> transformarAEntidadUIEnvolviendoErrores(accion: () -> T): T
{
    try
    {
        return accion()
    }
    catch (e: DateTimeException)
    {
        throw IllegalStateException(e)
    }
    catch (e: NullPointerException)
    {
        throw IllegalStateException(e)
    }
}

@Throws(IllegalArgumentException::class)
internal inline fun ejecutarAccionEnvolviendoErrores(accion: () -> Unit)
{
    try
    {
        return accion()
    }
    catch (e: NullPointerException)
    {
        throw IllegalArgumentException(e)
    }
}

inline fun <Valor> emitirUnicoValorSi(condicion: Boolean, valorUnico: Valor, crearObservablePorDefecto: (Valor) -> Observable<Valor>)
        : Observable<Valor>
{
    return if (condicion)
    {
        Observable.just<Valor>(valorUnico)
    }
    else
    {
        crearObservablePorDefecto(valorUnico)
    }
}


internal typealias TipoObservableListaNotificadoraCambios<TipoItem> = List<TipoItem>

class ListaNotificadoraCambios<TipoItem>
{
    private val itemsAgregados = mutableListOf<TipoItem>()

    private val puedeHacerModificaciones = PublishProcessor.create<Boolean>()
    private val eventoModificacion = PublishSubject.create<Modificacion<TipoItem>>()
    private val modificacionesPendientes = eventoModificacion.toFlowable(BackpressureStrategy.BUFFER)

    val snapshotItems: List<TipoItem>
        get() = itemsAgregados.toList()

    private val eventosDeCambio = PublishSubject.create<TipoObservableListaNotificadoraCambios<TipoItem>>()

    init
    {
        modificacionesPendientes
            .compose(FlowableTransformers.valve(puedeHacerModificaciones))
            .subscribe {
                when (it)
                {
                    is ListaNotificadoraCambios.Modificacion.Agregar -> ejecutarAgregarAlInicio(it.elemento)
                    is ListaNotificadoraCambios.Modificacion.Borrar  -> ejecutarRemoverPorHashcode(it.referenciaAItemABorrar)
                    is ListaNotificadoraCambios.Modificacion.Limpiar -> ejecutarLimpiar()
                }
            }
    }


    fun buscar(predicate: (TipoItem) -> Boolean): TipoItem?
    {
        puedeHacerModificaciones.onNext(false)
        val itemEncontrado = itemsAgregados.find(predicate)
        puedeHacerModificaciones.onNext(true)

        return itemEncontrado
    }

    fun paraCada(action: (TipoItem) -> Unit)
    {
        puedeHacerModificaciones.onNext(false)
        for (element in itemsAgregados)
        {
            action(element)
        }
        puedeHacerModificaciones.onNext(true)
    }

    fun agregarAlInicio(element: TipoItem)
    {
        eventoModificacion.onNext(Modificacion.Agregar(element))
    }

    private fun ejecutarAgregarAlInicio(itemAAgregar: TipoItem)
    {
        itemsAgregados.add(0, itemAAgregar).notificarCambio()
    }

    fun removerPorHashcode(hashCodeItemARemover: Int)
    {
        val indiceElemento = itemsAgregados.indexOfFirst { it?.hashCode() == hashCodeItemARemover }
        val referencia = WeakReference(itemsAgregados[indiceElemento])

        eventoModificacion.onNext(Modificacion.Borrar(referencia))
    }

    private fun ejecutarRemoverPorHashcode(referenciaAItemABorrar: WeakReference<TipoItem>)
    {
        val elemento = referenciaAItemABorrar.get()
        if (elemento != null)
        {
            itemsAgregados.remove(elemento).notificarCambio()
        }
    }

    fun limpiar()
    {
        eventoModificacion.onNext(Modificacion.Limpiar())
    }

    private fun ejecutarLimpiar()
    {
        itemsAgregados.clear().notificarCambio()
    }

    fun observar(): Observable<TipoObservableListaNotificadoraCambios<TipoItem>>
    {
        return eventosDeCambio.hide()
    }

    private inline fun <T> T.notificarCambio(): T
    {
        return also { eventosDeCambio.onNext(snapshotItems) }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListaNotificadoraCambios<*>

        if (itemsAgregados != other.itemsAgregados) return false

        return true
    }

    override fun hashCode(): Int
    {
        return itemsAgregados.hashCode()
    }

    @Suppress("unused")
    private sealed class Modificacion<in TipoElementoClase>
    {
        class Agregar<TipoElemento>(val elemento: TipoElemento) : Modificacion<TipoElemento>()
        class Borrar<TipoElemento>(val referenciaAItemABorrar: WeakReference<TipoElemento>) : Modificacion<TipoElemento>()
        class Limpiar<TipoElemento> : Modificacion<TipoElemento>()
    }
}