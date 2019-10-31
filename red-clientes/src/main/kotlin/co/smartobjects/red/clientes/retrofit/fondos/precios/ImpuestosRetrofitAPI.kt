package co.smartobjects.red.clientes.retrofit.fondos.precios

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.precios.ImpuestoDTO
import retrofit2.Call
import retrofit2.http.*

internal interface ImpuestosRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoImpuestosApi.RUTA_RESUELTA)
    fun crearImpuesto(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body impuesto: ImpuestoDTO): Call<ImpuestoDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoImpuestosApi.RUTA_RESUELTA)
    fun consultarTodasLasImpuesto(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<ImpuestoDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoImpuestosApi.RuteoImpuestoApi.RUTA_RESUELTA)
    fun actualizarImpuesto(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoImpuestosApi.RuteoImpuestoApi.PARAMETRO_RUTA) idImpuesto: Long,
            @Body impuesto: ImpuestoDTO
                          ): Call<ImpuestoDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoImpuestosApi.RuteoImpuestoApi.RUTA_RESUELTA)
    fun darImpuesto(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoImpuestosApi.RuteoImpuestoApi.PARAMETRO_RUTA) idImpuesto: Long
                   ): Call<ImpuestoDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoImpuestosApi.RuteoImpuestoApi.RUTA_RESUELTA)
    fun eliminarImpuesto(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoImpuestosApi.RuteoImpuestoApi.PARAMETRO_RUTA) idImpuesto: Long
                        ): Call<Unit>
}
