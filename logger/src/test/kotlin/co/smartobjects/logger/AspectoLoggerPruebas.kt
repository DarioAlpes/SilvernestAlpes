package co.smartobjects.logger

import co.smartobjects.logger.aspecto.AspectoLogger
import co.smartobjects.logger.aspecto.InformacionLogAspecto
import co.smartobjects.logger.aspecto.Loggear
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.aspectj.lang.reflect.SourceLocation
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import sun.reflect.annotation.AnnotationParser
import java.util.*


class AspectoLoggerPruebas
{
    companion object
    {
        const val CONTEXTO_PRUEBA = "Contexto de prueba"
    }

    private val mockProceedingJoinPoint = mock(ProceedingJoinPoint::class.java, {
        throw RuntimeException("Llamado a\n\t${it.method.declaringClass.simpleName}.${it.method.name}(\n\t\t${it.arguments.joinToString(",\n\t\t")}\n\t)\n\tno mockeado")
    })

    private val mockSourceLocation = mock(SourceLocation::class.java, {
        throw RuntimeException("Llamado a\n\t${it.method.declaringClass.simpleName}.${it.method.name}(\n\t\t${it.arguments.joinToString(",\n\t\t")}\n\t)\n\tno mockeado")
    })

    private val mockSiganture = mock(Signature::class.java, {
        throw RuntimeException("Llamado a\n\t${it.method.declaringClass.simpleName}.${it.method.name}(\n\t\t${it.arguments.joinToString(",\n\t\t")}\n\t)\n\tno mockeado")
    })

    private val mockLoggerInterno = mock(LoggerInterno::class.java, {
        throw RuntimeException("Llamado a\n\t${it.method.declaringClass.simpleName}.${it.method.name}(\n\t\t${it.arguments.joinToString(",\n\t\t")}\n\t)\n\tno mockeado")
    })

    private lateinit var aspectoEnPrueba: AspectoLogger

    private fun darAnotacion(): Loggear
    {
        return AnnotationParser.annotationForMap(Loggear::class.java, Collections.singletonMap("contexto", CONTEXTO_PRUEBA) as Map<String, Any>?) as Loggear
    }

    @BeforeEach
    fun before()
    {
        aspectoEnPrueba = AspectoLogger()
        FabricaLogger.loggerActual = mockLoggerInterno
    }

    @Test
    fun si_no_lanza_excepcion_logea_informacion_necesaria_correctamente()
    {
        doReturn(mockSourceLocation)
            .`when`(mockProceedingJoinPoint)
            .getSourceLocation()

        val nombreArchivo = "archivo"
        doReturn(nombreArchivo)
            .`when`(mockSourceLocation)
            .getFileName()

        val linea = 1
        doReturn(linea)
            .`when`(mockSourceLocation)
            .getLine()


        val nombreTipo = "tipo"
        doReturn(nombreTipo)
            .`when`(mockSiganture)
            .getDeclaringTypeName()

        val nombreDelMetodo = "elMetodoInvocado"
        doReturn(nombreDelMetodo)
            .`when`(mockSiganture)
            .getName()

        val objetoMatch = "el objeto de match"
        doReturn(objetoMatch)
            .`when`(mockProceedingJoinPoint)
            .getTarget()

        doReturn(mockSiganture)
            .`when`(mockProceedingJoinPoint)
            .getSignature()

        val args = arrayOf<Any?>(1, "algo", null, 3.45)
        doReturn(args)
            .`when`(mockProceedingJoinPoint)
            .getArgs()

        val retornoDelMetodo = "El Retorno"
        doReturn(retornoDelMetodo)
            .`when`(mockProceedingJoinPoint)
            .proceed()


        val informacionDeLogsEsperada = InformacionLogAspecto(mockProceedingJoinPoint, CONTEXTO_PRUEBA)

        doNothing()
            .`when`(mockLoggerInterno)
            .marcarInicioDeMetodo(informacionDeLogsEsperada)

        doNothing()
            .`when`(mockLoggerInterno)
            .marcarRetornoDeMetodo(informacionDeLogsEsperada, retornoDelMetodo)


        aspectoEnPrueba.alrededorMetodo(mockProceedingJoinPoint, darAnotacion())

        verify(mockProceedingJoinPoint).proceed()

        verify(mockLoggerInterno)
            .marcarInicioDeMetodo(informacionDeLogsEsperada)

        verify(mockLoggerInterno)
            .marcarRetornoDeMetodo(informacionDeLogsEsperada, retornoDelMetodo)
    }

    @Test
    fun si_lanzo_excepcion_logea_informacion_necesaria_correctamente()
    {
        doReturn(mockSourceLocation)
            .`when`(mockProceedingJoinPoint)
            .getSourceLocation()

        val nombreArchivo = "archivo"
        doReturn(nombreArchivo)
            .`when`(mockSourceLocation)
            .getFileName()

        val linea = 1
        doReturn(linea)
            .`when`(mockSourceLocation)
            .getLine()


        val nombreTipo = "tipo"
        doReturn(nombreTipo)
            .`when`(mockSiganture)
            .getDeclaringTypeName()

        val nombreDelMetodo = "elMetodoInvocado"
        doReturn(nombreDelMetodo)
            .`when`(mockSiganture)
            .getName()

        val objetoMatch = "el objeto de match"
        doReturn(objetoMatch)
            .`when`(mockProceedingJoinPoint)
            .getTarget()

        doReturn(mockSiganture)
            .`when`(mockProceedingJoinPoint)
            .getSignature()

        val args = arrayOf<Any?>(1, "algo", null, 3.45)
        doReturn(args)
            .`when`(mockProceedingJoinPoint)
            .getArgs()

        val excepcion = Exception("la excepcion lanzada")
        doThrow(excepcion)
            .`when`(mockProceedingJoinPoint)
            .proceed()

        val informacionDeLogsEsperada = InformacionLogAspecto(mockProceedingJoinPoint, CONTEXTO_PRUEBA)

        doNothing()
            .`when`(mockLoggerInterno)
            .marcarInicioDeMetodo(informacionDeLogsEsperada)

        doNothing()
            .`when`(mockLoggerInterno)
            .marcarExcepcionEnMetodo(informacionDeLogsEsperada, excepcion)

        try
        {
            aspectoEnPrueba.alrededorMetodo(mockProceedingJoinPoint, darAnotacion())
        }
        catch (ignored: Throwable)
        {
            // Se ignora porque esto corresponde a cuando relanza la excepción
        }

        verify(mockProceedingJoinPoint).proceed()

        verify(mockLoggerInterno)
            .marcarInicioDeMetodo(informacionDeLogsEsperada)

        verify(mockLoggerInterno)
            .marcarExcepcionEnMetodo(informacionDeLogsEsperada, excepcion)
    }
}