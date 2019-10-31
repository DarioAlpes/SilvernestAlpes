package co.smartobjects.logger

import co.smartobjects.logger.aspecto.Loggear

private class Prueba
{
    companion object
    {
        @Loggear(contexto = "Algo")
        fun enCompanion()
        {
            println("En companion")
        }

        @Loggear
        @JvmStatic
        fun estatico()
        {
            println("Estatico")
        }
    }

    @Loggear(contexto = "Algo")
    fun sinNullable(asd: Any)
    {
        println("Sin nullable")
    }

    @Loggear
    fun conNullable(asd: Any?): String
    {
        println("Con nullable")
        return asd.toString()
    }

    @Loggear
    fun conExcepcion(lanzar: Boolean)
    {
        println("Con excepcion?: $lanzar")
        if (lanzar)
        {
            throw Exception("Excepcion prueba")
        }
    }

    @Loggear(contexto = "Algo")
    fun padre(asd: Any)
    {
        @Loggear(contexto = "Algo")
        fun hijo(asd: Any)
        {
            println("hijo")
        }
        println("padre")
        hijo(asd)
    }

    override fun toString(): String
    {
        return "toString() de ${Prueba::class.java.simpleName}"
    }
}

object MyApp
{
    @JvmStatic
    fun main(args: Array<String>)
    {
        FabricaLogger.loggerActual = object : LoggerInterno
        {
            override fun marcarInicioDeMetodo(informacionLog: InformacionLog)
            {
                println("ENTRA [${informacionLog.contexto}] ${informacionLog.archivo}:${informacionLog.linea} -> ${informacionLog.clase}(${informacionLog.toStringDeInstancia}).${informacionLog.metodo}(${informacionLog.argumentosMetodo.joinToString(separator = ", ")})")
            }

            override fun marcarRetornoDeMetodo(informacionLog: InformacionLog, retorno: Any?)
            {
                println("RETORNA [${informacionLog.contexto}] ${informacionLog.archivo}:${informacionLog.linea} -> ${informacionLog.clase}(${informacionLog.toStringDeInstancia}).${informacionLog.metodo}(${informacionLog.argumentosMetodo.joinToString(separator = ", ")}) -> $retorno")
            }

            override fun marcarExcepcionEnMetodo(informacionLog: InformacionLog, excepcion: Throwable)
            {
                System.err.println("EXCEPCION [${informacionLog.contexto}] ${informacionLog.archivo}:${informacionLog.linea} -> ${informacionLog.clase}(${informacionLog.toStringDeInstancia}).${informacionLog.metodo}(${informacionLog.argumentosMetodo.joinToString(separator = ", ")}) -> \n$excepcion")
            }
        }

        val obj = Prueba()
        obj.padre(0)
    }
}