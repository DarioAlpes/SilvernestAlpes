package co.smartobjects.nfc.windows.pcsc.lectores.pcsc

import co.smartobjects.nfc.tags.ITag
import javax.smartcardio.Card

data class PCSCTag<out T : ITag>(val tag: T, val tarjeta: Card)