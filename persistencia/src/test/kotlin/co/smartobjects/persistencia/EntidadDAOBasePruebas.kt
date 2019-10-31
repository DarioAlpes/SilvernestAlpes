package co.smartobjects.persistencia

//import com.opentable.db.postgres.embedded.EmbeddedPostgres
import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.clientes.RepositorioClientes
import co.smartobjects.persistencia.clientes.RepositorioClientesSQL
import co.smartobjects.persistencia.postgresql.ConfiguracionPersistenciaPostgreSQL
import co.smartobjects.persistencia.sqlite.ConfiguracionPersistenciaSQLiteEnMemoria
import co.smartobjects.utilidades.ID_ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.logger.LocalLog
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.*
import org.junit.platform.commons.support.AnnotationSupport.isAnnotated
import java.util.stream.Stream


@ExtendWith(EjecutarPorCadaBD::class)
internal abstract class EntidadDAOBasePruebas(private val tieneEntidadClienteComoPadre: Boolean = true)
{
    companion object
    {
        val CONFIGURACION_POSTGRESQL =
                ConfiguracionPersistenciaPostgreSQL.Config(
                        "localhost",
                        "postgres",
                        "postgres",
                        "postgres"
                                                          )

        private lateinit var bdPostgreSQL: EmbeddedPostgres


        @[Suppress("unused") BeforeAll JvmStatic]
        fun desactivarLogsORMLite()
        {
            System.setProperty("user.timezone", ID_ZONA_HORARIA_POR_DEFECTO)
            System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "FATAL")

            bdPostgreSQL =
                    EmbeddedPostgres
                        .builder()
                        .setPort(5432)
                        .setServerConfig("fsync", "off")
                        .setServerConfig("full_page_writes", "off")
                        .start()
            /*bdPostgreSQL = EmbeddedPostgres(Version.V10_3)
            bdPostgreSQL
                .start(
                        EmbeddedPostgres.cachedRuntimeConfig(File("""C:\Users\mors\Documents\SmartObjects\PostgreSQLDePruebas""").toPath()),
                        CONFIGURACION_POSTGRESQL.ip,
                        5432,
                        CONFIGURACION_POSTGRESQL.nombreBD,
                        CONFIGURACION_POSTGRESQL.usuarioBD,
                        CONFIGURACION_POSTGRESQL.contraseñaBD,
                        emptyList()
                      )*/
        }

        @[Suppress("unused") AfterAll JvmStatic]
        fun limpiarPostgreSQL()
        {
            bdPostgreSQL.close()
            /*bdPostgreSQL.process.ifPresent(PostgresProcess::stop)*/
        }
    }

    protected val repositorioClientes: RepositorioClientes by lazy { RepositorioClientesSQL(configuracionRepositorios) }

    abstract val creadoresRepositoriosUsados: List<CreadorRepositorio<*>>

    protected lateinit var configuracionRepositorios: ConfiguracionRepositorios

    private lateinit var clientePruebas: Cliente
    private lateinit var clienteAlternativo: Cliente

    val idClientePruebas by lazy { clientePruebas.id!! }

    @BeforeEach
    fun crearClienteDePruebas(configuracionRepositorios: ConfiguracionRepositorios)
    {
        if (this::configuracionRepositorios.isInitialized)
        {
            this.configuracionRepositorios.limpiarRecursos()
        }
        this.configuracionRepositorios = configuracionRepositorios
        repositorioClientes.crearTablaSiNoExiste()
        repositorioClientes.limpiar() // Con PostgreSQL quedan datos viejos por culpa de que se haga timeout por el número de conexiones
        if (tieneEntidadClienteComoPadre)
        {
            clientePruebas = repositorioClientes.crear(Cliente(null, "Cliente pruebas"))
            clienteAlternativo = repositorioClientes.crear(Cliente(null, "Cliente alternativo"))
            creadoresRepositoriosUsados.forEach {
                it.inicializarParaCliente(clientePruebas.id!!)
                it.inicializarParaCliente(clienteAlternativo.id!!)
            }
        }
    }

    @AfterEach
    fun limpiarTablas()
    {
        repositorioClientes.limpiar()
    }

    protected fun ejecutarConClienteAlternativo(funcionAEjecutarConCliente: (Cliente) -> Unit)
    {
        funcionAEjecutarConCliente(clienteAlternativo)
    }
}

internal class EjecutarPorCadaBD : TestTemplateInvocationContextProvider
{
    override fun supportsTestTemplate(context: ExtensionContext): Boolean
    {
        return isAnnotated(context.testMethod.get(), TestConMultiplesDAO::class.java)
    }

    override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext>
    {
        return Stream.of(
                ContextoResolvedorConfiguracionDAOEspecifica("PostgreSQL", ConfiguracionPersistenciaPostgreSQL(EntidadDAOBasePruebas.CONFIGURACION_POSTGRESQL)),
                ContextoResolvedorConfiguracionDAOEspecifica("SQLite", ConfiguracionPersistenciaSQLiteEnMemoria("db_pruebas_backend"))
                        )
    }

    class ContextoResolvedorConfiguracionDAOEspecifica(
            private val nombreGestor: String,
            val configuracionGestorDeEntidades: ConfiguracionRepositorios)
        : TestTemplateInvocationContext
    {
        override fun getDisplayName(invocationIndex: Int): String
        {
            return "Con $nombreGestor"
        }

        override fun getAdditionalExtensions(): MutableList<Extension>
        {
            return mutableListOf(ResolvedorDAOEspecifico())
        }

        inner class ResolvedorDAOEspecifico : ParameterResolver
        {
            override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean
            {
                return parameterContext.parameter.type == ConfiguracionRepositorios::class.java
            }

            override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any
            {
                return configuracionGestorDeEntidades
            }
        }
    }
}