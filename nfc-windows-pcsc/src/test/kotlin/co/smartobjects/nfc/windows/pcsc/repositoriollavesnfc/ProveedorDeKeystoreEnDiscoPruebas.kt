package co.smartobjects.nfc.windows.pcsc.repositoriollavesnfc

import co.smartobjects.nfc.windows.pcsc.GeneradorUIDMaquina
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.KeyGenerator
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class ProveedorDeKeystoreEnDiscoPruebas
{
    companion object
    {
        @Suppress("unused")
        @[JvmStatic BeforeAll]
        fun eliminarKeystoreCreado()
        {
            File(ProveedorDeKeystoreEnDisco.RUTA_ARCHIVO_KEYSTORE).delete()
            Thread.sleep(50)
        }
    }

    private val mockGeneradorUIDMaquina: GeneradorUIDMaquina =
            object : GeneradorUIDMaquina
            {
                override suspend fun generar() = "123"
            }

    @Nested
    inner class SinInicializarProveedor
    {
        @Test
        fun al_consultar_la_contraseña_lanza_excepcion()
        {
            val proveedor = ProveedorDeKeystoreEnDisco(mockGeneradorUIDMaquina)

            assertThrows<IllegalStateException> {
                proveedor.contraseñaKeystore
            }
        }

        @Test
        fun al_pedir_keystore_lanza_excepcion()
        {
            val proveedor = ProveedorDeKeystoreEnDisco(mockGeneradorUIDMaquina)

            assertThrows<IllegalStateException> {
                proveedor.darKeystore()
            }
        }
    }

    @Nested
    inner class AlInicializar
    {
        private val archivoKeystore = File(ProveedorDeKeystoreEnDisco.RUTA_ARCHIVO_KEYSTORE)
        private val proveedor = ProveedorDeKeystoreEnDisco(mockGeneradorUIDMaquina)

        @AfterEach
        fun eliminarKeystoreCreado()
        {
            archivoKeystore.delete()
            Thread.sleep(50)
        }


        @Test
        fun si_no_existe_el_archivo_lo_crea()
        {
            assertFalse(archivoKeystore.exists())

            runBlocking { proveedor.inicializar() }

            Thread.sleep(80)

            assertTrue(archivoKeystore.exists())
        }

        @Test
        fun si_el_archivo_existe_no_crea_uno_nuevo_y_carga_el_existente()
        {
            assertFalse(archivoKeystore.exists())

            val contraseñaKeystore = "123".toCharArray()

            FileOutputStream(archivoKeystore).use {
                KeyStore.getInstance("JCEKS").run {
                    load(null, contraseñaKeystore)
                    this
                }.store(it, contraseñaKeystore)
            }

            assertTrue(archivoKeystore.exists())

            val timestampModificacion = archivoKeystore.lastModified()

            runBlocking { proveedor.inicializar() }

            assertEquals(timestampModificacion, archivoKeystore.lastModified())
        }
    }

    @Nested
    inner class ProveedorInicializado
    {
        @AfterEach
        fun eliminarKeystoreCreado()
        {
            File(ProveedorDeKeystoreEnDisco.RUTA_ARCHIVO_KEYSTORE).delete()
            Thread.sleep(50)
        }

        @Nested
        inner class ConUIDMaqinaNoGenerado
        {
            private val proveedor = ProveedorDeKeystoreEnDisco(object : GeneradorUIDMaquina
                                                               {
                                                                   override suspend fun generar() = null
                                                               })

            @BeforeEach
            fun inicializar()
            {
                runBlocking {
                    proveedor.inicializar()
                    Thread.sleep(50)
                }
            }

            @Test
            fun al_consultar_la_contraseña_keystore_retorna_entidad_correcta()
            {
                assertEquals("contraseña del keystore por defecto", proveedor.contraseñaKeystore)
            }

        }

        @Nested
        inner class ConUIDMaqinaGeneradoCorrectamente
        {
            private val proveedor = ProveedorDeKeystoreEnDisco(mockGeneradorUIDMaquina)

            @BeforeEach
            fun inicializar()
            {
                runBlocking {
                    proveedor.inicializar()
                    Thread.sleep(50)
                }
            }

            @Test
            fun al_consultar_la_contraseña_keystore_retorna_entidad_correcta()
            {
                assertEquals(mockGeneradorUIDMaquina.generarBloqueante(), proveedor.contraseñaKeystore)
            }

            @Test
            fun al_pedir_keystore_no_lanza_excepcion()
            {
                proveedor.darKeystore()
            }

            @Test
            fun al_persistir_el_keystore_se_escribe_el_mismo_que_esta_en_memoria()
            {
                val archivoKeystore = File(ProveedorDeKeystoreEnDisco.RUTA_ARCHIVO_KEYSTORE)
                val keystoreActual = proveedor.darKeystore()
                val llaveGenerada = KeyGenerator.getInstance("AES").run { init(128); this }.generateKey()

                val contraseñaKeystore = mockGeneradorUIDMaquina.generarBloqueante()!!.toCharArray()
                keystoreActual.setEntry(
                        "llave_cifrar_llave_nfc",
                        KeyStore.SecretKeyEntry(llaveGenerada),
                        KeyStore.PasswordProtection(contraseñaKeystore)
                                       )

                val llaveActual = keystoreActual.getKey("llave_cifrar_llave_nfc", contraseñaKeystore)!!

                archivoKeystore.delete()


                proveedor.persistirKeystore()

                Thread.sleep(80)

                val keystoreLeido = KeyStore.getInstance("JCEKS").also { it.load(null, contraseñaKeystore) }
                FileInputStream(archivoKeystore).use {
                    keystoreLeido.load(it, contraseñaKeystore)
                }

                val llaveLeida = keystoreActual.getKey("llave_cifrar_llave_nfc", contraseñaKeystore)!!

                assertEquals(llaveActual, llaveLeida)
            }
        }
    }
}