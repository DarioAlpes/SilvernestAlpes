package co.smartobjects.prompterbackend.serviciosrest.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Sku
import co.smartobjects.persistencia.fondos.skus.RepositorioSkus
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import co.smartobjects.red.modelos.fondos.SkuDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoSkus
(
        override val idCliente: Long,
        private val repositorio: RepositorioSkus,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoCreacionEnClienteAutoasignandoId<Sku, Long?, SkuDTO>,
    RecursoListarTodosDeCliente<Sku, SkuDTO>
{
    companion object
    {
        const val RUTA = "skus"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Skus"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = SkuDTO.CodigosError
    override val nombreEntidad: String = Sku.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Sku): SkuDTO
    {
        return SkuDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Sku): Sku
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Sku>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoSku
    {
        return RecursoSku(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoSku(override val id: Long)
        : RecursoActualizableDeCliente<Sku, SkuDTO, Long>,
          RecursoConsultarUnoDeCliente<Sku, SkuDTO, Long>,
          RecursoEliminarPorIdDeCliente<Sku, SkuDTO, Long>,
          RecursoActualizablePorCamposDeCliente<Sku, FondoDisponibleParaLaVentaDTO<Sku>, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoSkus.codigosError
        override val nombreEntidad: String = this@RecursoSkus.nombreEntidad
        override val idCliente: Long = this@RecursoSkus.idCliente
        private val repositorio: RepositorioSkus = this@RecursoSkus.repositorio
        override val nombrePermiso: String = this@RecursoSkus.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoSkus.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: SkuDTO): SkuDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: Sku): Sku
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Sku?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Sku): SkuDTO
        {
            return this@RecursoSkus.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: Long, camposModificables: Map<String, CampoModificable<Sku, *>>)
        {
            repositorio.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}