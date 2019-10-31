package co.smartobjects.red.modelos.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ClienteDTO @JsonCreator internal constructor(
        @get:JsonProperty(PropiedadesJSON.id)
        @param:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.nombre, required = true)
        @param:JsonProperty(PropiedadesJSON.nombre, required = true)
        val nombre: String
                                                       ) :
        EntidadDTO<Cliente>
{
    internal object PropiedadesJSON
    {
        const val id = "id"
        const val nombre = "name"
    }

    object CodigosError : CodigosErrorDTO(10000)
    {
        val NOMBRE_INVALIDO = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
    }

    constructor(cliente: Cliente) : this(cliente.id, cliente.nombre)

    override fun aEntidadDeNegocio(): Cliente
    {
        return Cliente(id, nombre)
    }
}