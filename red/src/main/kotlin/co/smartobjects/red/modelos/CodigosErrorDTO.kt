package co.smartobjects.red.modelos


open class CodigosErrorDTO(protected val codigoErrorBase: Int)
{
    companion object
    {
        internal const val OFFSET_ERRORES_BD = 20
        internal const val OFFSET_ERRORES_ENTIDAD = 40
    }

    @JvmField
    val NO_EXISTE: Int = codigoErrorBase

    @JvmField
    val ENTIDAD_DUPLICADA_EN_BD: Int = codigoErrorBase + OFFSET_ERRORES_BD + 1

    @JvmField
    val ENTIDAD_REFERENCIADA: Int = codigoErrorBase + OFFSET_ERRORES_BD + 2

    @JvmField
    val ERROR_DE_BD_DESCONOCIDO: Int = codigoErrorBase + OFFSET_ERRORES_BD

    @JvmField
    val ENTIDAD_REFERENCIADA_NO_EXISTE: Int = codigoErrorBase + 1

    @JvmField
    val ERROR_DE_ENTIDAD_DESCONOCIDO: Int = codigoErrorBase + OFFSET_ERRORES_ENTIDAD
}