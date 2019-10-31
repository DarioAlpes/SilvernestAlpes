package co.smartobjects.entidades.operativas.compras

import co.smartobjects.campos.*
import co.smartobjects.entidades.fondos.precios.ImpuestoSoloTasa
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.entidades.fondos.precios.PrecioCompleto
import co.smartobjects.utilidades.Decimal
import org.threeten.bp.ZonedDateTime

class CreditoFondo(
        val idCliente: Long,
        val id: Long?,
        val campoCantidad: CampoCantidad,
        val campoValorPagado: CampoValorPagado,
        val campoValorImpuestoPagado: CampoValorImpuestoPagado,
        val campoValidoDesde: CampoValidoDesde?,
        val campoValidoHasta: CampoValidoHasta?,
        val consumido: Boolean,
        val campoOrigen: CampoOrigen,
        val campoNombreUsuario: CampoNombreUsuario,
        val idPersonaDueña: Long,
        val idFondoComprado: Long,
        val codigoExternoFondo: String,
        val idImpuestoPagado: Long,
        val campoIdDispositivo: CampoIdDispositivo,
        val idUbicacionCompra: Long?,
        val idGrupoClientesPersona: Long?
                  )
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = CreditoFondo::class.java.simpleName
    }


    object Campos
    {
        @JvmField
        val CANTIDAD = CreditoFondo::cantidad.name
        @JvmField
        val VALOR_PAGADO = CreditoFondo::valorPagado.name
        @JvmField
        val VALOR_IMPUESTO_PAGADO = CreditoFondo::valorImpuestoPagado.name
        @JvmField
        val FECHA_VALIDEZ_DESDE = CreditoFondo::validoDesde.name
        @JvmField
        val FECHA_VALIDEZ_HASTA = CreditoFondo::validoHasta.name
        @JvmField
        val ORIGEN = CreditoFondo::origen.name
        @JvmField
        val NOMBRE_USUARIO = CreditoFondo::nombreUsuario.name
        @JvmField
        val ID_DISPOSITIVO = CreditoFondo::idDispositivo.name
        @JvmField
        val ID_PERSONA_DUEÑA = CreditoFondo::idPersonaDueña.name
    }

    constructor(
            idCliente: Long,
            id: Long?,
            cantidad: Decimal,
            valorPagado: Decimal,
            valorImpuestoPagado: Decimal,
            validoDesde: ZonedDateTime?,
            validoHasta: ZonedDateTime?,
            consumido: Boolean,
            origen: String,
            nombreUsuario: String,
            idPersonaDueña: Long,
            idFondoComprado: Long,
            codigoExternoFondo: String,
            idImpuestoPagado: Long,
            idDispositivo: String,
            idUbicacionCompra: Long?,
            idGrupoClientesPersona: Long?
               ) : this(
            idCliente,
            id,
            CampoCantidad(cantidad),
            CampoValorPagado(valorPagado),
            CampoValorImpuestoPagado(valorImpuestoPagado),
            if (validoDesde != null) CampoValidoDesde(validoDesde, validoHasta) else null,
            if (validoHasta != null) CampoValidoHasta(validoHasta) else null,
            consumido,
            CampoOrigen(origen),
            CampoNombreUsuario(nombreUsuario),
            idPersonaDueña,
            idFondoComprado,
            codigoExternoFondo,
            idImpuestoPagado,
            CampoIdDispositivo(idDispositivo),
            idUbicacionCompra,
            idGrupoClientesPersona
                       )

    val cantidad: Decimal = campoCantidad.valor
    val valorPagado: Decimal = campoValorPagado.valor
    val valorImpuestoPagado: Decimal = campoValorImpuestoPagado.valor
    val validoDesde: ZonedDateTime? = campoValidoDesde?.valor
    val validoHasta: ZonedDateTime? = campoValidoHasta?.valor
    val origen: String = campoOrigen.valor
    val nombreUsuario: String = campoNombreUsuario.valor
    val idDispositivo: String = campoIdDispositivo.valor

    val valorPagadoSinImpuesto = valorPagado - valorImpuestoPagado
    val tasaImpositivaUsada = if (valorPagado == Decimal.CERO) Decimal.CERO else (valorImpuestoPagado / valorPagado) * Decimal(100)

    fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            cantidad: Decimal = this.cantidad,
            valorPagado: Decimal = this.valorPagado,
            valorImpuestoPagado: Decimal = this.valorImpuestoPagado,
            validoDesde: ZonedDateTime? = this.validoDesde,
            validoHasta: ZonedDateTime? = this.validoHasta,
            consumido: Boolean = this.consumido,
            origen: String = this.origen,
            nombreUsuario: String = this.nombreUsuario,
            idPersonaDueña: Long = this.idPersonaDueña,
            idFondoComprado: Long = this.idFondoComprado,
            codigoExternoFondo: String = this.codigoExternoFondo,
            idImpuestoPagado: Long = this.idImpuestoPagado,
            idDispositivo: String = this.idDispositivo,
            idUbicacionCompra: Long? = this.idUbicacionCompra,
            idGrupoClientesPersona: Long? = this.idGrupoClientesPersona): CreditoFondo
    {
        return CreditoFondo(
                idCliente,
                id,
                cantidad,
                valorPagado,
                valorImpuestoPagado,
                validoDesde,
                validoHasta,
                consumido,
                origen,
                nombreUsuario,
                idPersonaDueña,
                idFondoComprado,
                codigoExternoFondo,
                idImpuestoPagado,
                idDispositivo,
                idUbicacionCompra,
                idGrupoClientesPersona
                           )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreditoFondo

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (consumido != other.consumido) return false
        if (idPersonaDueña != other.idPersonaDueña) return false
        if (idFondoComprado != other.idFondoComprado) return false
        if (codigoExternoFondo != other.codigoExternoFondo) return false
        if (idImpuestoPagado != other.idImpuestoPagado) return false
        if (idUbicacionCompra != other.idUbicacionCompra) return false
        if (idGrupoClientesPersona != other.idGrupoClientesPersona) return false
        if (cantidad != other.cantidad) return false
        if (valorPagado != other.valorPagado) return false
        if (valorImpuestoPagado != other.valorImpuestoPagado) return false
        if (validoDesde != other.validoDesde) return false
        if (validoHasta != other.validoHasta) return false
        if (origen != other.origen) return false
        if (nombreUsuario != other.nombreUsuario) return false
        if (idDispositivo != other.idDispositivo) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + consumido.hashCode()
        result = 31 * result + idPersonaDueña.hashCode()
        result = 31 * result + idFondoComprado.hashCode()
        result = 31 * result + codigoExternoFondo.hashCode()
        result = 31 * result + idImpuestoPagado.hashCode()
        result = 31 * result + idUbicacionCompra.hashCode()
        result = 31 * result + idGrupoClientesPersona.hashCode()
        result = 31 * result + cantidad.hashCode()
        result = 31 * result + valorPagado.hashCode()
        result = 31 * result + valorImpuestoPagado.hashCode()
        result = 31 * result + validoDesde.hashCode()
        result = 31 * result + validoHasta.hashCode()
        result = 31 * result + origen.hashCode()
        result = 31 * result + nombreUsuario.hashCode()
        result = 31 * result + idDispositivo.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "CreditoFondo(idCliente=$idCliente, id=$id, consumido=$consumido, idPersonaDueña=$idPersonaDueña, idFondoComprado=$idFondoComprado, codigoExternoFondo='$codigoExternoFondo', idImpuestoPagado=$idImpuestoPagado, idUbicacionCompra=$idUbicacionCompra, idGrupoClientesPersona=$idGrupoClientesPersona, cantidad=$cantidad, valorPagado=$valorPagado, valorImpuestoPagado=$valorImpuestoPagado, validoDesde=$validoDesde, validoHasta=$validoHasta, origen='$origen', nombreUsuario='$nombreUsuario', idDispositivo='$idDispositivo')"
    }


    class CampoCantidad(cantidad: Decimal)
        : CampoEntidad<CreditoFondo, Decimal>(cantidad, validadorDecimalMayorACero, NOMBRE_ENTIDAD, Campos.CANTIDAD)

    class CampoValorPagado(valorPagado: Decimal)
        : CampoEntidad<CreditoFondo, Decimal>(valorPagado, validadorDecimalMayorACero, NOMBRE_ENTIDAD, Campos.VALOR_PAGADO)

    class CampoValorImpuestoPagado(valorImpuestoPagado: Decimal)
        : CampoEntidad<CreditoFondo, Decimal>(valorImpuestoPagado, validadorDecimalMayorACero, NOMBRE_ENTIDAD, Campos.VALOR_IMPUESTO_PAGADO)

    class CampoValidoDesde(fechaActivacion: ZonedDateTime, fechaDesactivacion: ZonedDateTime?)
        : CampoEntidad<CreditoFondo, ZonedDateTime>(
            fechaActivacion,
            ValidadorEnCadena(
                    validadorDeZonaHoraria,
                    validadorFechaMayorAMinima,
                    *(
                            if (fechaDesactivacion != null)
                            {
                                arrayOf<ValidadorCampo<ZonedDateTime>>(ValidadorCampoEsMenorOIgualQueOtroCampo(fechaDesactivacion, Campos.FECHA_VALIDEZ_HASTA))
                            }
                            else
                            {
                                arrayOf()
                            }
                     )
                             ),
            NOMBRE_ENTIDAD,
            Campos.FECHA_VALIDEZ_DESDE)

    class CampoValidoHasta(validoHasta: ZonedDateTime)
        : CampoEntidad<CreditoFondo, ZonedDateTime>(
            validoHasta,
            validadorDeZonaHoraria,
            NOMBRE_ENTIDAD,
            Campos.FECHA_VALIDEZ_HASTA)

    class CampoOrigen(origen: String)
        : CampoEntidad<CreditoFondo, String>(origen, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.ORIGEN)

    class CampoNombreUsuario(nombreUsuario: String)
        : CampoEntidad<CreditoFondo, String>(nombreUsuario, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE_USUARIO)

    class CampoIdDispositivo(idDispositivo: String)
        : CampoEntidad<CreditoFondo, String>(idDispositivo, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.ID_DISPOSITIVO)
}

data class CreditoFondoConNombre(val nombreDeFondo: String, val creditoAsociado: CreditoFondo)
{
    val precioCompleto = PrecioCompleto(
            Precio(creditoAsociado.valorPagado, creditoAsociado.idImpuestoPagado),
            ImpuestoSoloTasa(creditoAsociado.idCliente, creditoAsociado.idImpuestoPagado, creditoAsociado.tasaImpositivaUsada)
                                       )
    val estaPagado = creditoAsociado.id != null
}