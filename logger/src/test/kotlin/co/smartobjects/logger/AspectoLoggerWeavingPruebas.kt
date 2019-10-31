package co.smartobjects.logger

import co.smartobjects.logger.aspecto.Loggear
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


private class InfoLogPrueba(
        override val contexto: String,
        override val archivo: String,
        override val linea: Int,
        override val toStringDeInstancia: String,
        override val clase: String,
        override val metodo: String,
        override val argumentosMetodo: Array<Any?>)
    : InformacionLog()

private fun InfoLogPrueba.fijarLinea(lineaNueva: Int): InfoLogPrueba
{
    return InfoLogPrueba(
            contexto,
            archivo,
            lineaNueva,
            toStringDeInstancia,
            clase,
            metodo,
            argumentosMetodo
                        )
}

private class ExcepcionDePrueba(mensaje: String) : Exception(mensaje)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is Exception) return false
        return message == other.message
    }
}

private sealed class Llamado(val infoEsperada: InfoLogPrueba)
{
    class Inicio(infoEsperada: InfoLogPrueba) : Llamado(infoEsperada)
    {
        override fun toString(): String = "(Inicio)" + infoEsperada.toString()
    }

    class Retorno(infoEsperada: InfoLogPrueba, internal val resultadoEsperado: Any?) : Llamado(infoEsperada)
    {
        override fun toString(): String = "(Retorno)" + infoEsperada.toString()
    }

    class Excepcion(infoEsperada: InfoLogPrueba, internal val excepcionEsperada: Throwable) : Llamado(infoEsperada)
    {
        override fun toString(): String = "(Excepcion)" + infoEsperada.toString()
    }
}

private class LoggerPruebas : LoggerInterno
{
    private val llamadosEsperados: Queue<Llamado> = LinkedList<Llamado>()

    fun agregarLlamado(informacionLlamado: Llamado)
    {
        llamadosEsperados.add(informacionLlamado)
    }

    fun revisarParaLlamadosTerminados()
    {
        assertTrue(llamadosEsperados.isEmpty(), "Finalizó la prueba y quedan '${llamadosEsperados.size}' llamados por verificar de '${llamadosEsperados.peek()?.infoEsperada?.metodo}'")
    }

    private fun verificarAsercion(metodo: String, valor: Boolean, metodoInvocado: String)
    {
        if (!valor)
        {
            throw RuntimeException("{$metodo} No hay llamados suficientes para $metodoInvocado")
        }
    }

    private fun <T> verificarIgualdad(metodo: String, izq: T, der: T)
    {
        if (izq != der)
        {
            throw RuntimeException(
                    """{$metodo} La información de log no concuerda:
                        Esperado: $izq
                        vs.
                        Obtenido: $der
                    """.trimMargin())
        }
    }

    private fun <T> verificarTipoDeLlamado(metodo: String, valor: T?, tipoRecibido: String)
    {
        if (valor == null)
        {
            throw RuntimeException("Estando en {$metodo} recibió $tipoRecibido")
        }
    }

    override fun marcarInicioDeMetodo(informacionLog: InformacionLog)
    {
        verificarAsercion("marcarInicioDeMetodo", llamadosEsperados.isNotEmpty(), informacionLog.metodo)
        val esperado = llamadosEsperados.remove()
        val esperadoConCast = esperado as? Llamado.Inicio
        verificarTipoDeLlamado("marcarInicioDeMetodo", esperadoConCast, esperado.toString())
        verificarIgualdad("marcarInicioDeMetodo", esperadoConCast!!.infoEsperada.fijarLinea(informacionLog.linea), informacionLog)
    }

    override fun marcarRetornoDeMetodo(informacionLog: InformacionLog, retorno: Any?)
    {
        verificarAsercion("marcarRetornoDeMetodo", llamadosEsperados.isNotEmpty(), informacionLog.metodo)
        val esperado = llamadosEsperados.remove()
        val esperadoConCast = esperado as? Llamado.Retorno
        verificarTipoDeLlamado("marcarRetornoDeMetodo", esperadoConCast, esperado.toString())
        verificarIgualdad("marcarRetornoDeMetodo", esperadoConCast!!.infoEsperada.fijarLinea(informacionLog.linea), informacionLog)
        verificarIgualdad("marcarRetornoDeMetodo", esperadoConCast.resultadoEsperado, retorno)
    }

