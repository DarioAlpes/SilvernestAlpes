package co.smartobjects.red.clientes.retrofit.operativas.ordenes

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

internal interface OrdenesRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoOrdenesApi.RUTA_RESUELTA)
    fun consultarTodasLasOrden(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<OrdenDTO>>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoOrdenesApi.RuteoOrdenApi.RUTA_RESUELTA)
    fun darOrden(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoOrdenesApi.RuteoOrdenApi.PARAMETRO_RUTA) idOrden: Long
                ): Call<OrdenDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoOrdenesApi.RuteoOrdenApi.RUTA_RESUELTA)
    fun eliminarOrden(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoOrdenesApi.RuteoOrdenApi.PARAMETRO_RUTA) idOrden: Long
                     ): Call<Unit>

}
