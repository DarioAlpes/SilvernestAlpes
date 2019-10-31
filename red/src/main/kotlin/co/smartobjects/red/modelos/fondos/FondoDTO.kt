package co.smartobjects.red.modelos.fondos

import co.smartobjects.entidades.fondos.*
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import co.smartobjects.red.modelos.fondos.precios.PrecioDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = IFondoDTO.PropiedadesJSON.tipoDeFondo,
        visible = true
             )
@JsonSubTypes(
        JsonSubTypes.Type(value = CategoriaSkuDTO::class, name = FondoDTO.TipoDeFondoEnRed.NOMBRE_EN_RED_CATEGORIA_SKU),
        JsonSubTypes.Type(value = DineroDTO::class, name = FondoDTO.TipoDeFondoEnRed.NOMBRE_EN_RED_DINERO),
        JsonSubTypes.Type(value = SkuDTO::class, name = FondoDTO.TipoDeFondoEnRed.NOMBRE_EN_RED_SKU),
        JsonSubTypes.Type(value = AccesoDTO::class, name = FondoDTO.TipoDeFondoEnRed.NOMBRE_EN_RED_ACCESO),
        JsonSubTypes.Type(value = EntradaDTO::class, name = FondoDTO.TipoDeFondoEnRed.NOMBRE_EN_RED_ENTRADA)
             )
interface IFondoDTO<T : Fondo<T>> : EntidadDTO<T>
{
    @get:JsonProperty(PropiedadesJSON.tipoDeFondo)
    val tipoDeFondo: FondoDTO.TipoDeFondoEnRed

    @get:JsonProperty(PropiedadesJSON.idCliente)
    val idCliente: Long

    @get:JsonProperty(PropiedadesJSON.id)
    val id: Long?

    @get:JsonProperty(PropiedadesJSON.nombre, required = true)
    val nombre: String

    @get:JsonProperty(PropiedadesJSON.disponibleParaLaVenta, required = true)
    val disponibleParaLaVenta: Boolean

    @get:JsonProperty(PropiedadesJSON.debeAparecerSoloUnaVez)
    val debeAparecerSoloUnaVez: Boolean

    @get:JsonProperty(PropiedadesJSON.esIlimitado, required = true)
    val esIlimitado: Boolean

    @get:JsonProperty(PropiedadesJSON.precioPorDefecto, required = true)
    val precioPorDefecto: PrecioDTO

    @get:JsonProperty(PropiedadesJSON.codigoExterno, required = true)
    val codigoExterno: String

    companion object
    {
        fun aIFondoDTO(fondo: Fondo<*>): IFondoDTO<*>
        {
            return when (fondo)
            {
                is CategoriaSku -> CategoriaSkuDTO(fondo)
                is Dinero       -> DineroDTO(fondo)
                is Sku          -> SkuDTO(fondo)
                is Entrada      -> EntradaDTO(fondo)
                is Acceso       -> AccesoDTO(fondo)
                else            -> throw RuntimeException("No existe conversión apropiada para el fondo '${fondo.javaClass.canonicalName}'")
            }
        }
    }

    object PropiedadesJSON
    {
        const val tipoDeFondo = "fund-type"
        const val idCliente = "client-id"
        const val id = "id"
        const val nombre = "name"
        const val disponibleParaLaVenta = "available-for-sale"
        const val debeAparecerSoloUnaVez = "once-per-session"
        const val esIlimitado = "unlimited"
        const val precioPorDefecto = "default-price"
        const val codigoExterno = "external-code"
    }
}

