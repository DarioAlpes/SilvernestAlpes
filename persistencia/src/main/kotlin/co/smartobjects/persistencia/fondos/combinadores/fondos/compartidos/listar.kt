@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos

import co.smartobjects.persistencia.CampoTabla
import co.smartobjects.persistencia.basederepositorios.FiltroIgualdad
import co.smartobjects.persistencia.basederepositorios.ListableDAO
import co.smartobjects.persistencia.basederepositorios.ListableSQLConFiltrosSQL
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAOEntidadDeCliente
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import com.j256.ormlite.field.SqlType


internal inline fun darListableFondoSegunTipoDeFondo(
        parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
        filtroTipoDeFondo: FiltroIgualdad<FondoDAO.TipoDeFondoEnBD>)
        : ListableSQLConFiltrosSQL<FondoDAO>
{
    return ListableSQLConFiltrosSQL(repositorioFondoDao(parametrosDaoFondo), listOf(filtroTipoDeFondo))
}

internal inline fun crearFiltroIgualdadFondoEspecifico(tipoDeFondo: FondoDAO.TipoDeFondoEnBD) =
        FiltroIgualdad(
                CampoTabla(FondoDAO.TABLA, FondoDAO.COLUMNA_TIPO_DE_FONDO),
                tipoDeFondo,
                SqlType.STRING
                      )

internal inline fun repositorioFondoDao(parametrosDao: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>): ListableDAO<FondoDAO, Long>
{
    return repositorioEntidadDao(parametrosDao, FondoDAO.COLUMNA_ID)
}