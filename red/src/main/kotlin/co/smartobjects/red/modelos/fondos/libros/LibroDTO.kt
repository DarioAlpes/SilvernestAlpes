package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.Libro
import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = ILibroDTO.PropiedadesJSON.tipo,
        visible = true
             )
@JsonSubTypes(
        JsonSubTypes.Type(value = LibroDePreciosDTO::class, name = LibroDTO.Tipo.NOMBRE_EN_RED_PRECIOS),
        JsonSubTypes.Type(value = LibroDeProhibicionesDTO::class, name = LibroDTO.Tipo.NOMBRE_EN_RED_PROHIBICIONES)
             )
interface ILibroDTO<T : Libro<T>> : EntidadDTO<T>
{
    @get:JsonProperty(value = PropiedadesJSON.tipo, required = true)
    val tipo: LibroDTO.Tipo

    @get:JsonProperty(PropiedadesJSON.idCliente)
    val idCliente: Long

    @get:JsonProperty(PropiedadesJSON.id)
    val id: Long?

    @get:JsonProperty(PropiedadesJSON.nombre, required = true)
    val nombre: String

    companion object
    {
        fun aILibroDTO(libro: Libro<*>): ILibroDTO<*>
        {
            return when (libro)
            {
                is LibroDePrecios       -> LibroDePreciosDTO(libro)
                is LibroDeProhibiciones -> LibroDeProhibicionesDTO(libro)
                else                    -> throw RuntimeException("No existe conversión apropiada para el libro '${libro.javaClass.canonicalName}'")
            }
        }
    }

    object PropiedadesJSON
    {
        const val tipo = "book-type"
        const val idCliente = "client-id"
        const val id = "id"
        const val nombre = "name"
    }
}

data class LibroDTO<LibroConcreto : Libro<LibroConcreto>> @JsonCreator internal constructor
(
        @param:JsonProperty(ILibroDTO.PropiedadesJSON.idCliente)
        override val idCliente: Long,

        @param:JsonProperty(ILibroDTO.PropiedadesJSON.id)
        override val id: Long?,

        @param:JsonProperty(ILibroDTO.PropiedadesJSON.nombre, required = true)
        override val nombre: String,

        @param:JsonProperty(ILibroDTO.PropiedadesJSON.tipo, required = true)
        override val tipo: Tipo
) : ILibroDTO<LibroConcreto>
{
    object CodigosError : CodigosErrorDTO(40200)
    {
        const val NOMBRE_INVALIDO = 40241
    }

    override fun aEntidadDeNegocio(): LibroConcreto
    {
        throw UnsupportedOperationException("No existe suficiente información para instanciar el libro correcto")
    }

    enum class Tipo(val valorEnRed: String)
    {
        PRECIOS("PRICES"), PROHIBICIONES("PROHIBITIONS"), DESCONOCIDO("UNKNOWN");

        companion object
        {
            const val NOMBRE_EN_RED_PRECIOS = "PRICES"
            const val NOMBRE_EN_RED_PROHIBICIONES = "PROHIBITIONS"
        }
    }
}