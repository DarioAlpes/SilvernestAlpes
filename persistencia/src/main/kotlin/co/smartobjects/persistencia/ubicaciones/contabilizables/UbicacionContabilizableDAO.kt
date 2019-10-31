package co.smartobjects.persistencia.ubicaciones.contabilizables

import co.smartobjects.persistencia.ubicaciones.UbicacionDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = UbicacionContabilizableDAO.TABLA)
internal data class UbicacionContabilizableDAO
(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_UBICACION, uniqueCombo = true, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${UbicacionDAO.TABLA}(${UbicacionDAO.COLUMNA_ID})")
        val ubicacionDAO: UbicacionDAO = UbicacionDAO()
)
{
    companion object
    {
        const val TABLA = "ubicacion_contabilizable"
        const val COLUMNA_ID = "id_ubicacion_contabilizable"
        const val COLUMNA_ID_UBICACION = "fk_${TABLA}_${UbicacionDAO.TABLA}"
    }


    constructor(idUbicacion: Long) : this(null, UbicacionDAO(id = idUbicacion))
}