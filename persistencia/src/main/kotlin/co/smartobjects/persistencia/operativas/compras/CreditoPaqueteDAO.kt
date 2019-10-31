package co.smartobjects.persistencia.operativas.compras

import co.smartobjects.entidades.operativas.compras.CreditoPaquete
import co.smartobjects.persistencia.fondos.paquete.PaqueteDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = CreditoPaqueteDAO.TABLA)
internal data class CreditoPaqueteDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_CODIGO_EXTERNO_PAQUETE, canBeNull = false)
        val codigoExternoPaquete: String = "",

        @DatabaseField(columnName = COLUMNA_ID_COMPRA, foreign = true, columnDefinition = "VARCHAR NOT NULL REFERENCES ${CompraDAO.TABLA}(${CompraDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val compraDAO: CompraDAO = CompraDAO(),

        @DatabaseField(columnName = COLUMNA_ID_PAQUETE, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${PaqueteDAO.TABLA}(${PaqueteDAO.COLUMNA_ID})")
        val paqueteDAO: PaqueteDAO = PaqueteDAO()
                                     )
{
    companion object
    {
        const val TABLA = "credito_paquete"
        const val COLUMNA_ID = "id_credito_paquete"
        const val COLUMNA_CODIGO_EXTERNO_PAQUETE = "codigo_externo_paquete"
        const val COLUMNA_ID_COMPRA = "fk_${TABLA}_${CompraDAO.TABLA}"
        const val COLUMNA_ID_PAQUETE = "fk_${TABLA}_${PaqueteDAO.TABLA}"
    }

    constructor(entidadDeNegocio: CreditoPaquete, idCompra: String) :
            this(
                    null,
                    entidadDeNegocio.codigoExternoPaquete,
                    CompraDAO(id = idCompra),
                    PaqueteDAO(id = entidadDeNegocio.idPaquete)
                )

    fun aEntidadDeNegocio(idCliente: Long, creditosFondos: List<CreditoFondoDAO>): CreditoPaquete
    {
        return CreditoPaquete(
                paqueteDAO.id!!,
                codigoExternoPaquete,
                creditosFondos.map { it.aEntidadDeNegocio(idCliente) }
                             )
    }
}