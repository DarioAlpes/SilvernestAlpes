package co.smartobjects.red.clientes.operativas.reservas

import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.red.clientes.ParserRespuestasRetrofit
import co.smartobjects.red.clientes.base.*
import co.smartobjects.red.clientes.retrofit.operativas.reservas.ReservasRetrofitAPI
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import co.smartobjects.red.modelos.operativas.reservas.ReservaDTO

interface ReservasAPI
    : ConsultarAPI<List<Reserva>>,
      ActualizarPorParametrosAPI<String, Reserva, Reserva>,
      ConsultarPorParametrosAPI<String, Reserva>,
      EliminarPorParametrosAPI<String, Reserva>,
      ActualizarCamposPorParametrosAPI<String, TransaccionEntidadTerminadaDTO<Reserva>>

internal class ReservasAPIRetrofit
(
        private val apiDeReservas: ReservasRetrofitAPI,
        private val parserRespuestasRetrofit: ParserRespuestasRetrofit,
        private val idCliente: Long
) : ReservasAPI
{
    override fun consultar(): RespuestaIndividual<List<Reserva>>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualColeccionDesdeDTO {
            apiDeReservas
                .consultarTodasLasReserva(idCliente)
                .execute()
        }
    }

    override fun actualizar(parametros: String, entidad: Reserva): RespuestaIndividual<Reserva>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeReservas
                .actualizarReserva(idCliente, parametros, ReservaDTO(entidad))
                .execute()
        }
    }

    override fun consultar(parametros: String): RespuestaIndividual<Reserva>
    {
        return parserRespuestasRetrofit.haciaRespuestaIndividualDesdeDTO {
            apiDeReservas
                .darReserva(idCliente, parametros)
                .execute()
        }
    }

    override fun eliminar(parametros: String): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeReservas
                .eliminarReserva(idCliente, parametros)
                .execute()
        }
    }

    override fun actualizarCampos(parametros: String, entidad: TransaccionEntidadTerminadaDTO<Reserva>): RespuestaVacia
    {
        return parserRespuestasRetrofit.haciaRespuestaVacia {
            apiDeReservas
                .actualizarPorCamposReserva(idCliente, parametros, entidad)
                .execute()
        }
    }
}
