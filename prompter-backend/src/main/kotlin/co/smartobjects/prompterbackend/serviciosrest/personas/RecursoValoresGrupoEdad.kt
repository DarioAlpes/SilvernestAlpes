package co.smartobjects.prompterbackend.serviciosrest.personas

import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.persistencia.personas.valorgrupoedad.RepositorioValoresGruposEdad
import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.prompterbackend.serviciosrest.recursosbase.*
import co.smartobjects.prompterbackend.serviciosrest.usuarios.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.personas.ValorGrupoEdadDTO
import javax.ws.rs.Consumes
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
internal class RecursoValoresGrupoEdad(override val idCliente: Long, private val repositorio: RepositorioValoresGruposEdad, override val manejadorSeguridad: ManejadorSeguridad)
    : RecursoCreacionConRestriccionDeCliente<ValorGrupoEdad, ValorGrupoEdadDTO>,
      RecursoListarTodosDeCliente<ValorGrupoEdad, ValorGrupoEdadDTO>
{
    companion object
    {
        const val RUTA = "age-groups"
        // Esta constante va hasta la bd en la tabla de permisos permitidos a un rol. Si se cambia deben actualizarse los permisos correspondientes en la bd
        private const val NOMBRE_PERMISO = "Grupos de Edad"
        internal val INFORMACION_PERMISO_CREACION = darInformacionPermisoParaCreacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_LISTAR = darInformacionPermisoParaListarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_CONSULTAR = darInformacionPermisoParaConsultarSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ACTUALIZACION = darInformacionPermisoParaActualizacionSegunNombrePermiso(NOMBRE_PERMISO)
        internal val INFORMACION_PERMISO_ELIMINACION = darInformacionPermisoParaEliminacionSegunNombrePermiso(NOMBRE_PERMISO)
    }

    override val codigoDeErrorRestriccionIncumplida: Int = ValorGrupoEdadDTO.CodigosError.INTERSECTA_OTRO_GRUPO_DE_EDAD
    override val codigosError: CodigosErrorDTO = ValorGrupoEdadDTO.CodigosError
    override val nombreEntidad: String = ValorGrupoEdad.NOMBRE_ENTIDAD
    override val nombrePermiso: String = NOMBRE_PERMISO

    override fun transformarHaciaDTO(entidadDeNegocio: ValorGrupoEdad): ValorGrupoEdadDTO
    {
        return ValorGrupoEdadDTO(entidadDeNegocio)
    }

    override fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: ValorGrupoEdad): ValorGrupoEdad
    {
        return repositorio.crear(idCliente, entidad)
    }

    override fun listarTodosSegunIdCliente(idCliente: Long): Sequence<ValorGrupoEdad>
    {
        return repositorio.listar(idCliente)
    }

    @Path("{valor}")
    fun darRecursosEntidadEspecifica(@PathParam("valor") valor: String): RecursoValorGrupoEdad
    {
        return RecursoValorGrupoEdad(valor)
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    internal inner class RecursoValorGrupoEdad(override val id: String)
        : RecursoActualizableConRestriccionEIdMutableDeCliente<ValorGrupoEdad, ValorGrupoEdadDTO, String>,
          RecursoConsultarUnoDeCliente<ValorGrupoEdad, ValorGrupoEdadDTO, String>,
          RecursoEliminarPorIdDeCliente<ValorGrupoEdad, ValorGrupoEdadDTO, String>
    {
        override val codigoDeErrorRestriccionIncumplida: Int = this@RecursoValoresGrupoEdad.codigoDeErrorRestriccionIncumplida
        override val codigosError: CodigosErrorDTO = this@RecursoValoresGrupoEdad.codigosError
        override val nombreEntidad: String = this@RecursoValoresGrupoEdad.nombreEntidad
        override val idCliente: Long = this@RecursoValoresGrupoEdad.idCliente
        private val repositorio: RepositorioValoresGruposEdad = this@RecursoValoresGrupoEdad.repositorio
        override val nombrePermiso: String = this@RecursoValoresGrupoEdad.nombrePermiso
        override val manejadorSeguridad: ManejadorSeguridad = this@RecursoValoresGrupoEdad.manejadorSeguridad

        override fun darIdDeEntidadDeNegocio(entidad: ValorGrupoEdad): String
        {
            return entidad.valor
        }

        override fun actualizarEntidadDeNegocioSegunIdEIdCliente(idCliente: Long, id: String, entidad: ValorGrupoEdad): ValorGrupoEdad
        {
            return repositorio.actualizar(idCliente, id, entidad)
        }

        override fun consultarPorIdSegunIdCliente(idCliente: Long, id: String): ValorGrupoEdad?
        {
            return repositorio.buscarPorId(idCliente, id)
        }

        override fun transformarHaciaDTO(entidadDeNegocio: ValorGrupoEdad): ValorGrupoEdadDTO
        {
            return this@RecursoValoresGrupoEdad.transformarHaciaDTO(entidadDeNegocio)
        }

        override fun eliminarPorIdSegunIdCliente(idCliente: Long, id: String): Boolean
        {
            return repositorio.eliminarPorId(idCliente, id)
        }
    }
}