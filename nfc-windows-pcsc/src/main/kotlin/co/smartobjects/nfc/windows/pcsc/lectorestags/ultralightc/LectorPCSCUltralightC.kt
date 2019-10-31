package co.smartobjects.nfc.windows.pcsc.lectorestags.ultralightc

import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.lectorestags.ultralightc.LectorUltralightC
import co.smartobjects.nfc.tags.ISO14443ATag
import co.smartobjects.nfc.tags.mifare.ultralight.UltralightC
import co.smartobjects.nfc.windows.pcsc.excepciones.ResultadoPN532Erroneo
import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.LectorPCSC
import co.smartobjects.nfc.windows.pcsc.lectores.pcsc.PCSCTag
import co.smartobjects.nfc.windows.pcsc.lectorestags.ILectorTagPCSC
import java.security.SecureRandom
import java.util.*
import javax.crypto.BadPaddingException
import javax.smartcardio.CardException
import javax.smartcardio.CommandAPDU


class LectorPCSCUltralightC<out Lector : LectorPCSC>
(
        override val lector: Lector,
        private var _tagPCSC: PCSCTag<UltralightC>
) : LectorUltralightC(), ILectorTagPCSC<Lector, UltralightC>
{
    override val tag: UltralightC
        get() = tagPCSC.tag

    override val tagPCSC: PCSCTag<UltralightC>
        get() = _tagPCSC

    override fun darUID(): ByteArray
    {
        return lector.ejecutarComandoAPDUPorCanalYVerificarResultado(
                tagPCSC.tarjeta.basicChannel,
                CommandAPDU(ISO14443ATag.COMANDO_LEER_UID)
                                                                    )
    }

    override fun leerPaginasEntre(paginaInicial: Byte, paginaFinal: Byte): ByteArray
    {
        val numeroPaginas = paginaFinal - paginaInicial + 1
        val datosLeidos = ByteArray(numeroPaginas * UltralightC.BYTES_POR_PAGINA.toInt())

        for (numeroPagina in 0 until numeroPaginas)
        {
            val pagina = (paginaInicial + numeroPagina).toByte()
            val datosPagina = lector.leerPaginaISO14443ATag(tagPCSC, pagina)
            System.arraycopy(datosPagina, 0, datosLeidos, numeroPagina * UltralightC.BYTES_POR_PAGINA.toInt(), datosPagina.size)
        }

        return datosLeidos
    }

    override fun autenticar(llave: ByteArray): Boolean
    {
        if (llave.size != 16)
        {
            throw NFCProtocolException("Se esperaba una llave de 16 bytes bytes para 2K3DES")
        }
        val llaveCompleta = Arrays.copyOf(llave, 24)
        System.arraycopy(llave, 0, llaveCompleta, 16, 8)
        val comandoIniciarAutenticacion = UltralightC.COMANDO_INICIAR_AUTENTICACION
        val retoDeTag = lector.ejecutarComandoNativo(tagPCSC.tarjeta, comandoIniciarAutenticacion)

        return try
        {
            val retoParaTag = ByteArray(8)
            SecureRandom().nextBytes(retoParaTag)
            val comandoRetoDeTagMasRetoParaTag = UltralightC.prepararRespuestaRetoAutenticacion(llaveCompleta, retoDeTag, retoParaTag)
            val resultadoRetoTag = lector.ejecutarComandoNativo(tagPCSC.tarjeta, comandoRetoDeTagMasRetoParaTag)
            return UltralightC.verificarRespuestaRetoTag(llaveCompleta, retoParaTag, comandoRetoDeTagMasRetoParaTag, resultadoRetoTag)
        }
        catch (e: ResultadoPN532Erroneo)
        {
            false
        }
        catch (e: BadPaddingException)
        {
            false
        }
    }

    override fun escribirPagina(paginaAEscribir: Byte, datos: ByteArray)
    {
        if (datos.size != UltralightC.BYTES_POR_PAGINA.toInt())
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
                nuevoTag == null            -> throw NFCProtocolException("Error conectando el tag")
                nuevoTag.tag is UltralightC -> _tagPCSC = nuevoTag as PCSCTag<UltralightC>
                else                        -> throw NFCProtocolException("Se esperaba un tag UltralightC")
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
        }
        catch (e: CardException)
        {
            throw NFCProtocolException("Error desconectando el tag", e)
        }
    }
}