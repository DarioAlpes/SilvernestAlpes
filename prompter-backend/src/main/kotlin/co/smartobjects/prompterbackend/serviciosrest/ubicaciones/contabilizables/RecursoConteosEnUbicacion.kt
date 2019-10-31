package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioConteosUbicaciones
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoCreacionDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaCreacionSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.ubicaciones.contabilizables.ConteoUbicacionDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoConteosEnUbicacion
(
        override val idCliente: Long,
        private val idUbicacion: Long,
        private val repositorio: RepositorioConteosUbicaciones,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoCreacionDeCliente<ConteoUbicacion, ConteoUbicacionDTO>
{
    companion object
    {
        const val RUTA = "count"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Conteo en Ubicación"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = ConteoUbicacionDTO.CodigosError
    override val nombreEntidad: String = ConteoUbicacion.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO


    override fun transformarHaciaDTO(entidadDeNegocio: ConteoUbicacion): ConteoUbicacionDTO
    {
        return ConteoUbicacionDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: ConteoUbicacion): ConteoUbicacion
    {
        val entidadConUbicacionAjustada = entidad.copiar(idUbicacion = idUbicacion)

        return repositorio.crear(idCliente, entidadConUbicacionAjustada)
    }
}