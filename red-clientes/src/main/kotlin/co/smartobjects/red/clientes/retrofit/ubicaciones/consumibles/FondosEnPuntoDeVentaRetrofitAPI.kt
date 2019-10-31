package co.smartobjects.red.clientes.retrofit.ubicaciones.consumibles

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.ListaFondosDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface FondosEnPuntoDeVentaRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.RuteoFondosEnPuntoDeVentaApi.RUTA_RESUELTA)
    fun listar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.PARAMETRO_RUTA) idUbicacion: Long
              ): Call<ListaFondosDTO>
}
