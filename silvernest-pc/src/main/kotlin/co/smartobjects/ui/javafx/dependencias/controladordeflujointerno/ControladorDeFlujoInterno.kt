package co.smartobjects.ui.javafx.dependencias.controladordeflujointerno

import io.datafx.controller.context.ApplicationContext
import io.datafx.controller.flow.FlowHandler
import io.datafx.controller.flow.ViewHistoryDefinition
import io.datafx.controller.flow.context.FlowActionHandler
import io.datafx.controller.flow.context.ViewFlowContext
import javafx.collections.ObservableList
import kotlin.reflect.KClass


class ControladorDeFlujoInterno(private val flowHandler: FlowHandler) : FlowActionHandler(flowHandler)
{
    val contextoFlujo: ViewFlowContext = flowHandler.flowContext
    val contextoAplicacion: ApplicationContext = contextoFlujo.applicationContext
    private val historialDePantallas =
            flowHandler
                .javaClass.declaredFields.first { it.name == "controllerHistory" }
                .apply { isAccessible = true }
                .get(flowHandler) as ObservableList<ViewHistoryDefinition<*>>

    fun navegarHaciaAtrasAPantalla(claseDelControlador: KClass<*>)
    {
        // Las pantallas se ordenan como un stack
        val indicePantallaANavegar = historialDePantallas.indexOfFirst { it.controllerClass == claseDelControlador.java }

        if (indicePantallaANavegar == -1)
        {
            throw IllegalStateException("No se encontró la pantalla ${claseDelControlador.java.name} en el historial de pantallas")
        }
        else
        {
            // El índice final es excluyente
            historialDePantallas.remove(0, indicePantallaANavegar)
            flowHandler.navigateBack()
        }
    }
}