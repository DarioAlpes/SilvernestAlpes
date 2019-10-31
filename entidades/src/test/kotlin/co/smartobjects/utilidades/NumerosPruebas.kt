package co.smartobjects.utilidades

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals


@DisplayName("Decimal")
internal class DecimalPruebas
{
    @Test
    fun el_numero_cifras_significativas_es_12()
    {
        assertEquals(12, Decimal.CIFRAS_SIGNIFICATIVAS)
    }

    @Test
    fun el_numero_cifras_significativas_del_valor_es_12_si_hay_mas_de_12_cifras_significativas()
    {
        assertEquals(12, Decimal(1.012345678901234).valor.scale())
    }

    @Test
    fun si_hay_mas_de_12_cifras_significativas_se_redondea_al_vecino_mas_cercano()
    {
        val bigDecimalAjustado = { n: Double ->
            val escala = BigDecimal.TEN.pow(Decimal.CIFRAS_SIGNIFICATIVAS + 1).stripTrailingZeros()
            BigDecimal(n * escala.toDouble()).divide(escala).stripTrailingZeros()
        }

        assertEquals(bigDecimalAjustado(1.012345678901), Decimal(1.0123456789010).valor)
        assertEquals(bigDecimalAjustado(1.012345678901), Decimal(1.0123456789011).valor)
        assertEquals(bigDecimalAjustado(1.012345678901), Decimal(1.0123456789012).valor)
        assertEquals(bigDecimalAjustado(1.012345678901), Decimal(1.0123456789013).valor)
        assertEquals(bigDecimalAjustado(1.012345678901), Decimal(1.0123456789014).valor)

        assertEquals(bigDecimalAjustado(1.012345678902), Decimal(1.0123456789015).valor)
        assertEquals(bigDecimalAjustado(1.012345678902), Decimal(1.0123456789016).valor)
        assertEquals(bigDecimalAjustado(1.012345678902), Decimal(1.0123456789017).valor)
        assertEquals(bigDecimalAjustado(1.012345678902), Decimal(1.0123456789018).valor)
        assertEquals(bigDecimalAjustado(1.012345678902), Decimal(1.0123456789019).valor)
    }

    @Test
    fun no_hay_perdida_de_precision()
    {
        val a = Decimal(19.15)
        val b = Decimal(12.45689)
        val resultado = a / b

        assertEquals(Decimal("1.537301846609"), resultado)
    }

    @Test
    fun se_puede_convertir_a_double()
    {
        var valorEsperado = 1.012345678901
        assertEquals(valorEsperado, Decimal(valorEsperado).aDouble())

        valorEsperado = 10.0
        assertEquals(valorEsperado, Decimal(valorEsperado).aDouble())

        valorEsperado = 10.01
        assertEquals(valorEsperado, Decimal(valorEsperado).aDouble())
    }

    @Test
    fun se_puede_convertir_a_int()
    {
        var valorEsperado = 12345
        assertEquals(valorEsperado, Decimal(valorEsperado).aInt())

        valorEsperado = -321654
        assertEquals(valorEsperado, Decimal(valorEsperado).aInt())
    }

    @Test
    fun se_puede_negar_el_valor()
    {
        val doubleEsperado = 1.012345678901

        assertEquals(Decimal(doubleEsperado * -1.0), -Decimal(doubleEsperado))
    }

    @Test
    fun max_retorna_el_maximo_de_dos_valores()
    {
        val a = Decimal(1.01234)
        val b = Decimal(3.01234)

        assertEquals(b, a.max(b))
        assertEquals(b, b.max(a))
    }

    @Test
    fun max_retorna_el_maximo_de_dos_valores_solo_incluyendo_cifras_significativas()
    {
        val a = Decimal(0.0000000000009)
        val b = Decimal(0.0000000000070)

        assertEquals(b, a.max(b))
        assertEquals(b, b.max(a))
    }

    @Test
    fun min_retorna_el_minimo_de_dos_valores()
    {
        val a = Decimal(1.01234)
        val b = Decimal(3.01234)

        assertEquals(a, a.min(b))
        assertEquals(a, b.min(a))
    }

