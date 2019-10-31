package co.smartobjects.prompterbackend.serviciosrest.personas

import co.smartobjects.entidades.personas.CampoDePersona
import co.smartobjects.persistencia.personas.camposdepersona.RepositorioCamposDePersonas
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoActualizableDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoConsultarUnoDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoListarTodosDeCliente
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.personas.CampoDePersonaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoCamposDePersona(override val idCliente: Long, private val repositorio: RepositorioCamposDePersonas, override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoListarTodosDeCliente<CampoDePersona, CampoDePersonaDTO>
{
    companion object
    {
        const val RUTA = "persons-fields"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Campos de Personas"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = CampoDePersonaDTO.CodigosError
    override val nombreEntidad: String = CampoDePersona.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: CampoDePersona): CampoDePersonaDTO
    {
        return CampoDePersonaDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<CampoDePersona>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{campo}")
    fun darRecursosEntidadEspecifica(@PathParam("campo") valorEnRed: String): RecursoCampoDePersona
    {
        val valorParseado = CampoDePersonaDTO.Predeterminado.desdeRed(valorEnRed)
                            ?:
                            throw EntidadNoExiste(valorEnRed, CampoDePersona.NOMBRE_ENTIDAD, CampoDePersonaDTO.CodigosError.NO_EXISTE)

        return RecursoCampoDePersona(valorParseado.valorEnNegocio)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoCampoDePersona(override val id: CampoDePersona.Predeterminado)
        : RecursoConsultarUnoDeCliente<CampoDePersona, CampoDePersonaDTO, CampoDePersona.Predeterminado>,
          RecursoActualizableDeCliente<CampoDePersona, CampoDePersonaDTO, CampoDePersona.Predeterminado>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoCamposDePersona.codigosError
        override val nombreEntidad: String = this@RecursoCamposDePersona.nombreEntidad
        override val idCliente: Long = this@RecursoCamposDePersona.idCliente
        private val repositorio: RepositorioCamposDePersonas = this@RecursoCamposDePersona.repositorio
        override val nombrePermiso: String = this@RecursoCamposDePersona.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoCamposDePersona.manejadorSeguridad

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: CampoDePersona.Predeterminado): CampoDePersona?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: CampoDePersona): CampoDePersonaDTO
        {
            return this@RecursoCamposDePersona.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun sustituirIdEnEntidad(idAUsar: CampoDePersona.Predeterminado, dto: CampoDePersonaDTO): CampoDePersonaDTO
        {
            return dto.copy(campo = CampoDePersonaDTO.Predeterminado.desdeNegocio(idAUsar))
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: CampoDePersona.Predeterminado, entidad: CampoDePersona): CampoDePersona
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }
    }
}
