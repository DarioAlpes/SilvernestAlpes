package co.smartobjects.nfc.excepciones

open class NFCProtocolException(mensage: String, causa: Throwable?) : Exception(mensage, causa)
{
    constructor(mensage: String) : this(mensage, null)
}