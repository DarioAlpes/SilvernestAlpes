package co.smartobjects.prompterbackend.serviciosrest.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.DineroDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoMonedas
(
        override val idCliente: Long,
        private val repositorio: RepositorioMonedas,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoCreacionEnClienteAutoasignandoId<Dinero, Long?, DineroDTO>,
    RecursoListarTodosDeCliente<Dinero, DineroDTO>
{
    companion object
    {
        const val RUTA = "currencies"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Monedas"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = DineroDTO.CodigosError
    override val nombreEntidad: String = Dinero.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Dinero): DineroDTO
    {
        return DineroDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Dinero): Dinero
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Dinero>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoMoneda
    {
        return RecursoMoneda(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoMoneda(override val id: Long)
        : RecursoActualizableDeCliente<Dinero, DineroDTO, Long>,
          RecursoConsultarUnoDeCliente<Dinero, DineroDTO, Long>,
          RecursoEliminarPorIdDeCliente<Dinero, DineroDTO, Long>,
          RecursoActualizablePorCamposDeCliente<Dinero, FondoDisponibleParaLaVentaDTO<Dinero>, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoMonedas.codigosError
        override val nombreEntidad: String = this@RecursoMonedas.nombreEntidad
        override val idCliente: Long = this@RecursoMonedas.idCliente
        private val repositorio: RepositorioMonedas = this@RecursoMonedas.repositorio
        override val nombrePermiso: String = this@RecursoMonedas.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoMonedas.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: DineroDTO): DineroDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: Dinero): Dinero
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Dinero?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Dinero): DineroDTO
        {
            return this@RecursoMonedas.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: Long, camposModificables: Map<String, CampoModificable<Dinero, *>>)
        {
            repositorio.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}