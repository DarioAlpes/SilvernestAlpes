package co.smartobjects.ui.modelos.carritocreditos

import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.entidades.operativas.compras.CreditoPaqueteConNombre
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.emitirUnicoValorSi
import co.smartobjects.utilidades.Decimal
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ItemCreditoUI : ModeloUI
{
    val idFondos: List<Long>
    val nombreProducto: String
    val precios: List<PrecioCompleto>
    val noEsModificable: Boolean
    val estaPagado: Boolean
    val idPaquete: Long?
    val esPaquete: Boolean
    val precioTotalInicial: Decimal
    val contieneFondoUnico: Boolean
    val cantidad: Observable<Int>
    val precioSinImpuesto: Observable<Decimal>
    val valorImpuesto: Observable<Decimal>
    val precioConImpuestos: Observable<Decimal>
    val seDebeBorrar: Observable<Int>
    val faltaConfirmacionAdicion: Observable<Boolean>


    fun confirmarAdicion()

    fun sumarUno()

    fun restarUno()

    fun borrar()

    fun aCreditoPaquete(
            idCliente: Long,
            origen: String,
            usuario: String,
            idPersona: Long,
            idDispositivo: String,
            idUbicacionCompra: Long?,
            idGrupoClientesPersona: Long?
                       ): CreditoPaqueteConNombre?

    fun aCreditoFondo(
            idCliente: Long,
            origen: String,
            usuario: String,
            idPersona: Long,
            idDispositivo: String,
            idUbicacionCompra: Long?,
            idGrupoClientesPersona: Long?
                     ): CreditoFondo?
}

