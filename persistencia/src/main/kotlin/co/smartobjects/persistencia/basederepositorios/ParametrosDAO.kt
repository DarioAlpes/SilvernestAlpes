package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.ConfiguracionRepositorios
import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


internal inline fun <reified T, reified ID> obtenerDAOEntidadGlobal(configuracion: ConfiguracionRepositorios): Dao<T, ID>
{
    return obtenerDAOSegunFuenteDeConexionesYClases(configuracion.fuenteConexionesEsquemaPrincipal, T::class.java)
}

internal fun <T, ID> obtenerDAOSegunFuenteDeConexionesYClases(fuenteConexiones: ConnectionSource, clase: Class<T>): Dao<T, ID>
{
    return DaoManager.createDao(fuenteConexiones, clase)
}

internal data class ParametrosParaDAO<EntidadDAO, IdDao>(val nombreTabla: String, val dao: Dao<EntidadDAO, IdDao>)
{
    val numeroColumnas = (dao as BaseDaoImpl<EntidadDAO, IdDao>).tableInfo.fieldTypes.size
}

internal class ParametrosParaDAOEntidadDeCliente<EntidadDAO, Id>(
        internal val configuracion: ConfiguracionRepositorios,
        val nombreTabla: String,
        val clase: Class<EntidadDAO>
                                                                )
{
    private val parametrosSegunIdCliente: ConcurrentMap<Long, ParametrosParaDAO<EntidadDAO, Id>> = ConcurrentHashMap()

    operator fun get(idCliente: Long) =
            parametrosSegunIdCliente.getOrPut(idCliente) {
                ParametrosParaDAO(
                        nombreTabla,
                        obtenerDAOSegunFuenteDeConexionesYClases(
                                configuracion.darFuenteDeConexionesParaLlave(darNombreEsquemaSegunIdCliente(idCliente)),
                                clase
                                                                )
                                 )
            }
}

internal fun darNombreEsquemaSegunIdCliente(idCliente: Long) = "${Cliente.NOMBRE_ENTIDAD.toLowerCase()}_$idCliente"