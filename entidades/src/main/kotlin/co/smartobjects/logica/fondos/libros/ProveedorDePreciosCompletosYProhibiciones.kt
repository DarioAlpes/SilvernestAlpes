package co.smartobjects.logica.fondos.libros

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.fondos.libros.*
import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.PrecioCompleto


interface ProveedorDePreciosCompletosYProhibiciones
{
    fun darPreciosCompletosDeFondos(
            idsFondos: LinkedHashSet<Long>,
            idUbicacion: Long?,
            idGrupoDeCliente: Long?,
            idPaquete: Long?): List<PrecioCompleto>

    fun verificarSiFondoEsVendible(
            idFondoAVerificar: Long,
            idUbicacion: Long?,
            idGrupoDeCliente: Long?,
            idsPaquetesEnCarrito: Set<Long>): Boolean

    fun verificarSiPaqueteEsVendible(
            idPaqueteAVerificar: Long,
            idUbicacion: Long?,
            idGrupoDeCliente: Long?,
            idsFondosEnCarrito: Set<Long>,
            idsPaquetesEnCarrito: Set<Long>): Boolean
}

interface BuscadorReglasDePreciosAplicables
{
    fun buscarReglasQueDeterminanPrecio(idFondo: Long, idUbicacion: Long?, idGrupoDeCliente: Long?, idPaquete: Long?)
            : Set<Regla<*>>
}

