package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("Sku")
internal class SkuPruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = Sku(
                1,
                null,
                "    Prueba    ",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                1,
                null
                                )
        val entidadConTrim = Sku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                1,
                null
                                )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Sku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                1,
                null
                         )
        assertEquals("Prueba", entidad.campoNombre.valor)
        assertEquals(true, entidad.campoDisponibleParaLaVenta.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Sku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "eutirtrbtn",
                1,
                null
                                )
        val entidadCopiada = entidadInicial.copiar(nombre = "Prueba editada", precioPorDefecto = Precio(Decimal.DIEZ, 4L), codigoExterno = "344h4545")
        val entidadEsperada = Sku(
                1,
                null,
                "Prueba editada",
                true,
                true,
                true,
                Precio(Decimal.DIEZ, 4L),
                "344h4545",
                1,
                null
                                 )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_cliente_funciona_correctamente()
    {
        val entidadInicial = Sku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                1,
                null
                                )
        val entidadCopiada = entidadInicial.copiarConIdCliente(2)
        val entidadEsperada = Sku(
                2,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                1,
                null
                                 )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadInicial = Sku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                1,
                null
                                )
        val entidadEsperada = Sku(
                1,
                3579943,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                1,
                null
                                 )

        val entidadCopiada = entidadInicial.copiarConId(3579943)

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Sku(
                    1,
                    null,
                    "",
                    true,
                    true,
                    true,
                    Precio(Decimal.UNO, 1L),
                    "",
                    1,
                    null
               )
        }

        assertEquals(Sku.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Fondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Sku(
                    1,
                    null,
                    "           ",
                    true,
                    true,
                    true,
                    Precio(Decimal.UNO, 1L),
                    "",
                    1,
                    null
               )
        }

        assertEquals(Sku.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Fondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }
}