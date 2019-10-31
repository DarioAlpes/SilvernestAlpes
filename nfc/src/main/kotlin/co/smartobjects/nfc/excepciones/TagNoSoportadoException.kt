package co.smartobjects.nfc.excepciones

class TagNoSoportadoException(nombreTag: String, causa: Throwable?) : NFCProtocolException("Tag no soportado: $nombreTag", causa)
{
    constructor(nombreTag: String) : this(nombreTag, null)
}