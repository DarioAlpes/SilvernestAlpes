package co.smartobjects.logica.fondos

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


internal class ProveedorCodigosExternosFondosEnMemoriaPruebas
{
    private val fondosDisponibles =
            sequenceOf(
                    Dinero(1, 1, "Fondo id 1", true, false, false, Precio(Decimal(111), 1), ""),
                    CategoriaSku(1, 1, "Asfasdf", true, false, false, Precio(Decimal.UNO, 1), "", 1, linkedSetOf(58, 9, 7, 56, 444), null)
                      )

    private val proveedorEnPrueba = ProveedorCodigosExternosFondosEnMemoria(fondosDisponibles)

    @Test
    fun si_no_existen_fondos_con_el_id_buscado_retorna_null()
    {
        assertNull(proveedorEnPrueba.darCodigoExterno(78979L))
    }

    @Test
    fun si_existe_un_fondo_con_el_id_buscado_retorna_codigo_externo_correcto()
    {
        assertEquals(fondosDisponibles.first().codigoExterno, proveedorEnPrueba.darCodigoExterno(fondosDisponibles.first().id!!))
        assertEquals(fondosDisponibles.last().codigoExterno, proveedorEnPrueba.darCodigoExterno(fondosDisponibles.last().id!!))
    }
}