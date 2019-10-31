package co.smartobjects.red.clientes.ubicaciones.consumibles

import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ConsultarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.retrofit.ubicaciones.consumibles.ConsumiblesEnPuntoDeVentaRetrofitAPI

interface ConsumiblesEnPuntoDeVentaAPI
    : ConsultarPorParametrosAPI<Long, List<ConsumibleEnPuntoDeVenta>>

internal class ConsumiblesEnPuntoDeVentaAPIRetrofit
(
        private val apiDeConsumiblesEnPuntoDeVenta: ConsumiblesEnPuntoDeVentaRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : ConsumiblesEnPuntoDeVentaAPI
{
    override fun consultar(parametros: Long): RespuestaIndividual<List<ConsumibleEnPuntoDeVenta>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeConsumiblesEnPuntoDeVenta
                .consultarTodasLasConsumibleEnPuntoDeVenta(idCliente, parametros)
                .execute()
        }
    }
}
