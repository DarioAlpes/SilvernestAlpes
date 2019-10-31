package co.smartobjects.prompterbackend.serviciosrest.personas.creditos

import co.smartobjects.persistencia.operativas.compras.FiltroCreditosPersona
import co.smartobjects.persistencia.operativas.compras.RepositorioCreditosDeUnaPersona
import co.smartobjects.prompterbackend.excepciones.EntidadInvalida
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.red.modelos.operativas.compras.CreditosDeUnaPersonaDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.ZonedDateTime
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoCreditosDeUnaPersona(
        private val idCliente: Long,
        private val idPersona: Long,
        private val repositorio: RepositorioCreditosDeUnaPersona,
        private val manejadorSeguridad: ManejadorSeguridad)
{
    companion object
    {
        const val RUTA = "credits"
        const val NOMBRE_PARAMETRO_TIEMPO_CONSULTA = "base-datetime"

        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Créditos de Persona"
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
    }

    @GET
    fun buscar(@QueryParam(NOMBRE_PARAMETRO_TIEMPO_CONSULTA) fecha: ZonedDateTime?): CreditosDeUnaPersonaDTO
    {
        return ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend(CreditosDeUnaPersonaDTO.CodigosError) {
            ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(manejadorSeguridad, INFORMACION_PERMISO_CONSULTAR, idCliente)
            {
                if (fecha == null)
                {
                    throw EntidadInvalida("'$NOMBRE_PARAMETRO_TIEMPO_CONSULTA' no puede ser nulo", CreditosDeUnaPersonaDTO.CodigosError.FECHA_CONSULTA_INVALIDA)
                }
                val parametros = FiltroCreditosPersona.NoConsumidosValidosParaDia(idPersona, fecha.withZoneSameInstant(ZONA_HORARIA_POR_DEFECTO))

                CreditosDeUnaPersonaDTO(repositorio.buscarSegunParametros(idCliente, parametros)!!)
            }
        }
    }
}