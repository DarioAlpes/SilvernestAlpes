package co.smartobjects.ui.modelos

import io.reactivex.Scheduler
import io.reactivex.functions.Predicate
import io.reactivex.observers.BaseTestConsumer
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import java.util.concurrent.Executor

private object ejecutorThreadActual : Executor
{
    override fun execute(command: Runnable?)
    {
        command?.run()
    }
}

val schedulerThreadActual: Scheduler = Schedulers.from(ejecutorThreadActual)

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

internal fun <T> TestObserver<T>.ultimoEmitido(): T
{
    return values().last()
}

internal fun <T, U : BaseTestConsumer<T, U>> BaseTestConsumer<T, U>.verificarUltimoValorEmitido(valorEsperado: T): U
{
    return assertSubscribed().assertNoErrors().assertNotComplete().assertValueAt(valueCount() - 1, valorEsperado)
}

internal fun <T, U : BaseTestConsumer<T, U>> BaseTestConsumer<T, U>.verificarUltimoValorEmitido(predicado: Predicate<T>): U
{
    return assertSubscribed().assertNoErrors().assertNotComplete().assertValueAt(valueCount() - 1, predicado)
}

fun <T : Any> eqParaKotlin(value: T): T = ArgumentMatchers.eq(value) ?: value

fun <T> cualquiera(): T
{
    ArgumentMatchers.any<T>()
    @Suppress("UNCHECKED_CAST")
    return null as T
}

fun <T> argCumpleQue(matcher: ArgumentMatcher<T>): T
{
    ArgumentMatchers.argThat(matcher)
    @Suppress("UNCHECKED_CAST")
    return null as T
}