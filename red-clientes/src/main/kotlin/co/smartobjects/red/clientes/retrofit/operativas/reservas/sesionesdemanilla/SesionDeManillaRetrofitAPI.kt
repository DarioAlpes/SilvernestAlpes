package co.smartobjects.red.clientes.retrofit.operativas.reservas.sesionesdemanilla

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaDTO
import co.smartobjects.red.modelos.operativas.reservas.SesionDeManillaPatchDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

internal interface SesionDeManillaRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoSesionesDeManillaApi.RuteoSesionDeManillaApi.RUTA_RESUELTA)
    fun consultar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoSesionesDeManillaApi.RuteoSesionDeManillaApi.PARAMETRO_RUTA) idSesionDeManilla: Long
                 ): Call<SesionDeManillaDTO>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoSesionesDeManillaApi.RuteoSesionDeManillaApi.RUTA_RESUELTA)
    fun actualizarPorCamposSesionDeManilla(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoSesionesDeManillaApi.RuteoSesionDeManillaApi.PARAMETRO_RUTA) idSesionDeManilla: Long,
            @Body sesionDeManillaPatch: SesionDeManillaPatchDTO
                                          ): Call<Unit>
}
