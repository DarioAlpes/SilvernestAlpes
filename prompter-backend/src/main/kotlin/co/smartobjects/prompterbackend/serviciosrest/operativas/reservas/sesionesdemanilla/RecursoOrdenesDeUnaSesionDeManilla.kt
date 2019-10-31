package co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla

import co.smartobjects.persistencia.operativas.ordenes.IdSesionDeManillaParaConsultaOrdenes
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesDeUnaSesionDeManilla
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoOrdenesDeUnaSesionDeManilla
(
        private val idCliente: Long,
        private val idSesionDeManilla: Long,
        private val repositorioOrdenesDeUnaSesionDeManilla: RepositorioOrdenesDeUnaSesionDeManilla,
        private val manejadorSeguridad: ManejadorSeguridad
)
{
    companion object
    {
        const val RUTA = "orders"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "OrdenesDeUnaSesionDeManilla"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
    }

    @GET
    fun listar(): Sequence<OrdenDTO>
    {
        return ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend(
                OrdenDTO.CodigosError,
                {
                    val parametros = IdSesionDeManillaParaConsultaOrdenes(idSesionDeManilla)
                    ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                            manejadorSeguridad,
                            INFORMACION_PERMISO_LISTAR,
                            idCliente,
                            {
                                repositorioOrdenesDeUnaSesionDeManilla
                                    .listarSegunParametros(idCliente, parametros)
                                    .map { OrdenDTO(it) }
                            }
                                                                                           )
                }
                                                                               )
    }
}