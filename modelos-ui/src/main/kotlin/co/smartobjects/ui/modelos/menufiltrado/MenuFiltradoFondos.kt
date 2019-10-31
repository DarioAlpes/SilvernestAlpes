package co.smartobjects.ui.modelos.menufiltrado

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.ui.modelos.ModeloUI
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject

class MenuFiltradoFondos<TipoIcono : ProveedorIconosCategoriasFiltrado.Icono>(
        private val proveedorIconosCategoriasFiltrado: ProveedorIconosCategoriasFiltrado<TipoIcono>,
        categoriasSkuDisponibles: Single<List<CategoriaSku>>)
    : MenuFiltradoFondosUI<TipoIcono>
{
    private val disposables = CompositeDisposable()

    override val observadoresInternos: List<Observer<*>> = emptyList()

    private val modelosHijosAgregados = mutableListOf<ModeloUI>()
    override val modelosHijos: List<ModeloUI>
        get() = modelosHijosAgregados

    override val filtrosDisponibles: Single<List<MenuFiltradoFondosUI.ItemFiltradoFondoUI<TipoIcono>>> =
            categoriasSkuDisponibles
                .map {
                    modelosHijosAgregados.clear()

                    fun generarItemFiltrado(
                            nombreFiltro: String,
                            criterioDeFiltrado: MenuFiltradoFondosUI.CriterioFiltrado,
                            activo: Boolean,
                            coleccionDeHijos: MutableList<ModeloUI>,
                            proveedorIconosCategoriasFiltrado: ProveedorIconosCategoriasFiltrado<TipoIcono>)
                            : ItemFiltradoFondo<TipoIcono>
                    {
                        return ItemFiltradoFondo(
                                nombreFiltro,
                                proveedorIconosCategoriasFiltrado.darIcono(criterioDeFiltrado),
                                criterioDeFiltrado,
                                activo
                                                )
                            .also {
                                coleccionDeHijos.add(it)
                            }
                    }

                    val itemsFiltrado =
                            mutableListOf<MenuFiltradoFondosUI.ItemFiltradoFondoUI<TipoIcono>>(
                                    generarItemFiltrado(
                                            "Todos",
                                            MenuFiltradoFondosUI.CriterioFiltrado.Todos,
                                            true,
                                            modelosHijosAgregados,
                                            proveedorIconosCategoriasFiltrado
                                                       ),
                                    generarItemFiltrado(
                                            "Paquetes",
                                            MenuFiltradoFondosUI.CriterioFiltrado.EsPaquete,
                                            false,
                                            modelosHijosAgregados,
                                            proveedorIconosCategoriasFiltrado
                                                       ),
                                    generarItemFiltrado(
                                            "Entradas",
                                            MenuFiltradoFondosUI.CriterioFiltrado.EsEntrada,
                                            false,
                                            modelosHijosAgregados,
                                            proveedorIconosCategoriasFiltrado
                                                       ),
                                    generarItemFiltrado(
                                            "Accesos",
                                            MenuFiltradoFondosUI.CriterioFiltrado.EsAcceso,
                                            false,
                                            modelosHijosAgregados,
                                            proveedorIconosCategoriasFiltrado
                                                       ),
                                    generarItemFiltrado(
                                            "Dinero",
                                            MenuFiltradoFondosUI.CriterioFiltrado.EsDinero,
                                            false,
                                            modelosHijosAgregados,
                                            proveedorIconosCategoriasFiltrado
                                                       )
                                                                                              )
                    it.forEach {
                        val criterio =
                                MenuFiltradoFondosUI.CriterioFiltrado.TieneCategoriaSku(it.id!!, it.idsDeAncestros)

                        itemsFiltrado.add(
                                generarItemFiltrado(
                                        it.nombre,
                                        criterio,
                                        false,
                                        modelosHijosAgregados,
                                        proveedorIconosCategoriasFiltrado
                                                   )
                                         )
                    }

                    for (itemFiltradoFondoUI in itemsFiltrado)
                    {
                        itemFiltradoFondoUI.filtroActivado.subscribe {
                            for (itemADesactivar in itemsFiltrado.asSequence().filter { it != itemFiltradoFondoUI })
                            {
                                itemADesactivar.cambiarEstado(false)
                            }
                        }.addTo(disposables)
                    }

                    itemsFiltrado.toList()
                }.cache()

    override fun finalizarProceso()
    {
        disposables.dispose()
        super.finalizarProceso()
    }

    class ItemFiltradoFondo<TipoIcono : ProveedorIconosCategoriasFiltrado.Icono>(
            override val nombreFiltrado: String,
            override val icono: TipoIcono,
            override val criterioFiltrado: MenuFiltradoFondosUI.CriterioFiltrado,
            estadoInicial: Boolean)
        : MenuFiltradoFondosUI.ItemFiltradoFondoUI<TipoIcono>
    {
        private var estadoActual = BehaviorSubject.createDefault(estadoInicial)

        override val estado: Observable<Boolean> = estadoActual.hide().distinctUntilChanged()

        private var eventosDeActivacion = BehaviorSubject.create<MenuFiltradoFondosUI.CriterioFiltrado>()
        override val filtroActivado = eventosDeActivacion.hide()!!

        override val observadoresInternos: List<Observer<*>> = listOf(estadoActual, eventosDeActivacion)
        override val modelosHijos: List<ModeloUI> = emptyList()

        override fun cambiarEstado(estadoNuevo: Boolean)
        {
            estadoActual.onNext(estadoNuevo)
        }

        override fun activarFiltro()
        {
            eventosDeActivacion.onNext(criterioFiltrado)
            cambiarEstado(true)
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ItemFiltradoFondo<*>

            if (nombreFiltrado != other.nombreFiltrado) return false

            return true
        }

        override fun hashCode(): Int
        {
            return nombreFiltrado.hashCode()
        }

        override fun toString(): String
        {
            return "ItemFiltradoFondo(nombreFiltrado='$nombreFiltrado', estadoActual=${estadoActual.value})"
        }
    }
}