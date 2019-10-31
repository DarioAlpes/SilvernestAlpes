package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionTodosSegunNombrePermiso
import co.smartobjects.red.modelos.EntidadDTO
import javax.ws.rs.PUT

// Se necesita tercer parametro generico, EntidadLista: List<EntidadDTO>, porque si no jackson no logra resolver el tipo del generico. Probablemente por culpa del type erasure
internal interface RecursoCreacionColeccion<EntidadNegocio, out TipoEntidadDTO : EntidadDTO<EntidadNegocio>, in EntidadLista : List<TipoEntidadDTO>>
    : TransformacionesConDTO<EntidadNegocio, TipoEntidadDTO>, RecursoConErroresDTO
{
    fun crearEntidadDeNegocio(entidad: List<EntidadNegocio>): List<EntidadNegocio>

    @PUT
    fun crear(dto: EntidadLista): List<TipoEntidadDTO>
    {
        val entidadDeNegocio =
                ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(codigosError) {
                    dto.map { it.aEntidadDeNegocio() }
                }

        return ejecutarFuncionCreacionTransformandoExcepcionesPersistenciaAExcepcionesBackend(nombreEntidad, codigosError) {
            crearEntidadDeNegocio(entidadDeNegocio).map { transformarHaciaDTO(it) }
        }
    }
}

internal interface RecursoCreacionColeccionDeCliente<EntidadCreacion, out TipoEntidadDTO : EntidadDTO<EntidadCreacion>, in EntidadLista : List<TipoEntidadDTO>>
    : RecursoCreacionColeccion<EntidadCreacion, TipoEntidadDTO, EntidadLista>,
      RecursoDeCliente
{
    fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: List<EntidadCreacion>): List<EntidadCreacion>

    override fun crearEntidadDeNegocio(entidad: List<EntidadCreacion>): List<EntidadCreacion>
    {
        return crearEntidadDeNegocioSegunIdCliente(idCliente, entidad)
    }

    override fun crear(dto: EntidadLista): List<TipoEntidadDTO>
    {
        return ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                manejadorSeguridad,
                darInformacionPermisoParaActualizacionTodosSegunNombrePermiso(nombrePermiso),
                idCliente
                                                                                      ) { super.crear(dto) }
    }
}