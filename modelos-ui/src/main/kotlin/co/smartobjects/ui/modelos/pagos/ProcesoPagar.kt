package co.smartobjects.ui.modelos.pagos

import co.smartobjects.entidades.operativas.compras.*
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.logica.fondos.ProveedorNombresYPreciosPorDefectoCompletosFondos
import co.smartobjects.logica.fondos.libros.MapeadorReglasANombresRestricciones
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.operativas.compras.ComprasAPI
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProcesoSeleccionCreditosUI
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.Opcional
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import co.smartobjects.utilidades.sumar
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.threeten.bp.ZonedDateTime

interface ProcesoPagarUI : ModeloUI
{
    val totalAPagarSegunPersonas: TotalAPagarSegunPersonasUI
    val pagosDeUnaCompra: PagosDeUnaCompraUI
    val puedePagar: Observable<Boolean>
    val estado: Observable<Estado>
    val mensajesDeError: Observable<String>
    val creditosACodificar: Single<List<CreditosACodificarPorPersona>>
    val resumenDeCreditosAPagar: Single<List<ResumenPagoProducto>>

    fun pagar()

    enum class Estado
    {
        SIN_CREAR_COMPRA, CREANDO_COMPRA, COMPRA_CREADA, CONFIRMANDO_COMPRA, COMPRA_CONFIRMADA, PROCESO_COMPLETADO
    }

    data class CreditosACodificarPorPersona(
            val personaConGrupoCliente: PersonaConGrupoCliente,
            val creditosFondoPagados: List<CreditoFondoConNombre>,
            val creditosPaquetePagados: List<CreditoPaqueteConNombre>
                                           )
    {
        val creditosFondoTotales: List<CreditoFondo> =
                (
                        creditosFondoPagados.asSequence().map { it.creditoAsociado } +
                        creditosPaquetePagados.asSequence().flatMap { it.creditoAsociado.creditosFondos.asSequence() }
                ).toList()
    }

    data class ResumenPagoProducto(
            val cantidad: Decimal,
            val nombre: String,
            val nombresRestriccionesUsadasParaPrecio: List<String>
                                  )
}

