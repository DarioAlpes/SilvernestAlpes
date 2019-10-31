package co.smartobjects.campos

import java.io.Serializable

@Suppress("unused")
open class CampoEntidad<out Entidad, out TipoCampo>(
        valorOriginal: TipoCampo,
        validadorCampo: ValidadorCampo<TipoCampo>?,
        nombreEntidad: String,
        nombreCampo: String
                                                   ) : Serializable
{
    val valor: TipoCampo = validadorCampo?.limpiarValorInicial(valorOriginal) ?: valorOriginal

    init
    {
        validadorCampo?.validarCampo(valor, valorOriginal, nombreEntidad, nombreCampo)
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CampoEntidad<*, *>

        if (valor != other.valor) return false

        return true
    }

    override fun hashCode(): Int
    {
        return valor.hashCode()
    }
}


open class CampoModificable<out Entidad, out TipoCampo> internal constructor(
        valorOriginal: TipoCampo,
        validadorCampo: ValidadorCampo<TipoCampo>?,
        nombreEntidad: String,
        override val nombreCampo: String) :
        CampoEntidad<Entidad, TipoCampo>(
                valorOriginal,
                validadorCampo,
                nombreEntidad,
                nombreCampo
                                        ),
        CampoModificableEntidad<Entidad, TipoCampo>
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as CampoModificable<*, *>

        if (nombreCampo != other.nombreCampo) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + nombreCampo.hashCode()
        return result
    }
}

@Suppress("unused")
interface CampoModificableEntidad<out Entidad, out TipoCampo>
{
    val valor: TipoCampo
    val nombreCampo: String
}