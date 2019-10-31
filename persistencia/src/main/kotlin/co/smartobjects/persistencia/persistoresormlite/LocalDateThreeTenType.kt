package co.smartobjects.persistencia.persistoresormlite

import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.field.FieldType
import com.j256.ormlite.field.SqlType
import com.j256.ormlite.field.types.BaseDataType
import com.j256.ormlite.misc.SqlExceptionUtil
import com.j256.ormlite.support.DatabaseResults
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.sql.SQLException


class LocalDateThreeTenType : BaseDataType
{
    companion object
    {
        @Volatile
        private var instancia: LocalDateThreeTenType? = null
        @Volatile
        private var zonedDateTimeClass: Class<*>? = null
        private val nombreClasesAsociadas = arrayOf("org.threeten.bp.LocalDate")

        @[JvmStatic Suppress("unused")]
        fun getSingleton(): LocalDateThreeTenType
        {
            return instancia ?: synchronized(this) { instancia ?: LocalDateThreeTenType().also { instancia = it } }
        }

        @JvmStatic
        fun darClase(): Class<*>
        {
            return zonedDateTimeClass ?: synchronized(this) { zonedDateTimeClass ?: Class.forName(nombreClasesAsociadas.first()).also { zonedDateTimeClass = it } }
        }

        fun deNegocio(fechaHora: LocalDate): String
        {
            return fechaHora.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
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
        val objetoFinal = javaObject as? LocalDate ?: LocalDate.parse(javaObject as String)

        return deNegocio(objetoFinal)
    }

    override fun getDefaultWidth(): Int = 64

    @Throws(SQLException::class)
    override fun parseDefaultString(fieldType: FieldType, defaultStr: String): Any
    {
        try
        {
            return LocalDate.parse(defaultStr)
        }
        catch (e: NumberFormatException)
        {
            throw SqlExceptionUtil.create("Problems with field $fieldType parsing default LocalDate value: $defaultStr", e)
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
            LocalDate.parse((sqlArg as String))
        }
    }

    override fun isValidForVersion() = true

    override fun getPrimaryClass(): Class<*>?
    {
        return try
        {
            darClase()
        }
        catch (e: ClassNotFoundException)
        {
            null
        }

    }

    @Throws(SQLException::class)
    override fun moveToNextValue(currentValue: Any?): Any
    {
        val zonedDateTimeNuevo = LocalDate.now(ZONA_HORARIA_POR_DEFECTO)
        if (currentValue == null)
        {
            return zonedDateTimeNuevo
        }
        val zonedDateTimeActual = LocalDate.parse((currentValue as String), DateTimeFormatter.ISO_OFFSET_DATE)
        return if (zonedDateTimeNuevo == zonedDateTimeActual)
        {
            zonedDateTimeNuevo.plusDays(1)
        }
        else
        {
            zonedDateTimeNuevo
        }
    }
}