package co.smartobjects.red.clientes.retrofit.personas

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.personas.CampoDePersonaDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface CampoDePersonaRetrofitAPI
{
    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoCamposDePersonaApi.RuteoCampoDePersonaApi.RUTA_RESUELTA)
    fun actualizarCampoDePersona(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoCamposDePersonaApi.RuteoCampoDePersonaApi.PARAMETRO_RUTA) campo_campodepersona: Long,
            @Body campoDePersona: CampoDePersonaDTO
                                ): Call<CampoDePersonaDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoCamposDePersonaApi.RuteoCampoDePersonaApi.RUTA_RESUELTA)
    fun darCampoDePersona(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoCamposDePersonaApi.RuteoCampoDePersonaApi.PARAMETRO_RUTA) campo_campodepersona: Long
                         ): Call<CampoDePersonaDTO>
}
