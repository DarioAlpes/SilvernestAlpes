package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.prompterbackend.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import javax.ws.rs.PUT

internal inline fun <T> ejecutarFuncionActualizacionTransformandoExcepcionesAExcepcionesBackend(
        funcionAEjecutar: () -> T,
        darErrorBackendParaErrorDeLlaveForanea: (ex: ErrorDeLlaveForanea) -> ErrorAPI,
        nombreEntidad: String,
        codigosError: CodigosErrorDTO): T
{
    try
    {
        return funcionAEjecutar()
    }
    catch (e: co.smartobjects.persistencia.excepciones.EntidadNoExiste)
    {
        throw EntidadNoExiste(e.id, e.entidad, codigosError.NO_EXISTE)
    }
    catch (ex: ErrorCreacionActualizacionPorDuplicidad)
    {
        throw EntidadInvalida("Ya existe un(a) $nombreEntidad en base de datos", codigosError.ENTIDAD_DUPLICADA_EN_BD, ex)
    }
    catch (ex: ErrorDeCreacionActualizacionEntidad)
    {
        throw EntidadInvalida("Error creando $nombreEntidad: ${ex.message}", codigosError.ERROR_DE_BD_DESCONOCIDO, ex)
    }
    catch (ex: ErrorDeLlaveForanea)
    {
        throw darErrorBackendParaErrorDeLlaveForanea(ex)
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun darErrorBackendParaErrorDeLlaveForanea(
        ex: ErrorDeLlaveForanea,
        codigosError: CodigosErrorDTO): ErrorAPI
{
    return EntidadReferenciadaNoExiste(codigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, ex)
}

internal interface RecursoActualizable<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : TransformacionesConDTO<EntidadNegocio, TipoEntidadDTO>
      , RecursoConErroresDTO
      , RecursoEspecifico<TipoId>
{
    fun actualizarEntidadDeNegocio(entidad: EntidadNegocio): EntidadNegocio
    fun sustituirIdEnEntidad(idAUsar: TipoId, dto: TipoEntidadDTO): TipoEntidadDTO
    fun darErrorBackendParaErrorDeLlaveForanea(entidad: EntidadNegocio, ex: ErrorDeLlaveForanea): ErrorAPI = darErrorBackendParaErrorDeLlaveForanea(ex, codigosError)

    @PUT
    fun actualizar(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        val entidadDeNegocio = ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(
                codigosError,
                { sustituirIdEnEntidad(id, dto).aEntidadDeNegocio() }
                                                                                                                  )
        return ejecutarFuncionActualizacionTransformandoExcepcionesAExcepcionesBackend(
                { transformarHaciaDTO(actualizarEntidadDeNegocio(entidadDeNegocio)) },
                { darErrorBackendParaErrorDeLlaveForanea(entidadDeNegocio, it) },
                nombreEntidad,
                codigosError
                                                                                      )
    }
}

internal interface RecursoActualizableConAutenticacionGlobal<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoActualizable<EntidadNegocio, TipoEntidadDTO, TipoId>
{
    val manejadorSeguridad: ManejadorSeguridad

    override fun actualizar(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        manejadorSeguridad.verificarUsuarioGlobalEstaAutenticado()
        return super.actualizar(dto)
    }
}

internal interface RecursoActualizableDeCliente<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoActualizable<EntidadNegocio, TipoEntidadDTO, TipoId>,
      RecursoDeCliente
{
    fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: TipoId, entidad: EntidadNegocio): EntidadNegocio

    override fun actualizarEntidadDeNegocio(entidad: EntidadNegocio): EntidadNegocio
    {
        return actualizarEntidadDeNegocioSegunIdCliente(idCliente, id, entidad)
    }

    override fun actualizar(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        return ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                manejadorSeguridad,
                darInformacionPermisoParaActualizacionSegunNombrePermiso(nombrePermiso),
                idCliente,
                { super.actualizar(dto) }
                                                                                      )
    }
}

internal interface RecursoActualizableColeccion<
        EntidadNegocioEntrada, TipoEntidadDTOEntrada : EntidadDTO<EntidadNegocioEntrada>,
        TipoIdNegocio,
        out EntidadNegocioRetorno, EntidadColeccionNegocio : Collection<EntidadNegocioRetorno>, out EntidadDTORetorno : EntidadDTO<EntidadNegocioRetorno>
        >
    : TransformacionesConDTOColeccion<EntidadNegocioRetorno, EntidadColeccionNegocio, EntidadDTORetorno>
      , RecursoConErroresDTO
      , RecursoEspecifico<TipoIdNegocio>
{
    fun actualizarEntidadDeNegocio(entidad: EntidadNegocioEntrada): EntidadColeccionNegocio
    fun sustituirIdEnEntidad(idAUsar: TipoIdNegocio, dto: TipoEntidadDTOEntrada): TipoEntidadDTOEntrada
    fun darErrorBackendParaErrorDeLlaveForanea(entidad: EntidadNegocioEntrada, ex: ErrorDeLlaveForanea): ErrorAPI = darErrorBackendParaErrorDeLlaveForanea(ex, codigosError)

    @PUT
    fun actualizar(dto: TipoEntidadDTOEntrada): Collection<EntidadDTORetorno>
    {
        val entidadDeNegocio = ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(
                codigosError,
                { sustituirIdEnEntidad(id, dto).aEntidadDeNegocio() }
                                                                                                                  )

        return ejecutarFuncionActualizacionTransformandoExcepcionesAExcepcionesBackend(
                { transformarHaciaDTO(actualizarEntidadDeNegocio(entidadDeNegocio)) },
                { darErrorBackendParaErrorDeLlaveForanea(entidadDeNegocio, it) },
                nombreEntidad,
                codigosError
                                                                                      )
    }
}

