package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.Prohibicion
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.paquete.PaqueteDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = ProhibicionDAO.TABLA)
internal data class ProhibicionDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_LIBRO, foreign = true, uniqueCombo = true, columnDefinition = "BIGINT NOT NULL REFERENCES ${LibroDAO.TABLA}(${LibroDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val libro: LibroDAO = LibroDAO(),

        @DatabaseField(columnName = COLUMNA_ID_FONDO, foreign = true, uniqueCombo = true, columnDefinition = "BIGINT NULL REFERENCES ${FondoDAO.TABLA}(${FondoDAO.COLUMNA_ID})")
        val fondoDAO: FondoDAO? = null,

        @DatabaseField(columnName = COLUMNA_ID_PAQUETE, foreign = true, uniqueCombo = true, columnDefinition = "BIGINT NULL REFERENCES ${PaqueteDAO.TABLA}(${PaqueteDAO.COLUMNA_ID})")
        val paqueteDao: PaqueteDAO? = null,

        @DatabaseField(columnName = COLUMNA_TIPO, uniqueCombo = true, canBeNull = false)
        val tipo: Tipo = Tipo.DESCONOCIDO
                                  )
{
    companion object
    {
        const val TABLA = "prohibicion"
        const val COLUMNA_ID = "id_prohibicion"
        const val COLUMNA_TIPO = "tipo"

        const val COLUMNA_ID_LIBRO = "fk_${TABLA}_${LibroDAO.TABLA}"
        const val COLUMNA_ID_FONDO = "fk_${TABLA}_${FondoDAO.TABLA}"
        const val COLUMNA_ID_PAQUETE = "fk_${TABLA}_${PaqueteDAO.TABLA}"


        fun convertirAEntidadesDeNegocio(prohibicionesDao: Set<ProhibicionDAO>): ProhibicionesConvertidas
        {
            val prohibicionesDeFondos: MutableSet<Prohibicion.DeFondo> = mutableSetOf()
            val prohibicionesDePaquetes: MutableSet<Prohibicion.DePaquete> = mutableSetOf()

            for (prohibicionDAO in prohibicionesDao)
            {
                when (prohibicionDAO.tipo)
                {
                    Tipo.FONDO       ->
                    {
                        prohibicionesDeFondos.add(prohibicionDAO.convertirAProhibicionDeFondo()!!)
                    }
                    Tipo.PAQUETE     ->
                    {
                        prohibicionesDePaquetes.add(prohibicionDAO.convertirAProhibicionDePaquete()!!)
                    }
                    Tipo.DESCONOCIDO ->
                    {
                        throw IllegalStateException("Existe una prohibicion en base de datos con tipo desconocido")
                    }
                }
            }

            return ProhibicionesConvertidas(prohibicionesDeFondos, prohibicionesDePaquetes)
        }
    }

    constructor(idLibroDeProhibiciones: Long?, prohibicionDeFondo: Prohibicion.DeFondo) :
            this(
                    null,
                    LibroDAO(id = idLibroDeProhibiciones),
                    FondoDAO(id = prohibicionDeFondo.id),
                    null,
                    Tipo.FONDO
                )

    constructor(idLibroDeProhibiciones: Long?, prohibicionDePaquete: Prohibicion.DePaquete) :
            this(
                    null,
                    LibroDAO(id = idLibroDeProhibiciones),
                    null,
                    PaqueteDAO(id = prohibicionDePaquete.id),
                    Tipo.PAQUETE
                )

    fun convertirAProhibicionDeFondo(): Prohibicion.DeFondo?
    {
        return if (tipo == Tipo.FONDO) Prohibicion.DeFondo(fondoDAO!!.id!!) else null
    }

    fun convertirAProhibicionDePaquete(): Prohibicion.DePaquete?
    {
        return if (tipo == Tipo.PAQUETE) Prohibicion.DePaquete(paqueteDao!!.id!!) else null
    }

    internal enum class Tipo
    {
        FONDO, PAQUETE, DESCONOCIDO;
    }

    internal data class ProhibicionesConvertidas(
            val prohibicionesDeFondos: MutableSet<Prohibicion.DeFondo>,
            val prohibicionesDePaquetes: MutableSet<Prohibicion.DePaquete>
                                                )
}