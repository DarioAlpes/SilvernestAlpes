package co.smartobjects.red.clientes.retrofit.personas

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.personas.PersonaConFamiliaresDTO
import co.smartobjects.red.modelos.personas.PersonaDTO
import retrofit2.Call
import retrofit2.http.*

internal interface PersonasRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RUTA_RESUELTA)
    fun crearPersona(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long, @Body persona: PersonaDTO): Call<PersonaDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RUTA_RESUELTA)
    fun consultarTodasLasPersona(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<PersonaDTO>>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoPersonaPorDocumentoApi.RUTA_RESUELTA)
    fun consultarPorDocumento(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Query(RuteoClientesApi.RuteoClienteApi.RuteoPersonaPorDocumentoApi.Operaciones.GET.numeroDocumento) numeroDocumento: String,
            @Query(RuteoClientesApi.RuteoClienteApi.RuteoPersonaPorDocumentoApi.Operaciones.GET.tipoDocumentoStr) tipoDocumentoStr: String
                             ): Call<PersonaConFamiliaresDTO>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.RUTA_RESUELTA)
    fun actualizarPersona(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.PARAMETRO_RUTA) idPersona: Long,
            @Body persona: PersonaDTO
                         ): Call<PersonaDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.RUTA_RESUELTA)
    fun darPersona(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.PARAMETRO_RUTA) idPersona: Long
                  ): Call<PersonaDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.RUTA_RESUELTA)
    fun eliminarPersona(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoPersonasApi.RuteoPersonaApi.PARAMETRO_RUTA) idPersona: Long
                       ): Call<Unit>
}
