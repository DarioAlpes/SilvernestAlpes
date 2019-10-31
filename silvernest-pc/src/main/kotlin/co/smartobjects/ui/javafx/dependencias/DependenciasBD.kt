package co.smartobjects.ui.javafx.dependencias

import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.clientes.RepositorioClientes
import co.smartobjects.persistencia.clientes.RepositorioClientesSQL
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFCSQL
import co.smartobjects.persistencia.fondos.RepositorioFondos
import co.smartobjects.persistencia.fondos.RepositorioFondosSQL
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesos
import co.smartobjects.persistencia.fondos.acceso.RepositorioAccesosSQL
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradas
import co.smartobjects.persistencia.fondos.acceso.RepositorioEntradasSQL
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkus
import co.smartobjects.persistencia.fondos.categoriaskus.RepositorioCategoriasSkusSQL
import co.smartobjects.persistencia.fondos.libros.*
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedas
import co.smartobjects.persistencia.fondos.monedas.RepositorioMonedasSQL
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetes
import co.smartobjects.persistencia.fondos.paquete.RepositorioPaquetesSQL
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientesSQL
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestos
import co.smartobjects.persistencia.fondos.precios.impuestos.RepositorioImpuestosSQL
import co.smartobjects.persistencia.fondos.skus.RepositorioSkus
import co.smartobjects.persistencia.fondos.skus.RepositorioSkusSQL
import co.smartobjects.persistencia.operativas.compras.*
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenes
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesDeUnaSesionDeManilla
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesDeUnaSesionDeManillaSQL
import co.smartobjects.persistencia.operativas.ordenes.RepositorioOrdenesSQL
import co.smartobjects.persistencia.operativas.reservas.RepositorioDeSesionDeManilla
import co.smartobjects.persistencia.operativas.reservas.RepositorioDeSesionDeManillaSQL
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservas
import co.smartobjects.persistencia.operativas.reservas.RepositorioReservasSQL
import co.smartobjects.persistencia.personas.RepositorioPersonas
import co.smartobjects.persistencia.personas.RepositorioPersonasSQL
import co.smartobjects.persistencia.personas.camposdepersona.RepositorioCampoDePersonasSQL
import co.smartobjects.persistencia.personas.camposdepersona.RepositorioCamposDePersonas
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdad
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdadSQL
import co.smartobjects.persistencia.sqlite.ConfiguracionPersistenciaSQLiteEnDisco
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicacionesSQL
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioConsumibleEnPuntoDeVenta
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioConsumibleEnPuntoDeVentaSQL
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioFondosEnPuntoDeVenta
import co.smartobjects.persistencia.ubicaciones.consumibles.RepositorioFondosEnPuntoDeVentaSQL
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioConteosUbicaciones
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioConteosUbicacionesSQL
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioUbicacionesContabilizables
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioUbicacionesContabilizablesSQL
import co.smartobjects.persistencia.usuarios.Hasher
import co.smartobjects.persistencia.usuarios.RepositorioUsuarios
import co.smartobjects.persistencia.usuarios.RepositorioUsuariosSQL
import co.smartobjects.persistencia.usuarios.roles.RepositorioRoles
import co.smartobjects.persistencia.usuarios.roles.RepositorioRolesSQL
import java.io.File


internal interface DependenciasBD
{
    val configuracionRepositorios: ConfiguracionRepositorios

