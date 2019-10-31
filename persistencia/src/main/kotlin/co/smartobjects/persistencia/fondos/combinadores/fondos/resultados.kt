@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos

import co.smartobjects.persistencia.EntidadRelacionUnoAUno
import co.smartobjects.persistencia.TransformadorEntidadCliente

internal inline fun <T, S, U> shiftEntidadRelacionUnoAUno(): TransformadorEntidadCliente<EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, U>>, EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>>
{
    return object : TransformadorEntidadCliente<EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, U>>, EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>>
    {
        override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAUno<T, EntidadRelacionUnoAUno<S, U>>): EntidadRelacionUnoAUno<EntidadRelacionUnoAUno<T, S>, U>
        {
            return EntidadRelacionUnoAUno(EntidadRelacionUnoAUno(origen.entidadOrigen, origen.entidadDestino.entidadOrigen), origen.entidadDestino.entidadDestino)
        }
    }
}