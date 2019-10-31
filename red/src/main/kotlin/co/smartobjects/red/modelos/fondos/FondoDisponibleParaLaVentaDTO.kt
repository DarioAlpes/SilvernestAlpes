package co.smartobjects.red.modelos.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.red.modelos.EntidadDTOParcial
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class FondoDisponibleParaLaVentaDTO<T : Fondo<T>> @JsonCreator constructor
(
        @get:JsonProperty(value = IFondoDTO.PropiedadesJSON.disponibleParaLaVenta, required = true)
        val disponibleParaLaVenta: Boolean
) : EntidadDTOParcial<T>
{
    override fun aConjuntoCamposModificables(): Set<CampoModificable<T, *>>
    {
        return setOf(Fondo.CampoDisponibleParaLaVenta(disponibleParaLaVenta))
    }
}