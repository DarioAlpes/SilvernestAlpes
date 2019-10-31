package co.smartobjects.nfc.windows.pcsc.lectorestags

import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.LectorPCSC
import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.PCSCTag
import co.smartobjects.nfc.tags.ITag

interface ILectorTagPCSC<out Lector : LectorPCSC, out Tag: ITag>
{
    val tagPCSC: PCSCTag<Tag>
    val lector: Lector
}