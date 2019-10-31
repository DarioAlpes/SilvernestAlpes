package co.smartobjects.entidades.tagscodificables

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.experimental.and


internal fun concatenarArreglos(vararg arreglosBytes: ByteArray): ByteArray
{
    val tamañoFinal = arreglosBytes.fold(0) { acc, bytes -> acc + bytes.size }
    val arregloFinal = ByteArray(tamañoFinal)

    var posicionSiguiente = 0
    arreglosBytes.forEach {
        System.arraycopy(it, 0, arregloFinal, posicionSiguiente, it.size)
        posicionSiguiente += it.size
    }

    return arregloFinal
}

internal fun longABytes(valorAConvertir: Long): ByteArray
{
    var enProceso = valorAConvertir
    val result = ByteArray(8)
    for (i in 7 downTo 0)
    {
        result[i] = (enProceso and 0xFF).toByte()
        enProceso = enProceso shr 8
    }
    return result
}

internal fun bytesALong(arregloBytes: ByteArray): Long
{
    var result: Long = 0
    for (i in 0..7)
    {
        result = result shl 8
        result = result or (arregloBytes[i] and 0xFF.toByte()).toLong()
    }
    return result
}

private class UtilidadesPruebas
{
    @Test
    fun longABytes_funciona_correctamente()
    {
        val arregloEsperado =
                byteArrayOf(0x88.toByte(), 0x88.toByte(), 0x88.toByte(), 0x88.toByte(), 0x88.toByte(), 0x88.toByte(), 0x88.toByte(), 0x88.toByte())

        Assertions.assertArrayEquals(arregloEsperado, longABytes(-8608480567731124088))
    }
}