    val repositorioClientes: RepositorioClientes
    val repositorioLlavesNFC: RepositorioLlavesNFC
    val repositorioRoles: RepositorioRoles
    val repositorioUsuarios: RepositorioUsuarios
    val repositorioUbicaciones: RepositorioUbicaciones
    val repositorioUbicacionesContabilizables: RepositorioUbicacionesContabilizables
    val repositorioPersonas: RepositorioPersonas
    val repositorioImpuestos: RepositorioImpuestos
    val repositorioGrupoClientes: RepositorioGrupoClientes
    val repositorioCampoDePersonas: RepositorioCamposDePersonas
    val repositorioValoresGruposEdad: RepositorioValoresGruposEdad
    val repositorioFondos: RepositorioFondos
    val repositorioMonedas: RepositorioMonedas
    val repositorioAccesos: RepositorioAccesos
    val repositorioEntradas: RepositorioEntradas
    val repositorioSkus: RepositorioSkus
    val repositorioCategoriasSkus: RepositorioCategoriasSkus
    val repositorioPaquetes: RepositorioPaquetes
    val repositorioLibroDePrecios: RepositorioLibrosDePrecios
    val repositorioLibroDeProhibiciones: RepositorioLibrosDeProhibiciones
    val repositorioLibrosSegunReglas: RepositorioLibrosSegunReglas
    val repositorioLibrosSegunReglasCompleto: RepositorioLibrosSegunReglasCompleto
    val repositorioCompras: RepositorioCompras
    val repositorioConsumibleEnPuntoDeVenta: RepositorioConsumibleEnPuntoDeVenta
    val repositorioReservas: RepositorioReservas
    val repositorioDeSesionDeManilla: RepositorioDeSesionDeManilla
    val repositorioOrdenes: RepositorioOrdenes
    val repositorioOrdenesDeUnaSesionDeManilla: RepositorioOrdenesDeUnaSesionDeManilla
    val repositorioConteoUbicaciones: RepositorioConteosUbicaciones

    val repositorioComprasDeUnaPersona: RepositorioComprasDeUnaPersona
    val repositorioPersonasDeUnaCompra: RepositorioPersonasDeUnaCompra
    val repositorioFondosEnPuntoDeVenta: RepositorioFondosEnPuntoDeVenta
    val repositorioCreditosDeUnaPersona: RepositorioCreditosDeUnaPersona

    val configuradoresRepositoriosHijo: List<CreadorRepositorio<*>>

    fun inicializarTablasNecesariasCliente(id: Long)
}


internal object DependenciasBDImpl : DependenciasBD
{
    override val configuracionRepositorios: ConfiguracionRepositorios by lazy {
        ConfiguracionPersistenciaSQLiteEnDisco(File(System.getProperty("user.dir"), "bd"), "BDSilvernest")
    }

