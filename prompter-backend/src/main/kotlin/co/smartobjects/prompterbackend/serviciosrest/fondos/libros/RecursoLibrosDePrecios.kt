package co.smartobjects.prompterbackend.serviciosrest.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosDePrecios
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.libros.LibroDePreciosDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoLibrosDePrecios(
        override val idCliente: Long,
        private val repositorio: RepositorioLibrosDePrecios,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionEnClienteAutoasignandoId<LibroDePrecios, Long?, LibroDePreciosDTO>,
      RecursoListarTodosDeCliente<LibroDePrecios, LibroDePreciosDTO>
{
    companion object
    {
        const val RUTA = "pricing-books"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "LibroDePrecios"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = LibroDePreciosDTO.CodigosError
    override val nombreEntidad: String = LibroDePrecios.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: LibroDePrecios): LibroDePreciosDTO
    {
        return LibroDePreciosDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: LibroDePrecios): LibroDePrecios
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<LibroDePrecios>
    {   println("listarTodosSegunIdCliente")
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoLibroDePrecios
    {
        return RecursoLibroDePrecios(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoLibroDePrecios(override val id: Long)
        : RecursoActualizableDeCliente<LibroDePrecios, LibroDePreciosDTO, Long>,
          RecursoConsultarUnoDeCliente<LibroDePrecios, LibroDePreciosDTO, Long>,
          RecursoEliminarPorIdDeCliente<LibroDePrecios, LibroDePreciosDTO, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoLibrosDePrecios.codigosError
        override val nombreEntidad: String = this@RecursoLibrosDePrecios.nombreEntidad
        override val idCliente: Long = this@RecursoLibrosDePrecios.idCliente
        private val repositorio: RepositorioLibrosDePrecios = this@RecursoLibrosDePrecios.repositorio
        override val nombrePermiso: String = this@RecursoLibrosDePrecios.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoLibrosDePrecios.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: LibroDePreciosDTO): LibroDePreciosDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: LibroDePrecios): LibroDePrecios
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): LibroDePrecios?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: LibroDePrecios): LibroDePreciosDTO
        {
            return this@RecursoLibrosDePrecios.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }
    }
}