package co.smartobjects.persistencia.fondos.paquete

import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.utilidades.Decimal
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.math.BigDecimal

@DatabaseTable(tableName = FondoPaqueteDAO.TABLA)
internal data class FondoPaqueteDAO(
        @DatabaseField(columnName = COLUMNA_ID_DUMMY, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_FONDO, uniqueCombo = true, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${FondoDAO.TABLA}(${FondoDAO.COLUMNA_ID})")
        val fondoDAO: FondoDAO = FondoDAO(),

        @DatabaseField(columnName = COLUMNA_CODIGO_EXTERNO_FONDO_INCLUIDO, canBeNull = false)
        val codigoExternoFondo: String = "",

        @DatabaseField(columnName = COLUMNA_ID_PAQUETE, uniqueCombo = true, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${PaqueteDAO.TABLA}(${PaqueteDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val paqueteDAO: PaqueteDAO = PaqueteDAO(),

        // Debe ser BigDecimal porque ORMLite tiene quemado el persister para campos de tipo BIG_DECIMAL en SQLite y si se usa Decimal simplemente ignora el persister
        @DatabaseField(columnName = COLUMNA_CANTIDAD, canBeNull = false, dataType = DataType.BIG_DECIMAL_NUMERIC)
        private val cantidadBigDecimal: BigDecimal = BigDecimal.ZERO
                                   ) : EntidadDAO<Paquete.FondoIncluido>
{
    companion object
    {
        const val TABLA = "fondo_paquete"
        const val COLUMNA_ID_DUMMY = "id_dummy_fondopaquete"
        const val COLUMNA_CANTIDAD = "cantidad"
        const val COLUMNA_CODIGO_EXTERNO_FONDO_INCLUIDO = "codigo_externo_fondo_incluido"

        const val COLUMNA_ID_FONDO = "fk_${TABLA}_${FondoDAO.TABLA}"
        const val COLUMNA_ID_PAQUETE = "fk_${TABLA}_${PaqueteDAO.TABLA}"
    }

    val cantidad: Decimal
        get()
        {
            return Decimal(cantidadBigDecimal)
        }

    constructor(idPaquete: Long?, fondoDelPaquete: Paquete.FondoIncluido) :
            this(
                    null,
                    FondoDAO(id = fondoDelPaquete.idFondo),
                    fondoDelPaquete.codigoExterno,
                    PaqueteDAO(id = idPaquete),
                    fondoDelPaquete.cantidad.valor
                )

    override fun aEntidadDeNegocio(idCliente: Long): Paquete.FondoIncluido
    {
        return Paquete.FondoIncluido(fondoDAO.id!!, codigoExternoFondo, cantidad)
    }
}