package co.smartobjects.persistencia.sqlite

import com.zaxxer.hikari.HikariDataSource
import java.io.Closeable
import java.sql.Connection
import javax.sql.DataSource

internal class FuenteDeConexionesManteniendoPrimerasDosConexiones(private val dataSource: HikariDataSource) :
        DataSource by dataSource, Closeable by dataSource, DataSourceCerrable
{
    override val jdbcUrl: String = dataSource.jdbcUrl
    private var primeraConexion: Connection? = null
    private var devolvioPrimeraConexion: Boolean = false
    override fun getConnection(): Connection?
    {
        return if (devolvioPrimeraConexion)
        {
            if (primeraConexion == null)
            {
                dataSource.connection
            }
            else
            {
                val conexion = primeraConexion
                primeraConexion = null
                conexion
            }
        }
        else
        {
            primeraConexion = dataSource.connection
            devolvioPrimeraConexion = true
            primeraConexion
        }
    }
}