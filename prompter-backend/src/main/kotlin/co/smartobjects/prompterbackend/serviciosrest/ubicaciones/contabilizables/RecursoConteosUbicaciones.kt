package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad
import co.smartobjects.persistencia.ubicaciones.contabilizables.FiltroConteosUbicaciones
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioConteosUbicaciones
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoListarTodosDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaEliminacionSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.ubicaciones.contabilizables.ConteoUbicacionDTO
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoConteosUbicaciones
(
        override val idCliente: Long,
        private val repositorio: RepositorioConteosUbicaciones,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoListarTodosDeCliente<ConteoUbicacion, ConteoUbicacionDTO>
{
    companion object
    {
        const val RUTA = "locations-counts"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Conteos de Ubicaciones"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = ConteoUbicacionDTO.CodigosError
    override val nombreEntidad: String = ConteoUbicacion.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO


    override fun transformarHaciaDTO(entidadDeNegocio: ConteoUbicacion): ConteoUbicacionDTO
    {
        return ConteoUbicacionDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<ConteoUbicacion>
    {
        return repositorio.listarSegunParametros(idCliente, FiltroConteosUbicaciones.Todos)
    }

    @DELETE
    fun eliminarTodas()
    {
        ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                manejadorSeguridad,
                INFORMACION_PERMISO_ELIMINACION,
                idCliente
                                                                               )
        {
            try
            {
                val resultadoExitoso = repositorio.eliminarSegunFiltros(idCliente, FiltroConteosUbicaciones.Todos)

                if (!resultadoExitoso)
                {
                    throw co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad("Todos los conteos", codigosError.ERROR_DE_BD_DESCONOCIDO)
                }
            }
            catch (e: ErrorEliminandoEntidad)
            {
                throw co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad(e.entidad, codigosError.ERROR_DE_BD_DESCONOCIDO)
            }
            catch (e: ErrorDeLlaveForanea)
            {
                throw co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad(nombreEntidad, codigosError.ENTIDAD_REFERENCIADA, e)
            }
        }
    }
}