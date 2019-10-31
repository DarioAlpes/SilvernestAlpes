package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("Acceso")
internal class AccesoPruebas
{
    @Test
    fun hace_trim_a_nombre_correctamente()
    {
        val entidadSinTrim = Acceso(
                1,
                null,
                "    Prueba    ",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                0L
                                   )
        val entidadConTrim = Acceso(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                0L
                                   )
        assertEquals(entidadConTrim, entidadSinTrim)
    }

    @Test
    fun campos_quedan_con_valores_correctos()
    {
        val entidad = Acceso(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                0L
                            )
        assertEquals("Prueba", entidad.campoNombre.valor)
        assertEquals(true, entidad.campoDisponibleParaLaVenta.valor)
    }

    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Acceso(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "asdfasdfasdf",
                0L
                                   )
        val entidadCopiada = entidadInicial.copiar(nombre = "Prueba editada", precioPorDefecto = Precio(Decimal.DIEZ, 4L), codigoExterno = "45236236")
        val entidadEsperada = Acceso(
                1,
                null,
                "Prueba editada",
                true,
                true,
                true,
                Precio(Decimal.DIEZ, 4L),
                "45236236",
                0L
                                    )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_cliente_funciona_correctamente()
    {
        val entidadInicial = Acceso(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                0L
                                   )
        val entidadCopiada = entidadInicial.copiarConIdCliente(2)
        val entidadEsperada = Acceso(
                2,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                0L
                                    )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun copiar_con_id_funciona_correctamente()
    {
        val entidadInicial = Acceso(
                1,
                null,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                0L
                                   )
        val entidadCopiada = entidadInicial.copiarConId(56765)
        val entidadEsperada = Acceso(
                1,
                56765,
                "Prueba",
                true,
                true,
                true,
                Precio(Decimal.UNO, 1L),
                "",
                0L
                                    )
        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_vacio()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Acceso(
                    1,
                    null,
                    "",
                    true,
                    true,
                    true,
                    Precio(Decimal.UNO, 1L),
                    "",
                    0L
                  )
        }

        assertEquals(AccesoBase.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Fondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }

    @Test
    fun no_se_permite_instanciar_con_nombre_con_espacios_o_tabs()
    {
        val excepcion = assertThrows<EntidadConCampoVacio> {
            Acceso(
                    1,
                    null,
                    "           ",
                    true,
                    true,
                    true,
                    Precio(Decimal.UNO, 1L),
                    "",
                    0L
                  )
        }

        assertEquals(AccesoBase.Campos.NOMBRE, excepcion.nombreDelCampo)
        assertEquals(Fondo.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
    }
}