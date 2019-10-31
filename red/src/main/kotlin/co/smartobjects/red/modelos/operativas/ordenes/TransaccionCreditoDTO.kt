package co.smartobjects.red.modelos.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.ZonedDateTime

data class TransaccionCreditoDTO @JsonCreator internal constructor
(
        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idCliente)
        override val idCliente: Long = 0,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.id)
        override val id: Long? = null,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.nombreUsuario, required = true)
        override val nombreUsuario: String,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idUbicacion)
        override val idUbicacion: Long? = null,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idFondo, required = true)
        override val idFondo: Long,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.codigoExternoFondo, required = true)
        override val codigoExternoFondo: String,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.cantidad, required = true)
        override val cantidad: Decimal,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idGrupoClientesPersona)
        override val idGrupoClientesPersona: Long? = null,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idDispositivo, required = true)
        override val idDispositivo: String,


        @get:JsonProperty(PropiedadesJSON.validoDesde)
        val validoDesde: ZonedDateTime? = null,

        @get:JsonProperty(PropiedadesJSON.validoHasta)
        val validoHasta: ZonedDateTime? = null
) : ITransaccionDTO
{
    internal object PropiedadesJSON
    {
        const val validoDesde = "valid-from"
        const val validoHasta = "valid-until"
    }

    object CodigosError : CodigosErrorDTO(60300)

    @get:JsonProperty(ITransaccionDTO.PropiedadesJSON.tipo, required = true)
    override val tipo = TransaccionDTO.Tipo.CREDITO

    constructor(transaccion: Transaccion.Credito) :
            this(
                    transaccion.idCliente,
                    transaccion.id,
                    transaccion.nombreUsuario,
                    transaccion.idUbicacion,
                    transaccion.idFondo,
                    transaccion.codigoExternoFondo,
                    transaccion.cantidad,
                    transaccion.idGrupoClientesPersona,
                    transaccion.idDispositivo,
                    transaccion.validoDesde,
                    transaccion.validoHasta
                )

    override fun aEntidadDeNegocio(): Transaccion.Credito
    {
        return Transaccion.Credito(
                idCliente,
                id,
                nombreUsuario,
                idUbicacion,
                idFondo,
                codigoExternoFondo,
                cantidad,
                idGrupoClientesPersona,
                idDispositivo,
                validoDesde,
                validoHasta
                                  )
    }
}