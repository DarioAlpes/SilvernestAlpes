package co.smartobjects.logica.fondos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@DisplayName("CalculadorPuedeAgregarseSegunUnicidadEnMemoria")
internal class CalculadorPuedeAgregarseSegunUnicidadEnMemoriaPruebas
{
    private val fondoNoUnico = Dinero(1, 1, "Fondo 1 no es único", true, false, false, Precio(Decimal(111), 1), "")
    private val fondoUnico = Dinero(1, 2, "Fondo 2 es único", true, true, false, Precio(Decimal(222), 1), "")
    private val fondosDisponibles = sequenceOf(fondoNoUnico, fondoUnico)

    @Nested
    inner class PuedeAgregarFondos
    {
        @Nested
        inner class AlVerificarUnFondoNoUnico
        {
            @Test
            fun si_estan_vacios_los_fondos_del_carrito_puede_agregarlo()
            {
                val calculador = CalculadorPuedeAgregarseSegunUnicidadEnMemoria(fondosDisponibles)

                val resultado = calculador.puedeAgregarFondos(setOf(fondoNoUnico.id!!), setOf())

                assertTrue(resultado)
            }

            @Test
            fun si_el_carrito_ya_contiene_el_fondo_puede_agregarlo()
            {
                val calculador = CalculadorPuedeAgregarseSegunUnicidadEnMemoria(fondosDisponibles)

                val resultado = calculador.puedeAgregarFondos(setOf(fondoNoUnico.id!!), setOf(fondoNoUnico.id!!))

                assertTrue(resultado)
            }
        }

        @Nested
        inner class AlVerificarUnFondoUnico
        {
            @Test
            fun si_estan_vacios_los_fondos_del_carrito_puede_agregarlo()
            {
                val calculador = CalculadorPuedeAgregarseSegunUnicidadEnMemoria(fondosDisponibles)

                val resultado = calculador.puedeAgregarFondos(setOf(fondoUnico.id!!), setOf())

                assertTrue(resultado)
            }

            @Test
            fun si_el_carrito_ya_contiene_el_fondo_no_puede_agregarlo()
            {
                val calculador = CalculadorPuedeAgregarseSegunUnicidadEnMemoria(fondosDisponibles)

                val resultado = calculador.puedeAgregarFondos(setOf(fondoUnico.id!!), setOf(fondoUnico.id!!))

                assertFalse(resultado)
            }
        }
    }

    @Nested
    inner class AlgunoEsUnico
    {
        @Test
        fun retorna_true_si_algun_id_de_fondo_corresponde_a_un_fondo_unico()
        {
            val calculador = CalculadorPuedeAgregarseSegunUnicidadEnMemoria(fondosDisponibles)

            val resultado = calculador.algunoEsUnico(setOf(fondoUnico.id!!))

            assertTrue(resultado)
        }

        @Test
        fun retorna_false_si_algun_id_de_fondo_corresponde_a_un_fondo_no_unico()
        {
            val calculador = CalculadorPuedeAgregarseSegunUnicidadEnMemoria(fondosDisponibles)

            val resultado = calculador.algunoEsUnico(setOf(fondoNoUnico.id!!))

            assertFalse(resultado)
        }
    }
}