@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores

import co.smartobjects.persistencia.basederepositorios.ListableDAO
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAOEntidadDeCliente


internal inline fun <EntidadDao, TipoId> repositorioEntidadDao(
        parametrosDao: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>,
        nombreColumnasOrdenamiento: List<String>) = ListableDAO(nombreColumnasOrdenamiento, parametrosDao)


internal inline fun <EntidadDao, TipoId> repositorioEntidadDao(
        parametrosDao: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>,
        nombreColumnasOrdenamiento: String) = repositorioEntidadDao(parametrosDao, listOf(nombreColumnasOrdenamiento))