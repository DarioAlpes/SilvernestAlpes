package co.smartobjects.red.modelos.operativas.reservas

import co.smartobjects.campos.CampoModificable
import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.red.modelos.EntidadDTOParcial
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.ZonedDateTime

data class SesionDeManillaPatchDTO @JsonCreator constructor
(
        @get:JsonProperty(SesionDeManillaDTO.PropiedadesJSON.uuidTag)
        @param:JsonProperty(SesionDeManillaDTO.PropiedadesJSON.uuidTag)
        val uuidTag: ByteArray?,

        @get:JsonProperty(SesionDeManillaDTO.PropiedadesJSON.fechaDesactivacion)
        @param:JsonProperty(SesionDeManillaDTO.PropiedadesJSON.fechaDesactivacion)
        val fechaDesactivacion: ZonedDateTime?
) : EntidadDTOParcial<SesionDeManilla>
{
    override fun aConjuntoCamposModificables(): Set<CampoModificable<SesionDeManilla, *>>
    {
        val camposAActualizar = mutableSetOf<CampoModificable<SesionDeManilla, *>>()

        if (uuidTag != null)
        {
            camposAActualizar.add(SesionDeManilla.CampoUuidTag(uuidTag))
        }

        if (fechaDesactivacion != null)
        {
            // Se usa una fecha de activación dummy para que activación < desactivación
            camposAActualizar.add(SesionDeManilla.CampoFechaDesactivacion(fechaDesactivacion.minusYears(1), fechaDesactivacion))
        }

        return camposAActualizar
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SesionDeManillaPatchDTO

        if (uuidTag != null)
        {
            if (other.uuidTag == null) return false
            if (!uuidTag.contentEquals(other.uuidTag)) return false
        }
        else if (other.uuidTag != null) return false
        if (fechaDesactivacion != other.fechaDesactivacion) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = uuidTag?.contentHashCode() ?: 0
        result = 31 * result + fechaDesactivacion.hashCode()
        return result
    }
}