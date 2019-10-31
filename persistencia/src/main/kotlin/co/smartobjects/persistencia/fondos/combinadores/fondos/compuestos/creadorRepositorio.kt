@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos

import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorioCompuesto
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorioDAO
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAOEntidadDeCliente
import co.smartobjects.persistencia.fondos.FondoDAO

internal inline fun <TipoFondo, TipoFondoDAO> darCreadorRepositorioFondoCompuesto(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosDaoFondoEspecifico: ParametrosParaDAOEntidadDeCliente<TipoFondoDAO, Long>,
        nombreEntidad: String)
        : CreadorRepositorio<TipoFondo>
{
    return CreadorRepositorioCompuesto(
            listOf(
                    CreadorRepositorioDAO(parametrosDaoFondo, nombreEntidad),
                    CreadorRepositorioDAO(parametrosDaoFondoEspecifico, nombreEntidad)
                  ),
            nombreEntidad
                                      )
}