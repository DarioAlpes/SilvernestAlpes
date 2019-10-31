package co.smartobjects.red.clientes.retrofit.ubicaciones.contabilizables

import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.ubicaciones.contabilizables.ConteoUbicacionDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface ConteosEnUbicacionRetrofitAPI
{
    @POST(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.RuteoConteosEnUbicacionApi.RUTA_RESUELTA)
    fun crearConteoUbicacion(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoUbicacionesApi.RuteoUbicacionApi.PARAMETRO_RUTA) idUbicacion: Long,
            @Body conteoUbicacion: ConteoUbicacionDTO
                            ): Call<ConteoUbicacionDTO>
}
