package co.smartobjects.red.clientes.retrofit.ubicaciones.consumibles

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.ubicaciones.consumibles.ConsumibleEnPuntoDeVentaDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface ConsumiblesEnPuntoDeVentaRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.RuteoConsumiblesEnPuntoDeVentaApi.RUTA_RESUELTA)
    fun consultarTodasLasConsumibleEnPuntoDeVenta(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.PARAMETRO_RUTA) idUbicacion: Long
                                                 ): Call<List<ConsumibleEnPuntoDeVentaDTO>>
}
