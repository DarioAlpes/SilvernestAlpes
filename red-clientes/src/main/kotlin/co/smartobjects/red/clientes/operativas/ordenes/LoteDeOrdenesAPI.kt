package co.smartobjects.red.clientes.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.ActualizarCamposPorParametrosAPI
import co.smartobjects.red.clientes.base.ActualizarPorParametrosAPI
import co.smartobjects.red.clientes.base.RespuestaIndividual
import co.smartobjects.red.clientes.base.RespuestaVacia
import co.smartobjects.red.clientes.retrofit.operativas.ordenes.LoteDeOrdenesRetrofitAPI
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.ordenes.LoteDeOrdenesDTO

interface LoteDeOrdenesAPI
    : ActualizarPorParametrosAPI<String, LoteDeOrdenes, List<Orden>>,
      ActualizarCamposPorParametrosAPI<String, TransaccionEntidadTerminadaDTO<LoteDeOrdenes>>

internal class LoteDeOrdenesAPIRetrofit
(
        private val apiDeLoteDeOrdenes: LoteDeOrdenesRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : LoteDeOrdenesAPI
{
    override fun actualizar(parametros: String, entidad: LoteDeOrdenes): RespuestaIndividual<List<Orden>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeLoteDeOrdenes
                .actualizar(idCliente, parametros, LoteDeOrdenesDTO(entidad))
                .execute()
        }
    }

    override fun actualizarCampos(parametros: String, entidad: TransaccionEntidadTerminadaDTO<LoteDeOrdenes>): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeLoteDeOrdenes
                .actualizarPorCampos(idCliente, parametros, entidad)
                .execute()
        }
    }
}
