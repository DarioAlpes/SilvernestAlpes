@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos

import co.smartobjects.persistencia.TransformadorIdentidad
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.FondoDAO


internal inline fun <TipoFondo> darCombinadorEliminableParaFiltrosFondo(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        nombreEntidad: String,
        filtrosFondo: Sequence<FiltroIgualdad<*>>
                                                                       )
        : EliminablePorId<TipoFondo, Long>
        = EliminablePorIdEnTransaccionSQL(
        parametrosDaoFondo.configuracion,
        EliminableSimple(
                EliminableDao(nombreEntidad, parametrosDaoFondo).conFiltrosSQL(filtrosFondo),
                TransformadorIdentidad()
                        )
                                         )


internal inline fun <TipoFondo> darCombinadorEliminableParaFondoEspecifico(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        nombreEntidad: String,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>
                                                                          )
        : EliminablePorId<TipoFondo, Long>
        = darCombinadorEliminableParaFiltrosFondo(parametrosDaoFondo, nombreEntidad, sequenceOf(filtroTipoDeFondo))