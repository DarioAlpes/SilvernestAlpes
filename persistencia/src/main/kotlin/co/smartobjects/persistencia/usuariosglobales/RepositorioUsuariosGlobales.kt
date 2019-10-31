package co.smartobjects.persistencia.usuariosglobales

import co.smartobjects.entidades.usuariosglobales.UsuarioGlobal
import co.smartobjects.persistencia.CampoModificableEntidadDao
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.basederepositorios.CamposDeEntidad
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAO
import co.smartobjects.persistencia.basederepositorios.obtenerDAOEntidadGlobal
import co.smartobjects.persistencia.excepciones.*
import co.smartobjects.persistencia.usuarios.Hasher
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.misc.TransactionManager
import com.j256.ormlite.table.TableUtils
import java.sql.SQLException

interface RepositorioUsuariosGlobales
{
    val configuracion: ConfiguracionRepositorios

    @Throws(ErrorDAO::class)
    fun crear(entidadACrear: UsuarioGlobal.UsuarioParaCreacion): UsuarioGlobal

    @Throws(ErrorDAO::class)
    fun listar(): Sequence<UsuarioGlobal>

    @Throws(ErrorDAO::class)
    fun buscarPorId(id: String): UsuarioGlobal?

    @Throws(ErrorDAO::class)
    fun actualizar(id: String, entidadAActualizar: UsuarioGlobal.UsuarioParaCreacion): UsuarioGlobal

    @Throws(ErrorDAO::class)
    fun actualizarCamposIndividuales(id: String, camposAActualizar: CamposDeEntidad<UsuarioGlobal>)

    @Throws(ErrorDAO::class)
    fun eliminarPorId(id: String): Boolean

    @Throws(ErrorDAO::class)
    fun inicializar(): Boolean

    @Throws(ErrorDAO::class)
    fun limpiar()
}

interface RepositorioCredencialesGuardadasUsuarioGlobal
{
    @Throws(ErrorDAO::class)
    fun buscarPorId(id: String): UsuarioGlobal.CredencialesGuardadas?
}

