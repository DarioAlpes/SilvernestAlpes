package co.smartobjects.persistencia.operativas.reservas

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.persistencia.personas.PersonaDAO
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.threeten.bp.ZonedDateTime
import java.util.*

@DatabaseTable(tableName = SesionDeManillaDAO.TABLA)
internal data class SesionDeManillaDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_PERSONA, foreign = true, canBeNull = false, columnDefinition = "BIGINT NOT NULL REFERENCES ${PersonaDAO.TABLA}(${PersonaDAO.COLUMNA_ID})")
        val personaDao: PersonaDAO = PersonaDAO(),

        @DatabaseField(columnName = COLUMNA_UUID_TAG, dataType = DataType.BYTE_ARRAY)
        val uuidTag: ByteArray? = null,

        @DatabaseField(columnName = COLUMNA_FECHA_ACTIVACION, persisterClass = ZonedDateTimeThreeTenType::class)
        val fechaActivacion: ZonedDateTime? = null,

        @DatabaseField(columnName = COLUMNA_FECHA_DESACTIVACION, persisterClass = ZonedDateTimeThreeTenType::class)
        val fechaDesactivacion: ZonedDateTime? = null,

        @DatabaseField(columnName = COLUMNA_ID_RESERVA, foreign = true, canBeNull = false, columnDefinition = "VARCHAR NOT NULL REFERENCES ${ReservaDAO.TABLA}(${ReservaDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val reservaDao: ReservaDAO = ReservaDAO()
                                      )
{
    companion object
    {
        const val TABLA = "sesion_de_manilla"
        const val COLUMNA_ID = "id_sesion_de_manilla"
        const val COLUMNA_ID_PERSONA = "fk_${TABLA}_${PersonaDAO.TABLA}"
        const val COLUMNA_UUID_TAG = "uuid_tag"
        const val COLUMNA_FECHA_ACTIVACION = "fecha_activacion"
        const val COLUMNA_FECHA_DESACTIVACION = "fecha_desactivacion"
        const val COLUMNA_ID_RESERVA = "fk_${TABLA}_${ReservaDAO.TABLA}"
    }

    constructor(entidadDeNegocio: SesionDeManilla, idReserva: String) :
            this(
                    null,
                    PersonaDAO(id = entidadDeNegocio.idPersona),
                    null,
                    null,
                    null,
                    ReservaDAO(id = idReserva)
                )

    fun aEntidadDeNegocio(idCliente: Long, creditosCodificados: List<CreditoEnSesionDeManillaDAO>): SesionDeManilla
    {
        return SesionDeManilla(
                idCliente,
                id,
                personaDao.id!!,
                uuidTag,
                fechaActivacion,
                fechaDesactivacion,
                creditosCodificados.map { it.creditoFondoDAO.id!! }.toSet()
                              )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SesionDeManillaDAO

        if (id != other.id) return false
        if (personaDao != other.personaDao) return false
        if (!Arrays.equals(uuidTag, other.uuidTag)) return false
        if (fechaActivacion != other.fechaActivacion) return false
        if (fechaDesactivacion != other.fechaDesactivacion) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = id.hashCode()
        result = 31 * result + personaDao.hashCode()
        result = 31 * result + (uuidTag?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + fechaActivacion.hashCode()
        result = 31 * result + fechaDesactivacion.hashCode()
        return result
    }
}