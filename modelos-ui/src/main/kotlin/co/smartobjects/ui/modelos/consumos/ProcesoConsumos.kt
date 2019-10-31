package co.smartobjects.ui.modelos.consumos

import co.smartobjects.logica.fondos.*
import co.smartobjects.logica.fondos.precios.CalculadorGrupoCliente
import co.smartobjects.logica.fondos.precios.CalculadorGrupoClientesEnMemoria
import co.smartobjects.logica.personas.CalculadorGrupoEdadEnMemoria
import co.smartobjects.nfc.ProveedorOperacionesNFC
import co.smartobjects.persistencia.fondos.RepositorioFondos
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdad
import co.smartobjects.ui.modelos.ContextoDeSesion
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.catalogo.Catalogo
import co.smartobjects.ui.modelos.catalogo.CatalogoUI
import co.smartobjects.ui.modelos.catalogo.ProveedorImagenesProductos
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondos
import co.smartobjects.ui.modelos.menufiltrado.MenuFiltradoFondosUI
import co.smartobjects.ui.modelos.menufiltrado.ProveedorIconosCategoriasFiltrado
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

interface ProcesoConsumosUI<TipoIconoFiltrado : ProveedorIconosCategoriasFiltrado.Icono> : ModeloUI
{
    val menuFiltradoFondos: MenuFiltradoFondosUI<TipoIconoFiltrado>
    val catalogo: CatalogoUI
    val codificacionDeConsumos: CodificacionDeConsumosUI
}

