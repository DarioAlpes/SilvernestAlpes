package co.smartobjects.persistencia.sqlite

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.MapeadorCodigosSQL
import com.j256.ormlite.jdbc.DataSourceConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.zaxxer.hikari.HikariDataSource
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.sql.DataSource


abstract class ConfiguracionPersistenciaSQLite(nombreBD: String, override val llavesForaneasActivadas: Boolean)
    : ConfiguracionRepositorios(nombreBD)
{
    private val poolsEsquemasCreados: ConcurrentMap<String, DataSourceCerrable> = ConcurrentHashMap()

    private val poolConexiones: DataSourceCerrable by lazy {
        crearPoolConexionesParaBD(nombreBD)
    }
    override val fuenteConexionesEsquemaPrincipal: ConnectionSource by lazy {
        DataSourceConnectionSource(poolConexiones, poolConexiones.jdbcUrl)
        // Si activar lo de llaves foráneas presenta problemas en connectionTestQuery, descomentar esto
        /*.also {
            activarLlavesForaneasSiEsNecesario(it)
        }*/
    }

    override val mapeadorCodigosError: MapeadorCodigosSQL = MapeadorCodigosSQLite()


    protected abstract fun darUrlJDBC(nombreBaseDeDatos: String): String

    private fun crearPoolConexionesParaBD(nombreBD: String): DataSourceCerrable
    {
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = darUrlJDBC(nombreBD)
        dataSource.connectionInitSql = "PRAGMA journal_mode = WAL;"
        if (llavesForaneasActivadas)
        {
            dataSource.connectionTestQuery = "PRAGMA foreign_keys = ON;"
        }
        dataSource.driverClassName = "org.sqlite.JDBC"
        dataSource.maximumPoolSize = Runtime.getRuntime().availableProcessors() + 1

        return FuenteDeConexionesManteniendoPrimerasDosConexiones(dataSource)
    }

    override fun crearEsquemaSiNoExiste(nombreDeEsquema: String)
    {
        poolsEsquemasCreados.getOrPut(nombreDeEsquema) {
            crearPoolConexionesParaBD("${nombreBD}_$nombreDeEsquema")
        }
    }

    override fun eliminarEsquema(nombreDeEsquema: String)
    {
        try
        {
            poolsEsquemasCreados.remove(nombreDeEsquema)?.close()
        }
        catch (e: SQLException)
        {
            // Se ignoran los errores, estamos saliendo
        }
    }

    override fun crearFuenteDeConexionesParaNombreDeEsquema(nombreDeEsquema: String): ConnectionSource
    {
        val poolEsquema = poolsEsquemasCreados[nombreDeEsquema]!!
        return DataSourceConnectionSource(poolEsquema, poolEsquema.jdbcUrl)
        // Si activar lo de llaves foráneas presenta problemas en connectionTestQuery, descomentar esto
        /*.also {
            activarLlavesForaneasSiEsNecesario(it)
        }*/
    }

    override fun limpiarRecursos()
    {
        try
        {
            poolsEsquemasCreados.values.forEach {
                it.close()
            }
            poolConexiones.close()
        }
        catch (e: SQLException)
        {
            // Se ignoran los errores, estamos saliendo
        }
    }

    /*private fun activarLlavesForaneasSiEsNecesario(fuenteDeConxion: DataSourceConnectionSource)
    {
        if (llavesForaneasActivadas)
        {
            val connection = fuenteDeConxion.getReadWriteConnection(null)
            try
            {
                with(connection as JdbcDatabaseConnection)
                {
                    // SQLite solo soporta ResultSet.TYPE_FORWARD_ONLY
                    val statement = internalConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                    statement.execute("PRAGMA foreign_keys = ON;")
                }
            }
            finally
            {
                try
                {
                    fuenteDeConxion.releaseConnection(connection)
                }
                catch (e: SQLException)
                {
                }
            }
        }
    }*/
}

class ConfiguracionPersistenciaSQLiteEnMemoria(nombreBD: String) : ConfiguracionPersistenciaSQLite(nombreBD, true)
{
    override fun darUrlJDBC(nombreBaseDeDatos: String) = "jdbc:sqlite:file:$nombreBaseDeDatos?mode=memory&cache=shared"
}

class ConfiguracionPersistenciaSQLiteEnDisco
(
        private val rutaCarpeta: File,
        nombreBD: String
) : ConfiguracionPersistenciaSQLite(nombreBD, false)
{
    init
    {
        if (!rutaCarpeta.exists())
        {
            if (!rutaCarpeta.mkdir())
            {
                throw IOException("No fue posible crear el directorio '${rutaCarpeta.absolutePath}'")
            }
        }

        if (!rutaCarpeta.isDirectory)
        {
            rutaCarpeta.delete()
            throw Exception("La ruta '${rutaCarpeta.absolutePath}' no es un directorio")
        }
    }

    override fun darUrlJDBC(nombreBaseDeDatos: String) = "jdbc:sqlite:file:${File(rutaCarpeta, "$nombreBaseDeDatos.db").path}"
}

interface DataSourceCerrable : DataSource, Closeable
{
    val jdbcUrl: String
}