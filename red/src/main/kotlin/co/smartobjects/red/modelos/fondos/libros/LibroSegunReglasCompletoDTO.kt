package co.smartobjects.red.modelos.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.entidades.fondos.libros.LibroSegunReglasCompleto
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class LibroSegunReglasCompletoDTO @JsonCreator internal constructor
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

        @get:JsonProperty(PropiedadesJSON.libro, required = true)
        @param:JsonProperty(PropiedadesJSON.libro, required = true)
        val libro: ILibroDTO<*>,

        @get:JsonProperty(PropiedadesJSON.reglasIdUbicacion, required = true)
        @param:JsonProperty(PropiedadesJSON.reglasIdUbicacion, required = true)
        val reglasIdUbicacion: List<ReglaDeIdUbicacionDTO>,

        @get:JsonProperty(PropiedadesJSON.reglasIdGrupoDeClientes, required = true)
        @param:JsonProperty(PropiedadesJSON.reglasIdGrupoDeClientes, required = true)
        val reglasIdGrupoDeClientes: List<ReglaDeIdGrupoDeClientesDTO>,

        @get:JsonProperty(PropiedadesJSON.reglasIdPaquete, required = true)
        @param:JsonProperty(PropiedadesJSON.reglasIdPaquete, required = true)
        val reglasIdPaquete: List<ReglaDeIdPaqueteDTO>
) : EntidadDTO<LibroSegunReglasCompleto<*>>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val nombre = "name"
        const val libro = "book"
        const val reglasIdUbicacion = "rules-by-location-id"
        const val reglasIdGrupoDeClientes = "rules-by-clients-group-id"
        const val reglasIdPaquete = "rules-by-package-id"
    }

    constructor(libroSegunReglas: LibroSegunReglasCompleto<*>) :
            this(
                    libroSegunReglas.idCliente,
                    libroSegunReglas.id,
                    libroSegunReglas.nombre,
                    ILibroDTO.aILibroDTO(libroSegunReglas.libro),
                    libroSegunReglas.reglasIdUbicacion.map { ReglaDeIdUbicacionDTO(it) },
                    libroSegunReglas.reglasIdGrupoDeClientes.map { ReglaDeIdGrupoDeClientesDTO(it) },
                    libroSegunReglas.reglasIdPaquete.map { ReglaDeIdPaqueteDTO(it) }
                )

    override fun aEntidadDeNegocio(): LibroSegunReglasCompleto<*>
    {
        return when (libro.tipo)
        {
            LibroDTO.Tipo.PRECIOS       ->
            {
                LibroSegunReglasCompleto(
                        idCliente,
                        id,
                        nombre,
                        libro.aEntidadDeNegocio() as LibroDePrecios,
                        reglasIdUbicacion.map { it.aEntidadDeNegocio() }.toMutableSet(),
                        reglasIdGrupoDeClientes.map { it.aEntidadDeNegocio() }.toMutableSet(),
                        reglasIdPaquete.map { it.aEntidadDeNegocio() }.toMutableSet()
                                        )
            }
            LibroDTO.Tipo.PROHIBICIONES ->
            {
                LibroSegunReglasCompleto(
                        idCliente,
                        id,
                        nombre,
                        libro.aEntidadDeNegocio() as LibroDeProhibiciones,
                        reglasIdUbicacion.map { it.aEntidadDeNegocio() }.toMutableSet(),
                        reglasIdGrupoDeClientes.map { it.aEntidadDeNegocio() }.toMutableSet(),
                        reglasIdPaquete.map { it.aEntidadDeNegocio() }.toMutableSet()
                                        )
            }
            LibroDTO.Tipo.DESCONOCIDO   ->
            {
                throw RuntimeException("No existe una conversión apropiada para el libro de ${LibroSegunReglasCompletoDTO::class.java.simpleName} a negocio")
            }
        }
    }
}