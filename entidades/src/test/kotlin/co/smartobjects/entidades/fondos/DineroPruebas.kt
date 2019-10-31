package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("Dinero")
internal class DineroPruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = Dinero(
                1,
                null,
                "    Prueba    ",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                ""
                                   )
        val entidadConTrim = Dinero(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                ""
                                   )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Dinero(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                ""
                            )
        assertEquals("Prueba", entidad.campoNombre.valor)
        assertEquals(true, entidad.campoDisponibleParaLaVenta.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Dinero(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "asdfasdf4t4"
                                   )
        val entidadCopiada = entidadInicial.copiar(nombre = "Prueba editada", precioPorDefecto = Precio(Decimal.DIEZ, 4L), codigoExterno = "35673626246")
        val entidadEsperada = Dinero(
                1,
                null,
                "Prueba editada",
                true,
                true,
                true,
                Precio(Decimal.DIEZ, 4L),
                "35673626246"
                                    )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_cliente_funciona_correctamente()
    {
        val entidadInicial = Dinero(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                ""
                                   )
        val entidadCopiada = entidadInicial.copiarConIdCliente(2)
        val entidadEsperada = Dinero(
                2,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                ""
                                    )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadInicial = Dinero(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                ""
                                   )
        val entidadCopiada = entidadInicial.copiarConId(3556)
        val entidadEsperada = Dinero(
                1,
                3556,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                ""
                                    )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Dinero(
                    1,
                    null,
                    "",
                    true,
                    true,
                    true,
                    Precio(Decimal.UNO, 1L),
                    ""
                  )
        }

        assertEquals(Dinero.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Fondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Dinero(
                    1,
                    null,
                    "           ",
                    true,
                    true,
                    true,
                    Precio(Decimal.UNO, 1L),
                    ""
                  )
        }

        assertEquals(Dinero.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Fondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }
}