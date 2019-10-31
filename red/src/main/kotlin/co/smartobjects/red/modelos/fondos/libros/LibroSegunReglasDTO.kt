package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroSegunReglas
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class LibroSegunReglasDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id)
        @param:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.nombre, required = true)
        @param:JsonProperty(PropiedadesJSON.nombre, required = true)
        val nombre: String,

        @get:JsonProperty(PropiedadesJSON.idLibro, required = true)
        @param:JsonProperty(PropiedadesJSON.idLibro, required = true)
        val idLibro: Long,

        @get:JsonProperty(PropiedadesJSON.reglasIdUbicacion, required = true)
        @param:JsonProperty(PropiedadesJSON.reglasIdUbicacion, required = true)
        val reglasIdUbicacion: List<ReglaDeIdUbicacionDTO>,

        @get:JsonProperty(PropiedadesJSON.reglasIdGrupoDeClientes, required = true)
        @param:JsonProperty(PropiedadesJSON.reglasIdGrupoDeClientes, required = true)
        val reglasIdGrupoDeClientes: List<ReglaDeIdGrupoDeClientesDTO>,

        @get:JsonProperty(PropiedadesJSON.reglasIdPaquete, required = true)
        @param:JsonProperty(PropiedadesJSON.reglasIdPaquete, required = true)
        val reglasIdPaquete: List<ReglaDeIdPaqueteDTO>
) : EntidadDTO<LibroSegunReglas>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val nombre = "name"
        const val idLibro = "book-id"
        const val reglasIdUbicacion = "rules-by-location-id"
        const val reglasIdGrupoDeClientes = "rules-by-clients-group-id"
        const val reglasIdPaquete = "rules-by-package-id"
    }

    object CodigosError : CodigosErrorDTO(40700)
    {
        const val NOMBRE_INVALIDO = 40741
        const val EL_LIBRO_ASOCIADO_REPITE_REGLAS = 40742
    }

    constructor(libroSegunReglas: LibroSegunReglas) :
            this(
                    libroSegunReglas.idCliente,
                    libroSegunReglas.id,
                    libroSegunReglas.nombre,
                    libroSegunReglas.idLibro,
                    libroSegunReglas.reglasIdUbicacion.map { ReglaDeIdUbicacionDTO(it) },
                    libroSegunReglas.reglasIdGrupoDeClientes.map { ReglaDeIdGrupoDeClientesDTO(it) },
                    libroSegunReglas.reglasIdPaquete.map { ReglaDeIdPaqueteDTO(it) }
                )

    override fun aEntidadDeNegocio(): LibroSegunReglas
    {
        return LibroSegunReglas(
                idCliente,
                id,
                nombre,
                idLibro,
                reglasIdUbicacion.map { it.aEntidadDeNegocio() }.toMutableSet(),
                reglasIdGrupoDeClientes.map { it.aEntidadDeNegocio() }.toMutableSet(),
                reglasIdPaquete.map { it.aEntidadDeNegocio() }.toMutableSet()
                               )
    }
}