package co.smartobjects.red.modelos.personas

import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class ValorGrupoEdadDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.valor, required = true)
        @param:JsonProperty(PropiedadesJSON.valor, required = true)
        val valor: String,

        @get:JsonProperty(PropiedadesJSON.edadMinima)
        @param:JsonProperty(PropiedadesJSON.edadMinima)
        val edadMinima: Int? = null,

        @get:JsonProperty(PropiedadesJSON.edadMaxima)
        @param:JsonProperty(PropiedadesJSON.edadMaxima)
        val edadMaxima: Int? = null
) : EntidadDTO<ValorGrupoEdad>
{
    internal object PropiedadesJSON
    {
        const val valor = "value"
        const val edadMinima = "minimum-age"
        const val edadMaxima = "maximum-age"
    }

    object CodigosError : CodigosErrorDTO(10400)
    {
        // Errores por campos
        const val VALOR_INVALIDO = 10441
        const val EDAD_MINIMA_SUPERIOR_A_MAXIMA = 10442
        const val INTERSECTA_OTRO_GRUPO_DE_EDAD = 10443
    }

    constructor(valorGrupoEdad: ValorGrupoEdad) :
            this(
                    valorGrupoEdad.valor,
                    valorGrupoEdad.edadMinima,
                    valorGrupoEdad.edadMaxima
                )

    override fun aEntidadDeNegocio(): ValorGrupoEdad
    {
        return ValorGrupoEdad(valor, edadMinima, edadMaxima)
    }
}