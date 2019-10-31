package co.smartobjects.nfc.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


@Throws(IOException::class)
fun comprimir(data: ByteArray): ByteArray
{
    val deflater = Deflater()
    deflater.setInput(data)
    val outputStream = ByteArrayOutputStream(data.size)
    deflater.finish()

    val buffer = ByteArray(1024)

    while (!deflater.finished())
    {
        val count = deflater.deflate(buffer)
        outputStream.write(buffer, 0, count)
    }

    outputStream.close()

    return outputStream.toByteArray()
}

@Throws(IOException::class, DataFormatException::class)
fun descomprimir(data: ByteArray): ByteArray
{
    try
    {
        val inflater = Inflater()
        inflater.setInput(data)
        val outputStream = ByteArrayOutputStream(data.size)
        val buffer = ByteArray(1024)

        while (!inflater.finished())
        {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }

        outputStream.close()

        return outputStream.toByteArray()
    }
    catch (e: Exception)
    {
        return byteArrayOf()
    }
}

fun descifrarConDES(mensaje: ByteArray, llave: ByteArray, ivBytes: ByteArray): ByteArray
{
    return realizar3DES(Cipher.DECRYPT_MODE, mensaje, llave, ivBytes)
}

fun cifrarConDES(mensaje: ByteArray, llave: ByteArray, ivBytes: ByteArray): ByteArray
{
    return realizar3DES(Cipher.ENCRYPT_MODE, mensaje, llave, ivBytes)
}

private fun realizar3DES(modo: Int, mensaje: ByteArray, llave: ByteArray, ivBytes: ByteArray): ByteArray
{
    val key = SecretKeySpec(llave, "DESede")
    val iv = IvParameterSpec(ivBytes)
    val cifrado = Cipher.getInstance("DESede/CBC/NoPadding")
    cifrado.init(modo, key, iv)
    return cifrado.doFinal(mensaje)
}

fun byteAIntSinSigno(byte: Byte): Int
{
    return 0xFF and byte.toInt()
}