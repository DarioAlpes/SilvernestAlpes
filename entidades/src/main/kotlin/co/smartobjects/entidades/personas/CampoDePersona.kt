package co.smartobjects.entidades.personas

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.entidades.EntidadReferenciable


data class CampoDePersona constructor(
        val campo: Predeterminado,
        val esRequerido: CampoEsRequerido)
    : EntidadReferenciable<CampoDePersona.Predeterminado, CampoDePersona>
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = CampoDePersona::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val ES_REQUERIDO = CampoDePersona::esRequerido.name
    }

    override val id: Predeterminado = campo

    constructor(
            campo: Predeterminado,
            esRequerido: Boolean
               ) : this(campo, CampoEsRequerido(esRequerido))

    override fun copiarConId(idNuevo: Predeterminado): CampoDePersona
    {
        return copy(campo = idNuevo)
    }

    class CampoEsRequerido(esRequerido: Boolean)
        : CampoEntidad<CampoDePersona, Boolean>(esRequerido, null, NOMBRE_ENTIDAD, Campos.ES_REQUERIDO)

    enum class Predeterminado(val nombreDelCampo: String, val nombreEnNegocio: String)
    {

        NOMBRE_COMPLETO(Persona.Campos.NOMBRE_COMPLETO, "Nombre completo"),
        TIPO_DOCUMENTO(Persona.Campos.TIPO_DOCUMENTO, "Tipo de documento"),
        NUMERO_DOCUMENTO(Persona.Campos.NUMERO_DOCUMENTO, "Número de documento"),
        GENERO(Persona.Campos.GENERO, "Género"),
        FECHA_NACIMIENTO(Persona.Campos.FECHA_NACIMIENTO, "Fecha de nacimiento"),
        CATEGORIA(Persona.Campos.CATEGORIA, "Categoría"),
        AFILIACION(Persona.Campos.AFILIACION, "Afiliación"),
        ES_ANONIMA(Persona.Campos.ES_ANONIMA, "Es persona anónima"),
        LLAVE_IMAGEN(Persona.Campos.LLAVE_IMAGEN, "Imagen"),
        EMPRESA(Persona.Campos.EMPRESA, "Empresa"),
        NIT_EMPRESA(Persona.Campos.NIT_EMPRESA, "Nit Empresa"),
        TIPO(Persona.Campos.TIPO, "Tipo");

        companion object
        {
            fun desdeNombreEnCodigo(nombreDelCampo: String): Predeterminado
            {
                println("")
                return Predeterminado.values().first { it.nombreDelCampo == nombreDelCampo }
            }

            fun desdeNombreEnNegocio(nombreEnNegocio: String): Predeterminado
            {
                return Predeterminado.values().first { it.nombreEnNegocio == nombreEnNegocio }
            }
        }
    }
}