package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.prompterbackend.serviciosrest.usuarios.darInformacionPermisoParaActualizacionTodosSegunNombrePermiso
import co.smartobjects.red.modelos.EntidadDTO
import javax.ws.rs.PUT

// Se necesita tercer parametro generico, EntidadLista: List<EntidadDTO>, porque si no jackson no logra resolver el tipo del generico. Probablemente por culpa del type erasure
internal interface RecursoReemplazableAColeccionNoDTO<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, ElementoEnColeccion>
    : RecursoConErroresDTO
{
    fun crearEntidadDeNegocio(entidad: EntidadNegocio): List<ElementoEnColeccion>

    @PUT
    fun crear(dto: TipoEntidadDTO): List<ElementoEnColeccion>
    {
        val entidadDeNegocio =
                ejecutarFuncionTransformacionDTOANegocioTransformandoExcepcionesAExcepcionesBackend(codigosError) {
                    dto.aEntidadDeNegocio()
                }

        return ejecutarFuncionCreacionTransformandoExcepcionesPersistenciaAExcepcionesBackend(nombreEntidad, codigosError) {
            crearEntidadDeNegocio(entidadDeNegocio)
        }
    }
}

internal interface RecursoReemplazableAColeccionNoDTODeCliente<EntidadNegocio, TipoEntidadDTO : EntidadDTO<EntidadNegocio>, ElementoEnColeccion>
    : RecursoReemplazableAColeccionNoDTO<EntidadNegocio, TipoEntidadDTO, ElementoEnColeccion>,
      RecursoDeCliente
{
    fun crearEntidadDeNegocioSegunIdCliente(idCliente: Long, entidad: EntidadNegocio): List<ElementoEnColeccion>

    override fun crearEntidadDeNegocio(entidad: EntidadNegocio): List<ElementoEnColeccion>
    {
        return crearEntidadDeNegocioSegunIdCliente(idCliente, entidad)
    }

    override fun crear(dto: TipoEntidadDTO): List<ElementoEnColeccion>
    {
        return ejecutarVerificandoPermisosYTransformandoExcepcionesAExcepcionesBackend(
                manejadorSeguridad,
                darInformacionPermisoParaActualizacionTodosSegunNombrePermiso(nombrePermiso),
                idCliente
                                                                                      ) {
            super.crear(dto)
        }
    }
}