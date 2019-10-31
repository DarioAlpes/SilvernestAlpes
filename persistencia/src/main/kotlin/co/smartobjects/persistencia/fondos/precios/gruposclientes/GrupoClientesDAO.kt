package co.smartobjects.persistencia.fondos.precios.gruposclientes

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.persistencia.fondos.precios.SegmentoClientesDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = GrupoClientesDAO.TABLA)
internal data class GrupoClientesDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_NOMBRE, canBeNull = false, uniqueCombo = true, uniqueIndexName = "ux_${COLUMNA_NOMBRE}_$TABLA")
        val nombre: String = ""
                                    )
{
    companion object
    {
        const val TABLA = "grupo_cliente"
        const val COLUMNA_ID = "id_grupo_clientes"
        const val COLUMNA_NOMBRE = "nombre"
    }

    constructor(entidadDeNegocio: GrupoClientes) :
            this(
                    entidadDeNegocio.id,
                    entidadDeNegocio.nombre
                )

    fun aEntidadDeNegocio(idCliente: Long, segmentosClientesDAO: List<SegmentoClientesDAO>): GrupoClientes
    {
        return GrupoClientes(id!!, nombre, segmentosClientesDAO.map { it.aEntidadDeNegocio(idCliente) })
    }
}