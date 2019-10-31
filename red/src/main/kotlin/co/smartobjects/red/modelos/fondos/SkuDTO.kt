package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.Sku
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.precios.PrecioDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SkuDTO @JsonCreator internal constructor
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


        @get:JsonProperty(PropiedadesJSON.idDeCategoria, required = true)
        @param:JsonProperty(PropiedadesJSON.idDeCategoria, required = true)
        val idDeCategoria: Long,

        @get:JsonProperty(PropiedadesJSON.llaveDeIcono)
        @param:JsonProperty(PropiedadesJSON.llaveDeIcono)
        val llaveDeImagen: String? = null

) : IFondoDTO<Sku>
{
    internal object PropiedadesJSON
    {
        const val idDeCategoria = "category-id"
        const val llaveDeIcono = "image-key"
    }

    object CodigosError : CodigosErrorDTO(30700)

    @get:JsonProperty(IFondoDTO.PropiedadesJSON.tipoDeFondo)
    override val tipoDeFondo = FondoDTO.TipoDeFondoEnRed.SKU

    constructor(sku: Sku) :
            this(
                    sku.idCliente,
                    sku.id,
                    sku.nombre,
                    sku.disponibleParaLaVenta,
                    sku.debeAparecerSoloUnaVez,
                    sku.esIlimitado,
                    PrecioDTO(sku.precioPorDefecto),
                    sku.codigoExterno,
                    sku.idDeCategoria,
                    sku.llaveDeImagen
                )

    override fun aEntidadDeNegocio(): Sku
    {
        return Sku(
                idCliente,
                id,
                nombre,
                disponibleParaLaVenta,
                debeAparecerSoloUnaVez,
                esIlimitado,
                precioPorDefecto.aEntidadDeNegocio(),
                codigoExterno,
                idDeCategoria,
                llaveDeImagen
                  )
    }
}