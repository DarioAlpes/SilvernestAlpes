package co.smartobjects.prompterbackend.serviciosrest.personas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.persistencia.operativas.compras.RepositorioComprasDeUnaPersona
import co.smartobjects.persistencia.operativas.compras.RepositorioCreditosDeUnaPersona
import co.smartobjects.persistencia.personas.RepositorioPersonas
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.personas.compras.RecursoComprasDeUnaPersona
import co.smartobjects.prompterbackend.serviciosrest.personas.creditos.RecursoCreditosDeUnaPersona
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoPersonas
(
        override val idCliente: Long,
        private val repositorio: RepositorioPersonas,
        private val repositorioComprasDeUnaPersona: RepositorioComprasDeUnaPersona,
        private val repositorioCreditosDeUnaPersona: RepositorioCreditosDeUnaPersona,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoCreacionEnClienteAutoasignandoId<Persona, Long?, PersonaDTO>,
    RecursoListarTodosDeCliente<Persona, PersonaDTO>
{
    companion object
    {
        const val RUTA = "persons"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Personas"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = PersonaDTO.CodigosError
    override val nombreEntidad: String = Persona.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Persona): PersonaDTO
    {
        return PersonaDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Persona): Persona
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Persona>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoPersona
    {
        return RecursoPersona(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoPersona(override val id: Long)
        : RecursoActualizableDeCliente<Persona, PersonaDTO, Long>,
          RecursoConsultarUnoDeCliente<Persona, PersonaDTO, Long>,
          RecursoEliminarPorIdDeCliente<Persona, PersonaDTO, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoPersonas.codigosError
        override val nombreEntidad: String = this@RecursoPersonas.nombreEntidad
        override val idCliente: Long = this@RecursoPersonas.idCliente
        private val repositorio: RepositorioPersonas = this@RecursoPersonas.repositorio
        override val nombrePermiso: String = this@RecursoPersonas.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoPersonas.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: Long, dto: PersonaDTO): PersonaDTO
        {
            return dto.copy(id = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: Long, entidad: Persona): Persona
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): Persona?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Persona): PersonaDTO
        {
            return this@RecursoPersonas.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        @Path(RecursoComprasDeUnaPersona.RUTA)
        fun darRecursoComprasDeUnaPersona() = RecursoComprasDeUnaPersona(idCliente, id, repositorioComprasDeUnaPersona, manejadorSeguridad)

        @Path(RecursoCreditosDeUnaPersona.RUTA)
        fun darRecursoCreditosDeUnaPersona() = RecursoCreditosDeUnaPersona(idCliente, id, repositorioCreditosDeUnaPersona, manejadorSeguridad)
    }
}