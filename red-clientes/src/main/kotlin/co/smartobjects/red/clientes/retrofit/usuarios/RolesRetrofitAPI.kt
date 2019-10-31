package co.smartobjects.red.clientes.retrofit.usuarios

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.usuarios.RolDTO
import retrofit2.Call
import retrofit2.http.*

internal interface RolesRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoRolesApi.RUTA_RESUELTA)
    fun crearRol(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Body rol: RolDTO
                ): Call<RolDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoRolesApi.RUTA_RESUELTA)
    fun consultarTodasLasRol(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<RolDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoRolesApi.RuteoRolApi.RUTA_RESUELTA)
    fun actualizarRol(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoRolesApi.RuteoRolApi.PARAMETRO_RUTA) nombreRol: String,
            @Body rol: RolDTO
                     ): Call<RolDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoRolesApi.RuteoRolApi.RUTA_RESUELTA)
    fun darRol(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoRolesApi.RuteoRolApi.PARAMETRO_RUTA) nombreRol: String
              ): Call<RolDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoRolesApi.RuteoRolApi.RUTA_RESUELTA)
    fun eliminarRol(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoRolesApi.RuteoRolApi.PARAMETRO_RUTA) nombreRol: String
                   ): Call<Unit>
}
