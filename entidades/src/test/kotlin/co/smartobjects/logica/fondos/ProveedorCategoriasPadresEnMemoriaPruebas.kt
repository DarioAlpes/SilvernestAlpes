package co.smartobjects.logica.fondos

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


@DisplayName("ProveedorNombresYPreciosPorDefectoCompletosEnMemoria")
internal class ProveedorCategoriasPadresEnMemoriaPruebas
{
    private val categoriasDisponibles =
            sequenceOf(
                    CategoriaSku(
                            1,
                            1,
                            "Asfasdf",
                            true,
                            false,
                            false,
                            Precio(Decimal.UNO, 1),
                            "",
                            1,
                            linkedSetOf(58, 9, 7, 56, 444),
                            null
                                )
                      )

    private val proveedorEnPrueba = ProveedorCategoriasPadresEnMemoria(categoriasDisponibles)

    @Test
    fun si_no_existen_fondos_con_los_ids_buscados_retorna_null()
    {
        val nombresEncontrados = proveedorEnPrueba.darPadres(78979L)

        assertNull(nombresEncontrados)
    }

    @Test
    fun si_existen_fondos_con_los_ids_buscados_retorna_lista_correcta()
    {
        val nombresEncontrados = proveedorEnPrueba.darPadres(categoriasDisponibles.first().id!!)

        assertEquals(categoriasDisponibles.first().idsDeAncestros, nombresEncontrados)
    }
}