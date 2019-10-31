@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.ui.javafx

import javafx.scene.Node
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.PickResult
import org.mockito.Mockito.mock
import kotlin.test.assertFalse
import kotlin.test.assertTrue


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

internal inline fun Node.hacerClick()
{
    val eventoClick = MouseEvent(
            MouseEvent.MOUSE_CLICKED,
            0.0,
            0.0,
            0.0,
            0.0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, true,
            PickResult(this, 0.0, 0.0)
                                )

    MouseEvent.fireEvent(this, eventoClick)
}

internal fun Node.verificarEstilos(estiloQueDebeEstar: String, estiloQueNoDebeEstar: String)
{
    assertTrue(styleClass.contains(estiloQueDebeEstar))
    assertFalse(styleClass.contains(estiloQueNoDebeEstar))
}