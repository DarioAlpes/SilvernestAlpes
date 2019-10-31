package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Acceso
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.precios.PrecioDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class AccesoDTO @JsonCreator internal constructor
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
        override val codigoExterno: String,


        @get:JsonProperty(PropiedadesJSON.idUbicacion, required = true)
        @param:JsonProperty(PropiedadesJSON.idUbicacion, required = true)
        val idUbicacion: Long
) : IFondoDTO<Acceso>
{
    internal object PropiedadesJSON
    {
        const val idUbicacion = "location-id"
    }

    object CodigosError : CodigosErrorDTO(30100)

    @get:JsonProperty(IFondoDTO.PropiedadesJSON.tipoDeFondo)
    override val tipoDeFondo = FondoDTO.TipoDeFondoEnRed.ACCESO

    constructor(acceso: Acceso) :
            this(
                    acceso.idCliente,
                    acceso.id,
                    acceso.nombre,
                    acceso.disponibleParaLaVenta,
                    acceso.debeAparecerSoloUnaVez,
                    acceso.esIlimitado,
                    PrecioDTO(acceso.precioPorDefecto),
                    acceso.codigoExterno,
                    acceso.idUbicacion
                )

    override fun aEntidadDeNegocio(): Acceso
    {
        return Acceso(
                idCliente,
                id,
                nombre,
                disponibleParaLaVenta,
                debeAparecerSoloUnaVez,
                esIlimitado,
                precioPorDefecto.aEntidadDeNegocio(),
                codigoExterno,
                idUbicacion
                     )
    }
}