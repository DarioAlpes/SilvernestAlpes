package co.smartobjects.ui.modelos.catalogo

import co.smartobjects.entidades.fondos.*
import co.smartobjects.logica.fondos.ProveedorCategoriasPadres
import co.smartobjects.logica.fondos.ProveedorNombresYPreciosPorDefectoCompletosFondos
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.NoExisteCategoriaParaElFondo
import co.smartobjects.ui.modelos.NoSePudoCargarNingunDatoAsociadoAlCatalogo
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoFondo
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoPaquete
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.BehaviorSubject

interface ProveedorImagenesProductos
{
    fun darImagen(idProducto: Long, esPaquete: Boolean): Maybe<ImagenProducto>

    interface ImagenProducto
    {
        val urlImagen: String
    }
}

interface CatalogoUI : ModeloUI
{
    val catalogoDeProductos: Observable<ResultadoCatalogo>

    fun filtrarPorCriterio(filtro: MenuFiltradoFondosUI.CriterioFiltrado)

    data class ResultadoCatalogo(val catalogo: List<ProductoUI>, val error: Throwable?)

}

class Catalogo(
        catalogoPaquetes: Single<List<Paquete>>,
        catalogoDeFondos: Single<List<Fondo<*>>>,
        proveedorCategoriasPadres: Single<ProveedorCategoriasPadres>,
        proveedorNombresYPreciosPorDefectoCompletosFondos: Single<ProveedorNombresYPreciosPorDefectoCompletosFondos>,
        private val proveedorImagenesProductos: ProveedorImagenesProductos
              ) : CatalogoUI
{
    private val eventosDeFiltrado: BehaviorSubject<MenuFiltradoFondosUI.CriterioFiltrado> =
            BehaviorSubject.createDefault(MenuFiltradoFondosUI.CriterioFiltrado.Todos)


    private val productosDeCatalogo =
            Single.mergeDelayError(
                    catalogoPaquetes
                        .zipWith(proveedorNombresYPreciosPorDefectoCompletosFondos)
                        .map { pareja ->
                            pareja.first.map {
                                val idsFondos = linkedSetOf<Long>().run {
                                    it.fondosIncluidos.mapTo(this) { it.idFondo }
                                }

                                val productoPaquete =
                                        ProductoPaquete(
                                                it,
                                                pareja.second.darNombresFondosSegunIds(idsFondos),
                                                pareja.second.completarPreciosFondos(idsFondos)
                                                       )

                                Producto(productoPaquete, proveedorImagenesProductos) as ProductoUI
                            }
                        },
                    Singles
                        .zip(catalogoDeFondos, proveedorNombresYPreciosPorDefectoCompletosFondos, proveedorCategoriasPadres)
                        .map { tripleta ->
                            tripleta.first.map {
                                val precioCompleto = tripleta.second.completarPrecioFondo(it.id!!)!!
                                val productoFondo: ProductoFondo =
                                        when (it)
                                        {
                                            is CategoriaSku -> ProductoFondo.ConCategoria(it, precioCompleto)
                                            is Dinero       -> ProductoFondo.SinCategoria(it, precioCompleto)
                                            is Sku          ->
                                            {
                                                val categoriasPadres =
                                                        tripleta.third.darPadres(it.idDeCategoria)
                                                        ?: throw NoExisteCategoriaParaElFondo(it.id, it.idDeCategoria)

                                                ProductoFondo.ConCategoria(it, precioCompleto, categoriasPadres)
                                            }
                                            is Entrada      -> ProductoFondo.SinCategoria(it, precioCompleto)
                                            is Acceso       -> ProductoFondo.SinCategoria(it, precioCompleto)
                                            else            -> throw RuntimeException("No existe conversión apropiada para el fondo '${it.javaClass.canonicalName}'")
                                        }

                                Producto(productoFondo, proveedorImagenesProductos) as ProductoUI
                            }
                        }
                                  )
                .toObservable()
                .materialize()
                .buffer(3) // Si ambos funcionan se emiten 3 (2 valores y completion). Si falla solo 1, se emiten 2 (valor y error). Si ambos fallan se emite 1 (error compuesto)
                .map {
                    if (it.size == 1 && it.first().isOnError)
                    {
                        throw NoSePudoCargarNingunDatoAsociadoAlCatalogo("Error al cargar catálogo de productos", it.first().error)
                    }

                    var posibleError: Notification<List<ProductoUI>>? = null
                    val onNextConcatenados = mutableListOf<ProductoUI>()
                    var conteoDeProductos = 0
                    for (notificacion in it)
                    {
                        if (notificacion.isOnNext)
                        {
                            conteoDeProductos += notificacion.value!!.size
                            onNextConcatenados.addAll(notificacion.value!!)
                        }
                        else if (notificacion.isOnError)
                        {
                            posibleError = notificacion
                        }
                    }

                    CatalogoUI.ResultadoCatalogo(onNextConcatenados, posibleError?.error)
                }
                .doOnNext {
                    val catalogoEmitido = it.catalogo
                    catalogoEmitido.forEach { productoUI ->
                        productoUI.estaSiendoAgregado.subscribe { seEstaAgregando ->
                            catalogoEmitido.asSequence().filter { it != productoUI }.forEach {
                                it.habilitar(!seEstaAgregando)
                            }
                        }.addTo(disposables)
                    }
                }
                .cache()

    override val catalogoDeProductos =
            Observables.combineLatest(productosDeCatalogo, eventosDeFiltrado)
            { resultadoCatalogo, filtro ->
                resultadoCatalogo.copy(
                        catalogo = resultadoCatalogo.catalogo.filter { it.criteriorDeFiltrado.aplicaFiltro(filtro) }
                                      )
            }

    override val observadoresInternos: List<Observer<*>> = listOf(eventosDeFiltrado)
    override val modelosHijos: List<ModeloUI> = emptyList()

    private val disposables = CompositeDisposable()


    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    override fun filtrarPorCriterio(filtro: MenuFiltradoFondosUI.CriterioFiltrado)
    {
        eventosDeFiltrado.onNext(filtro)
    }
}