package co.smartobjects.red.clientes.retrofit.operativas.compras

import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import retrofit2.Call
import retrofit2.http.*

internal interface ComprasRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoComprasApi.RUTA_RESUELTA)
    fun consultarTodasLasCompra(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<CompraDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoComprasApi.RuteoCompraApi.RUTA_RESUELTA)
    fun actualizarCompra(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoComprasApi.RuteoCompraApi.PARAMETRO_RUTA) idCompra: String,
            @Body compra: CompraDTO
                        ): Call<CompraDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoComprasApi.RuteoCompraApi.RUTA_RESUELTA)
    fun darCompra(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoComprasApi.RuteoCompraApi.PARAMETRO_RUTA) idCompra: String
                 ): Call<CompraDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoComprasApi.RuteoCompraApi.RUTA_RESUELTA)
    fun eliminarCompra(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoComprasApi.RuteoCompraApi.PARAMETRO_RUTA) idCompra: String
                      ): Call<Unit>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoComprasApi.RuteoCompraApi.RUTA_RESUELTA)
    fun actualizarPorCamposCompra(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoComprasApi.RuteoCompraApi.PARAMETRO_RUTA) idCompra: String,
            @Body compra: TransaccionEntidadTerminadaDTO<Compra>
                                 ): Call<Unit>
}
