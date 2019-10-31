package co.smartobjects.red.modelos.usuarios

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class PermisoBackDTO @JsonCreator internal constructor(
        @get:JsonProperty("client-id")
        @param:JsonProperty("client-id")
        val idCliente: Long = 0L,

        @get:JsonProperty("endpoint", required = true)
        @param:JsonProperty("endpoint", required = true)
        val endpoint: String,

        @get:JsonProperty("action", required = true)
        @param:JsonProperty("action", required = true)
        val accion: AccionDTO
                                                           ) :
        EntidadDTO<PermisoBack>
{
    object CodigosError : CodigosErrorDTO(80200)
    {
        val ENDPOINT_INVALIDO = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
    }

    constructor(permiso: PermisoBack) : this(permiso.idCliente, permiso.endPoint, AccionDTO.desdeNegocio(permiso.accion))

    override fun aEntidadDeNegocio(): PermisoBack
    {
        return PermisoBack(idCliente, endpoint, accion.valorEnNegocio)
    }

    enum class AccionDTO(val valorEnNegocio: PermisoBack.Accion, val valorEnRed: String)
    {
        GET_TODOS(PermisoBack.Accion.GET_TODOS, "GET_ALL"),
        GET_UNO(PermisoBack.Accion.GET_UNO, "GET_ONE"),
        PUT(PermisoBack.Accion.PUT, "PUT"),
        PUT_TODOS(PermisoBack.Accion.PUT_TODOS, "PUT_TODOS"),
        POST(PermisoBack.Accion.POST, "POST"),
        PATCH(PermisoBack.Accion.PATCH, "PATCH"),
        DELETE(PermisoBack.Accion.DELETE, "DELETE");

        companion object
        {
            fun desdeNegocio(valorEnNegocio: PermisoBack.Accion): AccionDTO
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }
}