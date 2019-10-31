package co.smartobjects.entidades.operativas.reservas

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoColeccionNoVacio
import co.smartobjects.campos.ValidadorCampoColeccionSinRepetidos
import co.smartobjects.campos.ValidadorEnCadena
import co.smartobjects.entidades.operativas.EntidadTransaccional
import java.util.*

class Reserva private constructor(
        val idCliente: Long,
        nombreUsuario: String,
        uuid: UUID?,
        tiempoCreacion: Long?,
        creacionTerminada: Boolean?,
        val numeroDeReserva: Long?,
        val campoSesionesDeManilla: CampoSesionesDeManilla)
    : EntidadTransaccional<Reserva>(nombreUsuario, uuid, tiempoCreacion, creacionTerminada)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Reserva::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val SESIONES_DE_MANILLA = Reserva::sesionesDeManilla.name
    }

    val sesionesDeManilla = campoSesionesDeManilla.valor

    // Constructor a usar para crear nueva entidad y obtener id según campos
    constructor(
            idCliente: Long,
            nombreUsuario: String,
            sesionesDeManilla: List<SesionDeManilla>)
            : this(idCliente,
                   nombreUsuario,
                   null,
                   null,
                   null,
                   null,
                   CampoSesionesDeManilla(sesionesDeManilla)
                  )

    // Constructor a usar para copiar y crear una entidad previamente guardada
    constructor(
            idCliente: Long,
            nombreUsuario: String,
            uuid: UUID,
            tiempoCreacion: Long,
            creacionTerminada: Boolean,
            numeroDeReserva: Long?,
            sesionesDeManilla: List<SesionDeManilla>)
            : this(idCliente,
                   nombreUsuario,
                   uuid,
                   tiempoCreacion,
                   creacionTerminada,
                   numeroDeReserva,
                   CampoSesionesDeManilla(sesionesDeManilla)
                  )

    fun copiar(
            idCliente: Long = this.idCliente,
            creacionTerminada: Boolean? = this.creacionTerminada,
            numeroDeReserva: Long? = this.numeroDeReserva,
            sesionesDeManilla: List<SesionDeManilla> = this.sesionesDeManilla
              ): Reserva
    {
        return Reserva(
                idCliente,
                nombreUsuario,
                uuid,
                tiempoCreacion,
                creacionTerminada,
                numeroDeReserva,
                CampoSesionesDeManilla(sesionesDeManilla)
                      )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Reserva

        if (idCliente != other.idCliente) return false
        if (numeroDeReserva != other.numeroDeReserva) return false
        if (sesionesDeManilla != other.sesionesDeManilla) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + idCliente.hashCode()
        result = 31 * result + numeroDeReserva.hashCode()
        result = 31 * result + sesionesDeManilla.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Reserva(idCliente=$idCliente, numeroDeReserva=$numeroDeReserva, sesionesDeManilla=$sesionesDeManilla) ${super.toString()}"
    }

    class CampoSesionesDeManilla(sesionesDeManilla: List<SesionDeManilla>)
        : CampoEntidad<Reserva, List<SesionDeManilla>>(
            sesionesDeManilla,
            ValidadorEnCadena(
                    ValidadorCampoColeccionNoVacio(),
                    ValidadorCampoColeccionSinRepetidos(false, "id de la persona") { it.idPersona },
                    ValidadorCampoColeccionSinRepetidos(true, "UUID de tag no nulo") { it.uuidTag }
                             ),
            NOMBRE_ENTIDAD,
            Campos.SESIONES_DE_MANILLA
                                                      )
}