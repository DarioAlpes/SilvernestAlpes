package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.PrecioEnLibro
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.precios.impuestos.ImpuestoDAO
import co.smartobjects.utilidades.Decimal
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.math.BigDecimal

@DatabaseTable(tableName = PrecioDeFondoDAO.TABLA)
internal data class PrecioDeFondoDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_LIBRO, foreign = true, uniqueCombo = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${LibroDAO.TABLA}(${LibroDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val libro: LibroDAO = LibroDAO(),

        @DatabaseField(columnName = COLUMNA_ID_FONDO, foreign = true, uniqueCombo = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${FondoDAO.TABLA}(${FondoDAO.COLUMNA_ID})")
        val fondo: FondoDAO = FondoDAO(),

        @DatabaseField(columnName = COLUMNA_ID_IMPUESTO, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${ImpuestoDAO.TABLA}(${ImpuestoDAO.COLUMNA_ID})")
        val impuesto: ImpuestoDAO = ImpuestoDAO(),

        // Debe ser BigDecimal porque ORMLite tiene quemado el persister para campos de tipo BIG_DECIMAL en SQLite y si se usa Decimal simplemente ignora el persister
        @DatabaseField(columnName = COLUMNA_PRECIO, canBeNull = false, dataType = DataType.BIG_DECIMAL_NUMERIC)
        private val precioBigDecimal: BigDecimal = BigDecimal.ZERO)
    : EntidadDAO<PrecioEnLibro>
{
    companion object
    {
        const val TABLA = "precio_para_fondo"
        const val COLUMNA_ID = "id_precio_fondo"
        const val COLUMNA_PRECIO = "precio"

        const val COLUMNA_ID_LIBRO = "fk_${TABLA}_${LibroDAO.TABLA}"
        const val COLUMNA_ID_FONDO = "fk_${TABLA}_${FondoDAO.TABLA}"
        const val COLUMNA_ID_IMPUESTO = "fk_${TABLA}_${ImpuestoDAO.TABLA}"
    }

    val precio: Decimal
        get()
        {
            return Decimal(precioBigDecimal)
        }

    constructor(idLibroDePrecios: Long?, precioDeFondo: PrecioEnLibro) :
            this(
                    null,
                    LibroDAO(id = idLibroDePrecios),
                    FondoDAO(id = precioDeFondo.idFondo),
                    ImpuestoDAO(id = precioDeFondo.precio.idImpuesto),
                    precioDeFondo.precio.valor.valor
                )

    override fun aEntidadDeNegocio(idCliente: Long): PrecioEnLibro
    {
        return PrecioEnLibro(
                Precio(precio, impuesto.id!!),
                fondo.id!!
                            )
    }
}