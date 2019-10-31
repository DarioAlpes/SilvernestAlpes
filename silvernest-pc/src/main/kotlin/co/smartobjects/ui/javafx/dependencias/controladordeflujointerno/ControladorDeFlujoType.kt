package co.smartobjects.ui.javafx.dependencias.controladordeflujointerno

import io.datafx.controller.context.ViewContext
import io.datafx.controller.context.resource.AnnotatedControllerResourceType
import io.datafx.controller.flow.context.ViewFlowContext

class ControladorDeFlujoType : AnnotatedControllerResourceType<ControladorDeFlujo, ControladorDeFlujoInterno>
{
    override fun getResource(annotation: ControladorDeFlujo, cls: Class<ControladorDeFlujoInterno>, context: ViewContext<*>): ControladorDeFlujoInterno
    {
        return context.getRegisteredObject(ViewFlowContext::class.java).getRegisteredObject(ControladorDeFlujoInterno::class.java)
    }

    override fun getSupportedAnnotation(): Class<ControladorDeFlujo>
    {
        return ControladorDeFlujo::class.java
    }
}
