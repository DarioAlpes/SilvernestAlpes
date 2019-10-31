package co.smartobjects.persistencia.h2

import co.smartobjects.persistencia.MapeadorCodigosSQL

internal class MapeadorCodigosSQLH2 : MapeadorCodigosSQL()
{
    override fun transformarCodigoDeErrorAEnumeracion(codigoError: String): ErroresSQL
    {
        return when (codigoError)
        {
            "23505" -> ErroresSQL.VIOLACION_UNIQUE
            "23503" -> ErroresSQL.VIOLACION_FK
            "23506" -> ErroresSQL.VIOLACION_FK
            else    -> ErroresSQL.DESCONOCIDO
        }
    }
}