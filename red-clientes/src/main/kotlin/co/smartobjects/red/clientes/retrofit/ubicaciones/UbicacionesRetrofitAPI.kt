package co.smartobjects.red.clientes.retrofit.ubicaciones

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.ubicaciones.UbicacionDTO
import retrofit2.Call
import retrofit2.http.*

internal interface UbicacionesRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RUTA_RESUELTA)
    fun crearUbicacion(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Body ubicacion: UbicacionDTO
                      ): Call<UbicacionDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RUTA_RESUELTA)
    fun consultarTodasLasUbicacion(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<UbicacionDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.RUTA_RESUELTA)
    fun actualizarUbicacion(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.PARAMETRO_RUTA) idUbicacion: Long,
            @Body ubicacion: UbicacionDTO
                           ): Call<UbicacionDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.RUTA_RESUELTA)
    fun darUbicacion(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.PARAMETRO_RUTA) idUbicacion: Long
                    ): Call<UbicacionDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.RUTA_RESUELTA)
    fun eliminarUbicacion(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.PARAMETRO_RUTA) idUbicacion: Long
                         ): Call<Unit>
}
