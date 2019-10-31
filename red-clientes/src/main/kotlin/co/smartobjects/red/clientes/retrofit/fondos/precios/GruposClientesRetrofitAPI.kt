package co.smartobjects.red.clientes.retrofit.fondos.precios

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.precios.GrupoClientesDTO
import retrofit2.Call
import retrofit2.http.*

internal interface GruposClientesRetrofitAPI
{

    @POST(RuteoClientesApi.RuteoClienteApi.RuteoGruposClientesApi.RUTA_RESUELTA)
    fun crearGrupoClientes(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body grupoClientes: GrupoClientesDTO): Call<GrupoClientesDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoGruposClientesApi.RUTA_RESUELTA)
    fun consultarTodasLasGrupoClientes(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<GrupoClientesDTO>>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoGruposClientesApi.RuteoGrupoClientesApi.RUTA_RESUELTA)
    fun darGrupoClientes(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoGruposClientesApi.RuteoGrupoClientesApi.PARAMETRO_RUTA) idGrupoClientes: Long
                        ): Call<GrupoClientesDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoGruposClientesApi.RuteoGrupoClientesApi.RUTA_RESUELTA)
    fun eliminarGrupoClientes(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoGruposClientesApi.RuteoGrupoClientesApi.PARAMETRO_RUTA) idGrupoClientes: Long
                             ): Call<Unit>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoGruposClientesApi.RuteoGrupoClientesApi.RUTA_RESUELTA)
    fun actualizarPorCamposGrupoClientes(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoGruposClientesApi.RuteoGrupoClientesApi.PARAMETRO_RUTA) idGrupoClientes: Long,
            @Body grupoClientes: co.smartobjects.red.modelos.fondos.precios.NombreGrupoClientesDTO
                                        ): Call<Unit>
}
