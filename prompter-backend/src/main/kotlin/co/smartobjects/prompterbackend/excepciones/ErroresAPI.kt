package co.smartobjects.prompterbackend.excepciones

import co.smartobjects.prompterbackend.seguridad.CODIGO_ERROR_CREDENCIALES_INCORRECTAS
import co.smartobjects.prompterbackend.seguridad.CODIGO_ERROR_USUARIO_NO_AUTENTICADO
import co.smartobjects.prompterbackend.seguridad.CODIGO_ERROR_USUARIO_NO_TIENE_PERMISO
import javax.ws.rs.core.Response


internal abstract class ErrorAPI(mensaje: String, val codigoHTTP: Response.Status, val codigoInterno: Int, causa: Throwable? = null) : Exception(mensaje, causa)

internal class EntidadNoExiste(id: String?, entidad: String, codigoInterno: Int, causa: Throwable? = null)
    : ErrorAPI("La entidade '$entidad' con id '$id' no existe", Response.Status.NOT_FOUND, codigoInterno, causa)
{
    constructor(id: Long?, entidad: String, codigoInterno: Int, causa: Throwable? = null)
            : this(id.toString(), entidad, codigoInterno, causa)
}

internal class EntidadNoExisteParaParametros(parametros: List<Any?>, entidad: String, codigoInterno: Int, causa: Throwable? = null)
    : ErrorAPI("La entidad '$entidad' no existe para los parámetros: ${parametros.joinToString(prefix = "{", postfix = "}")}", Response.Status.NOT_FOUND, codigoInterno, causa)

internal class EntidadReferenciadaNoExiste(codigoInterno: Int, causa: Throwable? = null)
    : ErrorAPI("Al menos una de las entidades referenciadas no existe", Response.Status.NOT_FOUND, codigoInterno, causa)

internal class EntidadInvalida constructor(razon: String, codigoInterno: Int, causa: Throwable? = null)
    : ErrorAPI(razon, Response.Status.BAD_REQUEST, codigoInterno, causa)

internal class ErrorDesconocido(razon: String, codigoInterno: Int, causa: Throwable? = null)
    : ErrorAPI(razon, Response.Status.INTERNAL_SERVER_ERROR, codigoInterno, causa)

internal class ErrorCambiandoIdDeEntidadReferenciada(entidad: String, idOriginal: String, idNuevo: String?, codigoInterno: Int, causa: Throwable? = null)
    : ErrorAPI("Error al intentar actualizar id la entidad '$entidad' con id $idOriginal a $idNuevo", Response.Status.BAD_REQUEST, codigoInterno, causa)

internal class ErrorEliminandoEntidad(entidad: String, codigoInterno: Int, causa: Throwable? = null)
    : ErrorAPI("Error al intentar eliminar la entidad '$entidad'", Response.Status.INTERNAL_SERVER_ERROR, codigoInterno, causa)
{
    constructor(id: Long, entidad: String, codigoInterno: Int, causa: Throwable? = null)
            : this("$entidad con id '$id'", codigoInterno, causa)
}

internal class ErrorActualizandoEntidad(mensaje: String, codigoInterno: Int, causa: Throwable?)
    : ErrorAPI(mensaje, Response.Status.BAD_REQUEST, codigoInterno, causa)
{
    constructor(entidad: String, id: String?, codigoInterno: Int, causa: Throwable?)
            : this("Error al intentar actualizar la entidad '$entidad' con id '$id'", codigoInterno, causa)
}

internal class UsuarioNoAutenticado
    : ErrorAPI("Usuario no autenticado", Response.Status.UNAUTHORIZED, CODIGO_ERROR_USUARIO_NO_AUTENTICADO)

internal class UsuarioNoTienePermiso
    : ErrorAPI("El usuario no tiene el permisos para realizar la acción", Response.Status.FORBIDDEN, CODIGO_ERROR_USUARIO_NO_TIENE_PERMISO)

internal class CredencialesIncorrectas
    : ErrorAPI("Usuario incorrecto en Silvernest", Response.Status.UNAUTHORIZED, CODIGO_ERROR_CREDENCIALES_INCORRECTAS)
