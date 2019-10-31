package co.smartobjects.nfc.windows.pcsc.excepciones

import co.smartobjects.nfc.excepciones.NFCProtocolException

class ResultadoPN532Erroneo(mensaje: String, causa: Throwable?) : NFCProtocolException(mensaje, causa)
{
    constructor(mensaje: String) : this(mensaje, null)
}