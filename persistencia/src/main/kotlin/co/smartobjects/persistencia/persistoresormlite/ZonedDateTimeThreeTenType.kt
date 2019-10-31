package co.smartobjects.persistencia.persistoresormlite

import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.field.FieldType
import com.j256.ormlite.field.SqlType
import com.j256.ormlite.field.types.BaseDataType
import com.j256.ormlite.misc.SqlExceptionUtil
import com.j256.ormlite.support.DatabaseResults
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZonedDateTime
import java.sql.SQLException


class ZonedDateTimeThreeTenType : BaseDataType
{
    companion object
    {
        @Volatile
        private var instancia: ZonedDateTimeThreeTenType? = null
        private val nombreClasesAsociadas = arrayOf("org.threeten.bp.ZonedDateTime")

        @[Suppress("unused") JvmStatic]
        fun getSingleton(): ZonedDateTimeThreeTenType
        {
            return instancia ?: synchronized(this) { instancia ?: ZonedDateTimeThreeTenType().also { instancia = it } }
        }

        fun deNegocio(fechaHora: ZonedDateTime): String
        {
            return fechaHora.toOffsetDateTime().toString()
        }

        fun deBD(timestamp: String): ZonedDateTime = OffsetDateTime.parse(timestamp).atZoneSameInstant(ZONA_HORARIA_POR_DEFECTO)
    }

    private constructor() : super(SqlType.STRING)
    private constructor(sqlType: SqlType, classes: Array<Class<*>>) : super(sqlType, classes)

    override fun getAssociatedClassNames(): Array<String>
    {
        return nombreClasesAsociadas
    }

    @Throws(SQLException::class)
    override fun javaToSqlArg(fieldType: FieldType?, javaObject: Any): Any?
    {
        val objetoFinal = javaObject as? ZonedDateTime ?: ZonedDateTime.parse(javaObject as String)

        return deNegocio(objetoFinal)
    }

    override fun getDefaultWidth(): Int = 64

    @Throws(SQLException::class)
    override fun parseDefaultString(fieldType: FieldType, defaultStr: String): Any
    {
        try
        {
            return deBD(defaultStr)
        }
        catch (e: NumberFormatException)
        {
            throw SqlExceptionUtil.create("Error con el campo '$fieldType'. No se pudo parsear OffsetDateTime value: $defaultStr", e)
        }
    }

    @Throws(SQLException::class)
    override fun resultToSqlArg(fieldType: FieldType, results: DatabaseResults, columnPos: Int): Any?
    {
        return results.getString(columnPos)
    }

    @Throws(SQLException::class)
    override fun sqlArgToJava(fieldType: FieldType?, sqlArg: Any?, columnPos: Int): Any?
    {
        return if (sqlArg == null)
        {
            null
        }
        else
        {
            deBD(sqlArg as String)
        }
    }

    override fun isValidForVersion() = true

    override fun getPrimaryClass(): Class<*>?
    {
        return org.threeten.bp.ZonedDateTime::class.java
    }

    @Throws(SQLException::class)
    override fun moveToNextValue(currentValue: Any?): Any
    {
        val zonedDateTimeNuevo = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
        if (currentValue == null)
        {
            return zonedDateTimeNuevo
        }
        val zonedDateTimeActual = deBD(currentValue as String)
        return if (zonedDateTimeNuevo == zonedDateTimeActual)
        {
            zonedDateTimeNuevo.plusNanos(1)
        }
        else
        {
            zonedDateTimeNuevo
        }
    }
}