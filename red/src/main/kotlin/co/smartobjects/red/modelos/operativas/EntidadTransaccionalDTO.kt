package co.smartobjects.red.modelos.operativas

import co.smartobjects.red.modelos.CodigosErrorDTO

interface EntidadTransaccionalDTO
{
    object CodigosError : CodigosErrorDTO(90000)
    {
        val ID_INVALIDO = CodigosError.codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
        val NOMBRE_USUARIO_INVALIDO = ID_INVALIDO + 1
    }
}