package co.smartobjects.persistencia.clientes.llavesnfc

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.threeten.bp.ZonedDateTime


@DatabaseTable(tableName = LlaveNFCDAO.TABLA)
internal class LlaveNFCDAO
(
        @DatabaseField(columnName = COLUMNA_LLAVE, canBeNull = false, width = 256)
        val llave: String = "",

        @DatabaseField(columnName = COLUMNA_FECHA_CREACION, uniqueIndex = true, canBeNull = false, persisterClass = ZonedDateTimeThreeTenType::class)
        val fechaCreacion: ZonedDateTime = ZonedDateTime.now(ZONA_HORARIA_POR_DEFECTO)
) : EntidadDAO<Cliente.LlaveNFC>
{
    companion object
    {
        const val TABLA = "llave_nfc_cliente"
        const val COLUMNA_ID = "id_llave_nfc"
        const val COLUMNA_LLAVE = "llave"
        const val COLUMNA_FECHA_CREACION = "fecha_creacion_llave"
    }

    constructor(entidadDeNegocio: Cliente.LlaveNFC) : this(String(entidadDeNegocio.llave), entidadDeNegocio.fechaCreacion)

    override fun aEntidadDeNegocio(idCliente: Long): Cliente.LlaveNFC
    {
        return Cliente.LlaveNFC(idCliente, llave, fechaCreacion)
    }
}