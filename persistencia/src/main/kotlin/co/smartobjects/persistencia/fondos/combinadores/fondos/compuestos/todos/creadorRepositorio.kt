@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos.todos

import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorioCompuesto
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorioDAO
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAOEntidadDeCliente
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.acceso.AccesoDAO
import co.smartobjects.persistencia.fondos.categoriaskus.CategoriaSkuDAO
import co.smartobjects.persistencia.fondos.skus.SkuDAO

internal inline fun <TipoFondo> darCreadorRepositorioFondoCompleto(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        parametrosDaoSku: ParametrosParaDAOEntidadDeCliente<SkuDAO, Long>,
        parametrosDaoCategoriaSku: ParametrosParaDAOEntidadDeCliente<CategoriaSkuDAO, Long>,
        parametrosDaoAcceso: ParametrosParaDAOEntidadDeCliente<AccesoDAO, Long>,
        nombreEntidad: String

                                                                  ): CreadorRepositorio<TipoFondo>
{
    return CreadorRepositorioCompuesto(
            listOf(
                    CreadorRepositorioDAO(parametrosDaoFondo, nombreEntidad),
                    CreadorRepositorioDAO(parametrosDaoAcceso, nombreEntidad),
                    CreadorRepositorioDAO(parametrosDaoCategoriaSku, nombreEntidad),
                    CreadorRepositorioDAO(parametrosDaoSku, nombreEntidad)
                  ),
            nombreEntidad
                                      )
}