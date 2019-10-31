package co.smartobjects.red.clientes.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.retrofit.ubicaciones.contabilizables.ConteosUbicacionesRetrofitAPI

interface ConteosUbicacionesAPI
    : ConsultarAPI<List<ConteoUbicacion>>
{
    fun eliminarTodas(): RespuestaVacia
}

internal class ConteosUbicacionesAPIRetrofit
(
        private val apiDeConteosUbicaciones: ConteosUbicacionesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : ConteosUbicacionesAPI
{
    override fun consultar(): RespuestaIndividual<List<ConteoUbicacion>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeConteosUbicaciones
                .consultarTodasLasConteoUbicacion(idCliente)
                .execute()
        }
    }

    override fun eliminarTodas(): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeConteosUbicaciones
                .eliminarTodas(idCliente)
                .execute()
        }
    }
}