class ProcesoPagar internal constructor
(
        private val contextoDeSesion: ContextoDeSesion,
        override val totalAPagarSegunPersonas: TotalAPagarSegunPersonasUI,
        override val pagosDeUnaCompra: PagosDeUnaCompraUI,
        private val apiCompras: ComprasAPI,
        mapeadorReglasANombresRestricciones: Single<MapeadorReglasANombresRestricciones>,
        proveedorNombresYPreciosPorDefectoCompletosFondos: Single<ProveedorNombresYPreciosPorDefectoCompletosFondos>,
        private val schedulerBackground: Scheduler = Schedulers.io()
) : ProcesoPagarUI
{
    override val modelosHijos = listOf(totalAPagarSegunPersonas, pagosDeUnaCompra)

    private val eventosEstado = BehaviorSubject.createDefault<ProcesoPagarUI.Estado>(ProcesoPagarUI.Estado.SIN_CREAR_COMPRA)
    override val estado: Observable<ProcesoPagarUI.Estado> = eventosEstado

    private val eventosDeMensajesError = PublishSubject.create<String>()
    override val mensajesDeError: Observable<String> = eventosDeMensajesError

    private val elMontoPagadoEsMayorOIgualAlGranTotal =
            pagosDeUnaCompra
                .totalPagado.map { it >= totalAPagarSegunPersonas.granTotal }
                .distinctUntilChanged()

    private val eventosEstaPagando = BehaviorSubject.createDefault(false)
    override val puedePagar: Observable<Boolean> =
            Observables
                .combineLatest(elMontoPagadoEsMayorOIgualAlGranTotal, eventosEstaPagando)
                .map {
                    it.first && !it.second
                }

    private val eventosPago = PublishSubject.create<EventoPago>()
    private val eventosCompraCreada = BehaviorSubject.createDefault<Opcional<Compra>>(Opcional.Vacio())

    override val creditosACodificar: Single<List<ProcesoPagarUI.CreditosACodificarPorPersona>> =
            estado
                .filter { it === ProcesoPagarUI.Estado.PROCESO_COMPLETADO }
                .withLatestFrom(eventosCompraCreada.filter { !it.esVacio }.map { it.valor })
                .map {
                    val compra = it.second
                    val creditosFondoDeCompra = compra.creditosFondos.toMutableList()
                    val creditosPaqueteDeCompra = compra.creditosPaquetes.toMutableList()

                    totalAPagarSegunPersonas.listadoDePersonasConCreditos.items.map { creditosAProcesar ->
                        val idPersona = creditosAProcesar.personaConGrupoCliente.persona.id!!
                        val idFondoVsNombre =
                                creditosAProcesar.creditosFondoAPagar
                                    .associateBy({ it.creditoAsociado.idFondoComprado }, { it.nombreDeFondo })

                        val creditosFondoPagadosEnCompraDePersona = creditosAProcesar.creditosFondoPagados.toMutableList()

                        with(creditosFondoDeCompra.listIterator())
                        {
                            while (hasNext())
                            {
                                val creditoFondoDeCompra = next()
                                if (creditoFondoDeCompra.idPersonaDueña == idPersona)
                                {
                                    val nombre = idFondoVsNombre[creditoFondoDeCompra.idFondoComprado]!!
                                    creditosFondoPagadosEnCompraDePersona.add(CreditoFondoConNombre(nombre, creditoFondoDeCompra))
                                    remove()
                                }
                            }
                        }


                        val creditosPaquetePagadosEnCompraDePersona = creditosAProcesar.creditosPaquetePagados.toMutableList()

                        val idPaqueteVsCreditoPaqueteConNombre = creditosAProcesar.creditosPaqueteAPagar.associateBy { it.creditoAsociado.idPaquete }

                        with(creditosPaqueteDeCompra.listIterator())
                        {
                            while (hasNext())
                            {
                                val creditoPaqueteDeCompra = next()
                                if (creditoPaqueteDeCompra.creditosFondos.any { it.idPersonaDueña == idPersona })
                                {
                                    val creditoPaqueteConNombre = idPaqueteVsCreditoPaqueteConNombre[creditoPaqueteDeCompra.idPaquete]!!
                                    creditosPaquetePagadosEnCompraDePersona.add(creditoPaqueteConNombre.copy(creditoAsociado = creditoPaqueteDeCompra))
                                    remove()
                                }
                            }
                        }

                        ProcesoPagarUI.CreditosACodificarPorPersona(
                                creditosAProcesar.personaConGrupoCliente,
                                creditosFondoPagadosEnCompraDePersona,
                                creditosPaquetePagadosEnCompraDePersona
                                                                   )
                    }
                }
                .firstOrError()

    override val resumenDeCreditosAPagar: Single<List<ProcesoPagarUI.ResumenPagoProducto>> =
            Singles.zip(mapeadorReglasANombresRestricciones, proveedorNombresYPreciosPorDefectoCompletosFondos)
                .map { (mapeador, proveedor) ->
                    totalAPagarSegunPersonas.listadoDePersonasConCreditos
                        .items
                        .asSequence()
                        .flatMap {
                            val resumenDePaquetes =
                                    it.creditosPaqueteAPagar
                                        .asSequence()
                                        .flatMap { it.creditoAsociado.creditosFondos.asSequence() }
                                        .map {
                                            val nombresReglasAplicadas =
                                                    mapeador.mapear(
                                                            it.idFondoComprado,
                                                            it.idUbicacionCompra,
                                                            it.idGrupoClientesPersona,
                                                            null
                                                                   )

                                            val nombreDelFondo = proveedor.darNombreFondoSegunId(it.idFondoComprado)!!

                                            // La cantidad del crédito fondo ya tiene en cuenta la cantidad del paquete
                                            ProductoCompradoAResumir(nombreDelFondo, it.cantidad, nombresReglasAplicadas)
                                        }

                            val resumenDeFondos =
                                    it.creditosFondoAPagar
                                        .asSequence()
                                        .map {
                                            val nombresReglasAplicadas =
                                                    mapeador.mapear(
                                                            it.creditoAsociado.idFondoComprado,
                                                            it.creditoAsociado.idUbicacionCompra,
                                                            it.creditoAsociado.idGrupoClientesPersona,
                                                            null
                                                                   )

                                            ProductoCompradoAResumir(it.nombreDeFondo, it.creditoAsociado.cantidad, nombresReglasAplicadas)
                                        }

                            resumenDePaquetes + resumenDeFondos
                        }
                        .groupBy({ Pair(it.nombreProducto, it.nombresReglasAplicadas) }, { it.cantidad })
                        .asSequence()
                        .map {
                            val agregacionCantidad = it.value.sumar()

                            ProcesoPagarUI.ResumenPagoProducto(agregacionCantidad, it.key.first, it.key.second)
                        }
                        .toList()
                }

    override val observadoresInternos =
            listOf(eventosPago, eventosEstaPagando, eventosCompraCreada, eventosEstado, eventosDeMensajesError)
    private val disposables = CompositeDisposable()

    private constructor(
            contextoDeSesion: ContextoDeSesion,
            totalAPagarSegunPersonas: TotalAPagarSegunPersonasUI,
            apiCompras: ComprasAPI,
            mapeadorReglasANombresRestricciones: Single<MapeadorReglasANombresRestricciones>,
            proveedorNombresYPreciosPorDefectoCompletosFondos: Single<ProveedorNombresYPreciosPorDefectoCompletosFondos>
                       )
            : this(
            contextoDeSesion,
            totalAPagarSegunPersonas,
            PagosDeUnaCompra(totalAPagarSegunPersonas.total),
            apiCompras,
            mapeadorReglasANombresRestricciones,
            proveedorNombresYPreciosPorDefectoCompletosFondos
                  )

    constructor(
            contextoDeSesion: ContextoDeSesion,
            apiCompras: ComprasAPI,
            creditosPorPersonaAProcesar: List<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>,
            mapeadorReglasANombresRestricciones: Single<MapeadorReglasANombresRestricciones>,
            proveedorNombresYPreciosPorDefectoCompletosFondos: Single<ProveedorNombresYPreciosPorDefectoCompletosFondos>
               )
            : this(
            contextoDeSesion,
            TotalAPagarSegunPersonas(creditosPorPersonaAProcesar),
            apiCompras,
            mapeadorReglasANombresRestricciones,
            proveedorNombresYPreciosPorDefectoCompletosFondos
                  )


    init
    {
        eventosPago.withLatestFrom(puedePagar, estado)
            .filter {
                it.second
            }
            .doOnNext {
                eventosDeMensajesError.onNext("")
                eventosEstaPagando.onNext(true)
            }
            .switchMap {
                when (it.third)
                {
                    ProcesoPagarUI.Estado.SIN_CREAR_COMPRA ->
                    {
                        val eventoPago = it.first
                        val compraACrear = Compra(
                                contextoDeSesion.idCliente,
                                contextoDeSesion.nombreDeUsuario,
                                eventoPago.creditos.asSequence().flatMap { it.creditosFondoAPagar.asSequence().map { it.creditoAsociado } }.toList(),
                                eventoPago.creditos.asSequence().flatMap { it.creditosPaqueteAPagar.asSequence().map { it.creditoAsociado } }.toList(),
                                eventoPago.pagos,
                                ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                                                 )

                        eventosEstado.onNext(ProcesoPagarUI.Estado.CREANDO_COMPRA)
                        crearPeticionCreacionCompraYActualizarEstado(compraACrear)
                    }
                    ProcesoPagarUI.Estado.COMPRA_CREADA    ->
                    {
                        eventosCompraCreada
                    }
                    else                                   ->
                    {
                        throw IllegalStateException()
                    }
                }
            }
            .map {
                if (it.esVacio)
                {
                    Opcional.Vacio()
                }
                else
                {
                    eventosEstado.onNext(ProcesoPagarUI.Estado.CONFIRMANDO_COMPRA)
                    val pudoConfirmarCompra = crearPeticionConfirmacionCompra(it.valor)

                    if (pudoConfirmarCompra)
                    {
                        Opcional.De(Unit)
                    }
                    else
                    {
                        Opcional.Vacio()
                    }
                }
            }
            .doAfterNext {
                eventosEstaPagando.onNext(false)
            }
            .subscribeOn(schedulerBackground)
            .subscribe {
                if (!it.esVacio)
                {
                    eventosEstado.onNext(ProcesoPagarUI.Estado.PROCESO_COMPLETADO)
                }
            }.addTo(disposables)
    }

    private fun crearPeticionCreacionCompraYActualizarEstado(compraACrear: Compra): Observable<Opcional<Compra>>
    {
        return Observable
            .just(compraACrear)
            .map {
                apiCompras.actualizar(it.id, it)
            }
            .flatMap {
                when (it)
                {
                    is RespuestaIndividual.Exitosa       ->
                    {
                        eventosEstado.onNext(ProcesoPagarUI.Estado.COMPRA_CREADA)
                        val resultado = Opcional.De(it.respuesta)

                        eventosCompraCreada.onNext(resultado)

                        Observable.just<Opcional<Compra>>(resultado)
                    }
                    is RespuestaIndividual.Vacia         ->
                    {
                        throw IllegalStateException()
                    }
                    is RespuestaIndividual.Error.Timeout ->
                    {
                        eventosDeMensajesError.onNext("Timeout contactando el backend")
                        eventosEstado.onNext(ProcesoPagarUI.Estado.SIN_CREAR_COMPRA)

                        Observable.just(Opcional.Vacio())
                    }
                    is RespuestaIndividual.Error.Red     ->
                    {
                        eventosDeMensajesError.onNext("Error contactando el backend")
                        eventosEstado.onNext(ProcesoPagarUI.Estado.SIN_CREAR_COMPRA)

                        Observable.just(Opcional.Vacio())
                    }
                    is RespuestaIndividual.Error.Back    ->
                    {
                        when (it.error.codigoInterno)
                        {
                            CompraDTO.CodigosError.ENTIDAD_DUPLICADA_EN_BD                       ->
                            {
                                eventosDeMensajesError.onNext("La compra ya fue confirmada")
                                eventosEstado.onNext(ProcesoPagarUI.Estado.COMPRA_CONFIRMADA)
                            }
                            CompraDTO.CodigosError.PAGOS_CON_NUMERO_DE_TRANSACCION_POS_REPETIDOS ->
                            {
                                eventosDeMensajesError.onNext("La compra contiene pagos con números de transacción usados anteriormente")
                                eventosEstado.onNext(ProcesoPagarUI.Estado.SIN_CREAR_COMPRA)
                            }
                            CompraDTO.CodigosError.ENTIDAD_REFERENCIADA_NO_EXISTE                ->
                            {
                                eventosDeMensajesError.onNext("Algún dato prerequisito para la compra no existe")
                                eventosEstado.onNext(ProcesoPagarUI.Estado.SIN_CREAR_COMPRA)
                            }
                            else                                                                 ->
                            {
                                eventosDeMensajesError.onNext("Error en petición: ${it.error.mensaje}")
                                eventosEstado.onNext(ProcesoPagarUI.Estado.SIN_CREAR_COMPRA)
                            }
                        }

                        Observable.just(Opcional.Vacio())
                    }
                }
            }
            .subscribeOn(schedulerBackground)
    }

    private fun crearPeticionConfirmacionCompra(compraCreada: Compra): Boolean
    {
        return when (val respuesta = apiCompras.actualizarCampos(compraCreada.id, TransaccionEntidadTerminadaDTO(true)))
        {
            is RespuestaVacia.Exitosa       ->
            {
                eventosEstado.onNext(ProcesoPagarUI.Estado.COMPRA_CONFIRMADA)

                true
            }
            is RespuestaVacia.Error.Timeout ->
            {
                eventosDeMensajesError.onNext("Timeout contactando el backend")
                eventosEstado.onNext(ProcesoPagarUI.Estado.COMPRA_CREADA)

                false
            }
            is RespuestaVacia.Error.Red     ->
            {
                eventosDeMensajesError.onNext("Error contactando el backend")
                eventosEstado.onNext(ProcesoPagarUI.Estado.COMPRA_CREADA)

                false
            }
            is RespuestaVacia.Error.Back    ->
            {
                when (respuesta.error.codigoInterno)
                {
                    CompraDTO.CodigosError.NO_EXISTE ->
                    {
                        eventosDeMensajesError.onNext("La compra a confirmar no existe")
                    }
                    else                             ->
                    {
                        eventosDeMensajesError.onNext("Error en petición: ${respuesta.error.mensaje}")
                    }
                }

                eventosEstado.onNext(ProcesoPagarUI.Estado.COMPRA_CREADA)

                false
            }
        }
    }

    override fun pagar()
    {
        eventosPago.onNext(EventoPago(totalAPagarSegunPersonas.listadoDePersonasConCreditos.items, pagosDeUnaCompra.pagoActuales))
    }

    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    class EventoPago(
            val creditos: List<ProcesoSeleccionCreditosUI.CreditosPorPersonaAProcesar>,
            val pagos: List<Pago>
                    )

    class ProductoCompradoAResumir(
            val nombreProducto: String,
            val cantidad: Decimal,
            val nombresReglasAplicadas: List<String>
                                  )
}