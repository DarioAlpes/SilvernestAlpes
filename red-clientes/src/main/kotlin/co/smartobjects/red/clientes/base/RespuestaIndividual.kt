package co.smartobjects.red.clientes.base

import co.smartobjects.red.modelos.ErrorDePeticion
import java.io.IOException


sealed class RespuestaIndividual<T>
{
    data class Exitosa<T>(val respuesta: T) : RespuestaIndividual<T>()
    class Vacia<T> : RespuestaIndividual<T>()
    {
        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int
        {
            return javaClass.hashCode()
        }
    }

    sealed class Error<T> : RespuestaIndividual<T>()
    {
        class Timeout<T> : RespuestaIndividual<T>()
        {
            override fun equals(other: Any?): Boolean
            {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                return true
            }

            override fun hashCode(): Int
            {
                return javaClass.hashCode()
            }
        }

        data class Red<T>(val error: IOException) : RespuestaIndividual<T>()
        data class Back<T>(val codigoErrorHttp: Int, val error: ErrorDePeticion) : RespuestaIndividual<T>()
    }
}