    @Test
    fun min_retorna_el_minimo_de_dos_valores_solo_incluyendo_cifras_significativas()
    {
        val a = Decimal(0.0000000000009)
        val b = Decimal(0.0000000000070)

        assertEquals(a, a.min(b))
        assertEquals(a, b.min(a))
    }
}

internal class FuncionesDeExtensionSobreDecimalPruebas
{
    @Nested
    inner class SumarSobreArreglo
    {
        @Test
        fun para_rango_vacio_retorna_cero()
        {
            val valorEsperado = Decimal.CERO
            val retornado = arrayOf<Decimal>().sumar()

            assertEquals(valorEsperado, retornado)

            val retornadoExtrayendo = arrayOf<DummyConDecimal>().sumar { it.valor }

            assertEquals(valorEsperado, retornadoExtrayendo)
        }

        @Test
        fun con_valores_retorna_resultado_correcto()
        {
            val valorEsperado = Decimal(1 + 2 + 3 + 4)

            val retornado = arrayOf(Decimal(1), Decimal(2), Decimal(3), Decimal(4)).sumar()

            assertEquals(Decimal(1 + 2 + 3 + 4), retornado)

            val retornadoExtrayendo =
                    arrayOf(
                            DummyConDecimal(Decimal(1)),
                            DummyConDecimal(Decimal(2)),
                            DummyConDecimal(Decimal(3)),
                            DummyConDecimal(Decimal(4))
                           ).sumar { it.valor }

            assertEquals(valorEsperado, retornadoExtrayendo)
        }
    }

    @Nested
    inner class SumarSobreIterable
    {
        @Test
        fun para_rango_vacio_retorna_cero()
        {
            val valorEsperado = Decimal.CERO
            val retornado = listOf<Decimal>().sumar()

            assertEquals(valorEsperado, retornado)

            val retornadoExtrayendo = listOf<DummyConDecimal>().sumar { it.valor }

            assertEquals(valorEsperado, retornadoExtrayendo)
        }

        @Test
        fun con_valores_retorna_resultado_correcto()
        {
            val valorEsperado = Decimal(1 + 2 + 3 + 4)

            val retornado = listOf(Decimal(1), Decimal(2), Decimal(3), Decimal(4)).sumar()

            assertEquals(Decimal(1 + 2 + 3 + 4), retornado)

            val retornadoExtrayendo =
                    listOf(
                            DummyConDecimal(Decimal(1)),
                            DummyConDecimal(Decimal(2)),
                            DummyConDecimal(Decimal(3)),
                            DummyConDecimal(Decimal(4))
                          ).sumar { it.valor }

            assertEquals(valorEsperado, retornadoExtrayendo)
        }
    }

    @Nested
    inner class SumarSobreSequencia
    {
        @Test
        fun para_rango_vacio_retorna_cero()
        {
            val valorEsperado = Decimal.CERO
            val retornado = sequenceOf<Decimal>().sumar()

            assertEquals(valorEsperado, retornado)

            val retornadoExtrayendo = sequenceOf<DummyConDecimal>().sumar { it.valor }

            assertEquals(valorEsperado, retornadoExtrayendo)
        }

        @Test
        fun con_valores_retorna_resultado_correcto()
        {
            val valorEsperado = Decimal(1 + 2 + 3 + 4)

            val retornado = sequenceOf(Decimal(1), Decimal(2), Decimal(3), Decimal(4)).sumar()

            assertEquals(Decimal(1 + 2 + 3 + 4), retornado)

            val retornadoExtrayendo =
                    sequenceOf(
                            DummyConDecimal(Decimal(1)),
                            DummyConDecimal(Decimal(2)),
                            DummyConDecimal(Decimal(3)),
                            DummyConDecimal(Decimal(4))
                              ).sumar { it.valor }

            assertEquals(valorEsperado, retornadoExtrayendo)
        }
    }

    class DummyConDecimal(val valor: Decimal)
}