package co.smartobjects.utilidades

import org.junit.jupiter.api.*


@DisplayName("Opcional")
internal class OpcionalPruebas
{
    @Nested
    inner class Vacio
    {
        @Nested
        inner class Nulo
        {
            private val valorPruebas: Opcional<Int?> = Opcional.Vacio()

            @Test
            fun esVacio_retrona_true()
            {
                Assertions.assertTrue(valorPruebas.esVacio)
            }

            @Test
            fun valor_lanza_excepcion_NoSuchElementException()
            {
                assertThrows<NoSuchElementException> {
                    valorPruebas.valor
                }
            }

            @Test
            fun valorUOtro_retorna_otro_cuando_otro_es_null()
            {
                Assertions.assertEquals(null, valorPruebas.valorUOtro(null))
            }

            @Test
            fun valorUOtro_retorna_otro_cuando_otro_no_es_null()
            {
                Assertions.assertEquals(10, valorPruebas.valorUOtro(10))
            }
        }

        @Nested
        inner class NoNulo
        {
            private val valorPruebas: Opcional<Int> = Opcional.Vacio()

            @Test
            fun esVacio_retrona_true()
            {
                Assertions.assertTrue(valorPruebas.esVacio)
            }

            @Test
            fun valor_lanza_excepcion_NoSuchElementException()
            {
                assertThrows<NoSuchElementException> {
                    valorPruebas.valor
                }
            }

            @Test
            fun valorUOtro_retorna_otro_cuando_otro_es_null()
            {
                Assertions.assertEquals(null, valorPruebas.valorUOtro(null))
            }

            @Test
            fun valorUOtro_retorna_otro_cuando_otro_no_es_null()
            {
                Assertions.assertEquals(10, valorPruebas.valorUOtro(10))
            }
        }
    }

    @Nested
    inner class ConValor
    {
        @Nested
        inner class Nulo
        {
            private val valorPruebas: Opcional<Int?> = Opcional.De(null)

            @Test
            fun esVacio_retrona_false()
            {
                Assertions.assertFalse(valorPruebas.esVacio)
            }

            @Test
            fun valor_retorna_valor_correcto()
            {
                Assertions.assertEquals(null, valorPruebas.valor)
            }

            @Test
            fun valorUOtro_retorna_valor_cuando_otro_es_null()
            {
                Assertions.assertEquals(null, valorPruebas.valorUOtro(null))
            }

            @Test
            fun valorUOtro_retorna_valor_cuando_otro_no_es_null()
            {
                Assertions.assertEquals(null, valorPruebas.valorUOtro(10))
            }
        }

        @Nested
        inner class NoNulo
        {
            private val valorPruebas: Opcional<Int> = Opcional.De(20)

            @Test
            fun esVacio_retrona_false()
            {
                Assertions.assertFalse(valorPruebas.esVacio)
            }

            @Test
            fun valor_retorna_valor_correcto()
            {
                Assertions.assertEquals(20, valorPruebas.valor)
            }

            @Test
            fun valorUOtro_retorna_valor_cuando_otro_es_null()
            {
                Assertions.assertEquals(20, valorPruebas.valorUOtro(null))
            }

            @Test
            fun valorUOtro_retorna_valor_cuando_otro_no_es_null()
            {
                Assertions.assertEquals(20, valorPruebas.valorUOtro(10))
            }
        }
    }

    @Nested
    inner class DeNullable
    {
        @Nested
        inner class Nulo
        {
            private val valorPruebas: Opcional<Int> = Opcional.DeNullable(null)

            @Test
            fun esVacio_retrona_true()
            {
                Assertions.assertTrue(valorPruebas.esVacio)
            }

            @Test
            fun valor_lanza_excepcion_NoSuchElementException()
            {
                assertThrows<NoSuchElementException> {
                    valorPruebas.valor
                }
            }

            @Test
            fun valorUOtro_retorna_otro_cuando_otro_es_null()
            {
                Assertions.assertEquals(null, valorPruebas.valorUOtro(null))
            }

            @Test
            fun valorUOtro_retorna_otro_cuando_otro_no_es_null()
            {
                Assertions.assertEquals(10, valorPruebas.valorUOtro(10))
            }
        }

        @Nested
        inner class NoNulo
        {
            private val valorPruebas: Opcional<Int> = Opcional.DeNullable(20)

            @Test
            fun esVacio_retrona_false()
            {
                Assertions.assertFalse(valorPruebas.esVacio)
            }

            @Test
            fun valor_retorna_valor_correcto()
            {
                Assertions.assertEquals(20, valorPruebas.valor)
            }

            @Test
            fun valorUOtro_retorna_valor_cuando_otro_es_null()
            {
                Assertions.assertEquals(20, valorPruebas.valorUOtro(null))
            }

            @Test
            fun valorUOtro_retorna_valor_cuando_otro_no_es_null()
            {
                Assertions.assertEquals(20, valorPruebas.valorUOtro(10))
            }
        }
    }
}