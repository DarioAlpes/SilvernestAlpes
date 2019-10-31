package co.smartobjects.sincronizadordecontenido

import io.reactivex.plugins.RxJavaPlugins
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.fail

internal open class PruebasConRxJava
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
        errores.removeAll(erroresEsperados)

        if (errores.isNotEmpty())
        {
            errores.forEach {
                it.printStackTrace()
                println()
                println()
            }
            fail("Una o más aserciones fallaron")
        }
    }
}