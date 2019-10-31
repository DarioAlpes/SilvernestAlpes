package co.smartobjects.nfc.windows.pcsc.lectores.pcsc

import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.excepciones.TagNoSoportadoException
import co.smartobjects.nfc.tags.ISO14443ATag
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.nfc.tags.mifare.MifareTag
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1
import co.smartobjects.nfc.windows.pcsc.excepciones.ResultadoPN532Erroneo
import co.smartobjects.nfc.windows.pcsc.excepciones.TerminalPCSCNoSportado
import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.pn532.LectorACR1222L
import co.smartobjects.utilidades.aHexString
import javax.smartcardio.*

abstract class LectorPCSC protected constructor(private val terminal: CardTerminal)
{
    companion object
    {
        private const val PREFIJO_ACR1222 = "ACS ACR1222"
        fun darPrimerLector(): LectorPCSC?
        {
            return try
            {
                val fabricaTerminales = TerminalFactory.getDefault()
                val terminales = fabricaTerminales.terminals()?.list()
                if (terminales != null && terminales.isNotEmpty())
                {
                    val terminal = terminales.firstOrNull { it.name.startsWith(PREFIJO_ACR1222) }
                    println("\nterminal")
                    if (terminal != null)
                    {
                        LectorACR1222L(terminal)
                    }
                    else
                    {
                        throw TerminalPCSCNoSportado(terminales.firstOrNull()?.name ?: "Ninguno")
                    }
                }
                else
                {
                    null
                }
            }
            catch (e: IndexOutOfBoundsException)
            {
                null
            }
            catch (e: CardException)
            {
                null
            }
        }
    }

    abstract fun envolverComandoNativoEnAPDU(comandoNativo: ByteArray): CommandAPDU
    abstract fun desenvolverResultadoComandoNativo(resultado: ByteArray): ByteArray

    /**
     * Máxima longitud del mensaje es 16 caracteres ANSI
     */
    fun mostrarMensajePorControl(mensaje: String): ByteArray
    {
        return ejecutarComandoAPDUPorControlYVerificarResultado(crearAPDUMostrarMensaje(mensaje))
    }

    fun mostrarMensajePorTag(mensaje: String, pcscTag: PCSCTag<ITag>): ByteArray
    {
        return ejecutarComandoAPDUPorCanalYVerificarResultado(pcscTag.tarjeta.basicChannel, crearAPDUMostrarMensaje(mensaje))
    }

    fun hacerSonidoTagEncontradoOPerdidoPorControl()
    {
        ejecutarComandoAPDUPorControl(crearAPDUSonido(1))
    }

    fun hacerSonidoTagEncontradoOPerdidoPorTag(pcscTag: PCSCTag<ITag>)
    {
        ejecutarComandoAPDUPorControlTarjeta(pcscTag.tarjeta, crearAPDUSonido(1))
    }

    fun hacerSonidoExitoPorControl()
    {
        ejecutarComandoAPDUPorControl(crearAPDUSonido(1))
        ejecutarComandoAPDUPorControl(crearAPDUSonido(1))
    }

    fun hacerSonidoExitoPorTag(pcscTag: PCSCTag<ITag>)
    {
        ejecutarComandoAPDUPorControlTarjeta(pcscTag.tarjeta, crearAPDUSonido(1))
        ejecutarComandoAPDUPorControlTarjeta(pcscTag.tarjeta, crearAPDUSonido(1))
    }

    fun hacerSonidoErrorPorControl()
    {
        ejecutarComandoAPDUPorControl(crearAPDUSonido(10))
    }

    fun hacerSonidoErrorPorTag(pcscTag: PCSCTag<ITag>)
    {
        ejecutarComandoAPDUPorControlTarjeta(pcscTag.tarjeta, crearAPDUSonido(10))
    }

    fun desactivarSonidosPorDefecto()
    {
        ejecutarComandoAPDUPorControl(crearAPDUActivarODesactivarSonido(false))
    }

    fun activarSonidosPorDefecto()
    {
        ejecutarComandoAPDUPorControl(crearAPDUActivarODesactivarSonido(true))
    }

