package co.smartobjects.logica.fondos

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.PrecioCompleto


interface ProveedorNombresYPreciosPorDefectoCompletosFondos
{
    fun darNombresFondosSegunIds(idsFondos: LinkedHashSet<Long>): List<String>
    fun darNombreFondoSegunId(idFondo: Long): String?
    {
        return darNombresFondosSegunIds(linkedSetOf(idFondo)).firstOrNull()
    }

    fun completarPreciosFondos(idsFondos: LinkedHashSet<Long>): List<PrecioCompleto>
    fun completarPrecioFondo(idFondo: Long): PrecioCompleto?
    {
        return completarPreciosFondos(linkedSetOf(idFondo)).firstOrNull()
    }
}

class ProveedorNombresYPreciosPorDefectoPorDefectoCompletosEnMemoria
(
        fondos: Sequence<Fondo<*>>,
        private val impuestos: Sequence<Impuesto>
) : ProveedorNombresYPreciosPorDefectoCompletosFondos
{
    private val indiceDeBusqueda = fondos.associateBy { it.id!! }

    override fun completarPreciosFondos(idsFondos: LinkedHashSet<Long>): List<PrecioCompleto>
    {
        val preciosPertinentes = idsFondos.mapNotNull { indiceDeBusqueda[it]?.precioPorDefecto }

        val idsImpuestos = preciosPertinentes.asSequence().map { it.idImpuesto }.distinct().toSet()

        val impuestosPertinentes = impuestos.filter { it.id in idsImpuestos }

        return preciosPertinentes.asSequence().map { precio: Precio ->
            PrecioCompleto(precio, ImpuestoSoloTasa(impuestosPertinentes.first { it.id == precio.idImpuesto }))
        }.toList()
    }

    override fun darNombresFondosSegunIds(idsFondos: LinkedHashSet<Long>): List<String>
    {
        return idsFondos.mapNotNull { indiceDeBusqueda[it]?.nombre }
    }
}