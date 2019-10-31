package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.precios.PrecioDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class DineroDTO @JsonCreator internal constructor
(
        @param:JsonProperty(IFondoDTO.PropiedadesJSON.idCliente)
        override val idCliente: Long = 0,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.id)
        override val id: Long? = null,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.nombre, required = true)
        override val nombre: String,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.disponibleParaLaVenta, required = true)
        override val disponibleParaLaVenta: Boolean,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.debeAparecerSoloUnaVez)
        override val debeAparecerSoloUnaVez: Boolean = false,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.esIlimitado, required = true)
        override val esIlimitado: Boolean,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.precioPorDefecto, required = true)
        override val precioPorDefecto: PrecioDTO,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.codigoExterno, required = true)
        override val codigoExterno: String

) : IFondoDTO<Dinero>
{
    object CodigosError : CodigosErrorDTO(30400)

    @get:JsonProperty(IFondoDTO.PropiedadesJSON.tipoDeFondo)
    override val tipoDeFondo: FondoDTO.TipoDeFondoEnRed = FondoDTO.TipoDeFondoEnRed.DINERO

    constructor(dinero: Dinero) :
            this(
                    dinero.idCliente,
                    dinero.id,
                    dinero.nombre,
                    dinero.disponibleParaLaVenta,
                    dinero.debeAparecerSoloUnaVez,
                    dinero.esIlimitado,
                    PrecioDTO(dinero.precioPorDefecto),
                    dinero.codigoExterno
                )

    override fun aEntidadDeNegocio(): Dinero
    {
        return Dinero(
                idCliente,
                id,
                nombre,
                disponibleParaLaVenta,
                debeAparecerSoloUnaVez,
                esIlimitado,
                precioPorDefecto.aEntidadDeNegocio(),
                codigoExterno
                     )
    }
}