data class FondoDTO<FondoConcreto : Fondo<FondoConcreto>> @JsonCreator internal constructor
(
        @param:JsonProperty(IFondoDTO.PropiedadesJSON.idCliente)
        override val idCliente: Long,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.id)
        override val id: Long?,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.nombre, required = true)
        override val nombre: String,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.disponibleParaLaVenta, required = true)
        override val disponibleParaLaVenta: Boolean,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.debeAparecerSoloUnaVez)
        override val debeAparecerSoloUnaVez: Boolean,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.esIlimitado, required = true)
        override val esIlimitado: Boolean,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.tipoDeFondo, required = true)
        override val tipoDeFondo: TipoDeFondoEnRed,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.precioPorDefecto, required = true)
        override val precioPorDefecto: PrecioDTO,

        @param:JsonProperty(IFondoDTO.PropiedadesJSON.codigoExterno, required = true)
        override val codigoExterno: String

) : IFondoDTO<FondoConcreto>
{
    override fun aEntidadDeNegocio(): FondoConcreto
    {
        throw UnsupportedOperationException("No existe suficiente información para instanciar el fondo correcto")
    }

    object CodigosError : CodigosErrorDTO(30000)
    {
        const val NOMBRE_INVALIDO = 30041
    }

    constructor(fondo: Fondo<*>) :
            this(
                    fondo.idCliente,
                    fondo.id,
                    fondo.nombre,
                    fondo.disponibleParaLaVenta,
                    fondo.debeAparecerSoloUnaVez,
                    fondo.esIlimitado,
                    TipoDeFondoEnRed.aTipoDeFondo(fondo),
                    PrecioDTO(fondo.precioPorDefecto),
                    fondo.codigoExterno
                )

    enum class TipoDeFondoEnRed(val valorEnRed: String)
    {
        CATEGORIA_SKU("SKU-CATEGORY"), DINERO("CURRENCY"), SKU("SKU"), ACCESO("ACCESS"), ENTRADA("ENTRY"), DESCONOCIDO("UNKNOWN");

        companion object
        {
            const val NOMBRE_EN_RED_CATEGORIA_SKU = "SKU-CATEGORY"
            const val NOMBRE_EN_RED_DINERO = "CURRENCY"
            const val NOMBRE_EN_RED_SKU = "SKU"
            const val NOMBRE_EN_RED_ACCESO = "ACCESS"
            const val NOMBRE_EN_RED_ENTRADA = "ENTRY"

            fun aTipoDeFondo(fondo: Fondo<*>): TipoDeFondoEnRed
            {
                return if (fondo is CategoriaSku) CATEGORIA_SKU
                else if (fondo is Dinero) DINERO
                else if (fondo is Sku) SKU
                else if (fondo is Entrada) ENTRADA
                else if (fondo is Acceso && fondo !is Entrada) ACCESO
                else throw RuntimeException("No existe conversión apropiada para el fondo '${fondo.javaClass.canonicalName}'")
            }
        }
    }
}

data class WrapperDeserilizacionFondoDTO @JsonCreator constructor
(
        @get:JsonProperty(PropiedadesJSON.fondo, required = true)
        @param:JsonProperty(PropiedadesJSON.fondo, required = true)
        val fondo: IFondoDTO<*>
) : EntidadDTO<Fondo<*>>
{
    internal object PropiedadesJSON
    {
        const val fondo = "fund"
    }

    override fun aEntidadDeNegocio(): Fondo<*>
    {
        return deserializarSegunTipoFondo(fondo)
    }

    companion object
    {
        fun deserializarSegunTipoFondo(fondoADeserializar: IFondoDTO<*>): Fondo<*>
        {
            return when (fondoADeserializar.tipoDeFondo)
            {
                FondoDTO.TipoDeFondoEnRed.CATEGORIA_SKU -> (fondoADeserializar as CategoriaSkuDTO).aEntidadDeNegocio()
                FondoDTO.TipoDeFondoEnRed.DINERO        -> (fondoADeserializar as DineroDTO).aEntidadDeNegocio()
                FondoDTO.TipoDeFondoEnRed.SKU           -> (fondoADeserializar as SkuDTO).aEntidadDeNegocio()
                FondoDTO.TipoDeFondoEnRed.ACCESO        -> (fondoADeserializar as AccesoDTO).aEntidadDeNegocio()
                FondoDTO.TipoDeFondoEnRed.ENTRADA       -> (fondoADeserializar as EntradaDTO).aEntidadDeNegocio()
                FondoDTO.TipoDeFondoEnRed.DESCONOCIDO   ->
                {
                    throw IllegalStateException()
                }
            }
        }
    }
}