package co.smartobjects.persistencia.personas.relacionesdepersonas

import co.smartobjects.entidades.personas.Persona
import co.smartobjects.persistencia.personas.PersonaDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = RelacionPersonaDAO.TABLA)
internal data class RelacionPersonaDAO(
        @DatabaseField(columnName = COLUMNA_ID_DUMMY, generatedId = true)
        val id: Long? = null,

        @DatabaseField(
                columnName = COLUMNA_ID_PADRE,
                foreign = true,
                canBeNull = false,
                index = true, indexName = "ix_$COLUMNA_ID_PADRE",
                columnDefinition = "BIGINT NOT NULL REFERENCES ${PersonaDAO.TABLA}(${PersonaDAO.COLUMNA_ID}) ON DELETE CASCADE"
                      )
        val personaPadreDao: PersonaDAO = PersonaDAO(),

        @DatabaseField(
                columnName = COLUMNA_ID_HIJO,
                foreign = true,
                canBeNull = false,
                columnDefinition = "BIGINT NOT NULL REFERENCES ${PersonaDAO.TABLA}(${PersonaDAO.COLUMNA_ID}) ON DELETE CASCADE"
                      )
        val personaHijaDao: PersonaDAO = PersonaDAO()
                                      )
{
    companion object
    {
        const val TABLA = "relacion_persona"
        const val COLUMNA_ID_DUMMY = "id_relacion_persona"
        const val COLUMNA_ID_PADRE = "fk_${TABLA}_${PersonaDAO.TABLA}_padre"
        const val COLUMNA_ID_HIJO = "fk_${TABLA}_${PersonaDAO.TABLA}_hijo"
    }

    constructor(personaPadre: Persona, personaHija: Persona)
            : this(null, PersonaDAO(personaPadre), PersonaDAO(personaHija))
}