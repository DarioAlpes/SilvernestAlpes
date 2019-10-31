package co.smartobjects.nfc.windows.pcsc.repositoriollavesnfc

import co.smartobjects.nfc.windows.pcsc.mockConDefaultAnswer
import co.smartobjects.utilidades.aHexString
import co.smartobjects.utilidades.hexAByteArray
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class ProveedorLlaveCifradoLlavesNFCConKeyStorePruebas
{
    companion object
    {
        private const val CONTRASEÑA_KEYSTORE = "asdf123"
    }

    private var keystore = KeyStore.getInstance("JCEKS")


    @BeforeEach
    fun inicializarKeystoreDePrueba()
    {
        keystore = KeyStore.getInstance("JCEKS")
        keystore.load(null, CONTRASEÑA_KEYSTORE.toCharArray())
    }


    @Test
    fun se_genera_una_llave_maestra_nueva_si_no_existe_y_persiste_el_keystore()
    {
        val mockProveedorDeKeystore = mockConDefaultAnswer(ProveedorDeKeystore::class.java).also {
            doReturn(CONTRASEÑA_KEYSTORE).`when`(it).contraseñaKeystore
            doReturn(keystore).`when`(it).darKeystore()
            doNothing().`when`(it).persistirKeystore()
        }

        val proveedorEnPrueba = ProveedorLlaveCifradoLlavesNFCConKeyStore(mockProveedorDeKeystore)

        proveedorEnPrueba.generarYGuardarLLaveMaestraNFCSiNoExiste()

        assertTrue(keystore.containsAlias("llave_cifrar_llave_nfc"))
        verify(mockProveedorDeKeystore).persistirKeystore()
    }

    @Nested
    inner class CifradoYDescifrado
    {
        private lateinit var mockProveedorDeKeystore: ProveedorDeKeystore

        @BeforeEach
        fun prepararProveedorDeKeystore()
        {
            val llaveGenerada = KeyGenerator.getInstance("AES").run { init(128); this }.generateKey()

            keystore.setEntry(
                    "llave_cifrar_llave_nfc",
                    KeyStore.SecretKeyEntry(llaveGenerada),
                    KeyStore.PasswordProtection(CONTRASEÑA_KEYSTORE.toCharArray())
                             )

            mockProveedorDeKeystore = mockConDefaultAnswer(ProveedorDeKeystore::class.java).also {
                doReturn(CONTRASEÑA_KEYSTORE).`when`(it).contraseñaKeystore
                doReturn(keystore).`when`(it).darKeystore()
            }
        }


        private fun darLlaveEnKeystore(): Key
        {
            return keystore.getKey("llave_cifrar_llave_nfc", CONTRASEÑA_KEYSTORE.toCharArray())!!
        }


        @Test
        fun la_llave_nfc_maestra_se_puede_cifrar_con_AES_usando_la_almacenada_en_el_keystore()
        {
            val llaveCifrado = darLlaveEnKeystore()

            val llaveACifrar = "prueba123456"
            val proveedorEnPrueba = ProveedorLlaveCifradoLlavesNFCConKeyStore(mockProveedorDeKeystore)


            val llavCifradaObtenida = proveedorEnPrueba.cifrarConLlaveDelKeystore(llaveACifrar)!!


            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").also {
                it.init(Cipher.DECRYPT_MODE, llaveCifrado, IvParameterSpec(llavCifradaObtenida.iv.hexAByteArray()))
            }
            val llaveEsperada = String(cipher.doFinal(llavCifradaObtenida.llaveMaestraNFC.hexAByteArray()), StandardCharsets.UTF_8)

            assertEquals(llaveEsperada, llaveACifrar)
        }

        @Test
        fun la_llave_nfc_maestra_se_puede_descifrar_con_AES_usando_la_almacenada_en_el_keystore()
        {
            val llaveCifrado = darLlaveEnKeystore()

            val llaveACifrar = "prueba123456"

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").also { it.init(Cipher.ENCRYPT_MODE, llaveCifrado) }
            val iv = cipher.parameters.getParameterSpec(IvParameterSpec::class.java).iv
            val valorCifrado = cipher.doFinal(llaveACifrar.toByteArray(StandardCharsets.UTF_8))

            val valorADescifrar = ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada(valorCifrado.aHexString(), iv.aHexString())


            val proveedorEnPrueba = ProveedorLlaveCifradoLlavesNFCConKeyStore(mockProveedorDeKeystore)
            val llavCifradaObtenida = proveedorEnPrueba.descifrarConLlaveDelKeystore(valorADescifrar)!!


            assertEquals(llaveACifrar, llavCifradaObtenida)
        }
    }
}