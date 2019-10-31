package co.smartobjects.entidades.fondos.libros

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("Prohibicion")
internal class ProhibicionPruebas
{
    @Test
    fun para_fondo_copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Prohibicion.DeFondo(1L)
        val entidadCopiada = entidadInicial.copiar(id = 4L)
        val entidadEsperada = Prohibicion.DeFondo(4L)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun para_paquete_copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Prohibicion.DePaquete(1L)
        val entidadCopiada = entidadInicial.copiar(id = 4L)
        val entidadEsperada = Prohibicion.DePaquete(4L)

        assertEquals(entidadEsperada, entidadCopiada)
    }
}