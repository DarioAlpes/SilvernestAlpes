package co.smartobjects.logger

import java.util.*

interface LoggerInterno
{
    fun marcarInicioDeMetodo(informacionLog: InformacionLog)
    {
    }

    fun marcarRetornoDeMetodo(informacionLog: InformacionLog, retorno: Any?)
    {
    }

    fun marcarExcepcionEnMetodo(informacionLog: InformacionLog, excepcion: Throwable)
    {
    }

    fun trace(obj: Any)
    {
    }

    fun trace(message: String)
    {
    }

    fun trace(message: String, vararg arguments: Any)
    {
    }

    fun trace(exception: Throwable, message: String? = null)
    {
    }

    fun trace(exception: Throwable, message: String, vararg arguments: Any)
    {
    }

    fun debug(obj: Any)
    {
    }

    fun debug(message: String)
    {
    }

    fun debug(message: String, vararg arguments: Any)
    {
    }

    fun debug(exception: Throwable, message: String? = null)
    {
    }

    fun debug(exception: Throwable, message: String, vararg arguments: Any)
    {
    }

    fun info(obj: Any)
    {
    }

    fun info(message: String)
    {
    }

    fun info(message: String, vararg arguments: Any)
    {
    }

    fun info(exception: Throwable, message: String? = null)
    {
    }

    fun info(exception: Throwable, message: String, vararg arguments: Any)
    {
    }

    fun warn(obj: Any)
    {
    }

    fun warn(message: String)
    {
    }

    fun warn(message: String, vararg arguments: Any)
    {
    }

    fun warn(exception: Throwable, message: String? = null)
    {
    }

    fun warn(exception: Throwable, message: String, vararg arguments: Any)
    {
    }

    fun error(obj: Any)
    {
    }

    fun error(message: String)
    {
    }

    fun error(message: String, vararg arguments: Any)
    {
    }

    fun error(exception: Throwable, message: String? = null)
    {
    }

    fun error(exception: Throwable, message: String, vararg arguments: Any)
    {
    }
}

abstract class InformacionLog
{
    abstract val contexto: String
    abstract val archivo: String
    abstract val linea: Int
    abstract val toStringDeInstancia: String
    abstract val clase: String
    abstract val metodo: String
    abstract val argumentosMetodo: Array<Any?>

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is InformacionLog) return false

        if (contexto != other.contexto) return false
        if (archivo != other.archivo) return false
        if (linea != other.linea) return false
        if (toStringDeInstancia != other.toStringDeInstancia) return false
        if (clase != other.clase) return false
        if (metodo != other.metodo) return false
        if (!Arrays.equals(argumentosMetodo, other.argumentosMetodo)) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = contexto.hashCode()
        result = 31 * result + archivo.hashCode()
        result = 31 * result + linea
        result = 31 * result + toStringDeInstancia.hashCode()
        result = 31 * result + clase.hashCode()
        result = 31 * result + metodo.hashCode()
        result = 31 * result + Arrays.hashCode(argumentosMetodo)
        return result
    }

    override fun toString(): String
    {
        return "InformacionLog(contexto=$contexto, archivo=$archivo, linea=$linea, toStringDeInstancia=$toStringDeInstancia, clase=$clase, metodo=$metodo, argumentosMetodo=${Arrays.toString(argumentosMetodo)})"
    }
}

class FabricaLogger private constructor()
{
    companion object
    {
        @JvmStatic
        var loggerActual: LoggerInterno = object : LoggerInterno
        {}
            @JvmStatic @Synchronized set
            @JvmStatic get
    }
}