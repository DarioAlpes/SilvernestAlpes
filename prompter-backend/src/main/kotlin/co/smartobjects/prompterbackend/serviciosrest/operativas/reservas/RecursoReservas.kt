package co.smartobjects.prompterbackend.serviciosrest.operativas.reservas

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservas
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.reservas.ReservaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoReservas(
        override val idCliente: Long,
        val repositorioReservas: RepositorioReservas,
        override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoListarTodosDeCliente<Reserva, ReservaDTO>
{
    companion object
    {
        const val RUTA = "reservations"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Reservas"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = ReservaDTO.CodigosError
    override val nombreEntidad: String = Reserva.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Reserva): ReservaDTO
    {
        return ReservaDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Reserva>
    {
        return repositorioReservas.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: String): RecursoReserva
    {
        return RecursoReserva(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoReserva(override val id: String)
        : RecursoConsultarUnoDeCliente<Reserva, ReservaDTO, String>,
          RecursoEliminarPorIdDeClienteConRestriccion<Reserva, ReservaDTO, String>,
          RecursoActualizableQueCreaConRestriccionDeCliente<Reserva, ReservaDTO, String>,
          RecursoActualizablePorCamposDeCliente<Reserva, TransaccionEntidadTerminadaDTO<Reserva>, String>
    {
        override val codigoDeErrorRestriccionIncumplidaEliminacion: Int = ReservaDTO.CodigosError.ESTA_MARCADA_CON_CREACION_TERMINADA
        override val codigoDeErrorRestriccionIncumplida: Int = ReservaDTO.CodigosError.SESIONES_DE_MANILLAS_INVALIDAS
        override val codigosError: CodigosErrorDTO = this@RecursoReservas.codigosError
        override val nombreEntidad: String = this@RecursoReservas.nombreEntidad
        override val idCliente: Long = this@RecursoReservas.idCliente
        override val nombrePermiso: String = this@RecursoReservas.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoReservas.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: String, dto: ReservaDTO): ReservaDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: String, entidad: Reserva): Reserva
        {
            return repositorioReservas.crear(idCliente, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: String): Reserva?
        {
            return repositorioReservas.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Reserva): ReservaDTO
        {
            return this@RecursoReservas.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: String): Boolean
        {
            return repositorioReservas.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: String, camposModificables: Map<String, CampoModificable<Reserva, *>>)
        {
            repositorioReservas.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}