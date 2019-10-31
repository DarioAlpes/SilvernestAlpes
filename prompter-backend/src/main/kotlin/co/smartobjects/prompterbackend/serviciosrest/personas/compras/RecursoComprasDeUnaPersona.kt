package co.smartobjects.prompterbackend.serviciosrest.personas.compras

import co.smartobjects.persistencia.operativas.compras.FiltroComprasPersonaConCreditosPresentesOFuturos
import co.smartobjects.persistencia.operativas.compras.RepositorioComprasDeUnaPersona
import co.smartobjects.prompterbackend.excepciones.EntidadInvalida
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.ZonedDateTime
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoComprasDeUnaPersona(
        private val idCliente: Long,
        private val idPersona: Long,
        private val repositorioComprasDeUnaPersona: RepositorioComprasDeUnaPersona,
        private val manejadorSeguridad: ManejadorSeguridad)
{
    companion object
    {
        const val NOMBRE_PARAMETRO_TIEMPO_CONSULTA = "base-datetime"
        const val RUTA = "available-purchases"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Compras de Persona"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
    }

    @GET
    fun listar(@QueryParam(NOMBRE_PARAMETRO_TIEMPO_CONSULTA) fecha: ZonedDateTime?): Sequence<CompraDTO>
    {
        return ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend(CompraDTO.CodigosError) {
            ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(manejadorSeguridad, INFORMACION_PERMISO_LISTAR, idCliente)
            {
                if (fecha == null)
                {
                    throw EntidadInvalida("'$NOMBRE_PARAMETRO_TIEMPO_CONSULTA' no puede ser nulo", CompraDTO.CodigosError.FECHA_CONSULTA_INVALIDA)
                }
                val parametros = FiltroComprasPersonaConCreditosPresentesOFuturos(idPersona, fecha.withZoneSameInstant(ZONA_HORARIA_POR_DEFECTO))

                repositorioComprasDeUnaPersona.listarSegunParametros(idCliente, parametros).map { CompraDTO(it) }
            }
        }
    }
}