package co.smartobjects.prompterbackend.serviciosrest.usuariosglobales

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.persistencia.usuariosglobales.RepositorioUsuariosGlobales
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.usuariosglobales.ContraseñaUsuarioGlobalDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalParaCreacionDTO
import co.smartobjects.red.modelos.usuariosglobales.UsuarioGlobalPatchDTO
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path(RecursoUsuariosGlobales.RUTA)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoUsuariosGlobales
(
        private val repositorio: RepositorioUsuariosGlobales,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoListarTodosConAutenticacionGlobal<UsuarioGlobal, UsuarioGlobalDTO>
{
    companion object
    {
        const val RUTA = "global-users"
    }

    override val codigosError: CodigosErrorDTO = UsuarioGlobalDTO.CodigosError
    override val nombreEntidad: String = UsuarioGlobal.NOMBRE_ENTIDAD


    internal fun inicializar()
    {
        val existiaLaTabla = repositorio.inicializar()

        if (!existiaLaTabla)
        {
            val usuarioACrear =
                    UsuarioGlobal.UsuarioParaCreacion(
                            UsuarioGlobal.DatosUsuario("admin", "Juan Ciga", "juan.ciga@smartobjects.co", true),
                            "admin".toCharArray()
                                                     )

            repositorio.crear(usuarioACrear)
        }
    }


    @POST
    fun crear(dto: UsuarioGlobalParaCreacionDTO): UsuarioGlobalDTO
    {
        try
        {
            manejadorSeguridad.verificarUsuarioGlobalEstaAutenticado()
            val entidadDeNegocio =
                    ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(codigosError) {
                        dto.aEntidadDeNegocio()
                    }

            return ejecutarFuncionCreacionTransformandoExcepcionesPersistenciaAExcepcionesBackend(nombreEntidad, codigosError) {
                transformarHaciaDTO(repositorio.crear(entidadDeNegocio))
            }

        }
        finally
        {
            dto.limpiarContraseña()
        }
    }

    override fun transformarHaciaDTO(entidadDeNegocio: UsuarioGlobal): UsuarioGlobalDTO
    {
        return UsuarioGlobalDTO(entidadDeNegocio)
    }

    override fun listarTodos(): Sequence<UsuarioGlobal>
    {
        return repositorio.listar()
    }

    @Path("{usuario}")
    fun darRecursosEntidadEspecifica(@PathParam("usuario") usuario: String): RecursoUsuarioGlobal
    {
        return RecursoUsuarioGlobal(usuario)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoUsuarioGlobal(override val id: String)
        : RecursoConsultarUnoConAutenticacionGlobal<UsuarioGlobal, UsuarioGlobalDTO, String>,
          RecursoEliminarPorIdConAutenticacionGlobal<UsuarioGlobal, UsuarioGlobalDTO, String>,
          RecursoActualizablePorCamposConAutenticacionGlobal<UsuarioGlobal, UsuarioGlobalPatchDTO, String>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoUsuariosGlobales.codigosError
        override val nombreEntidad: String = this@RecursoUsuariosGlobales.nombreEntidad
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoUsuariosGlobales.manejadorSeguridad
        private val repositorio: RepositorioUsuariosGlobales = this@RecursoUsuariosGlobales.repositorio

        @PUT
        fun actualizar(dto: UsuarioGlobalParaCreacionDTO): UsuarioGlobalDTO
        {
            try
            {
                manejadorSeguridad.verificarUsuarioGlobalEstaAutenticado()
                val entidadDeNegocio =
                        ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(codigosError) {
                            dto.copy(usuario = id).aEntidadDeNegocio()
                        }

                return ejecutarFuncionActualizacionTransformandoExcepcionesAExcepcionesBackend(
                        { transformarHaciaDTO(repositorio.actualizar(id, entidadDeNegocio)) },
                        { darErrorBackendParaErrorDeLlaveForanea(it, codigosError) },
                        nombreEntidad,
                        codigosError
                                                                                              )

            }
            finally
            {
                dto.limpiarContraseña()
            }
        }

        override fun consultarPorId(id: String): UsuarioGlobal?
        {
            return repositorio.buscarPorId(id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: UsuarioGlobal): UsuarioGlobalDTO
        {
            return this@RecursoUsuariosGlobales.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorId(id: String): Boolean
        {
            return repositorio.eliminarPorId(id)
        }

        override fun actualizarEntidadPorCamposIndividuales(id: String, camposModificables: Map<String, CampoModificable<UsuarioGlobal, *>>)
        {
            try
            {
                repositorio.actualizarCamposIndividuales(id, camposModificables)

                if (camposModificables.values.contains(UsuarioGlobal.DatosUsuario.CampoActivo(false)))
                {
                    manejadorSeguridad.logoutUsuarioGlobal(id)
                }
            }
            finally
            {
                camposModificables.values.forEach {
                    if (it is UsuarioGlobal.CredencialesUsuario.CampoContraseña)
                    {
                        it.limpiarValor()
                    }
                }
            }
        }

        @Path("login")
        @POST
        fun login(contraseña: ContraseñaUsuarioGlobalDTO): UsuarioGlobalDTO
        {   println("\nid: "+id+" contraseña.contraseña: "+contraseña.contraseña)
            manejadorSeguridad.loginUsuarioGlobal(id, contraseña.contraseña)
            return UsuarioGlobalDTO(manejadorSeguridad.darUsuarioGlobalAutenticado()!!)
        }

        @Path("logout")
        @GET
        fun logout()
        {
            manejadorSeguridad.logoutUsuarioGlobal(id)
        }
    }
}