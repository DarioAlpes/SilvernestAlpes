package co.smartobjects.ui.modelos.consumos

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.tagscodificables.TagConsumos
import co.smartobjects.entidades.ubicaciones.consumibles.Consumo
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumoConNombreConsumible
import co.smartobjects.logica.fondos.CalculadorPuedeAgregarseSegunUnicidadUnit
import co.smartobjects.logica.fondos.CalculadoraDeConsumos
import co.smartobjects.logica.fondos.ProveedorCodigosExternosFondos
import co.smartobjects.logica.fondos.ProveedorNombresYPreciosPorDefectoCompletosFondos
import co.smartobjects.logica.fondos.precios.CalculadorGrupoCliente
import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.nfc.ResultadoNFC
import co.smartobjects.nfc.operacionessobretags.OperacionesCompuestas
import co.smartobjects.nfc.operacionessobretags.ResultadoLecturaNFC
import co.smartobjects.nfc.utils.comprimir
import co.smartobjects.nfc.utils.descomprimir
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.operativas.ordenes.LoteDeOrdenesAPI
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.PersonaPorIdSesionManillaAPI
import co.smartobjects.red.clientes.operativas.reservas.sesionesdemanilla.SesionDeManillaAPI
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditos
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditosInmutableUI
import co.smartobjects.ui.modelos.carritocreditos.CarritoDeCreditosUI
import co.smartobjects.ui.modelos.carritocreditos.ItemCreditoUI
import co.smartobjects.ui.modelos.catalogo.ProductoUI
import co.smartobjects.utilidades.Decimal
import co.smartobjects.utilidades.Opcional
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import co.smartobjects.utilidades.formatearComoFechaHoraConMesCompleto
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.threeten.bp.ZonedDateTime

interface CodificacionDeConsumosUI : ModeloUI, CarritoDeCreditosInmutableUI
{
    val estado: Observable<Estado>
    val mensajesDeError: Observable<String>

    override val creditosTotales: Observable<List<ItemCreditoUI>>
    val totalSinImpuesto: Observable<Decimal>
    val impuestoTotal: Observable<Decimal>
    val granTotal: Observable<Decimal>

    val ultimosResultadoDeConsumos: Observable<Opcional<ResultadosDeConsumos>>

    fun agregarProducto(producto: ProductoUI)

    /**```
     * Notas:
     *
     * Comienza en [CON_CARRITO_VACIO].
     *
     * Pasa a [ESPERANDO_TAG] solo si tiene ítems en carrito.
     *
     * Si tenía items y luego se remueven para quedar en cero, pasa a [CON_CARRITO_VACIO]
     *
     * Mientras que está en [CODIFICANDO] no debería poder modificarse el carrito
     *
     * Cuando pase a [CODIFICADO] se debe borrar el carrito solo si [ultimosResultadoDeConsumos] fue exitoso
     */
    enum class Estado
    {
        CON_CARRITO_VACIO, ESPERANDO_TAG, CODIFICANDO
    }

    data class ResultadosDeConsumos(
            val desgloseConNombres: List<DesgloseDeConsumoConNombres>,
            val todosLosConsumosRealizadosPorCompleto: Boolean,
            val fechaYHoraDeRealizacion: ZonedDateTime
                                   )
    {
        init
        {
            if (desgloseConNombres.isEmpty())
            {
                throw EntidadConCampoVacio(CodificacionDeConsumosUI::ResultadosDeConsumos.name, ResultadosDeConsumos::desgloseConNombres.name)
            }
        }
    }

    data class DesgloseDeConsumoConNombres(
            val consumoConNombreConsumible: ConsumoConNombreConsumible,
            val consumosRealizados: List<ConsumoRealizadoConNombre>,
            val consumidoCompetamente: Boolean
                                          )

    data class ConsumoRealizadoConNombre(
            val nombreFondoConsumido: String,
            val cantidadInicial: Decimal,
            val cantidadConsumida: Decimal,
            val cantidadFinal: Decimal
                                        )
}

