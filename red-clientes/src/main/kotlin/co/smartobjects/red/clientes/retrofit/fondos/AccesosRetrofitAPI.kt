package co.smartobjects.red.clientes.retrofit.fondos

import co.smartobjects.entidades.fondos.Acceso
import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.AccesoDTO
import co.smartobjects.red.modelos.fondos.FondoDisponibleParaLaVentaDTO
import retrofit2.Call
import retrofit2.http.*

internal interface AccesosRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RUTA_RESUELTA)
    fun crearAcceso(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body acceso: AccesoDTO): Call<AccesoDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RUTA_RESUELTA)
    fun consultarTodasLasAcceso(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<AccesoDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RuteoAccesoApi.RUTA_RESUELTA)
    fun actualizarAcceso(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RuteoAccesoApi.PARAMETRO_RUTA) idAcceso: Long,
            @Body acceso: AccesoDTO
                        ): Call<AccesoDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RuteoAccesoApi.RUTA_RESUELTA)
    fun darAcceso(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RuteoAccesoApi.PARAMETRO_RUTA) idAcceso: Long
                 ): Call<AccesoDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RuteoAccesoApi.RUTA_RESUELTA)
    fun eliminarAcceso(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RuteoAccesoApi.PARAMETRO_RUTA) idAcceso: Long
                      ): Call<Unit>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RuteoAccesoApi.RUTA_RESUELTA)
    fun actualizarPorCamposAcceso(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoAccesosApi.RuteoAccesoApi.PARAMETRO_RUTA) idAcceso: Long,
            @Body acceso: FondoDisponibleParaLaVentaDTO<Acceso>
                                 ): Call<Unit>
}
