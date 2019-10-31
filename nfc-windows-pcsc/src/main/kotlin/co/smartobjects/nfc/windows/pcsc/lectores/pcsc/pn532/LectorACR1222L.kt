package co.smartobjects.nfc.windows.pcsc.lectores.pcsc.pn532

import java.util.*
import javax.smartcardio.CardTerminal
import javax.smartcardio.CommandAPDU

class LectorACR1222L(terminal: CardTerminal) : LectorPCSCSobrePN532(terminal)
{
    companion object
    {
        private val ENVOLTURA_PASS_THROUGH = byteArrayOf(0xE0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x24.toByte(), 0x00.toByte())
        private const val TAMAÑO_ENVOLTURA_RESPUESTA = 5
    }

    override fun desenvolverPassThrough(resultado: ByteArray): ByteArray
    {
        val respuestaTag = ByteArray(resultado.size - TAMAÑO_ENVOLTURA_RESPUESTA)
        if (respuestaTag.isNotEmpty())
        {
            System.arraycopy(resultado, TAMAÑO_ENVOLTURA_RESPUESTA, respuestaTag, 0, respuestaTag.size)
        }
        return respuestaTag
    }

    override fun envolverPassThrough(comandoInCommunicateThru: ByteArray): CommandAPDU
    {
        val comandoEnvuelto = Arrays.copyOf(ENVOLTURA_PASS_THROUGH, ENVOLTURA_PASS_THROUGH.size + comandoInCommunicateThru.size)
        System.arraycopy(comandoInCommunicateThru, 0, comandoEnvuelto, ENVOLTURA_PASS_THROUGH.size, comandoInCommunicateThru.size)
        comandoEnvuelto[4] = (comandoInCommunicateThru.size and 0xFF).toByte()
        return CommandAPDU(comandoEnvuelto)
    }
}