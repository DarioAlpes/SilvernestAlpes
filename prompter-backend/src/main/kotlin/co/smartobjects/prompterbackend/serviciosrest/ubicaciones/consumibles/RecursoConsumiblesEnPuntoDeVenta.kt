package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles

import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.persistencia.ubicaciones.consumibles.IdUbicacionConsultaConsumibles
import co.smartobjects.persistencia.ubicaciones.consumibles.ListaConsumiblesEnPuntoDeVentaUbicaciones
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioConsumibleEnPuntoDeVenta
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoCreacionColeccionDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoListarTodosDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionTodosSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.ubicaciones.consumibles.ConsumibleEnPuntoDeVentaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoConsumiblesEnPuntoDeVenta(
        override val idCliente: Long,
        private val idUbicacion: Long,
        private val repositorioConsumiblesEnPuntoDeVenta: RepositorioConsumibleEnPuntoDeVenta,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionColeccionDeCliente<ConsumibleEnPuntoDeVenta, ConsumibleEnPuntoDeVentaDTO, List<ConsumibleEnPuntoDeVentaDTO>>,
      RecursoListarTodosDeCliente<ConsumibleEnPuntoDeVenta, ConsumibleEnPuntoDeVentaDTO>
{
    companion object
    {
        const val RUTA = "consumables"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Consumibles En Punto De Venta"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_TODOS = darInformacionPermisoParaActualizacionTodosSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = ConsumibleEnPuntoDeVentaDTO.CodigosError
    override val nombreEntidad: String = ConsumibleEnPuntoDeVenta.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: ConsumibleEnPuntoDeVenta): ConsumibleEnPuntoDeVentaDTO
    {
        return ConsumibleEnPuntoDeVentaDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: List<ConsumibleEnPuntoDeVenta>): List<ConsumibleEnPuntoDeVenta>
    {
        return repositorioConsumiblesEnPuntoDeVenta.crear(
                idCliente,
                ListaConsumiblesEnPuntoDeVentaUbicaciones(entidad.map { it.copiar(idUbicacion = idUbicacion) }.toSet(), idUbicacion)
                                                         )
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<ConsumibleEnPuntoDeVenta>
    {
        return repositorioConsumiblesEnPuntoDeVenta.listarSegunParametros(idCliente, IdUbicacionConsultaConsumibles(idUbicacion))
    }
}