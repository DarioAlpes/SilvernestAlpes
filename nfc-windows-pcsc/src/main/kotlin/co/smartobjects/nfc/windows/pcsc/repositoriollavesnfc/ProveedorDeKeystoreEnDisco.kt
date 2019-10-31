package co.smartobjects.nfc.windows.pcsc.repositoriollavesnfc

import co.smartobjects.nfc.windows.pcsc.GeneradorUIDMaquina
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.KeyStoreException


interface ProveedorDeKeystore
{
    val contraseñaKeystore: String

    suspend fun inicializar()
    suspend fun crearNuevoKeystore()

    fun persistirKeystore()
    fun darKeystore(): KeyStore
}

class ProveedorDeKeystoreEnDisco
(
        private val generadorUIDMaquina: GeneradorUIDMaquina
) : ProveedorDeKeystore
{
    companion object
    {
        const val RUTA_ARCHIVO_KEYSTORE = "llave_maestra_nfc.keystore"
        private const val CONTRASEÑA_POR_DEFECTO_KEYSTORE = "contraseña del keystore por defecto"
    }

    private lateinit var _contraseñaKeystore: String
    override val contraseñaKeystore: String
        get()
        {
            if (!estaKeystoreCargado())
            {
                throw IllegalStateException("El Keystore no ha sido inicializado")
            }

            return _contraseñaKeystore
        }
    private val keystoreActual: KeyStore = KeyStore.getInstance("JCEKS")


    override suspend fun inicializar()
    {
        if (!estaKeystoreCargado())
        {
            generarContraseñaKeystore()

            try
            {
                FileInputStream(RUTA_ARCHIVO_KEYSTORE).use {
                    keystoreActual.load(it, _contraseñaKeystore.toCharArray())
                }
            }
            catch (e: FileNotFoundException)
            {
                crearNuevoKeystore()
            }
        }
    }

    override fun darKeystore(): KeyStore
    {
        return if (estaKeystoreCargado())
        {
            keystoreActual
        }
        else
        {
            throw IllegalStateException("El Keystore no ha sido inicializado")
        }
    }

    override suspend fun crearNuevoKeystore()
    {
        val contraseñaComoArreglo = _contraseñaKeystore.toCharArray()

        keystoreActual.load(null, contraseñaComoArreglo)

        persistirKeystore()
    }

    private suspend fun generarContraseñaKeystore()
    {
        if (!this::_contraseñaKeystore.isInitialized)
        {
            _contraseñaKeystore = generadorUIDMaquina.generar() ?: CONTRASEÑA_POR_DEFECTO_KEYSTORE
        }
    }

    override fun persistirKeystore()
    {
        GlobalScope.launch(Dispatchers.IO) {
            FileOutputStream(RUTA_ARCHIVO_KEYSTORE).use {
                keystoreActual.store(it, _contraseñaKeystore.toCharArray())
            }
        }
    }

    private fun estaKeystoreCargado(): Boolean
    {
        return try
        {
            keystoreActual.size()
            true
        }
        catch (e: KeyStoreException)
        {
            false
        }
    }
}
