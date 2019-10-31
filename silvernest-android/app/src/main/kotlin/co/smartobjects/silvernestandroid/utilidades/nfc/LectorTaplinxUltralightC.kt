package co.smartobjects.silvernestandroid.utilidades.nfc

import android.util.Log
import co.smartobjects.nfc.excepciones.NFCProtocolException
import co.smartobjects.nfc.lectorestags.ultralightc.LectorUltralightC
import co.smartobjects.silvernestandroid.AplicacionPrincipal
import com.nxp.nfclib.defaultimpl.KeyData
import com.nxp.nfclib.exceptions.NxpNfcLibException
import com.nxp.nfclib.ultralight.UltralightC
import javax.crypto.spec.SecretKeySpec


typealias UltralightCInterna = co.smartobjects.nfc.tags.mifare.ultralight.UltralightC


internal class LectorTaplinxUltralightC(private val tagUltralightC: UltralightC) : LectorUltralightC()
{
    companion object
    {
        private const val MENSAJE_FALLO_AUTENTICACION = "Transceive failed"
    }

    override val tag: UltralightCInterna
        get() = UltralightCInterna()

    override fun darUID(): ByteArray
    {
        return tagUltralightC.uid
    }

    override fun autenticar(llave: ByteArray): Boolean
    {
        try
        {
            val llaveComoKeyData = KeyData()
            llaveComoKeyData.setKey(SecretKeySpec(llave, "DESede"))
            tagUltralightC.authenticate(llaveComoKeyData)
            return true
        }
        catch (e: NxpNfcLibException)
        {
            if (e.message.equals(MENSAJE_FALLO_AUTENTICACION))
            {
                return false
            }
            throw NFCProtocolException("Error en el método autenticar", e)
        }
        catch (e: Exception)
        {
            throw NFCProtocolException("Error en el método autenticar", e)
        }
    }

    override fun escribirPagina(paginaAEscribir: Byte, datos: ByteArray)
    {
        tagUltralightC.write(paginaAEscribir.toInt(), datos)
    }

    override fun leerPaginasEntre(paginaInicial: Byte, paginaFinal: Byte): ByteArray
    {
        val todasLasPaginas = tagUltralightC.readAll()
        val arregloDatos = ByteArray(UltralightCInterna.BYTES_POR_PAGINA * (paginaFinal - paginaInicial + 1))
        System.arraycopy(todasLasPaginas, paginaInicial * UltralightCInterna.BYTES_POR_PAGINA.toInt(), arregloDatos, 0, arregloDatos.size)
        return arregloDatos
    }

    override fun desconectar()
    {
        try
        {
            tagUltralightC.reader.close()
        }
        catch (e: Exception)
        {
            Log.e(AplicacionPrincipal.TAG, "", e)
        }
    }

    override fun conectar()
    {
        try
        {
            tagUltralightC.reader.connect()
            tagUltralightC.reader.timeout = 200
        }
        catch (e: Exception)
        {
            Log.e(AplicacionPrincipal.TAG, "", e)
        }
    }
}