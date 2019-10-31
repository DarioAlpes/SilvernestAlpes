package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroSegunReglas
import co.smartobjects.persistencia.EntidadReferenciableDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = LibroSegunReglasDAO.TABLA)
internal data class LibroSegunReglasDAO(
        @DatabaseField(columnName = LibroSegunReglasDAO.COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        override val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_NOMBRE, uniqueCombo = true, uniqueIndexName = "ux_${COLUMNA_NOMBRE}_$TABLA", canBeNull = false)
        val nombre: String = "",

        @DatabaseField(columnName = COLUMNA_ID_LIBRO, foreign = true, canBeNull = false, columnDefinition = "BIGINT NOT NULL REFERENCES ${LibroDAO.TABLA}(${LibroDAO.COLUMNA_ID})")
        val libro: LibroDAO = LibroDAO())
    : EntidadReferenciableDAO<Long?>
{
    companion object
    {
        const val TABLA = "libro_segun_reglas"
        const val COLUMNA_ID = "id"
        const val COLUMNA_NOMBRE = "nombre"
        const val COLUMNA_ID_LIBRO = "fk_${TABLA}_${LibroDAO.TABLA}"
    }

    constructor(entidadDeNegocio: LibroSegunReglas)
            : this(
            entidadDeNegocio.id,
            entidadDeNegocio.nombre,
            LibroDAO(id = entidadDeNegocio.idLibro)
                  )

    fun aEntidadDeNegocioConReglas(idCliente: Long, reglasDAO: List<ReglaDAO>): LibroSegunReglas
    {
        val reglasConvertidas = ReglaDAO.convertirAEntidadesDeNegocio(reglasDAO)

        return LibroSegunReglas(
                idCliente,
                id,
                nombre,
                libro.id!!,
                reglasConvertidas.reglasIdUbicacion,
                reglasConvertidas.reglasIdGrupoDeClientes,
                reglasConvertidas.reglasIdPaquete
                               )
    }
}