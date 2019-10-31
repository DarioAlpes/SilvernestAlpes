package co.smartobjects.nfc.windows.pcsc.lectorestags.ultralightev1

import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.lectorestags.ultralightev1.LectorUltralightEV1
import co.smartobjects.nfc.tags.ISO14443ATag
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightEV1
import co.smartobjects.nfc.windows.pcsc.excepciones.ResultadoPN532Erroneo
import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.LectorPCSC
import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.PCSCTag
import co.smartobjects.nfc.windows.pcsc.lectorestags.ILectorTagPCSC
import java.util.*
import javax.smartcardio.CardException
import javax.smartcardio.CommandAPDU

class LectorPCSCUltralightEV1<out Lector : LectorPCSC>
(
        override val lector: Lector,
        private var _tagPCSC: PCSCTag<UltralightEV1>
) : LectorUltralightEV1(), ILectorTagPCSC<Lector, UltralightEV1>
{
    private var conectado = true
    override val tag: UltralightEV1
        get() = tagPCSC.tag

    override val tagPCSC: PCSCTag<UltralightEV1>
        get() = _tagPCSC

    override fun darUID(): ByteArray
    {
        return lector.ejecutarComandoAPDUPorCanalYVerificarResultado(tagPCSC.tarjeta.basicChannel, CommandAPDU(ISO14443ATag.COMANDO_LEER_UID))
    }

    override fun leerPaginasEntre(paginaInicial: Byte, paginaFinal: Byte): ByteArray
    {
        return lector.ejecutarComandoNativo(tagPCSC.tarjeta, UltralightEV1.darComandoLeerPaginasEntre(paginaInicial, paginaFinal))
    }

    override fun autenticar(contraseña: ByteArray, pack: ByteArray): Boolean
    {
        val comandoAutenticar = UltralightEV1.darComandoAutenticarTag(contraseña)
        return try
        {
            val resultado = lector.ejecutarComandoNativo(tagPCSC.tarjeta, comandoAutenticar)
            Arrays.equals(pack, resultado)
        }
        catch (e: ResultadoPN532Erroneo)
        {
            false
        }
    }

    override fun escribirPagina(paginaAEscribir: Byte, datos: ByteArray)
    {
        if (datos.size != UltralightEV1.BYTES_POR_PAGINA.toInt())
        {
            throw NFCProtocolException("Los datos a escribir deben ser de tamaño 4")
        }
        lector.escribirPaginaISO14443ATag(tagPCSC, paginaAEscribir, datos)
    }

    override fun conectar()
    {

        try
        {
            val nuevoTag = lector.esperarTag(1)
            when
            {
                nuevoTag == null              -> throw NFCProtocolException("Error conectando el tag")
                nuevoTag.tag is UltralightEV1 -> _tagPCSC = nuevoTag as PCSCTag<UltralightEV1>
                else                          -> throw NFCProtocolException("Se esperaba un tag UltralightEV1")
            }
        }
        catch (e: CardException)
        {
            throw NFCProtocolException("Error conectando el tag", e)
        }
    }

    override fun desconectar()
    {
        try
        {
            tagPCSC.tarjeta.disconnect(true)
            conectado = false
        }
        catch (e: CardException)
        {
            throw NFCProtocolException("Error desconectando el tag", e)
        }
    }
}