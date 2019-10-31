package co.smartobjects.persistencia.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.operativas.reservas.SesionDeManillaDAO
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.persistencia.ubicaciones.UbicacionDAO
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.threeten.bp.ZonedDateTime


@DatabaseTable(tableName = ConteoUbicacionDAO.TABLA)
internal data class ConteoUbicacionDAO
(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_UBICACION, uniqueCombo = true, foreign = true, canBeNull = false, columnDefinition = "BIGINT NOT NULL REFERENCES ${UbicacionDAO.TABLA}(${UbicacionDAO.COLUMNA_ID})")
        val ubicacionDAO: UbicacionDAO = UbicacionDAO(),

        @DatabaseField(columnName = COLUMNA_ID_SESION_DE_MANILLA, uniqueCombo = true, foreign = true, canBeNull = false, columnDefinition = "BIGINT NOT NULL REFERENCES ${SesionDeManillaDAO.TABLA}(${SesionDeManillaDAO.COLUMNA_ID})")
        val sesionDeManillaDAO: SesionDeManillaDAO = SesionDeManillaDAO(),

        @DatabaseField(columnName = "fecha_realizacion", uniqueCombo = true, canBeNull = false, persisterClass = ZonedDateTimeThreeTenType::class)
        val fechaDeRealizacion: ZonedDateTime = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
) : EntidadDAO<ConteoUbicacion>
{
    companion object
    {
        const val TABLA = "conteo_ubicacion"
        const val COLUMNA_ID = "id_conteo_ubicacion"
        const val COLUMNA_ID_UBICACION = "fk_${TABLA}_${UbicacionDAO.TABLA}"
        const val COLUMNA_ID_SESION_DE_MANILLA = "fk_${TABLA}_${SesionDeManillaDAO.TABLA}"
    }

    constructor(entidadNegocio: ConteoUbicacion)
            : this(
            null,
            UbicacionDAO(id = entidadNegocio.idUbicacion),
            SesionDeManillaDAO(id = entidadNegocio.idSesionDeManilla),
            entidadNegocio.fechaDeRealizacion
                  )

    override fun aEntidadDeNegocio(idCliente: Long): ConteoUbicacion
    {
        return ConteoUbicacion(idCliente, ubicacionDAO.id!!, sesionDeManillaDAO.id!!, fechaDeRealizacion)
    }
}