package co.smartobjects.prompterbackend.serviciosrest.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenes
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoConsultarUnoDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoEliminarPorIdDeClienteConRestriccion
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoListarTodosDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaEliminacionSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoOrdenes(
        override val idCliente: Long,
        val repositorioOrdenes: RepositorioOrdenes,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoListarTodosDeCliente<Orden, OrdenDTO>
{
    companion object
    {
        const val RUTA = "orders"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Ordenes"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = OrdenDTO.CodigosError
    override val nombreEntidad: String = Orden.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Orden): OrdenDTO
    {
        return OrdenDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Orden>
    {
        return repositorioOrdenes.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoOrden
    {
        return RecursoOrden(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoOrden(override val id: Long)
        : RecursoConsultarUnoDeCliente<Orden, OrdenDTO, Long>,
          RecursoEliminarPorIdDeClienteConRestriccion<Orden, OrdenDTO, Long>
    {
        override val codigoDeErrorRestriccionIncumplidaEliminacion: Int = OrdenDTO.CodigosError.ESTA_MARCADA_CON_CREACION_TERMINADA
        override val codigosError: CodigosErrorDTO = this@RecursoOrdenes.codigosError
        override val nombreEntidad: String = this@RecursoOrdenes.nombreEntidad
        override val idCliente: Long = this@RecursoOrdenes.idCliente
        override val nombrePermiso: String = this@RecursoOrdenes.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoOrdenes.manejadorSeguridad

        override fun transformarHaciaDTO(entidadDeNegocio: Orden): OrdenDTO
        {
            return this@RecursoOrdenes.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Orden?
        {
            return repositorioOrdenes.buscarPorId(idCliente, id)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorioOrdenes.eliminarPorId(idCliente, id)
        }
    }
}