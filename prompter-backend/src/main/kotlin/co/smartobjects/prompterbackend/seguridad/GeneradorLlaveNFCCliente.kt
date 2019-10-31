package co.smartobjects.prompterbackend.seguridad

import co.smartobjects.utilidades.aHexString
import java.security.SecureRandom


internal interface GeneradorLlaveNFCCliente
{
    fun generarLlave(): CharArray
}

internal class GeneradorLlaveNFCClienteImpl() : GeneradorLlaveNFCCliente
{
    companion object
    {
        private const val NUMERO_BYTES_EN_LLAVE = 128
    }

    private val secureRandom = SecureRandom.getInstanceStrong()

    override fun generarLlave(): CharArray
    {
        val numeroAleatorio = ByteArray(NUMERO_BYTES_EN_LLAVE).apply {
            secureRandom.nextBytes(this)
        }

        return numeroAleatorio.aHexString().toCharArray()
    }
}