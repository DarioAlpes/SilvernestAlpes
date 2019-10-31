package co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.persistencia.operativas.reservas.RepositorioDeSesionDeManilla
import co.smartobjects.persistencia.personas.RepositorioPersonas
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoConsultarUnoDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoPersonaPorIdSesionManilla
(
        override val idCliente: Long,
        override val id: Long,
        private val repositorioDeSesionDeManilla: RepositorioDeSesionDeManilla,
        private val repositorioPersonas: RepositorioPersonas,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoConsultarUnoDeCliente<Persona, PersonaDTO, Long>
{
    companion object
    {
        const val RUTA = "person"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "PersonaDeUnaSesionDeManilla"
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val nombrePermiso: String = NOMBRE_PERMISO

    override val codigosError: CodigosErrorDTO = SesionDeManillaDTO.CodigosError
    override val nombreEntidad: String = SesionDeManilla.NOMBRE_ENTIDAD

    override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Persona?
    {
        return repositorioDeSesionDeManilla.buscarPorId(idCliente, id)?.let {
            repositorioPersonas.buscarPorId(idCliente, it.idPersona)
        }
    }

    override fun transformarHaciaDTO(entidadDeNegocio: Persona): PersonaDTO
    {
        return PersonaDTO(entidadDeNegocio)
    }
}