package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.ComparadorEntidades
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class SecuenciaAgrupadoraDeSecuenciasOrdenadasPruebas
{
    private data class PadreIzquierdo(val id: Int, val campo: String)
    private data class PadreDerecho(val id: Int, val campo: String)

    private val comparadorEntidades = object : ComparadorEntidades<Int, Int>
    {
        override fun comparar(origenIzquierda: Int, origenDerecha: Int): Int
        {
            return origenIzquierda.compareTo(origenDerecha)
        }
    }

    @Test
    fun para_ambas_secuencias_vacias_retorna_una_secuencia_vacia()
    {
        val resultadoEsperado = emptySequence<Pair<ElementoDeAgrupacion<PadreIzquierdo, Int>?, ElementoDeAgrupacion<PadreDerecho, Int>?>>()
        val resultadoObtenido =
                SecuenciaAgrupadoraDeSecuenciasOrdenadas(
                        sequenceOf<ElementoDeAgrupacion<PadreIzquierdo, Int>>(),
                        sequenceOf<ElementoDeAgrupacion<PadreDerecho, Int>>(),
                        comparadorEntidades
                                                        )

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }

    @Test
    fun para_secuencia_izquierda_con_datos_y_derecha_vacia_retorna_la_secuencia_izquieda_como_pair()
    {
        val secuenciaIzquierda =
                mutableListOf<ElementoDeAgrupacion<PadreIzquierdo, Int>>()
                    .apply {
                        for (idPadre in 0..2)
                        {
                            val elemento = ElementoDeAgrupacion(PadreIzquierdo(idPadre, "Padre $idPadre"), { it.id })

                            add(elemento)
                        }
                    }
                    .sortedBy { it.elemento.id }
                    .asSequence()

        val resultadoEsperado = secuenciaIzquierda.map { Pair(it, null) }

        val resultadoObtenido =
                SecuenciaAgrupadoraDeSecuenciasOrdenadas(
                        secuenciaIzquierda,
                        sequenceOf<ElementoDeAgrupacion<PadreDerecho, Int>>(),
                        comparadorEntidades
                                                        )

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }

    @Test
    fun para_secuencia_derecha_con_datos_e_izquierda_vacia_retorna_la_secuencia_derecha_como_pair()
    {
        val secuenciaDerecha =
                mutableListOf<ElementoDeAgrupacion<PadreDerecho, Int>>()
                    .apply {
                        for (idPadre in 0..2)
                        {
                            val elemento = ElementoDeAgrupacion(PadreDerecho(idPadre, "Padre $idPadre"), { it.id })

                            add(elemento)
                        }
                    }
                    .sortedBy { it.elemento.id }
                    .asSequence()

        val resultadoEsperado = secuenciaDerecha.map { Pair(null, it) }

        val resultadoObtenido =
                SecuenciaAgrupadoraDeSecuenciasOrdenadas(
                        sequenceOf<ElementoDeAgrupacion<PadreIzquierdo, Int>>(),
                        secuenciaDerecha,
                        comparadorEntidades
                                                        )

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }

    @Test
    fun para_ambas_secuencias_con_todos_los_datos_compartidos_retorna_secuencias_agrupadas_correctamente()
    {
        val listaIzquierda =
                mutableListOf<ElementoDeAgrupacion<PadreIzquierdo, Int>>()
                    .apply {
                        for (idPadre in 0..2)
                        {
                            val elemento = ElementoDeAgrupacion(
                                    PadreIzquierdo(idPadre, "Padre $idPadre"), { it.id }
                                                               )

                            add(elemento)
                        }
                    }
                    .sortedBy { it.elemento.id }

        val secuenciaIzquierda = listaIzquierda.asSequence()


        val listaDerecha =
                listaIzquierda
                    .map { relacionIzquierda ->
                        val idPadreDerecho = relacionIzquierda.elemento.id
                        ElementoDeAgrupacion(PadreDerecho(idPadreDerecho, "Padre $idPadreDerecho"), { it.id })
                    }
                    .sortedBy { it.elemento.id }

        val secuenciaDerecha = listaDerecha.asSequence()

        val resultadoEsperado =
                sequenceOf(
                        Pair(listaIzquierda[0], listaDerecha[0]),
                        Pair(listaIzquierda[1], listaDerecha[1]),
                        Pair(listaIzquierda[2], listaDerecha[2])
                          )

        val resultadoObtenido =
                SecuenciaAgrupadoraDeSecuenciasOrdenadas(
                        secuenciaIzquierda,
                        secuenciaDerecha,
                        comparadorEntidades
                                                        )

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }

    @Test
    fun para_secuencia_izquierda_con_elementos_que_no_estan_en_la_derecha_retorna_secuencias_agrupadas_correctamente()
    {
        val listaIzquierda =
                mutableListOf<ElementoDeAgrupacion<PadreIzquierdo, Int>>()
                    .apply {
                        for (idPadre in 0..2)
                        {
                            val elemento = ElementoDeAgrupacion(PadreIzquierdo(idPadre, "Padre $idPadre"), { it.id })

                            add(elemento)
                        }
                    }
                    .sortedBy { it.elemento.id }

        val secuenciaIzquierda = listaIzquierda.asSequence()

        val listaDerecha =
                mutableListOf<ElementoDeAgrupacion<PadreDerecho, Int>>()
                    .apply {
                        val idUnicoPadreCompartido = listaIzquierda[0].elemento.id

                        add(
                                ElementoDeAgrupacion(
                                        PadreDerecho(idUnicoPadreCompartido, "Padre $idUnicoPadreCompartido"),
                                        { it.id }
                                                    )
                           )
                    }

        val secuenciaDerecha = listaDerecha.asSequence()

        val resultadoEsperado =
                sequenceOf(
                        Pair(listaIzquierda[0], listaDerecha[0]),
                        Pair(listaIzquierda[1], null),
                        Pair(listaIzquierda[2], null)
                          )

        val resultadoObtenido =
                SecuenciaAgrupadoraDeSecuenciasOrdenadas(
                        secuenciaIzquierda,
                        secuenciaDerecha,
                        comparadorEntidades
                                                        )

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }

    @Test
    fun para_secuencia_derecha_con_elementos_que_no_estan_en_la_izquierda_retorna_secuencias_agrupadas_correctamente()
    {
        val listaDerecha =
                mutableListOf<ElementoDeAgrupacion<PadreDerecho, Int>>()
                    .apply {
                        for (idPadre in 0..2)
                        {
                            val relacion = ElementoDeAgrupacion(PadreDerecho(idPadre, "Padre $idPadre"), { it.id })

                            add(relacion)
                        }
                    }
                    .sortedBy { it.elemento.id }

        val secuenciaDerecha = listaDerecha.asSequence()

        val listaIzquierda =
                mutableListOf<ElementoDeAgrupacion<PadreIzquierdo, Int>>()
                    .apply {
                        val idUnicoPadreCompartido = listaDerecha[0].elemento.id

                        add(
                                ElementoDeAgrupacion(
                                        PadreIzquierdo(idUnicoPadreCompartido, "Padre $idUnicoPadreCompartido"),
                                        { it.id }
                                                    )
                           )
                    }

        val secuenciaIzquierda = listaIzquierda.asSequence()

        val resultadoEsperado =
                sequenceOf(
                        Pair(listaIzquierda[0], listaDerecha[0]),
                        Pair(null, listaDerecha[1]),
                        Pair(null, listaDerecha[2])
                          )

        val resultadoObtenido =
                SecuenciaAgrupadoraDeSecuenciasOrdenadas(
                        secuenciaIzquierda,
                        secuenciaDerecha,
                        comparadorEntidades
                                                        )

        assertEquals(resultadoEsperado.toList(), resultadoObtenido.toList())
    }
}