package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.entidades.EntidadReferenciable
import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.entidades.excepciones.EntidadMalInicializada
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.prompterbackend.excepciones.EntidadInvalida
import co.smartobjects.prompterbackend.excepciones.EntidadNoExiste
import co.smartobjects.prompterbackend.excepciones.EntidadReferenciadaNoExiste
import co.smartobjects.prompterbackend.excepciones.darCodigoInterno
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.usuarios.InformacionPermiso
import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaCreacionSegunNombrePermiso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.clientes.ClienteDTO
import javax.ws.rs.POST

internal inline fun <T> ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(
        codigosError: CodigosErrorDTO,
        funcionAEjecutar: () -> T): T
{
    return try
    {
        funcionAEjecutar()
    }
    catch (ex: EntidadMalInicializada)
    {
        throw EntidadInvalida(ex.message!!, ex.darCodigoInterno(codigosError.ERROR_DE_ENTIDAD_DESCONOCIDO), ex)
    }
}

internal fun <T> ejecutarFuncionCreacionTransformandoExcepcionesPersistenciaAExcepcionesBackend(
        nombreEntidad: String,
        codigosError: CodigosErrorDTO,
        funcionAEjecutar: () -> T): T
{
    return try
    {
        funcionAEjecutar()
    }
    catch (ex: ErrorCreacionActualizacionPorDuplicidad)
    {
        throw EntidadInvalida("Ya existe una $nombreEntidad con el nombre dado", codigosError.ENTIDAD_DUPLICADA_EN_BD, ex)
    }
    catch (ex: ErrorDeCreacionActualizacionEntidad)
    {
        throw EntidadInvalida("Error creando $nombreEntidad: ${ex.message}", codigosError.ERROR_DE_BD_DESCONOCIDO, ex)
    }
    catch (ex: ErrorDeLlaveForanea)
    {
        throw EntidadReferenciadaNoExiste(codigosError.ENTIDAD_REFERENCIADA_NO_EXISTE, ex)
    }
}

internal interface RecursoCreacion<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>>
    : TransformacionesConDTO<EntidadNegocio, TipoEntidadDTO>, RecursoConErroresDTO
{
    fun crearEntidadDeNegocio(entidad: EntidadNegocio): EntidadNegocio

    @POST
    fun crear(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        val entidadDeNegocio =
                ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(codigosError) {
                    dto.aEntidadDeNegocio()
                }

        return ejecutarFuncionCreacionTransformandoExcepcionesPersistenciaAExcepcionesBackend(nombreEntidad, codigosError) {
            transformarHaciaDTO(crearEntidadDeNegocio(entidadDeNegocio))
        }
    }
}

internal interface RecursoCreacionConAutenticacionGlobal<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>>
    : RecursoCreacion<EntidadNegocio, TipoEntidadDTO>
{
    val manejadorSeguridad: ManejadorSeguridad

    override fun crear(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        manejadorSeguridad.verificarUsuarioGlobalEstaAutenticado()
        return super.crear(dto)
    }
}

internal inline fun <T> ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
        manejadorSeguridad: ManejadorSeguridad,
        informacionPermiso: InformacionPermiso,
        idCliente: Long,
        funcionAEjecutar: () -> T): T
{
    manejadorSeguridad.verificarUsuarioDeClienteActualTienePermiso(informacionPermiso.aPermisoBackSegunIdCliente(idCliente))
    return try
    {
        funcionAEjecutar()
    }
    catch (ex: EsquemaNoExiste)
    {
        throw EntidadNoExiste(idCliente, Cliente.NOMBRE_ENTIDAD, ClienteDTO.CodigosError.NO_EXISTE, ex)
    }
}

internal interface RecursoCreacionDeCliente<EntidadCreacion, TipoEntidadDTO : EntidadDTO<EntidadCreacion>>
    : RecursoCreacion<EntidadCreacion, TipoEntidadDTO>,
      RecursoDeCliente
{
    fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: EntidadCreacion): EntidadCreacion

    override fun crearEntidadDeNegocio(entidad: EntidadCreacion): EntidadCreacion
    {
        return crearEntidadDeNegocioSegunIdCliente(idCliente, entidad)
    }

    override fun crear(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        return ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                manejadorSeguridad,
                darInformacionPermisoParaCreacionSegunNombrePermiso(nombrePermiso),
                idCliente
                                                                                      ) {
            super.crear(dto)
        }
    }
}

internal interface RecursoCreacionEnClienteAutoasignandoId<
        EntidadCreacion : EntidadReferenciable<TipoIdEntidad?, EntidadCreacion>,
        TipoIdEntidad,
        TipoEntidadDTO : EntidadDTO<EntidadCreacion>
        >
    : RecursoCreacionDeCliente<EntidadCreacion, TipoEntidadDTO>
{
    override fun crearEntidadDeNegocio(entidad: EntidadCreacion): EntidadCreacion
    {
        return super.crearEntidadDeNegocio(entidad.copiarConId(null))
    }
}

internal interface RecursoCreacionConRestriccionDeCliente<EntidadCreacion, TipoEntidadDTO : EntidadDTO<EntidadCreacion>>
    : RecursoCreacionDeCliente<EntidadCreacion, TipoEntidadDTO>
{
    val codigoDeErrorRestriccionIncumplida: Int

    override fun crear(dto: TipoEntidadDTO): TipoEntidadDTO
    {
        return try
        {
            super.crear(dto)
        }
        catch (ex: ErrorCreacionViolacionDeRestriccion)
        {
            throw EntidadInvalida(ex.message!!, codigoDeErrorRestriccionIncumplida, ex)
        }
    }
}

internal interface RecursoCreacionEnClienteConRestriccionYAutoasignandoId<
        EntidadCreacion : EntidadReferenciable<TipoIdEntidad?, EntidadCreacion>,
        TipoIdEntidad,
        TipoEntidadDTO : EntidadDTO<EntidadCreacion>
        >
    : RecursoCreacionEnClienteAutoasignandoId<EntidadCreacion, TipoIdEntidad, TipoEntidadDTO>,
      RecursoCreacionConRestriccionDeCliente<EntidadCreacion, TipoEntidadDTO>