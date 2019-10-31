package co.smartobjects.red.clientes.retrofit.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.ordenes.LoteDeOrdenesDTO
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface LoteDeOrdenesRetrofitAPI
{
    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoLotesDeOrdenesApi.RuteoLoteDeOrdenesApi.RUTA_RESUELTA)
    fun actualizar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLotesDeOrdenesApi.RuteoLoteDeOrdenesApi.PARAMETRO_RUTA) idLoteDeOrdenes: String,
            @Body loteDeOrdenes: LoteDeOrdenesDTO
                  ): Call<List<OrdenDTO>>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoLotesDeOrdenesApi.RuteoLoteDeOrdenesApi.RUTA_RESUELTA)
    fun actualizarPorCampos(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLotesDeOrdenesApi.RuteoLoteDeOrdenesApi.PARAMETRO_RUTA) idLoteDeOrdenes: String,
            @Body loteDeOrdenes: TransaccionEntidadTerminadaDTO<LoteDeOrdenes>
                           ): Call<Unit>
}
