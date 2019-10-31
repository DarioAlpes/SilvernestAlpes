@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.paquetes

import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAOEntidadDeCliente
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import co.smartobjects.persistencia.fondos.paquete.PaqueteDAO


internal val extractorIdPaqueteDao =
        object : Transformador<PaqueteDAO, Long>
        {
            override fun transformar(origen: PaqueteDAO): Long
            {
                return origen.id!!
            }
        }


internal inline fun repositorioPaqueteDao(
        parametrosDao: ParametrosParaDAOEntidadDeCliente<PaqueteDAO, Long>
                                         )
        = repositorioEntidadDao(parametrosDao, PaqueteDAO.COLUMNA_ID)