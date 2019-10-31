package co.smartobjects.red.modelos.operativas.compras

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.operativas.TransaccionEntidadTerminadaDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.ZonedDateTime

data class CompraDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id, required = true)
        @param:JsonProperty(PropiedadesJSON.id, required = true)
        val id: String,

        @get:JsonProperty(TransaccionEntidadTerminadaDTO.PropiedadesJSON.creacionTerminada)
        @param:JsonProperty(TransaccionEntidadTerminadaDTO.PropiedadesJSON.creacionTerminada)
        val creacionTerminada: Boolean = false,

        @get:JsonProperty(PropiedadesJSON.creditosFondos, required = true)
        @param:JsonProperty(PropiedadesJSON.creditosFondos, required = true)
        val creditosFondos: List<CreditoFondoDTO>,

        @get:JsonProperty(PropiedadesJSON.creditosPaquetes, required = true)
        @param:JsonProperty(PropiedadesJSON.creditosPaquetes, required = true)
        val creditosPaquetes: List<CreditoPaqueteDTO>,

        @get:JsonProperty(PropiedadesJSON.pagos, required = true)
        @param:JsonProperty(PropiedadesJSON.pagos, required = true)
        val pagos: List<PagoDTO>,

        @get:JsonProperty(PropiedadesJSON.fechaDeRealizacion, required = true)
        @param:JsonProperty(PropiedadesJSON.fechaDeRealizacion, required = true)
        val fechaDeRealizacion: ZonedDateTime
) : EntidadDTO<Compra>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val creditosFondos = "fund-credits"
        const val creditosPaquetes = "package-credits"
        const val pagos = "payments"
        const val fechaDeRealizacion = "operation-datetime"
    }

    object CodigosError : CodigosErrorDTO(50000)
    {
        @JvmField
        val CREDITOS_INVALIDOS = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
        @JvmField
        val PAGOS_INVALIDOS = CREDITOS_INVALIDOS + 1
        @JvmField
        val PAGOS_CON_NUMERO_DE_TRANSACCION_POS_REPETIDOS = PAGOS_INVALIDOS + 1
        @JvmField
        val FECHA_REALIZACION_INVALIDA = PAGOS_CON_NUMERO_DE_TRANSACCION_POS_REPETIDOS + 1
        @JvmField
        val FECHA_CONSULTA_INVALIDA = FECHA_REALIZACION_INVALIDA + 1
    }

    constructor(compra: Compra) :
            this(
                    compra.idCliente,
                    compra.id,
                    compra.creacionTerminada,
                    compra.creditosFondos.map { CreditoFondoDTO(it) },
                    compra.creditosPaquetes.map { CreditoPaqueteDTO(it) },
                    compra.pagos.map { PagoDTO(it) },
                    compra.fechaDeRealizacion
                )

    override fun aEntidadDeNegocio(): Compra
    {
        val partesId = EntidadTransaccional.idAPartes(id)
        return Compra(
                idCliente,
                partesId.nombreUsuario,
                partesId.uuid,
                partesId.tiempoCreacion,
                creacionTerminada,
                creditosFondos.map { it.aEntidadDeNegocio() },
                creditosPaquetes.map { it.aEntidadDeNegocio() },
                pagos.map { it.aEntidadDeNegocio() },
                fechaDeRealizacion
                     )
    }
}