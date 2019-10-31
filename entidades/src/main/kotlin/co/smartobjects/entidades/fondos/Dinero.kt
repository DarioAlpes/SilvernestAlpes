package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.fondos.precios.Precio

@Suppress("EqualsOrHashCode")
class Dinero(
        idCliente: Long,
        id: Long?,
        nombre: String,
        disponibleParaLaVenta: Boolean,
        debeAparecerSoloUnaVez: Boolean,
        esIlimitado: Boolean,
        precioPorDefecto: Precio,
        codigoExterno: String)
    : Fondo<Dinero>(idCliente, id, nombre, disponibleParaLaVenta, debeAparecerSoloUnaVez, esIlimitado, precioPorDefecto, codigoExterno)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Dinero::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Fondo.Campos.NOMBRE
    }

    override fun copiarConIdCliente(idCliente: Long): Dinero
    {
        return copiar(idCliente = idCliente)
    }

    override fun copiarConId(idNuevo: Long?): Dinero
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
            codigoExterno: String = this.codigoExterno
              ): Dinero
    {
        return Dinero(idCliente, id, nombre, disponibleParaLaVenta, debeAparecerSoloUnaVez, esIlimitado, precioPorDefecto, codigoExterno)
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
        return "Dinero() ${super.toString()}"
    }
}
