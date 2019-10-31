package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroSegunReglas
import co.smartobjects.entidades.fondos.libros.ReglaDeIdGrupoDeClientes
import co.smartobjects.entidades.fondos.libros.ReglaDeIdPaquete
import co.smartobjects.entidades.fondos.libros.ReglaDeIdUbicacion
import co.smartobjects.persistencia.fondos.paquete.PaqueteDAO
import co.smartobjects.persistencia.fondos.precios.gruposclientes.GrupoClientesDAO
import co.smartobjects.persistencia.ubicaciones.UbicacionDAO
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable


@DatabaseTable(tableName = ReglaDAO.TABLA)
internal data class ReglaDAO(
        @DatabaseField(columnName = COLUMNA_ID, generatedId = true)
        val id: Long? = null,

        @DatabaseField(columnName = COLUMNA_ID_LIBRO_SEGUN_REGLAS, foreign = true, uniqueCombo = true, canBeNull = false, columnDefinition = "BIGINT NOT NULL REFERENCES ${LibroSegunReglasDAO.TABLA}(${LibroSegunReglasDAO.COLUMNA_ID}) ON DELETE CASCADE")
        val libroSegunReglas: LibroSegunReglasDAO = LibroSegunReglasDAO(),

        @DatabaseField(columnName = COLUMNA_ID_UBICACION, foreign = true, uniqueCombo = true, columnDefinition = "BIGINT NULL REFERENCES ${UbicacionDAO.TABLA}(${UbicacionDAO.COLUMNA_ID})")
        val ubicacion: UbicacionDAO? = null,

        @DatabaseField(columnName = COLUMNA_ID_GRUPO_CLIENTES, foreign = true, uniqueCombo = true, columnDefinition = "BIGINT NULL REFERENCES ${GrupoClientesDAO.TABLA}(${GrupoClientesDAO.COLUMNA_ID})")
        val grupo: GrupoClientesDAO? = null,

        @DatabaseField(columnName = COLUMNA_ID_PAQUETE, foreign = true, uniqueCombo = true, columnDefinition = "BIGINT NULL REFERENCES ${PaqueteDAO.TABLA}(${PaqueteDAO.COLUMNA_ID})")
        val paquete: PaqueteDAO? = null,

        @DatabaseField(columnName = COLUMNA_TIPO, uniqueCombo = true, canBeNull = false)
        val tipo: Tipo = Tipo.DESCONOCIDO
                            )
{
    companion object
    {
        const val TABLA = "regla"
        const val COLUMNA_ID = "id_regla"
        const val COLUMNA_TIPO = "tipo"

        const val COLUMNA_ID_LIBRO_SEGUN_REGLAS = "fk_${TABLA}_${LibroSegunReglasDAO.TABLA}"
        const val COLUMNA_ID_UBICACION = "fk_${TABLA}_${UbicacionDAO.TABLA}"
        const val COLUMNA_ID_GRUPO_CLIENTES = "fk_${TABLA}_${GrupoClientesDAO.TABLA}"
        const val COLUMNA_ID_PAQUETE = "fk_${TABLA}_${PaqueteDAO.TABLA}"

        fun convertirAEntidadesDeNegocio(reglasDAO: List<ReglaDAO>): ReglasConvertidas
        {
            val reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion> = mutableSetOf()
            val reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes> = mutableSetOf()
            val reglasIdPaquete: MutableSet<ReglaDeIdPaquete> = mutableSetOf()

            for (reglaDAO in reglasDAO)
            {
                when (reglaDAO.tipo)
                {
                    Tipo.ID_UBICACION         ->
                    {
                        reglasIdUbicacion.add(reglaDAO.convertirAReglaDeIdUbicacion()!!)
                    }
                    Tipo.ID_GRUPO_DE_CLIENTES ->
                    {
                        reglasIdGrupoDeClientes.add(reglaDAO.convertirAReglaDeIdGrupoClientes()!!)
                    }
                    Tipo.ID_PAQUETE           ->
                    {
                        reglasIdPaquete.add(reglaDAO.convertirAReglaDeIdPaquete()!!)
                    }
                    Tipo.DESCONOCIDO          ->
                    {
                        throw RuntimeException("Existe una regla en base de datos con tipo desconocido")
                    }
                }
            }

            return ReglasConvertidas(reglasIdUbicacion, reglasIdGrupoDeClientes, reglasIdPaquete)
        }
    }

    constructor(libroSegunReglas: LibroSegunReglas, reglaDeIdUbicacion: ReglaDeIdUbicacion) :
            this(
                    null,
                    LibroSegunReglasDAO(id = libroSegunReglas.id),
                    UbicacionDAO(id = reglaDeIdUbicacion.restriccion),
                    null,
                    null,
                    Tipo.ID_UBICACION
                )

    constructor(libroSegunReglas: LibroSegunReglas, reglaDeIdGrupoDeClientes: ReglaDeIdGrupoDeClientes) :
            this(
                    null,
                    LibroSegunReglasDAO(id = libroSegunReglas.id),
                    null,
                    GrupoClientesDAO(id = reglaDeIdGrupoDeClientes.restriccion),
                    null,
                    Tipo.ID_GRUPO_DE_CLIENTES
                )

    constructor(libroSegunReglas: LibroSegunReglas, reglaDeIdPaquete: ReglaDeIdPaquete) :
            this(
                    null,
                    LibroSegunReglasDAO(id = libroSegunReglas.id),
                    null,
                    null,
                    PaqueteDAO(id = reglaDeIdPaquete.restriccion),
                    Tipo.ID_PAQUETE
                )

    fun convertirAReglaDeIdUbicacion(): ReglaDeIdUbicacion?
    {
        return if (tipo == Tipo.ID_UBICACION) ReglaDeIdUbicacion(ubicacion!!.id!!) else null
    }

    fun convertirAReglaDeIdGrupoClientes(): ReglaDeIdGrupoDeClientes?
    {
        return if (tipo == Tipo.ID_GRUPO_DE_CLIENTES) ReglaDeIdGrupoDeClientes(grupo!!.id!!) else null
    }

    fun convertirAReglaDeIdPaquete(): ReglaDeIdPaquete?
    {
        return if (tipo == Tipo.ID_PAQUETE) ReglaDeIdPaquete(paquete!!.id!!) else null
    }

    internal enum class Tipo
    {
        ID_UBICACION, ID_GRUPO_DE_CLIENTES, ID_PAQUETE, DESCONOCIDO;
    }

    internal data class ReglasConvertidas(
            val reglasIdUbicacion: MutableSet<ReglaDeIdUbicacion>,
            val reglasIdGrupoDeClientes: MutableSet<ReglaDeIdGrupoDeClientes>,
            val reglasIdPaquete: MutableSet<ReglaDeIdPaquete>
                                         )
}