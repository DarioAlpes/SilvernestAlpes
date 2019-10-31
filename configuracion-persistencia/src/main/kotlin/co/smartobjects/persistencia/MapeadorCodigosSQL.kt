package co.smartobjects.persistencia

import co.smartobjects.persistencia.excepciones.*
import java.sql.SQLException

abstract class MapeadorCodigosSQL
{
    abstract protected fun transformarCodigoDeErrorAEnumeracion(codigoError: String): ErroresSQL

    // Se debe sobrescribir en sqlite
    open protected fun extraerCodigoError(errorSQL: SQLException): String
    {
        var codigoErrorSQL = 0
        var excepcion: SQLException? = errorSQL
        while (codigoErrorSQL == 0 && excepcion != null)
        {
            codigoErrorSQL = excepcion.errorCode
            excepcion = when
            {
                excepcion.cause is SQLException -> excepcion.cause as SQLException
                else                            -> null
            }
        }
        return codigoErrorSQL.toString()
    }

    fun parsearCodigoDeErrorSQLParaCreacion(errorSQL: SQLException, nombreEntidad: String): ErrorDAO
    {
        return parsearCodigoDeErrorSQLAErrorAPI(errorSQL, nombreEntidad, AccionSQL.CREACION)
    }

    fun parsearCodigoDeErrorSQLParaConsulta(errorSQL: SQLException, nombreEntidad: String): ErrorDAO
    {
        return parsearCodigoDeErrorSQLAErrorAPI(errorSQL, nombreEntidad, AccionSQL.CONSULTA)
    }

    fun parsearCodigoDeErrorSQLParaActualizacion(errorSQL: SQLException, nombreEntidad: String): ErrorDAO
    {
        return parsearCodigoDeErrorSQLAErrorAPI(errorSQL, nombreEntidad, AccionSQL.EDICION)
    }

    fun parsearCodigoDeErrorSQLParaEliminacion(errorSQL: SQLException, nombreEntidad: String): ErrorDAO
    {
        return parsearCodigoDeErrorSQLAErrorAPI(errorSQL, nombreEntidad, AccionSQL.ELIMINACION)
    }

    private fun parsearCodigoDeErrorSQLAErrorAPI(errorSQL: SQLException, nombreEntidad: String, accionSQL: AccionSQL): ErrorDAO
    {
        if (errorSQL.cause is ErrorDAO)
        {
            return errorSQL.cause as ErrorDAO
        }

        val codigoDirecto =
                if (errorSQL.sqlState != null)
                {
                    transformarCodigoDeErrorAEnumeracion(errorSQL.sqlState)
                }
                else
                {
                    ErroresSQL.DESCONOCIDO
                }

        val codigoErrorSQL =
                if (codigoDirecto != ErroresSQL.DESCONOCIDO)
                {
                    codigoDirecto
                }
                else
                {

                    transformarCodigoDeErrorAEnumeracion(extraerCodigoError(errorSQL))
                }

        return when (codigoErrorSQL)
        {
            ErroresSQL.VIOLACION_UNIQUE, ErroresSQL.VIOLACION_PK -> ErrorCreacionActualizacionPorDuplicidad(nombreEntidad, errorSQL)
            ErroresSQL.VIOLACION_FK                              -> ErrorDeLlaveForanea(null as String?, nombreEntidad, errorSQL)
            ErroresSQL.DESCONOCIDO                               ->
            {
                when (accionSQL)
                {
                    AccionSQL.CREACION    -> ErrorDeCreacionActualizacionEntidad(nombreEntidad, errorSQL)
                    AccionSQL.EDICION     -> ErrorDeCreacionActualizacionEntidad(nombreEntidad, errorSQL)
                    AccionSQL.ELIMINACION -> ErrorEliminandoEntidad(nombreEntidad, errorSQL)
                    AccionSQL.CONSULTA    -> ErrorDeConsultaEntidad(nombreEntidad, errorSQL)
                }
            }
        }
    }

    private enum class AccionSQL
    {
        CREACION, EDICION, ELIMINACION, CONSULTA
    }

    enum class ErroresSQL
    {
        VIOLACION_PK, VIOLACION_UNIQUE, VIOLACION_FK, DESCONOCIDO
    }
}