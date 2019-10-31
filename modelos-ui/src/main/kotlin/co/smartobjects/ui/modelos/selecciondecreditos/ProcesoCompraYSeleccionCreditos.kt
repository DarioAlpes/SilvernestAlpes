package co.smartobjects.ui.modelos.selecciondecreditos

import co.smartobjects.entidades.operativas.compras.CreditoFondoConNombre
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.entidades.operativas.compras.CreditosDeUnaPersona
import co.smartobjects.entidades.personas.PersonaConGrupoCliente
import co.smartobjects.logica.fondos.*
import co.smartobjects.logica.fondos.libros.ProveedorDePreciosCompletosYProhibiciones
import co.smartobjects.logica.fondos.libros.ProveedorDePreciosCompletosYProhibicionesEnMemoria
import co.smartobjects.persistencia.fondos.RepositorioFondos
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosSegunReglasCompleto
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.red.clientes.ProblemaBackend
import co.smartobjects.red.clientes.ProblemaRed
import co.smartobjects.red.clientes.Timeout
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.personas.creditos.CreditosDeUnaPersonaAPI
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.ListaFiltrableUIConSujetos
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditos
import co.smartobjects.ui.modelos.catalogo.Catalogo
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondos
import co.smartobjects.ui.modelos.menufiltrado.ProveedorIconosCategoriasFiltrado
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.AgrupacionPersonasCarritosDeCreditos
import co.smartobjects.ui.modelos.selecciondecreditos.agrupacioncarritosdecreditos.PersonaConCarrito
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.Opcional
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.threeten.bp.LocalDate


interface ProcesoCompraYSeleccionCreditosUI<TipoIconoFiltrado : ProveedorIconosCategoriasFiltrado.Icono>
    : ModeloUI
{
    val procesoSeleccionCreditos: ProcesoSeleccionCreditosUI<TipoIconoFiltrado>

    val estadoConsulta: Observable<EstadoConsulta>
    val mensajeErrorConsulta: Observable<Opcional<String>>

    fun consultarComprasEnFecha(fecha: LocalDate)

    enum class EstadoConsulta
    {
        ESPERANDO, CONSULTANDO
    }

    interface IRepositorios
    {
        val impuestos: RepositorioImpuestos
        val categoriasSku: RepositorioCategoriasSkus
        val paquetes: RepositorioPaquetes
        val fondos: RepositorioFondos
        val librosSegunReglasCompleto: RepositorioLibrosSegunReglasCompleto
    }
}

