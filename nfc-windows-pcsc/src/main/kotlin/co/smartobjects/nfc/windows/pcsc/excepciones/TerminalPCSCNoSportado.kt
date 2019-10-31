package co.smartobjects.nfc.windows.pcsc.excepciones

import co.smartobjects.nfc.excepciones.NFCProtocolException

class TerminalPCSCNoSportado(nombreTerminal: String, causa: Throwable?) : NFCProtocolException("Terminal no soportado: $nombreTerminal", causa)
{
    constructor(nombreTerminal: String) : this(nombreTerminal, null)
}