    override fun marcarExcepcionEnMetodo(informacionLog: InformacionLog, excepcion: Throwable)
    {
        verificarAsercion("marcarExcepcionEnMetodo", llamadosEsperados.isNotEmpty(), informacionLog.metodo)
        val esperado = llamadosEsperados.remove()
        val esperadoConCast = esperado as? Llamado.Excepcion
        verificarTipoDeLlamado("marcarExcepcionEnMetodo", esperadoConCast, esperado.toString())
        verificarIgualdad("marcarExcepcionEnMetodo", esperadoConCast!!.infoEsperada.fijarLinea(informacionLog.linea), informacionLog)
        verificarIgualdad("marcarExcepcionEnMetodo", esperadoConCast.excepcionEsperada, excepcion)
    }
}

@Loggear(contexto = "Contexto por defecto")
private fun funcionExterna()
{
}

class AspectoLoggerWeavingPruebas
{
    private lateinit var objetoDummy: ObjetoDummy
    private lateinit var loggerDePruebas: LoggerPruebas

    @BeforeEach
    fun before()
    {
        loggerDePruebas = LoggerPruebas()
        FabricaLogger.loggerActual = loggerDePruebas
        objetoDummy = ObjetoDummy()
    }

    @AfterEach
    fun despuesDeCadaPrueba()
    {
        loggerDePruebas.revisarParaLlamadosTerminados()
    }


    private fun prepararPruebaDeEstatico()
    {
        val infoDePruebas = InfoLogPrueba(
                ObjetoDummy.CONTEXTO_DE_PRUEBA,
                "AspectoLoggerWeavingPruebas.kt",
                0,
                ObjetoDummy.Companion.toString(),
                ObjetoDummy.Companion::class.java.name,
                ObjetoDummy.Companion::estatico.name,
                arrayOf()
                                         )

        loggerDePruebas.agregarLlamado(Llamado.Inicio(infoDePruebas))
        loggerDePruebas.agregarLlamado(Llamado.Retorno(infoDePruebas, null))
    }

    @Test
    fun weaving_correcto_con_estatico()
    {
        prepararPruebaDeEstatico()

        ObjetoDummy.estatico()
    }

    private fun prepararPruebaJvmStatic()
    {
        val infoDePruebas = InfoLogPrueba(
                ObjetoDummy.CONTEXTO_DE_PRUEBA,
                "AspectoLoggerWeavingPruebas.kt",
                0,
                ObjetoDummy.Companion.toString(),
                ObjetoDummy.Companion::class.java.name,
                ObjetoDummy.Companion::conAnotacionJvmStatic.name,
                arrayOf()
                                         )

        loggerDePruebas.agregarLlamado(Llamado.Inicio(infoDePruebas))
        loggerDePruebas.agregarLlamado(Llamado.Retorno(infoDePruebas, null))
    }

    @Test
    fun weaving_correcto_con_anotacion_jvm_static()
    {
        prepararPruebaJvmStatic()

        ObjetoDummy.conAnotacionJvmStatic()
    }

    private fun prepararPruebaParametroNullableNoNulo(retornoEsperado: String)
    {
        val infoDePruebas = InfoLogPrueba(
                ObjetoDummy.CONTEXTO_DE_PRUEBA,
                "AspectoLoggerWeavingPruebas.kt",
                0,
                ObjetoDummy().toString(),
                ObjetoDummy::class.java.name,
                ObjetoDummy::conParametroNullable.name,
                arrayOf(retornoEsperado)
                                         )

        loggerDePruebas.agregarLlamado(Llamado.Inicio(infoDePruebas))
        loggerDePruebas.agregarLlamado(Llamado.Retorno(infoDePruebas, retornoEsperado))
    }

    @Test
    fun weaving_correcto_con_parametro_nullable_no_nulo()
    {
        val retornoEsperado = "Algo"
        prepararPruebaParametroNullableNoNulo(retornoEsperado)

        objetoDummy.conParametroNullable(retornoEsperado)
    }

    private fun prepararPruebaParametroNullableNulo()
    {
        val retornoEsperado = null
        val infoDePruebas = InfoLogPrueba(
                ObjetoDummy.CONTEXTO_DE_PRUEBA,
                "AspectoLoggerWeavingPruebas.kt",
                0,
                ObjetoDummy().toString(),
                ObjetoDummy::class.java.name,
                ObjetoDummy::conParametroNullable.name,
                arrayOf(retornoEsperado)
                                         )

        loggerDePruebas.agregarLlamado(Llamado.Inicio(infoDePruebas))
        loggerDePruebas.agregarLlamado(Llamado.Retorno(infoDePruebas, "$retornoEsperado"))
    }

    @Test
    fun weaving_correcto_con_parametro_nullable_nulo()
    {
        prepararPruebaParametroNullableNulo()

        objetoDummy.conParametroNullable(null)
    }

