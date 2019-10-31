package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.excepciones.ErrorDesconocido
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.clientes.ClienteDTO
import javax.ws.rs.GET

internal interface RecursoListarTodos<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>>
    : TransformacionesConDTO<EntidadNegocio, TipoEntidadDTO>,
      RecursoConErroresDTO
{
    fun listarTodos(): Sequence<EntidadNegocio>

    @GET
    fun darTodas(): Sequence<TipoEntidadDTO>
    {
        return ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend(codigosError) {
            listarTodos().map { transformarHaciaDTO(it) }
        }
    }
}

internal fun <T> ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend(
        codigosError: CodigosErrorDTO,
        funcionAEjecutar: () -> T
                                                                                 ): T
{
    return try
    {
        funcionAEjecutar()
    }
    catch (errorBD: ErrorDeConsultaEntidad)
    {
        throw ErrorDesconocido("Error consultando entidad", codigosError.ERROR_DE_BD_DESCONOCIDO, errorBD)
    }
}

internal interface RecursoListarTodosConAutenticacionGlobal<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>>
    : RecursoListarTodos<EntidadNegocio, TipoEntidadDTO>
{
    val manejadorSeguridad: ManejadorSeguridad

    override fun darTodas(): Sequence<TipoEntidadDTO>
    {
        manejadorSeguridad.verificarUsuarioGlobalEstaAutenticado()
        return super.darTodas()
    }
}

internal interface RecursoListarTodosDeCliente<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>>
    : RecursoListarTodos<EntidadNegocio, TipoEntidadDTO>,
      RecursoDeCliente
{
    fun listarTodosSegunIdCliente(idCliente: Long): Sequence<EntidadNegocio>

    override fun listarTodos(): Sequence<EntidadNegocio>
    {
        manejadorSeguridad
            .verificarUsuarioDeClienteActualTienePermiso(
                    darInformacionPermisoParaListarSegunNombrePermiso(nombrePermiso)
                        .aPermisoBackSegunIdCliente(idCliente)
                                                        )
        return listarTodosSegunIdCliente(idCliente)
    }

    override fun darTodas(): Sequence<TipoEntidadDTO>
    {
        return try
        {
            super.darTodas()
        }
        catch (ex: EsquemaNoExiste)
        {
            throw EntidadNoExiste(idCliente, Cliente.NOMBRE_ENTIDAD, ClienteDTO.CodigosError.NO_EXISTE, ex)
        }
    }
}