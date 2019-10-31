package co.smartobjects.red.clientes.retrofit.ubicaciones.contabilizables

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.ubicaciones.contabilizables.ConteoUbicacionDTO
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

internal interface ConteosUbicacionesRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoConteosUbicacionesApi.RUTA_RESUELTA)
    fun consultarTodasLasConteoUbicacion(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<ConteoUbicacionDTO>>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoConteosUbicacionesApi.RUTA_RESUELTA)
    fun eliminarTodas(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<Unit>
}
