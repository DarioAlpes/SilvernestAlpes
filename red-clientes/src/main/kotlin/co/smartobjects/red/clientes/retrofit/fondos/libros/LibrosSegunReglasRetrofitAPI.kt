package co.smartobjects.red.clientes.retrofit.fondos.libros

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.libros.LibroSegunReglasDTO
import retrofit2.Call
import retrofit2.http.*

internal interface LibrosSegunReglasRetrofitAPI
{

    @POST(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasApi.RUTA_RESUELTA)
    fun crearLibroSegunReglas(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body libroSegunReglas: LibroSegunReglasDTO): Call<LibroSegunReglasDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasApi.RUTA_RESUELTA)
    fun consultarTodasLasLibroSegunReglas(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<LibroSegunReglasDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasApi.RuteoLibroSegunReglasApi.RUTA_RESUELTA)
    fun actualizarLibroSegunReglas(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasApi.RuteoLibroSegunReglasApi.PARAMETRO_RUTA) idLibroSegunReglas: Long,
            @Body libroSegunReglas: LibroSegunReglasDTO
                                  ): Call<LibroSegunReglasDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasApi.RuteoLibroSegunReglasApi.RUTA_RESUELTA)
    fun darLibroSegunReglas(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasApi.RuteoLibroSegunReglasApi.PARAMETRO_RUTA) idLibroSegunReglas: Long
                           ): Call<LibroSegunReglasDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasApi.RuteoLibroSegunReglasApi.RUTA_RESUELTA)
    fun eliminarLibroSegunReglas(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosSegunReglasApi.RuteoLibroSegunReglasApi.PARAMETRO_RUTA) idLibroSegunReglas: Long
                                ): Call<Unit>
}
