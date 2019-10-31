package co.smartobjects.red.clientes.retrofit.personas

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.personas.ValorGrupoEdadDTO
import retrofit2.Call
import retrofit2.http.*

internal interface ValoresGrupoEdadRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoValoresGrupoEdadApi.RUTA_RESUELTA)
    fun crearValorGrupoEdad(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Body valorGrupoEdad: ValorGrupoEdadDTO
                           ): Call<ValorGrupoEdadDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoValoresGrupoEdadApi.RUTA_RESUELTA)
    fun consultarTodasLasValorGrupoEdad(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<ValorGrupoEdadDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoValoresGrupoEdadApi.RuteoValorGrupoEdadApi.RUTA_RESUELTA)
    fun actualizarValorGrupoEdad(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoValoresGrupoEdadApi.RuteoValorGrupoEdadApi.PARAMETRO_RUTA) valor_valorgrupoedad: Long,
            @Body valorGrupoEdad: ValorGrupoEdadDTO
                                ): Call<ValorGrupoEdadDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoValoresGrupoEdadApi.RuteoValorGrupoEdadApi.RUTA_RESUELTA)
    fun darValorGrupoEdad(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoValoresGrupoEdadApi.RuteoValorGrupoEdadApi.PARAMETRO_RUTA) valor_valorgrupoedad: Long
                         ): Call<ValorGrupoEdadDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoValoresGrupoEdadApi.RuteoValorGrupoEdadApi.RUTA_RESUELTA)
    fun eliminarValorGrupoEdad(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoValoresGrupoEdadApi.RuteoValorGrupoEdadApi.PARAMETRO_RUTA) valor_valorgrupoedad: Long
                              ): Call<Unit>
}
