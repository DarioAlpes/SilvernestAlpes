package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.fondos.precios.Precio


abstract class AccesoBase<TipoAcceso : AccesoBase<TipoAcceso>>(
        idCliente: Long,
        id: Long?,
        nombre: String,
        disponibleParaLaVenta: Boolean,
        debeAparecerSoloUnaVez: Boolean,
        esIlimitado: Boolean,
        precioPorDefecto: Precio,
        codigoExterno: String,
        val idUbicacion: Long)
    : Consumible<TipoAcceso>(idCliente, id, nombre, disponibleParaLaVenta, debeAparecerSoloUnaVez, esIlimitado, precioPorDefecto, codigoExterno)
{
    object Campos
    {
        @JvmField
        val NOMBRE = Fondo.Campos.NOMBRE
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as AccesoBase<*>

        if (idUbicacion != other.idUbicacion) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + idUbicacion.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "AccesoBase(idUbicacion=$idUbicacion) ${super.toString()}"
    }
}

@Suppress("EqualsOrHashCode")
class Acceso(
        idCliente: Long,
        id: Long?,
        nombre: String,
        disponibleParaLaVenta: Boolean,
        debeAparecerSoloUnaVez: Boolean,
        esIlimitado: Boolean,
        precioPorDefecto: Precio,
        codigoExterno: String,
        idUbicacion: Long)
    : AccesoBase<Acceso>(idCliente, id, nombre, disponibleParaLaVenta, debeAparecerSoloUnaVez, esIlimitado, precioPorDefecto, codigoExterno, idUbicacion)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Acceso::class.java.simpleName
    }

    override fun copiarConIdCliente(idCliente: Long): Acceso
    {
        return copiar(idCliente = idCliente)
    }

    override fun copiarConId(idNuevo: Long?): Acceso
    {
        return copiar(id = idNuevo)
    }

    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            nombre: String = this.nombre,
            disponibleParaLaVenta: Boolean = this.disponibleParaLaVenta,
            debeAparecerSoloUnaVez: Boolean = this.debeAparecerSoloUnaVez,
            esIlimitado: Boolean = this.esIlimitado,
            precioPorDefecto: Precio = this.precioPorDefecto,
            codigoExterno: String = this.codigoExterno,
            idUbicacion: Long = this.idUbicacion): Acceso
    {
        return Acceso(
                idCliente,
                id,
                nombre,
                disponibleParaLaVenta,
                debeAparecerSoloUnaVez,
                esIlimitado,
                precioPorDefecto,
                codigoExterno,
                idUbicacion
                     )
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
        return "Acceso() ${super.toString()}"
    }

}
