package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class GrupoClientesDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.id)
        @param:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.nombre, required = true)
        @param:JsonProperty(PropiedadesJSON.nombre, required = true)
        val nombre: String,

        @get:JsonProperty(PropiedadesJSON.segmentosCliente, required = true)
        @param:JsonProperty(PropiedadesJSON.segmentosCliente, required = true)
        val segmentosCliente: List<SegmentoClientesDTO>
) : EntidadDTO<GrupoClientes>
{
    internal object PropiedadesJSON
    {
        const val id = "id"
        const val nombre = "name"
        const val segmentosCliente = "clients-segments"
    }

    object CodigosError : CodigosErrorDTO(70000)
    {
        const val NOMBRE_INVALIDO = 70041
        const val SEGMENTOS_CLIENTES_INVALIDOS = 70042
        const val MISMOS_SEGMENTOS_OTRO_GRUPO_CLIENTES = 70043
    }

    constructor(grupoClientes: GrupoClientes) :
            this(
                    grupoClientes.id,
                    grupoClientes.nombre,
                    grupoClientes.segmentosClientes.map { SegmentoClientesDTO(it) }
                )

    override fun aEntidadDeNegocio(): GrupoClientes
    {
        return GrupoClientes(id, nombre, segmentosCliente.map { it.aEntidadDeNegocio() })
    }
}