class ProcesoCompraYSeleccionCreditos<TipoIconoFiltrado : ProveedorIconosCategoriasFiltrado.Icono>
internal constructor
(
        contextoDeSesion: ContextoDeSesion,
        private val repositorios: ProcesoCompraYSeleccionCreditosUI.IRepositorios,
        private val apiCreditosDeUnaPersona: CreditosDeUnaPersonaAPI,
        proveedorImagenesProductos: ProveedorImagenesProductos,
        proveedorIconosCategoriasFiltrado: ProveedorIconosCategoriasFiltrado<TipoIconoFiltrado>,
        private val personas: List<PersonaConGrupoCliente>,
        _procesoSeleccionCreditos: ProcesoSeleccionCreditosUI<TipoIconoFiltrado>?,
        schedulerBackground: Scheduler = Schedulers.io()
) : ProcesoCompraYSeleccionCreditosUI<TipoIconoFiltrado>
{
    constructor
            (
                    contextoDeSesion: ContextoDeSesion,
                    repositorios: ProcesoCompraYSeleccionCreditosUI.IRepositorios,
                    apiCreditosDeUnaPersona: CreditosDeUnaPersonaAPI,
                    proveedorImagenesProductos: ProveedorImagenesProductos,
                    proveedorIconosCategoriasFiltrado: ProveedorIconosCategoriasFiltrado<TipoIconoFiltrado>,
                    personas: List<PersonaConGrupoCliente>,
                    schedulerBackground: Scheduler = Schedulers.io()
            )
            : this(
            contextoDeSesion,
            repositorios,
            apiCreditosDeUnaPersona,
            proveedorImagenesProductos,
            proveedorIconosCategoriasFiltrado,
            personas,
            null,
            schedulerBackground
                  )

    private val impuestos =
            Single
                .fromCallable { repositorios.impuestos.listar(contextoDeSesion.idCliente) }
                .cache()
                .subscribeOn(schedulerBackground)

    private val categoriasSku =
            Single
                .fromCallable { repositorios.categoriasSku.listar(contextoDeSesion.idCliente) }
                .cache()
                .subscribeOn(schedulerBackground)

    private val fondos =
            Single
                .fromCallable { repositorios.fondos.listar(contextoDeSesion.idCliente) }
                .cache()
                .subscribeOn(schedulerBackground)

    private val paquetes =
            Single
                .fromCallable { repositorios.paquetes.listar(contextoDeSesion.idCliente) }
                .cache()
                .subscribeOn(schedulerBackground)


    private val proveedorDePreciosCompletosYProhibiciones =
            Singles
                .zip(
                        Single
                            .fromCallable { repositorios.librosSegunReglasCompleto.listar(contextoDeSesion.idCliente) }
                            .cache()
                            .subscribeOn(schedulerBackground),
                        fondos,
                        impuestos
                    )
                .map<ProveedorDePreciosCompletosYProhibiciones> {
                    ProveedorDePreciosCompletosYProhibicionesEnMemoria(it.first, it.second, it.third)
                }
                .subscribeOn(schedulerBackground)

    private val proveedorNombresYPreciosCompletosFondos =
            Singles
                .zip(fondos, impuestos)
                .map<ProveedorNombresYPreciosPorDefectoCompletosFondos> {
                    ProveedorNombresYPreciosPorDefectoPorDefectoCompletosEnMemoria(it.first, it.second)
                }
                .subscribeOn(schedulerBackground)

    private val proveedorCategoriasPadres =
            categoriasSku
                .map<ProveedorCategoriasPadres> {
                    ProveedorCategoriasPadresEnMemoria(it)
                }
                .subscribeOn(schedulerBackground)

    private val calculadorPuedeAgregarseSegunUnicidad =
            fondos
                .map<CalculadorPuedeAgregarseSegunUnicidad> {
                    CalculadorPuedeAgregarseSegunUnicidadEnMemoria(it)
                }
                .subscribeOn(schedulerBackground)

    private val idPaqueteVsNombre = paquetes.map { it.associateBy({ it.id!! }, { it.nombre }) }.cache()


    override val procesoSeleccionCreditos: ProcesoSeleccionCreditosUI<TipoIconoFiltrado> =
            _procesoSeleccionCreditos
            ?: ProcesoSeleccionCreditos(
                    contextoDeSesion,
                    MenuFiltradoFondos(
                            proveedorIconosCategoriasFiltrado,
                            categoriasSku.map { it.toList() }
                                      ),
                    Catalogo(
                            paquetes.map { it.asSequence().filter { it.disponibleParaLaVenta }.toList() },
                            fondos.map { it.asSequence().filter { it.disponibleParaLaVenta }.toList() },
                            proveedorCategoriasPadres,
                            proveedorNombresYPreciosCompletosFondos,
                            proveedorImagenesProductos
                            ),
                    AgrupacionPersonasCarritosDeCreditos(
                            ListaFiltrableUIConSujetos(listOf()),
                            contextoDeSesion.idUbicacion,
                            proveedorDePreciosCompletosYProhibiciones,
                            calculadorPuedeAgregarseSegunUnicidad
                                                        )
                                       )

    private val eventosConsultaPorFecha = BehaviorSubject.create<LocalDate>()

    private val eventosEstadoConsulta = BehaviorSubject.createDefault(ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO)
    override val estadoConsulta = eventosEstadoConsulta.hide().distinctUntilChanged()!!

    private val eventosMensajeErrorConsulta = BehaviorSubject.createDefault(Opcional.Vacio<String>())
    override val mensajeErrorConsulta = eventosMensajeErrorConsulta.hide().distinctUntilChanged()!!

    override val observadoresInternos: List<Observer<*>> = listOf(eventosConsultaPorFecha, eventosEstadoConsulta, eventosMensajeErrorConsulta)
    override val modelosHijos: List<ModeloUI> = listOf(procesoSeleccionCreditos)

    private val disposables = CompositeDisposable()

    init
    {
        if (personas.isEmpty())
        {
            throw Exception("Las personas no pueden estar vacías")
        }

        eventosConsultaPorFecha
            .withLatestFrom(estadoConsulta)
            .filter {
                it.second === ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO
            }
            .doOnNext {
                eventosEstadoConsulta.onNext(ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.CONSULTANDO)
                eventosMensajeErrorConsulta.onNext(Opcional.Vacio())
            }
            .map { it.first }
            .flatMap {
                crearLlamadosABackend(it, schedulerBackground)
                    .onErrorResumeNext { e: Throwable ->
                        if (e is IllegalStateException)
                        {
                            Observable.error(e)
                        }
                        else
                        {
                            Observable.just(ListaFiltrableUIConSujetos(listOf()))
                        }
                    }
            }
            .doAfterNext {
                eventosEstadoConsulta.onNext(ProcesoCompraYSeleccionCreditosUI.EstadoConsulta.ESPERANDO)
            }
            .subscribeOn(schedulerBackground)
            .subscribe {
                procesoSeleccionCreditos.agrupacionCarritoDeCreditos.actualizarItems(it)
            }.addTo(disposables)
    }

    private fun crearLlamadosABackend(fechaDeCorte: LocalDate, schedulerBackground: Scheduler)
            : Observable<ListaFiltrableUIConSujetos<PersonaConCarrito>>
    {
        val llamadosACompras =
                personas.map { personaConGrupoCliente ->
                    val parametrosLlamado =
                            CreditosDeUnaPersonaAPI.ParametrosBuscarRecursoCreditosDeUnaPersona(
                                    personaConGrupoCliente.persona.id!!,
                                    fechaDeCorte.atStartOfDay(ZONA_HORARIA_POR_DEFECTO)
                                                                                               )
                    val llamado = Single.fromCallable {
                        apiCreditosDeUnaPersona.consultar(parametrosLlamado)
                    }.subscribeOn(schedulerBackground)

                    Singles
                        .zip(llamado, proveedorNombresYPreciosCompletosFondos, idPaqueteVsNombre, calculadorPuedeAgregarseSegunUnicidad)
                        { respuestaCompras, proveedorDeNombresDeFondos, proveedorDeNombresDePaquetes, calculadorUnicidad ->

                            parsearRespuestaRed(
                                    respuestaCompras,
                                    proveedorDeNombresDeFondos,
                                    calculadorUnicidad,
                                    proveedorDeNombresDePaquetes,
                                    personaConGrupoCliente,
                                    fechaDeCorte
                                               )
                        }
                }

        return Single.zip(llamadosACompras) { ListaFiltrableUIConSujetos(it.map { it as PersonaConCarrito }) }.toObservable()
    }


    private fun parsearRespuestaRed(
            respuestaCreditosDeUnaPersona: RespuestaIndividual<CreditosDeUnaPersona>,
            proveedorDeNombresDeFondos: ProveedorNombresYPreciosPorDefectoCompletosFondos,
            calculadorPuedeAgregarseSegunUnicidad: CalculadorPuedeAgregarseSegunUnicidad,
            proveedorDeNombresDePaquetes: Map<Long, String>,
            personaConGrupoCliente: PersonaConGrupoCliente,
            fechaDeCorte: LocalDate
                                   ): PersonaConCarrito
    {
        return when (respuestaCreditosDeUnaPersona)
        {
            is RespuestaIndividual.Exitosa       ->
            {
                crearPersonasConCarritos(
                        respuestaCreditosDeUnaPersona.respuesta,
                        proveedorDeNombresDeFondos,
                        calculadorPuedeAgregarseSegunUnicidad,
                        proveedorDeNombresDePaquetes,
                        personaConGrupoCliente
                                        )
            }
            is RespuestaIndividual.Vacia         ->
            {
                throw IllegalStateException()
            }
            is RespuestaIndividual.Error.Timeout ->
            {
                val mensajeError =
                        "Tiempo de espera al servidor agotado. No se pudieron consultar compras de la persona con documento ${personaConGrupoCliente.persona.documentoCompleto}"

                eventosMensajeErrorConsulta.onNext(Opcional.De(mensajeError))

                throw Timeout("La consulta de compras para la persona[id = ${personaConGrupoCliente.persona.id}] en la fecha[$fechaDeCorte] hizo timeout")
            }
            is RespuestaIndividual.Error.Red     ->
            {
                val mensajeError = "Hubo un error en la conexión y no fue posible contactar al servidor"

                eventosMensajeErrorConsulta.onNext(Opcional.De(mensajeError))

                throw ProblemaRed("Se produjo un error de red: ${respuestaCreditosDeUnaPersona.error.message}")
            }
            is RespuestaIndividual.Error.Back    ->
            {
                val mensajeError = "La petición realizada es errónea y no pudo ser procesada"

                eventosMensajeErrorConsulta.onNext(Opcional.De(mensajeError))

                throw ProblemaBackend("Se produjo un error de red: ${respuestaCreditosDeUnaPersona.error}")
            }
        }
    }

    private fun crearPersonasConCarritos(
            creditosDeUnaPersona: CreditosDeUnaPersona,
            proveedorDeNombresDeFondos: ProveedorNombresYPreciosPorDefectoCompletosFondos,
            calculadorPuedeAgregarseSegunUnicidad: CalculadorPuedeAgregarseSegunUnicidad,
            proveedorDeNombresDePaquetes: Map<Long, String>,
            personaConGrupoCliente: PersonaConGrupoCliente
                                        ): PersonaConCarrito
    {
        val creditosFondoPreIncluidos =
                creditosDeUnaPersona.creditosFondos.map {
                    CreditoFondoConNombre(
                            proveedorDeNombresDeFondos.darNombreFondoSegunId(it.idFondoComprado)!!,
                            it
                                         )
                }

        val creditosPaquetePreIncluidos =
                creditosDeUnaPersona.creditosPaquetes.map {
                    CreditoPaqueteConNombre(
                            proveedorDeNombresDePaquetes[it.idPaquete]!!,
                            1,
                            it
                                           )
                }


        return PersonaConCarrito(
                personaConGrupoCliente.persona,
                personaConGrupoCliente.posibleGrupoCliente,
                CarritoDeCreditos(Decimal.CERO, creditosFondoPreIncluidos, creditosPaquetePreIncluidos, calculadorPuedeAgregarseSegunUnicidad)
                                )
    }

    override fun consultarComprasEnFecha(fecha: LocalDate)
    {
        eventosConsultaPorFecha.onNext(fecha)
    }

    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    class Repositorios(
            override val impuestos: RepositorioImpuestos,
            override val categoriasSku: RepositorioCategoriasSkus,
            override val paquetes: RepositorioPaquetes,
            override val fondos: RepositorioFondos,
            override val librosSegunReglasCompleto: RepositorioLibrosSegunReglasCompleto
                      ) : ProcesoCompraYSeleccionCreditosUI.IRepositorios
}