package co.smartobjects.red.modelos.fondos.precios

import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SegmentoClientesDTO(
        @get:JsonProperty("id")
        val id: Long?,
        @get:JsonProperty("field-name", required = true)
        val campo: NombreCampo,
        @get:JsonProperty("field-value", required = true)
        val valor: String
                              ) : EntidadDTO<SegmentoClientes>
{
    object CodigosError : CodigosErrorDTO(70100)
    {
        // Errores por campos
        const val VALOR_INVALIDO = 70141
    }

    @Suppress("unused")
    @JsonCreator
    internal constructor(campo: NombreCampo, valor: String) : this(null, campo, valor)

    constructor(segmentoClientes: SegmentoClientes) :
            this(
                    segmentoClientes.id,
                    NombreCampo.desdeNombreCampoEnNegocio(segmentoClientes.campo),
                    segmentoClientes.valor
                )

    override fun aEntidadDeNegocio(): SegmentoClientes
    {
        return SegmentoClientes(id, campo.nombreCampoEnNegocio!!, valor)
    }

    enum class NombreCampo(val nombreCampoEnNegocio: SegmentoClientes.NombreCampo?, val valorEnRed: String)
    {
        CATEGORIA(SegmentoClientes.NombreCampo.CATEGORIA, "CATEGORY"), GRUPO_DE_EDAD(SegmentoClientes.NombreCampo.GRUPO_DE_EDAD, "AGE-GROUP"), TIPO(SegmentoClientes.NombreCampo.TIPO, "TYPE"), DESCONOCIDO(null, "DESCONOCIDO");

        companion object
        {
            fun desdeNombreCampoEnNegocio(nombreDeCampo: SegmentoClientes.NombreCampo): NombreCampo
            {
                return NombreCampo.values().first { it.nombreCampoEnNegocio == nombreDeCampo }
            }
        }
    }
}