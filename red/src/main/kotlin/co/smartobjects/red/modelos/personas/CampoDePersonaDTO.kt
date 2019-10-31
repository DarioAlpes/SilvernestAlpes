package co.smartobjects.red.modelos.personas

import co.smartobjects.entidades.personas.CampoDePersona
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class CampoDePersonaDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.campo, required = true)
        @param:JsonProperty(PropiedadesJSON.campo, required = true)
        val campo: CampoDePersonaDTO.Predeterminado,

        @get:JsonProperty(PropiedadesJSON.esRequerido, required = true)
        @param:JsonProperty(PropiedadesJSON.esRequerido, required = true)
        val esRequerido: Boolean
) : EntidadDTO<CampoDePersona>
{
    internal object PropiedadesJSON
    {
        const val campo = "field"
        const val esRequerido = "is-required"
    }

    object CodigosError : CodigosErrorDTO(10300)

    constructor(campoDePersona: CampoDePersona) : this(Predeterminado.desdeNegocio(campoDePersona.campo), campoDePersona.esRequerido.valor)

    override fun aEntidadDeNegocio(): CampoDePersona
    {
        return CampoDePersona(campo.valorEnNegocio, esRequerido)
    }

    enum class Predeterminado(val valorEnNegocio: CampoDePersona.Predeterminado, val valorEnRed: String)
    {
        NOMBRE_COMPLETO(CampoDePersona.Predeterminado.NOMBRE_COMPLETO, "FULL NAME"),
        TIPO_DOCUMENTO(CampoDePersona.Predeterminado.TIPO_DOCUMENTO, "ID TYPE"),
        NUMERO_DOCUMENTO(CampoDePersona.Predeterminado.NUMERO_DOCUMENTO, "ID NUMBER"),
        GENERO(CampoDePersona.Predeterminado.GENERO, "GENDER"),
        FECHA_NACIMIENTO(CampoDePersona.Predeterminado.FECHA_NACIMIENTO, "BIRTH DATE"),
        CATEGORIA(CampoDePersona.Predeterminado.CATEGORIA, "CATEGORY"),
        AFILIACION(CampoDePersona.Predeterminado.AFILIACION, "AFFILIATION"),
        ES_ANONIMA(CampoDePersona.Predeterminado.ES_ANONIMA, "IS ANONYMOUS"),
        LLAVE_IMAGEN(CampoDePersona.Predeterminado.LLAVE_IMAGEN, "THUMBNAIL KEY"),
        TIPO(CampoDePersona.Predeterminado.TIPO, "THUMBNAIL KEY");

        companion object
        {
            fun desdeNegocio(valorEnNegocio: CampoDePersona.Predeterminado): Predeterminado
            {
                return Predeterminado.values().first { it.valorEnNegocio == valorEnNegocio }
            }

            fun desdeRed(valorEnRed: String): Predeterminado?
            {
                return Predeterminado.values().firstOrNull { it.valorEnRed == valorEnRed }
            }
        }
    }
}