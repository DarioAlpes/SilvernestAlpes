package co.smartobjects.prompterbackend.excepciones

import co.smartobjects.red.modelos.ErrorDePeticion
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
internal class MapperErrorAPI : ExceptionMapper<ErrorAPI>
{
    override fun toResponse(exception: ErrorAPI): Response
    {
        return Response
            .status(exception.codigoHTTP)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(ErrorDePeticion(exception.codigoInterno, exception.message!!))
            .build()
    }
}