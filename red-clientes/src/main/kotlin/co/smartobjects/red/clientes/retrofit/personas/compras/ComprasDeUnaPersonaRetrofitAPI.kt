package co.smartobjects.red.clientes.retrofit.personas.compras

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.operativas.compras.CompraDTO
import org.threeten.bp.ZonedDateTime
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface ComprasDeUnaPersonaRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.RuteoComprasDeUnaPersonaApi.RUTA_RESUELTA)
    fun listar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.PARAMETRO_RUTA) idPersona: Long,
            @Query(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.RuteoComprasDeUnaPersonaApi.Operaciones.GET.fecha) fecha: ZonedDateTime
              ): Call<List<CompraDTO>>
}
