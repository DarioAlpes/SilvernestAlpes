package co.smartobjects.persistencia.operativas.reservas

import co.smartobjects.persistencia.operativas.compras.CreditoFondoDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = CreditoEnSesionDeManillaDAO.TABLA)
internal data class CreditoEnSesionDeManillaDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(
                columnName = COLUMNA_ID_SESION_DE_MANILLA,
                foreign = true,
                canBeNull = false,
                columnDefinition = "BIGINT NOT NULL REFERENCES ${SesionDeManillaDAO.TABLA}(${SesionDeManillaDAO.COLUMNA_ID}) ON DELETE CASCADE"
                      )
        val sesionDeManillaDAO: SesionDeManillaDAO = SesionDeManillaDAO(),

        @DatabaseField(
                columnName = COLUMNA_ID_CREDITO_FONDO,
                foreign = true,
                canBeNull = false,
                columnDefinition = "BIGINT NOT NULL REFERENCES ${CreditoFondoDAO.TABLA}(${CreditoFondoDAO.COLUMNA_ID})"
                      )
        val creditoFondoDAO: CreditoFondoDAO = CreditoFondoDAO()
                                               )
{
    companion object
    {
        const val TABLA = "credito_en_sesion_de_manilla"
        const val COLUMNA_ID = "id_credito_en_sesion_de_manilla"
        const val COLUMNA_ID_SESION_DE_MANILLA = "fk_${TABLA}_${SesionDeManillaDAO.TABLA}"
        const val COLUMNA_ID_CREDITO_FONDO = "fk_${TABLA}_${CreditoFondoDAO.TABLA}"
    }

    constructor(idSesionDeManilla: Long?, idCreditoDeFondo: Long) :
            this(
                    null,
                    SesionDeManillaDAO(id = idSesionDeManilla),
                    CreditoFondoDAO(id = idCreditoDeFondo)
                )
}