package co.smartobjects.persistencia.usuarios.roles

import co.smartobjects.entidades.usuarios.PermisoBack
import co.smartobjects.persistencia.EntidadDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = PermisoBackDAO.TABLA)
internal data class PermisoBackDAO constructor(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ENDPOINT, canBeNull = false, uniqueCombo = true)
        val endpoint: String = "",

        @DatabaseField(columnName = COLUMNA_ACCION, canBeNull = false, uniqueCombo = true)
        val accion: AccionDAO = AccionDAO.INVALIDO,

        @DatabaseField(columnName = COLUMNA_ID_ROL, foreign = true, uniqueCombo = true, columnDefinition = "VARCHAR NOT NULL REFERENCES ${RolDAO.TABLA}(${RolDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val rolDAO: RolDAO = RolDAO()
                                              ) : EntidadDAO<PermisoBack>
{
    companion object
    {
        const val TABLA = "permiso_back"
        const val COLUMNA_ID = "id_permiso_back"
        const val COLUMNA_ENDPOINT = "endpoint"
        const val COLUMNA_ACCION = "accion"
        const val COLUMNA_ID_ROL = "fk_${TABLA}_${RolDAO.TABLA}"
    }

    constructor(entidadDeNegocio: PermisoBack, nombreRol: String) : this(
            null,
            entidadDeNegocio.endPoint,
            AccionDAO.desdeNegocio(entidadDeNegocio.accion),
            RolDAO(nombre = nombreRol)
                                                                        )

    override fun aEntidadDeNegocio(idCliente: Long): PermisoBack
    {
        return PermisoBack(
                idCliente,
                endpoint,
                accion.valorEnNegocio!!
                          )
    }

    enum class AccionDAO(val valorEnNegocio: PermisoBack.Accion?)
    {
        INVALIDO(null),
        POST(PermisoBack.Accion.POST),
        PUT(PermisoBack.Accion.PUT),
        PUT_TODOS(PermisoBack.Accion.PUT_TODOS),
        GET_TODOS(PermisoBack.Accion.GET_TODOS),
        GET_UNO(PermisoBack.Accion.GET_UNO),
        PATCH(PermisoBack.Accion.PATCH),
        DELETE(PermisoBack.Accion.DELETE);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: PermisoBack.Accion): AccionDAO
            {
                return AccionDAO.values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }
}