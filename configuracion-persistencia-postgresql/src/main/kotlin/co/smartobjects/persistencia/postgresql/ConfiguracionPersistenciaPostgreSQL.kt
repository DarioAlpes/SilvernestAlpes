package co.smartobjects.persistencia.postgresql

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.MapeadorCodigosSQL
import com.j256.ormlite.jdbc.DataSourceConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.support.DatabaseConnection
import com.zaxxer.hikari.HikariDataSource
import java.sql.SQLException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class ConfiguracionPersistenciaPostgreSQL(val configuracion: Config) : ConfiguracionRepositorios(configuracion.nombreBD)
{
    companion object
    {
        private const val DRIVER = "org.postgresql.Driver"
    }

    private val poolConexiones: HikariDataSource by lazy {
        crearPoolConexionesBase {
            it.maximumPoolSize = 2 * Runtime.getRuntime().availableProcessors() + 1
        }
    }

    override val fuenteConexionesEsquemaPrincipal: ConnectionSource by lazy {
        DataSourceConnectionSource(poolConexiones, poolConexiones.jdbcUrl)
    }

    override val mapeadorCodigosError: MapeadorCodigosSQL = MapeadorCodigosSQLPostgreSQL()

    private val poolsEsquemasCreados: ConcurrentMap<String, HikariDataSource> = ConcurrentHashMap()


    private fun crearPoolConexionesBase(modificador: (HikariDataSource) -> Unit): HikariDataSource
    {
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = "jdbc:postgresql://${configuracion.ip}:5432/$nombreBD"
        dataSource.username = configuracion.usuarioBD
        dataSource.password = configuracion.contraseñaBD
        dataSource.minimumIdle = Runtime.getRuntime().availableProcessors() - 1
        dataSource.connectionTimeout = 13_000
        dataSource.driverClassName = DRIVER

        modificador(dataSource)

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

    private fun crearPoolConexionesParaEsquema(nombreEsquema: String): HikariDataSource
    {
        return crearPoolConexionesBase {
            it.connectionInitSql = "SET SCHEMA '$nombreEsquema'"
        }
    }

    override fun eliminarEsquema(nombreDeEsquema: String)
    {
        val connection = fuenteConexionesEsquemaPrincipal.getReadWriteConnection(null)
        try
        {
            connection.executeStatement("DROP SCHEMA $nombreDeEsquema CASCADE", DatabaseConnection.DEFAULT_RESULT_FLAGS)
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

    data class Config(
            val ip: String,
            val nombreBD: String,
            val usuarioBD: String,
            val contraseñaBD: String
                     )
}