package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.fondos.precios.PrecioDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CategoriaSkuDTO @JsonCreator internal constructor
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


        @get:JsonProperty(PropiedadesJSON.idDelPadre, required = true)
        @param:JsonProperty(PropiedadesJSON.idDelPadre, required = true)
        val idDelPadre: Long?,

        @get:JsonProperty(PropiedadesJSON.idsDeAncestros)
        @param:JsonProperty(PropiedadesJSON.idsDeAncestros)
        val idsDeAncestros: List<Long> = listOf(),

        @get:JsonProperty(PropiedadesJSON.llaveDeIcono)
        @param:JsonProperty(PropiedadesJSON.llaveDeIcono)
        val llaveDeIcono: String? = null
) : IFondoDTO<CategoriaSku>
{
    internal object PropiedadesJSON
    {
        const val idDelPadre = "parent-category-id"
        const val idsDeAncestros = "ancestors-ids"
        const val llaveDeIcono = "icon-key"
    }

    object CodigosError : CodigosErrorDTO(30200)
    {
        const val CICLO_JERARQUIA = 30223
    }

    @get:JsonProperty(IFondoDTO.PropiedadesJSON.tipoDeFondo)
    override val tipoDeFondo = FondoDTO.TipoDeFondoEnRed.CATEGORIA_SKU

    constructor(categoriaSku: CategoriaSku) :
            this(
                    categoriaSku.idCliente,
                    categoriaSku.id,
                    categoriaSku.nombre,
                    categoriaSku.disponibleParaLaVenta,
                    categoriaSku.debeAparecerSoloUnaVez,
                    categoriaSku.esIlimitado,
                    PrecioDTO(categoriaSku.precioPorDefecto),
                    categoriaSku.codigoExterno,
                    categoriaSku.idDelPadre,
                    categoriaSku.idsDeAncestros.toList(),
                    categoriaSku.llaveDeIcono
                )

    override fun aEntidadDeNegocio(): CategoriaSku
    {
        return CategoriaSku(
                idCliente,
                id,
                nombre,
                disponibleParaLaVenta,
                debeAparecerSoloUnaVez,
                esIlimitado,
                precioPorDefecto.aEntidadDeNegocio(),
                codigoExterno,
                idDelPadre,
                LinkedHashSet(idsDeAncestros),
                llaveDeIcono
                           )
    }
}