class RepositorioUsuariosGlobalesSQL constructor(override val configuracion: ConfiguracionRepositorios, private val hasher: Hasher)
    : RepositorioUsuariosGlobales
{
    private val parametrosDao by lazy { ParametrosParaDAO(UsuarioGlobalDAO.TABLA, obtenerDAOEntidadGlobal<UsuarioGlobalDAO, String>(configuracion)) }
    override fun crear(entidadACrear: UsuarioGlobal.UsuarioParaCreacion): UsuarioGlobal
    {
        return try
        {
            val usuarioDAO = UsuarioGlobalDAO(entidadACrear, hasher.calcularHash(entidadACrear.contraseña), true)

            TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                val resultadoCreacionEntidad = parametrosDao.dao.create(usuarioDAO)
                if (resultadoCreacionEntidad != 1)
                {
                    throw ErrorDeCreacionActualizacionEntidad(UsuarioGlobal.NOMBRE_ENTIDAD)
                }
                usuarioDAO.aEntidadDeNegocio()
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaCreacion(e, UsuarioGlobal.NOMBRE_ENTIDAD)
        }
        finally
        {
            entidadACrear.limpiarContraseña()
        }
    }

    override fun listar(): Sequence<UsuarioGlobal>
    {
        try
        {
            return TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                parametrosDao.dao.queryForAll().asSequence().map { it.aEntidadDeNegocio() }
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaConsulta(e, UsuarioGlobal.NOMBRE_ENTIDAD)
        }
    }

    override fun buscarPorId(id: String): UsuarioGlobal?
    {
        return try
        {
            TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                parametrosDao.dao.queryForId(id)?.aEntidadDeNegocio()
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaConsulta(e, UsuarioGlobal.NOMBRE_ENTIDAD)
        }
    }

    override fun actualizar(id: String, entidadAActualizar: UsuarioGlobal.UsuarioParaCreacion): UsuarioGlobal
    {
        return try
        {
            val entidadDAO = UsuarioGlobalDAO(entidadAActualizar, hasher.calcularHash(entidadAActualizar.contraseña), false)
            TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                val resultadoActualizacion = parametrosDao.dao.update(entidadDAO)
                if (resultadoActualizacion != 1)
                {
                    throw EntidadNoExiste(id, UsuarioGlobal.NOMBRE_ENTIDAD)
                }
                entidadDAO.aEntidadDeNegocio()
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaActualizacion(e, UsuarioGlobal.NOMBRE_ENTIDAD)
        }
        finally
        {
            entidadAActualizar.limpiarContraseña()
        }
    }

    override fun actualizarCamposIndividuales(id: String, camposAActualizar: CamposDeEntidad<UsuarioGlobal>)
    {
        try
        {
            val camposAActualizarDAO = camposAActualizar.map {
                when
                {
                    it.key == UsuarioGlobal.Campos.CONTRASEÑA ->
                    {
                        val campo = it.value as UsuarioGlobal.CredencialesUsuario.CampoContraseña
                        val hash = hasher.calcularHash(campo.valor)
                        campo.limpiarValor()
                        UsuarioGlobalDAO.COLUMNA_HASH_CONTRASEÑA to CampoModificableEntidadDao<UsuarioGlobalDAO, Any?>(hash, UsuarioGlobalDAO.COLUMNA_HASH_CONTRASEÑA)
                    }
                    it.key == UsuarioGlobal.Campos.ACTIVO     ->
                    {
                        UsuarioGlobalDAO.COLUMNA_ACTIVO to CampoModificableEntidadDao(it.value.valor, UsuarioGlobalDAO.COLUMNA_ACTIVO)
                    }
                    else                                      ->
                    {
                        throw CampoActualizableDesconocido(it.key, UsuarioGlobal.NOMBRE_ENTIDAD)
                    }
                }
            }.toMap()

            val updateBuilder = parametrosDao.dao.updateBuilder().apply {
                val filtro = where()
                filtro.idEq(id)
            }

            camposAActualizarDAO.forEach {
                updateBuilder.updateColumnValue(it.key, it.value.valor)
            }
            val numeroFilasActualizadas = updateBuilder.update()
            if (numeroFilasActualizadas != 1)
            {
                throw EntidadNoExiste(id, UsuarioGlobal.NOMBRE_ENTIDAD)
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaActualizacion(e, UsuarioGlobal.NOMBRE_ENTIDAD)
        }
    }

    override fun eliminarPorId(id: String): Boolean
    {
        return try
        {
            TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                parametrosDao.dao.deleteById(id) == 1
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaEliminacion(e, UsuarioGlobal.NOMBRE_ENTIDAD)
        }
    }

    override fun inicializar(): Boolean
    {
        // Hacer una invocacion dummy a la fuente principal para crear el esquema
        val conexionDummy = configuracion.fuenteConexionesEsquemaPrincipal.databaseType

        // Verificar si existe la tabla ya que con PostgreSQL `createTableIfNotExists` no funciona
        val existeLaTabla =
                try
                {
                    DaoManager
                        .createDao(parametrosDao.dao.connectionSource, UsuarioGlobalDAO::class.java)
                        .queryBuilder()
                        .selectColumns(UsuarioGlobalDAO.COLUMNA_ID)
                        .limit(1)
                        .queryForFirst()

                    true
                }
                catch (errorSQL: SQLException)
                {
                    false
                }

        if (!existeLaTabla)
        {
            try
            {
                TableUtils.createTable(parametrosDao.dao.connectionSource, UsuarioGlobalDAO::class.java)
            }
            catch (errorSQL: SQLException)
            {
                throw ErorCreandoTabla(UsuarioGlobalDAO::class.java, UsuarioGlobal.NOMBRE_ENTIDAD, errorSQL)
            }
        }

        return existeLaTabla
    }

    override fun limpiar()
    {
        try
        {
            TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                parametrosDao.dao.deleteBuilder().delete()
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaEliminacion(e, UsuarioGlobal.NOMBRE_ENTIDAD)
        }
    }

}

class RepositorioCredencialesGuardadasUsuarioGlobalSQL constructor(private val configuracion: ConfiguracionRepositorios)
    : RepositorioCredencialesGuardadasUsuarioGlobal
{
    private val parametrosDao by lazy {
        ParametrosParaDAO(UsuarioGlobalDAO.TABLA, obtenerDAOEntidadGlobal<UsuarioGlobalDAO, String>(configuracion))
    }

    override fun buscarPorId(id: String): UsuarioGlobal.CredencialesGuardadas?
    {
        return try
        {
            TransactionManager.callInTransaction(configuracion.fuenteConexionesEsquemaPrincipal) {
                parametrosDao.dao
                    .queryBuilder()
                    .apply {
                        where()
                            .ne(UsuarioGlobalDAO.COLUMNA_ACTIVO, false)
                            .and()
                            .idEq(id)
                    }
                    .queryForFirst()
                    ?.run {
                        UsuarioGlobal.CredencialesGuardadas(aEntidadDeNegocio(), hashContraseña)
                    }
            }
        }
        catch (e: SQLException)
        {
            throw configuracion.mapeadorCodigosError.parsearCodigoDeErrorSQLParaConsulta(e, UsuarioGlobal.NOMBRE_ENTIDAD)
        }
    }

}