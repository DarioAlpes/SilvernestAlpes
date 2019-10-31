package co.smartobjects.entidades.fondos.libros

sealed class Regla<TipoValidado>(val restriccion: TipoValidado)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Regla::class.java.simpleName
    }

    abstract fun validar(valor: TipoValidado): Boolean

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Regla<*>

        if (restriccion != other.restriccion) return false

        return true
    }

    override fun hashCode(): Int
    {
        return restriccion.hashCode()
    }

    override fun toString(): String
    {
        return "Regla(restriccion=$restriccion)"
    }
}

class ReglaDeIdUbicacion(idUbicacion: Long) : Regla<Long>(idUbicacion)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = ReglaDeIdUbicacion::class.java.simpleName
    }

    override fun validar(valor: Long): Boolean = restriccion == valor

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String
    {
        return "ReglaDeIdUbicacion() ${super.toString()}"
    }
}

class ReglaDeIdGrupoDeClientes(idGrupoDeClientes: Long) : Regla<Long>(idGrupoDeClientes)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = ReglaDeIdGrupoDeClientes::class.java.simpleName
    }

    override fun validar(valor: Long): Boolean = restriccion == valor

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String
    {
        return "ReglaDeIdGrupoDeClientes() ${super.toString()}"
    }
}

class ReglaDeIdPaquete(idPaquete: Long) : Regla<Long>(idPaquete)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = ReglaDeIdPaquete::class.java.simpleName
    }

    override fun validar(valor: Long): Boolean = restriccion == valor

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String
    {
        return "ReglaDeIdPaquete() ${super.toString()}"
    }
}