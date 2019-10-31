package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.fondos.precios.Precio


class Sku(
        idCliente: Long,
        id: Long?,
        nombre: String,
        disponibleParaLaVenta: Boolean,
        debeAparecerSoloUnaVez: Boolean,
        esIlimitado: Boolean,
        precioPorDefecto: Precio,
        codigoExterno: String,
        val idDeCategoria: Long,
        val llaveDeImagen: String?)
    : Consumible<Sku>(idCliente, id, nombre, disponibleParaLaVenta, debeAparecerSoloUnaVez, esIlimitado, precioPorDefecto, codigoExterno)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Sku::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Fondo.Campos.NOMBRE
    }

    override fun copiarConIdCliente(idCliente: Long): Sku
    {
        return copiar(idCliente = idCliente)
    }

    override fun copiarConId(idNuevo: Long?): Sku
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
            idDeCategoria: Long = this.idDeCategoria,
            llaveDeImagen: String? = this.llaveDeImagen): Sku
    {
        return Sku(
                idCliente,
                id,
                nombre,
                disponibleParaLaVenta,
                debeAparecerSoloUnaVez,
                esIlimitado,
                precioPorDefecto,
                codigoExterno,
                idDeCategoria,
                llaveDeImagen
                  )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Sku

        if (idDeCategoria != other.idDeCategoria) return false
        if (llaveDeImagen != other.llaveDeImagen) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + idDeCategoria.hashCode()
        result = 31 * result + llaveDeImagen.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Sku(idDeCategoria=$idDeCategoria, llaveDeImagen=$llaveDeImagen) ${super.toString()}"
    }
}