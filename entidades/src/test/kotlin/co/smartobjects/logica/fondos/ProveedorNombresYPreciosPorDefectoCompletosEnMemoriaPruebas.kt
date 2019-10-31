package co.smartobjects.logica.fondos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.utilidades.Decimal
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


@DisplayName("ProveedorNombresYPreciosPorDefectoCompletosEnMemoria")
internal class ProveedorNombresYPreciosPorDefectoCompletosEnMemoriaPruebas
{
    private val fondosDisponibles =
            sequenceOf(
                    Dinero(1, 1, "Fondo id 1", true, false, false, Precio(Decimal(111), 1), ""),
                    Dinero(1, 2, "Fondo id 2", true, false, false, Precio(Decimal(222), 1), "")
                      )

    private val impuestosDisponibles = sequenceOf(Impuesto(1, 1, "Impuesto", Decimal(10)))


    private val proveedorEnPrueba = ProveedorNombresYPreciosPorDefectoPorDefectoCompletosEnMemoria(fondosDisponibles, impuestosDisponibles)

    @Nested
    inner class DarNombresFondosSegunIds
    {
        @Test
        fun si_no_existen_fondos_con_los_ids_buscados_retorna_lista_vacia()
        {
            val idsBuscadosInexistentes = LinkedHashSet(fondosDisponibles.map { it.id!! + 456789 }.toList())

            val nombresEncontrados = proveedorEnPrueba.darNombresFondosSegunIds(idsBuscadosInexistentes)

            assertEquals(emptyList(), nombresEncontrados)
        }

        @Test
        fun si_existen_fondos_con_los_ids_buscados_retorna_lista_correcta()
        {
            val idsBuscadosExistentes = LinkedHashSet(fondosDisponibles.map { it.id!! }.toList())

            val nombresEncontrados = proveedorEnPrueba.darNombresFondosSegunIds(idsBuscadosExistentes)

            assertEquals(fondosDisponibles.map { it.nombre }.toList(), nombresEncontrados)
        }
    }

    @Nested
    inner class CompletarPreciosFondos
    {
        @Test
        fun si_no_existen_fondos_con_los_ids_buscados_retorna_lista_vacia()
        {
            val idsBuscadosInexistentes = LinkedHashSet(fondosDisponibles.map { it.id!! + 456789 }.toList())

            val nombresEncontrados = proveedorEnPrueba.completarPreciosFondos(idsBuscadosInexistentes)

            assertEquals(emptyList(), nombresEncontrados)
        }

        @Test
        fun si_existen_fondos_con_los_ids_buscados_retorna_lista_correcta()
        {
            val idsBuscadosExistentes = LinkedHashSet(fondosDisponibles.map { it.id!! }.toList())

            val preciosEsperados =
                    fondosDisponibles.map {
                        PrecioCompleto(it.precioPorDefecto, ImpuestoSoloTasa(impuestosDisponibles.first()))
                    }.toList()

            val nombresEncontrados = proveedorEnPrueba.completarPreciosFondos(idsBuscadosExistentes)

            assertEquals(preciosEsperados, nombresEncontrados)
        }
    }
}