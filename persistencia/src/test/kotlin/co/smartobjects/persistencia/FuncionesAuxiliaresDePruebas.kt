package co.smartobjects.persistencia

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAO
import co.smartobjects.persistencia.basederepositorios.ParametrosParaDAOEntidadDeCliente
import co.smartobjects.persistencia.basederepositorios.obtenerDAOEntidadGlobal
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.acceso.AccesoDAO
import co.smartobjects.persistencia.fondos.categoriaskus.CategoriaSkuDAO
import co.smartobjects.persistencia.fondos.libros.PrecioDeFondoDAO
import co.smartobjects.persistencia.fondos.libros.ProhibicionDAO
import co.smartobjects.persistencia.fondos.libros.ReglaDAO
import co.smartobjects.persistencia.fondos.paquete.FondoPaqueteDAO
import co.smartobjects.persistencia.fondos.precios.SegmentoClientesDAO
import co.smartobjects.persistencia.fondos.precios.gruposclientes.GrupoClientesDAO
import co.smartobjects.persistencia.fondos.skus.SkuDAO
import co.smartobjects.persistencia.operativas.compras.CompraDAO
import co.smartobjects.persistencia.operativas.compras.CreditoFondoDAO
import co.smartobjects.persistencia.operativas.compras.CreditoPaqueteDAO
import co.smartobjects.persistencia.operativas.compras.PagoDAO
import co.smartobjects.persistencia.operativas.ordenes.OrdenDAO
import co.smartobjects.persistencia.operativas.ordenes.TransaccionDAO
import co.smartobjects.persistencia.operativas.reservas.CreditoEnSesionDeManillaDAO
import co.smartobjects.persistencia.operativas.reservas.SesionDeManillaDAO
import co.smartobjects.persistencia.personas.relacionesdepersonas.RelacionPersonaDAO
import co.smartobjects.persistencia.ubicaciones.contabilizables.ConteoUbicacionDAO
import co.smartobjects.persistencia.ubicaciones.contabilizables.UbicacionContabilizableDAO
import co.smartobjects.persistencia.usuarios.RolUsuarioDAO
import co.smartobjects.persistencia.usuarios.UsuarioDAO
import co.smartobjects.persistencia.usuarios.roles.PermisoBackDAO
import co.smartobjects.persistencia.usuarios.roles.RolDAO
import co.smartobjects.persistencia.usuariosglobales.UsuarioGlobalDAO
import java.lang.RuntimeException

internal inline fun <reified T> existeAlguno(
        parametrosParaDAO: ParametrosParaDAO<T, *>,
        predicadoFiltro: (T) -> Boolean)
        : Boolean
{
    return parametrosParaDAO.dao.queryBuilder().query().any { predicadoFiltro(it) }
}

private inline fun <reified Tipo, reified TipoId> existeAlgunoSegunTipoId(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        nombreTabla: String,
        predicadoFiltro: (Tipo) -> Boolean = { true })
        : Boolean
{
    return existeAlguno(
            ParametrosParaDAOEntidadDeCliente<Tipo, TipoId?>(
                    configuracionRepositorios,
                    nombreTabla, Tipo::class.java)[idCliente],
            predicadoFiltro
                       )
}

private inline fun <reified Tipo> existeAlgunoIdLong(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        nombreTabla: String,
        predicadoFiltro: (Tipo) -> Boolean = { true })
        : Boolean = existeAlgunoSegunTipoId<Tipo, Long>(idCliente, configuracionRepositorios, nombreTabla, predicadoFiltro)

private inline fun <reified Tipo> existeAlgunoIdString(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        nombreTabla: String,
        predicadoFiltro: (Tipo) -> Boolean = { true })
        : Boolean = existeAlgunoSegunTipoId<Tipo, String>(idCliente, configuracionRepositorios, nombreTabla, predicadoFiltro)

