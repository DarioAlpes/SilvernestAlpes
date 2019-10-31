package co.smartobjects.entidades.ubicaciones.contabilizables

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.ValidadorCampoConLimiteInferior
import co.smartobjects.campos.ValidadorEnCadena
import co.smartobjects.campos.validadorDeZonaHoraria
import co.smartobjects.utilidades.FECHA_MINIMA_CREACION
import org.threeten.bp.ZonedDateTime


class ConteoUbicacion private constructor
(
        val idCliente: Long,
        val idUbicacion: Long,
        val idSesionDeManilla: Long,
        val campoFechaDeRealizacion: CampoFechaDeRealizacion
)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = ConteoUbicacion::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val FECHA_REALIZACION = ConteoUbicacion::fechaDeRealizacion.name
    }


    val fechaDeRealizacion: ZonedDateTime = campoFechaDeRealizacion.valor


    constructor(
            idCliente: Long,
            idUbicacion: Long,
            idSesionDeManilla: Long,
            fechaDeRealizacion: ZonedDateTime
               )
            : this(
            idCliente,
            idUbicacion,
            idSesionDeManilla,
            CampoFechaDeRealizacion(fechaDeRealizacion)
                  )

    fun copiar(
            idCliente: Long = this.idCliente,
            idUbicacion: Long = this.idUbicacion,
            idSesionDeManilla: Long = this.idSesionDeManilla,
            fechaDeRealizacion: ZonedDateTime = this.fechaDeRealizacion
              ): ConteoUbicacion
    {
        return ConteoUbicacion(idCliente, idUbicacion, idSesionDeManilla, fechaDeRealizacion)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConteoUbicacion

        if (idCliente != other.idCliente) return false
        if (idUbicacion != other.idUbicacion) return false
        if (idSesionDeManilla != other.idSesionDeManilla) return false
        if (fechaDeRealizacion != other.fechaDeRealizacion) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + idUbicacion.hashCode()
        result = 31 * result + idSesionDeManilla.hashCode()
        result = 31 * result + fechaDeRealizacion.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "ConteoUbicacion(idCliente=$idCliente, idUbicacion=$idUbicacion, idSesionDeManilla=$idSesionDeManilla, fechaDeRealizacion=$fechaDeRealizacion)"
    }


    class CampoFechaDeRealizacion(fechaDeRealizacion: ZonedDateTime)
        : CampoEntidad<ConteoUbicacion, ZonedDateTime>(
            fechaDeRealizacion,
            ValidadorEnCadena<ZonedDateTime>(validadorDeZonaHoraria, ValidadorCampoConLimiteInferior(FECHA_MINIMA_CREACION)),
            ConteoUbicacion.NOMBRE_ENTIDAD,
            ConteoUbicacion.Campos.FECHA_REALIZACION
                                                      )
}