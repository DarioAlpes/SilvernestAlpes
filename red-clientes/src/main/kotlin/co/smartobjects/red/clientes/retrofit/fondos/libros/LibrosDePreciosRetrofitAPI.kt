package co.smartobjects.red.clientes.retrofit.fondos.libros

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.fondos.libros.LibroDePreciosDTO
import retrofit2.Call
import retrofit2.http.*

internal interface LibrosDePreciosRetrofitAPI
{

    @POST(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDePreciosApi.RUTA_RESUELTA)
    fun crearLibroDePrecios(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Body libroDePrecios: LibroDePreciosDTO
                           ): Call<LibroDePreciosDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDePreciosApi.RUTA_RESUELTA)
    fun consultarTodasLasLibroDePrecios(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<LibroDePreciosDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDePreciosApi.RuteoLibroDePreciosApi.RUTA_RESUELTA)
    fun actualizarLibroDePrecios(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDePreciosApi.RuteoLibroDePreciosApi.PARAMETRO_RUTA) idLibroDePrecios: Long,
            @Body libroDePrecios: LibroDePreciosDTO
                                ): Call<LibroDePreciosDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDePreciosApi.RuteoLibroDePreciosApi.RUTA_RESUELTA)
    fun darLibroDePrecios(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDePreciosApi.RuteoLibroDePreciosApi.PARAMETRO_RUTA) idLibroDePrecios: Long
                         ): Call<LibroDePreciosDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDePreciosApi.RuteoLibroDePreciosApi.RUTA_RESUELTA)
    fun eliminarLibroDePrecios(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoLibrosDePreciosApi.RuteoLibroDePreciosApi.PARAMETRO_RUTA) idLibroDePrecios: Long
                              ): Call<Unit>
}
