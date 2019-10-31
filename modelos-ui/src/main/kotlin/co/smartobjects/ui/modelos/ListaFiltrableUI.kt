package co.smartobjects.ui.modelos

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject

interface ItemFiltrableUI<T> : ModeloUI
{
    val item: T
    val listaPadre: ListaFiltrableUI<T>
    val seleccionado: Observable<Boolean>
    val estaSeleccionado: Boolean
    val habilitado: Observable<Boolean>
    val estaHabilitado: Boolean

    fun seleccionar()
    fun deseleccionar()
    fun habilitar(habilitado: Boolean)

    override val modelosHijos: List<ModeloUI>
        get() = listOf()
}

interface ListaFiltrableUI<T> : ModeloUI
{
    val items: List<T>
    val itemsSeleccionados: Observable<List<T>>
    val itemsFiltrables: List<ItemFiltrableUI<T>>
    val todosLosHabilitadosEstanSeleccionados: Observable<Boolean>
    val numeroHabilitadosSeleccionados: Observable<Int>

    fun seleccionarItem(item: T)
    fun deseleccionarItem(item: T)
    fun seleccionarTodos()
    fun deseleccionarTodos()

    override val modelosHijos: List<ModeloUI>
        get() = itemsFiltrables
}

// Esta implementación se puede mejorar usando un set en lugar de filter + map en itemsConSeleccionTrue.
// Pero parte del contrato es que se mantiene el orden dado en items dentro de itemsSeleccionados.
// Entonces una implementación con sets es considerablemente más compleja para garantizar que se mantiene ese orden
class ListaFiltrableUIConSujetos<T>(override val items: List<T>) : ListaFiltrableUI<T>
{
    private val itemsConSeleccion = LinkedHashMap<T, ItemFiltrableConSujetos>()

    private val itemsConSeleccionTrue: List<T>
        get() = itemsConSeleccion.filter { it.value.estaSeleccionado }.map { it.key }

    private val sujetoItemsSeleccionados: BehaviorSubject<List<T>> = BehaviorSubject.create()

    private val itemsFiltrablesConSujetos: List<ItemFiltrableConSujetos> = items.map { ItemFiltrableConSujetos(it) }
    override val itemsFiltrables: List<ItemFiltrableUI<T>> = itemsFiltrablesConSujetos

    override val itemsSeleccionados: Observable<List<T>> = sujetoItemsSeleccionados
    override val todosLosHabilitadosEstanSeleccionados: Observable<Boolean> =
            itemsSeleccionados.map {
                itemsConSeleccion.filter { it.value.estaHabilitado }.size == itemsConSeleccionTrue.size
            }

    override val numeroHabilitadosSeleccionados: Observable<Int> =
            itemsSeleccionados.map { itemsFiltrables.count { it.estaHabilitado && it.estaSeleccionado } }

    override val observadoresInternos: List<Observer<*>> = listOf(sujetoItemsSeleccionados)

    init
    {
        itemsFiltrablesConSujetos.map { it.item to it }.toMap(itemsConSeleccion)
        sujetoItemsSeleccionados.onNext(itemsConSeleccionTrue)
    }

    override fun seleccionarItem(item: T)
    {
        val itemConSeleccion = itemsConSeleccion[item]
        if (itemConSeleccion?.estaSeleccionado == false && itemConSeleccion?.estaHabilitado == true)
        {
            itemConSeleccion.seleccionarSinActualizarPadre()
            sujetoItemsSeleccionados.onNext(itemsConSeleccionTrue)
        }
    }

    override fun deseleccionarItem(item: T)
    {
        val itemConSeleccion = itemsConSeleccion[item]
        if (itemConSeleccion?.estaSeleccionado == true && itemConSeleccion?.estaHabilitado == true)
        {
            itemConSeleccion.deseleccionarSinActualizarPadre()
            sujetoItemsSeleccionados.onNext(itemsConSeleccionTrue)
        }
    }

    override fun seleccionarTodos()
    {
        itemsConSeleccion.values.forEach {
            it.seleccionarSinActualizarPadre()
        }
        sujetoItemsSeleccionados.onNext(itemsConSeleccionTrue)
    }

    override fun deseleccionarTodos()
    {
        itemsConSeleccion.values.forEach {
            it.deseleccionarSinActualizarPadre()
        }
        sujetoItemsSeleccionados.onNext(itemsConSeleccionTrue)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListaFiltrableUIConSujetos<*>

        if (items != other.items) return false
        if (itemsConSeleccion != other.itemsConSeleccion) return false
        if (itemsFiltrables != other.itemsFiltrables) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = items.hashCode()
        result = 31 * result + itemsConSeleccion.hashCode()
        result = 31 * result + itemsFiltrables.hashCode()
        return result
    }

    private inner class ItemFiltrableConSujetos(override val item: T) : ItemFiltrableUI<T>
    {
        override val listaPadre: ListaFiltrableUI<T> = this@ListaFiltrableUIConSujetos
        private val sujetoSeleccionado: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(true)
        override val seleccionado: Observable<Boolean> = sujetoSeleccionado.distinctUntilChanged()
        override val estaSeleccionado: Boolean
            get() = sujetoSeleccionado.value!!

        private val eventosHabilitado: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(true)
        override val habilitado: Observable<Boolean> = eventosHabilitado.distinctUntilChanged()
        override val estaHabilitado: Boolean
            get() = eventosHabilitado.value!!

        override fun seleccionar()
        {
            listaPadre.seleccionarItem(item)
        }

        override fun deseleccionar()
        {
            listaPadre.deseleccionarItem(item)
        }

        internal fun seleccionarSinActualizarPadre()
        {
            if (estaHabilitado)
            {
                sujetoSeleccionado.onNext(true)
            }
        }

        internal fun deseleccionarSinActualizarPadre()
        {
            if (estaHabilitado)
            {
                sujetoSeleccionado.onNext(false)
            }
        }

        override fun habilitar(habilitado: Boolean)
        {
            eventosHabilitado.onNext(habilitado)
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ItemFiltrableUI<T>

            if (item != other.item) return false
            if (estaSeleccionado != other.estaSeleccionado) return false
            if (estaHabilitado != other.estaHabilitado) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = item.hashCode()
            result = 31 * result + estaSeleccionado.hashCode()
            result = 31 * result + estaHabilitado.hashCode()
            return result
        }

        override val observadoresInternos: List<Observer<*>> = listOf(sujetoSeleccionado)
    }
}