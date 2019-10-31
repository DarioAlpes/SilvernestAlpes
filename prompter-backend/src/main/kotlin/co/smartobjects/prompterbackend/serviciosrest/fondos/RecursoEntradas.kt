package co.smartobjects.prompterbackend.serviciosrest.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Entrada
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradas
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.EntradaDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoEntradas
(
        override val idCliente: Long,
        private val repositorio: RepositorioEntradas,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoCreacionEnClienteAutoasignandoId<Entrada, Long?, EntradaDTO>,
    RecursoListarTodosDeCliente<Entrada, EntradaDTO>
{
    companion object
    {
        const val RUTA = "entries"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Entradas"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = EntradaDTO.CodigosError
    override val nombreEntidad: String = Entrada.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Entrada): EntradaDTO
    {
        return EntradaDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Entrada): Entrada
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Entrada>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoEntrada
    {
        return RecursoEntrada(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoEntrada(override val id: Long)
        : RecursoActualizableDeCliente<Entrada, EntradaDTO, Long>,
          RecursoConsultarUnoDeCliente<Entrada, EntradaDTO, Long>,
          RecursoEliminarPorIdDeCliente<Entrada, EntradaDTO, Long>,
          RecursoActualizablePorCamposDeCliente<Entrada, FondoDisponibleParaLaVentaDTO<Entrada>, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoEntradas.codigosError
        override val nombreEntidad: String = this@RecursoEntradas.nombreEntidad
        override val idCliente: Long = this@RecursoEntradas.idCliente
        private val repositorio: RepositorioEntradas = this@RecursoEntradas.repositorio
        override val nombrePermiso: String = this@RecursoEntradas.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoEntradas.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: EntradaDTO): EntradaDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: Entrada): Entrada
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Entrada?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Entrada): EntradaDTO
        {
            return this@RecursoEntradas.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: Long, camposModificables: Map<String, CampoModificable<Entrada, *>>)
        {
            repositorio.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}