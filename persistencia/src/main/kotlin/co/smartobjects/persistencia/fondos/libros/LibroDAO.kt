package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = LibroDAO.TABLA)
internal data class LibroDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_NOMBRE, uniqueCombo = true, canBeNull = false)
        val nombre: String = "",

        @DatabaseField(columnName = COLUMNA_TIPO_LIBRO, uniqueCombo = true, canBeNull = false)
        val tipo: TipoEnBD = TipoEnBD.DESCONOCIDO
                            )
{
    companion object
    {
        const val TABLA = "libro"
        const val COLUMNA_ID = "id_libro"
        const val COLUMNA_TIPO_LIBRO = "tipo_de_libro"
        const val COLUMNA_NOMBRE = "nombre"
    }

    enum class TipoEnBD
    {
        PRECIOS, PROHIBICIONES, DESCONOCIDO;
    }

    constructor(entidadDeNegocio: LibroDePrecios)
            : this(
            entidadDeNegocio.id,
            entidadDeNegocio.nombre,
            TipoEnBD.PRECIOS
                  )

    constructor(entidadDeNegocio: LibroDeProhibiciones)
            : this(
            entidadDeNegocio.id,
            entidadDeNegocio.nombre,
            TipoEnBD.PROHIBICIONES
                  )

    fun aEntidadDeNegocio(idCliente: Long, precios: Set<PrecioDeFondoDAO>): LibroDePrecios
    {
        return LibroDePrecios(
                idCliente,
                id,
                nombre,
                precios.mapTo(LinkedHashSet()) { it.aEntidadDeNegocio(idCliente) }
                             )
    }

    fun aEntidadDeNegocio(idCliente: Long, prohibiciones: Set<ProhibicionDAO>): LibroDeProhibiciones
    {
        val prohibicionesConvertidas = ProhibicionDAO.convertirAEntidadesDeNegocio(prohibiciones)

        return LibroDeProhibiciones(
                idCliente,
                id,
                nombre,
                prohibicionesConvertidas.prohibicionesDeFondos,
                prohibicionesConvertidas.prohibicionesDePaquetes
                                   )
    }
}