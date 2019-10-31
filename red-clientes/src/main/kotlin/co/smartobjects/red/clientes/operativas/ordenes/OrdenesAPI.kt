package co.smartobjects.red.clientes.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.operativas.ordenes.OrdenesRetrofitAPI

interface OrdenesAPI
    : ConsultarAPI<List<Orden>>,
      ConsultarPorParametrosAPI<Long, Orden>,
      EliminarPorParametrosAPI<Long, Orden>

internal class OrdenesAPIRetrofit
(
        private val apiDeOrdenes: OrdenesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : OrdenesAPI
{
    override fun consultar(): RespuestaIndividual<List<Orden>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeOrdenes
                .consultarTodasLasOrden(idCliente)
                .execute()
        }
    }

    override fun consultar(parametros: Long): RespuestaIndividual<Orden>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeOrdenes
                .darOrden(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: Long): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeOrdenes
                .eliminarOrden(idCliente, parametros)
                .execute()
        }
    }
}
