package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.red.modelos.EntidadDTOParcial
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class NombreGrupoClientesDTO @JsonCreator constructor
(
        @get:JsonProperty(GrupoClientesDTO.PropiedadesJSON.nombre, required = true)
        @param:JsonProperty(GrupoClientesDTO.PropiedadesJSON.nombre, required = true)
        val nombre: String
) : EntidadDTOParcial<GrupoClientes>
{
    override fun aConjuntoCamposModificables(): Set<CampoModificable<GrupoClientes, *>>
    {
        return setOf(GrupoClientes.CampoNombre(nombre))
    }
}