package co.smartobjects.prompterbackend.serviciosrest.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Acceso
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesos
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.AccesoDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoAccesos(override val idCliente: Long, private val repositorio: RepositorioAccesos, override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionDeCliente<Acceso, AccesoDTO>,
      RecursoListarTodosDeCliente<Acceso, AccesoDTO>
{
    companion object
    {
        const val RUTA = "accesses"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Accesos"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = AccesoDTO.CodigosError
    override val nombreEntidad: String = Acceso.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Acceso): AccesoDTO
    {
        return AccesoDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Acceso): Acceso
    {
        return repositorio.crear(idCliente, entidad.copiar(id = null))
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Acceso>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoAcceso
    {
        return RecursoAcceso(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoAcceso(override val id: Long)
        : RecursoActualizableDeCliente<Acceso, AccesoDTO, Long>,
          RecursoConsultarUnoDeCliente<Acceso, AccesoDTO, Long>,
          RecursoEliminarPorIdDeCliente<Acceso, AccesoDTO, Long>,
          RecursoActualizablePorCamposDeCliente<Acceso, FondoDisponibleParaLaVentaDTO<Acceso>, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoAccesos.codigosError
        override val nombreEntidad: String = this@RecursoAccesos.nombreEntidad
        override val idCliente: Long = this@RecursoAccesos.idCliente
        private val repositorio: RepositorioAccesos = this@RecursoAccesos.repositorio
        override val nombrePermiso: String = this@RecursoAccesos.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoAccesos.manejadorSeguridad

        override fun transformarHaciaDTO(entidadDeNegocio: Acceso): AccesoDTO
        {
            return this@RecursoAccesos.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: AccesoDTO): AccesoDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: Acceso): Acceso
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Acceso?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: Long, camposModificables: Map<String, CampoModificable<Acceso, *>>)
        {
            repositorio.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}