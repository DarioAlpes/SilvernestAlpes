package co.smartobjects.persistencia.fondos

import co.smartobjects.entidades.fondos.*
import co.smartobjects.entidades.fondos.precios.Precio
import co.smartobjects.persistencia.EntidadReferenciableDAO
import co.smartobjects.persistencia.fondos.precios.impuestos.ImpuestoDAO
import co.smartobjects.utilidades.Decimal
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.math.BigDecimal

@DatabaseTable(tableName = FondoDAO.TABLA)
internal data class FondoDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        override val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_NOMBRE, uniqueCombo = true, canBeNull = false)
        val nombre: String = "",

        @DatabaseField(columnName = COLUMNA_DISPONIBLE_PARA_LA_VENTA, canBeNull = false, throwIfNull = true)
        val disponibleParaLaVenta: Boolean = true,

        @DatabaseField(columnName = COLUMNA_DEBE_APARECER_SOLO_UNA_VEZ, canBeNull = false, throwIfNull = true)
        val debeAparecerSoloUnaVez: Boolean = false,

        @DatabaseField(columnName = COLUMNA_ES_ILIMITADO, canBeNull = false, throwIfNull = true)
        val esIlimitado: Boolean = false,

        @DatabaseField(columnName = COLUMNA_IMPUESTO_POR_DEFECTO, foreign = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${ImpuestoDAO.TABLA}(${ImpuestoDAO.COLUMNA_ID})")
        val impuestoPorDefectoDAO: ImpuestoDAO = ImpuestoDAO(),

        // Debe ser BigDecimal porque ORMLite tiene quemado el persister para campos de tipo BIG_DECIMAL en SQLite y si se usa Decimal simplemente ignora el persister
        @DatabaseField(columnName = COLUMNA_PRECIO_POR_DEFECTO, canBeNull = false, dataType = DataType.BIG_DECIMAL_NUMERIC)
        private val precioPorDefectoBigDecimal: BigDecimal = BigDecimal.ZERO,

        @DatabaseField(columnName = COLUMNA_CODIGO_EXTERNO_FONDO, canBeNull = false)
        val codigoExterno: String = "",

        @DatabaseField(columnName = COLUMNA_TIPO_DE_FONDO, uniqueCombo = true, canBeNull = false)
        val tipoDeFondo: TipoDeFondoEnBD = TipoDeFondoEnBD.DESCONOCIDO)
    : EntidadReferenciableDAO<Long?>
{
    companion object
    {
        const val TABLA = "fondo"
        const val COLUMNA_ID = "id_fondo"
        const val COLUMNA_NOMBRE = "nombre"
        const val COLUMNA_DISPONIBLE_PARA_LA_VENTA = "disponible_para_la_venta"
        const val COLUMNA_DEBE_APARECER_SOLO_UNA_VEZ = "debe_aparecer_solo_una_vez"
        const val COLUMNA_ES_ILIMITADO = "es_ilimitado"
        const val COLUMNA_IMPUESTO_POR_DEFECTO = "id_impuesto_por_defecto"
        const val COLUMNA_PRECIO_POR_DEFECTO = "precio_por_defecto"
        const val COLUMNA_CODIGO_EXTERNO_FONDO = "codigo_externo_fondo"
        const val COLUMNA_TIPO_DE_FONDO = "tipo_de_fondo"
    }

    val precio: Decimal
        get()
        {
            return Decimal(precioPorDefectoBigDecimal)
        }

    constructor(entidadDeNegocio: Fondo<*>) : this(
            entidadDeNegocio.id,
            entidadDeNegocio.nombre,
            entidadDeNegocio.disponibleParaLaVenta,
            entidadDeNegocio.debeAparecerSoloUnaVez,
            entidadDeNegocio.esIlimitado,
            ImpuestoDAO(id = entidadDeNegocio.precioPorDefecto.idImpuesto),
            entidadDeNegocio.precioPorDefecto.valor.valor,
            entidadDeNegocio.codigoExterno,
            TipoDeFondoEnBD.aTipoDeFondo(entidadDeNegocio)
                                                  )

    fun aEntidadDeNegocio(idCliente: Long): Fondo<*>
    {
        return when (tipoDeFondo)
        {
            TipoDeFondoEnBD.DINERO ->
            {
                Dinero(
                        idCliente,
                        id,
                        nombre,
                        disponibleParaLaVenta,
                        debeAparecerSoloUnaVez,
                        esIlimitado,
                        Precio(precio, impuestoPorDefectoDAO.id!!),
                        codigoExterno
                      )
            }
            else                   ->
            {
                throw RuntimeException("El tipo de fondo $tipoDeFondo no se puede convertir a entidad de negocio desde fondo")
            }
        }
    }

    enum class TipoDeFondoEnBD
    {
        CATEGORIA_SKU, DINERO, SKU, ACCESO, ENTRADA, DESCONOCIDO;

        companion object
        {
            internal val TIPO_FONDOS_CONSUMIBLES = listOf(TipoDeFondoEnBD.ACCESO, TipoDeFondoEnBD.ENTRADA, TipoDeFondoEnBD.SKU)
            fun aTipoDeFondo(fondo: Fondo<*>): TipoDeFondoEnBD
            {
                return when (fondo)
                {
                    is CategoriaSku        -> CATEGORIA_SKU
                    is Dinero              -> DINERO
                    is Sku                 -> SKU
                    is Entrada             -> ENTRADA
                    is Acceso, !is Entrada -> ACCESO
                    else                   -> throw RuntimeException("No existe conversión apropiada para el fondo '${fondo.javaClass.canonicalName}'")
                }
            }
        }
    }
}