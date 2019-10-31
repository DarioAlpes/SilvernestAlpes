package co.smartobjects.entidades.personas

import co.smartobjects.campos.*
import co.smartobjects.entidades.EntidadReferenciable


class ValorGrupoEdad private constructor(
        val campoValor: CampoValor,
        val campoEdadMinima: CampoEdadMinima,
        val edadMaxima: Int?,
        override val id: CampoValor)
    : EntidadReferenciable<ValorGrupoEdad.CampoValor, ValorGrupoEdad>
{
    constructor(
            valor: String,
            edadMinima: Int?,
            edadMaxima: Int?
               ) : this(
            CampoValor(valor),
            CampoEdadMinima(edadMinima, edadMaxima),
            edadMaxima,
            CampoValor(valor)
                       )

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = ValorGrupoEdad::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val VALOR = ValorGrupoEdad::valor.name
        val EDAD_MINIMA = ValorGrupoEdad::edadMinima.name
        val EDAD_MAXIMA = ValorGrupoEdad::edadMaxima.name
    }

    val valor = campoValor.valor
    val edadMinima = campoEdadMinima.valor


    fun copiar(valor: String = this.valor, edadMinima: Int? = this.edadMinima, edadMaxima: Int? = this.edadMaxima): ValorGrupoEdad
    {
        return ValorGrupoEdad(valor, edadMinima, edadMaxima)
    }

    override fun copiarConId(idNuevo: CampoValor): ValorGrupoEdad
    {
        return copiar(valor = idNuevo.valor)
    }

    fun aplicaParaPersona(persona: Persona): Boolean
    {
        val esMayorAMinimo = edadMinima == null || edadMinima <= persona.edad
        val esMenorAMaximo = edadMaxima == null || edadMaxima >= persona.edad
        return esMayorAMinimo && esMenorAMaximo
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValorGrupoEdad

        if (edadMinima != other.edadMinima) return false
        if (edadMaxima != other.edadMaxima) return false
        if (valor != other.valor) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = edadMinima ?: 0
        result = 31 * result + (edadMaxima ?: 0)
        result = 31 * result + valor.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "ValorGrupoEdad(edadMinima=$edadMinima, edadMaxima=$edadMaxima, valor='$valor')"
    }

    class CampoValor(valor: String) : CampoEntidad<ValorGrupoEdad, String>(valor, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.VALOR)
    class CampoEdadMinima(edadMinima: Int?, edadMaxima: Int?) : CampoEntidad<ValorGrupoEdad, Int?>(
            edadMinima,
            ValidadorEnCadena(
                    ValidadorCampoEsNuloOCumpleValidacion(ValidadorCampoConLimiteInferior<Int, Int>(0)),
                    ValidadorCondicional(
                            edadMaxima != null,
                            ValidadorCampoEsNuloOCumpleValidacion(ValidadorCampoEsMenorOIgualQueOtroCampo<Int, Int>(edadMaxima ?: 0, Campos.EDAD_MAXIMA)))
                             ),
            NOMBRE_ENTIDAD,
            Campos.EDAD_MINIMA)

}