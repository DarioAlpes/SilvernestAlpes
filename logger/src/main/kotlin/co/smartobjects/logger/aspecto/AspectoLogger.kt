package co.smartobjects.logger.aspecto

import co.smartobjects.logger.FabricaLogger
import co.smartobjects.logger.InformacionLog
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut


internal class InformacionLogAspecto(private val joinPoint: JoinPoint, override val contexto: String) : InformacionLog()
{
    override val archivo: String by lazy { joinPoint.sourceLocation.fileName }
    override val linea: Int by lazy { joinPoint.sourceLocation.line }
    override val toStringDeInstancia: String by lazy { joinPoint.target?.toString() ?: "Función a nivel de archivo" }
    override val clase: String by lazy { joinPoint.signature.declaringTypeName }
    override val metodo: String by lazy { joinPoint.signature.name }
    override val argumentosMetodo: Array<Any?> by lazy { joinPoint.args }
}

@Aspect
class AspectoLogger
{
    @Pointcut(value = "call(@co.smartobjects.logger.aspecto.Loggear * *.*(..))")
    fun metodoConLogger()
    {
    }

    @Around(value = "metodoConLogger() && @annotation(informacionLogger)")
    fun alrededorMetodo(joinPoint: ProceedingJoinPoint, informacionLogger: Loggear): Any?
    {
        FabricaLogger.loggerActual
            .marcarInicioDeMetodo(InformacionLogAspecto(joinPoint, informacionLogger.contexto))
        try
        {
            val retorno = joinPoint.proceed()

            FabricaLogger.loggerActual
                .marcarRetornoDeMetodo(InformacionLogAspecto(joinPoint, informacionLogger.contexto), retorno)

            return retorno
        }
        catch (excepcionLanzada: Throwable)
        {
            FabricaLogger.loggerActual
                .marcarExcepcionEnMetodo(InformacionLogAspecto(joinPoint, informacionLogger.contexto), excepcionLanzada)

            throw excepcionLanzada
        }
    }
}