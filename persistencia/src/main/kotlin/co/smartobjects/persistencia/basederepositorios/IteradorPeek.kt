package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.utilidades.Opcional


internal class IteradorPeek<T>(private val iteradorSequenciaOriginalOrdenada: Iterator<T>) : Iterator<Opcional<T>>
{
    private var hasPeeked: Boolean = false
    private var peekedElement: Opcional<T> = Opcional.Vacio()

    override fun hasNext(): Boolean
    {
        return hasPeeked || iteradorSequenciaOriginalOrdenada.hasNext()
    }

    fun peek(): Opcional<T>
    {
        if (!hasNext())
        {
            return Opcional.Vacio()
        }

        if (!hasPeeked)
        {
            peekedElement = Opcional.De(iteradorSequenciaOriginalOrdenada.next())
            hasPeeked = true
        }

        return peekedElement
    }

    override fun next(): Opcional<T>
    {
        if (!hasPeeked)
        {
            return Opcional.De(iteradorSequenciaOriginalOrdenada.next())
        }

        val result = peekedElement
        hasPeeked = false
        peekedElement = Opcional.Vacio()

        return result
    }
}