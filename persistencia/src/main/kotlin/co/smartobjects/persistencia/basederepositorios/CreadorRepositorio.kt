package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.excepciones.ErorCreandoTabla
import co.smartobjects.persistencia.excepciones.ErrorDAO
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils
import java.sql.SQLException

@Suppress("unused")
interface CreadorRepositorio<out EntidadDeNegocio>
{
    val nombreEntidad: String

    @Throws(ErrorDAO::class)
    fun inicializarParaCliente(idCliente: Long)

    @Throws(ErrorDAO::class)
    fun limpiarParaCliente(idCliente: Long)
}

internal class CreadorUnicaVez<out EntidadNegocio>(
        private val repositorio: CreadorRepositorio<EntidadNegocio>
                                                  ) : CreadorRepositorio<EntidadNegocio>
{
    // El candado NO debe estar en el companion, no queremos sincronizar todos los llamados a todos los CreadorUnicaVez. Solo los llamados a esta instancia
    private val candadoSincronizacion = Object()

    // Si vamos a sincronizar a mano no tiene sentido usar un ConcurrentHashMap
    private val clientesInicializados = HashMap<Long, Boolean>()

    override fun inicializarParaCliente(idCliente: Long)
    {
        synchronized(candadoSincronizacion)
        {
            val estaInicializado = clientesInicializados[idCliente] ?: false

            if (!estaInicializado)
            {
                repositorio.inicializarParaCliente(idCliente)
                clientesInicializados[idCliente] = true
            }
        }
    }

    override fun limpiarParaCliente(idCliente: Long)
    {
        repositorio.limpiarParaCliente(idCliente)
    }

    override val nombreEntidad: String = repositorio.nombreEntidad

}

internal class CreadorRepositorioDAO<out EntidadDao, out TipoId>(
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>,
        override val nombreEntidad: String)
    : CreadorRepositorio<EntidadDao>
{
    override fun inicializarParaCliente(idCliente: Long)
    {
        // Verificar si existe la tabla ya que con PostgreSQL `createTableIfNotExists` no funciona
        val existeLaTabla =
                try
                {
                    DaoManager
                        .createDao(parametros[idCliente].dao.connectionSource, parametros.clase)
                        .queryBuilder()
                        .limit(1)
                        .query()
                        .firstOrNull()

                    true
                }
                catch (errorSQL: SQLException)
                {
                    false
                }
        try
        {
            if (!existeLaTabla)
            {
                TableUtils.createTable(parametros[idCliente].dao.connectionSource, parametros.clase)
            }
        }
        catch (errorSQL: SQLException)
        {
            throw ErorCreandoTabla(parametros.clase, nombreEntidad, errorSQL)
        }
    }

    override fun limpiarParaCliente(idCliente: Long)
    {
        try
        {
            parametros[idCliente].dao.deleteBuilder().delete()
        }
        catch (e: SQLException)
        {
            throw parametros.configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaEliminacion(e, nombreEntidad)
        }
    }
}

internal class CreadorRepositorioSimple<out EntidadNegocio, out EntidadDao, out TipoIdDao>(
        private val repositorio: CreadorRepositorio<EntidadDao>)
    : CreadorRepositorio<EntidadNegocio>
{
    override val nombreEntidad: String = repositorio.nombreEntidad

    constructor(
            parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoIdDao>,
            nombreEntidad: String
               ) : this(CreadorRepositorioDAO(parametros, nombreEntidad))

    override fun inicializarParaCliente(idCliente: Long)
    {
        repositorio.inicializarParaCliente(idCliente)
    }

    override fun limpiarParaCliente(idCliente: Long)
    {
        repositorio.limpiarParaCliente(idCliente)
    }
}

internal class CreadorRepositorioCompuesto<out EntidadNegocio>(
        private val repositorios: List<CreadorRepositorio<*>>,
        override val nombreEntidad: String)
    : CreadorRepositorio<EntidadNegocio>
{

    override fun inicializarParaCliente(idCliente: Long)
    {
        repositorios.forEach {
            it.inicializarParaCliente(idCliente)
        }
    }

    override fun limpiarParaCliente(idCliente: Long)
    {
        repositorios.forEach {
            it.limpiarParaCliente(idCliente)
        }
    }
}

internal class CreadorRepositorioEnTransaccionSQL<out EntidadDeNegocio>(
        private val configuracion: ConfiguracionRepositorios,
        private val creadorSinTransaccion: CreadorRepositorio<EntidadDeNegocio>)
    : CreadorRepositorio<EntidadDeNegocio>
{
    override val nombreEntidad: String = creadorSinTransaccion.nombreEntidad
    override fun inicializarParaCliente(idCliente: Long)
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaCreacion(configuracion, idCliente, nombreEntidad) {
            creadorSinTransaccion.inicializarParaCliente(idCliente)
        }
    }

    override fun limpiarParaCliente(idCliente: Long)
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaEliminacion(configuracion, idCliente, nombreEntidad) {
            creadorSinTransaccion.inicializarParaCliente(idCliente)
        }
    }
}