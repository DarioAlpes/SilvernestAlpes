package co.smartobjects.red.modelos

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class ErrorDePeticion @JsonCreator constructor
(
        @get:JsonProperty(PropiedadesJSON.codigoInterno, required = true)
        @param:JsonProperty(PropiedadesJSON.codigoInterno, required = true)
        val codigoInterno: Int,

        @get:JsonProperty(PropiedadesJSON.mensaje, required = true)
        @param:JsonProperty(PropiedadesJSON.mensaje, required = true)
        val mensaje: String
)
{
    internal object PropiedadesJSON
    {
        const val codigoInterno = "internal-code"
        const val mensaje = "message"
    }
}