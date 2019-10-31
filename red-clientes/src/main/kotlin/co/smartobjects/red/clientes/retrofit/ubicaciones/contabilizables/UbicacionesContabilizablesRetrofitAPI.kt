package co.smartobjects.red.clientes.retrofit.ubicaciones.contabilizables

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.ubicaciones.contabilizables.UbicacionesContabilizablesDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface UbicacionesContabilizablesRetrofitAPI
{
    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesContabilizablesApi.RUTA_RESUELTA)
    fun crear(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Body ubicacionContabilizable: UbicacionesContabilizablesDTO
             ): Call<List<Long>>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesContabilizablesApi.RUTA_RESUELTA)
    fun darTodas(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<Long>>
}