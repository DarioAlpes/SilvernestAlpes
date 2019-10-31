package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioUbicacionesContabilizables
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoListarTodosNoDTODeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoReemplazableAColeccionNoDTODeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionTodosSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.ubicaciones.contabilizables.UbicacionesContabilizablesDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoUbicacionesContabilizables
(
        override val idCliente: Long,
        private val repositorio: RepositorioUbicacionesContabilizables,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoReemplazableAColeccionNoDTODeCliente<UbicacionesContabilizables, UbicacionesContabilizablesDTO, Long>,
    RecursoListarTodosNoDTODeCliente<UbicacionesContabilizables, Long>
{
    companion object
    {
        const val RUTA = "countable-locations"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Ubicaciones Contabilizables"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_TODOS = darInformacionPermisoParaActualizacionTodosSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = UbicacionesContabilizablesDTO.CodigosError
    override val nombreEntidad: String = UbicacionesContabilizables.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO


    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: UbicacionesContabilizables): List<Long>
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Long>
    {
        return repositorio.listar(idCliente)
    }
}