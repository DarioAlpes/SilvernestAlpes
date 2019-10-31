package co.smartobjects.prompterbackend.serviciosrest.operativas.compras

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.persistencia.operativas.compras.RepositorioCompras
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoCompras(
        override val idCliente: Long,
        val repositorioCompras: RepositorioCompras,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoListarTodosDeCliente<Compra, CompraDTO>
{
    companion object
    {
        const val RUTA = "purchases"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Compras"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = CompraDTO.CodigosError
    override val nombreEntidad: String = Compra.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Compra): CompraDTO
    {
        return CompraDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Compra>
    {
        return repositorioCompras.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: String): RecursoCompra
    {
        return RecursoCompra(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoCompra(override val id: String)
        : RecursoConsultarUnoDeCliente<Compra, CompraDTO, String>,
          RecursoEliminarPorIdDeCliente<Compra, CompraDTO, String>,
          RecursoActualizableConRestriccionDeCliente<Compra, CompraDTO, String>,
          RecursoActualizablePorCamposDeCliente<Compra, TransaccionEntidadTerminadaDTO<Compra>, String>
    {
        override val codigoDeErrorRestriccionIncumplida: Int = CompraDTO.CodigosError.PAGOS_CON_NUMERO_DE_TRANSACCION_POS_REPETIDOS
        override val codigosError: CodigosErrorDTO = this@RecursoCompras.codigosError
        override val nombreEntidad: String = this@RecursoCompras.nombreEntidad
        override val idCliente: Long = this@RecursoCompras.idCliente
        override val nombrePermiso: String = this@RecursoCompras.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoCompras.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: String, dto: CompraDTO): CompraDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: String, entidad: Compra): Compra
        {
            return repositorioCompras.crear(idCliente, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: String): Compra?
        {
            return repositorioCompras.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Compra): CompraDTO
        {
            return this@RecursoCompras.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: String): Boolean
        {
            return repositorioCompras.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: String, camposModificables: Map<String, CampoModificable<Compra, *>>)
        {
            repositorioCompras.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}