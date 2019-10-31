package co.smartobjects.red.modelos.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.red.modelos.EntidadDTOParcial
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class PaquetePatchDTO @JsonCreator constructor
(
        @get:JsonProperty(PaqueteDTO.PropiedadesJSON.nombre)
        val nombre: String? = null,

        @get:JsonProperty(PaqueteDTO.PropiedadesJSON.descripcion)
        val descripcion: String? = null,

        @get:JsonProperty(PaqueteDTO.PropiedadesJSON.disponibleParaLaVenta)
        val disponibleParaLaVenta: Boolean? = null
) : EntidadDTOParcial<Paquete>
{
    override fun aConjuntoCamposModificables(): Set<CampoModificable<Paquete, *>>
    {
        val camposAActualizar = mutableSetOf<CampoModificable<Paquete, *>>()

        if (nombre != null)
        {
            camposAActualizar.add(Paquete.CampoNombre(nombre))
        }

        if (descripcion != null)
        {
            camposAActualizar.add(Paquete.CampoDescripcion(descripcion))
        }

        if (disponibleParaLaVenta != null)
        {
            camposAActualizar.add(Paquete.CampoDisponibleParaLaVenta(disponibleParaLaVenta))
        }

        return camposAActualizar
    }
}