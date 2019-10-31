package co.smartobjects.red.clientes.retrofit.fondos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.DineroDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import retrofit2.Call
import retrofit2.http.*

internal interface MonedasRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RUTA_RESUELTA)
    fun crearDinero(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body dinero: DineroDTO): Call<DineroDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RUTA_RESUELTA)
    fun consultarTodasLasDinero(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<DineroDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RuteoMonedaApi.RUTA_RESUELTA)
    fun actualizarDinero(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RuteoMonedaApi.PARAMETRO_RUTA) idMoneda: Long,
            @Body dinero: DineroDTO
                        ): Call<DineroDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RuteoMonedaApi.RUTA_RESUELTA)
    fun darDinero(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RuteoMonedaApi.PARAMETRO_RUTA) idMoneda: Long
                 ): Call<DineroDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RuteoMonedaApi.RUTA_RESUELTA)
    fun eliminarDinero(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RuteoMonedaApi.PARAMETRO_RUTA) idMoneda: Long
                      ): Call<Unit>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RuteoMonedaApi.RUTA_RESUELTA)
    fun actualizarPorCamposDinero(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoMonedasApi.RuteoMonedaApi.PARAMETRO_RUTA) idMoneda: Long,
            @Body dinero: FondoDisponibleParaLaVentaDTO<Dinero>
                                 ): Call<Unit>
}
