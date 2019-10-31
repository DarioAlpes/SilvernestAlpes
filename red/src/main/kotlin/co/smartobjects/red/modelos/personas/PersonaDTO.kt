package co.smartobjects.red.modelos.personas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.red.modelos.CodigosErrorDTO
import co.smartobjects.red.modelos.EntidadDTO
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.LocalDate

data class PersonaDTO @JsonCreator internal constructor
(
        @get:JsonProperty(PropiedadesJSON.idCliente)
        @param:JsonProperty(PropiedadesJSON.idCliente)
        val idCliente: Long = 0,

        @get:JsonProperty(PropiedadesJSON.id)
        @param:JsonProperty(PropiedadesJSON.id)
        val id: Long? = null,

        @get:JsonProperty(PropiedadesJSON.nombreCompleto, required = true)
        @param:JsonProperty(PropiedadesJSON.nombreCompleto, required = true)
        val nombreCompleto: String,

        @get:JsonProperty(PropiedadesJSON.tipoDocumento, required = true)
        @param:JsonProperty(PropiedadesJSON.tipoDocumento, required = true)
        val tipoDocumento: TipoDocumento,

        @get:JsonProperty(PropiedadesJSON.numeroDocumento, required = true)
        @param:JsonProperty(PropiedadesJSON.numeroDocumento, required = true)
        val numeroDocumento: String,

        @get:JsonProperty(PropiedadesJSON.genero, required = true)
        @param:JsonProperty(PropiedadesJSON.genero, required = true)
        val genero: Genero,

        @get:JsonProperty(PropiedadesJSON.fechaNacimiento, required = true)
        @param:JsonProperty(PropiedadesJSON.fechaNacimiento, required = true)
        val fechaNacimiento: LocalDate,

        @get:JsonProperty(PropiedadesJSON.categoria, required = true)
        @param:JsonProperty(PropiedadesJSON.categoria, required = true)
        val categoria: Categoria,

        @get:JsonProperty(PropiedadesJSON.afiliacion, required = true)
        @param:JsonProperty(PropiedadesJSON.afiliacion, required = true)
        val afiliacion: Afiliacion,

        @get:JsonProperty(PropiedadesJSON.esAnonima, required = true)
        @param:JsonProperty(PropiedadesJSON.esAnonima, required = true)
        val esAnonima: Boolean,

        @get:JsonProperty(PropiedadesJSON.llaveImagen)
        @param:JsonProperty(PropiedadesJSON.llaveImagen)
        val llaveImagen: String? = null,

        @get:JsonProperty(PropiedadesJSON.empresa, required = true)
        @param:JsonProperty(PropiedadesJSON.empresa, required = true)
        val empresa: String,

        @get:JsonProperty(PropiedadesJSON.nitEmpresa, required = true)
        @param:JsonProperty(PropiedadesJSON.nitEmpresa, required = true)
        val nitEmpresa: String,

        @get:JsonProperty(PropiedadesJSON.tipo, required = true)
        @param:JsonProperty(PropiedadesJSON.tipo, required = true)
        val tipo: Tipo

) : EntidadDTO<Persona>
{
    internal object PropiedadesJSON
    {
        const val idCliente = "client-id"
        const val id = "id"
        const val nombreCompleto = "full-name"
        const val tipoDocumento = "document-type"
        const val numeroDocumento = "document-number"
        const val genero = "gender"
        const val fechaNacimiento = "birthdate"
        const val categoria = "category"
        const val afiliacion = "affiliation"
        const val esAnonima = "is-anonymous"
        const val llaveImagen = "image-key"
        const val empresa = "company"
        const val nitEmpresa = "company_nit"
        const val tipo = "type"
    }

    object CodigosError : CodigosErrorDTO(10100)
    {
        // Errores por campos
        val NOMBRE_INVALIDO = codigoErrorBase + OFFSET_ERRORES_ENTIDAD + 1
        val DOCUMENTO_INVALIDO = NOMBRE_INVALIDO + 1
        val FECHA_DE_NACIMIENTO_INVALIDA = DOCUMENTO_INVALIDO + 1
    }

    constructor(persona: Persona) :
            this(
                    persona.idCliente,
                    persona.id,
                    persona.nombreCompleto,
                    TipoDocumento.desdeNegocio(persona.tipoDocumento),
                    persona.numeroDocumento,
                    Genero.desdeNegocio(persona.genero),
                    persona.fechaNacimiento,
                    Categoria.desdeNegocio(persona.categoria),
                    Afiliacion.desdeNegocio(persona.afiliacion),
                    persona.esAnonima,
                    persona.llaveImagen,
                    persona.empresa,
                    persona.nitEmpresa,
                    Tipo.desdeNegocio(persona.tipo)
                )

    override fun aEntidadDeNegocio(): Persona
    {
        return Persona(
                idCliente,
                id,
                nombreCompleto,
                tipoDocumento.valorEnNegocio,
                numeroDocumento,
                genero.valorEnNegocio,
                fechaNacimiento,
                categoria.valorEnNegocio,
                afiliacion.valorEnNegocio,
                esAnonima,
                llaveImagen,
                empresa,
                nitEmpresa,
                tipo.valorEnNegocio

                      )
    }

    enum class TipoDocumento(val valorEnNegocio: Persona.TipoDocumento, val valorEnRed: String)
    {
        CC(Persona.TipoDocumento.CC, "CC"),
        CD(Persona.TipoDocumento.CD, "CD"),
        CE(Persona.TipoDocumento.CE, "CE"),
        PA(Persona.TipoDocumento.PA, "PA"),
        RC(Persona.TipoDocumento.RC, "RC"),
        NIT(Persona.TipoDocumento.NIT, "NIT"),
        NUIP(Persona.TipoDocumento.NUIP, "NUIP"),
        TI(Persona.TipoDocumento.TI, "TI");

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.TipoDocumento): TipoDocumento
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Genero(val valorEnNegocio: Persona.Genero, val valorEnRed: String)
    {
        MASCULINO(Persona.Genero.MASCULINO, "MALE"),
        FEMENINO(Persona.Genero.FEMENINO, "FEMALE"),
        DESCONOCIDO(Persona.Genero.DESCONOCIDO, "UNKNOWN");

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.Genero): Genero
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Categoria(val valorEnNegocio: Persona.Categoria, val valorEnRed: String)
    {
        A(Persona.Categoria.A, "A"),
        B(Persona.Categoria.B, "B"),
        C(Persona.Categoria.C, "C"),
        D(Persona.Categoria.D, "D");

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.Categoria): Categoria
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Afiliacion(val valorEnNegocio: Persona.Afiliacion, val valorEnRed: String)
    {
        COTIZANTE(Persona.Afiliacion.COTIZANTE, "CONTRIBUTOR"),
        BENEFICIARIO(Persona.Afiliacion.BENEFICIARIO, "BENEFICIARY"),
        NO_AFILIADO(Persona.Afiliacion.NO_AFILIADO, "UNAFFILIATED");

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.Afiliacion): Afiliacion
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Tipo(val valorEnNegocio: Persona.Tipo, val valorEnRed: String)
    {
        TRABAJADOR(Persona.Tipo.TRABAJADOR, "EMPLOYEE"),
        FACULTATIVO(Persona.Tipo.FACULTATIVO, "FACULTATIVE"),
        INDEPENDIENTE(Persona.Tipo.INDEPENDIENTE, "INDEPENDENT"),
        PENSIONADO(Persona.Tipo.PENSIONADO, "PENSIONER"),
        AFILIADO_25_AÑOS(Persona.Tipo.AFILIADO_25_AÑOS, "AFFILLIATE25Y"),
        CORTESIA(Persona.Tipo.CORTESIA, "POLITENESS"),
        NO_AFILIADO(Persona.Tipo.NO_AFILIADO, "NONE");


        companion object
        {
            fun desdeNegocio(valorEnNegocio: Persona.Tipo): Tipo
            {
                return values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }
}