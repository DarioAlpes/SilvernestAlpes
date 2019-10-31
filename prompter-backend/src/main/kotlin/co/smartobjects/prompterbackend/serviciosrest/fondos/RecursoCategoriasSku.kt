package co.smartobjects.prompterbackend.serviciosrest.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.CategoriaSkuDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoCategoriasSku
(
        override val idCliente: Long,
        private val repositorio: RepositorioCategoriasSkus,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoCreacionEnClienteAutoasignandoId<CategoriaSku, Long?, CategoriaSkuDTO>,
    RecursoListarTodosDeCliente<CategoriaSku, CategoriaSkuDTO>
{
    companion object
    {
        const val RUTA = "sku-categories"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Categorias de Skus"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = CategoriaSkuDTO.CodigosError
    override val nombreEntidad: String = CategoriaSku.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: CategoriaSku): CategoriaSkuDTO
    {
        return CategoriaSkuDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: CategoriaSku): CategoriaSku
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<CategoriaSku>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoCategoriaSku
    {
        return RecursoCategoriaSku(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoCategoriaSku(override val id: Long)
        : RecursoActualizableJerarquicoDeCliente<CategoriaSku, CategoriaSkuDTO, Long>,
          RecursoConsultarUnoDeCliente<CategoriaSku, CategoriaSkuDTO, Long>,
          RecursoEliminarPorIdDeCliente<CategoriaSku, CategoriaSkuDTO, Long>,
          RecursoActualizablePorCamposDeCliente<CategoriaSku, FondoDisponibleParaLaVentaDTO<CategoriaSku>, Long>
    {
        override val codigoDeErrorCicloJerarquia = CategoriaSkuDTO.CodigosError.CICLO_JERARQUIA
        override val codigosError: CodigosErrorDTO = this@RecursoCategoriasSku.codigosError
        override val nombreEntidad: String = this@RecursoCategoriasSku.nombreEntidad
        override val idCliente: Long = this@RecursoCategoriasSku.idCliente
        private val repositorio: RepositorioCategoriasSkus = this@RecursoCategoriasSku.repositorio
        override val nombrePermiso: String = this@RecursoCategoriasSku.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoCategoriasSku.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: CategoriaSkuDTO): CategoriaSkuDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: CategoriaSku): CategoriaSkuDTO
        {
            return this@RecursoCategoriasSku.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: CategoriaSku): CategoriaSku
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): CategoriaSku?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: Long, camposModificables: Map<String, CampoModificable<CategoriaSku, *>>)
        {
            repositorio.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}