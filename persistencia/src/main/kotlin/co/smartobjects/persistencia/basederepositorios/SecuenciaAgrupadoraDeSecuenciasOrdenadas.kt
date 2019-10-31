package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.ComparadorEntidades

internal data class ElementoDeAgrupacion<out Elemento, out ValorComparador>
(
        val elemento: Elemento,
        private val _darValorComparacion: (Elemento) -> ValorComparador
)
{
    fun darValorComparacion() = _darValorComparacion(elemento)
}

internal typealias AgrupacionDeSecuencias<ElementoIzquierdo, ValorIzquierdo, ElementoDerecho, ValorDerecho> =
Pair<
        ElementoDeAgrupacion<ElementoIzquierdo, ValorIzquierdo>?,
        ElementoDeAgrupacion<ElementoDerecho, ValorDerecho>?
        >

internal class SecuenciaAgrupadoraDeSecuenciasOrdenadas<out ElementoIzquierdo, out ValorIzquierdo, out ElementoDerecho, out ValorDerecho>(
        sequenciaIzquierdaOrdenada: Sequence<ElementoDeAgrupacion<ElementoIzquierdo, ValorIzquierdo>>,
        sequenciaDerechaOrdenada: Sequence<ElementoDeAgrupacion<ElementoDerecho, ValorDerecho>>,
        private val comparadorEntidadesOrigen: ComparadorEntidades<ValorIzquierdo, ValorDerecho>)
    : Sequence<AgrupacionDeSecuencias<ElementoIzquierdo, ValorIzquierdo, ElementoDerecho, ValorDerecho>>
{
    private val iteradorIzquierda = sequenciaIzquierdaOrdenada.iterator()
    private val iteradorDerecha = sequenciaDerechaOrdenada.iterator()

    override fun iterator(): Iterator<AgrupacionDeSecuencias<ElementoIzquierdo, ValorIzquierdo, ElementoDerecho, ValorDerecho>>
    {
        return object : Iterator<AgrupacionDeSecuencias<ElementoIzquierdo, ValorIzquierdo, ElementoDerecho, ValorDerecho>>
        {
            private var ultimoElementoIzquierda: Lazy<ElementoDeAgrupacion<ElementoIzquierdo, ValorIzquierdo>?> = extraerSiguienteLazy(iteradorIzquierda)
            private var ultimoElementoDerecha: Lazy<ElementoDeAgrupacion<ElementoDerecho, ValorDerecho>?> = extraerSiguienteLazy(iteradorDerecha)

            private fun <T> extraerSiguienteLazy(iterador: Iterator<T>): Lazy<T?>
            {
                return object : Lazy<T?>
                {
                    private var valor: T? = null
                    private var tieneValor = false
                    override val value: T?
                        get()
                        {
                            if (!tieneValor)
                            {
                                valor = if (iterador.hasNext()) iterador.next() else null
                                tieneValor = true
                            }
                            return valor
                        }

                    override fun isInitialized(): Boolean
                    {
                        return tieneValor
                    }
                }
            }

            override fun hasNext(): Boolean
            {
                return ultimoElementoIzquierda.value != null || ultimoElementoDerecha.value != null
            }

            override fun next(): AgrupacionDeSecuencias<ElementoIzquierdo, ValorIzquierdo, ElementoDerecho, ValorDerecho>
            {
                val derecha = ultimoElementoDerecha.value
                val izquierda = ultimoElementoIzquierda.value
                return when
                {
                    derecha != null && izquierda != null ->
                    {
                        val comparacion = comparadorEntidadesOrigen.comparar(izquierda.darValorComparacion(), derecha.darValorComparacion())
                        when
                        {
                            comparacion == 0 ->
                            {
                                darResultadoAvanzandoAmbos()
                            }
                            comparacion > 0  ->
                            {
                                darResultadoAvanzandoDerecha()
                            }
                            else             ->
                            {
                                darResultadoAvanzandoIzquierda()
                            }
                        }
                    }
                    izquierda != null                    ->
                    {
                        darResultadoAvanzandoIzquierda()
                    }
                    else                                 ->
                    {
                        darResultadoAvanzandoDerecha()
                    }
                }
            }

            private fun darResultadoAvanzandoAmbos(): AgrupacionDeSecuencias<ElementoIzquierdo, ValorIzquierdo, ElementoDerecho, ValorDerecho>
            {
                val izquierda = ultimoElementoIzquierda.value!!
                val derecha = ultimoElementoDerecha.value!!

                ultimoElementoIzquierda = extraerSiguienteLazy(iteradorIzquierda)
                ultimoElementoDerecha = extraerSiguienteLazy(iteradorDerecha)

                return Pair(izquierda, derecha)
            }

            private fun darResultadoAvanzandoDerecha(): AgrupacionDeSecuencias<ElementoIzquierdo, ValorIzquierdo, ElementoDerecho, ValorDerecho>
            {
                val derecha = ultimoElementoDerecha.value!!

                ultimoElementoDerecha = extraerSiguienteLazy(iteradorDerecha)

                return Pair(null, derecha)
            }

            private fun darResultadoAvanzandoIzquierda(): AgrupacionDeSecuencias<ElementoIzquierdo, ValorIzquierdo, ElementoDerecho, ValorDerecho>
            {
                val izquierda = ultimoElementoIzquierda.value!!

                ultimoElementoIzquierda = extraerSiguienteLazy(iteradorIzquierda)

                return Pair(izquierda, null)
            }
        }
    }
}