package co.smartobjects.entidades.operativas.ordenes

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoColeccionNoVacio
import co.smartobjects.entidades.operativas.EntidadTransaccional
import java.util.*

class LoteDeOrdenes private constructor(
        val idCliente: Long,
        nombreUsuario: String,
        uuid: UUID?,
        tiempoCreacion: Long?,
        creacionTerminada: Boolean?,
        val campoOrdenes: CampoOrdenes)
    : EntidadTransaccional<LoteDeOrdenes>(nombreUsuario, uuid, tiempoCreacion, creacionTerminada)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = LoteDeOrdenes::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val ORDENES = LoteDeOrdenes::ordenes.name
    }

    val ordenes = campoOrdenes.valor

    // Constructor a usar para crear nueva entidad y obtener id según campos
    constructor(
            idCliente: Long,
            nombreUsuario: String,
            ordenes: List<Orden>)
            : this(idCliente,
                   nombreUsuario,
                   null,
                   null,
                   null,
                   CampoOrdenes(ordenes)
                  )

    // Constructor a usar para copiar y crear una entidad previamente guardada
    constructor(
            idCliente: Long,
            nombreUsuario: String,
            uuid: UUID,
            tiempoCreacion: Long,
            creacionTerminada: Boolean,
            ordenes: List<Orden>)
            : this(idCliente,
                   nombreUsuario,
                   uuid,
                   tiempoCreacion,
                   creacionTerminada,
                   CampoOrdenes(ordenes)
                  )

    fun copiar(
            idCliente: Long = this.idCliente,
            creacionTerminada: Boolean? = this.creacionTerminada,
            ordenes: List<Orden> = this.ordenes
              ): LoteDeOrdenes
    {
        return LoteDeOrdenes(
                idCliente,
                nombreUsuario,
                uuid,
                tiempoCreacion,
                creacionTerminada,
                CampoOrdenes(ordenes)
                            )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as LoteDeOrdenes

        if (idCliente != other.idCliente) return false
        if (ordenes != other.ordenes) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + idCliente.hashCode()
        result = 31 * result + ordenes.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "LoteDeOrdenes(idCliente=$idCliente, ordenes=$ordenes) ${super.toString()}"
    }

    class CampoOrdenes(ordenes: List<Orden>)
        : CampoEntidad<LoteDeOrdenes, List<Orden>>(ordenes, ValidadorCampoColeccionNoVacio(), NOMBRE_ENTIDAD, Campos.ORDENES)
}