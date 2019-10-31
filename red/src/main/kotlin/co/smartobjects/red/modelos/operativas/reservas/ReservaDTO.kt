package co.smartobjects.red.modelos.operativas.reservas

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.reservas.Reserva
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ReservaDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id, required = true)
        val id: String,

        @get:JsonProperty(TransaccionEntidadTerminadaDTO.PropiedadesJSON.creacionTerminada)
        val creacionTerminada: Boolean = false,

        @get:JsonProperty(PropiedadesJSON.numeroDeReserva)
        val numeroDeReserva: Long? = null,

        @get:JsonProperty(PropiedadesJSON.sesionesDeManilla, required = true)
        val sesionesDeManilla: List<SesionDeManillaDTO>
) : EntidadDTO<Reserva>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val numeroDeReserva = "reservation-number"
        const val sesionesDeManilla = "tag-sessions"
    }

    object CodigosError : CodigosErrorDTO(20000)
    {
        @JvmField
        val SESIONES_DE_MANILLAS_INVALIDAS = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1

        @JvmField
        val ESTA_MARCADA_CON_CREACION_TERMINADA = SESIONES_DE_MANILLAS_INVALIDAS + 1
    }

    constructor(reserva: Reserva) :
            this(
                    reserva.idCliente,
                    reserva.id,
                    reserva.creacionTerminada,
                    reserva.numeroDeReserva,
                    reserva.sesionesDeManilla.map { SesionDeManillaDTO(it) }
                )

    override fun aEntidadDeNegocio(): Reserva
    {
        val partesId = EntidadTransaccional.idAPartes(id)
        return Reserva(
                idCliente,
                partesId.nombreUsuario,
                partesId.uuid,
                partesId.tiempoCreacion,
                creacionTerminada,
                numeroDeReserva,
                sesionesDeManilla.map { it.aEntidadDeNegocio() }
                      )
    }
}