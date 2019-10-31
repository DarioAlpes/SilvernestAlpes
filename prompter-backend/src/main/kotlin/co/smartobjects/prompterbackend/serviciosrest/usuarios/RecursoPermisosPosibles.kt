package co.smartobjects.prompterbackend.serviciosrest.usuarios

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.entidades.usuarios.Rol
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.clientes.RecursoLlavesNFC
import co.smartobjects.prompterbackend.serviciosrest.fondos.*
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosDePrecios
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosDeProhibiciones
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosSegunReglas
import co.smartobjects.prompterbackend.serviciosrest.fondos.libros.RecursoLibrosSegunReglasCompleto
import co.smartobjects.prompterbackend.serviciosrest.fondos.precios.RecursoGruposClientes
import co.smartobjects.prompterbackend.serviciosrest.fondos.precios.RecursoImpuestos
import co.smartobjects.prompterbackend.serviciosrest.operativas.compras.RecursoCompras
import co.smartobjects.prompterbackend.serviciosrest.operativas.ordenes.RecursoLotesDeOrdenes
import co.smartobjects.prompterbackend.serviciosrest.operativas.ordenes.RecursoOrdenes
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.RecursoReservas
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla.RecursoOrdenesDeUnaSesionDeManilla
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla.RecursoPersonaPorIdSesionManilla
import co.smartobjects.prompterbackend.serviciosrest.operativas.reservas.sesionesdemanilla.RecursoSesionesDeManilla
import co.smartobjects.prompterbackend.serviciosrest.personas.*
import co.smartobjects.prompterbackend.serviciosrest.personas.compras.RecursoComprasDeUnaPersona
import co.smartobjects.prompterbackend.serviciosrest.personas.creditos.RecursoCreditosDeUnaPersona
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.RecursoListarTodosDeCliente
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.RecursoUbicaciones
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles.RecursoConsumiblesEnPuntoDeVenta
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.consumibles.RecursoFondosEnPuntoDeVenta
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables.RecursoConteosEnUbicacion
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables.RecursoConteosUbicaciones
import co.smartobjects.prompterbackend.serviciosrest.ubicaciones.contabilizables.RecursoUbicacionesContabilizables
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import co.smartobjects.red.modelos.usuarios.RolDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoPermisosPosibles(override val idCliente: Long, override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoListarTodosDeCliente<PermisoBack, PermisoBackDTO>
{
    companion object
    {
        const val RUTA = "possible-permissions"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Permisos Posibles"
        private val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        private val PERMISOS_VALIDOS = listOf(
                INFORMACION_PERMISO_LISTAR,

                RecursoRoles.INFORMACION_PERMISO_CREACION,
                RecursoRoles.INFORMACION_PERMISO_LISTAR,
                RecursoRoles.INFORMACION_PERMISO_CONSULTAR,
                RecursoRoles.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoRoles.INFORMACION_PERMISO_ELIMINACION,

                RecursoUsuarios.INFORMACION_PERMISO_CREACION,
                RecursoUsuarios.INFORMACION_PERMISO_LISTAR,
                RecursoUsuarios.INFORMACION_PERMISO_CONSULTAR,
                RecursoUsuarios.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoUsuarios.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoUsuarios.INFORMACION_PERMISO_ELIMINACION,

                RecursoLlavesNFC.INFORMACION_PERMISO_CREACION,
                RecursoLlavesNFC.INFORMACION_PERMISO_CONSULTAR,
                RecursoLlavesNFC.INFORMACION_PERMISO_ELIMINACION,

                RecursoUbicaciones.INFORMACION_PERMISO_CREACION,
                RecursoUbicaciones.INFORMACION_PERMISO_LISTAR,
                RecursoUbicaciones.INFORMACION_PERMISO_CONSULTAR,
                RecursoUbicaciones.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoUbicaciones.INFORMACION_PERMISO_ELIMINACION,

                RecursoUbicacionesContabilizables.INFORMACION_PERMISO_LISTAR,
                RecursoUbicacionesContabilizables.INFORMACION_PERMISO_ACTUALIZACION_TODOS,

                RecursoConteosUbicaciones.INFORMACION_PERMISO_LISTAR,
                RecursoConteosUbicaciones.INFORMACION_PERMISO_ELIMINACION,

                RecursoConteosEnUbicacion.INFORMACION_PERMISO_CREACION,

                RecursoCamposDePersona.INFORMACION_PERMISO_LISTAR,
                RecursoCamposDePersona.INFORMACION_PERMISO_CONSULTAR,
                RecursoCamposDePersona.INFORMACION_PERMISO_ACTUALIZACION,

                RecursoPersonas.INFORMACION_PERMISO_CREACION,
                RecursoPersonas.INFORMACION_PERMISO_LISTAR,
                RecursoPersonas.INFORMACION_PERMISO_CONSULTAR,
                RecursoPersonas.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoPersonas.INFORMACION_PERMISO_ELIMINACION,

                RecursoPersonaPorDocumento.INFORMACION_PERMISO_CONSULTAR,

                RecursoValoresGrupoEdad.INFORMACION_PERMISO_CREACION,
                RecursoValoresGrupoEdad.INFORMACION_PERMISO_LISTAR,
                RecursoValoresGrupoEdad.INFORMACION_PERMISO_CONSULTAR,
                RecursoValoresGrupoEdad.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoValoresGrupoEdad.INFORMACION_PERMISO_ELIMINACION,

                RecursoGruposClientes.INFORMACION_PERMISO_CREACION,
                RecursoGruposClientes.INFORMACION_PERMISO_LISTAR,
                RecursoGruposClientes.INFORMACION_PERMISO_CONSULTAR,
                RecursoGruposClientes.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoGruposClientes.INFORMACION_PERMISO_ELIMINACION,

                RecursoImpuestos.INFORMACION_PERMISO_CREACION,
                RecursoImpuestos.INFORMACION_PERMISO_LISTAR,
                RecursoImpuestos.INFORMACION_PERMISO_CONSULTAR,
                RecursoImpuestos.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoImpuestos.INFORMACION_PERMISO_ELIMINACION,

                RecursoFondos.INFORMACION_PERMISO_LISTAR,
                RecursoFondos.INFORMACION_PERMISO_CONSULTAR,
                RecursoFondos.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoFondos.INFORMACION_PERMISO_ELIMINACION,

                RecursoAccesos.INFORMACION_PERMISO_CREACION,
                RecursoAccesos.INFORMACION_PERMISO_LISTAR,
                RecursoAccesos.INFORMACION_PERMISO_CONSULTAR,
                RecursoAccesos.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoAccesos.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoAccesos.INFORMACION_PERMISO_ELIMINACION,

                RecursoCategoriasSku.INFORMACION_PERMISO_CREACION,
                RecursoCategoriasSku.INFORMACION_PERMISO_LISTAR,
                RecursoCategoriasSku.INFORMACION_PERMISO_CONSULTAR,
                RecursoCategoriasSku.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoCategoriasSku.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoCategoriasSku.INFORMACION_PERMISO_ELIMINACION,

                RecursoEntradas.INFORMACION_PERMISO_CREACION,
                RecursoEntradas.INFORMACION_PERMISO_LISTAR,
                RecursoEntradas.INFORMACION_PERMISO_CONSULTAR,
                RecursoEntradas.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoEntradas.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoEntradas.INFORMACION_PERMISO_ELIMINACION,

                RecursoMonedas.INFORMACION_PERMISO_CREACION,
                RecursoMonedas.INFORMACION_PERMISO_LISTAR,
                RecursoMonedas.INFORMACION_PERMISO_CONSULTAR,
                RecursoMonedas.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoMonedas.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoMonedas.INFORMACION_PERMISO_ELIMINACION,

                RecursoSkus.INFORMACION_PERMISO_CREACION,
                RecursoSkus.INFORMACION_PERMISO_LISTAR,
                RecursoSkus.INFORMACION_PERMISO_CONSULTAR,
                RecursoSkus.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoSkus.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoSkus.INFORMACION_PERMISO_ELIMINACION,

                RecursoPaquetes.INFORMACION_PERMISO_CREACION,
                RecursoPaquetes.INFORMACION_PERMISO_LISTAR,
                RecursoPaquetes.INFORMACION_PERMISO_CONSULTAR,
                RecursoPaquetes.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoPaquetes.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoPaquetes.INFORMACION_PERMISO_ELIMINACION,

                RecursoLibrosDePrecios.INFORMACION_PERMISO_CREACION,
                RecursoLibrosDePrecios.INFORMACION_PERMISO_LISTAR,
                RecursoLibrosDePrecios.INFORMACION_PERMISO_CONSULTAR,
                RecursoLibrosDePrecios.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoLibrosDePrecios.INFORMACION_PERMISO_ELIMINACION,

                RecursoLibrosDeProhibiciones.INFORMACION_PERMISO_CREACION,
                RecursoLibrosDeProhibiciones.INFORMACION_PERMISO_LISTAR,
                RecursoLibrosDeProhibiciones.INFORMACION_PERMISO_CONSULTAR,
                RecursoLibrosDeProhibiciones.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoLibrosDeProhibiciones.INFORMACION_PERMISO_ELIMINACION,

                RecursoLibrosSegunReglas.INFORMACION_PERMISO_CREACION,
                RecursoLibrosSegunReglas.INFORMACION_PERMISO_LISTAR,
                RecursoLibrosSegunReglas.INFORMACION_PERMISO_CONSULTAR,
                RecursoLibrosSegunReglas.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoLibrosSegunReglas.INFORMACION_PERMISO_ELIMINACION,

                RecursoLibrosSegunReglasCompleto.INFORMACION_PERMISO_LISTAR,
                RecursoLibrosSegunReglasCompleto.INFORMACION_PERMISO_CONSULTAR,

                RecursoCompras.INFORMACION_PERMISO_LISTAR,
                RecursoCompras.INFORMACION_PERMISO_CONSULTAR,
                RecursoCompras.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoCompras.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoCompras.INFORMACION_PERMISO_ELIMINACION,

                RecursoComprasDeUnaPersona.INFORMACION_PERMISO_LISTAR,

                RecursoPersonasDeUnaCompra.INFORMACION_PERMISO_LISTAR,

                RecursoCreditosDeUnaPersona.INFORMACION_PERMISO_CONSULTAR,

                RecursoConsumiblesEnPuntoDeVenta.INFORMACION_PERMISO_LISTAR,
                RecursoConsumiblesEnPuntoDeVenta.INFORMACION_PERMISO_ACTUALIZACION_TODOS,

                RecursoFondosEnPuntoDeVenta.INFORMACION_PERMISO_LISTAR,

                RecursoReservas.INFORMACION_PERMISO_LISTAR,
                RecursoReservas.INFORMACION_PERMISO_CONSULTAR,
                RecursoReservas.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoReservas.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoReservas.INFORMACION_PERMISO_ELIMINACION,

                RecursoSesionesDeManilla.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,
                RecursoSesionesDeManilla.INFORMACION_PERMISO_CONSULTAR,

                RecursoPersonaPorIdSesionManilla.INFORMACION_PERMISO_CONSULTAR,

                RecursoLotesDeOrdenes.INFORMACION_PERMISO_ACTUALIZACION,
                RecursoLotesDeOrdenes.INFORMACION_PERMISO_ACTUALIZACION_PARCIAL,

                RecursoOrdenes.INFORMACION_PERMISO_LISTAR,
                RecursoOrdenes.INFORMACION_PERMISO_CONSULTAR,
                RecursoOrdenes.INFORMACION_PERMISO_ELIMINACION,

                RecursoOrdenesDeUnaSesionDeManilla.INFORMACION_PERMISO_LISTAR

                                             )
    }

    override val codigosError: CodigosErrorDTO = RolDTO.CodigosError
    override val nombreEntidad: String = Rol.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: PermisoBack): PermisoBackDTO
    {
        return PermisoBackDTO(entidadDeNegocio)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<PermisoBack>
    {
        return PERMISOS_VALIDOS.asSequence().map { it.aPermisoBackSegunIdCliente(idCliente) }
    }
}

internal data class InformacionPermiso(private val nombrePermiso: String, private val accion: PermisoBack.Accion)
{
    fun aPermisoBackSegunIdCliente(idCliente: Long): PermisoBack
    {
        return PermisoBack(idCliente, nombrePermiso, accion)
    }
}

internal fun darInformacionPermisoParaCreacionSegunNombrePermiso(nombrePermiso: String): InformacionPermiso
{
    return InformacionPermiso(nombrePermiso, PermisoBack.Accion.POST)
}

internal fun darInformacionPermisoParaListarSegunNombrePermiso(nombrePermiso: String): InformacionPermiso
{
    return InformacionPermiso(nombrePermiso, PermisoBack.Accion.GET_TODOS)
}

internal fun darInformacionPermisoParaConsultarSegunNombrePermiso(nombrePermiso: String): InformacionPermiso
{
    return InformacionPermiso(nombrePermiso, PermisoBack.Accion.GET_UNO)
}

internal fun darInformacionPermisoParaActualizacionSegunNombrePermiso(nombrePermiso: String): InformacionPermiso
{
    return InformacionPermiso(nombrePermiso, PermisoBack.Accion.PUT)
}

internal fun darInformacionPermisoParaActualizacionTodosSegunNombrePermiso(nombrePermiso: String): InformacionPermiso
{
    return InformacionPermiso(nombrePermiso, PermisoBack.Accion.PUT_TODOS)
}

internal fun darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(nombrePermiso: String): InformacionPermiso
{
    return InformacionPermiso(nombrePermiso, PermisoBack.Accion.PATCH)
}

internal fun darInformacionPermisoParaEliminacionSegunNombrePermiso(nombrePermiso: String): InformacionPermiso
{
    return InformacionPermiso(nombrePermiso, PermisoBack.Accion.DELETE)
}