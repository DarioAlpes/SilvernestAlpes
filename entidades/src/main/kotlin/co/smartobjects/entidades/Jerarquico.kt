package co.smartobjects.entidades

import java.util.*

interface Jerarquico<EntidadDeNegocio : EntidadReferenciable<Long?, EntidadDeNegocio>>
    : EntidadReferenciable<Long?, EntidadDeNegocio>
{
    companion object
    {
        const val SEPARADOR_LLAVES_JERARQUIAS = ":"
    }

    val idsDeAncestros: LinkedHashSet<Long>
    val idDelPadre: Long?

    fun darLlaveJerarquia(): String
    {
        return idsAncestorsEIdALlave(idsDeAncestros, id)
    }
}

fun idsAncestorsEIdALlave(idsDeAncestros: LinkedHashSet<Long>, id: Long?): String
{
    var cadenaDeAncestros = idsDeAncestros.joinToString(":")

    if (cadenaDeAncestros.isNotEmpty())
    {
        cadenaDeAncestros += ":" + (id ?: 0L)
    }
    else
    {
        cadenaDeAncestros = (id ?: 0L).toString()
    }

    return cadenaDeAncestros
}