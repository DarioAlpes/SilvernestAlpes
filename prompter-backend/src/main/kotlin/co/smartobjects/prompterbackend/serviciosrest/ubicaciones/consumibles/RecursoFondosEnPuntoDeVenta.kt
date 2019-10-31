package co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.persistencia.ubicaciones.consumibles.IdUbicacionConsultaConsumibles
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioFondosEnPuntoDeVenta
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.fondos.RecursoFondos
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoListarTodosDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.FondoDTO
import co.smartobjects.red.modelos.fondos.IFondoDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoFondosEnPuntoDeVenta(
        override val idCliente: Long,
        private val idUbicacion: Long,
        private val repositorioFondosEnPuntoDeVenta: RepositorioFondosEnPuntoDeVenta,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoListarTodosDeCliente<Fondo<*>, IFondoDTO<*>>
{
    companion object
    {
        const val RUTA = RecursoFondos.RUTA
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Fondos En Punto De Venta"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = FondoDTO.CodigosError
    override val nombreEntidad: String = ConsumibleEnPuntoDeVenta.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Fondo<*>): IFondoDTO<*>
    {
        return IFondoDTO.aIFondoDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Fondo<*>>
    {
        return repositorioFondosEnPuntoDeVenta.listarSegunParametros(idCliente, IdUbicacionConsultaConsumibles(idUbicacion))
    }
}