package co.smartobjects.persistencia.clientes

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAO
import co.smartobjects.persistencia.basederepositorios.darNombreEsquemaSegunIdCliente
import co.smartobjects.persistencia.basederepositorios.obtenerDAOEntidadGlobal
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErorCreandoTabla
import co.smartobjects.persistencia.excepciones.ErrorDAO
import co.smartobjects.persistencia.excepciones.ErrorDeCreacionActualizacionEntidad
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.misc.TransactionManager
import com.j256.ormlite.table.TableUtils
import java.sql.SQLException


interface
RepositorioClientes
{
    val configuracion: ConfiguracionRepositorios

    @Throws(ErrorDAO::class)
    fun crear(entidadACrear: Cliente): Cliente

    @Throws(ErrorDAO::class)
    fun listar(): Sequence<Cliente>

    @Throws(ErrorDAO::class)
    fun buscarPorId(id: Long): Cliente?

    @Throws(ErrorDAO::class)
    fun actualizar(id: Long, entidadAActualizar: Cliente): Cliente

    @Throws(ErrorDAO::class)
    fun eliminarPorId(id: Long): Boolean

    @Throws(ErrorDAO::class)
    fun crearTablaSiNoExiste()

    fun inicializarConexionAEsquemaDeSerNecesario(idCliente: Long)

    @Throws(ErrorDAO::class)
    fun limpiar()
}

class RepositorioClientesSQL constructor(override val configuracion: ConfiguracionRepositorios)
    : RepositorioClientes
{
    private val parametrosDao by lazy { ParametrosParaDAO(ClienteDAO.TABLA, obtenerDAOEntidadGlobal<ClienteDAO, Long>(configuracion)) }
    override fun crear(entidadACrear: Cliente): Cliente
    {
        val clienteDao = ClienteDAO(entidadACrear)

        try
        {
            TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                val resultadoCreacionEntidad = parametrosDao.dao.create(clienteDao)
                if (resultadoCreacionEntidad != 1)
                {
                    throw ErrorDeCreacionActualizacionEntidad(Cliente.NOMBRE_ENTIDAD)
                }
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaCreacion(e, Cliente.NOMBRE_ENTIDAD)
        }

        inicializarConexionAEsquemaDeSerNecesario(clienteDao.id!!)

        return clienteDao.aEntidadDeNegocio()
    }

    override fun listar(): Sequence<Cliente>
    {
        try
        {
            return TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                parametrosDao.dao.queryForAll().map { it.aEntidadDeNegocio() }.toList().asSequence()
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaConsulta(e, Cliente.NOMBRE_ENTIDAD)
        }
    }

    override fun buscarPorId(id: Long): Cliente?
    {
        try
        {
            return TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                parametrosDao.dao.queryForId(id)?.aEntidadDeNegocio()
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaConsulta(e, Cliente.NOMBRE_ENTIDAD)
        }
    }

    override fun actualizar(id: Long, entidadAActualizar: Cliente): Cliente
    {
        try
        {
            val idCliente = id
            val entidadDAO = ClienteDAO(entidadAActualizar.copiar(id = idCliente))
            return TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                val resultadoActualizacion = parametrosDao.dao.update(entidadDAO)
                if (resultadoActualizacion != 1)
                {
                    throw EntidadNoExiste(id, Cliente.NOMBRE_ENTIDAD)
                }
                entidadDAO.aEntidadDeNegocio()
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaActualizacion(e, Cliente.NOMBRE_ENTIDAD)
        }
    }

    override fun eliminarPorId(id: Long): Boolean
    {
        try
        {
            return TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                val elimino = parametrosDao.dao.deleteById(id) == 1
                if (elimino)
                {
                    configuracion.eliminarEsquemaParaLlave(darNombreEsquemaSegunIdCliente(id))
                }
                elimino
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaEliminacion(e, Cliente.NOMBRE_ENTIDAD)
        }
    }

    override fun inicializarConexionAEsquemaDeSerNecesario(idCliente: Long)
    {
        configuracion.inicializarConexionAEsquemaDeSerNecesario(darNombreEsquemaSegunIdCliente(idCliente))
    }

    override fun crearTablaSiNoExiste()
    {
        // Hacer una invocacion dummy a la fuente principal para crear el esquema
        val conexionDummy = configuracion.fuenteConexionesEsquemaPrincipal.databaseType

        // Verificar si existe la tabla ya que con PostgreSQL `createTableIfNotExists` no funciona
        val existeLaTabla =
                try
                {
                    DaoManager
                        .createDao(parametrosDao.dao.connectionSource, ClienteDAO::class.java)
                        .queryBuilder()
                        .selectColumns(ClienteDAO.COLUMNA_ID)
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
                TableUtils.createTable(parametrosDao.dao.connectionSource, ClienteDAO::class.java)
            }
        }
        catch (errorSQL: SQLException)
        {
            throw ErorCreandoTabla(ClienteDAO::class.java, Cliente.NOMBRE_ENTIDAD, errorSQL)
        }
    }

    override fun limpiar()
    {
        try
        {
            val clientes = listar()
            clientes.forEach {
                eliminarPorId(it.id!!)
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaEliminacion(e, Cliente.NOMBRE_ENTIDAD)
        }
    }
}