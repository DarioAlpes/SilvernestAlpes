package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.fondos.precios.Precio

@Suppress("EqualsOrHashCode")
class Entrada(
        idCliente: Long,
        id: Long?,
        nombre: String,
        disponibleParaLaVenta: Boolean,
        esIlimitado: Boolean,
        precioPorDefecto: Precio,
        codigoExterno: String,
        idUbicacion: Long)
    : AccesoBase<Entrada>(idCliente, id, nombre, disponibleParaLaVenta, true, esIlimitado, precioPorDefecto, codigoExterno, idUbicacion)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Entrada::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Fondo.Campos.NOMBRE
    }

    override fun copiarConIdCliente(idCliente: Long): Entrada
    {
        return copiar(idCliente = idCliente)
    }

    override fun copiarConId(idNuevo: Long?): Entrada
    {
        return copiar(id = idNuevo)
    }

    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            nombre: String = this.nombre,
            disponibleParaLaVenta: Boolean = this.disponibleParaLaVenta,
            esIlimitado: Boolean = this.esIlimitado,
            precioPorDefecto: Precio = this.precioPorDefecto,
            codigoExterno: String = this.codigoExterno,
            idUbicacion: Long = this.idUbicacion): Entrada
    {
        return Entrada(
                idCliente,
                id,
                nombre,
                disponibleParaLaVenta,
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
        return "Entrada() ${super.toString()}"
    }
}