package co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.logica.fondos.CalculadorPuedeAgregarseSegunUnicidad
import co.smartobjects.logica.fondos.libros.ProveedorDePreciosCompletosYProhibiciones
import co.smartobjects.ui.modelos.ListaFiltrableUI
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditosUI
import co.smartobjects.ui.modelos.carritocreditos.ItemCreditoUI
import co.smartobjects.ui.modelos.catalogo.ProductoUI
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.sumar
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


interface AgrupacionPersonasCarritosDeCreditosUI : ModeloUI
{
    val personasConCarritos: Observable<ListaFiltrableUI<PersonaConCarrito>>
    val totalSinImpuesto: Observable<Decimal>
    val impuestoTotal: Observable<Decimal>
    val valorYaPagado: Observable<Decimal>
    val saldo: Observable<Decimal>
    val creditosAProcesar: Single<List<PersonaConCreditosSeleccionados>>
    val hayCreditosSinConfirmar: Observable<Boolean>
    val estaAgregandoProducto: Observable<Boolean>
    val puedePagar: Observable<Boolean>
    val todosLosCreditosEstanPagados: Observable<Boolean>

    fun agregarProducto(producto: ProductoUI)
    fun confirmarCreditosAgregados()
    fun cancelarCreditosAgregados()
    fun pagar()
    fun actualizarItems(nuevasPersonasConCarritos: ListaFiltrableUI<PersonaConCarrito>)
}

data class PersonaConCarrito(val persona: Persona, val grupoDeClientes: GrupoClientes?, val carritoDeCreditos: CarritoDeCreditosUI)
data class PersonaConCreditosSeleccionados(
        val persona: Persona,
        val grupoDeClientes: GrupoClientes?,
        val creditosAPagar: List<ItemCreditoUI>,
        val creditosPagados: List<ItemCreditoUI>
                                          )

