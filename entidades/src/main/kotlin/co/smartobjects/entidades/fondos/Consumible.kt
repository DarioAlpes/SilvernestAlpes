package co.smartobjects.entidades.fondos

import co.smartobjects.entidades.fondos.precios.Precio

@Suppress("EqualsOrHashCode")
abstract class Consumible<TipoFondo : Consumible<TipoFondo>>(
        idCliente: Long,
        id: Long?,
        nombre: String,
        disponibleParaLaVenta: Boolean,
        debeAparecerSoloUnaVez: Boolean,
        esIlimitado: Boolean,
        precioPorDefecto: Precio,
        codigoExterno: String)
    : Fondo<TipoFondo>(idCliente, id, nombre, disponibleParaLaVenta, debeAparecerSoloUnaVez, esIlimitado, precioPorDefecto, codigoExterno)
{
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun toString(): String
    {
        return "Consumible() ${super.toString()}"
    }
}