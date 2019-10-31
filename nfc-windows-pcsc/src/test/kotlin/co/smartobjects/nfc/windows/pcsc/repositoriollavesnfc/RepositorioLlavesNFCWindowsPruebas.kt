package co.smartobjects.nfc.windows.pcsc.repositoriollavesnfc

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.nfc.windows.pcsc.cualquiera
import co.smartobjects.nfc.windows.pcsc.eqParaKotlin
import co.smartobjects.nfc.windows.pcsc.mockConDefaultAnswer
import co.smartobjects.persistencia.clientes.llavesnfc.FiltroLlavesNFC
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.excepciones.ErrorDeConsultaEntidad
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import kotlin.test.assertEquals
import kotlin.test.assertNull


internal class RepositorioLlavesNFCWindowsPruebas
{
    companion object
    {
        private val LLAVE_A_CIFRAR = Cliente.LlaveNFC(1L, "a cifrar")
        private val LLAVE_CIFRADA = ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada("abc", "def")
        private const val LLAVE_DESCIFRADA = "23235235"
    }

    private val mockRepositorioLlavesNFC = mockConDefaultAnswer(RepositorioLlavesNFC::class.java).also {
        doReturn("No importa").`when`(it).nombreEntidad
    }


    @Nested
    inner class Crear
    {
        private val mockProveedorLlaveCifradoLlavesNFC =
                mockConDefaultAnswer(ProveedorLlaveCifradoLlavesNFC::class.java).also {
                    doNothing().`when`(it).generarYGuardarLLaveMaestraNFCSiNoExiste()
                    doReturn(LLAVE_CIFRADA).`when`(it).cifrarConLlaveDelKeystore(String(LLAVE_A_CIFRAR.llave))
                }

        private val repositorioLlavesNFCWindows = RepositorioLlavesNFCWindows(mockRepositorioLlavesNFC, mockProveedorLlaveCifradoLlavesNFC)


        @BeforeEach
        fun prepararMockDeCrear()
        {
            doReturn(Cliente.LlaveNFC(1L, LLAVE_CIFRADA.llaveConIV))
                .`when`(mockRepositorioLlavesNFC)
                .crear(ArgumentMatchers.anyLong(), cualquiera())
        }


        @Test
        fun se_genera_y_guarda_una_llave_nfc_maestra()
        {
            repositorioLlavesNFCWindows.crear(1L, LLAVE_A_CIFRAR)

            verify(mockProveedorLlaveCifradoLlavesNFC).generarYGuardarLLaveMaestraNFCSiNoExiste()
        }

        @Test
        fun se_guarda_llave_cifrada_en_la_base_de_datos()
        {
            val llaveAGuardar = Cliente.LlaveNFC(1L, LLAVE_CIFRADA.llaveConIV)

            repositorioLlavesNFCWindows.crear(1L, LLAVE_A_CIFRAR)

            verify(mockProveedorLlaveCifradoLlavesNFC).cifrarConLlaveDelKeystore(String(LLAVE_A_CIFRAR.llave))
            verify(mockRepositorioLlavesNFC).crear(1L, llaveAGuardar)
        }

        @Test
        fun si_no_se_pudo_cifrar_la_llave_lanza_excepcion_de_ErrorDeCreacionActualizacionEntidad()
        {
            doReturn(null)
                .`when`(mockProveedorLlaveCifradoLlavesNFC)
                .cifrarConLlaveDelKeystore(String(LLAVE_A_CIFRAR.llave))

            assertThrows<ErrorDeCreacionActualizacionEntidad> {
                repositorioLlavesNFCWindows.crear(1L, LLAVE_A_CIFRAR)
            }
        }
    }

    @Nested
    inner class BuscarSegunParametros
    {

        private val parametros = mockConDefaultAnswer(FiltroLlavesNFC::class.java)

        private val mockProveedorLlaveCifradoLlavesNFC =
                mockConDefaultAnswer(ProveedorLlaveCifradoLlavesNFC::class.java).also {
                    doReturn(LLAVE_DESCIFRADA).`when`(it).descifrarConLlaveDelKeystore(cualquiera())
                }

        private val repositorioLlavesNFCWindows = RepositorioLlavesNFCWindows(mockRepositorioLlavesNFC, mockProveedorLlaveCifradoLlavesNFC)


        @Test
        fun si_no_se_encuentra_la_llave_retorna_null()
        {
            doReturn(null)
                .`when`(mockRepositorioLlavesNFC)
                .buscarSegunParametros(ArgumentMatchers.anyLong(), eqParaKotlin(parametros))

            val encontrado = repositorioLlavesNFCWindows.buscarSegunParametros(1L, parametros)

            assertNull(encontrado)
        }

        @Test
        fun si_existe_la_llave_la_retorna_descifrada()
        {
            doReturn(Cliente.LlaveNFC(1L, "$LLAVE_DESCIFRADA:012824"))
                .`when`(mockRepositorioLlavesNFC)
                .buscarSegunParametros(ArgumentMatchers.anyLong(), eqParaKotlin(parametros))

            val llaveADescifrar = ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada(LLAVE_DESCIFRADA, "012824")
            val llaveEsperada = Cliente.LlaveNFC(1L, LLAVE_DESCIFRADA)

            val llaveEncontrada = repositorioLlavesNFCWindows.buscarSegunParametros(1L, parametros)

            verify(mockProveedorLlaveCifradoLlavesNFC).descifrarConLlaveDelKeystore(llaveADescifrar)
            assertEquals(llaveEsperada, llaveEncontrada)
        }

        @Test
        fun si_no_se_pudo_cifrar_la_llave_lanza_excepcion_de_ErrorDeCreacionActualizacionEntidad()
        {
            doReturn(Cliente.LlaveNFC(1L, "$LLAVE_DESCIFRADA:012824"))
                .`when`(mockRepositorioLlavesNFC)
                .buscarSegunParametros(ArgumentMatchers.anyLong(), eqParaKotlin(parametros))

            doReturn(null)
                .`when`(mockProveedorLlaveCifradoLlavesNFC)
                .descifrarConLlaveDelKeystore(cualquiera())

            assertThrows<ErrorDeConsultaEntidad> {
                repositorioLlavesNFCWindows.buscarSegunParametros(1L, parametros)
            }
        }
    }
}