package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.PrecioEnLibro
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.precios.PrecioDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class LibroDePreciosDTO @JsonCreator internal constructor
(
        @param:JsonProperty(ILibroDTO.PropiedadesJSON.idCliente)
        override val idCliente: Long = 0,

        @param:JsonProperty(ILibroDTO.PropiedadesJSON.id)
        override val id: Long? = null,

        @param:JsonProperty(ILibroDTO.PropiedadesJSON.nombre, required = true)
        override var nombre: String,

        @get:JsonProperty(PropiedadesJSON.precios, required = true)
        @param:JsonProperty(PropiedadesJSON.precios, required = true)
        val precios: List<PrecioDeFondoDTO>
) : ILibroDTO<LibroDePrecios>
{
    internal object PropiedadesJSON
    {
        const val precios = "prices"
    }

    object CodigosError : CodigosErrorDTO(40300)
    {
        const val PRECIOS_VACIOS = 40341
    }

    override val tipo = LibroDTO.Tipo.PRECIOS

    constructor(libroDePrecios: LibroDePrecios) :
            this(
                    libroDePrecios.idCliente,
                    libroDePrecios.id,
                    libroDePrecios.nombre,
                    libroDePrecios.precios.map { PrecioDeFondoDTO(it) }
                )

    override fun aEntidadDeNegocio(): LibroDePrecios
    {
        return LibroDePrecios(
                idCliente,
                id,
                nombre,
                precios.map { it.aEntidadDeNegocio() }.toSet()
                             )
    }

    data class PrecioDeFondoDTO @JsonCreator constructor(
            @get:JsonProperty(PropiedadesJSON.precio, required = true)
            @param:JsonProperty(PropiedadesJSON.precio, required = true)
            val precio: PrecioDTO,

            @get:JsonProperty(PropiedadesJSON.idFondo, required = true)
            @param:JsonProperty(PropiedadesJSON.idFondo, required = true)
            val idFondo: Long
                                                        )
    {
        internal object PropiedadesJSON
        {
            const val precio = "price"
            const val idFondo = "fund-id"
        }

        constructor(precioDeFondo: PrecioEnLibro) :
                this(
                        PrecioDTO(precioDeFondo.precio),
                        precioDeFondo.idFondo
                    )

        fun aEntidadDeNegocio(): PrecioEnLibro
        {
            return PrecioEnLibro(precio.aEntidadDeNegocio(), idFondo)
        }
    }
}