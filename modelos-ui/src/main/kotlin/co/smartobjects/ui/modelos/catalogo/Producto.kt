package co.smartobjects.ui.modelos.catalogo

import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoFondo
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoPaquete
import co.smartobjects.utilidades.Decimal
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ProductoUI : ModeloUI
{
    val imagen: Maybe<ProveedorImagenesProductos.ImagenProducto>
    val nombre: String
    val criteriorDeFiltrado: MenuFiltradoFondosUI.CriterioFiltrado
    val idPaquete: Long?
    val codigoExternoPaquete: String?
    val idsFondosAsociados: LinkedHashSet<Long>
    val codigosExternosAsociados: List<String>
    val nombresFondosAsociados: List<String>
    val cantidadesFondosEnPaquete: List<Decimal>?
    val preciosDeFondosAsociados: List<PrecioCompleto>
    val esPaquete: Boolean
    val precioTotal: Decimal
    val productoAAgregar: Observable<ProductoUI>
    val estaSiendoAgregado: Observable<Boolean>
    val estaHabilitado: Observable<Boolean>

    fun agregar()
    fun terminarAgregar()
    fun habilitar(estaHabilitado: Boolean)
    fun actualizarPreciosAsociados(preciosNuevos: List<PrecioCompleto>): ProductoUI
}

class Producto private constructor(
        override val imagen: Maybe<ProveedorImagenesProductos.ImagenProducto>,
        override val nombre: String,
        override val criteriorDeFiltrado: MenuFiltradoFondosUI.CriterioFiltrado,
        override val idPaquete: Long?,
        override val codigoExternoPaquete: String?,
        override val idsFondosAsociados: LinkedHashSet<Long>,
        override val codigosExternosAsociados: List<String>,
        override val nombresFondosAsociados: List<String>,
        override val cantidadesFondosEnPaquete: List<Decimal>?,
        override val preciosDeFondosAsociados: List<PrecioCompleto>)
    : ProductoUI
{
    override val esPaquete = idPaquete != null

    override val precioTotal =
            preciosDeFondosAsociados
                .asSequence()
                .map { it.precioConImpuesto }
                .reduce { acc, siguientePrecio -> acc + siguientePrecio }

    private val eventosDeAgregar = PublishSubject.create<ProductoUI>()
    override val productoAAgregar = eventosDeAgregar.hide()!!

    private val eventosEstaSiendoAgregado = BehaviorSubject.createDefault(false)
    override val estaSiendoAgregado = eventosEstaSiendoAgregado.hide().distinctUntilChanged()!!

    private val eventosDeHabilitar = BehaviorSubject.createDefault(true)
    override val estaHabilitado = eventosDeHabilitar.hide().distinctUntilChanged()!!


    override val observadoresInternos: List<Observer<*>> = listOf(eventosDeAgregar, eventosEstaSiendoAgregado, eventosDeHabilitar)
    override val modelosHijos: List<ModeloUI> = emptyList()

    constructor(productoFondo: ProductoFondo, proveedorImagenesProductos: ProveedorImagenesProductos)
            : this(
            proveedorImagenesProductos.darImagen(productoFondo.fondo.id!!, false),
            productoFondo.fondo.nombre,
            MenuFiltradoFondosUI.CriterioFiltrado.deProductoFondoACriterioFiltrado(productoFondo),
            null,
            null,
            linkedSetOf(productoFondo.fondo.id!!),
            listOf(productoFondo.fondo.codigoExterno),
            listOf(productoFondo.fondo.nombre),
            null,
            listOf(productoFondo.precioCompleto)
                  )

    constructor(productoPaquete: ProductoPaquete, proveedorImagenesProductos: ProveedorImagenesProductos)
            : this(
            proveedorImagenesProductos.darImagen(productoPaquete.id, true),
            productoPaquete.nombre,
            MenuFiltradoFondosUI.CriterioFiltrado.EsPaquete,
            productoPaquete.id,
            productoPaquete.codigoExternoPaquete,
            productoPaquete.idsFondosIncluidos,
            productoPaquete.codigosExternosFondosIncluidos,
            productoPaquete.nombresFondosAsociados,
            productoPaquete.cantidadesFondosIncluidos,
            productoPaquete.preciosCompletosFondosIncluidos
                  )

    override fun agregar()
    {
        if (eventosEstaSiendoAgregado.value == false && eventosDeHabilitar.value == true) // Value puede ser null después de invocar finalizarProceso
        {
            eventosEstaSiendoAgregado.onNext(true)
            eventosDeAgregar.onNext(this)
        }
    }

    override fun terminarAgregar()
    {
        if (eventosEstaSiendoAgregado.value == true && eventosDeHabilitar.value == true) // Value puede ser null después de invocar finalizarProceso
        {
            eventosEstaSiendoAgregado.onNext(false)
        }
    }

    override fun habilitar(estaHabilitado: Boolean)
    {
        eventosDeHabilitar.onNext(estaHabilitado)
    }

    override fun actualizarPreciosAsociados(preciosNuevos: List<PrecioCompleto>): ProductoUI
    {
        return Producto(
                imagen,
                nombre,
                criteriorDeFiltrado,
                idPaquete,
                codigoExternoPaquete,
                idsFondosAsociados,
                codigosExternosAsociados,
                nombresFondosAsociados,
                cantidadesFondosEnPaquete,
                preciosNuevos)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Producto

        if (nombre != other.nombre) return false
        if (criteriorDeFiltrado != other.criteriorDeFiltrado) return false
        if (idPaquete != other.idPaquete) return false
        if (codigoExternoPaquete != other.codigoExternoPaquete) return false
        if (idsFondosAsociados != other.idsFondosAsociados) return false
        if (codigosExternosAsociados != other.codigosExternosAsociados) return false
        if (nombresFondosAsociados != other.nombresFondosAsociados) return false
        if (cantidadesFondosEnPaquete != other.cantidadesFondosEnPaquete) return false
        if (preciosDeFondosAsociados != other.preciosDeFondosAsociados) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = nombre.hashCode()
        result = 31 * result + criteriorDeFiltrado.hashCode()
        result = 31 * result + idPaquete.hashCode()
        result = 31 * result + codigoExternoPaquete.hashCode()
        result = 31 * result + idsFondosAsociados.hashCode()
        result = 31 * result + codigosExternosAsociados.hashCode()
        result = 31 * result + nombresFondosAsociados.hashCode()
        result = 31 * result + cantidadesFondosEnPaquete.hashCode()
        result = 31 * result + preciosDeFondosAsociados.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Producto(nombre='$nombre', criteriorDeFiltrado=$criteriorDeFiltrado, idPaquete=$idPaquete, codigoExternoPaquete=${if (codigoExternoPaquete == null) "null" else "'$codigoExternoPaquete'"} idsFondosAsociados=$idsFondosAsociados, codigosExternosAsociados=$codigosExternosAsociados, nombresFondosAsociados=$nombresFondosAsociados, cantidadesFondosEnPaquete=$cantidadesFondosEnPaquete, preciosDeFondosAsociados=$preciosDeFondosAsociados, esPaquete=$esPaquete, precioTotal=$precioTotal)"
    }
}