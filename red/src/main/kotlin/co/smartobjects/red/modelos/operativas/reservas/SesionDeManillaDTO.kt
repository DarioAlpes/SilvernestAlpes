package co.smartobjects.red.modelos.operativas.reservas

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.ZonedDateTime
import java.util.*

data class SesionDeManillaDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id)
        @param:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.idPersona, required = true)
        @param:JsonProperty(PropiedadesJSON.idPersona, required = true)
        val idPersona: Long,

        @get:JsonProperty(PropiedadesJSON.uuidTag)
        @param:JsonProperty(PropiedadesJSON.uuidTag)
        val uuidTag: ByteArray? = null,

        @get:JsonProperty(PropiedadesJSON.fechaActivacion)
        @param:JsonProperty(PropiedadesJSON.fechaActivacion)
        val fechaActivacion: ZonedDateTime? = null,

        @get:JsonProperty(PropiedadesJSON.fechaDesactivacion)
        @param:JsonProperty(PropiedadesJSON.fechaDesactivacion)
        val fechaDesactivacion: ZonedDateTime? = null,

        @get:JsonProperty(PropiedadesJSON.idsCreditosCodificados, required = true)
        @param:JsonProperty(PropiedadesJSON.idsCreditosCodificados, required = true)
        val idsCreditosCodificados: List<Long>
) : EntidadDTO<SesionDeManilla>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val idPersona = "person-id"
        const val uuidTag = "tag-uuid"
        const val fechaActivacion = "activation-date"
        const val fechaDesactivacion = "deactivation-date"
        const val idsCreditosCodificados = "credits-ids-to-encode"
    }

    object CodigosError : CodigosErrorDTO(20100)
    {
        @JvmField
        val UUID_TAG_VACIO = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1

        @JvmField
        val FECHA_DE_ACTIVACION_INVALIDA = UUID_TAG_VACIO + 1

        @JvmField
        val FECHA_DE_DESACTIVACION_INVALIDA = FECHA_DE_ACTIVACION_INVALIDA + 1

        @JvmField
        val IDS_CREDITOS_A_CODIFICAR_VACIOS = FECHA_DE_DESACTIVACION_INVALIDA + 1

        @JvmField
        val SESION_YA_TIENE_TAG_ASOCIADO = IDS_CREDITOS_A_CODIFICAR_VACIOS + 1
    }

    constructor(entidadDeNegocio: SesionDeManilla) :
            this(
                    entidadDeNegocio.idCliente,
                    entidadDeNegocio.id,
                    entidadDeNegocio.idPersona,
                    entidadDeNegocio.uuidTag,
                    entidadDeNegocio.fechaActivacion,
                    entidadDeNegocio.fechaDesactivacion,
                    entidadDeNegocio.idsCreditosCodificados.toList()
                )

    override fun aEntidadDeNegocio(): SesionDeManilla
    {
        return SesionDeManilla(
                idCliente,
                id,
                idPersona,
                uuidTag,
                fechaActivacion,
                fechaDesactivacion,
                idsCreditosCodificados.toSet()
                              )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SesionDeManillaDTO

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
}