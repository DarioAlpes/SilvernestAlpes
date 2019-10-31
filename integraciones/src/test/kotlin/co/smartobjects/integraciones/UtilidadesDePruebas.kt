package co.smartobjects.integraciones

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock

internal fun <T> mockConDefaultAnswer(clase: Class<T>): T
{
    return mock(clase) {
        val argumentos =
                it.arguments.joinToString(",\n\t\t").let {
                    if (it.isEmpty())
                    {
                        ""
                    }
                    else
                    {
                        "\n\t\t$it\n\t"
                    }
                }

        throw RuntimeException("Llamado a\n\t${it.method.declaringClass.simpleName}.${it.method.name}($argumentos)\n\tno esta mockeado\n[$it]")
    }
}


fun <T : Any> eqParaKotlin(value: T): T = ArgumentMatchers.eq(value) ?: value

fun <T> cualquiera(): T
{
    ArgumentMatchers.any<T>()
    @Suppress("UNCHECKED_CAST")
    return null as T
}