class CodificacionDeConsumos
(
        private val contextoDeSesion: ContextoDeSesion,
        proveedorNombresYPreciosPorDefectoCompletosFondos: Single<ProveedorNombresYPreciosPorDefectoCompletosFondos>,
        private val proveedorOperacionesNFC: ProveedorOperacionesNFC,
        proveedorCodigosExternosFondos: Single<ProveedorCodigosExternosFondos>,
        calculadorGrupoCliente: Single<CalculadorGrupoCliente>,
        calculadoraDeConsumos: Single<CalculadoraDeConsumos>,
        private val apis: Apis,
        schedulerBackground: Scheduler = Schedulers.io()
) : CodificacionDeConsumosUI
{
    private val eventosDeEstado = BehaviorSubject.createDefault<CodificacionDeConsumosUI.Estado>(CodificacionDeConsumosUI.Estado.CON_CARRITO_VACIO)
    override val estado: Observable<CodificacionDeConsumosUI.Estado> = eventosDeEstado.distinctUntilChanged()

    private val eventosDeMensajesError = PublishSubject.create<String>()
    override val mensajesDeError: Observable<String> = eventosDeMensajesError

    private val carritoDeCreditos: CarritoDeCreditosUI =
            CarritoDeCreditos(Decimal.CERO, listOf(), listOf(), CalculadorPuedeAgregarseSegunUnicidadUnit)

    override val creditosTotales: Observable<List<ItemCreditoUI>> = carritoDeCreditos.creditosTotales
    override val totalSinImpuesto: Observable<Decimal> = carritoDeCreditos.totalSinImpuesto
    override val impuestoTotal: Observable<Decimal> = carritoDeCreditos.impuestoTotal
    override val granTotal: Observable<Decimal> = carritoDeCreditos.granTotal

    private val eventosResultadosDeConsumos =
            BehaviorSubject.createDefault<Opcional<CodificacionDeConsumosUI.ResultadosDeConsumos>>(Opcional.Vacio())

    override val ultimosResultadoDeConsumos: Observable<Opcional<CodificacionDeConsumosUI.ResultadosDeConsumos>> = eventosResultadosDeConsumos

    private val eventosAgregarAlCarrito = PublishSubject.create<ProductoUI>()

    override val observadoresInternos: List<Observer<*>> =
            listOf(eventosDeEstado, eventosDeMensajesError, eventosResultadosDeConsumos, eventosAgregarAlCarrito)
    override val modelosHijos: List<ModeloUI> = listOf(carritoDeCreditos)

    private val disposables = CompositeDisposable()


    init
    {
        creditosTotales
            .map { if (it.isEmpty()) CodificacionDeConsumosUI.Estado.CON_CARRITO_VACIO else CodificacionDeConsumosUI.Estado.ESPERANDO_TAG }
            .distinctUntilChanged()
            .doOnNext {
                proveedorOperacionesNFC.permitirLecturaNFC = it !== CodificacionDeConsumosUI.Estado.CON_CARRITO_VACIO
            }
            .subscribeOn(schedulerBackground)
            .subscribe(eventosDeEstado)

        eventosAgregarAlCarrito
            .filter { !it.esPaquete }
            .withLatestFrom(eventosDeEstado)
            .filter {
                it.second === CodificacionDeConsumosUI.Estado.CON_CARRITO_VACIO
                || it.second === CodificacionDeConsumosUI.Estado.ESPERANDO_TAG
            }
            .subscribeOn(schedulerBackground)
            .subscribe {
                carritoDeCreditos.agregarAlCarrito(it.first)
                carritoDeCreditos.confirmarCreditosAgregados()
                it.first.terminarAgregar()
            }
            .addTo(disposables)

        val proveedores =
                Singles
                    .zip(
                            proveedorNombresYPreciosPorDefectoCompletosFondos,
                            proveedorCodigosExternosFondos,
                            calculadorGrupoCliente,
                            calculadoraDeConsumos
                        )
                    { a, b, c, d -> Proveedores(a, b, c, d) }
                    .cache()
                    .subscribeOn(schedulerBackground)

        proveedorOperacionesNFC.resultadosNFCLeidos
            .doOnNext {
                if (it is ResultadoNFC.Error)
                {
                    val mensaje = when (it)
                    {
                        is ResultadoNFC.Error.TagNoSoportado ->
                        {
                            "El tag '${it.nombreTag}' no se encuentra soportado en el momento"
                        }
                        ResultadoNFC.Error.ConectandoseAlTag ->
                        {
                            "No fue posible conectarse con el tag"
                        }
                    }

                    eventosDeMensajesError.onNext(mensaje)
                }
            }
            .filter { it is ResultadoNFC.Exitoso }
            .toObservable()
            .cast<ResultadoNFC.Exitoso>()
            .withLatestFrom(estado)
            .filter { it.second === CodificacionDeConsumosUI.Estado.ESPERANDO_TAG }
            .map { it.first }
            .withLatestFrom(creditosTotales)
            .map {
                val consumos = it.second.flatMap {
                    val creditoFondo = it.aCreditoFondo(0, "no importa", "no importa", 1, "no importa", 0, null)
                    listOf(Consumo(contextoDeSesion.idUbicacion, creditoFondo!!.idFondoComprado, creditoFondo.cantidad))
                }

                Pair(it.first, consumos)
            }
            .withLatestFrom(proveedores.toObservable())
            .subscribeOn(schedulerBackground)
            .subscribe {
                consumir(it.first.first, it.first.second, it.second)
            }
    }

    override fun agregarProducto(producto: ProductoUI)
    {
        eventosAgregarAlCarrito.onNext(producto)
    }

    private var cachePersona: Pair<Long, Persona>? = null
    private var cacheSesionDeManilla: Pair<Long, SesionDeManilla>? = null
    private fun consumir(resultadoNFC: ResultadoNFC.Exitoso, consumos: List<Consumo>, proveedores: Proveedores)
    {
        // Limpiar mensaje anterior
        eventosDeMensajesError.onNext("")

        eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.CODIFICANDO)

        when (val lecturaNFC = resultadoNFC.operacion.leerTag())
        {
            is ResultadoLecturaNFC.TagLeido              ->
            {
                val tagConsumosLeido = TagConsumos(descomprimir(lecturaNFC.valor))

                if (tagConsumosLeido.puedeConsumirEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)))
                {
                    cacheSesionDeManilla = consultarSesionDeManilla(tagConsumosLeido.idSesionDeManilla)

                    if (cacheSesionDeManilla != null)
                    {
                        if (cacheSesionDeManilla!!.second.fechaDesactivacion == null)
                        {
                            cachePersona = consultarPersonaAsociada(tagConsumosLeido.idSesionDeManilla)
                            if (cachePersona != null)
                            {
                                efectuarConsumos(tagConsumosLeido, consumos, resultadoNFC.operacion, proveedores)
                            }
                            else
                            {
                                eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
                            }
                        }
                        else
                        {
                            eventosDeMensajesError.onNext("La sesión se encuentra desactivada (el ${formatearComoFechaHoraConMesCompleto(cacheSesionDeManilla!!.second.fechaDesactivacion!!)})")
                            eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
                        }
                    }
                    else
                    {
                        eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
                    }
                }
                else
                {
                    eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
                    val fechaInicioFormateada =
                            tagConsumosLeido.fechaValidoDesdeMinima?.let {
                                " a partir de ${formatearComoFechaHoraConMesCompleto(it)}"
                            } ?: ""

                    val fechaFinFormateada =
                            tagConsumosLeido.fechaValidoHastaMaxima?.let {
                                " hasta ${formatearComoFechaHoraConMesCompleto(it)}"
                            } ?: ""

                    eventosDeMensajesError.onNext("La persona solo puede realizar consumos$fechaInicioFormateada$fechaFinFormateada")
                }
            }
            ResultadoLecturaNFC.TagVacio                 ->
            {
                eventosDeMensajesError.onNext("El tag leído está vacío")
                eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
            }
            ResultadoLecturaNFC.LlaveDesconocida         ->
            {
                eventosDeMensajesError.onNext("El tag está programado con una llave desconocida")
                eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
            }
            ResultadoLecturaNFC.SinAutenticacionActivada ->
            {
                eventosDeMensajesError.onNext("El tag no tiene autenticación activada")
                eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
            }
            is ResultadoLecturaNFC.ErrorDeLectura        ->
            {
                eventosDeMensajesError.onNext("Error al leer el tag")
                eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
            }
        }
    }

    private fun consultarSesionDeManilla(id: Long): Pair<Long, SesionDeManilla>?
    {
        return if (cacheSesionDeManilla == null || cacheSesionDeManilla!!.first != id)
        {
            when (val respuesta = apis.sesionDeManillaAPI.consultar(id))
            {
                is RespuestaIndividual.Exitosa       ->
                {
                    Pair(id, respuesta.respuesta)
                }
                is RespuestaIndividual.Vacia         ->
                {
                    eventosDeMensajesError.onNext("No se encontró a la sesión de la manilla")
                    null
                }
                is RespuestaIndividual.Error.Timeout ->
                {
                    eventosDeMensajesError.onNext("Timeout contactando el backend")
                    null
                }
                is RespuestaIndividual.Error.Red     ->
                {
                    eventosDeMensajesError.onNext("Error contactando el backend")
                    null
                }
                is RespuestaIndividual.Error.Back    ->
                {
                    eventosDeMensajesError.onNext("Error al consultar la sesión de la manilla: ${respuesta.error.mensaje}")
                    null
                }
            }
        }
        else
        {
            cacheSesionDeManilla
        }
    }

    private fun consultarPersonaAsociada(idSesionDeManilla: Long): Pair<Long, Persona>?
    {
        return if (cachePersona == null || cachePersona!!.first != idSesionDeManilla)
        {
            when (val respuesta = apis.personaPorIdSesionManilla.consultar(idSesionDeManilla))
            {
                is RespuestaIndividual.Exitosa       ->
                {
                    Pair(idSesionDeManilla, respuesta.respuesta)
                }
                is RespuestaIndividual.Vacia         ->
                {
                    eventosDeMensajesError.onNext("No se encontró a la persona asociada")
                    null
                }
                is RespuestaIndividual.Error.Timeout ->
                {
                    eventosDeMensajesError.onNext("Timeout contactando el backend")
                    null
                }
                is RespuestaIndividual.Error.Red     ->
                {
                    eventosDeMensajesError.onNext("Error contactando el backend")
                    null
                }
                is RespuestaIndividual.Error.Back    ->
                {
                    eventosDeMensajesError.onNext("Error al consultar la persona: ${respuesta.error.mensaje}")
                    null
                }
            }
        }
        else
        {
            cachePersona
        }
    }

    private fun efectuarConsumos(
            tagConsumosLeido: TagConsumos,
            consumos: List<Consumo>,
            operacionesNFC: OperacionesCompuestas<*, *>,
            proveedores: Proveedores
                                )
    {
        val fondosEnTagConIdPaquete = tagConsumosLeido.fondosEnTag.map { CalculadoraDeConsumos.FondoEnTagConIdPaquete(it) }
        val resultadoConsumo = proveedores.calculadoraDeConsumos.descontarConsumosDeFondos(consumos, fondosEnTagConIdPaquete)

        if (resultadoConsumo.todosLosConsumosRealizadosPorCompleto)
        {
            val nuevoTagConsumos = tagConsumosLeido.actualizarCreditos(resultadoConsumo.fondosEnTag)
            val codificoCorrectamente = operacionesNFC.escribirTag(comprimir(nuevoTagConsumos.aByteArray()))

            if (codificoCorrectamente)
            {
                val loteDeOrdenes =
                        crearLoteDeOrdenes(
                                resultadoConsumo,
                                proveedores.calculadorGrupoCliente.darGrupoClienteParaPersona(cachePersona!!.second),
                                tagConsumosLeido.idSesionDeManilla,
                                proveedores.proveedorCodigosExternosFondos
                                          )

                val respuestaCreacionLote = apis.lotesDeOrdenes.actualizar(loteDeOrdenes.id, loteDeOrdenes)

                if (respuestaCreacionLote is RespuestaIndividual.Exitosa)
                {
                    val respuestaConfirmacion = apis.lotesDeOrdenes.actualizarCampos(loteDeOrdenes.id, TransaccionEntidadTerminadaDTO(true))

                    if (respuestaConfirmacion === RespuestaVacia.Exitosa)
                    {
                        carritoDeCreditos.removerCreditosAgregados()
                        eventosResultadosDeConsumos.onNext(Opcional.De(generarResultadoConsumosFinal(resultadoConsumo, proveedores.proveedorNombresYPreciosPorDefectoCompletosFondos)))
                        eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.CON_CARRITO_VACIO)
                    }
                    else
                    {
                        operacionesNFC.escribirTag(comprimir(tagConsumosLeido.aByteArray()))

                        val mensajeDeErrorAlConfirmarLote =
                                when (respuestaConfirmacion)
                                {
                                    RespuestaVacia.Exitosa       -> ""
                                    RespuestaVacia.Error.Timeout -> "Timeout contactando el backend"
                                    is RespuestaVacia.Error.Red  -> "Hubo un error en la conexión y no fue posible contactar al servidor"
                                    is RespuestaVacia.Error.Back -> "Error al reportar orden: ${respuestaConfirmacion.error.mensaje}"
                                }

                        eventosDeMensajesError.onNext(mensajeDeErrorAlConfirmarLote)
                        eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
                    }
                }
                else
                {
                    operacionesNFC.escribirTag(comprimir(tagConsumosLeido.aByteArray()))

                    val mensajeDeErrorAlCrearLote =
                            when (respuestaCreacionLote)
                            {
                                is RespuestaIndividual.Exitosa       -> ""
                                is RespuestaIndividual.Vacia         -> "Error al reportar la orden creada"
                                is RespuestaIndividual.Error.Timeout -> "Timeout contactando el backend"
                                is RespuestaIndividual.Error.Red     -> "Hubo un error en la conexión y no fue posible contactar al servidor"
                                is RespuestaIndividual.Error.Back    -> "Error al reportar orden: ${respuestaCreacionLote.error.mensaje}"
                            }

                    eventosDeMensajesError.onNext(mensajeDeErrorAlCrearLote)
                    eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
                }
            }
            else
            {
                eventosDeMensajesError.onNext("Error al intentar escribir en el tag")
                eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
            }
        }
        else
        {
            eventosResultadosDeConsumos.onNext(Opcional.De(generarResultadoConsumosFinal(resultadoConsumo, proveedores.proveedorNombresYPreciosPorDefectoCompletosFondos)))
            eventosDeEstado.onNext(CodificacionDeConsumosUI.Estado.ESPERANDO_TAG)
        }
    }

    private fun crearLoteDeOrdenes(
            resultadoConsumo: CalculadoraDeConsumos.ResultadosDeConsumos,
            grupoClientePersona: GrupoClientes?,
            idSesionDeManilla: Long,
            proveedorCodigosExternosFondos: ProveedorCodigosExternosFondos
                                  ): LoteDeOrdenes
    {
        val transacciones =
                resultadoConsumo.desgloses.flatMap { desglose ->
                    desglose.consumosRealizados.map {
                        Transaccion.Debito(
                                contextoDeSesion.idCliente,
                                null,
                                contextoDeSesion.nombreDeUsuario,
                                contextoDeSesion.idUbicacion,
                                it.idFondoConsumido,
                                // Asume que existe el código externo porque se agregó el producto al carrito
                                proveedorCodigosExternosFondos.darCodigoExterno(it.idFondoConsumido)!!,
                                it.cantidadConsumida,
                                grupoClientePersona?.id,
                                contextoDeSesion.idDispositivoDeProcesamiento
                                          )
                    }
                }

        return LoteDeOrdenes(
                contextoDeSesion.idCliente,
                contextoDeSesion.nombreDeUsuario,
                listOf(
                        Orden(
                                contextoDeSesion.idCliente,
                                null,
                                idSesionDeManilla,
                                transacciones,
                                ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                             )
                      )
                            )
    }

    private fun generarResultadoConsumosFinal(
            resultadoConsumo: CalculadoraDeConsumos.ResultadosDeConsumos,
            proveedorNombresYPreciosPorDefectoCompletosFondos: ProveedorNombresYPreciosPorDefectoCompletosFondos
                                             )
            : CodificacionDeConsumosUI.ResultadosDeConsumos
    {
        return CodificacionDeConsumosUI.ResultadosDeConsumos(
                resultadoConsumo.desgloses.map {
                    val consumoConNombreConsumible =
                            ConsumoConNombreConsumible(
                                    //Si pudo agregar el consumible al carrito quiere decir que ya está guardado localmente, por lo que
                                    //siempre debería encontrar el nombre
                                    proveedorNombresYPreciosPorDefectoCompletosFondos.darNombreFondoSegunId(it.consumo.idConsumible)!!,
                                    it.consumo
                                                      )

                    val consumosRealizadosConNombre = it.consumosRealizados.map {
                        CodificacionDeConsumosUI.ConsumoRealizadoConNombre(
                                // Asume que se sincronizaron todos los fondos correctamente y por lo tanto se va a encontrar el nombre
                                proveedorNombresYPreciosPorDefectoCompletosFondos.darNombreFondoSegunId(it.idFondoConsumido)!!,
                                it.cantidadInicial,
                                it.cantidadConsumida,
                                it.cantidadFinal
                                                                          )
                    }

                    CodificacionDeConsumosUI.DesgloseDeConsumoConNombres(
                            consumoConNombreConsumible,
                            consumosRealizadosConNombre,
                            it.consumidoCompetamente
                                                                        )
                },
                resultadoConsumo.todosLosConsumosRealizadosPorCompleto,
                ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
                                                            )
    }

    override fun finalizarProceso()
    {
        proveedorOperacionesNFC.permitirLecturaNFC = false
        disposables.dispose()
        super.finalizarProceso()
    }

    private class Proveedores(
            val proveedorNombresYPreciosPorDefectoCompletosFondos: ProveedorNombresYPreciosPorDefectoCompletosFondos,
            val proveedorCodigosExternosFondos: ProveedorCodigosExternosFondos,
            val calculadorGrupoCliente: CalculadorGrupoCliente,
            val calculadoraDeConsumos: CalculadoraDeConsumos
                             )

    class Apis(
            val lotesDeOrdenes: LoteDeOrdenesAPI,
            val personaPorIdSesionManilla: PersonaPorIdSesionManillaAPI,
            val sesionDeManillaAPI: SesionDeManillaAPI
              )
}