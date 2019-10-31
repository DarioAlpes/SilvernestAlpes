package co.smartobjects.prompterbackend.serviciosrest.recursosbase

import co.smartobjects.prompterbackend.seguridad.ManejadorSeguridad
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO

interface RecursoConErroresDTO
{
    val codigosError: CodigosErrorDTO
    val nombreEntidad: String
}

interface TransformacionesConDTOColeccion<
        out EntidadNegocioRetorno,
        in EntidadColeccionNegocio : Collection<EntidadNegocioRetorno>,
        out EntidadDTORetorno : EntidadDTO<EntidadNegocioRetorno>
        >
{
    fun transformarHaciaDTO(entidadDeNegocio: EntidadColeccionNegocio): Collection<EntidadDTORetorno>
}

interface TransformacionesConDTO<EntidadCreacion, out TipoEntidadDTO : EntidadDTO<EntidadCreacion>>
{
    fun transformarHaciaDTO(entidadDeNegocio: EntidadCreacion): TipoEntidadDTO
}

internal interface RecursoDeCliente
{
    val manejadorSeguridad: ManejadorSeguridad
    val idCliente: Long
    val nombrePermiso: String
}

interface RecursoEspecifico<out TipoId>
{
    val id: TipoId
}