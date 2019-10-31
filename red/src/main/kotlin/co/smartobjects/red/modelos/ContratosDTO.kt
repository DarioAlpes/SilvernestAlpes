package co.smartobjects.red.modelos

import co.smartobjects.campos.CampoModificable

interface EntidadDTO<out EntidadDeNegocio>
{
    fun aEntidadDeNegocio(): EntidadDeNegocio
}

interface EntidadDTOParcial<out EntidadDeNegocio>
{
    fun aConjuntoCamposModificables(): Set<CampoModificable<EntidadDeNegocio, *>>

    fun aMapaDeCamposAModificables(): Map<String, CampoModificable<EntidadDeNegocio, *>>
    {
        return aConjuntoCamposModificables().map { it.nombreCampo to it }.toMap()
    }
}