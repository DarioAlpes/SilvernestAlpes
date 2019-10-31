package co.smartobjects.red.clientes.retrofit.fondos.libros

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.libros.LibroDeProhibicionesDTO
import retrofit2.Call
import retrofit2.http.*

internal interface LibrosDeProhibicionesRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDeProhibicionesApi.RUTA_RESUELTA)
    fun crearLibroDeProhibiciones(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Body libroDeProhibiciones: LibroDeProhibicionesDTO
                                 ): Call<LibroDeProhibicionesDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDeProhibicionesApi.RUTA_RESUELTA)
    fun consultarTodasLasLibroDeProhibiciones(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<LibroDeProhibicionesDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDeProhibicionesApi.RuteoLibroDeProhibicionesApi.RUTA_RESUELTA)
    fun actualizarLibroDeProhibiciones(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDeProhibicionesApi.RuteoLibroDeProhibicionesApi.PARAMETRO_RUTA) idLibroDeProhibiciones: Long,
            @Body libroDeProhibiciones: LibroDeProhibicionesDTO
                                      ): Call<LibroDeProhibicionesDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDeProhibicionesApi.RuteoLibroDeProhibicionesApi.RUTA_RESUELTA)
    fun darLibroDeProhibiciones(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDeProhibicionesApi.RuteoLibroDeProhibicionesApi.PARAMETRO_RUTA) idLibroDeProhibiciones: Long
                               ): Call<LibroDeProhibicionesDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDeProhibicionesApi.RuteoLibroDeProhibicionesApi.RUTA_RESUELTA)
    fun eliminarLibroDeProhibiciones(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDeProhibicionesApi.RuteoLibroDeProhibicionesApi.PARAMETRO_RUTA) idLibroDeProhibiciones: Long
                                    ): Call<Unit>
}
