package co.smartobjects.red.clientes.retrofit.operativas.reservas.sesionesdemanilla

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.operativas.ordenes.OrdenDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface OrdenesDeUnaSesionDeManillaRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoSesionesDeManillaApi.RuteoSesionDeManillaApi.RuteoOrdenesDeUnaSesionDeManillaApi.RUTA_RESUELTA)
    fun listar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoSesionesDeManillaApi.RuteoSesionDeManillaApi.PARAMETRO_RUTA) idSesionDeManilla: Long
              ): Call<List<OrdenDTO>>
}