internal interface RecursoActualizableColeccionDeCliente<
        EntidadNegocioEntrada, TipoEntidadDTOEntrada : EntidadDTO<EntidadNegocioEntrada>,
        TipoIdNegocio,
        out EntidadNegocioRetorno, EntidadColeccionNegocio : Collection<EntidadNegocioRetorno>, out EntidadDTORetorno : EntidadDTO<EntidadNegocioRetorno>
        >
    : RecursoActualizableColeccion<EntidadNegocioEntrada, TipoEntidadDTOEntrada, TipoIdNegocio, EntidadNegocioRetorno, EntidadColeccionNegocio, EntidadDTORetorno>,
      RecursoDeCliente
{
    fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: TipoIdNegocio, entidad: EntidadNegocioEntrada): EntidadColeccionNegocio

    override fun actualizarEntidadDeNegocio(entidad: EntidadNegocioEntrada): EntidadColeccionNegocio
    {
        return actualizarEntidadDeNegocioSegunIdCliente(idCliente, id, entidad)
    }

    override fun actualizar(dto: TipoEntidadDTOEntrada): Collection<EntidadDTORetorno>
    {
        return ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                manejadorSeguridad,
                darInformacionPermisoParaActualizacionSegunNombrePermiso(nombrePermiso),
                idCliente,
                { super.actualizar(dto) }
                                                                                      )
    }
}

internal interface RecursoActualizableJerarquicoDeCliente<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoActualizableDeCliente<EntidadNegocio, TipoEntidadDTO, TipoId>
{
    val codigoDeErrorCicloJerarquia: Int

    override fun actualizar(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        return try
        {
            super.actualizar(dto)
        }
        catch (ex: ErrorDeJerarquiaPorCiclo)
        {
            throw EntidadInvalida(ex.message!!, codigoDeErrorCicloJerarquia, ex)
        }
    }
}

internal interface RecursoActualizableConRestriccionDeCliente<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoActualizableDeCliente<EntidadNegocio, TipoEntidadDTO, TipoId>
{
    val codigoDeErrorRestriccionIncumplida: Int

    override fun actualizar(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        return try
        {
            super.actualizar(dto)
        }
        catch (ex: ErrorActualizacionViolacionDeRestriccion)
        {
            throw EntidadInvalida(ex.message!!, codigoDeErrorRestriccionIncumplida, ex)
        }
    }
}

internal interface RecursoActualizableQueCreaConRestriccionDeCliente<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoActualizableConRestriccionDeCliente<EntidadNegocio, TipoEntidadDTO, TipoId>
{
    override fun actualizar(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        return try
        {
            super.actualizar(dto)
        }
        catch (ex: ErrorCreacionViolacionDeRestriccion)
        {
            throw EntidadInvalida(ex.message!!, codigoDeErrorRestriccionIncumplida, ex)
        }
    }
}

internal interface RecursoActualizableConIdMutableDeCliente<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoActualizableDeCliente<EntidadNegocio, TipoEntidadDTO, TipoId>
{
    override fun sustituirIdEnEntidad(idAUsar: TipoId, dto: TipoEntidadDTO): TipoEntidadDTO = dto

    fun actualizarEntidadDeNegocioSegunIdEIdCliente(idCliente: Long, id: TipoId, entidad: EntidadNegocio): EntidadNegocio
    fun darIdDeEntidadDeNegocio(entidad: EntidadNegocio): TipoId

    override fun darErrorBackendParaErrorDeLlaveForanea(entidad: EntidadNegocio, ex: ErrorDeLlaveForanea): ErrorAPI
    {
        throw ErrorCambiandoIdDeEntidadReferenciada(ex.entidad, id.toString(), darIdDeEntidadDeNegocio(entidad).toString(), codigosError.ENTIDAD_REFERENCIADA)
    }

    override fun actualizarEntidadDeNegocioSegunIdCliente(idCliente: Long, idAUsar: TipoId, entidad: EntidadNegocio): EntidadNegocio
    {
        return actualizarEntidadDeNegocioSegunIdEIdCliente(idCliente, idAUsar, entidad)
    }
}

internal interface RecursoActualizableConRestriccionEIdMutableDeCliente<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, TipoId>
    : RecursoActualizableConRestriccionDeCliente<EntidadNegocio, TipoEntidadDTO, TipoId>, RecursoActualizableConIdMutableDeCliente<EntidadNegocio, TipoEntidadDTO, TipoId>