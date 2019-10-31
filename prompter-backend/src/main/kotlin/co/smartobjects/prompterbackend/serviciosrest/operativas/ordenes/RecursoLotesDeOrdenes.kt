package co.smartobjects.prompterbackend.serviciosrest.operativas.ordenes

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.persistencia.operativas.ordenes.IdTransaccionActualizacionTerminacionOrden
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenes
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoActualizableColeccionDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoActualizablePorCamposDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionParcialSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.ordenes.LoteDeOrdenesDTO
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoLotesDeOrdenes(
        override val idCliente: Long,
        val repositorioOrdenes: RepositorioOrdenes,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoDeCliente
{
    companion object
    {
        const val RUTA = "orders-batches"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "LotesDeOrdenes"
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val nombrePermiso: String = NOMBRE_PERMISO

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: String): RecursoLoteDeOrdenes
    {
        return RecursoLoteDeOrdenes(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoLoteDeOrdenes(override val id: String)
        : RecursoActualizableColeccionDeCliente<LoteDeOrdenes, LoteDeOrdenesDTO, String, Orden, List<Orden>, OrdenDTO>,
          RecursoActualizablePorCamposDeCliente<LoteDeOrdenes, TransaccionEntidadTerminadaDTO<LoteDeOrdenes>, String>
    {
        override val codigosError: CodigosErrorDTO = LoteDeOrdenesDTO.CodigosError
        override val nombreEntidad: String = LoteDeOrdenes.NOMBRE_ENTIDAD
        override val idCliente: Long = this@RecursoLotesDeOrdenes.idCliente
        override val nombrePermiso: String = this@RecursoLotesDeOrdenes.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoLotesDeOrdenes.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: String, dto: LoteDeOrdenesDTO): LoteDeOrdenesDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: String, entidad: LoteDeOrdenes): List<Orden>
        {
            return repositorioOrdenes.crear(idCliente, entidad)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: List<Orden>): Collection<OrdenDTO>
        {
            return entidadDeNegocio.map { OrdenDTO(it) }
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: String, camposModificables: Map<String, CampoModificable<LoteDeOrdenes, *>>)
        {
            repositorioOrdenes.actualizarCamposIndividuales(idCliente, camposModificables, IdTransaccionActualizacionTerminacionOrden(id))
        }
    }
}