    private fun prepararPruebaMetodoParametroNormal(retornoEsperado: String)
    {
        val infoDePruebas = InfoLogPrueba(
                ObjetoDummy.CONTEXTO_DE_PRUEBA,
                "AspectoLoggerWeavingPruebas.kt",
                0,
                ObjetoDummy().toString(),
                ObjetoDummy::class.java.name,
                ObjetoDummy::conParametroNormal.name,
                arrayOf(retornoEsperado)
                                         )

        loggerDePruebas.agregarLlamado(Llamado.Inicio(infoDePruebas))
        loggerDePruebas.agregarLlamado(Llamado.Retorno(infoDePruebas, retornoEsperado))
    }

    @Test
    fun weaving_correcto_con_parametro_no_nullable()
    {
        val retornoEsperado = "Algo"

        prepararPruebaMetodoParametroNormal(retornoEsperado)

        objetoDummy.conParametroNormal(retornoEsperado)
    }

    private fun prepararPruebaMetodoQuePuedeLanzarExcepcionPeroNoLanzandoExcepcion(retornoEsperado: String, excepcionALanzar: ExcepcionDePrueba)
    {
        val infoDePruebas = InfoLogPrueba(
                ObjetoDummy.CONTEXTO_DE_PRUEBA,
                "AspectoLoggerWeavingPruebas.kt",
                0,
                ObjetoDummy().toString(),
                ObjetoDummy::class.java.name,
                ObjetoDummy::conExcepcion.name,
                arrayOf(retornoEsperado, excepcionALanzar)
                                         )

        loggerDePruebas.agregarLlamado(Llamado.Inicio(infoDePruebas))
        loggerDePruebas.agregarLlamado(Llamado.Retorno(infoDePruebas, retornoEsperado))
    }

    @Test
    fun weaving_correcto_con_metodo_que_puede_lanzar_excepcion_pero_no_lanzando_excepcion()
    {
        val excepcionALanzar = ExcepcionDePrueba("Una prueba")
        val retornoEsperado = "Algo"

        prepararPruebaMetodoQuePuedeLanzarExcepcionPeroNoLanzandoExcepcion(retornoEsperado, excepcionALanzar)

        objetoDummy.conExcepcion(retornoEsperado, excepcionALanzar)
    }

    private fun prepararPruebaMetodoQuePuedeLanzarExcepcionLanzandoExcepcion(excepcionALanzar: ExcepcionDePrueba)
    {
        val infoDePruebas = InfoLogPrueba(
                ObjetoDummy.CONTEXTO_DE_PRUEBA,
                "AspectoLoggerWeavingPruebas.kt",
                0,
                ObjetoDummy().toString(),
                ObjetoDummy::class.java.name,
                ObjetoDummy::conExcepcion.name,
                arrayOf("", excepcionALanzar)
                                         )

        loggerDePruebas.agregarLlamado(Llamado.Inicio(infoDePruebas))
        loggerDePruebas.agregarLlamado(Llamado.Excepcion(infoDePruebas, excepcionALanzar))
    }

    @Test
    fun weaving_correcto_con_metodo_que_puede_lanzar_excepcion_lanzando_excepcion()
    {
        val excepcionALanzar = ExcepcionDePrueba("Una prueba")

        prepararPruebaMetodoQuePuedeLanzarExcepcionLanzandoExcepcion(excepcionALanzar)

        val excepcionLanzada = Assertions.assertThrows(Throwable::class.java, { objetoDummy.conExcepcion("", excepcionALanzar) })
        assertEquals(excepcionALanzar, excepcionLanzada)
    }

    @Test
    fun weaving_correcto_con_metodo_que_invoca_todos()
    {
        val retornoEsperado = "Algo"
        val excepcionALanzar = ExcepcionDePrueba("Una prueba")

        val infoDePruebas = InfoLogPrueba(
                ObjetoDummy.CONTEXTO_DE_PRUEBA,
                "AspectoLoggerWeavingPruebas.kt",
                0,
                ObjetoDummy().toString(),
                ObjetoDummy::class.java.name,
                ObjetoDummy::invocaTodo.name,
                arrayOf(retornoEsperado, retornoEsperado, excepcionALanzar)
                                         )

        loggerDePruebas.agregarLlamado(Llamado.Inicio(infoDePruebas))
        prepararPruebaMetodoParametroNormal(retornoEsperado)
        prepararPruebaDeEstatico()
        prepararPruebaJvmStatic()
        prepararPruebaParametroNullableNoNulo(retornoEsperado)
        prepararPruebaParametroNullableNulo()
        prepararPruebaMetodoQuePuedeLanzarExcepcionPeroNoLanzandoExcepcion(retornoEsperado, excepcionALanzar)
        prepararPruebaMetodoQuePuedeLanzarExcepcionLanzandoExcepcion(excepcionALanzar)
        loggerDePruebas.agregarLlamado(Llamado.Retorno(infoDePruebas, null))

        objetoDummy.invocaTodo(retornoEsperado, retornoEsperado, excepcionALanzar)
    }

