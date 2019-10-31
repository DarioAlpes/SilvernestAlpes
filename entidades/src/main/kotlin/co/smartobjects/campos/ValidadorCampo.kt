package co.smartobjects.campos

import co.smartobjects.entidades.excepciones.EntidadMalInicializada

interface ValidadorCampo<TipoCampo>
{
    @Throws(EntidadMalInicializada::class)
    fun validarCampo(valorLimpio: TipoCampo, valorOriginal: TipoCampo, nombreEntidad: String, nombreCampo: String)

    fun limpiarValorInicial(valorOriginal: TipoCampo): TipoCampo = valorOriginal
}