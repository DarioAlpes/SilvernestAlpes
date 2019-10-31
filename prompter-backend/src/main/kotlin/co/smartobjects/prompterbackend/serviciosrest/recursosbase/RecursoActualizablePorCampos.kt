package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.persistencia.excepciones.ErrorActualizacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.ErrorAlActualizarCampo
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.prompterbackend.excepciones.EntidadInvalida
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.excepciones.ErrorActualizandoEntidad
import co.smartobjects.prompterbackend.excepciones.darCodigoInterno
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionParcialSegunNombrePermiso
import co.smartobjects.red.modelos.EntidadDTOParcial
import co.smartobjects.red.modelos.clientes.ClienteDTO
import javax.ws.rs.PATCH

internal interface RecursoActualizablePorCampos<EntidadNegocio, in EntidadDTO : EntidadDTOParcial<EntidadNegocio>, TipoId>
    : RecursoConErroresDTO
      , RecursoEspecifico<TipoId>
{
    fun actualizarEntidadPorCamposIndividuales(id: TipoId, camposModificables: Map<String, CampoModificable<EntidadNegocio, *>>)

    @PATCH
    fun patch(dto: EntidadDTO)
    {
        try
        {
            actualizarEntidadPorCamposIndividuales(id, dto.aMapaDeCamposAModificables())
        }
        catch (e: co.smartobjects.persistencia.excepciones.EntidadNoExiste)
        {
            throw EntidadNoExiste(e.id, e.entidad, codigosError.NO_EXISTE)
        }
        catch (ex: EntidadMalInicializada)
        {
            throw EntidadInvalida(ex.message!!, ex.darCodigoInterno(codigosError.ERROR_DE_ENTIDAD_DESCONOCIDO), ex)
        }
        catch (ex: ErrorDeCreacionActualizacionEntidad)
        {
            throw EntidadInvalida("Error creando $nombreEntidad: ${ex.message}", codigosError.ERROR_DE_BD_DESCONOCIDO, ex)
        }
        catch (ex: ErrorAlActualizarCampo)
        {
            throw ErrorActualizandoEntidad(ex.message!!, codigosError.ERROR_DE_BD_DESCONOCIDO, ex)
        }
    }
}

internal interface RecursoActualizablePorCamposConAutenticacionGlobal<EntidadNegocio, in EntidadDTO : EntidadDTOParcial<EntidadNegocio>, TipoId>
    : RecursoActualizablePorCampos<EntidadNegocio, EntidadDTO, TipoId>
{
    val manejadorSeguridad: ManejadorSeguridad

    override fun patch(dto: EntidadDTO)
    {
        manejadorSeguridad.verificarUsuarioGlobalEstaAutenticado()
        super.patch(dto)
    }
}


internal interface RecursoActualizablePorCamposDeClienteConAutenticacionCondicional<EntidadNegocio, in EntidadDTO : EntidadDTOParcial<EntidadNegocio>, TipoId>
    : RecursoActualizablePorCampos<EntidadNegocio, EntidadDTO, TipoId>,
      RecursoDeCliente
{
    fun seDebeValidarPermiso(campos: Map<String, CampoModificable<EntidadNegocio, *>>): Boolean

    fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: TipoId, camposModificables: Map<String, CampoModificable<EntidadNegocio, *>>)

    override fun actualizarEntidadPorCamposIndividuales(id: TipoId, camposModificables: Map<String, CampoModificable<EntidadNegocio, *>>)
    {
        if (seDebeValidarPermiso(camposModificables))
        {
            manejadorSeguridad.verificarUsuarioDeClienteActualTienePermiso(
                    darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(nombrePermiso)
                        .aPermisoBackSegunIdCliente(idCliente)
                                                                          )
        }
        actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente, id, camposModificables)
    }

    override fun patch(dto: EntidadDTO)
    {
        return try
        {
            super.patch(dto)
        }
        catch (ex: EsquemaNoExiste)
        {
            throw EntidadNoExiste(idCliente, Cliente.NOMBRE_ENTIDAD, ClienteDTO.CodigosError.NO_EXISTE, ex)
        }
    }
}


internal interface RecursoActualizablePorCamposDeCliente<EntidadNegocio, in EntidadDTO : EntidadDTOParcial<EntidadNegocio>, TipoId>
    : RecursoActualizablePorCampos<EntidadNegocio, EntidadDTO, TipoId>,
      RecursoDeCliente
{
    fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: TipoId, camposModificables: Map<String, CampoModificable<EntidadNegocio, *>>)

    override fun actualizarEntidadPorCamposIndividuales(id: TipoId, camposModificables: Map<String, CampoModificable<EntidadNegocio, *>>)
    {
        manejadorSeguridad.verificarUsuarioDeClienteActualTienePermiso(darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(nombrePermiso).aPermisoBackSegunIdCliente(idCliente))
        actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente, id, camposModificables)
    }

    override fun patch(dto: EntidadDTO)
    {
        return try
        {
            super.patch(dto)
        }
        catch (ex: EsquemaNoExiste)
        {
            throw EntidadNoExiste(idCliente, Cliente.NOMBRE_ENTIDAD, ClienteDTO.CodigosError.NO_EXISTE, ex)
        }
    }
}

internal interface RecursoActualizablePorCamposDeClienteConRestriccion<EntidadNegocio, in EntidadDTO : EntidadDTOParcial<EntidadNegocio>, TipoId>
    : RecursoActualizablePorCamposDeCliente<EntidadNegocio, EntidadDTO, TipoId>
{
    val codigoDeErrorRestriccionIncumplida: Int

    override fun patch(dto: EntidadDTO)
    {
        return try
        {
            super.patch(dto)
        }
        catch (ex: ErrorActualizacionViolacionDeRestriccion)
        {
            throw ErrorActualizandoEntidad(ex.message!!, codigoDeErrorRestriccionIncumplida, ex)
        }
    }
}