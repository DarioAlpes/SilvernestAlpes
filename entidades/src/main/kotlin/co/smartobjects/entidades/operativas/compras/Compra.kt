package co.smartobjects.entidades.operativas.compras

import co.smartobjects.campos.*
import co.smartobjects.entidades.operativas.EntidadTransaccional
import org.threeten.bp.ZonedDateTime
import java.util.*

class Compra private constructor
(
        val idCliente: Long,
        nombreUsuario: String,
        uuid: UUID?,
        tiempoCreacion: Long?,
        creacionTerminada: Boolean?,
        val creditosFondos: List<CreditoFondo>,
        val creditosPaquetes: List<CreditoPaquete>,
        val campoPagos: CampoPagos,
        val campoFechaDeRealizacion: CampoFechaDeRealizacion
) : EntidadTransaccional<Compra>(nombreUsuario, uuid, tiempoCreacion, creacionTerminada)
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Compra::class.java.simpleName
    }

    object Campos
    {
        @JvmField
        val CREDITOS = Compra::creditos.name
        @JvmField
        val PAGOS = Compra::pagos.name
        @JvmField
        val FECHA_REALIZACION = Compra::fechaDeRealizacion.name
    }

    val pagos: List<Pago> = campoPagos.valor
    val fechaDeRealizacion: ZonedDateTime = campoFechaDeRealizacion.valor
    val campoCreditos = CampoCreditos(creditosFondos + creditosPaquetes.flatMap { it.creditosFondos })
    val creditos: List<CreditoFondo> = campoCreditos.valor

    // Constructor a usar para crear nueva entidad y obtener id según campos
    constructor(
            idCliente: Long,
            nombreUsuario: String,
            creditosFondos: List<CreditoFondo>,
            creditosPaquetes: List<CreditoPaquete>,
            pagos: List<Pago>,
            fechaDeRealizacion: ZonedDateTime
               ) : this(
            idCliente,
            nombreUsuario,
            null, // Se pasa null para delegar asignación de campos de id a subclase
            null,  // Se pasa null para delegar asignación de campos de id a subclase
            null, // Se pasa null para delegar asignación de campos de id a subclase
            creditosFondos,
            creditosPaquetes,
            CampoPagos(pagos),
            CampoFechaDeRealizacion(fechaDeRealizacion)
                       )

    // Constructor a usar para copiar y crear una entidad previamente guardada
    constructor(
            idCliente: Long,
            nombreUsuario: String,
            uuid: UUID,
            tiempoCreacion: Long,
            creacionTerminada: Boolean,
            creditosFondos: List<CreditoFondo>,
            creditosPaquetes: List<CreditoPaquete>,
            pagos: List<Pago>,
            fechaDeRealizacion: ZonedDateTime
               ) : this(
            idCliente,
            nombreUsuario,
            uuid,
            tiempoCreacion,
            creacionTerminada,
            creditosFondos,
            creditosPaquetes,
            CampoPagos(pagos),
            CampoFechaDeRealizacion(fechaDeRealizacion)
                       )


    fun copiar(
            idCliente: Long = this.idCliente,
            creacionTerminada: Boolean = this.creacionTerminada,
            creditosFondos: List<CreditoFondo> = this.creditosFondos,
            creditosPaquetes: List<CreditoPaquete> = this.creditosPaquetes,
            pagos: List<Pago> = this.pagos,
            fechaDeRealizacion: ZonedDateTime = this.fechaDeRealizacion
              ): Compra
    {
        return Compra(
                idCliente,
                nombreUsuario,
                uuid,
                tiempoCreacion,
                creacionTerminada,
                creditosFondos,
                creditosPaquetes,
                pagos,
                fechaDeRealizacion
                     )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Compra

        if (idCliente != other.idCliente) return false
        if (creditosFondos != other.creditosFondos) return false
        if (creditosPaquetes != other.creditosPaquetes) return false
        if (pagos != other.pagos) return false
        if (fechaDeRealizacion != other.fechaDeRealizacion) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + idCliente.hashCode()
        result = 31 * result + creditosFondos.hashCode()
        result = 31 * result + creditosPaquetes.hashCode()
        result = 31 * result + pagos.hashCode()
        result = 31 * result + fechaDeRealizacion.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "Compra(idCliente=$idCliente, pagos=$pagos, fechaDeRealizacion=$fechaDeRealizacion, creditosFondos=$creditosFondos, creditosPaquetes=$creditosPaquetes) ${super.toString()}"
    }


    class CampoPagos(pagos: List<Pago>)
        : CampoEntidad<Compra, List<Pago>>(pagos, ValidadorCampoColeccionNoVacio(), NOMBRE_ENTIDAD, Campos.PAGOS)

    class CampoCreditos(creditos: List<CreditoFondo>)
        : CampoEntidad<Compra, List<CreditoFondo>>(creditos, ValidadorCampoColeccionNoVacio(), NOMBRE_ENTIDAD, Campos.CREDITOS)

    class CampoFechaDeRealizacion(fechaDeRealizacion: ZonedDateTime)
        : CampoEntidad<Compra, ZonedDateTime>(
            fechaDeRealizacion,
            ValidadorEnCadena(validadorDeZonaHoraria, validadorFechaMayorAMinima),
            NOMBRE_ENTIDAD,
            Campos.FECHA_REALIZACION)
}