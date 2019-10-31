package co.smartobjects.red.clientes.retrofit.fondos

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.ListaFondosDTO
import co.smartobjects.red.modelos.fondos.WrapperDeserilizacionFondoDTO
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

internal interface FondosRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoFondosApi.RUTA_RESUELTA)
    fun listar(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<ListaFondosDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoFondosApi.RuteoFondoApi.RUTA_RESUELTA)
    fun consultar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoFondosApi.RuteoFondoApi.PARAMETRO_RUTA) idFondo: Long
                 ): Call<WrapperDeserilizacionFondoDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoFondosApi.RuteoFondoApi.RUTA_RESUELTA)
    fun eliminar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoFondosApi.RuteoFondoApi.PARAMETRO_RUTA) idFondo: Long
                ): Call<Unit>

    /*@PATCH(RuteoClientesApi.RuteoClienteApi.RuteoFondosApi.RuteoFondoApi.RUTA_RESUELTA)
    fun <T : Fondo<T>> actualizarPorCamposIndividuales(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente : Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoFondosApi.RuteoFondoApi.PARAMETRO_RUTA) idFondo : Long,
            @Body fondo: FondoDisponibleParaLaVentaDTO<T>
                                                      ): Call<Unit>*/
}
