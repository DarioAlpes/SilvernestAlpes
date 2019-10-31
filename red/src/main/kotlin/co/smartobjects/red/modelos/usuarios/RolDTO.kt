package co.smartobjects.red.modelos.usuarios

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class RolDTO @JsonCreator constructor(
        @get:JsonProperty(PropiedadesJSON.nombre, required = true)
        @param:JsonProperty(PropiedadesJSON.nombre, required = true)
        val nombre: String,

        @get:JsonProperty(PropiedadesJSON.descripcion, required = true)
        @param:JsonProperty(PropiedadesJSON.descripcion, required = true)
        val descripcion: String,

        @get:JsonProperty(PropiedadesJSON.permisos, required = true)
        @param:JsonProperty(PropiedadesJSON.permisos, required = true)
        val permisos: List<PermisoBackDTO>
                                          ) :
        EntidadDTO<Rol>
{
    internal object PropiedadesJSON
    {
        const val nombre = "name"
        const val descripcion = "description"
        const val permisos = "permissions"
    }

    object CodigosError : CodigosErrorDTO(80100)
    {
        val NOMBRE_INVALIDO = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
        val DESCRIPCION_INVALIDA = NOMBRE_INVALIDO + 1
        val PERMISOS_INVALIDOS = DESCRIPCION_INVALIDA + 1
    }

    constructor(rol: Rol) : this(rol.nombre, rol.descripcion, rol.permisos.map { PermisoBackDTO(it as PermisoBack) })

    override fun aEntidadDeNegocio(): Rol
    {
        return Rol(nombre, descripcion, permisos.map { it.aEntidadDeNegocio() }.toSet())
    }
}