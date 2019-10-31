package co.smartobjects.entidades.operativas.ordenes

import co.smartobjects.campos.*
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.utilidades.Decimal
import org.threeten.bp.ZonedDateTime

sealed class Transaccion constructor(
        val idCliente: Long,
        val id: Long?,
        val campoNombreUsuario: CampoNombreUsuario,
        val idUbicacion: Long?,
        val idFondo: Long,
        val codigoExternoFondo: String,
        val campoCantidad: CampoCantidad,
        val idGrupoClientesPersona: Long?,
        val campoIdDispositivo: CampoIdDispositivo
                                    )
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Transaccion::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val NOMBRE_USUARIO = Transaccion::nombreUsuario.name
        @JvmField
        val CANTIDAD = Transaccion::cantidad.name
        @JvmField
        val ID_UBICACION = Transaccion::idUbicacion.name
        @JvmField
        val ID_DISPOSITIVO = Transaccion::idDispositivo.name
    }

    val nombreUsuario: String = campoNombreUsuario.valor
    val cantidad: Decimal = campoCantidad.valor
    val idDispositivo: String = campoIdDispositivo.valor

    constructor(
            idCliente: Long,
            id: Long?,
            nombreUsuario: String,
            idUbicacion: Long?,
            idFondo: Long,
            codigoExternoFondo: String,
            cantidad: Decimal,
            idGrupoClientesPersona: Long?,
            idDispositivo: String
               )
            : this(
            idCliente,
            id,
            CampoNombreUsuario(nombreUsuario),
            idUbicacion,
            idFondo,
            codigoExternoFondo,
            CampoCantidad(cantidad),
            idGrupoClientesPersona,
            CampoIdDispositivo(idDispositivo)
                  )

    abstract fun copiar(
            idCliente: Long = this.idCliente,
            id: Long? = this.id,
            nombreUsuario: String = this.nombreUsuario,
            idUbicacion: Long? = this.idUbicacion,
            idFondo: Long = this.idFondo,
            codigoExternoFondo: String = this.codigoExternoFondo,
            cantidad: Decimal = this.cantidad,
            idGrupoClientesPersona: Long? = this.idGrupoClientesPersona,
            idDispositivo: String = this.idDispositivo)
            : Transaccion

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaccion

        if (idCliente != other.idCliente) return false
        if (id != other.id) return false
        if (idUbicacion != other.idUbicacion) return false
        if (idFondo != other.idFondo) return false
        if (codigoExternoFondo != other.codigoExternoFondo) return false
        if (idGrupoClientesPersona != other.idGrupoClientesPersona) return false
        if (nombreUsuario != other.nombreUsuario) return false
        if (cantidad != other.cantidad) return false
        if (idDispositivo != other.idDispositivo) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idCliente.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + idUbicacion.hashCode()
        result = 31 * result + idFondo.hashCode()
        result = 31 * result + codigoExternoFondo.hashCode()
        result = 31 * result + idGrupoClientesPersona.hashCode()
        result = 31 * result + nombreUsuario.hashCode()
        result = 31 * result + cantidad.hashCode()
        result = 31 * result + idDispositivo.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Transaccion(idCliente=$idCliente, id=$id, idUbicacion=$idUbicacion, idFondo=$idFondo, codigoExternoFondo='$codigoExternoFondo', idGrupoClientesPersona=$idGrupoClientesPersona, nombreUsuario='$nombreUsuario', cantidad=$cantidad, idDispositivo='$idDispositivo')"
    }

    class CampoNombreUsuario(nombreUsuario: String)
        : CampoEntidad<Transaccion, String>(nombreUsuario, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.NOMBRE_USUARIO)

    class CampoCantidad(cantidad: Decimal)
        : CampoEntidad<Transaccion, Decimal>(cantidad, validadorDecimalMayorACero, NOMBRE_ENTIDAD, Campos.CANTIDAD)

    class CampoIdDispositivo(idDispositivo: String)
        : CampoEntidad<Transaccion, String>(idDispositivo, ValidadorCampoStringNoVacio(), NOMBRE_ENTIDAD, Campos.ID_DISPOSITIVO)


    class Debito : Transaccion
    {
        companion object
        {
            @JvmField
            val NOMBRE_ENTIDAD: String = Transaccion.Debito::class.java.simpleName
        }

        constructor(
                idCliente: Long,
                id: Long?,
                nombreUsuario: String,
                idUbicacion: Long,
                idFondo: Long,
                codigoExternoFondo: String,
                cantidad: Decimal,
                idGrupoClientesPersona: Long?,
                idDispositivo: String)
                : super(
                idCliente,
                id,
                nombreUsuario,
                idUbicacion,
                idFondo,
                codigoExternoFondo,
                cantidad,
                idGrupoClientesPersona,
                idDispositivo
                       )

        constructor(
                idCliente: Long,
                id: Long?,
                nombreUsuario: String,
                cantidad: Decimal,
                idGrupoClientesPersona: Long?,
                idDispositivo: String,
                consumible: ConsumibleEnPuntoDeVenta)
                : this(
                idCliente,
                id,
                nombreUsuario,
                consumible.idUbicacion,
                consumible.idConsumible,
                consumible.codigoExternoConsumible,
                cantidad,
                idGrupoClientesPersona,
                idDispositivo
                      )

        fun copiar(
                idCliente: Long = this.idCliente,
                id: Long? = this.id,
                nombreUsuario: String = this.nombreUsuario,
                cantidad: Decimal = this.cantidad,
                idGrupoClientesPersona: Long? = this.idGrupoClientesPersona,
                idDispositivo: String = this.idDispositivo,
                consumible: ConsumibleEnPuntoDeVenta = ConsumibleEnPuntoDeVenta(idUbicacion!!, idFondo, codigoExternoFondo))
                : Transaccion
        {
            return Transaccion.Debito(
                    idCliente,
                    id,
                    nombreUsuario,
                    cantidad,
                    idGrupoClientesPersona,
                    idDispositivo,
                    consumible
                                     )
        }

        override fun copiar(
                idCliente: Long,
                id: Long?,
                nombreUsuario: String,
                idUbicacion: Long?,
                idFondo: Long,
                codigoExternoFondo: String,
                cantidad: Decimal,
                idGrupoClientesPersona: Long?,
                idDispositivo: String
                           ): Transaccion
        {
            return copiar(
                    idCliente,
                    id,
                    nombreUsuario,
                    cantidad,
                    idGrupoClientesPersona,
                    idDispositivo,
                    ConsumibleEnPuntoDeVenta(idUbicacion!!, idFondo, codigoExternoFondo)
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
            return "Transaccion.Debito() ${super.toString()}"
        }
    }

    class Credito private constructor(
            idCliente: Long,
            id: Long?,
            nombreUsuario: String,
            idUbicacion: Long?,
            idFondo: Long,
            codigoExternoFondo: String,
            cantidad: Decimal,
            idGrupoClientesPersona: Long?,
            idDispositivo: String,
            val campoValidoDesde: CampoValidoDesde?,
            val campoValidoHasta: CampoValidoHasta?)
        : Transaccion(idCliente, id, nombreUsuario, idUbicacion, idFondo, codigoExternoFondo, cantidad, idGrupoClientesPersona, idDispositivo)
    {
        companion object
        {
            @JvmField
            val NOMBRE_ENTIDAD: String = Transaccion.Credito::class.java.simpleName
        }

        object Campos
        {
            @JvmField
            val FECHA_VALIDEZ_DESDE = Transaccion.Credito::validoDesde.name
            @JvmField
            val FECHA_VALIDEZ_HASTA = Transaccion.Credito::validoHasta.name
        }

        val validoDesde: ZonedDateTime? = campoValidoDesde?.valor
        val validoHasta: ZonedDateTime? = campoValidoHasta?.valor

        constructor(
                idCliente: Long,
                id: Long?,
                nombreUsuario: String,
                idUbicacion: Long?,
                idFondo: Long,
                codigoExternoFondo: String,
                cantidad: Decimal,
                idGrupoClientesPersona: Long?,
                idDispositivo: String,
                validoDesde: ZonedDateTime?,
                validoHasta: ZonedDateTime?
                   )
                : this(
                idCliente,
                id,
                nombreUsuario,
                idUbicacion,
                idFondo,
                codigoExternoFondo,
                cantidad,
                idGrupoClientesPersona,
                idDispositivo,
                if (validoDesde != null) CampoValidoDesde(validoDesde, validoHasta) else null,
                if (validoHasta != null) CampoValidoHasta(validoHasta) else null
                      )

        fun copiar(
                idCliente: Long = this.idCliente,
                id: Long? = this.id,
                nombreUsuario: String = this.nombreUsuario,
                idUbicacion: Long? = this.idUbicacion,
                idFondo: Long = this.idFondo,
                codigoExternoFondo: String = this.codigoExternoFondo,
                cantidad: Decimal = this.cantidad,
                idGrupoClientesPersona: Long? = this.idGrupoClientesPersona,
                idDispositivo: String = this.idDispositivo,
                validoDesde: ZonedDateTime? = this.validoDesde,
                validoHasta: ZonedDateTime? = this.validoHasta)
                : Transaccion
        {
            return Transaccion.Credito(
                    idCliente,
                    id,
                    nombreUsuario,
                    idUbicacion,
                    idFondo,
                    codigoExternoFondo,
                    cantidad,
                    idGrupoClientesPersona,
                    idDispositivo,
                    validoDesde,
                    validoHasta
                                      )
        }

        override fun copiar(
                idCliente: Long,
                id: Long?,
                nombreUsuario: String,
                idUbicacion: Long?,
                idFondo: Long,
                codigoExternoFondo: String,
                cantidad: Decimal,
                idGrupoClientesPersona: Long?,
                idDispositivo: String
                           ): Transaccion
        {
            return copiar(
                    idCliente,
                    id,
                    nombreUsuario,
                    idUbicacion,
                    idFondo,
                    codigoExternoFondo,
                    cantidad,
                    idGrupoClientesPersona,
                    idDispositivo,
                    validoDesde,
                    validoHasta
                         )
        }

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as Transaccion.Credito

            if (validoDesde != other.validoDesde) return false
            if (validoHasta != other.validoHasta) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = super.hashCode()
            result = 31 * result + validoDesde.hashCode()
            result = 31 * result + validoHasta.hashCode()
            return result
        }

        override fun toString(): String
        {
            return "Transaccion.Credito(validoDesde=$validoDesde, validoHasta=$validoHasta) ${super.toString()}"
        }

        class CampoValidoDesde(validoDesde: ZonedDateTime, validoHasta: ZonedDateTime?)
            : CampoEntidad<Transaccion.Credito, ZonedDateTime>(
                validoDesde,
                ValidadorEnCadena(
                        validadorDeZonaHoraria,
                        validadorFechaMayorAMinima,
                        *(
                                if (validoHasta != null)
                                {
                                    arrayOf<ValidadorCampo<ZonedDateTime>>(ValidadorCampoEsMenorOIgualQueOtroCampo(validoHasta, Campos.FECHA_VALIDEZ_HASTA))
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
            : CampoEntidad<Transaccion.Credito, ZonedDateTime>(
                validoHasta,
                validadorDeZonaHoraria,
                NOMBRE_ENTIDAD,
                Campos.FECHA_VALIDEZ_HASTA)
    }
}