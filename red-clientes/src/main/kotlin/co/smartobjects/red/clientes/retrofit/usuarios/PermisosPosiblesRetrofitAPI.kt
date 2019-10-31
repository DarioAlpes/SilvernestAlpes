package co.smartobjects.red.clientes.retrofit.usuarios

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.usuarios.PermisoBackDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface PermisosPosiblesRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoPermisosPosiblesApi.RUTA_RESUELTA)
    fun consultarTodasLasPermisoBack(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<PermisoBackDTO>>
}
