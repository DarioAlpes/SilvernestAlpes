package co.smartobjects.red.clientes.retrofit.fondos

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.CategoriaSkuDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import retrofit2.Call
import retrofit2.http.*

internal interface CategoriasSkuRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RUTA_RESUELTA)
    fun crearCategoriaSku(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body categoriaSku: CategoriaSkuDTO): Call<CategoriaSkuDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RUTA_RESUELTA)
    fun consultarTodasLasCategoriaSku(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<CategoriaSkuDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RuteoCategoriaSkuApi.RUTA_RESUELTA)
    fun actualizarCategoriaSku(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RuteoCategoriaSkuApi.PARAMETRO_RUTA) idCategoriaSku: Long,
            @Body categoriaSku: CategoriaSkuDTO
                              ): Call<CategoriaSkuDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RuteoCategoriaSkuApi.RUTA_RESUELTA)
    fun darCategoriaSku(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RuteoCategoriaSkuApi.PARAMETRO_RUTA) idCategoriaSku: Long
                       ): Call<CategoriaSkuDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RuteoCategoriaSkuApi.RUTA_RESUELTA)
    fun eliminarCategoriaSku(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RuteoCategoriaSkuApi.PARAMETRO_RUTA) idCategoriaSku: Long
                            ): Call<Unit>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RuteoCategoriaSkuApi.RUTA_RESUELTA)
    fun actualizarPorCamposCategoriaSku(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoCategoriasSkuApi.RuteoCategoriaSkuApi.PARAMETRO_RUTA) idCategoriaSku: Long,
            @Body categoriaSku: FondoDisponibleParaLaVentaDTO<CategoriaSku>
                                       ): Call<Unit>
}
