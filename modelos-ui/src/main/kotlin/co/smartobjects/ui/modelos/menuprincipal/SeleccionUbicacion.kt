package co.smartobjects.ui.modelos.menuprincipal

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.ui.modelos.ModeloUI
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface SeleccionUbicacionUI : ModeloUI
{
    val estado: Observable<Estado>
    val puntosDeContactoDisponibles: Observable<List<Ubicacion>>

    fun seleccionarUbicacion()

    override val modelosHijos: List<ModeloUI>
        get() = listOf()

    enum class Estado
    {
        PERMITIENDO_SELECCIONAR_UBICACION, NO_PERMITIENDO_SELECCIONAR_UBICACION
    }
}

class SeleccionUbicacion
(
        private val idCliente: Long,
        private val repositorioUbicaciones: RepositorioUbicaciones
) : SeleccionUbicacionUI
{
    private val eventosEstado = BehaviorSubject.createDefault<SeleccionUbicacionUI.Estado>(SeleccionUbicacionUI.Estado.NO_PERMITIENDO_SELECCIONAR_UBICACION)
    override val estado: Observable<SeleccionUbicacionUI.Estado> = eventosEstado

    private val eventosPuntosDeContactoDisponibles = PublishSubject.create<List<Ubicacion>>()
    override val puntosDeContactoDisponibles: Observable<List<Ubicacion>> = eventosPuntosDeContactoDisponibles

    override val observadoresInternos: List<Observer<*>> = listOf(eventosEstado)

    init
    {
        estado
            .filter { it === SeleccionUbicacionUI.Estado.PERMITIENDO_SELECCIONAR_UBICACION }
            .doOnNext { eventosEstado.onNext(SeleccionUbicacionUI.Estado.NO_PERMITIENDO_SELECCIONAR_UBICACION) }
            .observeOn(Schedulers.io())
            .map {
                repositorioUbicaciones
                    .listar(idCliente)
                    .filter { it.tipo === Ubicacion.Tipo.PUNTO_DE_CONTACTO }
                    .toList()
            }
            .subscribe(eventosPuntosDeContactoDisponibles)
    }

    override fun seleccionarUbicacion()
    {
        eventosEstado.onNext(SeleccionUbicacionUI.Estado.PERMITIENDO_SELECCIONAR_UBICACION)
    }
}