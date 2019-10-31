package co.smartobjects.nfc.windows.pcsc.repositoriollavesnfc

import co.smartobjects.utilidades.aHexString
import co.smartobjects.utilidades.hexAByteArray
import com.sun.javaws.exceptions.InvalidArgumentException
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec


interface ProveedorLlaveCifradoLlavesNFC
{
    fun generarYGuardarLLaveMaestraNFCSiNoExiste()
    fun cifrarConLlaveDelKeystore(valorACifrar: String): LlaveMaestraNFCCifrada?
    fun descifrarConLlaveDelKeystore(valorCifrado: LlaveMaestraNFCCifrada): String?

    data class LlaveMaestraNFCCifrada(val llaveMaestraNFC: String, val iv: String)
    {
        companion object
        {
            private const val SEPARADOR = ":"

            fun desdeLlaveConIV(llaveConIV: String): LlaveMaestraNFCCifrada
            {
                if (llaveConIV.isEmpty())
                {
                    throw InvalidArgumentException(arrayOf("'llaveConIV' no puede estar vacío"))
                }

                val partes = llaveConIV.split(SEPARADOR)

                if (partes.size != 2)
                {
                    throw InvalidArgumentException(arrayOf("'llaveConIV' debe estar en el formato 'XXX...XXX${SEPARADOR}YYY...YYY'"))
                }

                return LlaveMaestraNFCCifrada(partes[0], partes[1])
            }
        }

        val llaveConIV = "$llaveMaestraNFC$SEPARADOR$iv"
    }
}


internal class ProveedorLlaveCifradoLlavesNFCConKeyStore
(
        private val proveedorDeKeystore: ProveedorDeKeystore
) : ProveedorLlaveCifradoLlavesNFC
{
    companion object
    {
        private const val AES_MODE = "AES/CBC/PKCS5Padding"
        private const val ALIAS_LLAVE = "llave_cifrar_llave_nfc"
    }

    private fun consultarLlaveDeKeystore(): Key?
    {
        return try
        {
            val contraseñaCharArray = proveedorDeKeystore.contraseñaKeystore.toCharArray()

            val keystore = proveedorDeKeystore.darKeystore()

            keystore.getKey(ALIAS_LLAVE, contraseñaCharArray)
        }
        catch (errorContraseña: UnrecoverableKeyException)
        {
            null
        }
    }

    override fun generarYGuardarLLaveMaestraNFCSiNoExiste()
    {
        val llaveMaestra = consultarLlaveDeKeystore()
        if (llaveMaestra == null)
        {
            val generador = KeyGenerator.getInstance("AES").also { it.init(128) }

            val llaveQueCifraLlaveMaestraNFC = generador.generateKey()

            val entradaLlave = KeyStore.SecretKeyEntry(llaveQueCifraLlaveMaestraNFC)

            val keystore = proveedorDeKeystore.darKeystore()
            val contraseñaCharArray = proveedorDeKeystore.contraseñaKeystore.toCharArray()

            keystore.setEntry(ALIAS_LLAVE, entradaLlave, KeyStore.PasswordProtection(contraseñaCharArray))
            proveedorDeKeystore.persistirKeystore()
        }
    }

    override fun cifrarConLlaveDelKeystore(valorACifrar: String): ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada?
    {
        val llaveMaestra = consultarLlaveDeKeystore()
        return if (llaveMaestra != null)
        {
            val cipher = Cipher.getInstance(AES_MODE).also { it.init(Cipher.ENCRYPT_MODE, llaveMaestra) }
            val iv = cipher.parameters.getParameterSpec(IvParameterSpec::class.java).iv
            val valorCifrado = cipher.doFinal(valorACifrar.toByteArray(StandardCharsets.UTF_8))

            ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada(valorCifrado.aHexString(), iv.aHexString())
        }
        else
        {
            null
        }
    }

    override fun descifrarConLlaveDelKeystore(valorCifrado: ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada): String?
    {
        val llaveMaestra = consultarLlaveDeKeystore()
        return if (llaveMaestra != null)
        {
            val cipher = Cipher.getInstance(AES_MODE).also {
                it.init(Cipher.DECRYPT_MODE, llaveMaestra, IvParameterSpec(valorCifrado.iv.hexAByteArray()))
            }

            String(cipher.doFinal(valorCifrado.llaveMaestraNFC.hexAByteArray()), StandardCharsets.UTF_8)
        }
        else
        {
            null
        }
    }
}