    private fun crearAPDUActivarODesactivarSonido(activar: Boolean): CommandAPDU
    {
        var byteSonido = (1 shl 1) or (1 shl 7)
        if (activar)
        {
            byteSonido = byteSonido or (1 shl 4) or (1 shl 5)
        }
        return CommandAPDU(0xE0, 0x00, 0x00, 0x21, byteArrayOf(byteSonido.toByte()))
    }

    private fun crearAPDUSonido(duracion: Byte): CommandAPDU
    {
        return CommandAPDU(0xE0, 0x00, 0x00, 0x28, byteArrayOf(duracion))
    }

    private fun crearAPDUMostrarMensaje(mensaje: String): CommandAPDU
    {
        return CommandAPDU(0xFF, 0x01, 0x68, 0x00, padMensagePantalla(mensaje).toByteArray())
    }

    private fun padMensagePantalla(mensage: String): String
    {
        return when
        {
            mensage.length < 16  -> mensage + " ".repeat(16 - mensage.length)
            mensage.length == 16 -> mensage
            else                 -> mensage.substring(0, 16)
        }
    }

    private fun ejecutarComandoAPDUPorControl(apduComando: CommandAPDU): ByteArray
    {
        try
        {
            val canalControl = terminal.connect("DIRECT")
            try
            {
                return canalControl.transmitControlCommand(darCodigoControl(), apduComando.bytes)
            }
            finally
            {
                canalControl.disconnect(false)
            }
        }
        catch (e: CardException)
        {
            throw NFCProtocolException("Error enviando comando ${apduComando.bytes.aHexString(" ")}", e)
        }
    }

    private fun ejecutarComandoAPDUPorControlYVerificarResultado(apduComando: CommandAPDU): ByteArray
    {
        val resultado = ejecutarComandoAPDUPorControl(apduComando)
        return verificarResultado(apduComando, ResponseAPDU(resultado))
    }

    private fun ejecutarComandoAPDUPorCanal(canalComando: CardChannel, apduComando: CommandAPDU): ResponseAPDU
    {
        try
        {
            return canalComando.transmit(apduComando)
        }
        catch (e: CardException)
        {
            throw NFCProtocolException("Error enviando comando ${apduComando.bytes.aHexString(" ")}", e)
        }
        catch (e: IllegalArgumentException)
        {
            throw NFCProtocolException("Error enviando comando ${apduComando.bytes.aHexString(" ")}", e)
        }
    }

    fun ejecutarComandoAPDUPorCanalYVerificarResultado(canalComando: CardChannel, apduComando: CommandAPDU): ByteArray
    {
        val resultado = ejecutarComandoAPDUPorCanal(canalComando, apduComando)
        return verificarResultado(apduComando, resultado)
    }

    private fun ejecutarComandoAPDUPorControlTarjeta(tarjeta: Card, apduComando: CommandAPDU): ByteArray
    {
        try
        {
            return tarjeta.transmitControlCommand(darCodigoControl(), apduComando.bytes)
        }
        catch (e: CardException)
        {
            throw NFCProtocolException("Error enviando comando ${apduComando.bytes.aHexString(" ")}", e)
        }
    }

    private fun ejecutarComandoAPDUPorControlTarjetaYVerificarResultado(tarjeta: Card, apduComando: CommandAPDU): ByteArray
    {
        val resultado = ejecutarComandoAPDUPorControlTarjeta(tarjeta, apduComando)
        return verificarResultado(apduComando, ResponseAPDU(resultado))
    }

    private fun verificarResultado(apduComando: CommandAPDU, resultado: ResponseAPDU): ByteArray
    {
        if (resultado.sW1 == 0x90 && resultado.sW2 == 0x00)
        {
            return resultado.data
        }
        else
        {
            throw NFCProtocolException("Error ejecutando comando con APDU {${apduComando.bytes.aHexString(" ")}}. " +
                                       "Resultado obtenido: {${byteArrayOf(resultado.sW1.toByte(), resultado.sW2.toByte()).aHexString(" ")}}")
        }

    }

    protected open fun darCodigoControl(): Int
    {
        val command = 3500
        val isWindows = System.getProperty("os.name").startsWith("Windows")

        return if (isWindows)
        {
            0x00310000 or (command shl 2)
        }
        else
        {
            0x42000000 or command
        }
    }

