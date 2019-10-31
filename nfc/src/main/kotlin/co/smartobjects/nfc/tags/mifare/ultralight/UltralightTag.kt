package co.smartobjects.nfc.tags.mifare.ultralight

import co.smartobjects.nfc.tags.ISO14443ATag
import co.smartobjects.nfc.tags.mifare.MifareTag

interface UltralightTag : MifareTag, ISO14443ATag
{
    companion object
    {
        const val ULTRALIGHT_VERSION_ID = 0x03.toByte()
    }
}