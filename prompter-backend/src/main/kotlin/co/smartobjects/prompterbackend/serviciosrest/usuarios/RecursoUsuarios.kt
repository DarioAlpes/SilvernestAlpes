package co.smartobjects.prompterbackend.serviciosrest.usuarios

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.entidades.usuarios.Usuario
import co.smartobjects.persistencia.usuarios.RepositorioUsuarios
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.usuarios.ContraseñaUsuarioDTO
import co.smartobjects.red.modelos.usuarios.UsuarioDTO
import co.smartobjects.red.modelos.usuarios.UsuarioParaCreacionDTO
import co.smartobjects.red.modelos.usuarios.UsuarioPatchDTO
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoUsuarios(override val idCliente: Long, private val repositorio: RepositorioUsuarios, override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoListarTodosDeCliente<Usuario, UsuarioDTO>
{
    companion object
    {
        const val RUTA = "users"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        internal const val NOMBRE_PERMISO = "Usuarios"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = UsuarioDTO.CodigosError
    override val nombreEntidad: String = Usuario.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    @POST
    fun crear(dto: UsuarioParaCreacionDTO): UsuarioDTO
    {
        try
        {
            val entidadDeNegocio =
                    ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(codigosError) {
                        dto.aEntidadDeNegocio()
                    }

            return ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                    manejadorSeguridad,
                    darInformacionPermisoParaCreacionSegunNombrePermiso(nombrePermiso),
                    idCliente
                                                                                          )
            {
                ejecutarFuncionCreacionTransformandoExcepcionesPersistenciaAExcepcionesBackend(nombreEntidad, codigosError) {
                    transformarHaciaDTO(repositorio.crear(idCliente, entidadDeNegocio))
                }
            }

        }
        finally
        {
            dto.limpiarContraseña()
        }
    }

    override fun transformarHaciaDTO(entidadDeNegocio: Usuario): UsuarioDTO
    {
        return UsuarioDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Usuario>
    {
        return repositorio.listar(idCliente)
    }

    fun crearUsuarioParaConfiguracionInicial()
    {
        val usuarioParaConfiguracionInicial =
                Usuario.UsuarioParaCreacion(
                        Usuario.DatosUsuario(0, "configuracion_inicial", "Usuario Para Configuración Inicial", "correo_no_existente@smartobjects.co", true,"apellido","CC","1234","2019-10-01","2019-10-01","co01"),
                        "configuracion_inicial".toCharArray().copyOf(),
                        setOf(Rol.RolParaCreacionDeUsuario(RecursoRoles.NOMBRE_ROL_PARA_CONFIGURACION))
                                           )

        repositorio.crear(idCliente, usuarioParaConfiguracionInicial)
    }


    @Path("{usuario}")
    fun darRecursosEntidadEspecifica(@PathParam("usuario") usuario: String): RecursoUsuario
    {
        return RecursoUsuario(usuario)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoUsuario(override val id: String)
        : RecursoConsultarUnoDeCliente<Usuario, UsuarioDTO, String>,
          RecursoEliminarPorIdDeCliente<Usuario, UsuarioDTO, String>,
          RecursoActualizablePorCamposDeClienteConAutenticacionCondicional<Usuario, UsuarioPatchDTO, String>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoUsuarios.codigosError
        override val nombreEntidad: String = this@RecursoUsuarios.nombreEntidad
        override val idCliente: Long = this@RecursoUsuarios.idCliente
        private val repositorio: RepositorioUsuarios = this@RecursoUsuarios.repositorio
        override val nombrePermiso: String = this@RecursoUsuarios.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoUsuarios.manejadorSeguridad

        @PUT
        fun actualizar(dto: UsuarioParaCreacionDTO): UsuarioDTO
        {
            try
            {
                val entidadDeNegocio =
                        ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(codigosError) {
                            dto.copy(usuario = id).aEntidadDeNegocio()
                        }

                return ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                        manejadorSeguridad,
                        darInformacionPermisoParaActualizacionSegunNombrePermiso(nombrePermiso),
                        idCliente
                                                                                              )
                {
                    ejecutarFuncionActualizacionTransformandoExcepcionesAExcepcionesBackend(
                            { transformarHaciaDTO(repositorio.actualizar(idCliente, id, entidadDeNegocio)) },
                            { darErrorBackendParaErrorDeLlaveForanea(it, codigosError) },
                            nombreEntidad,
                            codigosError
                                                                                           )
                }

            }
            finally
            {
                dto.limpiarContraseña()
            }
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: String): Usuario?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Usuario): UsuarioDTO
        {
            return this@RecursoUsuarios.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: String): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(
                idCliente: Long,
                id: String,
                camposModificables: Map<String, CampoModificable<Usuario, *>>
                                                                         )
        {
            try
            {
                repositorio.actualizarCamposIndividuales(idCliente, id, camposModificables)

                if (camposModificables.values.contains(Usuario.DatosUsuario.CampoActivo(false)))
                {
                    manejadorSeguridad.logoutUsuarioDeCliente(idCliente, id)
                }
            }
            finally
            {
                camposModificables.values.forEach {
                    if (it is Usuario.CredencialesUsuario.CampoContraseña)
                    {
                        it.limpiarValor()
                    }
                }
            }
        }

        // El usuario puede cambiar su contraseña sin tener permisos de patch sobre todos los usuarios (pero no con put)
        override fun seDebeValidarPermiso(campos: Map<String, CampoModificable<Usuario, *>>): Boolean
        {
            val usuario = manejadorSeguridad.darUsuarioDeClienteAutenticado()
            return if (usuario != null)
            {
                val esMismoUsuario = usuario.datosUsuario.let { it.usuario == id && it.idCliente == idCliente }

                val soloEstaEditandoContraseña =
                        campos.values.let { it.size == 1 && it.first() is Usuario.CredencialesUsuario.CampoContraseña }

                !esMismoUsuario || !soloEstaEditandoContraseña
            }
            else
            {
                true
            }
        }

        @Path("login")
        @POST
        fun login(contraseña: ContraseñaUsuarioDTO): UsuarioDTO
        //fun login(): UsuarioDTO
        {   //println("\nid: "+id+"contraseña.contraseña: "+contraseña.contraseña)

            manejadorSeguridad.loginUsuarioDeCliente(idCliente, id, contraseña.contraseña)
            return UsuarioDTO(manejadorSeguridad.darUsuarioDeClienteAutenticado()!!)
        }

        @Path("logout")
        @GET
        fun logout()
        {
            manejadorSeguridad.logoutUsuarioDeCliente(idCliente, id)
        }
    }
}