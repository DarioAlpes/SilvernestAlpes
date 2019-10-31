package co.smartobjects.red.clientes.retrofit.operativas.reservas

import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.red.clientes.retrofit.RuteoClientesApi
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.reservas.ReservaDTO
import retrofit2.Call
import retrofit2.http.*

internal interface ReservasRetrofitAPI
{
    @GET(RuteoClientesApi.RuteoClienteApi.RuteoReservasApi.RUTA_RESUELTA)
    fun consultarTodasLasReserva(@Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long): Call<List<ReservaDTO>>

    @PUT(RuteoClientesApi.RuteoClienteApi.RuteoReservasApi.RuteoReservaApi.RUTA_RESUELTA)
    fun actualizarReserva(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoReservasApi.RuteoReservaApi.PARAMETRO_RUTA) idReserva: String,
            @Body reserva: ReservaDTO
                         ): Call<ReservaDTO>

    @GET(RuteoClientesApi.RuteoClienteApi.RuteoReservasApi.RuteoReservaApi.RUTA_RESUELTA)
    fun darReserva(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoReservasApi.RuteoReservaApi.PARAMETRO_RUTA) idReserva: String
                  ): Call<ReservaDTO>

    @DELETE(RuteoClientesApi.RuteoClienteApi.RuteoReservasApi.RuteoReservaApi.RUTA_RESUELTA)
    fun eliminarReserva(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoReservasApi.RuteoReservaApi.PARAMETRO_RUTA) idReserva: String
                       ): Call<Unit>

    @PATCH(RuteoClientesApi.RuteoClienteApi.RuteoReservasApi.RuteoReservaApi.RUTA_RESUELTA)
    fun actualizarPorCamposReserva(
            @Path(RuteoClientesApi.RuteoClienteApi.PARAMETRO_RUTA) idCliente: Long,
            @Path(RuteoClientesApi.RuteoClienteApi.RuteoReservasApi.RuteoReservaApi.PARAMETRO_RUTA) idReserva: String,
            @Body reserva: TransaccionEntidadTerminadaDTO<Reserva>
                                  ): Call<Unit>
}