class AgrupacionPersonasCarritosDeCreditos
(
        personasConCarritosIniciales: ListaFiltrableUI<PersonaConCarrito>,
        private val idUbicacion: Long,
        _proveedorDePreciosCompletosYProhibiciones: Single<ProveedorDePreciosCompletosYProhibiciones>,
        _calculadorPuedeAgregarseSegunUnicidad: Single<CalculadorPuedeAgregarseSegunUnicidad>
) : AgrupacionPersonasCarritosDeCreditosUI
{
    private val eventosDePagar = PublishSubject.create<Unit>()

    private val eventosDeAgregandoProducto = BehaviorSubject.create<EventoAgregar>()
    private val eventoProductoAgregado = eventosDeAgregandoProducto.distinctUntilChanged().replay(1).refCount()

    override val estaAgregandoProducto =
            eventoProductoAgregado
                .map { it is EventoAgregar.Agregando }
                .distinctUntilChanged()
                .replay(1).refCount()
                .startWith(false)!!

    private val eventosDeCambioDeItems = PublishSubject.create<ListaFiltrableUI<PersonaConCarrito>>()

    override val observadoresInternos: List<Observer<*>> =
            listOf(eventosDePagar, eventosDeAgregandoProducto, eventosDeCambioDeItems)

    private val modelosUIActuales = mutableListOf<ModeloUI>()
    override val modelosHijos: List<ModeloUI>
        get() = modelosUIActuales

    private val disposablesEnUsoDeListaFiltrable = CompositeDisposable()
    private val disposables = CompositeDisposable().also {
        it.add(disposablesEnUsoDeListaFiltrable)
    }

    override val personasConCarritos =
            eventosDeCambioDeItems
                .startWith(personasConCarritosIniciales)
                .doOnNext {
                    disposablesEnUsoDeListaFiltrable.clear()
                    modelosHijos.forEach { it.finalizarProceso() }
                    modelosUIActuales.clear()
                    it.items.mapTo(modelosUIActuales) { it.carritoDeCreditos }
                    modelosUIActuales.add(it)
                }
                .replay(1).refCount()

    private val carritos = personasConCarritos.map { it.items.map { it.carritoDeCreditos } }

    override val totalSinImpuesto = carritos.flatMap { it.map { it.totalSinImpuesto }.reducirASumaObservable() }!!

    override val impuestoTotal = carritos.flatMap { it.map { it.impuestoTotal }.reducirASumaObservable() }!!

    override val valorYaPagado =
            carritos.map { it.fold(Decimal.CERO) { acc, carrito -> acc + carrito.valorYaPagado } }!!

    override val saldo = carritos.flatMap { it.map { it.saldo }.reducirASumaObservable() }!!

    override val hayCreditosSinConfirmar =
            carritos.flatMap {
                it.map { it.hayCreditosSinConfirmar }.reducirAExistenciaObservable()
            }
                .distinctUntilChanged()!!

    private val todosLosCarritosTienenItems =
            carritos.flatMap {
                it.map { it.creditosTotales }.reducirAForAllObservable(false) { it.isNotEmpty() }
            }!!

    override val puedePagar =
            Observables.combineLatest(todosLosCarritosTienenItems, hayCreditosSinConfirmar)
                .map {
                    it.first && !it.second
                }.distinctUntilChanged()!!

    override val todosLosCreditosEstanPagados: Observable<Boolean> =
            personasConCarritos
                .map { it.items.map { it.carritoDeCreditos.creditosTotales.map { it.isNotEmpty() && it.all { it.estaPagado } } } }
                .switchMap {
                    Observable.combineLatest(it) { emisiones: Array<out Any> ->
                        emisiones.asSequence().all { it as Boolean }
                    }
                }
                .distinctUntilChanged()
                .startWith(false)

    override val creditosAProcesar: Single<List<PersonaConCreditosSeleccionados>> =
            personasConCarritos
                .filter { it.items.isNotEmpty() }
                .map {
                    it.items.map { personaConCarrito ->
                        personaConCarrito.carritoDeCreditos.creditosAProcesar.map {
                            PersonaConCreditosSeleccionados(
                                    personaConCarrito.persona,
                                    personaConCarrito.grupoDeClientes,
                                    it.sinPagar,
                                    it.pagados
                                                           )
                        }
                    }
                }
                .flatMap {
                    Single.zip(it) {
                        it.map { it as PersonaConCreditosSeleccionados }.toList()
                    }.toObservable()
                }
                .firstOrError()


    init
    {
        val proveedorDePreciosCompletosYProhibiciones = _proveedorDePreciosCompletosYProhibiciones.toObservable()
        val calculadorPuedeAgregarSegunUnicidad = _calculadorPuedeAgregarseSegunUnicidad.toObservable()
        personasConCarritos.forEach { listado ->
            for (itemFiltrable in listado.itemsFiltrables)
            {
                itemFiltrable.deseleccionar()

                val carritoAsociado = itemFiltrable.item.carritoDeCreditos

                // Cuando se está agregando, se confirma o se cancela un producto
                eventoProductoAgregado
                    .withLatestFrom(
                            arrayOf(
                                    carritoAsociado.idsTotalesDeFondos,
                                    carritoAsociado.idsDiferentesDeFondos,
                                    carritoAsociado.idsDiferentesDePaquetes,
                                    proveedorDePreciosCompletosYProhibiciones,
                                    calculadorPuedeAgregarSegunUnicidad
                                   )
                                   )
                    {
                        //eventoAgregar, idsTotalesDeFondos, idsFondos, idsPaquetes, proveedorDePreciosCompletosYProhibiciones, calculadorPuedeAgregarSegunUnicidad
                        EmisionProductoAgregado(
                                (it[0] as EventoAgregar),
                                (it[1] as Set<Long>),
                                (it[2] as Set<Long>),
                                (it[3] as Set<Long>),
                                (it[4] as ProveedorDePreciosCompletosYProhibiciones),
                                (it[5] as CalculadorPuedeAgregarseSegunUnicidad)
                                               )
                    }
                    .forEach {
                        if (it.eventoAgregar is EventoAgregar.Agregando)
                        {
                            val producto = it.eventoAgregar.producto
                            val noRestringidoPorItemUnicoYaAgregado =
                                    it.calculadorPuedeAgregarSegunUnicidad
                                        .puedeAgregarFondos(producto.idsFondosAsociados, it.idsTotalesDeFondos)

                            val noEstaProhibido =
                                    if (producto.esPaquete)
                                    {
                                        it.proveedorDePreciosCompletosYProhibiciones
                                            .verificarSiPaqueteEsVendible(
                                                    producto.idPaquete!!,
                                                    idUbicacion,
                                                    itemFiltrable.item.grupoDeClientes?.id,
                                                    it.idsDiferentesDeFondos,
                                                    it.idsDiferentesDePaquetes
                                                                         )
                                    }
                                    else
                                    {
                                        it.proveedorDePreciosCompletosYProhibiciones
                                            .verificarSiFondoEsVendible(
                                                    producto.idsFondosAsociados.first(),
                                                    idUbicacion,
                                                    itemFiltrable.item.grupoDeClientes?.id,
                                                    it.idsDiferentesDePaquetes
                                                                       )
                                    }

                            val estaItemHabilitado = noEstaProhibido && noRestringidoPorItemUnicoYaAgregado

                            if (estaItemHabilitado)
                            {
                                itemFiltrable.seleccionar()
                            }
                            else
                            {
                                itemFiltrable.deseleccionar()
                            }

                            // Esto se debe hacer después de cambiar la selección porque cuando está deshabilitado se ignoran esos eventos
                            itemFiltrable.habilitar(estaItemHabilitado)
                        }
                        else
                        {
                            itemFiltrable.habilitar(true)
                            listado.deseleccionarTodos()
                        }
                    }
                    .also { disposablesEnUsoDeListaFiltrable.add(it) }

                // Cuando se modifica la selección de un ítem. Se traduce en agregar o cancelar ítems de un solo carrito
                itemFiltrable.seleccionado
                    .filter {
                        eventosDeAgregandoProducto.value is EventoAgregar.Agregando
                    }
                    .withLatestFrom(proveedorDePreciosCompletosYProhibiciones)
                    .map {
                        EventoItemSeleccionado(
                                (eventosDeAgregandoProducto.value as EventoAgregar.Agregando).producto,
                                it.first,
                                itemFiltrable.item.carritoDeCreditos,
                                itemFiltrable.item.grupoDeClientes?.id,
                                it.second
                                              )
                    }
                    .map {
                        val idGrupoClientesPersona = it.idGrupoClientesPersona

                        val preciosCompletos =
                                it.proveedorDePreciosCompletosYProhibiciones
                                    .darPreciosCompletosDeFondos(
                                            it.producto.idsFondosAsociados,
                                            idUbicacion,
                                            idGrupoClientesPersona,
                                            it.producto.idPaquete
                                                                )

                        it.copy(producto = it.producto.actualizarPreciosAsociados(preciosCompletos))
                    }
                    .subscribe {
                        val estaAgregando = it.estaSeleccionado
                        val carritoDeCreditos = it.carritoDeCreditos
                        if (estaAgregando)
                        {
                            carritoDeCreditos.agregarAlCarrito(it.producto)
                        }
                        else
                        {
                            carritoDeCreditos.cancelarCreditosAgregados()
                        }
                    }
                    .also { disposablesEnUsoDeListaFiltrable.add(it) }
            }
        }

        eventosDeAgregandoProducto.withLatestFrom(carritos).forEach {
            if (it.first === EventoAgregar.Confirmar)
            {
                it.second.forEach { it.confirmarCreditosAgregados() }
            }
            else if (it.first === EventoAgregar.Cancelar)
            {
                it.second.forEach { it.cancelarCreditosAgregados() }
            }
        }.addTo(disposables)

        eventosDePagar.withLatestFrom(carritos, puedePagar).filter { it.third }.forEach {
            it.second.forEach { it.pagar() }
        }.addTo(disposables)
    }


    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    override fun agregarProducto(producto: ProductoUI)
    {
        eventosDeAgregandoProducto.onNext(EventoAgregar.Agregando(producto))
    }

    override fun confirmarCreditosAgregados()
    {
        eventosDeAgregandoProducto.onNext(EventoAgregar.Confirmar)
    }

    override fun cancelarCreditosAgregados()
    {
        eventosDeAgregandoProducto.onNext(EventoAgregar.Cancelar)
    }

    override fun pagar()
    {
        eventosDePagar.onNext(Unit)
    }

    override fun actualizarItems(nuevasPersonasConCarritos: ListaFiltrableUI<PersonaConCarrito>)
    {
        eventosDeCambioDeItems.onNext(nuevasPersonasConCarritos)
    }


    private fun List<Observable<Decimal>>.reducirASumaObservable(): Observable<Decimal>
    {
        return if (isEmpty())
        {
            Observable.just(Decimal.CERO)
        }
        else
        {
            Observable.combineLatest(this) { emisiones: Array<out Any> ->
                emisiones.asSequence().map { it as Decimal }.sumar()
            }
        }
    }

    private fun List<Observable<Boolean>>.reducirAExistenciaObservable(): Observable<Boolean>
    {
        return if (isEmpty())
        {
            Observable.just(false)
        }
        else
        {
            Observable.combineLatest(this) { emisiones: Array<out Any> ->
                emisiones.asSequence().map { it as Boolean }.fold(false) { acumulado, siguiente -> acumulado || siguiente }
            }
        }
    }

    private fun <T> List<Observable<T>>.reducirAForAllObservable(
            valorParaVacio: Boolean,
            mapABoolean: (T) -> Boolean
                                                                ): Observable<Boolean>
    {
        return if (isEmpty())
        {
            Observable.just(valorParaVacio)
        }
        else
        {
            val comoObservables = map { it.flatMap { Observable.just(mapABoolean(it)) } }

            Observable.combineLatest(comoObservables) { emisiones: Array<out Any> ->
                emisiones.asSequence().map { it as Boolean }.fold(true) { acumulado, siguiente -> acumulado && siguiente }
            }
        }
    }

    private data class EmisionProductoAgregado(
            val eventoAgregar: EventoAgregar,
            val idsTotalesDeFondos: Set<Long>,
            val idsDiferentesDeFondos: Set<Long>,
            val idsDiferentesDePaquetes: Set<Long>,
            val proveedorDePreciosCompletosYProhibiciones: ProveedorDePreciosCompletosYProhibiciones,
            val calculadorPuedeAgregarSegunUnicidad: CalculadorPuedeAgregarseSegunUnicidad
                                              )

    private data class EventoItemSeleccionado(
            val producto: ProductoUI,
            val estaSeleccionado: Boolean,
            val carritoDeCreditos: CarritoDeCreditosUI,
            val idGrupoClientesPersona: Long?,
            val proveedorDePreciosCompletosYProhibiciones: ProveedorDePreciosCompletosYProhibiciones
                                             )

    private sealed class EventoAgregar
    {
        object Confirmar : EventoAgregar()
        object Cancelar : EventoAgregar()
        data class Agregando(val producto: ProductoUI) : EventoAgregar()
    }
}