package co.smartobjects.entidades.operativas.reservas

import co.smartobjects.campos.*
import org.threeten.bp.ZonedDateTime
import java.util.*

class SesionDeManilla private constructor(
        val idCliente: Long,
        val id: Long?,
        val idPersona: Long,
        val campoUuidTag: CampoUuidTag?,
        val campoFechaActivacion: CampoFechaActivacion?,
        val campoFechaDesactivacion: CampoFechaDesactivacion?,
        val campoIdsCreditosCodificados: CampoIdsCreditosCodificados
                                         )
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = SesionDeManilla::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val UUID_TAG = SesionDeManilla::uuidTag.name
        @JvmField
        val FECHA_ACTIVACION = SesionDeManilla::fechaActivacion.name
        @JvmField
        val FECHA_DESACTIVACION = SesionDeManilla::fechaDesactivacion.name
        @JvmField
        val IDS_CREDITOS_CODIFICADOS = SesionDeManilla::idsCreditosCodificados.name
    }

    val uuidTag = campoUuidTag?.valor
    val fechaActivacion = campoFechaActivacion?.valor
    val fechaDesactivacion = campoFechaDesactivacion?.valor
    val idsCreditosCodificados = campoIdsCreditosCodificados.valor

    constructor(
            idCliente: Long,
            id: Long?,
            idPersona: Long,
            uuidTag: ByteArray?,
            fechaActivacion: ZonedDateTime?,
            fechaDesactivacion: ZonedDateTime?,
            idsCreditosCodificados: Set<Long>
               )
            : this(
            idCliente,
            id,
            idPersona,
            uuidTag?.let { CampoUuidTag(it) },
            if (fechaActivacion != null) CampoFechaActivacion(fechaActivacion, fechaDesactivacion) else null,
            if (fechaDesactivacion != null) CampoFechaDesactivacion(fechaActivacion, fechaDesactivacion) else null,
            CampoIdsCreditosCodificados(idsCreditosCodificados)
                  )

    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            idPersona: Long = this.idPersona,
            uuidTag: ByteArray? = this.uuidTag,
            fechaActivacion: ZonedDateTime? = this.fechaActivacion,
            fechaDesactivacion: ZonedDateTime? = this.fechaDesactivacion,
            idsCreditosInicialmenteCodificados: Set<Long> = this.idsCreditosCodificados
              ): SesionDeManilla
    {
        return SesionDeManilla(idCliente, id, idPersona, uuidTag, fechaActivacion, fechaDesactivacion, idsCreditosInicialmenteCodificados)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SesionDeManilla

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (idPersona != other.idPersona) return false
        if (!Arrays.equals(uuidTag, other.uuidTag)) return false
        if (fechaActivacion != other.fechaActivacion) return false
        if (fechaDesactivacion != other.fechaDesactivacion) return false
        if (idsCreditosCodificados != other.idsCreditosCodificados) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + idPersona.hashCode()
        result = 31 * result + (uuidTag?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + fechaActivacion.hashCode()
        result = 31 * result + fechaDesactivacion.hashCode()
        result = 31 * result + idsCreditosCodificados.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "SesionDeManilla(idCliente=$idCliente, id=$id, idPersona=$idPersona, uuidTag=${Arrays.toString(uuidTag)}, fechaActivacion=$fechaActivacion, fechaDesactivacion=$fechaDesactivacion, idsCreditosCodificados=$idsCreditosCodificados)"
    }

    class CampoUuidTag(uuidDeTag: ByteArray)
        : CampoModificable<SesionDeManilla, ByteArray>(uuidDeTag, ValidadorCampoArregloBytesNoVacio(), NOMBRE_ENTIDAD, Campos.UUID_TAG)

    class CampoFechaActivacion(fechaActivacion: ZonedDateTime, fechaDesactivacion: ZonedDateTime?)
        : CampoEntidad<SesionDeManilla, ZonedDateTime>(
            fechaActivacion,
            ValidadorEnCadena(
                    validadorDeZonaHoraria,
                    *(
                            if (fechaDesactivacion != null)
                            {
                                arrayOf<ValidadorCampo<ZonedDateTime>>(ValidadorCampoEsMenorOIgualQueOtroCampo(fechaDesactivacion, Campos.FECHA_DESACTIVACION))
                            }
                            else
                            {
                                arrayOf()
                            }
                     )
                             ),
            NOMBRE_ENTIDAD,
            Campos.FECHA_ACTIVACION)

    class CampoFechaDesactivacion(fechaActivacion: ZonedDateTime?, fechaDesactivacion: ZonedDateTime)
        : CampoModificable<SesionDeManilla, ZonedDateTime>(
            fechaDesactivacion,
            ValidadorEnCadena(validadorDeZonaHoraria, ValidadorCampoDeReferenciaNoNulo(fechaActivacion, Campos.FECHA_ACTIVACION)),
            NOMBRE_ENTIDAD,
            Campos.FECHA_DESACTIVACION)

    class CampoIdsCreditosCodificados(idsCreditosCodificados: Set<Long>)
        : CampoEntidad<SesionDeManilla, Set<Long>>(idsCreditosCodificados, ValidadorCampoColeccionNoVacio(), NOMBRE_ENTIDAD, Campos.IDS_CREDITOS_CODIFICADOS)
}