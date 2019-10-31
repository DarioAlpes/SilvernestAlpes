package co.smartobjects.prompterbackend.serviciosrest.fondos.precios

import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.precios.ImpuestoDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoImpuestos(override val idCliente: Long, private val repositorio: RepositorioImpuestos, override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionEnClienteAutoasignandoId<Impuesto, Long?, ImpuestoDTO>,
      RecursoListarTodosDeCliente<Impuesto, ImpuestoDTO>
{
    companion object
    {
        const val RUTA = "taxes"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Impuestos"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = ImpuestoDTO.CodigosError
    override val nombreEntidad: String = Impuesto.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Impuesto): ImpuestoDTO
    {
        return ImpuestoDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Impuesto): Impuesto
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Impuesto>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoImpuesto
    {
        return RecursoImpuesto(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoImpuesto(override val id: Long)
        : RecursoActualizableDeCliente<Impuesto, ImpuestoDTO, Long>,
          RecursoConsultarUnoDeCliente<Impuesto, ImpuestoDTO, Long>,
          RecursoEliminarPorIdDeCliente<Impuesto, ImpuestoDTO, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoImpuestos.codigosError
        override val nombreEntidad: String = this@RecursoImpuestos.nombreEntidad
        override val idCliente: Long = this@RecursoImpuestos.idCliente
        private val repositorio: RepositorioImpuestos = this@RecursoImpuestos.repositorio
        override val nombrePermiso: String = this@RecursoImpuestos.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoImpuestos.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: ImpuestoDTO): ImpuestoDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: Impuesto): Impuesto
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Impuesto?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Impuesto): ImpuestoDTO
        {
            return this@RecursoImpuestos.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }
    }
}