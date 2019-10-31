package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.Jerarquico
import co.smartobjects.entidades.fondos.precios.Precio

class CategoriaSku(
        idCliente: Long,
        id: Long?,
        nombre: String,
        disponibleParaLaVenta: Boolean,
        debeAparecerSoloUnaVez: Boolean,
        esIlimitado: Boolean,
        precioPorDefecto: Precio,
        codigoExterno: String,
        override val idDelPadre: Long?,
        override val idsDeAncestros: LinkedHashSet<Long>,
        val llaveDeIcono: String?)
    : Fondo<CategoriaSku>(idCliente, id, nombre, disponibleParaLaVenta, debeAparecerSoloUnaVez, esIlimitado, precioPorDefecto, codigoExterno),
      Jerarquico<CategoriaSku>
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = CategoriaSku::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Fondo.Campos.NOMBRE
    }

    override fun copiarConIdCliente(idCliente: Long): CategoriaSku
    {
        return copiar(idCliente = idCliente)
    }

    override fun copiarConId(idNuevo: Long?): CategoriaSku
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
            idDelPadre: Long? = this.idDelPadre,
            idsDeAncestros: LinkedHashSet<Long> = this.idsDeAncestros,
            llaveDeIcono: String? = this.llaveDeIcono): CategoriaSku
    {
        return CategoriaSku(
                idCliente,
                id,
                nombre,
                disponibleParaLaVenta,
                debeAparecerSoloUnaVez,
                esIlimitado,
                precioPorDefecto,
                codigoExterno,
                idDelPadre,
                idsDeAncestros,
                llaveDeIcono
                           )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as CategoriaSku

        if (idDelPadre != other.idDelPadre) return false
        if (idsDeAncestros != other.idsDeAncestros) return false
        if (llaveDeIcono != other.llaveDeIcono) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + idDelPadre.hashCode()
        result = 31 * result + idsDeAncestros.hashCode()
        result = 31 * result + llaveDeIcono.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "CategoriaSku(idDelPadre=$idDelPadre, idsDeAncestros=$idsDeAncestros, llaveDeIcono=$llaveDeIcono) ${super.toString()}"
    }
}