internal fun existeAlgunRolDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (RolDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdString(idCliente, configuracionRepositorios, RolDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunUsuarioDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (UsuarioDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdString(idCliente, configuracionRepositorios, UsuarioDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunPermisoBackDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (PermisoBackDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, PermisoBackDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunRolUsuarioDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (RolUsuarioDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, RolUsuarioDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunGrupoClientesDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (GrupoClientesDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, GrupoClientesDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunFondoDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (FondoDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, FondoDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunSkuDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (SkuDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, SkuDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunAccesoDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (AccesoDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, AccesoDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunaCategoriaSkuDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (CategoriaSkuDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, CategoriaSkuDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunSegmentoClientesDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (SegmentoClientesDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, SegmentoClientesDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunFondoPaqueteDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (FondoPaqueteDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, FondoPaqueteDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunaCompraDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (CompraDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdString(idCliente, configuracionRepositorios, CompraDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunCreditoPaqueteDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (CreditoPaqueteDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, CreditoPaqueteDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunCreditoFondoDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (CreditoFondoDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, CreditoFondoDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunPagoDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (PagoDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, PagoDAO.TABLA, predicadoFiltro)
}

internal fun existeAlgunPrecioDeFondoDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : Boolean
{
    return existeAlgunoIdLong<PrecioDeFondoDAO>(idCliente, configuracionRepositorios, PrecioDeFondoDAO.TABLA)
}

internal fun existeAlgunaProhibicionDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : Boolean
{
    return existeAlgunoIdLong<ProhibicionDAO>(idCliente, configuracionRepositorios, ProhibicionDAO.TABLA)
}

internal fun existeAlgunaReglaDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : Boolean
{
    return existeAlgunoIdLong<ReglaDAO>(idCliente, configuracionRepositorios, ReglaDAO.TABLA)
}

internal fun listarTodasLasReglas(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : ReglaDAO.ReglasConvertidas
{
    val parametrosDao = ParametrosParaDAOEntidadDeCliente<ReglaDAO, Long?>(configuracionRepositorios, ReglaDAO.TABLA, ReglaDAO::class.java)[idCliente]

    return ReglaDAO.convertirAEntidadesDeNegocio(parametrosDao.dao.queryForAll())
}


internal fun desactivarUsuario(
        configuracionRepositorios: ConfiguracionRepositorios,
        idCliente: Long?,
        idUsuario: String
                              )
{
    val parametrosDao =
            if (idCliente != null)
            {
                ParametrosParaDAOEntidadDeCliente<UsuarioDAO, String?>(configuracionRepositorios, UsuarioDAO.TABLA, UsuarioDAO::class.java)[idCliente]
            }
            else
            {
                ParametrosParaDAO(UsuarioGlobalDAO.TABLA, obtenerDAOEntidadGlobal<UsuarioGlobalDAO, String>(configuracionRepositorios))
            }

    val updateBuilder =
            parametrosDao.dao
                .updateBuilder().apply {
                    val columnaId = if (idCliente != null) UsuarioDAO.COLUMNA_ID else UsuarioGlobalDAO.COLUMNA_ID
                    where().eq(columnaId, idUsuario)
                }

    val columna = if (idCliente != null) UsuarioDAO.COLUMNA_ACTIVO else UsuarioGlobalDAO.COLUMNA_ACTIVO
    updateBuilder.updateColumnValue(columna, false)
    val numeroFilasActualizadas = updateBuilder.update()
    if (numeroFilasActualizadas != 1)
    {
        throw RuntimeException("No se pudo desactivar el usuario durante la prueba")
    }
}

internal fun marcarCreditoComoConsumido(configuracionRepositorios: ConfiguracionRepositorios, idCliente: Long, idCredito: Long)
{
    val parametrosDao = ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long?>(
            configuracionRepositorios,
            CreditoFondoDAO.TABLA,
            CreditoFondoDAO::class.java)[idCliente]

    val updateBuilder = parametrosDao.dao.updateBuilder().apply { where().idEq(idCredito) }

    updateBuilder.updateColumnValue(CreditoFondoDAO.COLUMNA_CONSUMIDO, true)
    val numeroFilasActualizadas = updateBuilder.update()
    if (numeroFilasActualizadas != 1)
    {
        throw RuntimeException("No se pudo desactivar el usuario durante la prueba")
    }
}

internal fun darCreditosSegunIds(configuracionRepositorios: ConfiguracionRepositorios, idCliente: Long, idsCreditos: Set<Long>)
        : List<CreditoFondo>
{
    if (idsCreditos.isEmpty())
    {
        throw Exception("Debe haber al menos 1 id de crédito")
    }

    val parametrosDao = ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long?>(
            configuracionRepositorios,
            CreditoFondoDAO.TABLA,
            CreditoFondoDAO::class.java)[idCliente]

    val queryBuilder = parametrosDao.dao.queryBuilder().apply {
        where().`in`(CreditoFondoDAO.COLUMNA_ID, idsCreditos)
    }
    val creditosEncontrados = queryBuilder.query()
    if (creditosEncontrados.isEmpty())
    {
        throw RuntimeException("No se pudo desactivar el usuario durante la prueba")
    }

    return creditosEncontrados.map { it.aEntidadDeNegocio(idCliente) }
}

internal fun existeAlgunaSesionDeManillaDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : Boolean
{
    return existeAlgunoIdLong<SesionDeManillaDAO>(idCliente, configuracionRepositorios, SesionDeManillaDAO.TABLA)
}

internal fun contarSesionDeManillaDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : Long
{
    return ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long?>(configuracionRepositorios, SesionDeManillaDAO.TABLA, SesionDeManillaDAO::class.java)[idCliente]
        .dao.countOf()
}

internal fun existeAlgunCreditoEnSesionManillaDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : Boolean
{
    return existeAlgunoIdLong<CreditoEnSesionDeManillaDAO>(idCliente, configuracionRepositorios, CreditoEnSesionDeManillaDAO.TABLA)
}

internal fun contarCreditoEnSesionDeManillaDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : Long
{
    return ParametrosParaDAOEntidadDeCliente<CreditoEnSesionDeManillaDAO, Long?>(configuracionRepositorios, CreditoEnSesionDeManillaDAO.TABLA, CreditoEnSesionDeManillaDAO::class.java)[idCliente]
        .dao.countOf()
}

internal fun buscarSesionDeManillaDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        idDeSesionDeManilla: Long): SesionDeManillaDAO?
{
    val parametrosDao = ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long?>(configuracionRepositorios, SesionDeManillaDAO.TABLA, SesionDeManillaDAO::class.java)[idCliente]

    return parametrosDao.dao.queryForId(idDeSesionDeManilla)
}

internal fun borrarSesionDeManillaDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        idDeSesionDeManilla: Long)
{
    val parametrosDao =
            ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long?>(
                    configuracionRepositorios,
                    SesionDeManillaDAO.TABLA,
                    SesionDeManillaDAO::class.java)[idCliente]

    parametrosDao.dao.deleteById(idDeSesionDeManilla)
}

internal fun listarTodasLasSesionDeManillaDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : List<SesionDeManillaDAO>
{
    return ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long?>(configuracionRepositorios, SesionDeManillaDAO.TABLA, SesionDeManillaDAO::class.java)[idCliente]
        .dao.queryForAll()
}

internal fun listarTodasLasOrdenesDAOSegunIdTransaccion(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        idTransaccion: String)
        : List<OrdenDAO>
{
    return ParametrosParaDAOEntidadDeCliente<OrdenDAO, Long?>(configuracionRepositorios, OrdenDAO.TABLA, OrdenDAO::class.java)[idCliente]
        .dao.queryForEq(OrdenDAO.COLUMNA_ID_TRANSACCION, idTransaccion)
}

internal fun listarTodasLasOrdenesDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : List<OrdenDAO>
{
    return ParametrosParaDAOEntidadDeCliente<OrdenDAO, Long?>(configuracionRepositorios, OrdenDAO.TABLA, OrdenDAO::class.java)[idCliente]
        .dao.queryForAll()
}

internal fun listarTodasLasTransaccionesDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : List<TransaccionDAO>
{
    return ParametrosParaDAOEntidadDeCliente<TransaccionDAO, Long?>(configuracionRepositorios, TransaccionDAO.TABLA, TransaccionDAO::class.java)[idCliente]
        .dao.queryForAll()
}

internal fun borrarTodasLasOrdenesDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
{
    ParametrosParaDAOEntidadDeCliente<OrdenDAO, Long?>(configuracionRepositorios, OrdenDAO.TABLA, OrdenDAO::class.java)[idCliente]
        .dao.deleteBuilder().delete()
}

internal fun existeAlgunaTransaccionDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios,
        predicadoFiltro: (TransaccionDAO) -> Boolean = { true })
        : Boolean
{
    return existeAlgunoIdLong(idCliente, configuracionRepositorios, TransaccionDAO.TABLA, predicadoFiltro)
}


internal fun listarTodasLasUbicacionContabilizableDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : List<UbicacionContabilizableDAO>
{
    return ParametrosParaDAOEntidadDeCliente<UbicacionContabilizableDAO, Long?>(
            configuracionRepositorios,
            UbicacionContabilizableDAO.TABLA,
            UbicacionContabilizableDAO::class.java
                                                                               )[idCliente]
        .dao.queryForAll()
}

internal fun listarTodasLosConteosUbicacionDAO(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : List<ConteoUbicacionDAO>
{
    return ParametrosParaDAOEntidadDeCliente<ConteoUbicacionDAO, Long?>(
            configuracionRepositorios,
            ConteoUbicacionDAO.TABLA,
            ConteoUbicacionDAO::class.java
                                                                       )[idCliente]
        .dao.queryForAll()
}

internal fun listarTodasLasRelacionesPersonas(
        idCliente: Long,
        configuracionRepositorios: ConfiguracionRepositorios)
        : List<RelacionPersonaDAO>
{
    return ParametrosParaDAOEntidadDeCliente<RelacionPersonaDAO, Long?>(
            configuracionRepositorios,
            RelacionPersonaDAO.TABLA,
            RelacionPersonaDAO::class.java)[idCliente]
        .dao.queryForAll()
}