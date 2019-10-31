package co.smartobjects.persistencia.ubicaciones

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.JerarquicoDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = UbicacionDAO.TABLA)
internal data class UbicacionDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true, allowGeneratedIdInsert = true)
        override val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_NOMBRE, uniqueCombo = true, uniqueIndexName = "ux_${COLUMNA_NOMBRE}_$TABLA", canBeNull = false)
        val nombre: String = "",

        @DatabaseField(columnName = COLUMNA_TIPO, canBeNull = false)
        val tipo: Tipo = Tipo.INVALIDO,

        @DatabaseField(columnName = COLUMNA_SUBTIPO, canBeNull = false)
        val subtipo: Subtipo = Subtipo.INVALIDO,

        @DatabaseField(columnName = COLUMNA_ID_PADRE, foreign = true, index = true, columnDefinition = "BIGINT REFERENCES $TABLA($COLUMNA_ID)")
        val padreDAO: UbicacionDAO? = null,

        @DatabaseField(columnName = JerarquicoDAO.NOMBRE_CAMPO_LLAVE, canBeNull = false)
        override var llaveJerarquia: String = "")
    : JerarquicoDAO,
      EntidadDAO<Ubicacion>
{
    companion object
    {
        const val TABLA = "ubicacion"
        const val COLUMNA_ID = "id_ubicacion"
        const val COLUMNA_NOMBRE = "nombre"
        const val COLUMNA_TIPO = "tipo"
        const val COLUMNA_SUBTIPO = "subtipo"
        const val COLUMNA_ID_PADRE = "${COLUMNA_ID}_padre"
    }

    override val idDelPadre: Long?
        get() = padreDAO?.id

    constructor(entidadDeNegocio: Ubicacion) : this(
            entidadDeNegocio.id,
            entidadDeNegocio.nombre,
            Tipo.desdeNegocio(entidadDeNegocio.tipo),
            Subtipo.desdeNegocio(entidadDeNegocio.subtipo),
            UbicacionDAO(entidadDeNegocio.idDelPadre),
            entidadDeNegocio.darLlaveJerarquia()
                                                   )

    constructor(idComoLlaveForanea: Long) : this(id = idComoLlaveForanea)

    override fun aEntidadDeNegocio(idCliente: Long): Ubicacion
    {
        return Ubicacion(idCliente, id, nombre, tipo.valorEnNegocio!!, subtipo.valorEnNegocio!!, padreDAO?.id, darIdsAncestros())
    }

    enum class Tipo(val valorEnNegocio: Ubicacion.Tipo?)
    {
        INVALIDO(null),
        AREA(Ubicacion.Tipo.AREA),
        CIUDAD(Ubicacion.Tipo.CIUDAD),
        PAIS(Ubicacion.Tipo.PAIS),
        PROPIEDAD(Ubicacion.Tipo.PROPIEDAD),
        PUNTO_DE_CONTACTO(Ubicacion.Tipo.PUNTO_DE_CONTACTO),
        PUNTO_DE_INTERES(Ubicacion.Tipo.PUNTO_DE_INTERES),
        REGION(Ubicacion.Tipo.REGION),
        ZONA(Ubicacion.Tipo.ZONA);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Ubicacion.Tipo?): Tipo
            {
                return Tipo.values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }

    enum class Subtipo(val valorEnNegocio: Ubicacion.Subtipo?)
    {
        INVALIDO(null),
        AP(Ubicacion.Subtipo.AP),
        AP_INALAMBRICO(Ubicacion.Subtipo.AP_INALAMBRICO),
        AP_RESTRINGIDO(Ubicacion.Subtipo.AP_RESTRINGIDO),
        KIOSKO(Ubicacion.Subtipo.KIOSKO),
        POS(Ubicacion.Subtipo.POS),
        POS_SIN_DINERO(Ubicacion.Subtipo.POS_SIN_DINERO);

        companion object
        {
            fun desdeNegocio(valorEnNegocio: Ubicacion.Subtipo): Subtipo
            {
                return Subtipo.values().first { it.valorEnNegocio == valorEnNegocio }
            }
        }
    }
}