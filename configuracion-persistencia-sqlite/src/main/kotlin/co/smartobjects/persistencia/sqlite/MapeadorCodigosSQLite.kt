package co.smartobjects.persistencia.sqlite

import co.smartobjects.persistencia.MapeadorCodigosSQL
import org.sqlite.SQLiteException
import java.sql.SQLException

class MapeadorCodigosSQLite : MapeadorCodigosSQL()
{
    override fun extraerCodigoError(errorSQL: SQLException): String
    {
        var codigoErrorSQL = 0
        var excepcion: SQLException? = errorSQL

        while (codigoErrorSQL == 0 && excepcion != null)
        {
            codigoErrorSQL = if (excepcion is SQLiteException) excepcion.resultCode.code else excepcion.errorCode
            excepcion = if (excepcion.cause is SQLException) excepcion.cause as SQLException else null
        }

        return codigoErrorSQL.toString()
    }

    override fun transformarCodigoDeErrorAEnumeracion(codigoError: String): ErroresSQL
    {
        return when (codigoError)
        {
            "1555" -> ErroresSQL.VIOLACION_PK
            "2067" -> ErroresSQL.VIOLACION_UNIQUE
            "787"  -> ErroresSQL.VIOLACION_FK
            else   -> ErroresSQL.DESCONOCIDO
        }
    }
}