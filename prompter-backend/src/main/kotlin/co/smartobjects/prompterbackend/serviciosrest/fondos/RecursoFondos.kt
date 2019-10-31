package co.smartobjects.prompterbackend.serviciosrest.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.fondos.RepositorioFondos
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoActualizablePorCamposDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoConsultarUnoDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoEliminarPorIdDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoListarTodosDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionParcialSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaEliminacionSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.FondoDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import co.smartobjects.red.modelos.fondos.IFondoDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoFondos(override val idCliente: Long, private val repositorio: RepositorioFondos, override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoListarTodosDeCliente<Fondo<*>, IFondoDTO<*>>
{
    companion object
    {
        const val RUTA = "funds"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Fondos"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = FondoDTO.CodigosError
    override val nombreEntidad: String = Fondo.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Fondo<*>): IFondoDTO<*>
    {
        return IFondoDTO.aIFondoDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Fondo<*>>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoFondo
    {
        return RecursoFondo(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoFondo(override val id: Long)
        : RecursoConsultarUnoDeCliente<Fondo<*>, IFondoDTO<*>, Long>,
          RecursoEliminarPorIdDeCliente<Fondo<*>, IFondoDTO<*>, Long>,
          RecursoActualizablePorCamposDeCliente<Fondo<*>, FondoDisponibleParaLaVentaDTO<*>, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoFondos.codigosError
        override val nombreEntidad: String = this@RecursoFondos.nombreEntidad
        override val idCliente: Long = this@RecursoFondos.idCliente
        private val repositorio: RepositorioFondos = this@RecursoFondos.repositorio
        override val nombrePermiso: String = this@RecursoFondos.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoFondos.manejadorSeguridad

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Fondo<*>?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Fondo<*>): IFondoDTO<*>
        {
            return this@RecursoFondos.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: Long, camposModificables: Map<String, CampoModificable<Fondo<*>, *>>)
        {
            repositorio.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}