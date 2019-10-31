package co.smartobjects.red.clientes

import co.smartobjects.red.ConfiguracionJackson
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.ErrorDePeticion
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.nio.charset.Charset


internal interface ParserRespuestasRetrofit
{
    fun haciaRespuestaVacia(darRespuesta: () -> Response<Unit>): RespuestaVacia

    fun <EntidadDeNegocio, TipoDTODevuelto : EntidadDTO<EntidadDeNegocio>> haciaRespuestaIndividualDesdeDTO(darRespuesta: () -> Response<TipoDTODevuelto>)
            : RespuestaIndividual<EntidadDeNegocio>

    fun <T> haciaRespuestaIndividualSimple(darRespuesta: () -> Response<T>): RespuestaIndividual<T>

    fun <EntidadDeNegocio, TipoDTODevuelto : EntidadDTO<EntidadDeNegocio>> haciaRespuestaIndividualColeccionDesdeDTO(darRespuesta: () -> Response<List<TipoDTODevuelto>>)
            : RespuestaIndividual<List<EntidadDeNegocio>>

    fun <T> haciaRespuestaIndividualColeccionSimple(darRespuesta: () -> Response<List<T>>): RespuestaIndividual<List<T>>
}

internal class ParserRespuestasRetrofitImpl : ParserRespuestasRetrofit
{
    companion object
    {
        @[Suppress("unused") JvmStatic]
        fun darMensajeDeError(respuesta: Response<*>): ErrorDePeticion?
        {
            val errorBody = respuesta.errorBody()

            if (!respuesta.isSuccessful && errorBody != null)
            {
                val fuente = errorBody.source()
                fuente?.request(errorBody.contentLength())
                val buffer = fuente?.buffer()
                val charset = errorBody.contentType()?.charset() ?: Charset.forName("UTF-8")

                val mensaje = buffer?.clone()?.readString(charset)!!

                return deErrorBodyAErroDePeticion(mensaje)
            }

            return null
        }

        private fun deErrorBodyAErroDePeticion(errorBodyComoString: String): ErrorDePeticion
        {
            return ConfiguracionJackson
                .objectMapperDeJackson.readValue(errorBodyComoString, ErrorDePeticion::class.java)
        }
    }


    override fun haciaRespuestaVacia(darRespuesta: () -> Response<Unit>): RespuestaVacia
    {
        return try
        {
            val respuesta = darRespuesta()
            val errorDePeticion = darMensajeDeError(respuesta)

            return if (errorDePeticion != null)
            {
                RespuestaVacia.Error.Back(respuesta.code(), errorDePeticion)
            }
            else
            {
                RespuestaVacia.Exitosa
            }
        }
        catch (exception: SocketTimeoutException)
        {
            RespuestaVacia.Error.Timeout
        }
        catch (e: IOException)
        {
            RespuestaVacia.Error.Red(e)
        }
    }

    override fun <T> haciaRespuestaIndividualSimple(darRespuesta: () -> Response<T>): RespuestaIndividual<T>
    {
        return try
        {
            val respuesta = darRespuesta()
            val errorDePeticion = darMensajeDeError(respuesta)
            return if (errorDePeticion != null)
            {
                RespuestaIndividual.Error.Back(respuesta.code(), errorDePeticion)
            }
            else
            {
                val body = respuesta.body()
                if (body != null)
                {
                    RespuestaIndividual.Exitosa(body)
                }
                else
                {
                    RespuestaIndividual.Vacia()
                }
            }
        }
        catch (exception: SocketTimeoutException)
        {
            RespuestaIndividual.Error.Timeout()
        }
        catch (e: IOException)
        {
            RespuestaIndividual.Error.Red(e)
        }
    }

    override fun <EntidadDeNegocio, TipoDTODevuelto : EntidadDTO<EntidadDeNegocio>> haciaRespuestaIndividualDesdeDTO(
            darRespuesta: () -> Response<TipoDTODevuelto>
                                                                                                                    )
            : RespuestaIndividual<EntidadDeNegocio>
    {
        return try
        {
            val respuesta = darRespuesta()
            val errorDePeticion = darMensajeDeError(respuesta)
            return if (errorDePeticion != null)
            {
                RespuestaIndividual.Error.Back(respuesta.code(), errorDePeticion)
            }
            else
            {
                val body = respuesta.body()
                if (body != null)
                {
                    RespuestaIndividual.Exitosa(body.aEntidadDeNegocio())
                }
                else
                {
                    RespuestaIndividual.Vacia()
                }
            }
        }
        catch (exception: SocketTimeoutException)
        {
            RespuestaIndividual.Error.Timeout()
        }
        catch (e: IOException)
        {
            RespuestaIndividual.Error.Red(e)
        }
    }

    override fun <T> haciaRespuestaIndividualColeccionSimple(darRespuesta: () -> Response<List<T>>)
            : RespuestaIndividual<List<T>>
    {
        return try
        {
            val respuesta = darRespuesta()
            val errorDePeticion = darMensajeDeError(respuesta)
            return if (errorDePeticion != null)
            {
                RespuestaIndividual.Error.Back(respuesta.code(), errorDePeticion)
            }
            else
            {
                val body = respuesta.body()
                if (body != null)
                {
                    RespuestaIndividual.Exitosa(body)
                }
                else
                {
                    RespuestaIndividual.Vacia()
                }
            }
        }
        catch (exception: SocketTimeoutException)
        {
            RespuestaIndividual.Error.Timeout()
        }
        catch (e: IOException)
        {
            RespuestaIndividual.Error.Red(e)
        }
    }

    override fun <EntidadDeNegocio, TipoDTODevuelto : EntidadDTO<EntidadDeNegocio>> haciaRespuestaIndividualColeccionDesdeDTO(
            darRespuesta: () -> Response<List<TipoDTODevuelto>>
                                                                                                                             )
            : RespuestaIndividual<List<EntidadDeNegocio>>
    {
        return try
        {
            val respuesta = darRespuesta()
            val errorDePeticion = darMensajeDeError(respuesta)
            return if (errorDePeticion != null)
            {
                RespuestaIndividual.Error.Back(respuesta.code(), errorDePeticion)
            }
            else
            {
                val body = respuesta.body()
                if (body != null)
                {
                    RespuestaIndividual.Exitosa(body.map { it.aEntidadDeNegocio() })
                }
                else
                {
                    RespuestaIndividual.Vacia()
                }
            }
        }
        catch (exception: SocketTimeoutException)
        {
            RespuestaIndividual.Error.Timeout()
        }
        catch (e: IOException)
        {
            RespuestaIndividual.Error.Red(e)
        }
    }
}