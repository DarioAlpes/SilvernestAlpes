package co.smartobjects.persistencia.fondos.precios.impuestos

import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.EntidadReferenciableDAO
import co.smartobjects.utilidades.Decimal
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.math.BigDecimal

@DatabaseTable(tableName = ImpuestoDAO.TABLA)
internal data class ImpuestoDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        override val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_NOMBRE, uniqueCombo = true, uniqueIndexName = "ux_${COLUMNA_NOMBRE}_$TABLA", canBeNull = false)
        var nombre: String = "",

        // Debe ser BigDecimal porque ORMLite tiene quemado el persister para campos de tipo BIG_DECIMAL en SQLite y si se usa Decimal simplemente ignora el persister
        @DatabaseField(columnName = COLUMNA_TASA, canBeNull = false, dataType = DataType.BIG_DECIMAL_NUMERIC)
        private var tasaBigDecimal: BigDecimal = BigDecimal.ZERO)
    : EntidadDAO<Impuesto>,
      EntidadReferenciableDAO<Long?>
{
    companion object
    {
        const val TABLA = "impuesto"
        const val COLUMNA_ID = "id_impuesto"
        const val COLUMNA_NOMBRE = "nombre"
        const val COLUMNA_TASA = "tasa"
    }

    val tasa: Decimal
        get()
        {
            return Decimal(tasaBigDecimal)
        }

    constructor(entidadDeNegocio: Impuesto)
            : this(
            entidadDeNegocio.id,
            entidadDeNegocio.nombre,
            entidadDeNegocio.tasa.valor
                  )

    override fun aEntidadDeNegocio(idCliente: Long): Impuesto
    {
        return Impuesto(
                idCliente,
                id,
                nombre,
                tasa
                       )
    }
}