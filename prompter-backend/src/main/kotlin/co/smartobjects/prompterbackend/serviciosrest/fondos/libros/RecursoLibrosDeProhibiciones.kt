package co.smartobjects.prompterbackend.serviciosrest.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosDeProhibiciones
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.libros.LibroDeProhibicionesDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoLibrosDeProhibiciones(
        override val idCliente: Long,
        private val repositorio: RepositorioLibrosDeProhibiciones,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionEnClienteAutoasignandoId<LibroDeProhibiciones, Long?, LibroDeProhibicionesDTO>,
      RecursoListarTodosDeCliente<LibroDeProhibiciones, LibroDeProhibicionesDTO>
{
    companion object
    {
        const val RUTA = "sales-prohibitions-books"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "LibroDeProhibiciones"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = LibroDeProhibicionesDTO.CodigosError
    override val nombreEntidad: String = LibroDeProhibiciones.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: LibroDeProhibiciones): LibroDeProhibicionesDTO
    {
        return LibroDeProhibicionesDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: LibroDeProhibiciones): LibroDeProhibiciones
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<LibroDeProhibiciones>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoLibroDeProhibiciones
    {
        return RecursoLibroDeProhibiciones(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoLibroDeProhibiciones(override val id: Long)
        : RecursoActualizableDeCliente<LibroDeProhibiciones, LibroDeProhibicionesDTO, Long>,
          RecursoConsultarUnoDeCliente<LibroDeProhibiciones, LibroDeProhibicionesDTO, Long>,
          RecursoEliminarPorIdDeCliente<LibroDeProhibiciones, LibroDeProhibicionesDTO, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoLibrosDeProhibiciones.codigosError
        override val nombreEntidad: String = this@RecursoLibrosDeProhibiciones.nombreEntidad
        override val idCliente: Long = this@RecursoLibrosDeProhibiciones.idCliente
        private val repositorio: RepositorioLibrosDeProhibiciones = this@RecursoLibrosDeProhibiciones.repositorio
        override val nombrePermiso: String = this@RecursoLibrosDeProhibiciones.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoLibrosDeProhibiciones.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: LibroDeProhibicionesDTO): LibroDeProhibicionesDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: LibroDeProhibiciones): LibroDeProhibiciones
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): LibroDeProhibiciones?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: LibroDeProhibiciones): LibroDeProhibicionesDTO
        {
            return this@RecursoLibrosDeProhibiciones.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }
    }
}