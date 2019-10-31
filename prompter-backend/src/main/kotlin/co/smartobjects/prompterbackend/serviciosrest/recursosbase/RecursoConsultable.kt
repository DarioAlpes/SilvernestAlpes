package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.excepciones.ErrorDesconocido
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaConsultarSegunNombrePermiso
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.clientes.ClienteDTO
import javax.ws.rs.GET

internal interface RecursoConsultarUno<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : TransformacionesConDTO<EntidadNegocio, TipoEntidadDTO>,
      RecursoConErroresDTO,
      RecursoEspecifico<TipoId>
{
    fun consultarPorId(id: TipoId): EntidadNegocio?

    @GET
    fun darPorId(): TipoEntidadDTO
    {
        try
        {
            return consultarPorId(id)?.let { transformarHaciaDTO(it) }
                   ?: throw EntidadNoExiste(id.toString(), nombreEntidad, codigosError.NO_EXISTE)
        }
        catch (errorBD: ErrorDeConsultaEntidad)
        {
            throw ErrorDesconocido("Error consultando entidad", codigosError.ERROR_DE_BD_DESCONOCIDO, errorBD)
        }
    }
}

internal interface RecursoConsultarUnoConAutenticacionGlobal<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoConsultarUno<EntidadNegocio, TipoEntidadDTO, TipoId>
{
    val manejadorSeguridad: ManejadorSeguridad
    override fun darPorId(): TipoEntidadDTO
    {
        manejadorSeguridad.verificarUsuarioGlobalEstaAutenticado()
        return super.darPorId()
    }
}

internal interface RecursoConsultarUnoDeCliente<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoConsultarUno<EntidadNegocio, TipoEntidadDTO, TipoId>, RecursoDeCliente
{
    fun consultarPorIdSegunIdCliente(idCliente: Long, id: TipoId): EntidadNegocio?

    override fun consultarPorId(id: TipoId): EntidadNegocio?
    {
        manejadorSeguridad.verificarUsuarioDeClienteActualTienePermiso(darInformacionPermisoParaConsultarSegunNombrePermiso(nombrePermiso).aPermisoBackSegunIdCliente(idCliente))
        return consultarPorIdSegunIdCliente(idCliente, id)
    }

    override fun darPorId(): TipoEntidadDTO
    {
        return try
        {
            super.darPorId()
        }
        catch (ex: EsquemaNoExiste)
        {
            throw EntidadNoExiste(idCliente, Cliente.NOMBRE_ENTIDAD, ClienteDTO.CodigosError.NO_EXISTE, ex)
        }
    }
}