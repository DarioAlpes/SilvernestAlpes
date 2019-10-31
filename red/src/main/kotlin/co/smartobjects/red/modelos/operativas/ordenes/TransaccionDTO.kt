package co.smartobjects.red.modelos.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = ITransaccionDTO.PropiedadesJSON.tipo,
        visible = true
             )
@JsonSubTypes(
        JsonSubTypes.Type(value = TransaccionCreditoDTO::class, name = TransaccionDTO.Tipo.NOMBRE_EN_RED_CREDITO),
        JsonSubTypes.Type(value = TransaccionDebitoDTO::class, name = TransaccionDTO.Tipo.NOMBRE_EN_RED_DEBITO)
             )
interface ITransaccionDTO : EntidadDTO<Transaccion>
{
    @get:JsonProperty(PropiedadesJSON.tipo, required = true)
    val tipo: TransaccionDTO.Tipo

    @get:JsonProperty(PropiedadesJSON.idCliente)
    val idCliente: Long

    @get:JsonProperty(PropiedadesJSON.id)
    val id: Long?

    @get:JsonProperty(PropiedadesJSON.nombreUsuario, required = true)
    val nombreUsuario: String

    @get:JsonProperty(PropiedadesJSON.idUbicacion)
    val idUbicacion: Long?

    @get:JsonProperty(PropiedadesJSON.idFondo, required = true)
    val idFondo: Long

    @get:JsonProperty(PropiedadesJSON.codigoExternoFondo, required = true)
    val codigoExternoFondo: String

    @get:JsonProperty(PropiedadesJSON.cantidad, required = true)
    val cantidad: Decimal

    @get:JsonProperty(PropiedadesJSON.idGrupoClientesPersona)
    val idGrupoClientesPersona: Long?

    @get:JsonProperty(PropiedadesJSON.idDispositivo, required = true)
    val idDispositivo: String

    companion object
    {
        fun aITransaccionDTO(transaccion: Transaccion): ITransaccionDTO
        {
            return when (transaccion)
            {
                is Transaccion.Credito -> TransaccionCreditoDTO(transaccion)
                is Transaccion.Debito  -> TransaccionDebitoDTO(transaccion)
            }
        }
    }

    object PropiedadesJSON
    {
        const val tipo = "transaction-type"
        const val idCliente = "client-id"
        const val id = "id"
        const val nombreUsuario = "username"
        const val idUbicacion = "location-id"
        const val idFondo = "fund-id"
        const val codigoExternoFondo = "fund-external-code"
        const val cantidad = "amount"
        const val idGrupoClientesPersona = "customer-group-id"
        const val idDispositivo = "device-id"
    }
}

data class TransaccionDTO @JsonCreator internal constructor
(
        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idCliente)
        override val idCliente: Long,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.id)
        override val id: Long?,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.nombreUsuario, required = true)
        override val nombreUsuario: String,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idUbicacion)
        override val idUbicacion: Long?,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idFondo, required = true)
        override val idFondo: Long,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.codigoExternoFondo, required = true)
        override val codigoExternoFondo: String,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.cantidad, required = true)
        override val cantidad: Decimal,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idGrupoClientesPersona)
        override val idGrupoClientesPersona: Long?,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idDispositivo, required = true)
        override val idDispositivo: String,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.tipo, required = true)
        override val tipo: Tipo
) : ITransaccionDTO
{
    object CodigosError : CodigosErrorDTO(60200)

    override fun aEntidadDeNegocio(): Transaccion
    {
        throw UnsupportedOperationException("No existe suficiente información para instanciar la transacción correcta")
    }

    enum class Tipo(val valorEnRed: String)
    {
        CREDITO("CREDIT"), DEBITO("DEBIT"), DESCONOCIDO("UNKNOWN");

        companion object
        {
            const val NOMBRE_EN_RED_CREDITO = "CREDIT"
            const val NOMBRE_EN_RED_DEBITO = "DEBIT"
        }
    }
}