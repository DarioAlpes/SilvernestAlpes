package co.smartobjects.red.clientes.retrofit.clientes

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.clientes.LlaveNFCDTO
import org.threeten.bp.ZonedDateTime
import retrofit2.Call
import retrofit2.http.*

internal interface LlavesNFCRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoLlavesNFCApi.RUTA_RESUELTA)
    fun crear(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Body llaveNFC: LlaveNFCDTO
             ): Call<LlaveNFCDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoLlavesNFCApi.RUTA_RESUELTA)
    fun consultar(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Query(RuteoClientesApi.RuteoClienteApi.RuteoLlavesNFCApi.Operaciones.GET.fecha) fecha: ZonedDateTime
                 ): Call<LlaveNFCDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoLlavesNFCApi.RUTA_RESUELTA)
    fun eliminarHastaFechaCorte(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Query(RuteoClientesApi.RuteoClienteApi.RuteoLlavesNFCApi.Operaciones.GET.fecha) fecha: ZonedDateTime
                               ): Call<Unit>
}
