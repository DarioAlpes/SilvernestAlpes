package co.smartobjects.entidades.fondos

import co.smartobjects.campos.CampoEntidad
import co.smartobjects.campos.CampoModificable
import co.smartobjects.campos.ValidadorCampoStringNoVacio
import co.smartobjects.entidades.EntidadReferenciable
import co.smartobjects.entidades.fondos.precios.Precio

abstract class Fondo<TipoFondo : Fondo<TipoFondo>> private constructor(
        val idCliente: Long,
        override val id: Long?,
        val campoNombre: CampoNombre<TipoFondo>,
        val campoDisponibleParaLaVenta: CampoDisponibleParaLaVenta<TipoFondo>,
        val debeAparecerSoloUnaVez: Boolean,
        val esIlimitado: Boolean,
        val precioPorDefecto: Precio,
        val codigoExterno: String)
    : EntidadReferenciable<Long?, TipoFondo>
{
    constructor(
            idCliente: Long,
            id: Long?,
            nombre: String,
            disponibleParaLaVenta: Boolean,
            debeAparecerSoloUnaVez: Boolean,
            esIlimitado: Boolean,
            precioPorDefecto: Precio,
            codigoExterno: String
               ) :
            this(
                    idCliente,
                    id,
                    CampoNombre(nombre),
                    CampoDisponibleParaLaVenta(disponibleParaLaVenta),
                    debeAparecerSoloUnaVez,
                    esIlimitado,
                    precioPorDefecto,
                    codigoExterno
                )

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Fondo::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Fondo<*>::nombre.name

        @JvmField
        val DISPONIBLE_PARA_LA_VENTA = Fondo<*>::disponibleParaLaVenta.name
    }

    val nombre = campoNombre.valor
    val disponibleParaLaVenta = campoDisponibleParaLaVenta.valor

    abstract fun copiarConIdCliente(idCliente: Long): TipoFondo

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fondo<*>

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (debeAparecerSoloUnaVez != other.debeAparecerSoloUnaVez) return false
        if (esIlimitado != other.esIlimitado) return false
        if (precioPorDefecto != other.precioPorDefecto) return false
        if (nombre != other.nombre) return false
        if (disponibleParaLaVenta != other.disponibleParaLaVenta) return false
        if (codigoExterno != other.codigoExterno) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + debeAparecerSoloUnaVez.hashCode()
        result = 31 * result + esIlimitado.hashCode()
        result = 31 * result + precioPorDefecto.hashCode()
        result = 31 * result + nombre.hashCode()
        result = 31 * result + disponibleParaLaVenta.hashCode()
        result = 31 * result + codigoExterno.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Fondo(idCliente=$idCliente, id=$id, debeAparecerSoloUnaVez=$debeAparecerSoloUnaVez, esIlimitado=$esIlimitado, precioPorDefecto=$precioPorDefecto, nombre='$nombre', disponibleParaLaVenta=$disponibleParaLaVenta, codigoExterno='$codigoExterno')"
    }

    class CampoNombre<TipoFondo : Fondo<TipoFondo>>(nombre: String)
        : CampoEntidad<TipoFondo, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE)

    class CampoDisponibleParaLaVenta<TipoFondo : Fondo<TipoFondo>>(disponibleParaLaVenta: Boolean)
        : CampoModificable<TipoFondo, Boolean>(disponibleParaLaVenta, null, NOMBRE_ENTIDAD, Campos.DISPONIBLE_PARA_LA_VENTA)
}