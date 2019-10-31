package co.smartobjects.red.clientes.retrofit.fondos

import co.smartobjects.entidades.fondos.Entrada
import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.EntradaDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import retrofit2.Call
import retrofit2.http.*

internal interface EntradasRetrofitAPI
{

    @POST(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RUTA_RESUELTA)
    fun crearEntrada(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body entrada: EntradaDTO): Call<EntradaDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RUTA_RESUELTA)
    fun consultarTodasLasEntrada(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<EntradaDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RuteoEntradaApi.RUTA_RESUELTA)
    fun actualizarEntrada(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RuteoEntradaApi.PARAMETRO_RUTA) idEntrada: Long,
            @Body entrada: EntradaDTO
                         ): Call<EntradaDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RuteoEntradaApi.RUTA_RESUELTA)
    fun darEntrada(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RuteoEntradaApi.PARAMETRO_RUTA) idEntrada: Long
                  ): Call<EntradaDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RuteoEntradaApi.RUTA_RESUELTA)
    fun eliminarEntrada(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RuteoEntradaApi.PARAMETRO_RUTA) idEntrada: Long
                       ): Call<Unit>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RuteoEntradaApi.RUTA_RESUELTA)
    fun actualizarPorCamposEntrada(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoEntradasApi.RuteoEntradaApi.PARAMETRO_RUTA) idEntrada: Long,
            @Body entrada: FondoDisponibleParaLaVentaDTO<Entrada>
                                  ): Call<Unit>
}
