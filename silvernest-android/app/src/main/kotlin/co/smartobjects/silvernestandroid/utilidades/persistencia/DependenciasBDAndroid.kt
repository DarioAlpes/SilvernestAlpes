package co.smartobjects.silvernestandroid.utilidades.persistencia


/*
internal class DependenciasBDAndroid(contextoAplicacion: Application)
    : MiOrmLiteSqliteOpenHelper(
        contextoAplicacion,
        DependenciasBDAndroid.NOMBRE_BASEDATOS,
        null,
        DependenciasBDAndroid.VERSION_BASEDATOS
                               ),
      DependenciasBD
{
    companion object
    {
        private const val VERSION_BASEDATOS = 1
        internal const val NOMBRE_BASEDATOS = "BDSilvernest.db"
    }

    private var _configuracionRepositorios: ConfiguracionRepositorios? = null
    override val configuracionRepositorios: ConfiguracionRepositorios
        get() = _configuracionRepositorios
                ?: throw IllegalStateException("No se ha invocado el método onCraete todavía")

    init
    {
        contextoAplicacion.openOrCreateDatabase(DependenciasBDAndroid.NOMBRE_BASEDATOS, Context.MODE_PRIVATE, null)

        _configuracionRepositorios = ConfiguracionPersistenciaSQLiteAndroid(getConnectionSource())
    }

    override fun onCreate(database: SQLiteDatabase, connectionSource: ConnectionSource)
    {
    }

    override fun onUpgrade(database: SQLiteDatabase, connectionSource: ConnectionSource, oldVersion: Int, newVersion: Int)
    {
    }

    override fun onConfigure(db: SQLiteDatabase)
    {
        super.onConfigure(db)

        db.setForeignKeyConstraintsEnabled(false)
        db.enableWriteAheadLogging()

        Log.d(AplicacionPrincipal.TAG, "++ Base de datos configurada ++")
    }

    override val repositorioClientes by lazy<RepositorioClientes> { RepositorioClientesSQL(configuracionRepositorios) }
    override val repositorioLlavesNFC by lazy<RepositorioLlavesNFC> { RepositorioLlavesNFCSQL(configuracionRepositorios) }
    override val repositorioRoles by lazy<RepositorioRoles> { RepositorioRolesSQL(configuracionRepositorios) }
    override val repositorioUsuarios by lazy<RepositorioUsuarios> {
        RepositorioUsuariosSQL(configuracionRepositorios, object : Hasher
        {
            override fun calcularHash(entrada: CharArray): String = String(entrada)
        })
    }
    override val repositorioUbicaciones by lazy<RepositorioUbicaciones> { RepositorioUbicacionesSQL(configuracionRepositorios) }
    override val repositorioUbicacionesContabilizables by lazy<RepositorioUbicacionesContabilizables> { RepositorioUbicacionesContabilizablesSQL(configuracionRepositorios) }
    override val repositorioPersonas by lazy<RepositorioPersonas> { RepositorioPersonasSQL(configuracionRepositorios) }
    override val repositorioImpuestos by lazy<RepositorioImpuestos> { RepositorioImpuestosSQL(configuracionRepositorios) }
    override val repositorioGrupoClientes by lazy<RepositorioGrupoClientes> { RepositorioGrupoClientesSQL(configuracionRepositorios) }
    override val repositorioCampoDePersonas by lazy<RepositorioCamposDePersonas> { RepositorioCampoDePersonasSQL(configuracionRepositorios) }
    override val repositorioValoresGruposEdad by lazy<RepositorioValoresGruposEdad> { RepositorioValoresGruposEdadSQL(configuracionRepositorios) }
    override val repositorioFondos by lazy<RepositorioFondos> { RepositorioFondosSQL(configuracionRepositorios) }
    override val repositorioMonedas by lazy<RepositorioMonedas> { RepositorioMonedasSQL(configuracionRepositorios) }
    override val repositorioAccesos by lazy<RepositorioAccesos> { RepositorioAccesosSQL(configuracionRepositorios) }
    override val repositorioEntradas by lazy<RepositorioEntradas> { RepositorioEntradasSQL(configuracionRepositorios) }
    override val repositorioSkus by lazy<RepositorioSkus> { RepositorioSkusSQL(configuracionRepositorios) }
    override val repositorioCategoriasSkus by lazy<RepositorioCategoriasSkus> { RepositorioCategoriasSkusSQL(configuracionRepositorios) }
    override val repositorioPaquetes by lazy<RepositorioPaquetes> { RepositorioPaquetesSQL(configuracionRepositorios) }
    override val repositorioLibroDePrecios by lazy<RepositorioLibrosDePrecios> { RepositorioLibrosDePreciosSQL(configuracionRepositorios) }
    override val repositorioLibroDeProhibiciones by lazy<RepositorioLibrosDeProhibiciones> { RepositorioLibrosDeProhibicionesSQL(configuracionRepositorios) }
    override val repositorioLibrosSegunReglas by lazy<RepositorioLibrosSegunReglas> { RepositorioLibrosSegunReglasSQL(configuracionRepositorios) }
    override val repositorioLibrosSegunReglasCompleto by lazy<RepositorioLibrosSegunReglasCompleto> { RepositorioLibrosSegunReglasCompletoSQL(configuracionRepositorios) }
    override val repositorioCompras by lazy<RepositorioCompras> { RepositorioComprasSQL(configuracionRepositorios) }
    override val repositorioConsumibleEnPuntoDeVenta by lazy<RepositorioConsumibleEnPuntoDeVenta> { RepositorioConsumibleEnPuntoDeVentaSQL(configuracionRepositorios) }
    override val repositorioReservas by lazy<RepositorioReservas> { RepositorioReservasSQL(configuracionRepositorios) }
    override val repositorioDeSesionDeManilla by lazy<RepositorioDeSesionDeManilla> { RepositorioDeSesionDeManillaSQL(configuracionRepositorios) }
    override val repositorioOrdenes by lazy<RepositorioOrdenes> { RepositorioOrdenesSQL(configuracionRepositorios) }
    override val repositorioOrdenesDeUnaSesionDeManilla by lazy<RepositorioOrdenesDeUnaSesionDeManilla> { RepositorioOrdenesDeUnaSesionDeManillaSQL(configuracionRepositorios) }
    override val repositorioConteoUbicaciones by lazy<RepositorioConteosUbicaciones> { RepositorioConteosUbicacionesSQL(configuracionRepositorios) }

    // Repositorios que no incializan nada
    override val repositorioComprasDeUnaPersona by lazy<RepositorioComprasDeUnaPersona> { RepositorioComprasDeUnaPersonaSQL(configuracionRepositorios) }
    override val repositorioPersonasDeUnaCompra by lazy<RepositorioPersonasDeUnaCompra> { RepositorioPersonasDeUnaCompraSQL(configuracionRepositorios) }
    override val repositorioFondosEnPuntoDeVenta by lazy<RepositorioFondosEnPuntoDeVenta> { RepositorioFondosEnPuntoDeVentaSQL(configuracionRepositorios) }
    override val repositorioCreditosDeUnaPersona by lazy<RepositorioCreditosDeUnaPersona> { RepositorioCreditosDeUnaPersonaSQL(configuracionRepositorios) }

    // Deben estar en este orden para inicializar las tablas en el orden correcto
    override val configuradoresRepositoriosHijo by lazy<List<CreadorRepositorio<*>>> {
        listOf<CreadorRepositorio<*>>(
                repositorioLlavesNFC,
                repositorioRoles,
                repositorioUsuarios,
                repositorioUbicaciones,
                repositorioUbicacionesContabilizables,
                repositorioPersonas,
                repositorioCampoDePersonas,
                repositorioValoresGruposEdad,
                repositorioGrupoClientes,
                repositorioImpuestos,
                repositorioFondos,
                repositorioAccesos,
                repositorioEntradas,
                repositorioCategoriasSkus,
                repositorioSkus,
                repositorioMonedas,
                repositorioPaquetes,
                repositorioLibroDePrecios,
                repositorioLibroDeProhibiciones,
                repositorioLibrosSegunReglas,
                repositorioLibrosSegunReglasCompleto,
                repositorioCompras,
                repositorioConsumibleEnPuntoDeVenta,
                repositorioReservas,
                repositorioDeSesionDeManilla,
                repositorioOrdenes,
                repositorioConteoUbicaciones
                                     )
    }
}

private class ConfiguracionPersistenciaSQLiteAndroid(override val fuenteConexionesEsquemaPrincipal: ConnectionSource)
    : ConfiguracionRepositorios(DependenciasBDAndroid.NOMBRE_BASEDATOS)
{
    override val mapeadorCodigosError: MapeadorCodigosSQL = MapeadorCodigosSQLite()

    override fun crearEsquemaSiNoExiste(nombreDeEsquema: String)
    {
    }

    override fun eliminarEsquema(nombreDeEsquema: String)
    {
    }

    override fun crearFuenteDeConexionesParaNombreDeEsquema(nombreDeEsquema: String): ConnectionSource
    {
        return fuenteConexionesEsquemaPrincipal
    }

    override fun limpiarRecursos()
    {
    }
}*/