    override val repositorioClientes: RepositorioClientes by lazy { RepositorioClientesSQL(configuracionRepositorios) }
    override val repositorioLlavesNFC: RepositorioLlavesNFC by lazy { RepositorioLlavesNFCSQL(configuracionRepositorios) }
    override val repositorioRoles: RepositorioRoles by lazy { RepositorioRolesSQL(configuracionRepositorios) }
    override val repositorioUsuarios: RepositorioUsuarios by lazy {
        RepositorioUsuariosSQL(configuracionRepositorios, object : Hasher
        {
            override fun calcularHash(entrada: CharArray): String = String(entrada)
        })
    }
    override val repositorioUbicaciones: RepositorioUbicaciones by lazy { RepositorioUbicacionesSQL(configuracionRepositorios) }
    override val repositorioUbicacionesContabilizables: RepositorioUbicacionesContabilizables by lazy { RepositorioUbicacionesContabilizablesSQL(configuracionRepositorios) }
    override val repositorioPersonas: RepositorioPersonas by lazy { RepositorioPersonasSQL(configuracionRepositorios) }
    override val repositorioImpuestos: RepositorioImpuestos by lazy { RepositorioImpuestosSQL(configuracionRepositorios) }
    override val repositorioGrupoClientes: RepositorioGrupoClientes by lazy { RepositorioGrupoClientesSQL(configuracionRepositorios) }
    override val repositorioCampoDePersonas: RepositorioCamposDePersonas by lazy { RepositorioCampoDePersonasSQL(configuracionRepositorios) }
    override val repositorioValoresGruposEdad: RepositorioValoresGruposEdad by lazy { RepositorioValoresGruposEdadSQL(configuracionRepositorios) }
    override val repositorioFondos: RepositorioFondos by lazy { RepositorioFondosSQL(configuracionRepositorios) }
    override val repositorioMonedas: RepositorioMonedas by lazy { RepositorioMonedasSQL(configuracionRepositorios) }
    override val repositorioAccesos: RepositorioAccesos by lazy { RepositorioAccesosSQL(configuracionRepositorios) }
    override val repositorioEntradas: RepositorioEntradas by lazy { RepositorioEntradasSQL(configuracionRepositorios) }
    override val repositorioSkus: RepositorioSkus by lazy { RepositorioSkusSQL(configuracionRepositorios) }
    override val repositorioCategoriasSkus: RepositorioCategoriasSkus by lazy { RepositorioCategoriasSkusSQL(configuracionRepositorios) }
    override val repositorioPaquetes: RepositorioPaquetes by lazy { RepositorioPaquetesSQL(configuracionRepositorios) }
    override val repositorioLibroDePrecios: RepositorioLibrosDePrecios by lazy { RepositorioLibrosDePreciosSQL(configuracionRepositorios) }
    override val repositorioLibroDeProhibiciones: RepositorioLibrosDeProhibiciones by lazy { RepositorioLibrosDeProhibicionesSQL(configuracionRepositorios) }
    override val repositorioLibrosSegunReglas: RepositorioLibrosSegunReglas by lazy { RepositorioLibrosSegunReglasSQL(configuracionRepositorios) }
    override val repositorioLibrosSegunReglasCompleto: RepositorioLibrosSegunReglasCompleto by lazy { RepositorioLibrosSegunReglasCompletoSQL(configuracionRepositorios) }
    override val repositorioCompras: RepositorioCompras by lazy { RepositorioComprasSQL(configuracionRepositorios) }
    override val repositorioConsumibleEnPuntoDeVenta: RepositorioConsumibleEnPuntoDeVenta by lazy { RepositorioConsumibleEnPuntoDeVentaSQL(configuracionRepositorios) }
    override val repositorioReservas: RepositorioReservas by lazy { RepositorioReservasSQL(configuracionRepositorios) }
    override val repositorioDeSesionDeManilla: RepositorioDeSesionDeManilla by lazy { RepositorioDeSesionDeManillaSQL(configuracionRepositorios) }
    override val repositorioOrdenes: RepositorioOrdenes by lazy { RepositorioOrdenesSQL(configuracionRepositorios) }
    override val repositorioOrdenesDeUnaSesionDeManilla: RepositorioOrdenesDeUnaSesionDeManilla by lazy { RepositorioOrdenesDeUnaSesionDeManillaSQL(configuracionRepositorios) }
    override val repositorioConteoUbicaciones: RepositorioConteosUbicaciones by lazy { RepositorioConteosUbicacionesSQL(configuracionRepositorios) }

    // Repositorios que no incializan nada
    override val repositorioComprasDeUnaPersona: RepositorioComprasDeUnaPersona by lazy { RepositorioComprasDeUnaPersonaSQL(configuracionRepositorios) }
    override val repositorioPersonasDeUnaCompra: RepositorioPersonasDeUnaCompra by lazy { RepositorioPersonasDeUnaCompraSQL(configuracionRepositorios) }
    override val repositorioFondosEnPuntoDeVenta: RepositorioFondosEnPuntoDeVenta by lazy { RepositorioFondosEnPuntoDeVentaSQL(configuracionRepositorios) }
    override val repositorioCreditosDeUnaPersona: RepositorioCreditosDeUnaPersona by lazy { RepositorioCreditosDeUnaPersonaSQL(configuracionRepositorios) }

    // Deben estar en este orden para inicializar las tablas en el orden correcto
    override val configuradoresRepositoriosHijo: List<CreadorRepositorio<*>> by lazy {
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

    override fun inicializarTablasNecesariasCliente(id: Long)
    {
        repositorioClientes.crearTablaSiNoExiste()

        repositorioClientes.inicializarConexionAEsquemaDeSerNecesario(id)

        configuradoresRepositoriosHijo.forEach {
            it.inicializarParaCliente(id)
        }
    }
}