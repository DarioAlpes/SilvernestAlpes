package co.smartobjects.nfc.tags

interface ISO14443ATag : ITag
{
    companion object
    {
        val COMANDO_LEER_UID = byteArrayOf(0xFF.toByte(), 0xCA.toByte(), 0x00, 0x00, 0x00)

        fun darComandoLeerPagina(paginaALeer: Byte): ByteArray
        {
            return byteArrayOf(0xFF.toByte(), 0xB0.toByte(), 0x00, paginaALeer, 0x04)
        }

        fun darComandoEscribirPagina(paginaAEscribir: Byte, datos: ByteArray): ByteArray
        {
            val comando = ByteArray(5 + datos.size)
            comando[0] = 0xFF.toByte()
            comando[1] = 0xD6.toByte()
            comando[2] = 0x00
            comando[3] = paginaAEscribir
            comando[4] = datos.size.toByte()

            System.arraycopy(datos, 0, comando, 5, datos.size)

            return comando
        }
    }
}