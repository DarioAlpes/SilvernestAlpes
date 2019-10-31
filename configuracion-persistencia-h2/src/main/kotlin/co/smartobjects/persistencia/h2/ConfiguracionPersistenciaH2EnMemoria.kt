package co.smartobjects.persistencia.h2

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.MapeadorCodigosSQL
import com.j256.ormlite.jdbc.DataSourceConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.support.DatabaseConnection
import com.zaxxer.hikari.HikariDataSource
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class ConfiguracionPersistenciaH2EnMemoria(nombreBD: String) : ConfiguracionRepositorios(nombreBD)
{
    override val fuenteConexionesEsquemaPrincipal: ConnectionSource by lazy {
        DataSourceConnectionSource(poolConexiones, poolConexiones.jdbcUrl)
    }

    override val mapeadorCodigosError: MapeadorCodigosSQL = MapeadorCodigosSQLH2()

    private val poolsEsquemasCreados: ConcurrentMap<String, HikariDataSource> = ConcurrentHashMap()

    private val poolConexiones: HikariDataSource by lazy {
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = "jdbc:h2:mem:$nombreBD"
        dataSource.driverClassName = "org.h2.Driver"
        dataSource.maximumPoolSize = 2
        dataSource
    }

    private fun crearPoolConexionesParaEsquema(nombreEsquema: String): HikariDataSource
    {
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = "jdbc:h2:mem:$nombreBD"
        dataSource.connectionInitSql = "SET SCHEMA $nombreEsquema"
        dataSource.driverClassName = "org.h2.Driver"
        return dataSource
    }

    override fun crearEsquemaSiNoExiste(nombreDeEsquema: String)
    {
        val connection = fuenteConexionesEsquemaPrincipal.getReadWriteConnection(null)
        try
        {
            connection.executeStatement("CREATE SCHEMA IF NOT EXISTS $nombreDeEsquema", DatabaseConnection.DEFAULT_RESULT_FLAGS)
        }
        finally
        {
            try
            {
                fuenteConexionesEsquemaPrincipal.releaseConnection(connection)
            }
            catch (e: SQLException)
            {
                // Se ignoran los errores, estamos saliendo
            }
        }
        poolsEsquemasCreados.getOrPut(nombreDeEsquema) { crearPoolConexionesParaEsquema(nombreDeEsquema) }
    }

    override fun eliminarEsquema(nombreDeEsquema: String)
    {
        val connection = fuenteConexionesEsquemaPrincipal.getReadWriteConnection(null)
        try
        {
            connection.executeStatement("DROP SCHEMA $nombreDeEsquema", DatabaseConnection.DEFAULT_RESULT_FLAGS)
        }
        finally
        {
            try
            {
                fuenteConexionesEsquemaPrincipal.releaseConnection(connection)
            }
            catch (e: SQLException)
            {
                // Se ignoran los errores, estamos saliendo
            }
        }
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
}