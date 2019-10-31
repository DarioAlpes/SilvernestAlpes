package co.smartobjects.prompterbackend.serviciosrest.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroSegunReglasCompleto
import co.smartobjects.persistencia.fondos.libros.RepositorioLibrosSegunReglasCompleto
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoConsultarUnoDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoListarTodosDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.libros.LibroSegunReglasCompletoDTO
import co.smartobjects.red.modelos.fondos.libros.LibroSegunReglasDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoLibrosSegunReglasCompleto(
        override val idCliente: Long,
        private val repositorio: RepositorioLibrosSegunReglasCompleto,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoListarTodosDeCliente<LibroSegunReglasCompleto<*>, LibroSegunReglasCompletoDTO>
{
    companion object
    {
        const val RUTA = "rules-books-complete"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "LibroSegunReglasCompleto"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = LibroSegunReglasDTO.CodigosError
    override val nombreEntidad: String = LibroSegunReglasCompleto.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: LibroSegunReglasCompleto<*>): LibroSegunReglasCompletoDTO
    {
        return LibroSegunReglasCompletoDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<LibroSegunReglasCompleto<*>>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoLibroSegunReglasCompleto
    {
        return RecursoLibroSegunReglasCompleto(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoLibroSegunReglasCompleto(override val id: Long)
        : RecursoConsultarUnoDeCliente<LibroSegunReglasCompleto<*>, LibroSegunReglasCompletoDTO, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoLibrosSegunReglasCompleto.codigosError
        override val nombreEntidad: String = this@RecursoLibrosSegunReglasCompleto.nombreEntidad
        override val idCliente: Long = this@RecursoLibrosSegunReglasCompleto.idCliente
        private val repositorio: RepositorioLibrosSegunReglasCompleto = this@RecursoLibrosSegunReglasCompleto.repositorio
        override val nombrePermiso: String = this@RecursoLibrosSegunReglasCompleto.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoLibrosSegunReglasCompleto.manejadorSeguridad

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): LibroSegunReglasCompleto<*>?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: LibroSegunReglasCompleto<*>): LibroSegunReglasCompletoDTO
        {
            return this@RecursoLibrosSegunReglasCompleto.transformarHaciaDTO(entidadDeNegocio)
        }
    }
}