class ItemCredito private constructor
(
        override val idFondos: List<Long>,
        private val codigosExternos: List<String>,
        override val nombreProducto: String,
        override val precios: List<PrecioCompleto>,
        private val idsCreditos: List<Long?>,
        override val contieneFondoUnico: Boolean,
        cantidadInicial: Int,
        preIncluido: Boolean,
        override val estaPagado: Boolean,
        faltaConfirmacionAdicion: Boolean,
        override val idPaquete: Long?,
        private val codigoExternoPaquete: String?,
        private val cantidadesEnPaqute: List<Decimal>?
) : ItemCreditoUI
{
    constructor(
            nombre: String,
            idFondo: Long,
            codigoExterno: String,
            precio: PrecioCompleto,
            idCredito: Long?,
            contieneItemUnico: Boolean,
            cantidadInicial: Int,
            preIncluido: Boolean,
            faltaConfirmacion: Boolean
               )
            : this(
            listOf(idFondo),
            listOf(codigoExterno),
            nombre,
            listOf(precio),
            if (idCredito == null) listOf(null) else listOf(idCredito),
            contieneItemUnico,
            cantidadInicial,
            preIncluido,
            idCredito != null,
            faltaConfirmacion,
            null,
            null,
            null
                  )

    constructor(
            nombre: String,
            idPaquete: Long,
            codigoExternoPaquete: String,
            idFondos: List<Long>,
            codigosExterno: List<String>,
            precios: List<PrecioCompleto>,
            idsCreditos: List<Long>?,
            contieneItemUnico: Boolean,
            cantidadesEnPaqute: List<Decimal>,
            cantidadInicial: Int,
            preIncluido: Boolean,
            faltaConfirmacion: Boolean
               )
            : this(
            idFondos,
            codigosExterno,
            nombre,
            precios,
            idsCreditos ?: List(precios.size) { null },
            contieneItemUnico,
            cantidadInicial,
            preIncluido,
            idsCreditos != null,
            faltaConfirmacion,
            idPaquete,
            codigoExternoPaquete,
            cantidadesEnPaqute
                  )

    override val noEsModificable = estaPagado || preIncluido

    private val eventosIncrementar = PublishSubject.create<Int>().also { if (noEsModificable) it.onComplete() }
    private val eventosDecerementar = PublishSubject.create<Int>().also { if (noEsModificable) it.onComplete() }
    private val eventoDeBorrado = PublishSubject.create<Boolean>()
    override val esPaquete = idPaquete != null

    private val precioSinImpuestosTotal =
            precios
                .asSequence()
                .mapIndexed { i, precio -> precio.precioSinImpuesto * (cantidadesEnPaqute?.get(i) ?: Decimal.UNO) }
                .reduce { acc, siguientePrecio -> acc + siguientePrecio }

    private val valorImpuestosTotal =
            precios
                .asSequence()
                .mapIndexed { i, precio -> precio.valorImpuesto * (cantidadesEnPaqute?.get(i) ?: Decimal.UNO) }
                .reduce { acc, siguientePrecio -> acc + siguientePrecio }

    override val precioTotalInicial = precioSinImpuestosTotal + valorImpuestosTotal


    override val cantidad =
            emitirUnicoValorSi(noEsModificable, cantidadInicial)
            {
                val incrementosFiltradosSegunUnicidad = eventosIncrementar.filter { !contieneFondoUnico }

                Observable.merge<Int>(incrementosFiltradosSegunUnicidad, eventosDecerementar)
                    .scan(cantidadInicial) { acumulado: Int, delta: Int -> acumulado + delta }
                    .takeUntil { it == 0 }
            }.replay(1).refCount()

    override val precioSinImpuesto =
            emitirUnicoValorSi(noEsModificable, precioSinImpuestosTotal * cantidadInicial)
            { cantidad.map { precioSinImpuestosTotal * it }!! }

    override val valorImpuesto =
            emitirUnicoValorSi(noEsModificable, valorImpuestosTotal * cantidadInicial)
            { cantidad.map { valorImpuestosTotal * it }!! }

    override val precioConImpuestos =
            emitirUnicoValorSi(noEsModificable, precioTotalInicial * cantidadInicial)
            {
                Observables.zip(precioSinImpuesto, valorImpuesto) { precio, impuestos ->
                    precio + impuestos
                }
            }

    override val seDebeBorrar =
            Observable.merge(cantidad.skipWhile { it > 0 }, eventoDeBorrado)
                .skipWhile { noEsModificable }
                .map { hashCode() }
                .takeUntil { true }!!

    private val eventosFaltaConfirmacionAdicion = BehaviorSubject.createDefault(!noEsModificable && faltaConfirmacionAdicion)
    override val faltaConfirmacionAdicion: Observable<Boolean> =
            emitirUnicoValorSi(noEsModificable, false)
            { eventosFaltaConfirmacionAdicion.map { it }.distinctUntilChanged() }


    override val observadoresInternos: List<Observer<*>> =
            listOf(eventosIncrementar, eventosDecerementar, eventoDeBorrado, eventosFaltaConfirmacionAdicion)

    override val modelosHijos: List<ModeloUI> = emptyList()

    private val disposables = CompositeDisposable()

    private var creditoFondoParcial: CreditoFondo? = null
    private var itemCreditoPaqueteParcial: CreditoPaqueteConNombre? = null

    init
    {
        if (idFondos.size != codigosExternos.size)
        {
            throw EntidadMalInicializada(
                    this::class.java.simpleName,
                    ::idFondos.name + " y/o " + ::codigosExternos.name,
                    "${::idFondos.name}.size = ${idFondos.size}; ${::codigosExternos.name}.size = ${codigosExternos.size}",
                    "Deben tener el mismo número de ítems"
                                        )
        }

        if (idFondos.size != precios.size)
        {
            throw EntidadMalInicializada(
                    this::class.java.simpleName,
                    ::idFondos.name + " y/o " + ::precios.name,
                    "${::idFondos.name}.size = ${idFondos.size}; ${::precios.name}.size = ${precios.size}",
                    "Deben tener el mismo número de ítems"
                                        )
        }

        if (precios.size != idsCreditos.size)
        {
            throw EntidadMalInicializada(
                    this::class.java.simpleName,
                    ::precios.name + " y/o " + ::idsCreditos.name,
                    "${::precios.name}.size = ${precios.size}; ${::idsCreditos.name}.size = ${idsCreditos.size}",
                    "Deben tener el mismo número de ítems"
                                        )
        }

        if (esPaquete)
        {
            itemCreditoPaqueteParcial =
                    CreditoPaqueteConNombre(
                            nombreProducto,
                            cantidadInicial,
                            CreditoPaquete(
                                    idPaquete!!,
                                    codigoExternoPaquete!!,
                                    idFondos.mapIndexed { index, idFondo ->
                                        val cantidad = cantidadesEnPaqute!![index] * cantidadInicial
                                        CreditoFondo(
                                                idCliente = 0,
                                                id = idsCreditos[index],
                                                cantidad = cantidad,
                                                valorPagado = precios[index].precioConImpuesto * cantidad,
                                                valorImpuestoPagado = precios[index].valorImpuesto * cantidad,
                                                validoDesde = null,
                                                validoHasta = null,
                                                consumido = false,
                                                origen = "x",
                                                nombreUsuario = "x",
                                                idPersonaDueña = 0,
                                                idFondoComprado = idFondo,
                                                codigoExternoFondo = codigosExternos[index],
                                                idImpuestoPagado = precios[index].impuesto.id!!,
                                                idDispositivo = "x",
                                                idUbicacionCompra = null,
                                                idGrupoClientesPersona = null
                                                    )
                                    }
                                          )
                                           )

            emitirUnicoValorSi(noEsModificable, itemCreditoPaqueteParcial) {
                cantidad
                    .filter { it > 0 }
                    .map {
                        CreditoPaqueteConNombre(
                                nombreProducto,
                                it,
                                CreditoPaquete(
                                        idPaquete,
                                        codigoExternoPaquete,
                                        idFondos.mapIndexed { index, idFondo ->
                                            val cantidad = cantidadesEnPaqute!![index] * it
                                            CreditoFondo(
                                                    idCliente = 0,
                                                    id = idsCreditos[index],
                                                    cantidad = cantidad,
                                                    valorPagado = precios[index].precioConImpuesto * cantidad,
                                                    valorImpuestoPagado = precios[index].valorImpuesto * cantidad,
                                                    validoDesde = null,
                                                    validoHasta = null,
                                                    consumido = false,
                                                    origen = "x",
                                                    nombreUsuario = "x",
                                                    idPersonaDueña = 0,
                                                    idFondoComprado = idFondo,
                                                    codigoExternoFondo = codigosExternos[index],
                                                    idImpuestoPagado = precios[index].impuesto.id!!,
                                                    idDispositivo = "x",
                                                    idUbicacionCompra = null,
                                                    idGrupoClientesPersona = null
                                                        )
                                        }
                                              )
                                               )
                    }
                    .defaultIfEmpty(itemCreditoPaqueteParcial)!!
            }.subscribe {
                itemCreditoPaqueteParcial = it
            }.addTo(disposables)
        }
        else
        {
            creditoFondoParcial =
                    CreditoFondo(
                            idCliente = 0,
                            id = idsCreditos.first(),
                            cantidad = Decimal(cantidadInicial),
                            valorPagado = precios.first().precioConImpuesto * cantidadInicial,
                            valorImpuestoPagado = precios.first().valorImpuesto * cantidadInicial,
                            validoDesde = null,
                            validoHasta = null,
                            consumido = false,
                            origen = "x",
                            nombreUsuario = "x",
                            idPersonaDueña = 0,
                            idFondoComprado = idFondos.first(),
                            codigoExternoFondo = codigosExternos.first(),
                            idImpuestoPagado = precios.first().impuesto.id!!,
                            idDispositivo = "x",
                            idUbicacionCompra = null,
                            idGrupoClientesPersona = null
                                )

            emitirUnicoValorSi(noEsModificable, creditoFondoParcial) {
                cantidad
                    .filter { it > 0 }
                    .map {
                        CreditoFondo(
                                idCliente = 0,
                                id = idsCreditos.first(),
                                cantidad = Decimal(it),
                                valorPagado = precios.first().precioConImpuesto * it,
                                valorImpuestoPagado = precios.first().valorImpuesto * it,
                                validoDesde = null,
                                validoHasta = null,
                                consumido = false,
                                origen = "x",
                                nombreUsuario = "x",
                                idPersonaDueña = 0,
                                idFondoComprado = idFondos.first(),
                                codigoExternoFondo = codigosExternos.first(),
                                idImpuestoPagado = precios.first().impuesto.id!!,
                                idDispositivo = "x",
                                idUbicacionCompra = null,
                                idGrupoClientesPersona = null
                                    )
                    }
                    .defaultIfEmpty(creditoFondoParcial)!!
            }.subscribe {
                creditoFondoParcial = it
            }.addTo(disposables)
        }
    }

    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    override fun confirmarAdicion()
    {
        eventosFaltaConfirmacionAdicion.onNext(false)
    }

    override fun sumarUno()
    {
        eventosIncrementar.onNext(1)
    }

    override fun restarUno()
    {
        eventosDecerementar.onNext(-1)
    }

    override fun borrar()
    {
        if (!noEsModificable)
        {
            eventoDeBorrado.onNext(true)
            finalizarProceso()
        }
    }

    override fun aCreditoPaquete(
            idCliente: Long,
            origen: String,
            usuario: String,
            idPersona: Long,
            idDispositivo: String,
            idUbicacionCompra: Long?,
            idGrupoClientesPersona: Long?
                                ): CreditoPaqueteConNombre?
    {
        return itemCreditoPaqueteParcial?.let {
            val creditoPaquete = it.creditoAsociado
            it.copy(
                    creditoAsociado = creditoPaquete.copiar(
                            creditosFondos = creditoPaquete.creditosFondos.map {
                                it.copiar(
                                        idCliente = idCliente,
                                        origen = origen,
                                        nombreUsuario = usuario,
                                        idPersonaDueña = idPersona,
                                        idDispositivo = idDispositivo,
                                        idUbicacionCompra = idUbicacionCompra,
                                        idGrupoClientesPersona = idGrupoClientesPersona
                                         )
                            }
                                                           )
                   )
        }
    }

    override fun aCreditoFondo(
            idCliente: Long,
            origen: String,
            usuario: String,
            idPersona: Long,
            idDispositivo: String,
            idUbicacionCompra: Long?,
            idGrupoClientesPersona: Long?
                              ): CreditoFondo?
    {
        return creditoFondoParcial?.copiar(
                idCliente = idCliente,
                origen = origen,
                nombreUsuario = usuario,
                idPersonaDueña = idPersona,
                idDispositivo = idDispositivo,
                idUbicacionCompra = idUbicacionCompra,
                idGrupoClientesPersona = idGrupoClientesPersona
                                          )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemCredito

        if (idFondos != other.idFondos) return false
        if (codigosExternos != other.codigosExternos) return false
        if (nombreProducto != other.nombreProducto) return false
        if (precios != other.precios) return false
        if (noEsModificable != other.noEsModificable) return false
        if (estaPagado != other.estaPagado) return false
        if (idPaquete != other.idPaquete) return false
        if (codigoExternoPaquete != other.codigoExternoPaquete) return false
        if (creditoFondoParcial != other.creditoFondoParcial) return false
        if (itemCreditoPaqueteParcial != other.itemCreditoPaqueteParcial) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idFondos.hashCode()
        result = 31 * result + codigosExternos.hashCode()
        result = 31 * result + nombreProducto.hashCode()
        result = 31 * result + precios.hashCode()
        result = 31 * result + noEsModificable.hashCode()
        result = 31 * result + estaPagado.hashCode()
        result = 31 * result + idPaquete.hashCode()
        result = 31 * result + codigoExternoPaquete.hashCode()
        result = 31 * result + creditoFondoParcial.hashCode()
        result = 31 * result + itemCreditoPaqueteParcial.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "ItemCredito(idFondos=$idFondos, codigosExternos=$codigosExternos, nombreProducto='$nombreProducto', precios=$precios, noEsModificable=$noEsModificable, estaPagado=$estaPagado, faltaConfirmacionAdicion=${eventosFaltaConfirmacionAdicion.value}, idPaquete=$idPaquete, codigoExternoPaquete=$codigoExternoPaquete, creditoFondoParcial=$creditoFondoParcial, itemCreditoPaqueteParcial=$itemCreditoPaqueteParcial)"
    }
}