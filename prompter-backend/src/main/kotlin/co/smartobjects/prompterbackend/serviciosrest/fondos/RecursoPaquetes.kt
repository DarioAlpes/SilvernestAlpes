package co.smartobjects.prompterbackend.serviciosrest.fondos

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.PaqueteDTO
import co.smartobjects.red.modelos.fondos.PaquetePatchDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoPaquetes(
        override val idCliente: Long,
        val repositorioPaquetes: RepositorioPaquetes,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionEnClienteAutoasignandoId<Paquete, Long?, PaqueteDTO>,
      RecursoListarTodosDeCliente<Paquete, PaqueteDTO>
{
    companion object
    {
        const val RUTA = "packages"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Paquetes"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = PaqueteDTO.CodigosError
    override val nombreEntidad: String = Paquete.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Paquete): PaqueteDTO
    {
        return PaqueteDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Paquete): Paquete
    {
        return repositorioPaquetes.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Paquete>
    {
        return repositorioPaquetes.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoPaquete
    {
        return RecursoPaquete(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoPaquete(override val id: Long)
        : RecursoConsultarUnoDeCliente<Paquete, PaqueteDTO, Long>,
          RecursoEliminarPorIdDeCliente<Paquete, PaqueteDTO, Long>,
          RecursoActualizableDeCliente<Paquete, PaqueteDTO, Long>,
          RecursoActualizablePorCamposDeCliente<Paquete, PaquetePatchDTO, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoPaquetes.codigosError
        override val nombreEntidad: String = this@RecursoPaquetes.nombreEntidad
        override val idCliente: Long = this@RecursoPaquetes.idCliente
        private val repositorio: RepositorioPaquetes = this@RecursoPaquetes.repositorioPaquetes
        override val nombrePermiso: String = this@RecursoPaquetes.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoPaquetes.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: PaqueteDTO): PaqueteDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: Paquete): Paquete
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Paquete?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Paquete): PaqueteDTO
        {
            return this@RecursoPaquetes.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: Long, camposModificables: Map<String, CampoModificable<Paquete, *>>)
        {
            repositorio.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}