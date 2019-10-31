package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaListarSegunNombrePermiso
import co.smartobjects.red.modelos.clientes.ClienteDTO
import javax.ws.rs.GET


@Suppress("unused")
internal interface RecursoListarTodosNoDTO<EntidadNegocio, TipoRetornado>
    : RecursoConErroresDTO
{
    fun listarTodos(): Sequence<TipoRetornado>

    @GET
    fun darTodas(): Sequence<TipoRetornado>
    {
        return ejecutarFuncionListarTransformandoExcepcionesAExcepcionesBackend(codigosError, ::listarTodos)
    }
}

internal interface RecursoListarTodosNoDTODeCliente<EntidadNegocio, TipoRetornado>
    : RecursoListarTodosNoDTO<EntidadNegocio, TipoRetornado>,
      RecursoDeCliente
{
    fun listarTodosSegunIdCliente(idCliente: Long): Sequence<TipoRetornado>

    override fun listarTodos(): Sequence<TipoRetornado>
    {
        manejadorSeguridad
            .verificarUsuarioDeClienteActualTienePermiso(
                    darInformacionPermisoParaListarSegunNombrePermiso(nombrePermiso)
                        .aPermisoBackSegunIdCliente(idCliente)
                                                        )

        return listarTodosSegunIdCliente(idCliente)
    }

    override fun darTodas(): Sequence<TipoRetornado>
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