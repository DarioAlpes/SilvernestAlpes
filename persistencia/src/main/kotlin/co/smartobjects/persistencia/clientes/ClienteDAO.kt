package co.smartobjects.persistencia.clientes

import co.smartobjects.entidades.clientes.Cliente
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = ClienteDAO.TABLA)
internal data class ClienteDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_NOMBRE, canBeNull = false, uniqueCombo = true, uniqueIndexName = "ux_${COLUMNA_NOMBRE}_$TABLA")
        val nombre: String = ""
                              )
{
    companion object
    {
        const val TABLA = "cliente"
        const val COLUMNA_ID = "id_cliente"
        const val COLUMNA_NOMBRE = "nombre"
    }

    constructor(entidadNegocio: Cliente) :
            this(
                    entidadNegocio.id,
                    entidadNegocio.nombre
                )

    fun aEntidadDeNegocio(): Cliente
    {
        return Cliente(id, nombre)
    }
}