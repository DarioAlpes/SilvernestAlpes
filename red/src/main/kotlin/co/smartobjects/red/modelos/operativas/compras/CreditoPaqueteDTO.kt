package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CreditoPaqueteDTO @JsonCreator constructor
(
        @get:JsonProperty(PropiedadesJSON.idPaquete, required = true)
        @param:JsonProperty(PropiedadesJSON.idPaquete, required = true)
        val idPaquete: Long,

        @get:JsonProperty(PropiedadesJSON.codigoExternoFondo, required = true)
        @param:JsonProperty(PropiedadesJSON.codigoExternoFondo, required = true)
        val codigoExternoFondo: String,

        @get:JsonProperty(PropiedadesJSON.creditosFondos, required = true)
        @param:JsonProperty(PropiedadesJSON.creditosFondos, required = true)
        val creditosFondos: List<CreditoFondoDTO>
) : EntidadDTO<CreditoPaquete>
{
    internal object PropiedadesJSON
    {
        const val idPaquete = "package-id"
        const val codigoExternoFondo = "package-external-code"
        const val creditosFondos = "credits"
    }

    object CodigosError : CodigosErrorDTO(50200)
    {
        val CREDITOS_FONDOS_INVALIDOS = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
    }

    constructor(creditoPaquete: CreditoPaquete) :
            this(
                    creditoPaquete.idPaquete,
                    creditoPaquete.codigoExternoPaquete,
                    creditoPaquete.creditosFondos.map { CreditoFondoDTO(it) }
                )

    override fun aEntidadDeNegocio(): CreditoPaquete
    {
        return CreditoPaquete(
                idPaquete,
                codigoExternoFondo,
                creditosFondos.map { it.aEntidadDeNegocio() }
                             )
    }
}