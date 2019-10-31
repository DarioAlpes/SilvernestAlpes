package co.smartobjects.prompterbackend.serviciosrest.usuarios

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.persistencia.usuarios.roles.RepositorioRoles
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.usuarios.RolDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoRoles(override val idCliente: Long, private val repositorio: RepositorioRoles, override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionDeCliente<Rol, RolDTO>,
      RecursoListarTodosDeCliente<Rol, RolDTO>
{
    companion object
    {
        const val RUTA = "roles"

        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Roles"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)

        internal const val NOMBRE_ROL_PARA_CONFIGURACION = "ConfiguracionInicial"
        internal const val DESCRIPCION_ROL_PARA_CONFIGURACION = "Rol inicial para configuración del cliente"
    }

    override val codigosError: CodigosErrorDTO = RolDTO.CodigosError
    override val nombreEntidad: String = Rol.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: Rol): RolDTO
    {
        return RolDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Rol): Rol
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<Rol>
    {
        return repositorio.listar(idCliente)
    }

    fun crearRolParaConfiguracionInicial()
    {
        // Rol por defecto debe tener permisos para CRUD de Roles y Usuarios
        val permisos = setOf(
                PermisoBack(idCliente, RecursoUsuarios.NOMBRE_PERMISO, PermisoBack.Accion.POST),
                PermisoBack(idCliente, RecursoUsuarios.NOMBRE_PERMISO, PermisoBack.Accion.PUT),
                PermisoBack(idCliente, RecursoUsuarios.NOMBRE_PERMISO, PermisoBack.Accion.GET_UNO),
                PermisoBack(idCliente, RecursoUsuarios.NOMBRE_PERMISO, PermisoBack.Accion.GET_TODOS),
                PermisoBack(idCliente, RecursoUsuarios.NOMBRE_PERMISO, PermisoBack.Accion.PATCH),
                PermisoBack(idCliente, RecursoUsuarios.NOMBRE_PERMISO, PermisoBack.Accion.DELETE),

                PermisoBack(idCliente, NOMBRE_PERMISO, PermisoBack.Accion.POST),
                PermisoBack(idCliente, NOMBRE_PERMISO, PermisoBack.Accion.PUT),
                PermisoBack(idCliente, NOMBRE_PERMISO, PermisoBack.Accion.GET_UNO),
                PermisoBack(idCliente, NOMBRE_PERMISO, PermisoBack.Accion.GET_TODOS),
                PermisoBack(idCliente, NOMBRE_PERMISO, PermisoBack.Accion.DELETE)
                            )

        crearEntidadDeNegocioSegunIdCliente(
                idCliente,
                Rol(NOMBRE_ROL_PARA_CONFIGURACION, DESCRIPCION_ROL_PARA_CONFIGURACION, permisos)
                                           )
    }

    @Path("{rol}")
    fun darRecursosEntidadEspecifica(@PathParam("rol") rol: String): RecursoRol
    {
        return RecursoRol(rol)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoRol(override val id: String)
        : RecursoActualizableDeCliente<Rol, RolDTO, String>,
          RecursoConsultarUnoDeCliente<Rol, RolDTO, String>,
          RecursoEliminarPorIdDeCliente<Rol, RolDTO, String>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoRoles.codigosError
        override val nombreEntidad: String = this@RecursoRoles.nombreEntidad
        override val idCliente: Long = this@RecursoRoles.idCliente
        private val repositorio: RepositorioRoles = this@RecursoRoles.repositorio
        override val nombrePermiso: String = this@RecursoRoles.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoRoles.manejadorSeguridad

        override fun sustituirIdEnEntidad(idAUsar: String, dto: RolDTO): RolDTO
        {
            return dto.copy(nombre = idAUsar)
        }

        override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: String, entidad: Rol): Rol
        {
            return repositorio.actualizar(idCliente, idAUsar, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: String): Rol?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: Rol): RolDTO
        {
            return this@RecursoRoles.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: String): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }
    }
}