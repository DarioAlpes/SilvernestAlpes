package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.Transformador
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class SecuenciaAgrupadaPruebas
{
    private data class ObjetoDePrueba(val id: Int, val campo: String)

    private class TransformadorDeLlave : Transformador<ObjetoDePrueba, Int>
    {
        override fun transformar(origen: ObjetoDePrueba): Int
        {
            return origen.id
        }
    }

    private class TransformadorDeComparacion : Transformador<Int, Int>
    {
        override fun transformar(origen: Int): Int
        {
            return origen
        }
    }

    private class TransformadorDeLlaveNull : Transformador<ObjetoDePrueba?, Int?>
    {
        override fun transformar(origen: ObjetoDePrueba?): Int?
        {
            return origen?.id
        }
    }

    private class TransformadorDeLlaveEnPareja<in T : Any?> : Transformador<Pair<ObjetoDePrueba, T>, Int>
    {
        override fun transformar(origen: Pair<ObjetoDePrueba, T>): Int
        {
            return origen.first.id
        }
    }

    private class TransformadorDeComparacionIdentidad<T : Any?> : Transformador<T, T>
    {
        override fun transformar(origen: T): T
        {
            return origen
        }
    }

    @Test
    fun para_una_secuencia_vacia_retorna_una_secuencia_vacia()
    {
        val resultadoEsperado = emptySequence<Pair<Int, List<ObjetoDePrueba>>>()
        val resultadoObtenido =
                emptySequence<ObjetoDePrueba>()
                    .agruparSecuenciaOrdenadaSegunTransformacion(TransformadorDeLlave(), TransformadorDeComparacion())

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }

    @Test
    fun para_una_secuencia_con_nulls_retorna_una_secuencia_con_nulls()
    {
        val resultadoEsperado =
                sequenceOf<Pair<Int?, List<ObjetoDePrueba?>>>(
                        Pair(null, listOf<ObjetoDePrueba?>(null, null, null))
                                                             )
        val resultadoObtenido =
                sequenceOf<ObjetoDePrueba?>(null, null, null)
                    .agruparSecuenciaOrdenadaSegunTransformacion(TransformadorDeLlaveNull(), TransformadorDeComparacionIdentidad())
                    .map {
                        Pair(it.first, it.second.toList())
                    }

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }

    @Test
    fun para_una_secuencia_con_items_retorna_una_secuencia_agrupada_correcta()
    {
        val secuenciaDePrueba =
                mutableListOf<ObjetoDePrueba>()
                    .apply {
                        addAll(List(3) { ObjetoDePrueba(1, "Objeto 1-$it") })
                        addAll(List(2) { ObjetoDePrueba(2, "Objeto 2-$it") })
                        addAll(List(1) { ObjetoDePrueba(3, "Objeto 3-$it") })
                    }
                    .sortedBy { it.id }
                    .asSequence()

        val resultadoEsperado =
                sequenceOf(
                        Pair(1, listOf(ObjetoDePrueba(1, "Objeto 1-0"), ObjetoDePrueba(1, "Objeto 1-1"), ObjetoDePrueba(1, "Objeto 1-2"))),
                        Pair(2, listOf(ObjetoDePrueba(2, "Objeto 2-0"), ObjetoDePrueba(2, "Objeto 2-1"))),
                        Pair(3, listOf(ObjetoDePrueba(3, "Objeto 3-0")))
                          )
        val resultadoObtenido =
                secuenciaDePrueba
                    .agruparSecuenciaOrdenadaSegunTransformacion(TransformadorDeLlave(), TransformadorDeComparacion())
                    .map {
                        Pair(it.first, it.second.toList())
                    }

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }

    @Test
    fun para_una_secuencia_con_items_y_ultimo_null_retorna_una_secuencia_agrupada_correcta()
    {
        val secuenciaDePrueba =
                mutableListOf<ObjetoDePrueba?>()
                    .apply {
                        addAll(List(3) { ObjetoDePrueba(1, "Objeto 1-$it") })
                        addAll(List(2) { ObjetoDePrueba(2, "Objeto 2-$it") })
                        addAll(List(1) { ObjetoDePrueba(3, "Objeto 3-$it") })
                    }
                    .sortedBy { it?.id }
                    .toMutableList()
                    .apply { add(null) }
                    .asSequence()

        val resultadoEsperado =
                sequenceOf(
                        Pair(1, listOf(ObjetoDePrueba(1, "Objeto 1-0"), ObjetoDePrueba(1, "Objeto 1-1"), ObjetoDePrueba(1, "Objeto 1-2"))),
                        Pair(2, listOf(ObjetoDePrueba(2, "Objeto 2-0"), ObjetoDePrueba(2, "Objeto 2-1"))),
                        Pair(3, listOf(ObjetoDePrueba(3, "Objeto 3-0"))),
                        Pair(null, listOf(null))
                          )
        val resultadoObtenido: Sequence<Pair<Int?, List<ObjetoDePrueba?>>> =
                secuenciaDePrueba
                    .agruparSecuenciaOrdenadaSegunTransformacion(TransformadorDeLlaveNull(), TransformadorDeComparacionIdentidad())
                    .map {
                        Pair(it.first, it.second.toList())
                    }

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }


    @Test
    fun agrupa_correctamente_otra_secuencia_previamente_agrupada()
    {
        val secuenciaDePrueba =
                mutableListOf<ObjetoDePrueba>()
                    .apply {
                        addAll(List(4) { ObjetoDePrueba(1, "Objeto 1-1") })
                        addAll(List(3) { ObjetoDePrueba(1, "Objeto 1-2") })
                        addAll(List(2) { ObjetoDePrueba(2, "Objeto 2-1") })
                        addAll(List(1) { ObjetoDePrueba(2, "Objeto 2-2") })
                    }
                    .toMutableList()
                    .asSequence()

        val agrupacion: Sequence<Pair<Int, List<Pair<ObjetoDePrueba, List<ObjetoDePrueba>>>>> =
                secuenciaDePrueba
                    .agruparSecuenciaOrdenadaSegunTransformacion(TransformadorDeComparacionIdentidad(), TransformadorDeComparacionIdentidad())
                    .map { Pair(it.first, it.second) }
                    .agruparSecuenciaOrdenadaSegunTransformacion(TransformadorDeLlaveEnPareja(), TransformadorDeComparacionIdentidad())

        val resultadoEsperado =
                listOf(
                        Pair(
                                1,
                                listOf(
                                        Pair(ObjetoDePrueba(1, "Objeto 1-1"), List(4) { ObjetoDePrueba(1, "Objeto 1-1") }),
                                        Pair(ObjetoDePrueba(1, "Objeto 1-2"), List(3) { ObjetoDePrueba(1, "Objeto 1-2") })
                                      )
                            ),
                        Pair(
                                2,
                                listOf(
                                        Pair(ObjetoDePrueba(2, "Objeto 2-1"), List(2) { ObjetoDePrueba(2, "Objeto 2-1") }),
                                        Pair(ObjetoDePrueba(2, "Objeto 2-2"), List(1) { ObjetoDePrueba(2, "Objeto 2-2") })

                                      )
                            )
                      )
        val resultadoObtenido = agrupacion.map { Pair(it.first, it.second.map { Pair(it.first, it.second.toList()) }.toList()) }.toList()

        assertEquals(resultadoEsperado, resultadoObtenido)
    }
}