@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos.todos

import co.smartobjects.persistencia.basederepositorios.EliminablePorId
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAOEntidadDeCliente
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.darCombinadorEliminableParaFiltrosFondo


internal inline fun <TipoFondo> darCombinadorEliminableParaFondoCompleto(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        nombreEntidad: String
                                                                        )
        : EliminablePorId<TipoFondo, Long>
        = darCombinadorEliminableParaFiltrosFondo(parametrosDaoFondo, nombreEntidad, sequenceOf())