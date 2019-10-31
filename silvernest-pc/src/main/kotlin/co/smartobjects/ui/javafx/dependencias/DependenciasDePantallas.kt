package co.smartobjects.ui.javafx.dependencias

import io.datafx.controller.flow.context.ViewFlowContext
import sun.plugin.dom.exception.InvalidStateException
import java.util.*


internal fun ViewFlowContext.darDependenciasDePantallas(): DependenciasDePantallas
{
    val dependencias = getRegisteredObject(DependenciasDePantallas::class.java)
                       ?: throw InvalidStateException("No se han registrado dependencias para pantallas")

    return dependencias
}

internal fun ViewFlowContext.agregarDependenciaDePantallaUnSoloUso(valor: Any)
{
    return darDependenciasDePantallas().agregarUnSoloUso(valor)
}

internal fun ViewFlowContext.agregarDependenciaDePantallaMultiplesUsos(valor: Any)
{
    return darDependenciasDePantallas().agregarMultiplesUsos(valor)
}

internal fun <T> ViewFlowContext.obtenerDependenciaDePantalla(clase: Class<T>): T
{
    return darDependenciasDePantallas().obtener(clase)!!
}

internal fun <T> ViewFlowContext.obtenerCondicionalDependenciaDePantalla(clase: Class<T>): T?
{
    return darDependenciasDePantallas().obtener(clase)
}

internal fun <T> ViewFlowContext.eliminarDependenciaDePantalla(clase: Class<T>)
{
    return darDependenciasDePantallas().eliminar(clase)
}

internal fun ViewFlowContext.eliminarTodasLasDependenciasDePantalla()
{
    return darDependenciasDePantallas().eliminarTodas()
}

internal class DependenciasDePantallas
{
    private val id = UUID.randomUUID().toString()

    private val dependenciasVsUnSoloUso = mutableMapOf<String, Boolean>()
    private val dependencias = mutableMapOf<String, Any>()


    private fun agregar(llave: String, valor: Any, unSoloUso: Boolean)
    {
        dependencias[llave] = valor
        dependenciasVsUnSoloUso[llave] = unSoloUso
    }

    fun agregarMultiplesUsos(llave: String, valor: Any)
    {
        agregar(llave, valor, false)
    }

    fun agregarMultiplesUsos(valor: Any)
    {
        agregarMultiplesUsos(valor.javaClass.toString(), valor)
    }

    fun agregarUnSoloUso(llave: String, valor: Any)
    {
        agregar(llave, valor, true)
    }

    fun agregarUnSoloUso(valor: Any)
    {
        agregarUnSoloUso(valor.javaClass.toString(), valor)
    }


    fun <T> obtener(clase: Class<T>): T?
    {
        return obtener(clase.toString()) as T?
    }

    fun obtener(llave: String): Any?
    {
        val dependencia = dependencias[llave]

        if (dependenciasVsUnSoloUso[llave] == true)
        {
            eliminar(llave)
        }

        return dependencia
    }


    fun eliminar(llave: String)
    {
        dependencias.remove(llave)
        dependenciasVsUnSoloUso.remove(llave)
    }

    fun <T> eliminar(cls: Class<T>)
    {
        eliminar(cls.toString())
    }

    fun eliminarTodas()
    {
        dependencias.clear()
        dependenciasVsUnSoloUso.clear()
    }

    override fun toString(): String
    {
        val sb = StringBuffer("DependenciasDePantallas {")
        sb.append("id = '").append(id).append('\'')
        sb.append(", dependencias = ").append(dependencias)
        sb.append('}')
        return sb.toString()
    }

    override fun hashCode(): Int = id.hashCode()
}