package co.smartobjects.nfc.windows.pcsc.lectores.pcsc.pn532

import co.smartobjects.nfc.windows.pcsc.excepciones.ResultadoPN532Erroneo
import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.LectorPCSC
import co.smartobjects.utilidades.aHexString
import java.util.*
import javax.smartcardio.CardTerminal
import javax.smartcardio.CommandAPDU

abstract class LectorPCSCSobrePN532(terminal: CardTerminal) : LectorPCSC(terminal)
{
    companion object
    {
        private val ENVOLTURA_IN_COMMUNICATE_THRU = byteArrayOf(0xD4.toByte(), 0x42.toByte())
        private val ENVOLTURA_RESULTADO_IN_COMMUNICATE_THRU = byteArrayOf(0xD5.toByte(), 0x43.toByte(), 0x00.toByte())
    }

    override fun desenvolverResultadoComandoNativo(resultado: ByteArray): ByteArray
    {
        val respuestaSinPassThrough = desenvolverPassThrough(resultado)
        ENVOLTURA_RESULTADO_IN_COMMUNICATE_THRU
            .indices
            .asSequence()
            .filter { respuestaSinPassThrough[it] != ENVOLTURA_RESULTADO_IN_COMMUNICATE_THRU[it] }
            .forEach { _ ->
                throw ResultadoPN532Erroneo("Resultado incorrecto, " +
                                            "debe empezar por ${ENVOLTURA_RESULTADO_IN_COMMUNICATE_THRU.aHexString(" ")}." +
                                            "Resultado obtenido: ${respuestaSinPassThrough.aHexString(" ")}")
            }

        val respuestaTag = ByteArray(respuestaSinPassThrough.size - ENVOLTURA_RESULTADO_IN_COMMUNICATE_THRU.size)
        if (respuestaTag.isNotEmpty())
        {
            System.arraycopy(respuestaSinPassThrough, ENVOLTURA_RESULTADO_IN_COMMUNICATE_THRU.size, respuestaTag, 0, respuestaTag.size)
        }

        return respuestaTag
    }

    override fun envolverComandoNativoEnAPDU(comandoNativo: ByteArray): CommandAPDU
    {
        val comandoInCommunicateThru = Arrays.copyOf(ENVOLTURA_IN_COMMUNICATE_THRU, ENVOLTURA_IN_COMMUNICATE_THRU.size + comandoNativo.size)
        System.arraycopy(comandoNativo, 0, comandoInCommunicateThru, ENVOLTURA_IN_COMMUNICATE_THRU.size, comandoNativo.size)
        return envolverPassThrough(comandoInCommunicateThru)
    }

    abstract fun envolverPassThrough(comandoInCommunicateThru: ByteArray): CommandAPDU

    abstract fun desenvolverPassThrough(resultado: ByteArray): ByteArray
}