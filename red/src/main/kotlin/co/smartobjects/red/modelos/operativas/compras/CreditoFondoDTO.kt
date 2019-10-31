package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.ZonedDateTime

data class CreditoFondoDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id)
        @param:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.cantidad, required = true)
        @param:JsonProperty(PropiedadesJSON.cantidad, required = true)
        val cantidad: Decimal,

        @get:JsonProperty(PropiedadesJSON.valorBasePagado, required = true)
        @param:JsonProperty(PropiedadesJSON.valorBasePagado, required = true)
        val valorBasePagado: Decimal,

        @get:JsonProperty(PropiedadesJSON.valorImpuestoPagado, required = true)
        @param:JsonProperty(PropiedadesJSON.valorImpuestoPagado, required = true)
        val valorImpuestoPagado: Decimal,

        @get:JsonProperty(PropiedadesJSON.validoDesde)
        @param:JsonProperty(PropiedadesJSON.validoDesde)
        val validoDesde: ZonedDateTime? = null,

        @get:JsonProperty(PropiedadesJSON.validoHasta)
        @param:JsonProperty(PropiedadesJSON.validoHasta)
        val validoHasta: ZonedDateTime? = null,

        @get:JsonProperty(PropiedadesJSON.consumido)
        @param:JsonProperty(PropiedadesJSON.consumido)
        val consumido: Boolean = false,

        @get:JsonProperty(PropiedadesJSON.origen, required = true)
        @param:JsonProperty(PropiedadesJSON.origen, required = true)
        val origen: String,

        @get:JsonProperty(PropiedadesJSON.nombreUsuario, required = true)
        @param:JsonProperty(PropiedadesJSON.nombreUsuario, required = true)
        val nombreUsuario: String,

        @get:JsonProperty(PropiedadesJSON.idPersonaDueña, required = true)
        @param:JsonProperty(PropiedadesJSON.idPersonaDueña, required = true)
        val idPersonaDueña: Long,

        @get:JsonProperty(PropiedadesJSON.idFondoComprado, required = true)
        @param:JsonProperty(PropiedadesJSON.idFondoComprado, required = true)
        val idFondoComprado: Long,

        @get:JsonProperty(PropiedadesJSON.codigoExternoFondo, required = true)
        @param:JsonProperty(PropiedadesJSON.codigoExternoFondo, required = true)
        val codigoExternoFondo: String,

        @get:JsonProperty(PropiedadesJSON.idImpuestoPagado, required = true)
        @param:JsonProperty(PropiedadesJSON.idImpuestoPagado, required = true)
        val idImpuestoPagado: Long,

        @get:JsonProperty(PropiedadesJSON.idDispositivo, required = true)
        @param:JsonProperty(PropiedadesJSON.idDispositivo, required = true)
        val idDispositivo: String,

        @get:JsonProperty(PropiedadesJSON.idUbicacionCompra, required = true)
        @param:JsonProperty(PropiedadesJSON.idUbicacionCompra, required = true)
        val idUbicacionCompra: Long?,

        @get:JsonProperty(PropiedadesJSON.idGrupoClientesPersona, required = true)
        @param:JsonProperty(PropiedadesJSON.idGrupoClientesPersona, required = true)
        val idGrupoClientesPersona: Long?
) : EntidadDTO<CreditoFondo>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val cantidad = "amount"
        const val valorBasePagado = "price-paid"
        const val valorImpuestoPagado = "tax-paid"
        const val validoDesde = "valid-from"
        const val validoHasta = "valid-until"
        const val consumido = "consumed"
        const val origen = "source"
        const val nombreUsuario = "username"
        const val idPersonaDueña = "person-id"
        const val idFondoComprado = "fund-id"
        const val codigoExternoFondo = "fund-external-code"
        const val idImpuestoPagado = "tax-id"
        const val idDispositivo = "device-id"
        const val idUbicacionCompra = "location-id"
        const val idGrupoClientesPersona = "customer-group-id"
    }

    object CodigosError : CodigosErrorDTO(50100)
    {
        val CANTIDAD_INVALIDA = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
        val VALOR_BASE_PAGADO_INVALIDO = CANTIDAD_INVALIDA + 1
        val VALOR_IMPUESTO_PAGADO_INVALIDO = VALOR_BASE_PAGADO_INVALIDO + 1
        val FECHA_VALIDEZ_DESDE_INVALIDA = VALOR_IMPUESTO_PAGADO_INVALIDO + 1
        val FECHA_VALIDEZ_HASTA_INVALIDA = FECHA_VALIDEZ_DESDE_INVALIDA + 1
        val ORIGEN_INVALIDO = FECHA_VALIDEZ_HASTA_INVALIDA + 1
        val ID_DISPOSITIVO_INVALIDO = ORIGEN_INVALIDO + 1
    }

    constructor(creditoFondo: CreditoFondo) :
            this(
                    creditoFondo.idCliente,
                    creditoFondo.id,
                    creditoFondo.cantidad,
                    creditoFondo.valorPagado,
                    creditoFondo.valorImpuestoPagado,
                    creditoFondo.validoDesde,
                    creditoFondo.validoHasta,
                    creditoFondo.consumido,
                    creditoFondo.origen,
                    creditoFondo.nombreUsuario,
                    creditoFondo.idPersonaDueña,
                    creditoFondo.idFondoComprado,
                    creditoFondo.codigoExternoFondo,
                    creditoFondo.idImpuestoPagado,
                    creditoFondo.idDispositivo,
                    creditoFondo.idUbicacionCompra,
                    creditoFondo.idGrupoClientesPersona
                )

    override fun aEntidadDeNegocio(): CreditoFondo
    {
        return CreditoFondo(
                idCliente,
                id,
                cantidad,
                valorBasePagado,
                valorImpuestoPagado,
                validoDesde,
                validoHasta,
                consumido,
                origen,
                nombreUsuario,
                idPersonaDueña,
                idFondoComprado,
                codigoExternoFondo,
                idImpuestoPagado,
                idDispositivo,
                idUbicacionCompra,
                idGrupoClientesPersona
                           )
    }
}