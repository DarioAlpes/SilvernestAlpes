package co.smartobjects.utilidades

import java.math.BigDecimal
import java.math.RoundingMode

class Decimal(valorEntrada: BigDecimal) : Comparable<Decimal>
{
    companion object
    {
        const val CIFRAS_SIGNIFICATIVAS = 12

        @JvmField
        val CERO = Decimal(BigDecimal.ZERO)

        @JvmField
        val UNO = Decimal(BigDecimal.ONE)

        @JvmField
        val DIEZ = Decimal(BigDecimal.TEN)
    }

    private val valorRedondeado: BigDecimal = valorEntrada.setScale(CIFRAS_SIGNIFICATIVAS, RoundingMode.HALF_UP)
    val valor: BigDecimal = valorRedondeado.stripTrailingZeros()

    constructor(valor: Int) : this(BigDecimal(valor))
    constructor(valor: Long) : this(BigDecimal(valor))
    constructor(valor: String) : this(BigDecimal(valor))
    constructor(valor: Double) : this(BigDecimal.valueOf(valor))

    override fun equals(other: Any?): Boolean
    {
        return other is Decimal && other.valor == valor
    }

    override fun hashCode(): Int
    {
        return valor.hashCode()
    }

    override fun toString(): String
    {
        return valor.toPlainString()
    }

    fun aDouble() = valor.toDouble()
    fun aInt() = valor.toInt()


    operator fun compareTo(other: Double) = compareTo(Decimal(other))

    operator fun compareTo(other: Int) = compareTo(Decimal(other))

    override fun compareTo(other: Decimal): Int
    {
        return valor.compareTo(other.valor)
    }

    operator fun times(numero: Decimal): Decimal
    {
        return Decimal(valorRedondeado.multiply(numero.valorRedondeado))
    }

    operator fun times(numero: Int): Decimal
    {
        return this * Decimal(numero)
    }

    operator fun plus(numero: Decimal): Decimal
    {
        return Decimal(valorRedondeado.plus(numero.valorRedondeado))
    }

    operator fun minus(numero: Decimal): Decimal
    {
        return Decimal(valorRedondeado.minus(numero.valorRedondeado))
    }

    operator fun plus(numero: Int): Decimal
    {
        return this + Decimal(numero)
    }

    operator fun div(numero: Decimal): Decimal
    {
        return Decimal(valorRedondeado.div(numero.valorRedondeado))
    }

    operator fun div(numero: Double): Decimal
    {
        return this.div(Decimal(numero))
    }

    operator fun unaryMinus(): Decimal
    {
        return Decimal(valorRedondeado.negate())
    }

    fun max(otro: Decimal): Decimal = if (otro > this) otro else this
    fun min(otro: Decimal): Decimal = if (otro < this) otro else this
}

fun Array<Decimal>.sumar() = fold(Decimal.CERO) { sumado, valorSiguiente -> sumado + valorSiguiente }
fun <T> Array<T>.sumar(extraerValor: (T) -> Decimal): Decimal
{
    return fold(Decimal.CERO) { sumado, valorSiguiente -> sumado + extraerValor(valorSiguiente) }
}

fun Iterable<Decimal>.sumar() = fold(Decimal.CERO) { sumado, valorSiguiente -> sumado + valorSiguiente }
fun <T> Iterable<T>.sumar(extraerValor: (T) -> Decimal): Decimal
{
    return fold(Decimal.CERO) { sumado, valorSiguiente -> sumado + extraerValor(valorSiguiente) }
}

fun Sequence<Decimal>.sumar() = fold(Decimal.CERO) { sumado, valorSiguiente -> sumado + valorSiguiente }
fun <T> Sequence<T>.sumar(extraerValor: (T) -> Decimal): Decimal
{
    return fold(Decimal.CERO) { sumado, valorSiguiente -> sumado + extraerValor(valorSiguiente) }
}