class ProcesoConsumos<TipoIconoFiltrado : ProveedorIconosCategoriasFiltrado.Icono> internal constructor
(
        private val contextoDeSesion: ContextoDeSesion,
        private val repositorios: Repositorios,
        private val apis: CodificacionDeConsumos.Apis,
        proveedorImagenesProductos: ProveedorImagenesProductos,
        proveedorIconosCategoriasFiltrado: ProveedorIconosCategoriasFiltrado<TipoIconoFiltrado>,
        private val proveedorOperacionesNFC: ProveedorOperacionesNFC,
        schedulerBackground: Scheduler = Schedulers.io(),

        _menuFiltradoFondos: MenuFiltradoFondosUI<TipoIconoFiltrado>?,
        _catalogo: CatalogoUI?,
        _codificacionDeConsumos: CodificacionDeConsumosUI?
) : ProcesoConsumosUI<TipoIconoFiltrado>
{
    constructor
            (
                    contextoDeSesion: ContextoDeSesion,
                    repositorios: Repositorios,
                    apis: CodificacionDeConsumos.Apis,
                    proveedorImagenesProductos: ProveedorImagenesProductos,
                    proveedorIconosCategoriasFiltrado: ProveedorIconosCategoriasFiltrado<TipoIconoFiltrado>,
                    proveedorOperacionesNFC: ProveedorOperacionesNFC,
                    schedulerBackground: Scheduler = Schedulers.io()
            )
            : this(
            contextoDeSesion,
            repositorios,
            apis,
            proveedorImagenesProductos,
            proveedorIconosCategoriasFiltrado,
            proveedorOperacionesNFC,
            schedulerBackground,
            null,
            null,
            null
                  )


    private val impuestos =
            Single
                .fromCallable { repositorios.impuestos.listar(contextoDeSesion.idCliente) }
                .cache()
                .subscribeOn(schedulerBackground)

    private val fondos =
            Single
                .fromCallable { repositorios.fondos.listar(contextoDeSesion.idCliente) }
                .cache()
                .subscribeOn(schedulerBackground)

    private val categoriasSku =
            Single
                .fromCallable { repositorios.categoriasSku.listar(contextoDeSesion.idCliente) }
                .cache()
                .subscribeOn(schedulerBackground)

    private val gruposClientes =
            Single
                .fromCallable { repositorios.repositorioGrupoClientes.listar(contextoDeSesion.idCliente) }
                .cache()
                .subscribeOn(schedulerBackground)

    private val valoresGruposEdad =
            Single
                .fromCallable { repositorios.repositorioValoresGruposEdad.listar(contextoDeSesion.idCliente) }
                .cache()
                .subscribeOn(schedulerBackground)


    private val proveedorNombresYPreciosCompletosFondos =
            Singles
                .zip(fondos, impuestos)
                .map<ProveedorNombresYPreciosPorDefectoCompletosFondos> {
                    ProveedorNombresYPreciosPorDefectoPorDefectoCompletosEnMemoria(it.first, it.second)
                }
                .cache()
                .subscribeOn(schedulerBackground)

    private val proveedorCategoriasPadres =
            categoriasSku
                .map<ProveedorCategoriasPadres> {
                    ProveedorCategoriasPadresEnMemoria(it)
                }
                .cache()
                .subscribeOn(schedulerBackground)

    private val proveedorCodigosExternosFondos =
            fondos
                .map<ProveedorCodigosExternosFondos> {
                    ProveedorCodigosExternosFondosEnMemoria(it)
                }
                .cache()
                .subscribeOn(schedulerBackground)

    private val calculadorGrupoDeEdad =
            Singles
                .zip(gruposClientes, valoresGruposEdad)
                .map<CalculadorGrupoCliente> {
                    CalculadorGrupoClientesEnMemoria(it.first.toList(), CalculadorGrupoEdadEnMemoria(it.second.toList()))
                }
                .cache()
                .subscribeOn(schedulerBackground)

    private val calculadoraDeConsumos =
            fondos
                .map<CalculadoraDeConsumos> {
                    CalculadoraDeConsumosEnMemoria(it)
                }
                .cache()
                .subscribeOn(schedulerBackground)


    override val menuFiltradoFondos: MenuFiltradoFondosUI<TipoIconoFiltrado> =
            _menuFiltradoFondos
            ?: MenuFiltradoFondos(
                    proveedorIconosCategoriasFiltrado,
                    categoriasSku.map
                    { it.toList() }
                                 )

    override val catalogo: CatalogoUI =
            _catalogo
            ?: Catalogo(
                    Single.just(listOf()),
                    fondos.map { it.toList() },
                    proveedorCategoriasPadres,
                    proveedorNombresYPreciosCompletosFondos,
                    proveedorImagenesProductos
                       )

    override val codificacionDeConsumos: CodificacionDeConsumosUI =
            _codificacionDeConsumos
            ?: CodificacionDeConsumos(
                    contextoDeSesion,
                    proveedorNombresYPreciosCompletosFondos,
                    proveedorOperacionesNFC,
                    proveedorCodigosExternosFondos,
                    calculadorGrupoDeEdad,
                    calculadoraDeConsumos,
                    apis
                                     )


    override val observadoresInternos: List<Observer<*>> = listOf()
    override val modelosHijos: List<ModeloUI> = listOf(menuFiltradoFondos, catalogo, codificacionDeConsumos)

    private val disposables = CompositeDisposable()

    init
    {
        catalogo
            .catalogoDeProductos
            .firstElement()
            .subscribe {
                for (productoUI in it.catalogo)
                {
                    productoUI.productoAAgregar.subscribe { producto ->
                        codificacionDeConsumos.agregarProducto(producto)
                    }.addTo(disposables)
                }
            }.addTo(disposables)

        menuFiltradoFondos.filtrosDisponibles.subscribe { filtros ->
            filtros.forEach {
                it.filtroActivado.subscribe(catalogo::filtrarPorCriterio).addTo(disposables)
            }
        }.addTo(disposables)
    }

    class Repositorios(
            val impuestos: RepositorioImpuestos,
            val categoriasSku: RepositorioCategoriasSkus,
            val fondos: RepositorioFondos,
            val repositorioGrupoClientes: RepositorioGrupoClientes,
            val repositorioValoresGruposEdad: RepositorioValoresGruposEdad
                      )
}