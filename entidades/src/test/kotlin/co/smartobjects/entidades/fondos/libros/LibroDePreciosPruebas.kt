package co.smartobjects.entidades.fondos.libros

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("LibroDePrecios")
internal class LibroDePreciosPruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val precio = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val entidadEsperada = LibroDePrecios(1, 2, "Nombre", setOf(precio))

        val entidadProcesada = LibroDePrecios(1, 2, "   \t  Nombre\t\t   ", setOf(precio))

        assertEquals(entidadEsperada, entidadProcesada)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val precio1 = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val precio2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val entidad = LibroDePrecios(1, 2, "Libro precios", setOf(precio1, precio2))

        assertEquals("Libro precios", entidad.campoNombre.valor)
        assertEquals(setOf(precio1, precio2), entidad.campoPrecios.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val precioInicial1 = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val precioInicial2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val entidadInicial = LibroDePrecios(1, 1, "Libro precios", setOf(precioInicial1, precioInicial2))

        val precioFinal1 = PrecioEnLibro(Precio(Decimal(20.455), 7L), 8L)
        val precioFinal2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val precioFinal3 = PrecioEnLibro(Precio(Decimal(33.45689), 5L), 6L)

        val entidadEsperada = LibroDePrecios(10, 20, "Libro precios copiado", setOf(precioFinal1, precioFinal2, precioFinal3))

        val entidadCopiada = entidadInicial.copiar(idCliente = 10, id = 20, nombre = "Libro precios copiado", precios = setOf(precioFinal1, precioFinal2, precioFinal3))

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val precioInicial1 = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val precioInicial2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val entidadInicial = LibroDePrecios(1, 1, "Libro precios", setOf(precioInicial1, precioInicial2))
        val entidadEsperada = LibroDePrecios(1, 456352, "Libro precios", setOf(precioInicial1, precioInicial2))

        val entidadCopiada = entidadInicial.copiarConId(456352)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun dos_entidades_son_iguales_cuando_el_orden_de_la_lista_de_precios_es_diferente()
    {
        val precio1 = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val precio2 = PrecioEnLibro(Precio(Decimal(13.45689), 3L), 4L)
        val precio3 = PrecioEnLibro(Precio(Decimal(14.45689), 5L), 6L)
        val entidades = listOf(
                LibroDePrecios(1, 1, "Libro precios", setOf(precio1, precio2, precio3)),
                LibroDePrecios(1, 1, "Libro precios", setOf(precio1, precio3, precio2)),
                LibroDePrecios(1, 1, "Libro precios", setOf(precio2, precio1, precio3)),
                LibroDePrecios(1, 1, "Libro precios", setOf(precio2, precio3, precio1)),
                LibroDePrecios(1, 1, "Libro precios", setOf(precio3, precio2, precio1)),
                LibroDePrecios(1, 1, "Libro precios", setOf(precio3, precio1, precio2))
                              )
        entidades.forEach { libro1 ->
            entidades.forEach { libro2 ->
                assertEquals(libro1, libro2)
            }
        }
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val precio = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            LibroDePrecios(1, 2, "", setOf(precio))
        })

        assertEquals(LibroDePrecios.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Libro.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_solo_espacios_o_tabs()
    {
        val precio = PrecioEnLibro(Precio(Decimal(12.45689), 1L), 2L)
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            LibroDePrecios(1, 2, "   \t\t   ", setOf(precio))
        })

        assertEquals(LibroDePrecios.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Libro.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_precios_vacio()
    {
        val excepcion = Assertions.assertThrows(EntidadConCampoVacio::class.java, {
            LibroDePrecios(1, 1, "Libro precios", setOf())
        })

        assertEquals(LibroDePrecios.Campos.PRECIOS, excepcion.nombreDelCampo)
        assertEquals(LibroDePrecios.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }
}