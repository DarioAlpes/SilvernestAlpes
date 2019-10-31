package co.smartobjects.red.clientes.retrofit.personas

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.personas.PersonaDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface PersonasDeUnaCompraRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoPersonasDeUnaCompraApi.RUTA_RESUELTA)
    fun listar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Query(RuteoClientesApi.RuteoClienteApi.RuteoPersonasDeUnaCompraApi.Operaciones.GET.numeroTransaccion) numeroTransaccion: String
              ): Call<List<PersonaDTO>>
}
