package co.smartobjects.entidades.operativas.ordenes

import co.smartobjects.campos.*
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import org.threeten.bp.ZonedDateTime

class Orden private constructor(
        val idCliente: Long,
        val id: Long?,
        val idSesionDeManilla: Long,
        val campoTransacciones: CampoTransacciones,
        val campoFechaDeRealizacion: CampoFechaDeRealizacion
                               )
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Orden::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val TRANSACCIONES = Orden::transacciones.name
        @JvmField
        val FECHA_REALIZACION = Orden::fechaDeRealizacion.name
    }

    val transacciones: List<Transaccion> = campoTransacciones.valor
    val fechaDeRealizacion: ZonedDateTime = campoFechaDeRealizacion.valor


    constructor(
            idCliente: Long,
            id: Long?,
            idSesionDeManilla: Long,
            transacciones: List<Transaccion>,
            fechaDeRealizacion: ZonedDateTime
               )
            : this(
            idCliente,
            id,
            idSesionDeManilla,
            CampoTransacciones(transacciones),
            CampoFechaDeRealizacion(fechaDeRealizacion)
                  )

    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            idSesionDeManilla: Long = this.idSesionDeManilla,
            transacciones: List<Transaccion> = this.transacciones,
            fechaDeRealizacion: ZonedDateTime = this.fechaDeRealizacion
              ): Orden
    {
        return Orden(idCliente, id, idSesionDeManilla, transacciones, fechaDeRealizacion)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Orden

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (idSesionDeManilla != other.idSesionDeManilla) return false
        if (transacciones != other.transacciones) return false
        if (fechaDeRealizacion != other.fechaDeRealizacion) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + idSesionDeManilla.hashCode()
        result = 31 * result + transacciones.hashCode()
        result = 31 * result + fechaDeRealizacion.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Orden(idCliente=$idCliente, id=$id, idSesionDeManilla=$idSesionDeManilla, transacciones=$transacciones, fechaDeRealizacion=$fechaDeRealizacion)"
    }

    class CampoTransacciones(creditos: List<Transaccion>)
        : CampoEntidad<Orden, List<Transaccion>>(creditos, ValidadorCampoColeccionNoVacio(), NOMBRE_ENTIDAD, Campos.TRANSACCIONES)

    class CampoFechaDeRealizacion(fechaDeRealizacion: ZonedDateTime)
        : CampoEntidad<Orden, ZonedDateTime>(
            fechaDeRealizacion,
            ValidadorEnCadena<ZonedDateTime>(validadorDeZonaHoraria, ValidadorCampoConLimiteInferior(FECHA_MINIMA_CREACION)),
            NOMBRE_ENTIDAD,
            Campos.FECHA_REALIZACION
                                            )
}