    @Test
    fun weaving_correcto_con_funcion_externa()
    {
        val infoDePruebas = InfoLogPrueba(
                "Contexto por defecto",
                "AspectoLoggerWeavingPruebas.kt",
                0,
                "Función a nivel de archivo",
                "${AspectoLoggerWeavingPruebas::class.java.`package`.name}.${AspectoLoggerWeavingPruebas::class.java.simpleName}Kt",
                "funcionExterna",
                arrayOf()
                                         )

        loggerDePruebas.agregarLlamado(Llamado.Inicio(infoDePruebas))
        loggerDePruebas.agregarLlamado(Llamado.Retorno(infoDePruebas, null))

        funcionExterna()
    }

    @Nested
    inner class PruebasMetodosAnidados
    {
        private fun darInfoLogLlamadoVoidSinArgumentos(nombreMetodo: String): InfoLogPrueba
        {
            return InfoLogPrueba(
                    ObjetoDummy.CONTEXTO_DE_PRUEBA,
                    "AspectoLoggerWeavingPruebas.kt",
                    0,
                    ObjetoDummy().toString(),
                    ObjetoDummy::class.java.name,
                    nombreMetodo,
                    arrayOf()
                                )
        }

        private inline fun crearLlamadoNormalVoid(metodo: String, llamadoAnidado: () -> Unit)
        {
            val info = darInfoLogLlamadoVoidSinArgumentos(metodo)
            loggerDePruebas.agregarLlamado(Llamado.Inicio(info))
            llamadoAnidado()
            loggerDePruebas.agregarLlamado(Llamado.Retorno(info, null))
        }

        @Test
        fun weaving_correcto_con_metodo_que_invoca_otros_metodos()
        {
            crearLlamadoNormalVoid(ObjetoDummy::abuelo.name)
            {
                crearLlamadoNormalVoid(ObjetoDummy::padre.name)
                {
                    crearLlamadoNormalVoid(ObjetoDummy::hijo.name)
                    {

                    }
                }
            }

            objetoDummy.abuelo()
        }
    }

    private class ObjetoDummy
    {
        companion object
        {
            const val CONTEXTO_DE_PRUEBA = "prueba"

            @Loggear(contexto = CONTEXTO_DE_PRUEBA)
            fun estatico()
            {
            }

            @Loggear(contexto = CONTEXTO_DE_PRUEBA)
            @JvmStatic
            fun conAnotacionJvmStatic()
            {
            }

            override fun toString(): String
            {
                return "toString() de ${ObjetoDummy.Companion::class.java.name}"
            }
        }

        @Loggear(contexto = CONTEXTO_DE_PRUEBA)
        fun conParametroNormal(arg: Any): String
        {
            return "$arg"
        }

        @Loggear(contexto = CONTEXTO_DE_PRUEBA)
        fun conParametroNullable(arg: Any?): String
        {
            return "$arg"
        }

        @Loggear(contexto = CONTEXTO_DE_PRUEBA)
        fun conExcepcion(retorno: String, excepcionALanzar: ExcepcionDePrueba): String
        {
            if (retorno.isEmpty())
            {
                throw excepcionALanzar
            }
            return retorno
        }

        @Loggear(contexto = CONTEXTO_DE_PRUEBA)
        fun invocaTodo(retorno: String, parametroNullable: Any?, excepcionALanzar: ExcepcionDePrueba)
        {
            conParametroNormal(retorno)
            estatico()
            conAnotacionJvmStatic()
            conParametroNullable(parametroNullable)
            conParametroNullable(null)
            conExcepcion(retorno, excepcionALanzar)
            try
            {
                conExcepcion("", excepcionALanzar)
            }
            catch (e: Throwable)
            {
            }
        }

        @Loggear(contexto = CONTEXTO_DE_PRUEBA)
        fun abuelo()
        {
            padre()
        }

        @Loggear(contexto = CONTEXTO_DE_PRUEBA)
        fun padre()
        {
            hijo()
        }

        @Loggear(contexto = CONTEXTO_DE_PRUEBA)
        fun hijo()
        {
        }

        override fun toString(): String
        {
            return "toString()"
        }
    }
}