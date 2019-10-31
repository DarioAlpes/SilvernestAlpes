package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.ErrorEliminacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.excepciones.ErrorEliminandoEntidad
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaEliminacionSegunNombrePermiso
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.clientes.ClienteDTO
import javax.ws.rs.DELETE

internal interface RecursoEliminarPorId<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : TransformacionesConDTO<EntidadNegocio, TipoEntidadDTO>,
      RecursoConErroresDTO,
      RecursoEspecifico<TipoId>
{
    fun eliminarPorId(id: TipoId): Boolean

    @DELETE
    fun eliminarPorId()
    {
        val seEliminoCorrectamente =
                try
                {
                    eliminarPorId(id)
                }
                catch (e: co.smartobjects.persistencia.excepciones.ErrorEliminandoEntidad)
                {
                    throw ErrorEliminandoEntidad(
                            e.entidad,
                            codigosError.ERROR_DE_BD_DESCONOCIDO
                                                )
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
            throw EntidadNoExiste(id.toString(), nombreEntidad, codigosError.NO_EXISTE)
        }
    }
}

internal interface RecursoEliminarPorIdConAutenticacionGlobal<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoEliminarPorId<EntidadNegocio, TipoEntidadDTO, TipoId>
{
    val manejadorSeguridad: ManejadorSeguridad

    override fun eliminarPorId()
    {
        manejadorSeguridad.verificarUsuarioGlobalEstaAutenticado()
        super.eliminarPorId()
    }
}

internal interface RecursoEliminarPorIdDeCliente<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoEliminarPorId<EntidadNegocio, TipoEntidadDTO, TipoId>, RecursoDeCliente
{
    fun eliminarPorIdSegunIdCliente(idCliente: Long, id: TipoId): Boolean

    override fun eliminarPorId(id: TipoId): Boolean
    {
        manejadorSeguridad.verificarUsuarioDeClienteActualTienePermiso(darInformacionPermisoParaEliminacionSegunNombrePermiso(nombrePermiso).aPermisoBackSegunIdCliente(idCliente))
        return eliminarPorIdSegunIdCliente(idCliente, id)
    }

    override fun eliminarPorId()
    {
        try
        {
            super.eliminarPorId()
        }
        catch (ex: EsquemaNoExiste)
        {
            throw EntidadNoExiste(idCliente, Cliente.NOMBRE_ENTIDAD, ClienteDTO.CodigosError.NO_EXISTE, ex)
        }
    }
}

internal interface RecursoEliminarPorIdDeClienteConRestriccion<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoEliminarPorIdDeCliente<EntidadNegocio, TipoEntidadDTO, TipoId>, RecursoDeCliente
{
    val codigoDeErrorRestriccionIncumplidaEliminacion: Int

    override fun eliminarPorId(id: TipoId): Boolean
    {
        manejadorSeguridad.verificarUsuarioDeClienteActualTienePermiso(darInformacionPermisoParaEliminacionSegunNombrePermiso(nombrePermiso).aPermisoBackSegunIdCliente(idCliente))
        return eliminarPorIdSegunIdCliente(idCliente, id)
    }

    override fun eliminarPorId()
    {
        try
        {
            super.eliminarPorId()
        }
        catch (ex: ErrorEliminacionViolacionDeRestriccion)
        {
            throw ErrorEliminandoEntidad(ex.message!!, codigoDeErrorRestriccionIncumplidaEliminacion, ex)
        }
    }
}