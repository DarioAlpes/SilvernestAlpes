package co.smartobjects.entidades.fondos.libros

sealed class Prohibicion(val id: Long)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Prohibicion::class.java.simpleName
    }

    class DeFondo(id: Long) : Prohibicion(id)
    {
        companion object
        {
            @JvmField
            val NOMBRE_ENTIDAD: String = Prohibicion.DeFondo::class.java.simpleName
        }

        fun copiar(id: Long = this.id): DeFondo
        {
            return DeFondo(id)
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false
            return true
        }

        override fun toString(): String
        {
            return "DeFondo() ${super.toString()}"
        }
    }

    class DePaquete(id: Long) : Prohibicion(id)
    {
        companion object
        {
            @JvmField
            val NOMBRE_ENTIDAD: String = Prohibicion.DePaquete::class.java.simpleName
        }

        fun copiar(id: Long = this.id): DePaquete
        {
            return DePaquete(id)
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false
            return true
        }

        override fun toString(): String
        {
            return "DePaquete() ${super.toString()}"
        }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Prohibicion

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int
    {
        return id.hashCode()
    }

    override fun toString(): String
    {
        return "Prohibicion(id=$id)"
    }
}