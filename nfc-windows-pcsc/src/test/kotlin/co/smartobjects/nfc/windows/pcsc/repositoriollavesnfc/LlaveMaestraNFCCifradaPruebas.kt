package co.smartobjects.nfc.windows.pcsc.repositoriollavesnfc

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class LlaveMaestraNFCCifradaPruebas
{
    @Test
    fun llaveConIV_retorna_valor_esperado()
    {
        val valorEsperado = "abc:def"
        val entidadDePrueba = ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada("abc", "def")

        assertEquals(valorEsperado, entidadDePrueba.llaveConIV)
    }

    @Test
    fun desdeLlaveConIV_retorna_valor_esperado()
    {
        val valorEsperado = ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada("abc", "def")
        val entidadDePrueba = "abc:def"

        assertEquals(valorEsperado, ProveedorLlaveCifradoLlavesNFC.LlaveMaestraNFCCifrada.desdeLlaveConIV(entidadDePrueba))
    }
}