package co.smartobjects.logica.fondos

import co.smartobjects.entidades.fondos.CategoriaSku

interface ProveedorCategoriasPadres
{
    fun darPadres(idCategoria: Long): LinkedHashSet<Long>?
}

class ProveedorCategoriasPadresEnMemoria(private val categoriasSku: Sequence<CategoriaSku>)
    : ProveedorCategoriasPadres
{
    override fun darPadres(idCategoria: Long): LinkedHashSet<Long>?
    {
        return categoriasSku.find { it.id == idCategoria }?.idsDeAncestros
    }
}