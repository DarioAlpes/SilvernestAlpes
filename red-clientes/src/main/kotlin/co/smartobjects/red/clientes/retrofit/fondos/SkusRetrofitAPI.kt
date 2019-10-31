package co.smartobjects.red.clientes.retrofit.fondos

import co.smartobjects.entidades.fondos.Sku
import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import co.smartobjects.red.modelos.fondos.SkuDTO
import retrofit2.Call
import retrofit2.http.*

internal interface SkusRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RUTA_RESUELTA)
    fun crearSku(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body sku: SkuDTO): Call<SkuDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RUTA_RESUELTA)
    fun consultarTodasLasSku(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<SkuDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RuteoSkuApi.RUTA_RESUELTA)
    fun actualizarSku(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RuteoSkuApi.PARAMETRO_RUTA) idSku: Long,
            @Body sku: SkuDTO
                     ): Call<SkuDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RuteoSkuApi.RUTA_RESUELTA)
    fun darSku(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RuteoSkuApi.PARAMETRO_RUTA) idSku: Long
              ): Call<SkuDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RuteoSkuApi.RUTA_RESUELTA)
    fun eliminarSku(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RuteoSkuApi.PARAMETRO_RUTA) idSku: Long
                   ): Call<Unit>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RuteoSkuApi.RUTA_RESUELTA)
    fun actualizarPorCamposSku(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoSkusApi.RuteoSkuApi.PARAMETRO_RUTA) idSku: Long,
            @Body sku: FondoDisponibleParaLaVentaDTO<Sku>
                              ): Call<Unit>
}
