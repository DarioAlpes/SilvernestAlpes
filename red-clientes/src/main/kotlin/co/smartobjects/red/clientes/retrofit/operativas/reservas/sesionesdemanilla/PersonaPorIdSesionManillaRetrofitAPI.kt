package co.smartobjects.red.clientes.retrofit.operativas.reservas.sesionesdemanilla

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.personas.PersonaDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

internal interface PersonaPorIdSesionManillaRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoSesionesDeManillaApi.RuteoSesionDeManillaApi.RuteoPersonaPorIdSesionManillaApi.RUTA_RESUELTA)
    fun darPersona(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoSesionesDeManillaApi.RuteoSesionDeManillaApi.PARAMETRO_RUTA) idSesionDeManilla: Long
                  ): Call<PersonaDTO>
}
