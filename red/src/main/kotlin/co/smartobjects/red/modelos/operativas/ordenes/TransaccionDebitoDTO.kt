package co.smartobjects.red.modelos.operativas.ordenes

import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class TransaccionDebitoDTO @JsonCreator internal constructor
(
        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idCliente)
        override val idCliente: Long = 0,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.id)
        override val id: Long? = null,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.nombreUsuario, required = true)
        override val nombreUsuario: String,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idUbicacion, required = true)
        override val idUbicacion: Long,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idFondo, required = true)
        override val idFondo: Long,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.codigoExternoFondo, required = true)
        override val codigoExternoFondo: String,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.cantidad, required = true)
        override val cantidad: Decimal,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idGrupoClientesPersona)
        override val idGrupoClientesPersona: Long? = null,

        @param:JsonProperty(ITransaccionDTO.PropiedadesJSON.idDispositivo, required = true)
        override val idDispositivo: String

) : ITransaccionDTO
{
    object CodigosError : CodigosErrorDTO(60400)

    @get:JsonProperty(ITransaccionDTO.PropiedadesJSON.tipo, required = true)
    override val tipo = TransaccionDTO.Tipo.DEBITO

    constructor(transaccion: Transaccion.Debito) :
            this(
                    transaccion.idCliente,
                    transaccion.id,
                    transaccion.nombreUsuario,
                    transaccion.idUbicacion!!,
                    transaccion.idFondo,
                    transaccion.codigoExternoFondo,
                    transaccion.cantidad,
                    transaccion.idGrupoClientesPersona,
                    transaccion.idDispositivo
                )

    override fun aEntidadDeNegocio(): Transaccion.Debito
    {
        return Transaccion.Debito(
                idCliente,
                id,
                nombreUsuario,
                cantidad,
                idGrupoClientesPersona,
                idDispositivo,
                ConsumibleEnPuntoDeVenta(idUbicacion, idFondo, codigoExternoFondo)
                                 )
    }
}