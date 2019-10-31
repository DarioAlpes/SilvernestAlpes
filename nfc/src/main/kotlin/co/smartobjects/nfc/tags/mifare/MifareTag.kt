package co.smartobjects.nfc.tags.mifare

import co.smartobjects.nfc.tags.ITag

interface MifareTag : ITag
{
    companion object
    {
        val COMANDO_OBTENER_VERSION = byteArrayOf(0x60)
    }
}