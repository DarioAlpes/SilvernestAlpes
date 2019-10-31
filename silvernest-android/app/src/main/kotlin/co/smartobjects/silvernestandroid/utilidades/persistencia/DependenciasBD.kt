package co.smartobjects.silvernestandroid.utilidades.persistencia

import co.smartobjects.persistencia.clientes.llavesnfc.RepositorioLlavesNFC
import co.smartobjects.persistencia.ubicaciones.RepositorioUbicaciones
import co.smartobjects.persistencia.ubicaciones.contabilizables.RepositorioUbicacionesContabilizables


internal interface DependenciasBD
{
//    val configuracionRepositorios: ConfiguracionRepositorios

    //    val repositorioClientes: RepositorioClientes
    val repositorioLlavesNFC: RepositorioLlavesNFC
    //    val repositorioRoles: RepositorioRoles
//    val repositorioUsuarios: RepositorioUsuarios
    val repositorioUbicaciones: RepositorioUbicaciones
    val repositorioUbicacionesContabilizables: RepositorioUbicacionesContabilizables
//    val repositorioPersonas: RepositorioPersonas
//    val repositorioImpuestos: RepositorioImpuestos
//    val repositorioGrupoClientes: RepositorioGrupoClientes
//    val repositorioCampoDePersonas: RepositorioCamposDePersonas
//    val repositorioValoresGruposEdad: RepositorioValoresGruposEdad
//    val repositorioFondos: RepositorioFondos
//    val repositorioMonedas: RepositorioMonedas
//    val repositorioAccesos: RepositorioAccesos
//    val repositorioEntradas: RepositorioEntradas
//    val repositorioSkus: RepositorioSkus
//    val repositorioCategoriasSkus: RepositorioCategoriasSkus
//    val repositorioPaquetes: RepositorioPaquetes
//    val repositorioLibroDePrecios: RepositorioLibrosDePrecios
//    val repositorioLibroDeProhibiciones: RepositorioLibrosDeProhibiciones
//    val repositorioLibrosSegunReglas: RepositorioLibrosSegunReglas
//    val repositorioLibrosSegunReglasCompleto: RepositorioLibrosSegunReglasCompleto
//    val repositorioCompras: RepositorioCompras
//    val repositorioConsumibleEnPuntoDeVenta: RepositorioConsumibleEnPuntoDeVenta
//    val repositorioReservas: RepositorioReservas
//    val repositorioDeSesionDeManilla: RepositorioDeSesionDeManilla
//    val repositorioOrdenes: RepositorioOrdenes
//    val repositorioOrdenesDeUnaSesionDeManilla: RepositorioOrdenesDeUnaSesionDeManilla
//    val repositorioConteoUbicaciones: RepositorioConteosUbicaciones

//    val repositorioComprasDeUnaPersona: RepositorioComprasDeUnaPersona
//    val repositorioPersonasDeUnaCompra: RepositorioPersonasDeUnaCompra
//    val repositorioFondosEnPuntoDeVenta: RepositorioFondosEnPuntoDeVenta
//    val repositorioCreditosDeUnaPersona: RepositorioCreditosDeUnaPersona

//    val configuradoresRepositoriosHijo: List<CreadorRepositorio<*>>
}