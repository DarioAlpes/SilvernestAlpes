package co.smartobjects.red.clientes.retrofit.fondos.libros

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.libros.LibroSegunReglasCompletoDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface LibrosSegunReglasCompletoRetrofitAPI
{

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasCompletoApi.RUTA_RESUELTA)
    fun consultarTodasLasLibroSegunReglasCompleto(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<LibroSegunReglasCompletoDTO>>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasCompletoApi.RuteoLibroSegunReglasCompletoApi.RUTA_RESUELTA)
    fun darLibroSegunReglasCompleto(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasCompletoApi.RuteoLibroSegunReglasCompletoApi.PARAMETRO_RUTA) idLibroSegunReglasCompleto: Long
                                   ): Call<LibroSegunReglasCompletoDTO>
}
