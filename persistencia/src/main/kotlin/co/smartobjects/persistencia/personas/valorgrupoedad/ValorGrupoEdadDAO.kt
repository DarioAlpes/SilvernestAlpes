package co.smartobjects.persistencia.personas.valorgrupoedad

import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.persistencia.EntidadDAO
import co.smartobjects.persistencia.EntidadReferenciableDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = ValorGrupoEdadDAO.TABLA)
internal data class ValorGrupoEdadDAO(
        @DatabaseField(columnName = COLUMNA_VALOR, id = true, canBeNull = false)
        val valor: String = "",

        @DatabaseField(columnName = COLUMNA_EDAD_MINIMA, uniqueCombo = true, canBeNull = true)
        val edadMinima: Int? = null,

        @DatabaseField(columnName = COLUMNA_EDAD_MAXIMA, uniqueCombo = true, canBeNull = true)
        val edadMaxima: Int? = null)
    : EntidadDAO<ValorGrupoEdad>,
      EntidadReferenciableDAO<String>
{
    override val id: String = valor

    companion object
    {
        const val TABLA = "valor_grupo_edad"
        const val COLUMNA_VALOR = "valor"
        const val COLUMNA_EDAD_MINIMA = "edad_minima"
        const val COLUMNA_EDAD_MAXIMA = "edad_maxima"
    }

    constructor(entidadDeNegocio: ValorGrupoEdad) : this(entidadDeNegocio.valor, entidadDeNegocio.edadMinima, entidadDeNegocio.edadMaxima)

    override fun aEntidadDeNegocio(idCliente: Long): ValorGrupoEdad
    {
        return ValorGrupoEdad(valor, edadMinima, edadMaxima)
    }
}