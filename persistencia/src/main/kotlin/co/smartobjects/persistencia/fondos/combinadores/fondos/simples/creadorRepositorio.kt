@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.simples

import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorioCompuesto
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorioDAO
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAOEntidadDeCliente
import co.smartobjects.persistencia.fondos.FondoDAO

internal inline fun <TipoFondo> darCreadorRepositorioFondoSimple(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        nombreEntidad: String)
        : CreadorRepositorio<TipoFondo>
{
    return CreadorRepositorioCompuesto(listOf(CreadorRepositorioDAO(parametrosDaoFondo, nombreEntidad)), nombreEntidad)
}