package co.smartobjects.entidades.tagscodificables

import java.nio.ByteBuffer


abstract class SerializableABytes
{
    abstract val tamañoTotalEnBytes: Int

    abstract fun escribirComoBytes(buffer: ByteBuffer)

    fun aByteArray(): ByteArray
    {
        val buffer = ByteBuffer.allocate(tamañoTotalEnBytes)

        escribirComoBytes(buffer)

        return buffer.array()
    }
}