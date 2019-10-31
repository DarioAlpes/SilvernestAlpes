package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Entrada
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.precios.PrecioDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class EntradaDTO @JsonCreator internal constructor
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
        override val debeAparecerSoloUnaVez: Boolean = true,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.esIlimitado, required = true)
        override val esIlimitado: Boolean,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.precioPorDefecto, required = true)
        override val precioPorDefecto: PrecioDTO,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.codigoExterno, required = true)
        override val codigoExterno: String,


        @get:JsonProperty(PropiedadesJSON.idUbicacion, required = true)
        @param:JsonProperty(PropiedadesJSON.idUbicacion, required = true)
        val idUbicacion: Long
) : IFondoDTO<Entrada>
{
    internal object PropiedadesJSON
    {
        const val idUbicacion = "location-id"
    }

    object CodigosError : CodigosErrorDTO(30500)

    @get:JsonProperty(IFondoDTO.PropiedadesJSON.tipoDeFondo)
    override val tipoDeFondo = FondoDTO.TipoDeFondoEnRed.ENTRADA

    constructor(entrada: Entrada) :
            this(
                    entrada.idCliente,
                    entrada.id,
                    entrada.nombre,
                    entrada.disponibleParaLaVenta,
                    entrada.debeAparecerSoloUnaVez,
                    entrada.esIlimitado,
                    PrecioDTO(entrada.precioPorDefecto),
                    entrada.codigoExterno,
                    entrada.idUbicacion
                )

    override fun aEntidadDeNegocio(): Entrada
    {
        return Entrada(
                idCliente,
                id,
                nombre,
                disponibleParaLaVenta,
                esIlimitado,
                precioPorDefecto.aEntidadDeNegocio(),
                codigoExterno,
                idUbicacion
                      )
    }
}