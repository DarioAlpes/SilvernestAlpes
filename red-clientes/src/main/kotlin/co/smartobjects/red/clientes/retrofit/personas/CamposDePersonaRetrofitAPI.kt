package co.smartobjects.red.clientes.retrofit.personas

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.personas.CampoDePersonaDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface CamposDePersonaRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoCamposDePersonaApi.RUTA_RESUELTA)
    fun consultarTodasLasCampoDePersona(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<CampoDePersonaDTO>>
}
