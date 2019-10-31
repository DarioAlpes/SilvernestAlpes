package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.Transformador
import co.smartobjects.utilidades.Opcional


internal fun <ElementoHijo, ElementoPadre, Llave> Sequence<ElementoHijo>.agruparSecuenciaOrdenadaSegunTransformacion(
        transformadorLlave: Transformador<ElementoHijo, ElementoPadre>,
        transformadorComparacion: Transformador<ElementoPadre, Llave>)
        : Sequence<Pair<ElementoPadre, List<ElementoHijo>>>
{
    return SecuenciaAgrupada(this, transformadorLlave, transformadorComparacion)
}


private class SecuenciaAgrupada<out ElementoHijo, out ElementoPadre, Llave>(
        sequenciaOriginalOrdenada: Sequence<ElementoHijo>,
        private val transformadorLlave: Transformador<ElementoHijo, ElementoPadre>,
        private val transformadorComparacion: Transformador<ElementoPadre, Llave>)
    : Sequence<Pair<ElementoPadre, List<ElementoHijo>>>
{
    private val iteradorPeek = IteradorPeek(sequenciaOriginalOrdenada.iterator())

    override fun iterator(): Iterator<Pair<ElementoPadre, List<ElementoHijo>>>
    {
        return object : Iterator<Pair<ElementoPadre, List<ElementoHijo>>>
        {
            override fun hasNext(): Boolean
            {
                return iteradorPeek.hasNext()
            }

            override fun next(): Pair<ElementoPadre, List<ElementoHijo>>
            {
                val itemAgrupador: Opcional<ElementoHijo> = iteradorPeek.next()
                val secuencia = object : Sequence<ElementoHijo>
                {
                    override fun iterator(): Iterator<ElementoHijo>
                    {
                        var itemAnteriorDelGrupo: Opcional<ElementoHijo> = itemAgrupador
                        var esMismaLlave = true

                        return object : Iterator<ElementoHijo>
                        {
                            override fun hasNext(): Boolean
                            {
                                if (!esMismaLlave)
                                {
                                    return false
                                }

                                val itemQueSigue = iteradorPeek.peek()
                                esMismaLlave =
                                        if (itemQueSigue.esVacio)
                                        {
                                            false
                                        }
                                        else
                                        {

                                            (itemAnteriorDelGrupo.valor == null && itemQueSigue.valor == null)
                                            || (
                                                    (itemAnteriorDelGrupo.valor != null && itemQueSigue.valor != null)
                                                    &&
                                                    transformadorComparacion.transformar(transformadorLlave.transformar(itemAnteriorDelGrupo.valor)) ==
                                                    transformadorComparacion.transformar(transformadorLlave.transformar(itemQueSigue.valor))
                                               )

                                        }

                                var retorno = esMismaLlave || !itemQueSigue.esVacio

                                if (!retorno)
                                {
                                    esMismaLlave = false
                                    retorno = true
                                }

                                return retorno
                            }

                            override fun next(): ElementoHijo
                            {
                                val anterior = itemAnteriorDelGrupo.valor
                                if (esMismaLlave)
                                {
                                    itemAnteriorDelGrupo = iteradorPeek.next()
                                }
                                return anterior
                            }
                        }
                    }
                }
                return transformadorLlave.transformar(itemAgrupador.valor) to secuencia.toList()
            }
        }
    }
}