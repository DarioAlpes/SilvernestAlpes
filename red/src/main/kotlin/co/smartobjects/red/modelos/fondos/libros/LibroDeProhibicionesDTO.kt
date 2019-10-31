package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.red.modelos.CodigosErrorDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class LibroDeProhibicionesDTO @JsonCreator internal constructor
(
        @param:JsonProperty(ILibroDTO.PropiedadesJSON.idCliente)
        override val idCliente: Long = 0,

        @param:JsonProperty(ILibroDTO.PropiedadesJSON.id)
        override val id: Long? = null,

        @param:JsonProperty(ILibroDTO.PropiedadesJSON.nombre, required = true)
        override var nombre: String,

        @get:JsonProperty(PropiedadesJSON.prohibicionesDeFondo, required = true)
        @param:JsonProperty(PropiedadesJSON.prohibicionesDeFondo, required = true)
        val prohibicionesDeFondo: List<ProhibicionDeFondoDTO>,

        @get:JsonProperty(PropiedadesJSON.prohibicionesDePaquete, required = true)
        @param:JsonProperty(PropiedadesJSON.prohibicionesDePaquete, required = true)
        val prohibicionesDePaquete: List<ProhibicionDePaqueteDTO>
) : ILibroDTO<LibroDeProhibiciones>
{
    internal object PropiedadesJSON
    {
        const val prohibicionesDeFondo = "funds-prohibitions"
        const val prohibicionesDePaquete = "packages-prohibitions"
    }

    object CodigosError : CodigosErrorDTO(40500)
    {
        const val NO_HAY_PROHIBICIONES_DE_NINGUN_TIPO = 40541
    }

    override val tipo = LibroDTO.Tipo.PROHIBICIONES

    constructor(libroDeProhibiciones: LibroDeProhibiciones) :
            this(
                    libroDeProhibiciones.idCliente,
                    libroDeProhibiciones.id,
                    libroDeProhibiciones.nombre,
                    libroDeProhibiciones.prohibicionesDeFondo.map { ProhibicionDeFondoDTO(it) },
                    libroDeProhibiciones.prohibicionesDePaquete.map { ProhibicionDePaqueteDTO(it) }
                )

    override fun aEntidadDeNegocio(): LibroDeProhibiciones
    {
        return LibroDeProhibiciones(
                idCliente,
                id,
                nombre,
                prohibicionesDeFondo.map { it.aEntidadDeNegocio() }.toSet(),
                prohibicionesDePaquete.map { it.aEntidadDeNegocio() }.toSet()
                                   )
    }
}