class ProveedorDePreciosCompletosYProhibicionesEnMemoria
(
        librosSegunReglasCompletos: Sequence<LibroSegunReglasCompleto<*>>,
        fondos: Sequence<Fondo<*>>,
        impuestos: Sequence<Impuesto>
) : ProveedorDePreciosCompletosYProhibiciones,
    BuscadorReglasDePreciosAplicables
{
    private var librosDePrecios: List<LibroSegunReglasCompleto<LibroDePrecios>> = listOf()
    private var librosDeProhibiciones: List<LibroSegunReglasCompleto<LibroDeProhibiciones>> = listOf()

    init
    {
        val librosSegmentados = librosSegunReglasCompletos.partition { it.libro is LibroDePrecios }

        @Suppress("UNCHECKED_CAST")
        librosDePrecios = librosSegmentados.first as List<LibroSegunReglasCompleto<LibroDePrecios>>
        @Suppress("UNCHECKED_CAST")
        librosDeProhibiciones = librosSegmentados.second as List<LibroSegunReglasCompleto<LibroDeProhibiciones>>
    }

    private val idFondoVsFondo = fondos.associateBy({ it.id!! }, { it })
    private val idImpuestoVsImpuesto = impuestos.associateBy({ it.id!! }, { it })

    private fun <TipoLibro : Libro<TipoLibro>> seleccionarLibrosAplicables(
            libros: List<LibroSegunReglasCompleto<TipoLibro>>,
            idUbicacion: Long?,
            idGrupoDeCliente: Long?,
            idPaquete: Long?
                                                                          )
            : Sequence<LibroSegunReglasCompleto<TipoLibro>>
    {
        return libros.asSequence()
            .filter { it.esAplicable(idUbicacion, idGrupoDeCliente, idPaquete) }
            .sortedByDescending { it.calcularEspecificidad(idUbicacion, idGrupoDeCliente, idPaquete) }
    }

    override fun darPreciosCompletosDeFondos(
            idsFondos: LinkedHashSet<Long>,
            idUbicacion: Long?,
            idGrupoDeCliente: Long?,
            idPaquete: Long?
                                            )
            : List<PrecioCompleto>
    {
        val librosQueAplican = seleccionarLibrosAplicables(librosDePrecios, idUbicacion, idGrupoDeCliente, idPaquete)

        return idsFondos.map {
            val idFondoBuscado = it
            val precioPorDefecto = idFondoVsFondo[idFondoBuscado]!!.precioPorDefecto

            librosQueAplican
                .firstOrNull { it.libro.precios.find { it.idFondo == idFondoBuscado } != null }
                ?.libro
                ?.let {
                    it.precios.find { it.idFondo == idFondoBuscado }?.let {
                        // No debería pasar que no encuentre el impuesto por la verificación en SQL
                        val impuestoAsociado = idImpuestoVsImpuesto[it.precio.idImpuesto]!!
                        PrecioCompleto(it.precio, ImpuestoSoloTasa(impuestoAsociado))
                    }
                }
            ?: PrecioCompleto(precioPorDefecto, ImpuestoSoloTasa(idImpuestoVsImpuesto[precioPorDefecto.idImpuesto]!!))
        }
    }

    override fun buscarReglasQueDeterminanPrecio(idFondo: Long, idUbicacion: Long?, idGrupoDeCliente: Long?, idPaquete: Long?): Set<Regla<*>>
    {
        val librosQueAplican = seleccionarLibrosAplicables(librosDePrecios, idUbicacion, idGrupoDeCliente, idPaquete)

        return librosQueAplican
                   .firstOrNull { it.libro.precios.find { it.idFondo == idFondo } != null }
                   ?.let {
                       it.buscarReglasQueAplican(idUbicacion, idGrupoDeCliente, idPaquete)
                   }
               ?: setOf()
    }

    /**
    Cómo se interpreta la regla sobre el id de un paquete?

    El libro tiene una regla, la regla esta con el id paquete 1.
    En el libro de prohibiciones está
    [LibroDeProhibiciones.prohibicionesDeFondo] con ids de fondos 3 y 4
    [LibroDeProhibiciones.prohibicionesDePaquete] con ids de fondos 5 y 6

    Lo que dice la regla es que no puede comprar el paquete 1 con los fondos 3 o 4 ni con los paquetes 5 y 6.

    Por ejemplo, la compra del paquete 1 con el fondo 3 es invalida.
    La compra del paquete 1 con el paquete 5 es invalida.
    Pero una compra de los fondos 3 y 4 más los paquetes 5 y 6 si es válida.
     */
    override fun verificarSiFondoEsVendible(
            idFondoAVerificar: Long,
            idUbicacion: Long?,
            idGrupoDeCliente: Long?,
            idsPaquetesEnCarrito: Set<Long>
                                           )
            : Boolean
    {
        val prohibicionIdFondoAVerificar = Prohibicion.DeFondo(idFondoAVerificar)

        val librosQueAplican =
                if (idsPaquetesEnCarrito.isEmpty())
                {
                    seleccionarLibrosAplicables(librosDeProhibiciones, idUbicacion, idGrupoDeCliente, null)
                }
                else
                {
                    idsPaquetesEnCarrito
                        .asSequence()
                        .flatMap { idPaqueteEnCarrito ->
                            seleccionarLibrosAplicables(librosDeProhibiciones, idUbicacion, idGrupoDeCliente, idPaqueteEnCarrito)
                                .map { Pair(it.calcularEspecificidad(idUbicacion, idGrupoDeCliente, idPaqueteEnCarrito), it) }
                        }
                        .sortedByDescending { it.first }
                        .map { it.second }
                }

        // Si no hay paquetes en el carrito esto se lee como: El fondo a verificar se encuentra prohibido en el libro
        // Si sí hay paquets en el carrito esto se lee como: Hay un paquete en carrito que no se puede mezclar con el fondo a verificar
        val estaProhibido =
                librosQueAplican.any {
                    it.libro.prohibicionesDeFondo.contains(prohibicionIdFondoAVerificar)
                }

        return !estaProhibido
    }

    /**
    Cómo se interpreta la regla sobre el id de un paquete?

    El libro tiene una regla, la regla esta con el id paquete 1.
    En el libro de prohibiciones está
    [LibroDeProhibiciones.prohibicionesDeFondo] con ids de fondos 3 y 4
    [LibroDeProhibiciones.prohibicionesDePaquete] con ids de fondos 5 y 6

    Lo que dice la regla es que no puede comprar el paquete 1 con los fondos 3 o 4 ni con los paquetes 5 y 6.

    Por ejemplo, la compra del paquete 1 con el fondo 3 es invalida.
    La compra del paquete 1 con el paquete 5 es invalida.
    Pero una compra de los fondos 3 y 4 más los paquetes 5 y 6 si es válida.
     */
    override fun verificarSiPaqueteEsVendible(
            idPaqueteAVerificar: Long,
            idUbicacion: Long?,
            idGrupoDeCliente: Long?,
            idsFondosEnCarrito: Set<Long>,
            idsPaquetesEnCarrito: Set<Long>
                                             )
            : Boolean
    {
        val prohibicionIdPaqueteAVerificar = Prohibicion.DePaquete(idPaqueteAVerificar)

        val librosConReglaExplicitaHaciaIdPaquete =
                librosDeProhibiciones
                    .asSequence()
                    .filter {
                        it.reglasIdPaquete.isNotEmpty() && it.esAplicable(idUbicacion, idGrupoDeCliente, idPaqueteAVerificar)
                    }
                    .sortedByDescending { it.calcularEspecificidad(idUbicacion, idGrupoDeCliente, idPaqueteAVerificar) }

        val idsFondosComoProhibiciones = idsFondosEnCarrito.map { Prohibicion.DeFondo(it) }
        val idsPaquetesComoProhibiciones = idsPaquetesEnCarrito.map { Prohibicion.DePaquete(it) }

        val hayAlgunFondoOPaqueteProhibidoEnCarrito =
                librosConReglaExplicitaHaciaIdPaquete.any {
                    it.libro.prohibicionesDeFondo.intersect(idsFondosComoProhibiciones).isNotEmpty()
                    || it.libro.prohibicionesDePaquete.intersect(idsPaquetesComoProhibiciones).isNotEmpty()
                }

        if (hayAlgunFondoOPaqueteProhibidoEnCarrito)
        {
            return false
        }

        val librosConReferenciaImplicitaHaciaReglaIdPaquete =
                librosDeProhibiciones
                    .asSequence()
                    .filter { it.libro.prohibicionesDePaquete.contains(prohibicionIdPaqueteAVerificar) }
                    .sortedByDescending { it.calcularEspecificidad(idUbicacion, idGrupoDeCliente, idPaqueteAVerificar) }

        for (libroSegunReglas in librosConReferenciaImplicitaHaciaReglaIdPaquete)
        {
            for (idPaqueteEnCarrito in idsPaquetesEnCarrito)
            {
                if (libroSegunReglas.reglasIdPaquete.contains(ReglaDeIdPaquete(idPaqueteEnCarrito)))
                {
                    return false
                }
            }
        }

        val librosSinReglaExplicitaHaciaIdPaquete =
                librosDeProhibiciones
                    .asSequence()
                    .filter {
                        it.reglasIdPaquete.isEmpty() && it.esAplicable(idUbicacion, idGrupoDeCliente, null)
                    }
                    .sortedByDescending { it.calcularEspecificidad(idUbicacion, idGrupoDeCliente, null) }

        val paqueteProhibidoPorubicacionOGrupoCliente =
                librosSinReglaExplicitaHaciaIdPaquete.any {
                    it.libro.prohibicionesDePaquete.contains(prohibicionIdPaqueteAVerificar)
                }

        if (paqueteProhibidoPorubicacionOGrupoCliente)
        {
            return false
        }

        return true
    }
}