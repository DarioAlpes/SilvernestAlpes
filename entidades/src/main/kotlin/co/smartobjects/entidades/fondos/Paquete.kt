package co.smartobjects.entidades.fondos

import co.smartobjects.campos.*
import co.smartobjects.entidades.EntidadReferenciable
import co.smartobjects.utilidades.Decimal
import org.threeten.bp.ZonedDateTime

class Paquete private constructor(
        val idCliente: Long,
        override val id: Long?,
        val campoNombre: CampoNombre,
        val campoDescripcion: CampoDescripcion,
        val campoDisponibleParaLaVenta: CampoDisponibleParaLaVenta,
        val campoValidoDesde: CampoValidoDesde,
        val campoValidoHasta: CampoValidoHasta,
        val campoFondosIncluidos: CampoFondosIncluidos,
        val codigoExterno: String)
    : EntidadReferenciable<Long?, Paquete>
{
    constructor(
            idCliente: Long,
            id: Long?,
            nombre: String,
            descripcion: String,
            disponibleParaLaVenta: Boolean,
            validoDesde: ZonedDateTime,
            validoHasta: ZonedDateTime,
            fondosIncluidos: List<FondoIncluido>,
            codigoExterno: String
               ) :
            this(
                    idCliente,
                    id,
                    CampoNombre(nombre),
                    CampoDescripcion(descripcion),
                    CampoDisponibleParaLaVenta(disponibleParaLaVenta),
                    CampoValidoDesde(validoDesde, validoHasta),
                    CampoValidoHasta(validoHasta),
                    CampoFondosIncluidos(fondosIncluidos),
                    codigoExterno
                )

    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Paquete::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE = Paquete::nombre.name
        @JvmField
        val DESCRIPCION = Paquete::descripcion.name
        @JvmField
        val DISPONIBLE_PARA_LA_VENTA = Paquete::disponibleParaLaVenta.name
        @JvmField
        val FECHA_VALIDEZ_DESDE = Paquete::validoDesde.name
        @JvmField
        val FECHA_VALIDEZ_HASTA = Paquete::validoHasta.name
        @JvmField
        val FONDOS_INCLUIDOS = Paquete::fondosIncluidos.name
    }

    val nombre = campoNombre.valor
    val descripcion = campoDescripcion.valor
    val fondosIncluidos = campoFondosIncluidos.valor
    val disponibleParaLaVenta = campoDisponibleParaLaVenta.valor
    val validoDesde = campoValidoDesde.valor
    val validoHasta = campoValidoHasta.valor


    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            nombre: String = this.nombre,
            descripcion: String = this.descripcion,
            disponibleParaLaVenta: Boolean = this.disponibleParaLaVenta,
            validoDesde: ZonedDateTime = this.validoDesde,
            validoHasta: ZonedDateTime = this.validoHasta,
            fondosIncluidos: List<FondoIncluido> = this.fondosIncluidos,
            codigoExterno: String = this.codigoExterno
              ): Paquete
    {
        return Paquete(idCliente, id, nombre, descripcion, disponibleParaLaVenta, validoDesde, validoHasta, fondosIncluidos, codigoExterno)
    }

    override fun copiarConId(idNuevo: Long?): Paquete
    {
        return copiar(id = idNuevo)
    }


    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Paquete

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (disponibleParaLaVenta != other.disponibleParaLaVenta) return false
        if (nombre != other.nombre) return false
        if (descripcion != other.descripcion) return false
        if (fondosIncluidos != other.fondosIncluidos) return false
        if (validoDesde != other.validoDesde) return false
        if (validoHasta != other.validoHasta) return false
        if (codigoExterno != other.codigoExterno) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + disponibleParaLaVenta.hashCode()
        result = 31 * result + nombre.hashCode()
        result = 31 * result + descripcion.hashCode()
        result = 31 * result + fondosIncluidos.hashCode()
        result = 31 * result + validoDesde.hashCode()
        result = 31 * result + validoHasta.hashCode()
        result = 31 * result + codigoExterno.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Paquete(idCliente=$idCliente, id=$id, nombre='$nombre', disponibleParaLaVenta=$disponibleParaLaVenta, validoDesde=$validoDesde, validoHasta=$validoHasta, fondosIncluidos=$fondosIncluidos, descripcion='$descripcion', codigoExterno='$codigoExterno')"
    }

    class CampoNombre(nombre: String)
        : CampoModificable<Paquete, String>(nombre, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE)

    class CampoDescripcion(descripcion: String)
        : CampoModificable<Paquete, String>(descripcion, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.DESCRIPCION)

    class CampoFondosIncluidos(fondosIncluidos: List<FondoIncluido>)
        : CampoEntidad<Paquete, List<FondoIncluido>>(fondosIncluidos, ValidadorCampoColeccionNoVacio(), NOMBRE_ENTIDAD, Campos.FONDOS_INCLUIDOS)

    class CampoValidoDesde(validoDesde: ZonedDateTime, validoHasta: ZonedDateTime)
        : CampoEntidad<Paquete, ZonedDateTime>(
            validoDesde,
            ValidadorEnCadena<ZonedDateTime>(
                    validadorDeZonaHoraria,
                    validadorFechaMayorAMinima,
                    ValidadorCampoEsMenorOIgualQueOtroCampo(validoHasta, Campos.FECHA_VALIDEZ_HASTA)
                                            ),
            NOMBRE_ENTIDAD,
            Campos.FECHA_VALIDEZ_DESDE)

    class CampoValidoHasta(validoHasta: ZonedDateTime)
        : CampoEntidad<Paquete, ZonedDateTime>(
            validoHasta,
            validadorDeZonaHoraria,
            NOMBRE_ENTIDAD,
            Campos.FECHA_VALIDEZ_HASTA)

    class CampoDisponibleParaLaVenta(disponibleParaLaVenta: Boolean)
        : CampoModificable<Paquete, Boolean>(disponibleParaLaVenta, null, NOMBRE_ENTIDAD, Campos.DISPONIBLE_PARA_LA_VENTA)

    data class FondoIncluido(val idFondo: Long, val codigoExterno: String, val cantidad: Decimal)
    {
        companion object
        {
            @JvmField
            val NOMBRE_ENTIDAD: String = FondoIncluido::class.java.simpleName
        }
    }
}