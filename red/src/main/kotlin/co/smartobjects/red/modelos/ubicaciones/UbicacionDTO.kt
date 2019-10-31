package co.smartobjects.red.modelos.ubicaciones

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*


data class UbicacionDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id)
        @param:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.nombre, required = true)
        @param:JsonProperty(PropiedadesJSON.nombre, required = true)
        val nombre: String,

        @get:JsonProperty(PropiedadesJSON.tipo, required = true)
        @param:JsonProperty(PropiedadesJSON.tipo, required = true)
        val tipo: Tipo,

        @get:JsonProperty(PropiedadesJSON.subtipo, required = true)
        @param:JsonProperty(PropiedadesJSON.subtipo, required = true)
        val subtipo: Subtipo,

        @get:JsonProperty(PropiedadesJSON.idDelPadre, required = true)
        @param:JsonProperty(PropiedadesJSON.idDelPadre, required = true)
        val idDelPadre: Long?,

        @get:JsonProperty(PropiedadesJSON.idsDeAncestros)
        @param:JsonProperty(PropiedadesJSON.idsDeAncestros)
        val idsDeAncestros: List<Long> = listOf()
) : EntidadDTO<Ubicacion>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val nombre = "name"
        const val tipo = "type"
        const val subtipo = "subtype"
        const val idDelPadre = "parent-location-id"
        const val idsDeAncestros = "ancestors-ids"
    }

    object CodigosError : CodigosErrorDTO(10200)
    {
        const val CICLO_JERARQUIA = 10223
        const val NOMBRE_INVALIDO = 10241
    }

    constructor(ubicacion: Ubicacion) :
            this(
                    ubicacion.idCliente,
                    ubicacion.id,
                    ubicacion.nombre,
                    Tipo.desdeNegocio(ubicacion.tipo),
                    Subtipo.desdeNegocio(ubicacion.subtipo),
                    ubicacion.idDelPadre,
                    ubicacion.idsDeAncestros.toList()
                )

    override fun aEntidadDeNegocio(): Ubicacion
    {
        return Ubicacion(
                idCliente,
                id,
                nombre,
                tipo.valorEnNegocio!!,
                subtipo.valorEnNegocio!!,
                idDelPadre,
                LinkedHashSet(idsDeAncestros)
                        )
    }

    enum class Tipo(val valorEnNegocio: Ubicacion.Tipo?, val valorEnRed: String)
    {
        AREA(Ubicacion.Tipo.AREA, "AREA"),
        CIUDAD(Ubicacion.Tipo.CIUDAD, "CITY"),
        PAIS(Ubicacion.Tipo.PAIS, "COUNTRY"),
        PROPIEDAD(Ubicacion.Tipo.PROPIEDAD, "PROPERTY"),
        PUNTO_DE_CONTACTO(Ubicacion.Tipo.PUNTO_DE_CONTACTO, "CONTACT POINT"),
        PUNTO_DE_INTERES(Ubicacion.Tipo.PUNTO_DE_INTERES, "POINT OF INTEREST"),
        REGION(Ubicacion.Tipo.REGION, "REGION"),
        ZONA(Ubicacion.Tipo.ZONA, "ZONE");

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Ubicacion.Tipo): Tipo
            {
                return Tipo.values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Subtipo(val valorEnNegocio: Ubicacion.Subtipo?, val valorEnRed: String)
    {
        AP(Ubicacion.Subtipo.AP, "AP"),
        AP_INALAMBRICO(Ubicacion.Subtipo.AP_INALAMBRICO, "WIRELESS AP"),
        AP_RESTRINGIDO(Ubicacion.Subtipo.AP_RESTRINGIDO, "RESTRICTED AP"),
        KIOSKO(Ubicacion.Subtipo.KIOSKO, "KIOSK"),
        POS(Ubicacion.Subtipo.POS, "POS"),
        POS_SIN_DINERO(Ubicacion.Subtipo.POS_SIN_DINERO, "CASHLESS POS");

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Ubicacion.Subtipo): Subtipo
            {
                return Subtipo.values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }
}