package co.smartobjects.ui.modelos

import io.reactivex.exceptions.CompositeException
import io.reactivex.plugins.RxJavaPlugins
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.fail

internal open class PruebasModelosRxBase
{
    // RxJava por defecto ignora los errores no fatales (ver https://github.com/ReactiveX/RxJava/issues/5234)
    // => Se debe validar que no se lancen excepciones no esperadas después de cada prueba con estos métodos
    private val errores = mutableListOf<Throwable>()

    val erroresEsperados = mutableListOf<Throwable>()

    @BeforeEach
    fun registrarErrores()
    {
        RxJavaPlugins.setErrorHandler { errores.add(it.cause!!) }
    }

    @AfterEach
    fun revisarErrores()
    {
        val iterador = errores.iterator()
        while (iterador.hasNext())
        {
            val error = iterador.next()
            if (error is CompositeException)
            {
                for (errorEsperado in erroresEsperados)
                {
                    if (errorEsperado is CompositeException && error.exceptions.size == errorEsperado.exceptions.size)
                    {
                        var algunaDiferente = false

                        for (i in 0 until error.exceptions.size)
                        {
                            val excepcionEnErrorObtenido = error.exceptions[i]
                            val excepcionEnErrorEsperado = errorEsperado.exceptions[i]

                            if (excepcionEnErrorObtenido.message != excepcionEnErrorEsperado.message)
                            {
                                algunaDiferente = true
                                break
                            }
                        }

                        if (!algunaDiferente)
                        {
                            iterador.remove()
                            break
                        }
                    }
                }
            }
        }

        errores.removeAll(erroresEsperados)

        val excepcionesComoErroresEsperados = errores.asSequence().map { ErrorEsperado(it) }.toMutableSet()

        excepcionesComoErroresEsperados.removeAll(erroresEsperados)

        if (excepcionesComoErroresEsperados.isNotEmpty())
        {
            excepcionesComoErroresEsperados.forEach {
                it.printStackTrace()
                println()
                println()
            }
            fail("Una o más aserciones fallaron")
        }
    }

    class ErrorEsperado(val excepcion: Throwable) : Throwable(excepcion)
    {
        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ErrorEsperado

            if (excepcion::class != other.excepcion::class) return false

            return true
        }

        override fun hashCode(): Int
        {
            return excepcion::class.qualifiedName.hashCode()
        }
    }
}