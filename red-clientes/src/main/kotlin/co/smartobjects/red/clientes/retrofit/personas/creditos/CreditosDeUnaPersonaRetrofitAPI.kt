package co.smartobjects.red.clientes.retrofit.personas.creditos

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.operativas.compras.CreditosDeUnaPersonaDTO
import org.threeten.bp.ZonedDateTime
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface CreditosDeUnaPersonaRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.RuteoCreditosDeUnaPersonaApi.RUTA_RESUELTA)
    fun buscar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.PARAMETRO_RUTA) idPersona: Long,
            @Query(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.RuteoCreditosDeUnaPersonaApi.Operaciones.GET.fecha) fecha: ZonedDateTime
              ): Call<CreditosDeUnaPersonaDTO>
}
