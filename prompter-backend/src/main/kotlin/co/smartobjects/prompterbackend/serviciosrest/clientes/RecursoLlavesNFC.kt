package co.smartobjects.prompterbackend.serviciosrest.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.clientes.llavesnfc.FiltroLlavesNFC
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.prompterbackend.excepciones.EntidadInvalida
import co.smartobjects.prompterbackend.excepciones.EntidadNoExisteParaParametros
import co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoCreacionDeCliente
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaCreacionSegunNombrePermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaEliminacionSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.clientes.LlaveNFCDTO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.ZonedDateTime
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoLlavesNFC
(
        override val idCliente: Long,
        private val repositorio: RepositorioLlavesNFC,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoCreacionDeCliente<Cliente.LlaveNFC, LlaveNFCDTO>
{
    companion object
    {
        const val RUTA = "nfc-keys"
        const val NOMBRE_PARAMETRO_TIEMPO_CONSULTA = "base-datetime"

        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Llaves NFC"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigosError: CodigosErrorDTO = LlaveNFCDTO.CodigosError
    override val nombreEntidad: String = Cliente.LlaveNFC.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO


    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: Cliente.LlaveNFC): Cliente.LlaveNFC
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun transformarHaciaDTO(entidadDeNegocio: Cliente.LlaveNFC): LlaveNFCDTO
    {
        return LlaveNFCDTO(entidadDeNegocio)
    }


    @GET
    fun buscar(@QueryParam(NOMBRE_PARAMETRO_TIEMPO_CONSULTA) fecha: ZonedDateTime?): LlaveNFCDTO
    {
        return ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend(LlaveNFCDTO.CodigosError) {
            ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(manejadorSeguridad, INFORMACION_PERMISO_CONSULTAR, idCliente)
            {
                if (fecha == null)
                {
                    throw EntidadInvalida("'$NOMBRE_PARAMETRO_TIEMPO_CONSULTA' no puede ser nulo", LlaveNFCDTO.CodigosError.FECHA_CONSULTA_INVALIDA)
                }

                val parametros = FiltroLlavesNFC.ValidaEnFecha(fecha.withZoneSameInstant(ZONA_HORARIA_POR_DEFECTO))

                val entidadEncontrada =
                        repositorio.buscarSegunParametros(idCliente, parametros)
                        ?: throw EntidadNoExisteParaParametros(listOf(fecha.toString()), nombreEntidad, codigosError.NO_EXISTE)

                LlaveNFCDTO(entidadEncontrada)
            }
        }
    }

    @DELETE
    fun eliminarHastaFechaCorte(@QueryParam(NOMBRE_PARAMETRO_TIEMPO_CONSULTA) fecha: ZonedDateTime?)
    {
        ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(manejadorSeguridad, INFORMACION_PERMISO_ELIMINACION, idCliente)
        {
            if (fecha == null)
            {
                throw EntidadInvalida(
                        "'$NOMBRE_PARAMETRO_TIEMPO_CONSULTA' no puede ser nulo",
                        LlaveNFCDTO.CodigosError.FECHA_CONSULTA_INVALIDA
                                     )
            }

            val seEliminoCorrectamente =
                    try
                    {
                        val parametros = FiltroLlavesNFC.ValidaEnFecha(fecha.withZoneSameInstant(ZONA_HORARIA_POR_DEFECTO))
                        repositorio.eliminarSegunFiltros(idCliente, parametros)
                    }
                    catch (e: co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad)
                    {
                        throw ErrorEliminandoEntidad(e.entidad, codigosError.ERROR_DE_BD_DESCONOCIDO)
                    }
                    catch (e: ErrorDeLlaveForanea)
                    {
                        throw ErrorEliminandoEntidad(nombreEntidad, codigosError.ENTIDAD_REFERENCIADA, e)
                    }
                    catch (e: ErrorDeCreacionActualizacionEntidad)
                    {
                        throw ErrorEliminandoEntidad(nombreEntidad, codigosError.ERROR_DE_BD_DESCONOCIDO, e)
                    }

            if (!seEliminoCorrectamente)
            {
                throw EntidadNoExisteParaParametros(listOf(fecha.toString()), nombreEntidad, codigosError.NO_EXISTE)
            }
        }
    }
}