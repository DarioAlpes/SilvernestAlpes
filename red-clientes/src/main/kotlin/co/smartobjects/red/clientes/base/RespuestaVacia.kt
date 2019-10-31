package co.smartobjects.red.clientes.base

import co.smartobjects.red.modelos.ErrorDePeticion
import java.io.IOException


sealed class RespuestaVacia
{
    companion object
    {
        fun <T> desdeRespuestaInvidual(respuestaIndividual: RespuestaIndividual<T>): RespuestaVacia
        {
            return when (respuestaIndividual)
            {
                is RespuestaIndividual.Exitosa       -> RespuestaVacia.Exitosa
                is RespuestaIndividual.Vacia         -> RespuestaVacia.Exitosa
                is RespuestaIndividual.Error.Timeout -> RespuestaVacia.Error.Timeout
                is RespuestaIndividual.Error.Red     -> RespuestaVacia.Error.Red(respuestaIndividual.error)
                is RespuestaIndividual.Error.Back    -> RespuestaVacia.Error.Back(respuestaIndividual.codigoErrorHttp, respuestaIndividual.error)
            }
        }
    }

    object Exitosa : RespuestaVacia()
    sealed class Error : RespuestaVacia()
    {
        object Timeout : RespuestaVacia()

        data class Red(val error: IOException) : RespuestaVacia()
        data class Back(val codigoErrorHttp: Int, val error: ErrorDePeticion) : RespuestaVacia()
    }
}
