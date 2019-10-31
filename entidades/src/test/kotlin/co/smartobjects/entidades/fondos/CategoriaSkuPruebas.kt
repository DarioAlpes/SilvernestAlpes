package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("CategoriaSku")
internal class CategoriaSkuPruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = CategoriaSku(
                1,
                null,
                "    Prueba    ",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                null,
                LinkedHashSet(),
                null
                                         )
        val entidadConTrim = CategoriaSku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                null,
                LinkedHashSet(),
                null
                                         )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = CategoriaSku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                null,
                LinkedHashSet(),
                null
                                  )
        assertEquals("Prueba", entidad.campoNombre.valor)
        assertEquals(true, entidad.campoDisponibleParaLaVenta.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = CategoriaSku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "erhw5uw5u",
                null,
                LinkedHashSet(),
                null
                                         )
        val entidadCopiada = entidadInicial.copiar(nombre = "Prueba editada", precioPorDefecto = Precio(Decimal.DIEZ, 4L), codigoExterno = "3wvy37w3b")
        val entidadEsperada = CategoriaSku(
                1,
                null,
                "Prueba editada",
                true,
                true,
                true,
                Precio(Decimal.DIEZ, 4L),
                "3wvy37w3b",
                null,
                LinkedHashSet(),
                null
                                          )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_cliente_funciona_correctamente()
    {
        val entidadInicial = CategoriaSku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                null,
                LinkedHashSet(),
                null
                                         )
        val entidadCopiada = entidadInicial.copiarConIdCliente(2)
        val entidadEsperada = CategoriaSku(
                2,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                null,
                LinkedHashSet(),
                null
                                          )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadInicial = CategoriaSku(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                null,
                LinkedHashSet(),
                null
                                         )
        val entidadCopiada = entidadInicial.copiarConId(47457)
        val entidadEsperada = CategoriaSku(
                1,
                47457,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                null,
                LinkedHashSet(),
                null
                                          )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            CategoriaSku(
                    1,
                    null,
                    "",
                    true,
                    true,
                    true,
                    Precio(Decimal.UNO, 1L),
                    "",
                    null,
                    LinkedHashSet(),
                    null
                        )
        }

        assertEquals(CategoriaSku.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Fondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            CategoriaSku(
                    1,
                    null,
                    "           ",
                    true,
                    true,
                    true,
                    Precio(Decimal.UNO, 1L),
                    "",
                    null,
                    LinkedHashSet(),
                    null
                        )
        }

        assertEquals(CategoriaSku.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Fondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }
}