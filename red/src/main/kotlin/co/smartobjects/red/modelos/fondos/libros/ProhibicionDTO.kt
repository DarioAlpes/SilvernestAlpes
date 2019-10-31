package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.Prohibicion
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = IProhibicionDTO.PropiedadesJSON.tipo,
        visible = true
             )
@JsonSubTypes(
        JsonSubTypes.Type(value = ProhibicionDeFondoDTO::class, name = IProhibicionDTO.Tipo.NOMBRE_EN_RED_FONDO),
        JsonSubTypes.Type(value = ProhibicionDePaqueteDTO::class, name = IProhibicionDTO.Tipo.NOMBRE_EN_RED_PAQUETE)
             )
interface IProhibicionDTO : EntidadDTO<Prohibicion>
{
    @get:JsonProperty(value = PropiedadesJSON.tipo, required = true)
    val tipo: IProhibicionDTO.Tipo

    @get:JsonProperty(value = PropiedadesJSON.idProhibido, required = true)
    val idProhibido: Long

    object CodigosError : CodigosErrorDTO(40600)

    companion object
    {
        fun aIProhibicionDTO(prohibicion: Prohibicion): IProhibicionDTO
        {
            return when (prohibicion)
            {
                is Prohibicion.DeFondo   -> ProhibicionDeFondoDTO(prohibicion)
                is Prohibicion.DePaquete -> ProhibicionDePaqueteDTO(prohibicion)
                else                     -> throw RuntimeException("No existe conversión apropiada para la prohibición '${prohibicion.javaClass.canonicalName}'")
            }
        }
    }

    object PropiedadesJSON
    {
        const val tipo = "prohibition-type"
        const val idProhibido = "prohibition-id"
    }

    enum class Tipo(val valorEnRed: String)
    {
        FONDO("FUND"), PAQUETE("PACKAGE"), DESCONOCIDO("UNKNOWN");

        companion object
        {
            const val NOMBRE_EN_RED_FONDO = "FUND"
            const val NOMBRE_EN_RED_PAQUETE = "PACKAGE"
        }
    }
}

data class ProhibicionDeFondoDTO @JsonCreator internal constructor
(
        @param:JsonProperty(IProhibicionDTO.PropiedadesJSON.idProhibido, required = true)
        override val idProhibido: Long
) : IProhibicionDTO
{
    @get:JsonProperty(IProhibicionDTO.PropiedadesJSON.tipo, required = true)
    override val tipo: IProhibicionDTO.Tipo = IProhibicionDTO.Tipo.FONDO

    constructor(prohibicion: Prohibicion.DeFondo) : this(prohibicion.id)

    override fun aEntidadDeNegocio(): Prohibicion.DeFondo
    {
        return Prohibicion.DeFondo(idProhibido)
    }
}

data class ProhibicionDePaqueteDTO @JsonCreator internal constructor
(
        @param:JsonProperty(IProhibicionDTO.PropiedadesJSON.idProhibido, required = true)
        override val idProhibido: Long
) : IProhibicionDTO
{
    @get:JsonProperty(IProhibicionDTO.PropiedadesJSON.tipo, required = true)
    override val tipo: IProhibicionDTO.Tipo = IProhibicionDTO.Tipo.PAQUETE

    constructor(prohibicion: Prohibicion.DePaquete) : this(prohibicion.id)

    override fun aEntidadDeNegocio(): Prohibicion.DePaquete
    {
        return Prohibicion.DePaquete(idProhibido)
    }
}