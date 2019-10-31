package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.utilidades.Decimal
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.ZonedDateTime

data class PaqueteDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente, required = true)
        val idCliente: Long,

        @get:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.nombre, required = true)
        var nombre: String,

        @get:JsonProperty(PropiedadesJSON.descripcion, required = true)
        val descripcion: String,

        @get:JsonProperty(PropiedadesJSON.disponibleParaLaVenta, required = true)
        val disponibleParaLaVenta: Boolean,

        @get:JsonProperty(PropiedadesJSON.validoDesde, required = true)
        val validoDesde: ZonedDateTime,

        @get:JsonProperty(PropiedadesJSON.validoHasta, required = true)
        val validoHasta: ZonedDateTime,

        @get:JsonProperty(PropiedadesJSON.fondosIncluidos, required = true)
        val fondosIncluidos: List<FondoIncluidoDTO>,

        @get:JsonProperty(PropiedadesJSON.codigoExterno, required = true)
        var codigoExterno: String
) : EntidadDTO<Paquete>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val nombre = "name"
        const val descripcion = "description"
        const val disponibleParaLaVenta = "available-for-sale"
        const val validoDesde = "valid-from"
        const val validoHasta = "valid-until"
        const val fondosIncluidos = "included-funds"
        const val codigoExterno = "external-code"
    }

    object CodigosError : CodigosErrorDTO(30600)
    {
        const val NOMBRE_INVALIDO = 30641
        const val DESCRIPCION_INVALIDA = 30642
        const val FECHA_VALIDEZ_DESDE_INVALIDA = 30643
        const val FECHA_VALIDEZ_HASTA_INVALIDA = 30644
        const val FONDOS_INCLUIDOS_INVALIDOS = 30645
    }

    constructor(paquete: Paquete) :
            this(
                    paquete.idCliente,
                    paquete.id,
                    paquete.nombre,
                    paquete.descripcion,
                    paquete.disponibleParaLaVenta,
                    paquete.validoDesde,
                    paquete.validoHasta,
                    paquete.fondosIncluidos.map { FondoIncluidoDTO(it) },
                    paquete.codigoExterno
                )

    override fun aEntidadDeNegocio(): Paquete
    {
        return Paquete(
                idCliente,
                id,
                nombre,
                descripcion,
                disponibleParaLaVenta,
                validoDesde,
                validoHasta,
                fondosIncluidos.map { it.aEntidadDeNegocio() },
                codigoExterno
                      )
    }

    data class FondoIncluidoDTO @JsonCreator constructor(
            @get:JsonProperty("fund-id", required = true)
            val idFondo: Long,

            @get:JsonProperty(value = "fund-external-code", required = true)
            val codigoExterno: String,

            @get:JsonProperty("amount", required = true)
            val cantidad: Decimal
                                                        )
    {
        constructor(fondoIncluido: Paquete.FondoIncluido) :
                this(
                        fondoIncluido.idFondo,
                        fondoIncluido.codigoExterno,
                        fondoIncluido.cantidad
                    )

        fun aEntidadDeNegocio(): Paquete.FondoIncluido
        {
            return Paquete.FondoIncluido(idFondo, codigoExterno, cantidad)
        }
    }
}