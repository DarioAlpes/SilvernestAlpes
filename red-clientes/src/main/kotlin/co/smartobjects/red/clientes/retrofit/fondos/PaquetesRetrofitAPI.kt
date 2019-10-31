package co.smartobjects.red.clientes.retrofit.fondos

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.PaqueteDTO
import co.smartobjects.red.modelos.fondos.PaquetePatchDTO
import retrofit2.Call
import retrofit2.http.*

internal interface PaquetesRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RUTA_RESUELTA)
    fun crearPaquete(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body paquete: PaqueteDTO): Call<PaqueteDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RUTA_RESUELTA)
    fun consultarTodasLasPaquete(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<PaqueteDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RuteoPaqueteApi.RUTA_RESUELTA)
    fun actualizarPaquete(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RuteoPaqueteApi.PARAMETRO_RUTA) idPaquete: Long,
            @Body paquete: PaqueteDTO
                         ): Call<PaqueteDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RuteoPaqueteApi.RUTA_RESUELTA)
    fun darPaquete(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RuteoPaqueteApi.PARAMETRO_RUTA) idPaquete: Long
                  ): Call<PaqueteDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RuteoPaqueteApi.RUTA_RESUELTA)
    fun eliminarPaquete(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RuteoPaqueteApi.PARAMETRO_RUTA) idPaquete: Long
                       ): Call<Unit>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RuteoPaqueteApi.RUTA_RESUELTA)
    fun actualizarPorCamposPaquete(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoPaquetesApi.RuteoPaqueteApi.PARAMETRO_RUTA) idPaquete: Long,
            @Body paquete: PaquetePatchDTO
                                  ): Call<Unit>
}
