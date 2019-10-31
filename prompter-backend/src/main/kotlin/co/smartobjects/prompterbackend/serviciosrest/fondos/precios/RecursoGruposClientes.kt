package co.smartobjects.prompterbackend.serviciosrest.fondos.precios

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.persistencia.fondos.precios.gruposclientes.RepositorioGrupoClientes
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.precios.GrupoClientesDTO
import co.smartobjects.red.modelos.fondos.precios.NombreGrupoClientesDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoGruposClientes
(
        override val idCliente: Long,
        private val repositorio: RepositorioGrupoClientes,
        override val manejadorSeguridad: ManejadorSeguridad
) : RecursoCreacionEnClienteConRestriccionYAutoasignandoId<GrupoClientes, Long?, GrupoClientesDTO>,
    RecursoListarTodosDeCliente<GrupoClientes, GrupoClientesDTO>
{
    companion object
    {
        const val RUTA = "customers-groups"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Grupos de Clientes"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION_PARCIAL = darInformacionPermisoParaActualizacionParcialSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigoDeErrorRestriccionIncumplida: Int = GrupoClientesDTO.CodigosError.MISMOS_SEGMENTOS_OTRO_GRUPO_CLIENTES
    override val codigosError: CodigosErrorDTO = GrupoClientesDTO.CodigosError
    override val nombreEntidad: String = GrupoClientes.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: GrupoClientes): GrupoClientesDTO
    {
        return GrupoClientesDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: GrupoClientes): GrupoClientes
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<GrupoClientes>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{id}")
    fun darRecursosEntidadEspecifica(@PathParam("id") id: Long): RecursoGrupoClientes
    {
        return RecursoGrupoClientes(id)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoGrupoClientes(override val id: Long)
        : RecursoConsultarUnoDeCliente<GrupoClientes, GrupoClientesDTO, Long>,
          RecursoEliminarPorIdDeCliente<GrupoClientes, GrupoClientesDTO, Long>,
          RecursoActualizablePorCamposDeCliente<GrupoClientes, NombreGrupoClientesDTO, Long>
    {
        override val codigosError: CodigosErrorDTO = this@RecursoGruposClientes.codigosError
        override val nombreEntidad: String = this@RecursoGruposClientes.nombreEntidad
        override val idCliente: Long = this@RecursoGruposClientes.idCliente
        private val repositorioGrupoClientes: RepositorioGrupoClientes = this@RecursoGruposClientes.repositorio
        override val nombrePermiso: String = this@RecursoGruposClientes.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoGruposClientes.manejadorSeguridad

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: Long): GrupoClientes?
        {
            return repositorioGrupoClientes.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: GrupoClientes): GrupoClientesDTO
        {
            return this@RecursoGruposClientes.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: Long): Boolean
        {
            return repositorioGrupoClientes.eliminarPorId(idCliente, id)
        }

        override fun actualizarEntidadPorCamposIndividualesSegunIdCliente(idCliente: Long, id: Long, camposModificables: Map<String, CampoModificable<GrupoClientes, *>>)
        {
            repositorioGrupoClientes.actualizarCamposIndividuales(idCliente, id, camposModificables)
        }
    }
}