    fun esperarTag(timeout: Long): PCSCTag<ITag>?
    {
        if (terminal.waitForCardPresent(timeout))
        {
            val tarjeta = terminal.connect("*")
            val bytesATR = tarjeta.atr.bytes
            if (esUltralight(bytesATR))
            {
                try
                {
                    val version = obtenerVersionTarjetaDesfire(tarjeta)
                    if (UltralightEV1.esUltralightEV1(version))
                    {
                        return PCSCTag(UltralightEV1(version[6]), tarjeta)
                    }
                }
                catch (e: ResultadoPN532Erroneo)
                {
                    // Ultralight C no soporta comando de dar versión. Si se llego a este punto se puede suponer que es
                    // ultralight o ultralight C
                    val tarjetaC = reconectarTag(tarjeta) // Se debe desconectar y reconectar después de una fallo
                    if (esUltralightC(tarjetaC))
                    {
                        return PCSCTag(UltralightC(), reconectarTag(tarjetaC)) // Reconectar para cancelar autenticación
                    }
                }
                // UltralightC no tiene comando de dar versión
                // aparentemente la única opción es ejecutar el comando de autenticación (al menos la primera parte)
                // como dice en http://stackoverflow.com/questions/11897813/distinguish-mifare-ultralight-from-mifare-ultralight-c.
                // Aunque para diferenciar entre la C y la EV1 es suficiente con revisar que get version falle
                // http://stackoverflow.com/questions/37002498/distinguish-ntag213-from-mf0icu2/37047375#37047375
            }
            throw TagNoSoportadoException("Desconocido: ATR[${bytesATR.aHexString(" ")}]")
        }
        else
        {
            return null
        }
    }

    private fun reconectarTag(tarjeta: Card): Card
    {
        try
        {
            tarjeta.disconnect(true)
            return terminal.connect("*")
        }
        catch (e: CardException)
        {
            throw NFCProtocolException("Se perdio la conexión con el tag mientras se intentaba reconexión", e)
        }

    }

    fun esperarDesconexionTag(timeout: Long)
    {
        terminal.waitForCardAbsent(timeout)
    }

    private fun obtenerVersionTarjetaDesfire(tarjeta: Card): ByteArray
    {
        return ejecutarComandoNativo(tarjeta, MifareTag.COMANDO_OBTENER_VERSION)
    }

    private fun esUltralightC(tarjeta: Card): Boolean
    {
        return try
        {
            ejecutarComandoNativo(tarjeta, UltralightC.COMANDO_INICIAR_AUTENTICACION)
            true
        }
        catch (e: ResultadoPN532Erroneo)
        {
            false
        }
    }

    fun ejecutarComandoNativo(tarjeta: Card, comandoNativo: ByteArray): ByteArray
    {
        return desenvolverResultadoComandoNativo(ejecutarComandoAPDUPorControlTarjetaYVerificarResultado(tarjeta, envolverComandoNativoEnAPDU(comandoNativo)))
    }

    private fun esUltralight(bytesATR: ByteArray): Boolean
    {
        // ATR de tamaño 20 y corresponde a un Ultralight (00 03)
        return bytesATR.size == 20 && bytesATR[13] == 0x00.toByte() && bytesATR[14] == 0x03.toByte()
    }

    fun leerPaginaISO14443ATag(pcscTag: PCSCTag<ISO14443ATag>, paginaALeer: Byte): ByteArray
    {
        return ejecutarComandoAPDUPorCanalYVerificarResultado(pcscTag.tarjeta.basicChannel, CommandAPDU(ISO14443ATag.darComandoLeerPagina(paginaALeer)))
    }

    fun escribirPaginaISO14443ATag(pcscTag: PCSCTag<ISO14443ATag>, paginaAEscribir: Byte, datos: ByteArray): ByteArray
    {
        return ejecutarComandoAPDUPorCanalYVerificarResultado(pcscTag.tarjeta.basicChannel, CommandAPDU(ISO14443ATag.darComandoEscribirPagina(paginaAEscribir, datos)))
    }

    fun tieneTagConectado(): Boolean
    {
        return terminal.isCardPresent
    }
}