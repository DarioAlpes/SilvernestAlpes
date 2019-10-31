package co.smartobjects.persistencia.fondos.paquete

import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.threeten.bp.ZonedDateTime

@DatabaseTable(tableName = PaqueteDAO.TABLA)
internal data class PaqueteDAO
(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_NOMBRE, uniqueCombo = true, uniqueIndexName = "ux_${COLUMNA_NOMBRE}_$TABLA", canBeNull = false)
        var nombre: String = "",

        @DatabaseField(columnName = COLUMNA_DESCRIPCION, canBeNull = false)
        val descripcion: String = "",

        @DatabaseField(columnName = COLUMNA_DISPONIBLE_PARA_LA_VENTA, canBeNull = false, throwIfNull = true)
        val disponibleParaLaVenta: Boolean = true,

        @DatabaseField(columnName = COLUMNA_VALIDO_DESDE, canBeNull = false, persisterClass = ZonedDateTimeThreeTenType::class)
        val validoDesde: ZonedDateTime = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),

        @DatabaseField(columnName = COLUMNA_VALIDO_HASTA, persisterClass = ZonedDateTimeThreeTenType::class)
        val validoHasta: ZonedDateTime = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO),

        @DatabaseField(columnName = COLUMNA_CODIGO_EXTERNO, canBeNull = false)
        val codigoExterno: String = ""
)
{
    companion object
    {
        const val TABLA = "paquete"
        const val COLUMNA_ID = "id_paquete"
        const val COLUMNA_NOMBRE = "nombre"
        const val COLUMNA_DESCRIPCION = "descripcion"
        const val COLUMNA_DISPONIBLE_PARA_LA_VENTA = "disponible_para_la_venta"
        const val COLUMNA_VALIDO_DESDE = "valida_desde"
        const val COLUMNA_VALIDO_HASTA = "valida_hasta"
        const val COLUMNA_CODIGO_EXTERNO = "codigo_externo_paquete"
    }

    constructor(entidadDeNegocio: Paquete) :
            this(
                    entidadDeNegocio.id,
                    entidadDeNegocio.nombre,
                    entidadDeNegocio.descripcion,
                    entidadDeNegocio.disponibleParaLaVenta,
                    entidadDeNegocio.validoDesde,
                    entidadDeNegocio.validoHasta,
                    entidadDeNegocio.codigoExterno
                )

    fun aEntidadDeNegocioConPrecio(idCliente: Long, fondosIncluidos: List<FondoPaqueteDAO>): Paquete
    {
        return Paquete(
                idCliente,
                id,
                nombre,
                descripcion,
                disponibleParaLaVenta,
                validoDesde,
                validoHasta,
                fondosIncluidos.map { it.aEntidadDeNegocio(idCliente) },
                codigoExterno
                      )
    }
}