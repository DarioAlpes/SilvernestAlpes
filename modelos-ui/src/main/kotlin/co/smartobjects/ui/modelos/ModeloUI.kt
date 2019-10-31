package co.smartobjects.ui.modelos

import io.reactivex.Observer

interface ModeloUI
{
    val observadoresInternos: List<Observer<*>>
    val modelosHijos: List<ModeloUI>
    fun finalizarProceso()
    {
        modelosHijos.forEach {
            it.finalizarProceso()
        }
        observadoresInternos.forEach {
            it.onComplete()
        }
    }
}