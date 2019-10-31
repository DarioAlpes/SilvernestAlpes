package co.smartobjects.entidades.operativas.ordenes

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@DisplayName("Transaccion.Debito")
internal class TransaccionDebitoPruebas
{
    @Test
    fun copiar_funciona_correctamente_con_parametros_primitivos()
    {
        val entidadInicial = Transaccion.Debito(1, 2, "3", Decimal(4), 5, "6", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))

        val entidadEsperada = Transaccion.Debito(2, 3, "4", Decimal(5), 6, "7", ConsumibleEnPuntoDeVenta(8, 9, "código externo fondo nuevo"))

        val entidadCopiada = entidadInicial.copiar(2, 3, "4", Decimal(5), 6, "7", ConsumibleEnPuntoDeVenta(8, 9, "código externo fondo nuevo"))

        assertEquals(entidadEsperada, entidadCopiada)
    }

    @Nested
    inner class Instanciacion
    {
        @Test
        fun campos_quedan_con_valores_correctos()
        {
            val nombreUsuario = "3"
            val cantidad = Decimal(4)
            val idDispositivo = "6"
            val entidad = Transaccion.Debito(1, 2, nombreUsuario, cantidad, 5, idDispositivo, ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))

            assertEquals(nombreUsuario, entidad.campoNombreUsuario.valor)
            assertEquals(nombreUsuario, entidad.nombreUsuario)
            assertEquals(cantidad, entidad.campoCantidad.valor)
            assertEquals(cantidad, entidad.cantidad)
            assertEquals(idDispositivo, entidad.campoIdDispositivo.valor)
            assertEquals(idDispositivo, entidad.idDispositivo)
        }

        @Test
        fun hace_trim_a_nombre_usuario_correctamente()
        {
            val entidadSinTrim = Transaccion.Debito(1, 2, "  3   ", Decimal(4), 5, "6", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
            val entidadConTrim = Transaccion.Debito(1, 2, "3", Decimal(4), 5, "6", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Test
        fun hace_trim_a_id_de_dispositivo_correctamente()
        {
            val entidadSinTrim = Transaccion.Debito(1, 2, "3", Decimal(4), 5, "   6    ", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
            val entidadConTrim = Transaccion.Debito(1, 2, "3", Decimal(4), 5, "6", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
            assertEquals(entidadConTrim, entidadSinTrim)
        }

        @Nested
        inner class NoPermiteInstanciarCon
        {
            @Nested
            inner class Cantidad
            {
                @Test
                fun negativa()
                {
                    val excepcion = assertThrows<EntidadConCampoFueraDeRango> {
                        Transaccion.Debito(1, 2, "3", Decimal(-999), 5, "6", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
                    }

                    assertEquals(Transaccion.Campos.CANTIDAD, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                    assertEquals("0", excepcion.valorDelLimite)
                }
            }

            @Nested
            inner class NombreDeUsuario
            {
                @Test
                fun vacio()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Transaccion.Debito(1, 2, "", Decimal(4), 5, "6", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
                    }

                    assertEquals(Transaccion.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_espacios_o_tabs()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Transaccion.Debito(1, 2, "   \t  \t", Decimal(4), 5, "6", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
                    }

                    assertEquals(Transaccion.Campos.NOMBRE_USUARIO, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }

            @Nested
            inner class IdDeDispositivo
            {
                @Test
                fun vacio()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Transaccion.Debito(1, 2, "3", Decimal(4), 5, "", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
                    }

                    assertEquals(Transaccion.Campos.ID_DISPOSITIVO, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }

                @Test
                fun con_espacios_o_tabs()
                {
                    val excepcion = assertThrows<EntidadConCampoVacio> {
                        Transaccion.Debito(1, 2, "3", Decimal(4), 5, "  \t  \t", ConsumibleEnPuntoDeVenta(7, 8, "código externo fondo"))
                    }

                    assertEquals(Transaccion.Campos.ID_DISPOSITIVO, excepcion.nombreDelCampo)
                    assertEquals(Transaccion.NOMBRE_ENTIDAD, excepcion.nombreEntidad)
                }
            }
        }
    }
}