package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class ListaFondosDTO @JsonCreator constructor
(
        @get:JsonProperty(PropiedadesJSON.fondos, required = true)
        @param:JsonProperty(PropiedadesJSON.fondos, required = true)
        val fondos: List<IFondoDTO<*>>
) : EntidadDTO<List<Fondo<*>>>
{
    internal object PropiedadesJSON
    {
        const val fondos = "funds"
    }

    override fun aEntidadDeNegocio(): List<Fondo<*>>
    {
        return fondos.map {
            WrapperDeserilizacionFondoDTO.deserializarSegunTipoFondo(it)
        }
    }
}