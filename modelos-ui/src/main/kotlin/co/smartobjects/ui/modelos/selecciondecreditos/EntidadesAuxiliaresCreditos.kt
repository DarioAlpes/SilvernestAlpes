package co.smartobjects.ui.modelos.selecciondecreditos

import co.smartobjects.entidades.fondos.*
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.utilidades.Decimal


sealed class ProductoFondo(
        val fondo: Fondo<*>,
        val precioCompleto: PrecioCompleto
                          )
{
    class SinCategoria : ProductoFondo
    {
        constructor(dinero: Dinero, precioCompleto: PrecioCompleto) : super(dinero, precioCompleto)

        constructor(entrada: Entrada, precioCompleto: PrecioCompleto) : super(entrada, precioCompleto)

        constructor(acceso: Acceso, precioCompleto: PrecioCompleto) : super(acceso, precioCompleto)
    }

    class ConCategoria private constructor(
            fondo: Fondo<*>,
            precioCompleto: PrecioCompleto,
            val categoriasPadres: LinkedHashSet<Long>)
        : ProductoFondo(fondo, precioCompleto)
    {
        constructor(categoriaSku: CategoriaSku, precioCompleto: PrecioCompleto)
                : this(categoriaSku, precioCompleto, categoriaSku.idsDeAncestros)

        constructor(sku: Sku, precioCompleto: PrecioCompleto, categoriasPadres: LinkedHashSet<Long>)
                : this(sku as Fondo<Sku>, precioCompleto, categoriasPadres)
    }
}

class ProductoPaquete(
        paquete: Paquete,
        val nombresFondosAsociados: List<String>,
        val preciosCompletosFondosIncluidos: List<PrecioCompleto>
                     )
{
    val id = paquete.id!!
    val codigoExternoPaquete = paquete.codigoExterno
    val nombre = paquete.nombre
    val idsFondosIncluidos: LinkedHashSet<Long> = linkedSetOf()
    val codigosExternosFondosIncluidos: List<String>
    val cantidadesFondosIncluidos: List<Decimal>

    init
    {
        val cantidadesMutable = mutableListOf<Decimal>()
        val codigosExternosFondosIncluidosMutable = mutableListOf<String>()
        for (fondoIncluido in paquete.fondosIncluidos)
        {
            idsFondosIncluidos.add(fondoIncluido.idFondo)
            codigosExternosFondosIncluidosMutable.add(fondoIncluido.codigoExterno)
            cantidadesMutable.add(fondoIncluido.cantidad)
        }
        codigosExternosFondosIncluidos = codigosExternosFondosIncluidosMutable
        cantidadesFondosIncluidos = cantidadesMutable
    }
}