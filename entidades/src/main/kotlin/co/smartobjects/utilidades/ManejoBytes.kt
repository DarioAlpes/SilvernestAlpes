package co.smartobjects.utilidades


fun Byte.aHexString(): String
{
    val CHARS = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    val i = this.toInt()
    val char2 = CHARS[i and 0x0f]
    val char1 = CHARS[i shr 4 and 0x0f]
    return "$char1$char2"
}

fun ByteArray.aHexString(separador: String = ""): String
{
    return this.joinToString(separator = separador, transform = Byte::aHexString)
}


fun String.hexAByteArray(): ByteArray
{
    val data = ByteArray(length / 2)
    var i = 0
    while (i < length)
    {
        data[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
        i += 2
    }
    return data
}