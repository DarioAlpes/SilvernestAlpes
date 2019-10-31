package co.smartobjects.entidades.fondos.libros

import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("PrecioEnLibro")
internal class PrecioEnLibroPruebas
{
    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val entidadCopiada = entidadInicial.copiar(precio = Precio(Decimal(20.455), 3L), idFondo = 4L)
        val entidadEsperada = PrecioEnLibro(Precio(Decimal(20.455), 3L), 4L)
        assertEquals(entidadEsperada, entidadCopiada)
    }
}