package co.smartobjects.nfc.lectorestags

import co.smartobjects.nfc.tags.ITag

interface ILectorTag<out Tag : ITag>
{
    val tag: Tag

    /**
     * Cuando falla algún comando (ejemplo autenticación, lectura sin autenticarse, etc) el tag pasa a estado halt. Para
     * lectores PCSC es necesario reconectar al tag para reactivar el tag
     */
    fun reconectar()
    {
        desconectar()
        conectar()
    }

    fun conectar()
    fun desconectar()
    fun darUID(): ByteArray
}