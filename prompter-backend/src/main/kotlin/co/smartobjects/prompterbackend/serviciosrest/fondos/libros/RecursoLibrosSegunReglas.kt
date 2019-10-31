package co.smartobjects.prompterbackend.serviciosrest.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroSegunReglas
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosSegunReglas
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.libros.LibroSegunReglasDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoLibrosSegunReglas(
        override val idCliente: Long,
        private val repositorio: RepositorioLibrosSegunReglas,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionEnClienteConRestriccionYAutoasignandoId<LibroSegunReglas, Long?, LibroSegunReglasDTO>,
      RecursoListarTodosDeCliente<LibroSegunReglas, LibroSegunReglasDTO>
{
    companion object
    {
        const val RUTA = "rules-books"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "LibroSegunReglas"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigoDeErrorRestriccionIncumplida: Int = LibroSegunReglasDTO.CodigosError.EL_LIBRO_ASOCIADO_REPITE_REGLAS
    override val codigosError: CodigosErrorDTO = LibroSegunReglasDTO.CodigosError
    override val nombreEntidad: String = LibroSegunReglas.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: LibroSegunReglas): LibroSegunReglasDTO
    {
        return LibroSegunReglasDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: LibroSegunReglas): LibroSegunReglas
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<LibroSegunReglas>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoLibroSegunReglas
    {
        return RecursoLibroSegunReglas(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoLibroSegunReglas(override val id: Long)
        : RecursoActualizableConRestriccionDeCliente<LibroSegunReglas, LibroSegunReglasDTO, Long>,
          RecursoConsultarUnoDeCliente<LibroSegunReglas, LibroSegunReglasDTO, Long>,
          RecursoEliminarPorIdDeCliente<LibroSegunReglas, LibroSegunReglasDTO, Long>
    {
        override val codigoDeErrorRestriccionIncumplida: Int = this@RecursoLibrosSegunReglas.codigoDeErrorRestriccionIncumplida
        override val codigosError: CodigosErrorDTO = this@RecursoLibrosSegunReglas.codigosError
        override val nombreEntidad: String = this@RecursoLibrosSegunReglas.nombreEntidad
        override val idCliente: Long = this@RecursoLibrosSegunReglas.idCliente
        private val repositorio: RepositorioLibrosSegunReglas = this@RecursoLibrosSegunReglas.repositorio
        override val nombrePermiso: String = this@RecursoLibrosSegunReglas.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoLibrosSegunReglas.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: LibroSegunReglasDTO): LibroSegunReglasDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: LibroSegunReglas): LibroSegunReglas
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): LibroSegunReglas?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: LibroSegunReglas): LibroSegunReglasDTO
        {
            return this@RecursoLibrosSegunReglas.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }
    }
}