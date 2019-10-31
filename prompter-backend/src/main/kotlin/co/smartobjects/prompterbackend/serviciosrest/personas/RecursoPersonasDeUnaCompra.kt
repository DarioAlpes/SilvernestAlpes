package co.smartobjects.prompterbackend.serviciosrest.personas

import co.smartobjects.persistencia.operativas.compras.NumeroTransaccionPago
import co.smartobjects.persistencia.operativas.compras.RepositorioPersonasDeUnaCompra
import co.smartobjects.prompterbackend.excepciones.EntidadInvalida
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.operativas.compras.PagoDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoPersonasDeUnaCompra(
        private val idCliente: Long,
        private val repositorioPersonasDeUnaCompra: RepositorioPersonasDeUnaCompra,
        private val manejadorSeguridad: ManejadorSeguridad)
{
    companion object
    {
        const val NOMBRE_PARAMETRO_NUMERO_TRANSACCION = "transaction-number"
        const val RUTA = "persons-by-transaction-number"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Personas de una Compra"
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
    }

    @GET
    fun listar(@QueryParam(NOMBRE_PARAMETRO_NUMERO_TRANSACCION) numeroTransaccion: String?): Sequence<PersonaDTO>
    {
        return ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend(
                PersonaDTO.CodigosError,
                {
                    if (numeroTransaccion == null)
                    {
                        throw EntidadInvalida("$NOMBRE_PARAMETRO_NUMERO_TRANSACCION no puede ser nulo", PagoDTO.CodigosError.NUMERO_TRANSACCION_POS_INVALIDO)
                    }
                    val parametros = NumeroTransaccionPago(numeroTransaccion)
                    ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                            manejadorSeguridad,
                            INFORMACION_PERMISO_LISTAR,
                            idCliente,
                            { repositorioPersonasDeUnaCompra.listarSegunParametros(idCliente, parametros).map { PersonaDTO(it) } }
                                                                                           )
                }
                                                                               )
    }
}