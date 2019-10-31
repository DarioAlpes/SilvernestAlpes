package co.smartobjects.persistencia.excepciones

import java.sql.SQLException

abstract class ErrorDAO(mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa)

class ErorCreandoTabla(clase: Class<*>, nombreEntidad: String, causa: SQLException)
    : ErrorDAO("Error creando tabla para la clase ${clase.name} para la entidad $nombreEntidad", causa)

class EsquemaNoExiste(esquema: String, causa: Throwable? = null) : ErrorDAO("El esquema '$esquema' no existe", causa)

class CampoActualizableDesconocido(nombreCampo: String, nombreEntidad: String, causa: Throwable? = null)
    : ErrorDAO("El campo '$nombreCampo' de la entidad '$nombreEntidad' no se puede mapear a campo en persistencia", causa)

open class ErrorDeCreacionActualizacionEntidad(val entidad: String, causa: Throwable? = null)
    : ErrorDAO("Error creando/actualizando la entidad '$entidad'", causa)

class ErrorDeConsultaEntidad(val entidad: String, causa: Throwable? = null)
    : ErrorDAO("Error consultando la entidad '$entidad'", causa)

class ErrorCreacionActualizacionPorDuplicidad(entidad: String, causa: Throwable? = null)
    : ErrorDeCreacionActualizacionEntidad(entidad, causa)

class ErrorAlActualizarCampo(val entidad: String, val campo: String, val razon: String, causa: Throwable? = null)
    : ErrorDAO("Error al actualizar el campo '$campo' de la entidad '$entidad': $razon", causa)

class ErrorDeLlaveForanea constructor(val id: String?, val entidad: String, causa: Throwable? = null)
    : ErrorDAO("Error de llave foránea para la entidad '$entidad'", causa)
{
    constructor(id: Long?, entidad: String, causa: Throwable? = null) : this(id.toString(), entidad, causa)
}

class ErrorDeJerarquiaPorCiclo(val entidad: String, idEntidad: Long, idNuevoPadre: Long)
    : ErrorDAO("Error actualizando el padre de la entidad '$entidad' con id '$idEntidad' a '$idNuevoPadre'. Genera un ciclo en la jerarquia")

class EntidadNoExiste(val id: String?, val entidad: String, causa: Throwable? = null)
    : ErrorDAO("La entidad '$entidad' con id '$id' no existe", causa)
{
    constructor(id: Long?, entidad: String, causa: Throwable? = null) :
            this(id?.toString(), entidad, causa)
}

class ErrorEliminandoEntidad(val entidad: String, causa: Throwable? = null)
    : ErrorDAO("Error eliminando la entidad '$entidad'", causa)
{
    constructor(id: Long, entidad: String, causa: Throwable? = null) : this("'$entidad' con id '$id'", causa)
    constructor(id: String, entidad: String, causa: Throwable? = null) : this("'$entidad' con id '$id'", causa)
}

class ErrorCreacionViolacionDeRestriccion constructor(val entidad: String, val restriccion: String, valoresUsados: Array<String>?)
    : ErrorDAO(
        "Al crear la entidad '$entidad' se viola la restricción: '$restriccion'." +
        if (valoresUsados != null) " Los valores usados fueron: ${valoresUsados.joinToString(", ")}" else ""
              )

class ErrorActualizacionViolacionDeRestriccion(val entidad: String, idEntidad: String, restriccion: String, valoresUsados: Array<String>?)
    : ErrorDAO(
        "Al actualizar la entidad '$entidad' con id '$idEntidad' se viola la restricción: '$restriccion'." +
        if (valoresUsados != null) " Los valores usados fueron: ${valoresUsados.joinToString(", ")}" else ""
              )

class ErrorEliminacionViolacionDeRestriccion(val entidad: String, idEntidad: String, restriccion: String, valoresUsados: Array<String>?)
    : ErrorDAO(
        "Al eliminar la entidad '$entidad' con id '$idEntidad' se viola la restricción: '$restriccion'." +
        if (valoresUsados != null) " Los valores usados fueron: ${valoresUsados.joinToString(", ")}" else ""
              )
