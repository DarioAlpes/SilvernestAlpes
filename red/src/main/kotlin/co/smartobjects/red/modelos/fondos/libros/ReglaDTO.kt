package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.Regla
import co.smartobjects.entidades.fondos.libros.ReglaDeIdGrupoDeClientes
import co.smartobjects.entidades.fondos.libros.ReglaDeIdPaquete
import co.smartobjects.entidades.fondos.libros.ReglaDeIdUbicacion
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = IReglaDTO.PropiedadesJSON.tipo,
        visible = true
             )
@JsonSubTypes(
        JsonSubTypes.Type(value = ReglaDeIdUbicacionDTO::class, name = IReglaDTO.Tipo.NOMBRE_EN_RED_ID_UBICACION),
        JsonSubTypes.Type(value = ReglaDeIdGrupoDeClientesDTO::class, name = IReglaDTO.Tipo.NOMBRE_EN_RED_ID_GRUPO_DE_CLIENTES),
        JsonSubTypes.Type(value = ReglaDeIdPaqueteDTO::class, name = IReglaDTO.Tipo.NOMBRE_EN_RED_ID_PAQUETE)
             )
interface IReglaDTO<TipoRestringido, out TipoRegla : Regla<TipoRestringido>> : EntidadDTO<TipoRegla>
{
    @get:JsonProperty(value = PropiedadesJSON.tipo, required = true)
    val tipo: IReglaDTO.Tipo

    @get:JsonProperty(value = PropiedadesJSON.idRestringido, required = true)
    val idRestringido: Long

    object CodigosError : CodigosErrorDTO(40800)

    companion object
    {
        fun aIReglaDTO(regla: Regla<*>): IReglaDTO<*, *>
        {
            return when (regla)
            {
                is ReglaDeIdUbicacion       -> ReglaDeIdUbicacionDTO(regla)
                is ReglaDeIdGrupoDeClientes -> ReglaDeIdGrupoDeClientesDTO(regla)
                is ReglaDeIdPaquete         -> ReglaDeIdPaqueteDTO(regla)
            }
        }
    }

    object PropiedadesJSON
    {
        const val tipo = "rule-type"
        const val idRestringido = "restricted-id"
    }

    enum class Tipo(val valorEnRed: String)
    {
        ID_UBICACION("LOCATION_ID"), ID_GRUPO_DE_CLIENTES("CLIENTS_GROUP_ID"), ID_PAQUETE("PACKAGE_ID"), DESCONOCIDO("UNKNOWN");

        companion object
        {
            const val NOMBRE_EN_RED_ID_UBICACION = "LOCATION_ID"
            const val NOMBRE_EN_RED_ID_GRUPO_DE_CLIENTES = "CLIENTS_GROUP_ID"
            const val NOMBRE_EN_RED_ID_PAQUETE = "PACKAGE_ID"
        }
    }
}

data class ReglaDeIdUbicacionDTO @JsonCreator internal constructor
(
        @param:JsonProperty(IReglaDTO.PropiedadesJSON.idRestringido, required = true)
        override val idRestringido: Long
) : IReglaDTO<Long, ReglaDeIdUbicacion>
{
    @get:JsonProperty(IReglaDTO.PropiedadesJSON.tipo, required = true)
    override val tipo: IReglaDTO.Tipo = IReglaDTO.Tipo.ID_UBICACION

    constructor(regla: ReglaDeIdUbicacion) : this(regla.restriccion)

    override fun aEntidadDeNegocio(): ReglaDeIdUbicacion
    {
        return ReglaDeIdUbicacion(idRestringido)
    }
}

data class ReglaDeIdGrupoDeClientesDTO @JsonCreator internal constructor
(
        @param:JsonProperty(IReglaDTO.PropiedadesJSON.idRestringido, required = true)
        override val idRestringido: Long
) : IReglaDTO<Long, ReglaDeIdGrupoDeClientes>
{
    @get:JsonProperty(IReglaDTO.PropiedadesJSON.tipo, required = true)
    override val tipo: IReglaDTO.Tipo = IReglaDTO.Tipo.ID_GRUPO_DE_CLIENTES

    constructor(regla: ReglaDeIdGrupoDeClientes) : this(regla.restriccion)

    override fun aEntidadDeNegocio(): ReglaDeIdGrupoDeClientes
    {
        return ReglaDeIdGrupoDeClientes(idRestringido)
    }
}

data class ReglaDeIdPaqueteDTO @JsonCreator internal constructor
(
        @param:JsonProperty(IReglaDTO.PropiedadesJSON.idRestringido, required = true)
        override val idRestringido: Long
) : IReglaDTO<Long, ReglaDeIdPaquete>
{
    @get:JsonProperty(IReglaDTO.PropiedadesJSON.tipo, required = true)
    override val tipo: IReglaDTO.Tipo = IReglaDTO.Tipo.ID_PAQUETE

    constructor(regla: ReglaDeIdPaquete) : this(regla.restriccion)

    override fun aEntidadDeNegocio(): ReglaDeIdPaquete
    {
        return ReglaDeIdPaquete(idRestringido)
    }
}