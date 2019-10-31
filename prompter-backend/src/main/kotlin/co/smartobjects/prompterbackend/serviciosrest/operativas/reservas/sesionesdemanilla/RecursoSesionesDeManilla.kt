package co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesDeUnaSesionDeManilla
import co.smartobjects.persistencia.operativas.reservas.RepositorioDeSesionDeManilla
import co.smartobjects.persistencia.personas.RepositorioPersonas
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoActualizablePorCamposDeClienteConRestriccion
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoConsultarUnoDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionParcialSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaPatchDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoSesionesDeManilla
(
        override val idCliente: Long,
        private val repositorioDeSesionDeManilla: RepositorioDeSesionDeManilla,
        private val repositorioOrdenesDeUnaSesionDeManilla: RepositorioOrdenesDeUnaSesionDeManilla,
        private val repositorioPersonas: RepositorioPersonas,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoDeCliente
{
    companion object
    {
        const val RUTA = "tag-sessions"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "SesionesDeManillas"
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val nombrePermiso: String = NOMBRE_PERMISO

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoSesionDeManilla
    {
        return RecursoSesionDeManilla(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoSesionDeManilla(override val id: Long)
        : RecursoConsultarUnoDeCliente<SesionDeManilla, SesionDeManillaDTO, Long>,
          RecursoActualizablePorCamposDeClienteConRestriccion<SesionDeManilla, SesionDeManillaPatchDTO, Long>
    {
        override val codigosError: CodigosErrorDTO = SesionDeManillaDTO.CodigosError
        override val codigoDeErrorRestriccionIncumplida: Int = SesionDeManillaDTO.CodigosError.SESION_YA_TIENE_TAG_ASOCIADO
        override val nombreEntidad: String = SesionDeManilla.NOMBRE_ENTIDAD
        override val idCliente: Long = this@RecursoSesionesDeManilla.idCliente
        override val nombrePermiso: String = this@RecursoSesionesDeManilla.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoSesionesDeManilla.manejadorSeguridad

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): SesionDeManilla?
        {
            return repositorioDeSesionDeManilla.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: SesionDeManilla): SesionDeManillaDTO
        {
            return SesionDeManillaDTO(entidadDeNegocio)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: Long, camposModificables: Map<String, CampoModificable<SesionDeManilla, *>>)
        {
            repositorioDeSesionDeManilla.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }

        @Path(RecursoOrdenesDeUnaSesionDeManilla.RUTA)
        fun darRecursoOrdenesDeUnaSesionDeManilla() =
                RecursoOrdenesDeUnaSesionDeManilla(idCliente, id, repositorioOrdenesDeUnaSesionDeManilla, manejadorSeguridad)

        @Path(RecursoPersonaPorIdSesionManilla.RUTA)
        fun darRecursoPersonaPorIdSesionManilla() =
                RecursoPersonaPorIdSesionManilla(idCliente, id, repositorioDeSesionDeManilla, repositorioPersonas, manejadorSeguridad)
    }
}