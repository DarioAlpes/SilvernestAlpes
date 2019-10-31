package co.smartobjects.red.modelos.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.ZonedDateTime


data class ConteoUbicacionDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.idUbicacion, required = true)
        @param:JsonProperty(PropiedadesJSON.idUbicacion, required = true)
        val idUbicacion: Long,

        @get:JsonProperty(PropiedadesJSON.idSesionDeManilla, required = true)
        @param:JsonProperty(PropiedadesJSON.idSesionDeManilla, required = true)
        val idSesionDeManilla: Long,

        @get:JsonProperty(PropiedadesJSON.fechaDeRealizacion, required = true)
        @param:JsonProperty(PropiedadesJSON.fechaDeRealizacion, required = true)
        val fechaDeRealizacion: ZonedDateTime

) : EntidadDTO<ConteoUbicacion>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val idUbicacion = "location-id"
        const val idSesionDeManilla = "tag-session-id"
        const val fechaDeRealizacion = "location-count-timestamp"
    }

    object CodigosError : CodigosErrorDTO(10700)
    {
        @JvmField
        val FECHA_DE_REALIZACION_INVALIDA = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
    }

    constructor(conteoUbicacion: ConteoUbicacion)
            : this(
            conteoUbicacion.idCliente,
            conteoUbicacion.idUbicacion,
            conteoUbicacion.idSesionDeManilla,
            conteoUbicacion.fechaDeRealizacion
                  )


    override fun aEntidadDeNegocio(): ConteoUbicacion
    {
        return ConteoUbicacion(idCliente, idUbicacion, idSesionDeManilla, fechaDeRealizacion)
    }
}