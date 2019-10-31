package co.smartobjects.utilidades


sealed class Opcional<TipoClase>
{
    companion object
    {
        fun <T : Any?> Vacio(): Opcional<T> = Opcional.Vacio()

        fun <T> De(valor: T): Opcional<T> = Opcional.Valor(valor)

        fun <T : Any> DeNullable(valor: T?): Opcional<T>
        {
            return if (valor == null)
            {
                Vacio()
            }
            else
            {
                De(valor)
            }
        }
    }

    abstract val esVacio: Boolean

    abstract val valor: TipoClase

    //orElse de Optional
    abstract fun valorUOtro(otro: TipoClase?): TipoClase?

    private data class Valor<T>(override val valor: T) : Opcional<T>()
    {
        override fun toString() = "Valor $valor"

        override val esVacio: Boolean = false

        override fun valorUOtro(otro: T?): T?
        {
            return valor
        }
    }

    private class Vacio<T> : Opcional<T>()
    {
        override val esVacio: Boolean = true

        override val valor: T
            get() = throw NoSuchElementException()

        override fun valorUOtro(otro: T?): T?
        {
            return otro
        }

        override fun equals(other: Any?): Boolean
        {
            return other is Vacio<*>
        }

        override fun hashCode(): Int
        {
            return esVacio.hashCode()
        }
    }
}