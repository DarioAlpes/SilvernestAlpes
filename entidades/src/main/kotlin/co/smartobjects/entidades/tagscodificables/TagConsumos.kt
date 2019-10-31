package co.smartobjects.entidades.tagscodificables

import co.smartobjects.entidades.excepciones.EntidadConCampoFueraDeRango
import co.smartobjects.entidades.excepciones.EntidadConCampoVacio
import co.smartobjects.entidades.operativas.compras.CreditoFondo
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import java.nio.ByteBuffer

class TagConsumos : SerializableABytes
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = TagConsumos::class.java.simpleName

        /**
        idSesionDeManilla (long),
        fecha validez desde mínima como millis (long),
        fecha de validez hasta máxima como millis (long),
        número de creditos fondo (byte)
         */
        const val TAMAÑO_EN_BYTES_CABECERA =
                java.lang.Long.BYTES +
                java.lang.Long.BYTES +
                java.lang.Long.BYTES +
                java.lang.Byte.BYTES
    }

    val idSesionDeManilla: Long
    val fondosEnTag: List<FondoEnTag>
    val fechaValidoDesdeMinima: ZonedDateTime?
    val fechaValidoHastaMaxima: ZonedDateTime?
    override val tamañoTotalEnBytes: Int

    @Suppress("UsePropertyAccessSyntax")
    constructor(arregloBytesComprimido: ByteArray) : super()
    {
        if (arregloBytesComprimido.size < TAMAÑO_EN_BYTES_CABECERA)
        {
            throw EntidadConCampoFueraDeRango(
                    NOMBRE_ENTIDAD,
                    "Tamaño del arreglo de bytes",
                    arregloBytesComprimido.size.toString(),
                    TAMAÑO_EN_BYTES_CABECERA.toString(),
                    EntidadConCampoFueraDeRango.Limite.INFERIOR
                                             )
        }

        val buffer = ByteBuffer.allocate(arregloBytesComprimido.size)
        buffer.put(arregloBytesComprimido)
        buffer.flip()

        idSesionDeManilla = buffer.getLong()
        val millisFechaInicioReserva = buffer.getLong()
        val millisFechaFinalReserva = buffer.getLong()
        val numeroDeCreditos = buffer.get().toInt()

        if (numeroDeCreditos <= 0)
        {
            throw EntidadConCampoFueraDeRango(
                    NOMBRE_ENTIDAD,
                    "Número de créditos a leer en arreglo",
                    numeroDeCreditos.toString(),
                    "1",
                    EntidadConCampoFueraDeRango.Limite.INFERIOR
                                             )
        }

        tamañoTotalEnBytes = TAMAÑO_EN_BYTES_CABECERA + numeroDeCreditos * FondoEnTag.TAMAÑO_EN_BYTES

        if (arregloBytesComprimido.size < tamañoTotalEnBytes)
        {
            throw EntidadConCampoFueraDeRango(
                    NOMBRE_ENTIDAD,
                    "Tamaño esperado del arreglo",
                    arregloBytesComprimido.size.toString(),
                    tamañoTotalEnBytes.toString(),
                    EntidadConCampoFueraDeRango.Limite.DIFERENTE
                                             )
        }

        fechaValidoDesdeMinima =
                if (millisFechaInicioReserva == -1L)
                {
                    null
                }
                else
                {
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(millisFechaInicioReserva), ZONA_HORARIA_POR_DEFECTO)
                }

        fechaValidoHastaMaxima =
                if (millisFechaFinalReserva == -1L)
                {
                    null
                }
                else
                {
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(millisFechaFinalReserva), ZONA_HORARIA_POR_DEFECTO)
                }

        val _creditosFondoEnTag = mutableListOf<FondoEnTag>()

        for (i in 0 until numeroDeCreditos)
        {
            _creditosFondoEnTag.add(FondoEnTag(buffer))
        }
        fondosEnTag = _creditosFondoEnTag
    }

    constructor(idSesionDeManilla: Long, creditosFondoACodificar: List<CreditoFondo>) : super()
    {
        this.idSesionDeManilla = idSesionDeManilla

        if (creditosFondoACodificar.isEmpty())
        {
            throw EntidadConCampoVacio(NOMBRE_ENTIDAD, "créditos a codificar")
        }

        if (creditosFondoACodificar.size > 255)
        {
            throw EntidadConCampoFueraDeRango(
                    NOMBRE_ENTIDAD,
                    "número de créditos a codificar",
                    creditosFondoACodificar.size.toString(),
                    "255",
                    EntidadConCampoFueraDeRango.Limite.SUPERIOR
                                             )
        }


        val creditosConvertidos = mutableListOf<FondoEnTag>()
        var fechaMinima: ZonedDateTime? = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).plusYears(1000)
        var fechaMaxima: ZonedDateTime? = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO).minusYears(1000)

        for (creditoFondo in creditosFondoACodificar)
        {
            creditosConvertidos.add(FondoEnTag(creditoFondo))

            if (creditoFondo.validoDesde == null
                || fechaMinima != null && fechaMinima > creditoFondo.validoDesde)
            {
                fechaMinima = creditoFondo.validoDesde
            }

            if (creditoFondo.validoHasta == null
                || fechaMaxima != null && fechaMaxima < creditoFondo.validoHasta)
            {
                fechaMaxima = creditoFondo.validoHasta
            }
        }

        fondosEnTag = creditosConvertidos.toList()
        fechaValidoDesdeMinima = fechaMinima
        fechaValidoHastaMaxima = fechaMaxima

        tamañoTotalEnBytes = TAMAÑO_EN_BYTES_CABECERA + fondosEnTag.size * FondoEnTag.TAMAÑO_EN_BYTES
    }

    private constructor(
            idSesionDeManilla: Long,
            creditosFondoEnTag: List<FondoEnTag>,
            fechaValidoDesdeMinima: ZonedDateTime?,
            fechaValidoHastaMaxima: ZonedDateTime?
                       ) : super()
    {
        this.idSesionDeManilla = idSesionDeManilla
        this.fondosEnTag = creditosFondoEnTag
        this.fechaValidoDesdeMinima = fechaValidoDesdeMinima
        this.fechaValidoHastaMaxima = fechaValidoHastaMaxima
        tamañoTotalEnBytes = TAMAÑO_EN_BYTES_CABECERA + creditosFondoEnTag.size * FondoEnTag.TAMAÑO_EN_BYTES
    }


    fun actualizarCreditos(creditosFondoEnTag: List<FondoEnTag> = this.fondosEnTag): TagConsumos
    {
        return TagConsumos(idSesionDeManilla, creditosFondoEnTag, fechaValidoDesdeMinima, fechaValidoHastaMaxima)
    }


    fun puedeConsumirEnFecha(fechaHora: ZonedDateTime): Boolean
    {
        return when
        {
            fechaValidoDesdeMinima == null
            && fechaValidoHastaMaxima == null -> true
            fechaValidoDesdeMinima == null    -> fechaHora <= fechaValidoHastaMaxima
            fechaValidoHastaMaxima == null    -> fechaHora >= fechaValidoDesdeMinima
            else                              -> fechaHora in fechaValidoDesdeMinima..fechaValidoHastaMaxima
        }
    }

    override fun escribirComoBytes(buffer: ByteBuffer)
    {
        buffer.putLong(idSesionDeManilla)
        buffer.putLong(fechaValidoDesdeMinima?.toInstant()?.toEpochMilli() ?: -1L)
        buffer.putLong(fechaValidoHastaMaxima?.toInstant()?.toEpochMilli() ?: -1L)
        buffer.put(fondosEnTag.size.toByte())

        for (credito in fondosEnTag)
        {
            credito.escribirComoBytes(buffer)
        }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TagConsumos

        if (idSesionDeManilla != other.idSesionDeManilla) return false
        if (fondosEnTag != other.fondosEnTag) return false
        if (fechaValidoDesdeMinima != other.fechaValidoDesdeMinima) return false
        if (fechaValidoHastaMaxima != other.fechaValidoHastaMaxima) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = idSesionDeManilla.hashCode()
        result = 31 * result + fondosEnTag.hashCode()
        result = 31 * result + fechaValidoDesdeMinima.hashCode()
        result = 31 * result + fechaValidoHastaMaxima.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "TagConsumos(idSesionDeManilla=$idSesionDeManilla, fondosEnTag=$fondosEnTag, fechaValidoDesdeMinima=$fechaValidoDesdeMinima, fechaValidoHastaMaxima=$fechaValidoHastaMaxima, tamañoTotalEnBytes=$tamañoTotalEnBytes)"
    }
}