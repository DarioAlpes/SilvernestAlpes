package co.smartobjects.prompterbackend.serviciosrest.ubicaciones

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioConsumibleEnPuntoDeVenta
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioFondosEnPuntoDeVenta
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioConteosUbicaciones
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles.RecursoConsumiblesEnPuntoDeVenta
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles.RecursoFondosEnPuntoDeVenta
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables.RecursoConteosEnUbicacion
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.ubicaciones.UbicacionDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoUbicaciones(
        override val idCliente: Long,
        private val repositorio: RepositorioUbicaciones,
        private val repositorioConsumiblesEnPuntoDeVenta: RepositorioConsumibleEnPuntoDeVenta,
        private val repositorioFondosEnPuntoDeVenta: RepositorioFondosEnPuntoDeVenta,
        private val repositorioConteoUbicaciones: RepositorioConteosUbicaciones,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionEnClienteAutoasignandoId<Ubicacion, Long?, UbicacionDTO>,
      RecursoListarTodosDeCliente<Ubicacion, UbicacionDTO>
{
    companion object
    {
        const val RUTA = "locations"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Ubicaciones"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = UbicacionDTO.CodigosError
    override val nombreEntidad: String = Ubicacion.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Ubicacion): UbicacionDTO
    {
        return UbicacionDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Ubicacion): Ubicacion
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Ubicacion>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoUbicacion
    {
        return RecursoUbicacion(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoUbicacion(override val id: Long)
        : RecursoActualizableJerarquicoDeCliente<Ubicacion, UbicacionDTO, Long>,
          RecursoConsultarUnoDeCliente<Ubicacion, UbicacionDTO, Long>,
          RecursoEliminarPorIdDeCliente<Ubicacion, UbicacionDTO, Long>
    {
        override val codigoDeErrorCicloJerarquia = UbicacionDTO.CodigosError.CICLO_JERARQUIA
        override val codigosError: CodigosErrorDTO = this@RecursoUbicaciones.codigosError
        override val nombreEntidad: String = this@RecursoUbicaciones.nombreEntidad
        override val idCliente: Long = this@RecursoUbicaciones.idCliente
        private val repositorio: RepositorioUbicaciones = this@RecursoUbicaciones.repositorio
        override val nombrePermiso: String = this@RecursoUbicaciones.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoUbicaciones.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: UbicacionDTO): UbicacionDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: Ubicacion): Ubicacion
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Ubicacion?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Ubicacion): UbicacionDTO
        {
            return this@RecursoUbicaciones.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        @Path(RecursoConsumiblesEnPuntoDeVenta.RUTA)
        fun darRecursoConsumiblesEnPuntoDeVenta() = RecursoConsumiblesEnPuntoDeVenta(idCliente, id, repositorioConsumiblesEnPuntoDeVenta, manejadorSeguridad)

        @Path(RecursoFondosEnPuntoDeVenta.RUTA)
        fun darRecursoFondosEnPuntoDeVenta() = RecursoFondosEnPuntoDeVenta(idCliente, id, repositorioFondosEnPuntoDeVenta, manejadorSeguridad)

        @Path(RecursoConteosEnUbicacion.RUTA)
        fun darRecursoConteosEnUbicacion() = RecursoConteosEnUbicacion(idCliente, id, repositorioConteoUbicaciones, manejadorSeguridad)
    }
}