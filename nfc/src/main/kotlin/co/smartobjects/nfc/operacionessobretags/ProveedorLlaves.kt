package co.smartobjects.nfc.operacionessobretags

import co.smartobjects.nfc.lectorestags.ILectorTag
import co.smartobjects.nfc.tags.ITag
import co.smartobjects.persistencia.clientes.llavesnfc.FiltroLlavesNFC
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import co.smartobjects.utilidades.hexAByteArray
import org.threeten.bp.ZonedDateTime
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


interface ProveedorLlaves
{
    companion object
    {
        fun crearProveedorDeLlaves(idCliente: Long, repositorioLlavesNFC: RepositorioLlavesNFC): ProveedorLlaves
        {
            return ProveedorLlavesImpl(idCliente, repositorioLlavesNFC)
        }
    }

    fun generarLlaveDeTagSegunUUIDyLlaveMaestra(lector: ILectorTag<ITag>): ByteArray
    val llaveMaestra: ByteArray
}

private class ProveedorLlavesImpl(private val idCliente: Long, private val repositorioLlavesNFC: RepositorioLlavesNFC)
    : ProveedorLlaves
{
    override fun generarLlaveDeTagSegunUUIDyLlaveMaestra(lector: ILectorTag<ITag>): ByteArray
    {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val numeroBits = lector.tag.darTamañoLlave() * java.lang.Byte.SIZE
        val spec =
                PBEKeySpec(
                        String(llaveMaestra, StandardCharsets.US_ASCII).toCharArray(),
                        lector.darUID(),
                        1000,
                        numeroBits
                          )

        val tmp = factory.generateSecret(spec)

        return tmp.encoded
    }

    @Volatile
    private var _llaveMaestra: ByteArray? = null

    override val llaveMaestra =
            _llaveMaestra
            ?: synchronized(this) {
                _llaveMaestra
                ?: run {
                    val parametros = FiltroLlavesNFC.ValidaEnFecha(ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO))
                    repositorioLlavesNFC.buscarSegunParametros(idCliente, parametros)?.run {
                        String(llave).hexAByteArray()
                    }?.also { _llaveMaestra = it }
                    ?: throw LlaveNFCMaestraNoEncontrada()
                }
            }
}

class LlaveNFCMaestraNoEncontrada : Exception("No existe llave maestra NFC del cliente")