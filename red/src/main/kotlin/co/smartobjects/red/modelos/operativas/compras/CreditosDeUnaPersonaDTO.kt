package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.compras.CreditosDeUnaPersona
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CreditosDeUnaPersonaDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.idPersona, required = true)
        @param:JsonProperty(PropiedadesJSON.idPersona, required = true)
        val idPersona: Long,

        @get:JsonProperty(PropiedadesJSON.creditosFondos, required = true)
        @param:JsonProperty(PropiedadesJSON.creditosFondos, required = true)
        val creditosFondos: List<CreditoFondoDTO>,

        @get:JsonProperty(PropiedadesJSON.creditosPaquetes, required = true)
        @param:JsonProperty(PropiedadesJSON.creditosPaquetes, required = true)
        val creditosPaquetes: List<CreditoPaqueteDTO>
) : EntidadDTO<CreditosDeUnaPersona>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val idPersona = "person-id"
        const val creditosFondos = "fund-credits"
        const val creditosPaquetes = "package-credits"
    }

    object CodigosError : CodigosErrorDTO(50400)
    {
        @JvmField
        val FECHA_CONSULTA_INVALIDA = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
    }

    constructor(creditosDeUnaPersona: CreditosDeUnaPersona) :
            this(
                    creditosDeUnaPersona.idCliente,
                    creditosDeUnaPersona.idPersona,
                    creditosDeUnaPersona.creditosFondos.map { CreditoFondoDTO(it) },
                    creditosDeUnaPersona.creditosPaquetes.map { CreditoPaqueteDTO(it) }
                )

    override fun aEntidadDeNegocio(): CreditosDeUnaPersona
    {
        return CreditosDeUnaPersona(
                idCliente,
                idPersona,
                creditosFondos.map { it.aEntidadDeNegocio() },
                creditosPaquetes.map { it.aEntidadDeNegocio() }
                                   )
    }
}