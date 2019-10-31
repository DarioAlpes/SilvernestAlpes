package co.smartobjects.ui.modelos.menufiltrado

import co.smartobjects.entidades.fondos.*
import co.smartobjects.ui.modelos.ModeloUI
import co.smartobjects.ui.modelos.selecciondecreditos.ProductoFondo
import io.reactivex.Observable
import io.reactivex.Single

interface ProveedorIconosCategoriasFiltrado<Tipo : ProveedorIconosCategoriasFiltrado.Icono>
{
    fun darIcono(criterioDeFiltrado: MenuFiltradoFondosUI.CriterioFiltrado): Tipo

    interface Icono
}

interface MenuFiltradoFondosUI<TipoIcono : ProveedorIconosCategoriasFiltrado.Icono> : ModeloUI
{
    val filtrosDisponibles: Single<List<ItemFiltradoFondoUI<TipoIcono>>>

    interface ItemFiltradoFondoUI<TipoIcono : ProveedorIconosCategoriasFiltrado.Icono> : ModeloUI
    {
        val nombreFiltrado: String
        val criterioFiltrado: CriterioFiltrado
        val icono: TipoIcono
        val estado: Observable<Boolean>
        val filtroActivado: Observable<CriterioFiltrado>

        fun cambiarEstado(estadoNuevo: Boolean)
        fun activarFiltro()
    }

    sealed class CriterioFiltrado
    {
        companion object
        {
            fun deProductoFondoACriterioFiltrado(productoFondo: ProductoFondo): CriterioFiltrado
            {
                return when (productoFondo)
                {
                    is ProductoFondo.SinCategoria ->
                    {
                        when (productoFondo.fondo)
                        {
                            is Dinero  -> EsDinero
                            is Entrada -> EsEntrada
                            is Acceso  -> EsAcceso
                            else       -> throw RuntimeException("No existe conversión apropiada para el fondo '${productoFondo.fondo.javaClass.canonicalName}'")
                        }
                    }
                    is ProductoFondo.ConCategoria ->
                    {
                        when (productoFondo.fondo)
                        {
                            is CategoriaSku -> TieneCategoriaSku(productoFondo.fondo.id!!, productoFondo.fondo.idsDeAncestros)
                            is Sku          -> TieneCategoriaSku(productoFondo.fondo.idDeCategoria, productoFondo.categoriasPadres)
                            else            -> throw RuntimeException("No existe conversión apropiada para el fondo '${productoFondo.fondo.javaClass.canonicalName}'")
                        }
                    }
                }
            }
        }

        open fun aplicaFiltro(filtro: CriterioFiltrado): Boolean
        {
            return filtro == Todos || this == filtro
        }

        object Todos : CriterioFiltrado()
        object EsPaquete : CriterioFiltrado()
        object EsEntrada : CriterioFiltrado()
        object EsAcceso : CriterioFiltrado()
        object EsDinero : CriterioFiltrado()
        class TieneCategoriaSku(private val idCategoriaSku: Long?, private val padres: LinkedHashSet<Long>) : CriterioFiltrado()
        {
            override fun aplicaFiltro(filtro: CriterioFiltrado): Boolean
            {
                return super.aplicaFiltro(filtro)
                       || (filtro is TieneCategoriaSku && padres.contains(filtro.idCategoriaSku))
            }

            override fun equals(other: Any?): Boolean
            {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as TieneCategoriaSku

                if (idCategoriaSku != other.idCategoriaSku) return false
                if (padres != other.padres) return false

                return true
            }

            override fun hashCode(): Int
            {
                var result = idCategoriaSku.hashCode()
                result = 31 * result + padres.hashCode()
                return result
            }
        }
    }
}