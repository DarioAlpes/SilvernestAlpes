package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.excepciones.ErrorDAO
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DatabaseResultsMapper
import com.j256.ormlite.stmt.QueryBuilder
import com.j256.ormlite.stmt.SelectArg
import com.j256.ormlite.support.DatabaseResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

internal interface MapeadorResultadoORMLite<out Entidad>
{
    var columnaInicial: Int
    val numeroColumnas: Int
    fun mapearResultado(resultado: DatabaseResults): Entidad
}

internal class MapeadorResultadoORMLiteParaRango<out Entidad>(
        private val dao: Dao<Entidad, *>,
        override var columnaInicial: Int,
        override val numeroColumnas: Int)
    : MapeadorResultadoORMLite<Entidad>
{
    constructor(parametrosDao: ParametrosParaDAO<Entidad, *>, columnasActuales: Int)
            : this(parametrosDao.dao, columnasActuales, parametrosDao.numeroColumnas)

    override fun mapearResultado(resultado: DatabaseResults): Entidad
    {
        val rangoColumnas = columnaInicial until columnaInicial + numeroColumnas
        return dao.mapSelectStarRow(ResultadoBDJoin(resultado, rangoColumnas))
    }
}

internal class MapeadorResultadoParaColumnaString(override var columnaInicial: Int) : MapeadorResultadoORMLite<String>
{
    override val numeroColumnas: Int = 1
    override fun mapearResultado(resultado: DatabaseResults): String
    {
        val rangoColumnas = columnaInicial until columnaInicial + numeroColumnas
        return ResultadoBDJoin(resultado, rangoColumnas).getString(0)!!
    }
}

internal class MapeadorResultadoParaColumnaLong(override var columnaInicial: Int) : MapeadorResultadoORMLite<Long>
{
    override val numeroColumnas: Int = 1
    override fun mapearResultado(resultado: DatabaseResults): Long
    {
        val rangoColumnas = columnaInicial until columnaInicial + numeroColumnas
        return ResultadoBDJoin(resultado, rangoColumnas).getLong(0)
    }
}

internal class MapeadorResultadoORMLiteParaJoin<out EntidadOrigen, out EntidadDestino>(
        internal val mapeadorOrigen: MapeadorResultadoORMLite<EntidadOrigen>,
        private val mapeadorDestino: MapeadorResultadoORMLite<EntidadDestino>)
    : MapeadorResultadoORMLite<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino>>
{
    init
    {
        mapeadorDestino.columnaInicial = mapeadorOrigen.columnaInicial + mapeadorOrigen.numeroColumnas
    }

    override var columnaInicial: Int
        get() = mapeadorOrigen.columnaInicial
        set(value)
        {
            mapeadorOrigen.columnaInicial = value
            mapeadorDestino.columnaInicial = mapeadorOrigen.columnaInicial + mapeadorOrigen.numeroColumnas
        }

    override val numeroColumnas: Int = mapeadorOrigen.numeroColumnas + mapeadorDestino.numeroColumnas
    override fun mapearResultado(resultado: DatabaseResults): EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino>
    {
        return EntidadRelacionUnoAUno(mapeadorOrigen.mapearResultado(resultado), mapeadorDestino.mapearResultado(resultado))
    }
}

private class MapeadorResultadosPosibleNull<out Entidad>(
        private val mapeadorSinNull: MapeadorResultadoORMLite<Entidad>,
        private val transformadorEsNull: Transformador<Entidad, Boolean>
                                                        ) : MapeadorResultadoORMLite<Entidad?>
{
    override var columnaInicial: Int
        get() = mapeadorSinNull.columnaInicial
        set(value)
        {
            mapeadorSinNull.columnaInicial = value
        }
    override val numeroColumnas: Int = mapeadorSinNull.numeroColumnas

    override fun mapearResultado(resultado: DatabaseResults): Entidad?
    {
        val entidad = mapeadorSinNull.mapearResultado(resultado)
        return if (transformadorEsNull.transformar(entidad)) null else entidad
    }
}

internal data class InformacionProyeccion<out TipoEntidadProyeccion> constructor(
        val camposProyeccion: List<CampoProyeccion>,
        val mapeadorResultados: MapeadorResultadoORMLite<TipoEntidadProyeccion>
                                                                                )
{
    constructor(campoProyeccion: CampoProyeccion, mapeadorResultados: MapeadorResultadoORMLite<TipoEntidadProyeccion>)
            : this(listOf(campoProyeccion), mapeadorResultados)
}

internal fun InformacionProyeccion<*>?.joinConOtraProyeccion(proyeccionDestino: InformacionProyeccion<*>?): InformacionProyeccion<*>?
{
    return this?.let { _ ->
        proyeccionDestino?.let {
            InformacionProyeccion(
                    camposProyeccion + proyeccionDestino.camposProyeccion,
                    MapeadorResultadoORMLiteParaJoin(mapeadorResultados, proyeccionDestino.mapeadorResultados)
                                 )
        } ?: this
    } ?: proyeccionDestino
}

internal class ConstructorQueryORMLite<Entidad> private constructor(
        private val idCliente: Long,
        private val mapeadorResultados: MapeadorResultadoORMLite<Entidad>,
        private val parametrosTablasInvolucradas: List<ParametrosParaDAOEntidadDeCliente<*, *>>,
        private val queryBuilder: QueryBuilder<*, *>,
        private val informacionProyeccion: InformacionProyeccion<*>?,
        private val filtrosSQL: List<FiltroSQL>,
        private val camposOrdenamiento: List<CampoOrdenamiento>,
        private val distintos: Boolean
                                                                   )
{
    companion object
    {
        fun <EntidadOrigen, EntidadDestino> proyectarResultadosAOrigen(
                constructorOrigen: ConstructorQueryORMLite<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino>>,
                numeroTablasInvolucradas: Int)
                : ConstructorQueryORMLite<EntidadOrigen>
        {
            return ConstructorQueryORMLite(
                    constructorOrigen.idCliente,
                    (constructorOrigen.mapeadorResultados as MapeadorResultadoORMLiteParaJoin).mapeadorOrigen,
                    constructorOrigen.parametrosTablasInvolucradas.subList(0, numeroTablasInvolucradas),
                    constructorOrigen.queryBuilder,
                    constructorOrigen.informacionProyeccion,
                    constructorOrigen.filtrosSQL,
                    constructorOrigen.camposOrdenamiento,
                    constructorOrigen.distintos)
        }
    }

    constructor(idCliente: Long, parametros: ParametrosParaDAOEntidadDeCliente<Entidad, *>)
            : this(idCliente,
                   MapeadorResultadoORMLiteParaRango(parametros[idCliente], 0),
                   listOf(parametros),
                   parametros[idCliente].dao.queryBuilder(),
                   null,
                   listOf(),
                   listOf(),
                   false
                  )

    fun ordenarPorCampos(camposTabla: List<CampoOrdenamiento>): ConstructorQueryORMLite<Entidad>
    {
        return ConstructorQueryORMLite(
                idCliente,
                mapeadorResultados,
                parametrosTablasInvolucradas,
                queryBuilder,
                informacionProyeccion,
                filtrosSQL,
                camposOrdenamiento + camposTabla,
                distintos)
    }

    fun distintos(): ConstructorQueryORMLite<Entidad>
    {
        return ConstructorQueryORMLite(
                idCliente,
                mapeadorResultados,
                parametrosTablasInvolucradas,
                queryBuilder,
                informacionProyeccion,
                filtrosSQL,
                camposOrdenamiento,
                true)
    }

    fun <EntidadJoin> innerJoin(constructorDestino: ConstructorQueryORMLite<EntidadJoin>)
            : ConstructorQueryORMLite<EntidadRelacionUnoAUno<Entidad, EntidadJoin>>
    {
        return ConstructorQueryORMLite(
                idCliente,
                MapeadorResultadoORMLiteParaJoin(mapeadorResultados, constructorDestino.mapeadorResultados),
                parametrosTablasInvolucradas + constructorDestino.parametrosTablasInvolucradas,
                queryBuilder.join(constructorDestino.queryBuilder),
                informacionProyeccion.joinConOtraProyeccion(constructorDestino.informacionProyeccion),
                filtrosSQL + constructorDestino.filtrosSQL,
                camposOrdenamiento + constructorDestino.camposOrdenamiento,
                distintos || constructorDestino.distintos
                                      )
    }

    fun <EntidadJoin> innerJoinConColumnas(
            columnaIzq: String,
            columnaDer: String,
            constructorDestino: ConstructorQueryORMLite<EntidadJoin>
                                          )
            : ConstructorQueryORMLite<EntidadRelacionUnoAUno<Entidad, EntidadJoin>>
    {
        return ConstructorQueryORMLite(
                idCliente,
                MapeadorResultadoORMLiteParaJoin(mapeadorResultados, constructorDestino.mapeadorResultados),
                parametrosTablasInvolucradas + constructorDestino.parametrosTablasInvolucradas,
                queryBuilder.join(columnaIzq, columnaDer, constructorDestino.queryBuilder),
                informacionProyeccion.joinConOtraProyeccion(constructorDestino.informacionProyeccion),
                filtrosSQL + constructorDestino.filtrosSQL,
                camposOrdenamiento + constructorDestino.camposOrdenamiento,
                distintos || constructorDestino.distintos
                                      )
    }

    fun <EntidadJoin> leftJoin(
            constructorDestino: ConstructorQueryORMLite<EntidadJoin>,
            transformadorEsNull: Transformador<EntidadJoin, Boolean>
                              )
            : ConstructorQueryORMLite<EntidadRelacionUnoAUno<Entidad, EntidadJoin?>>
    {
        return ConstructorQueryORMLite(
                idCliente,
                MapeadorResultadoORMLiteParaJoin(
                        mapeadorResultados,
                        MapeadorResultadosPosibleNull(constructorDestino.mapeadorResultados, transformadorEsNull)
                                                ),
                parametrosTablasInvolucradas + constructorDestino.parametrosTablasInvolucradas,
                queryBuilder.leftJoin(constructorDestino.queryBuilder),
                informacionProyeccion.joinConOtraProyeccion(constructorDestino.informacionProyeccion),
                filtrosSQL + constructorDestino.filtrosSQL,
                camposOrdenamiento + constructorDestino.camposOrdenamiento,
                distintos || constructorDestino.distintos
                                      )
    }

    fun <EntidadJoin> leftJoinConColumnas(
            columnaIzq: String,
            columnaDer: String,
            constructorDestino: ConstructorQueryORMLite<EntidadJoin>,
            transformadorEsNull: Transformador<EntidadJoin, Boolean>
                                         )
            : ConstructorQueryORMLite<EntidadRelacionUnoAUno<Entidad, EntidadJoin?>>
    {
        return ConstructorQueryORMLite(
                idCliente,
                MapeadorResultadoORMLiteParaJoin(
                        mapeadorResultados,
                        MapeadorResultadosPosibleNull(constructorDestino.mapeadorResultados, transformadorEsNull)
                                                ),
                parametrosTablasInvolucradas + constructorDestino.parametrosTablasInvolucradas,
                queryBuilder.join(columnaIzq, columnaDer, constructorDestino.queryBuilder, QueryBuilder.JoinType.LEFT, QueryBuilder.JoinWhereOperation.AND),
                informacionProyeccion.joinConOtraProyeccion(constructorDestino.informacionProyeccion),
                filtrosSQL + constructorDestino.filtrosSQL,
                camposOrdenamiento + constructorDestino.camposOrdenamiento,
                distintos || constructorDestino.distintos
                                      )
    }

    fun <EntidadJoin> rightJoin(
            constructorDestino: ConstructorQueryORMLite<EntidadJoin>,
            transformadorEsNull: Transformador<Entidad, Boolean>
                               )
            : ConstructorQueryORMLite<EntidadRelacionUnoAUno<Entidad?, EntidadJoin>>
    {
        return ConstructorQueryORMLite(
                idCliente,
                MapeadorResultadoORMLiteParaJoin(
                        MapeadorResultadosPosibleNull(mapeadorResultados, transformadorEsNull),
                        constructorDestino.mapeadorResultados
                                                ),
                parametrosTablasInvolucradas + constructorDestino.parametrosTablasInvolucradas,
                constructorDestino.queryBuilder.leftJoin(queryBuilder),
                informacionProyeccion.joinConOtraProyeccion(constructorDestino.informacionProyeccion),
                filtrosSQL + constructorDestino.filtrosSQL,
                camposOrdenamiento + constructorDestino.camposOrdenamiento,
                distintos || constructorDestino.distintos
                                      )
    }

    fun conFiltrosSQL(nuevosFiltrosSQL: List<FiltroSQL>)
            : ConstructorQueryORMLite<Entidad>
    {
        return ConstructorQueryORMLite(
                idCliente,
                mapeadorResultados,
                parametrosTablasInvolucradas,
                queryBuilder,
                informacionProyeccion,
                filtrosSQL + nuevosFiltrosSQL,
                camposOrdenamiento,
                distintos
                                      )
    }

    fun conFiltrosSQLUsandoOr(nuevosFiltrosSQL: List<FiltroSQL>)
            : ConstructorQueryORMLite<Entidad>
    {
        return ConstructorQueryORMLite(
                idCliente,
                mapeadorResultados,
                parametrosTablasInvolucradas,
                queryBuilder,
                informacionProyeccion,
                listOf(combinarFiltrosConAnd(filtrosSQL).or(combinarFiltrosConAnd(nuevosFiltrosSQL))),
                camposOrdenamiento,
                distintos
                                      )
    }

    fun <EntidadProyectada> reemplazandoProyeccion(informacionProyeccionNueva: InformacionProyeccion<EntidadProyectada>)
            : ConstructorQueryORMLite<EntidadProyectada>
    {
        return ConstructorQueryORMLite(
                idCliente,
                informacionProyeccionNueva.mapeadorResultados,
                parametrosTablasInvolucradas,
                queryBuilder,
                informacionProyeccionNueva,
                filtrosSQL,
                camposOrdenamiento,
                distintos
                                      )
    }

    private fun combinarFiltrosConAnd(filtros: List<FiltroSQL>): FiltroSQL
    {
        return filtros.reduce { acc, filtroSiguiente -> acc.and(filtroSiguiente) }
    }

    /**
     * No llamar mútliples veces porque se agregan columnas cada vez en selectRaw
     */
    fun generarConsulta(): ConsultaParametrizada
    {
        val valorSelectRaw =
                informacionProyeccion?.camposProyeccion?.joinToString {
                    it.nombreCompleto
                }
                ?: parametrosTablasInvolucradas.joinToString {
                    ProyeccionTodos(it.nombreTabla).nombreCompleto
                }

        queryBuilder.selectRaw(valorSelectRaw)

        val parametros =
                if (filtrosSQL.isNotEmpty())
                {
                    val clausulaWhere = combinarFiltrosConAnd(filtrosSQL)

                    queryBuilder.where().raw(clausulaWhere.generarSQL().toString())

                    clausulaWhere.parametros.toList()
                }
                else
                {
                    emptyList()
                }

        if (camposOrdenamiento.isNotEmpty())
        {
            queryBuilder.orderByRaw(camposOrdenamiento.joinToString { it.nombreCompleto })
        }

        if (distintos)
        {
            queryBuilder.distinct()
        }
        println("\ngenerarConsulta: "+ConsultaParametrizada(queryBuilder.prepareStatementString(), parametros))

        return ConsultaParametrizada(queryBuilder.prepareStatementString(), parametros)
    }

    fun ejecutarQuery(): Sequence<Entidad>
    {
        val (sql, parametros) = generarConsulta()
        println("\nejecutarQuery: ")
 
        return parametrosTablasInvolucradas.first()[idCliente].dao
            .queryRaw<Entidad>(
                    sql,
                    DatabaseResultsMapper<Entidad> { mapeadorResultados.mapearResultado(it) },
                    *(parametros.map { it.toString() }.toTypedArray())
                              )
            .asSequence()
    }

    fun contar(): Long
    {
        return reemplazandoProyeccion(
                InformacionProyeccion(CampoContarTodos(), MapeadorResultadoParaColumnaLong(0))
                                     ).ejecutarQuery().first()
    }

    fun maximoComoString(columna: CampoTabla): ConstructorQueryORMLite<String>
    {
        return reemplazandoProyeccion(
                InformacionProyeccion(ProyeccionMaximo(columna), MapeadorResultadoParaColumnaString(0))
                                     )
    }

    data class ConsultaParametrizada(val sql: String, val parametros: List<SelectArg>)
}

@Suppress("unused")
interface Contable<out EntidadDeNegocio>
{
    @Throws(ErrorDAO::class)
    fun contar(idCliente: Long): Long
}

interface Listable<out EntidadDeNegocio>
{
    @Throws(ErrorDAO::class)
    fun listar(idCliente: Long): Sequence<EntidadDeNegocio>
}

internal interface ListableOrdenable<out EntidadDeNegocio> : Listable<EntidadDeNegocio>
{
    fun listarOrdenado(idCliente: Long): Sequence<EntidadDeNegocio>
}

internal interface ListableFiltrableOrdenable<out EntidadDeNegocio>
    : Listable<EntidadDeNegocio>,
      ListableOrdenable<EntidadDeNegocio>
{
    fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadDeNegocio>
}

internal interface ListableSQL<Entidad> : ListableFiltrableOrdenable<Entidad>
{
    val camposDeOrdenamiento: List<CampoTabla>
    fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<Entidad>

    fun darConstructorQueryOrdenandoPorId(idCliente: Long): ConstructorQueryORMLite<Entidad>
    {
        return darConstructorQuery(idCliente)
            .ordenarPorCampos(camposDeOrdenamiento.map { CampoOrdenamiento(it, CampoOrdenamiento.Orden.ASC) })
    }

    override fun listar(idCliente: Long): Sequence<Entidad>
    {   println("listar")
        return darConstructorQuery(idCliente).ejecutarQuery()
    }

    override fun listarOrdenado(idCliente: Long): Sequence<Entidad>
    {   println("listarOrdenado")
        return darConstructorQueryOrdenandoPorId(idCliente).ejecutarQuery()
    }
}

internal class ListableDAO<EntidadDao, TipoId>(
        nombreColumnasOrdenamiento: List<String>,
        private val parametros: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>)
    : ListableSQL<EntidadDao>, Contable<EntidadDao>
{
    override val camposDeOrdenamiento: List<CampoTabla> = nombreColumnasOrdenamiento.map { CampoTabla(parametros.nombreTabla, it) }
    override fun listar(idCliente: Long): Sequence<EntidadDao>
    {
        return parametros[idCliente].dao.queryForAll().asSequence()
    }

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<EntidadDao>
    {
        return ConstructorQueryORMLite(idCliente, parametros)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadDao>
    {
        return ListableSQLConFiltrosSQL(this, filtrosSQL)
    }

    override fun contar(idCliente: Long): Long
    {
        return parametros[idCliente].dao.queryBuilder().countOf()
    }
}

internal class ListableSQLConFiltrosSQL<EntidadDao>(
        private val listableSQL: ListableSQL<EntidadDao>,
        private val filtrosSQL: List<FiltroSQL>)
    : ListableSQL<EntidadDao>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listableSQL.camposDeOrdenamiento

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<EntidadDao>
    {
        return listableSQL.darConstructorQuery(idCliente).conFiltrosSQL(filtrosSQL)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadDao>
    {
        return ListableSQLConFiltrosSQL(listableSQL, this.filtrosSQL + filtrosSQL)
    }
}

internal class ListableInnerJoin<EntidadOrigen, EntidadDestino>(
        private val listadorEntidadOrigen: ListableSQL<EntidadOrigen>,
        private val listadorEntidadDestino: ListableSQL<EntidadDestino>)
    : ListableSQL<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino>>
{
    override val camposDeOrdenamiento: List<CampoTabla> =
            sequenceOf(listadorEntidadOrigen.camposDeOrdenamiento, listadorEntidadDestino.camposDeOrdenamiento)
                .flatten()
                .toList()

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino>>
    {
        val queryOrigen = listadorEntidadOrigen.darConstructorQuery(idCliente)
        val queryDestino = listadorEntidadDestino.darConstructorQuery(idCliente)

        return queryOrigen.innerJoin(queryDestino)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino>>
    {
        return ListableSQLConFiltrosSQL(this, filtrosSQL)
    }
}

internal class ListableLeftJoinSegunColumnas<EntidadOrigen, EntidadDestino>(
        private val listadorEntidadOrigen: ListableSQL<EntidadOrigen>,
        private val columnaOrigen: String,
        private val listadorEntidadDestino: ListableSQL<EntidadDestino>,
        private val columnaDestino: String,
        private val transformadorEsNull: Transformador<EntidadDestino, Boolean>)
    : ListableSQL<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>>
{
    override val camposDeOrdenamiento: List<CampoTabla> =
            sequenceOf(listadorEntidadOrigen.camposDeOrdenamiento, listadorEntidadDestino.camposDeOrdenamiento)
                .flatten()
                .toList()

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>>
    {
        val queryOrigen = listadorEntidadOrigen.darConstructorQuery(idCliente)
        val queryDestino = listadorEntidadDestino.darConstructorQuery(idCliente)

        return queryOrigen.leftJoinConColumnas(columnaOrigen, columnaDestino, queryDestino, transformadorEsNull)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>>
    {
        return ListableSQLConFiltrosSQL(this, filtrosSQL)
    }
}

internal class ListableLeftJoin<EntidadOrigen, EntidadDestino>(
        private val listadorEntidadOrigen: ListableSQL<EntidadOrigen>,
        private val listadorEntidadDestino: ListableSQL<EntidadDestino>,
        private val transformadorEsNull: Transformador<EntidadDestino, Boolean>)
    : ListableSQL<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>>
{
    override val camposDeOrdenamiento: List<CampoTabla> =
            sequenceOf(listadorEntidadOrigen.camposDeOrdenamiento, listadorEntidadDestino.camposDeOrdenamiento)
                .flatten()
                .toList()

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>>
    {
        val queryOrigen = listadorEntidadOrigen.darConstructorQuery(idCliente)
        val queryDestino = listadorEntidadDestino.darConstructorQuery(idCliente)

        return queryOrigen.leftJoin(queryDestino, transformadorEsNull)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>>
    {
        return ListableSQLConFiltrosSQL(this, filtrosSQL)
    }
}

internal class ListableRightJoin<EntidadOrigen, EntidadDestino>(
        private val listadorEntidadOrigen: ListableSQL<EntidadOrigen>,
        private val listadorEntidadDestino: ListableSQL<EntidadDestino>,
        private val transformadorEsNull: Transformador<EntidadOrigen, Boolean>)
    : ListableSQL<EntidadRelacionUnoAUno<EntidadOrigen?, EntidadDestino>>
{
    override val camposDeOrdenamiento: List<CampoTabla> = sequenceOf(listadorEntidadOrigen.camposDeOrdenamiento, listadorEntidadDestino.camposDeOrdenamiento).flatten().toList()

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<EntidadRelacionUnoAUno<EntidadOrigen?, EntidadDestino>>
    {
        val queryOrigen = listadorEntidadOrigen.darConstructorQuery(idCliente)
        val queryDestino = listadorEntidadDestino.darConstructorQuery(idCliente)

        return queryOrigen.rightJoin(queryDestino, transformadorEsNull)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadRelacionUnoAUno<EntidadOrigen?, EntidadDestino>>
    {
        return ListableSQLConFiltrosSQL(this, filtrosSQL)
    }
}

internal class ListableProyectandoAColumnaString<EntidadOrigen>(
        private val listadorOrigen: ListableSQL<EntidadOrigen>,
        private val campoTablaColumnaString: CampoTabla)
    : ListableSQL<String>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listadorOrigen.camposDeOrdenamiento

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<String>
    {
        return ListableSQLConFiltrosSQL(this, filtrosSQL)
    }

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<String>
    {
        return listadorOrigen.darConstructorQuery(idCliente).reemplazandoProyeccion(
                InformacionProyeccion(listOf(campoTablaColumnaString), MapeadorResultadoParaColumnaString(0))
                                                                                   )
    }
}

internal class ListableIgnorandoEntidadDestino<EntidadOrigen, EntidadDestino>(
        private val listadorOrigen: ListableSQL<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino>>,
        private val numeroTablasOrigen: Int)
    : ListableSQL<EntidadOrigen>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listadorOrigen.camposDeOrdenamiento.take(numeroTablasOrigen)

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<EntidadOrigen>
    {
        return ConstructorQueryORMLite.proyectarResultadosAOrigen(listadorOrigen.darConstructorQuery(idCliente), numeroTablasOrigen)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadOrigen>
    {
        return ListableIgnorandoEntidadDestino(ListableSQLConFiltrosSQL(listadorOrigen, filtrosSQL), numeroTablasOrigen)
    }
}

internal class ListableDistintos<Entidad>(
        private val listadorOrigen: ListableSQL<Entidad>)
    : ListableSQL<Entidad>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listadorOrigen.camposDeOrdenamiento

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<Entidad>
    {
        return listadorOrigen.darConstructorQuery(idCliente).distintos()
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<Entidad>
    {
        return ListableDistintos(ListableSQLConFiltrosSQL(listadorOrigen, filtrosSQL))
    }
}

internal class ListableInSubquery<Entidad>(
        private val listableEntidad: ListableSQL<Entidad>,
        private val listadorSubquery: ListableSQL<*>,
        private val campoTablaFiltroIn: CampoTabla)
    : ListableSQL<Entidad>
{

    override val camposDeOrdenamiento: List<CampoTabla> = listableEntidad.camposDeOrdenamiento

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<Entidad>
    {
        return ListableSQLConFiltrosSQL(this, filtrosSQL)
    }

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<Entidad>
    {
        return listableEntidad.darConstructorQuery(idCliente).conFiltrosSQL(
                listOf(FiltroInSubQuery(campoTablaFiltroIn, listadorSubquery.darConstructorQuery(idCliente)))
                                                                           )
    }
}

internal class ContableSegunContableEntidadDAOPrincipal<out EntidadNegocio, out EntidadDAO>(
        private val contablePrincipal: Contable<EntidadDAO>)
    : Contable<EntidadNegocio>
{
    override fun contar(idCliente: Long): Long
    {
        return contablePrincipal.contar(idCliente)
    }
}

internal class ListableConTransformacion<in EntidadOrigen, out EntidadDestino>(
        private val listableEntidadDestino: ListableFiltrableOrdenable<EntidadOrigen>,
        private val transformador: TransformadorEntidadCliente<EntidadOrigen, EntidadDestino>)
    : ListableFiltrableOrdenable<EntidadDestino>
{
    override fun listarOrdenado(idCliente: Long): Sequence<EntidadDestino>
    {
        return listableEntidadDestino.listarOrdenado(idCliente).map { transformador.transformar(idCliente, it) }
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadDestino>
    {
        return ListableConTransformacion(listableEntidadDestino.conFiltrosSQL(filtrosSQL), transformador)
    }

    override fun listar(idCliente: Long): Sequence<EntidadDestino>
    {
        return listableEntidadDestino.listar(idCliente).map { transformador.transformar(idCliente, it) }
    }
}

internal class ListableUnoAMuchos<EntidadOrigen, EntidadDestino : Any, out TipoIdOrigen>(
        private val listadorJoin: ListableFiltrableOrdenable<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>>,
        private val extractorIdOrigen: Transformador<EntidadOrigen, TipoIdOrigen>)
    : ListableFiltrableOrdenable<EntidadRelacionUnoAMuchos<EntidadOrigen, EntidadDestino>>
{
    override fun listarOrdenado(idCliente: Long): Sequence<EntidadRelacionUnoAMuchos<EntidadOrigen, EntidadDestino>>
    {
        return listar(idCliente)
    }

    override fun listar(idCliente: Long): Sequence<EntidadRelacionUnoAMuchos<EntidadOrigen, EntidadDestino>>
    {
        return listadorJoin
            .listarOrdenado(idCliente)
            .agruparSecuenciaOrdenadaSegunTransformacion(
                    object : Transformador<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>, EntidadOrigen>
                    {
                        override fun transformar(origen: EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>): EntidadOrigen
                        {
                            return origen.entidadOrigen
                        }
                    },
                    extractorIdOrigen)
            .map { EntidadRelacionUnoAMuchos(it.first, it.second.mapNotNull { it.entidadDestino }) }
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadRelacionUnoAMuchos<EntidadOrigen, EntidadDestino>>
    {
        return ListableUnoAMuchos(listadorJoin.conFiltrosSQL(filtrosSQL), extractorIdOrigen)
    }
}

internal typealias AgrupacionDosResultados<OrigenIzquierda, DestinoIzquierda, OrigenDerecha, DestinoDerecha> =
        EntidadRelacionUnoAUno<
                EntidadRelacionUnoAUno<OrigenIzquierda, DestinoIzquierda>?,
                EntidadRelacionUnoAUno<OrigenDerecha, DestinoDerecha>?
                >

internal class ListableAgrupandoConsultas<out OrigenIzquierda, out DestinoIzquierda, out OrigenDerecha, out DestinoDerecha>(
        private val listadorIzquierda: ListableFiltrableOrdenable<EntidadRelacionUnoAUno<OrigenIzquierda, DestinoIzquierda>>,
        private val listadorDerecha: ListableFiltrableOrdenable<EntidadRelacionUnoAUno<OrigenDerecha, DestinoDerecha>>,
        private val comparadorEntidades: ComparadorEntidades<EntidadRelacionUnoAUno<OrigenIzquierda, DestinoIzquierda>, EntidadRelacionUnoAUno<OrigenDerecha, DestinoDerecha>>)
    : ListableFiltrableOrdenable<AgrupacionDosResultados<OrigenIzquierda, DestinoIzquierda, OrigenDerecha, DestinoDerecha>>
{
    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>)
            : ListableFiltrableOrdenable<AgrupacionDosResultados<OrigenIzquierda, DestinoIzquierda, OrigenDerecha, DestinoDerecha>>
    {
        return ListableAgrupandoConsultas(
                listadorIzquierda.conFiltrosSQL(filtrosSQL),
                listadorDerecha.conFiltrosSQL(filtrosSQL),
                comparadorEntidades
                                         )
    }

    override fun listar(idCliente: Long): Sequence<AgrupacionDosResultados<OrigenIzquierda, DestinoIzquierda, OrigenDerecha, DestinoDerecha>>
    {   
        return runBlocking {
            val resultadoIzquierda = async(coroutineContext + Dispatchers.IO) {
                listadorIzquierda
                    .listarOrdenado(idCliente)
                    .map { relacion ->
                        ElementoDeAgrupacion(relacion, { it })
                    }
            }
            val resultadoDerecha = async(coroutineContext + Dispatchers.IO) {
                listadorDerecha
                    .listarOrdenado(idCliente)
                    .map { relacion ->
                        ElementoDeAgrupacion(relacion, { it })
                    }
            }

            SecuenciaAgrupadoraDeSecuenciasOrdenadas(
                    resultadoIzquierda.await(),
                    resultadoDerecha.await(),
                    comparadorEntidades
                                                    )
                .map {
                    EntidadRelacionUnoAUno(
                            it.first?.run { EntidadRelacionUnoAUno(elemento.entidadOrigen, elemento.entidadDestino) },
                            it.second?.run { EntidadRelacionUnoAUno(elemento.entidadOrigen, elemento.entidadDestino) }
                                          )
                }
        }
    }

    override fun listarOrdenado(idCliente: Long)
            : Sequence<AgrupacionDosResultados<OrigenIzquierda, DestinoIzquierda, OrigenDerecha, DestinoDerecha>>
    {
        return listar(idCliente)
    }
}

internal class ListableConTransaccion<out Entidad>
(
        private val configuracion: ConfiguracionRepositorios,
        private val nombreEntidad: String,
        private val listador: ListableFiltrableOrdenable<Entidad>
) : ListableFiltrableOrdenable<Entidad>
{
    override fun listarOrdenado(idCliente: Long): Sequence<Entidad>
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaConsulta(configuracion, idCliente, nombreEntidad) {
            listador.listarOrdenado(idCliente).toList().asSequence() // Hay que consumir a memoria resultado completo antes de terminar transaccion o se cierra el cursor
        }
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<Entidad>
    {
        return ListableConTransaccion(configuracion, nombreEntidad, listador.conFiltrosSQL(filtrosSQL))
    }

    override fun listar(idCliente: Long): Sequence<Entidad>
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaConsulta(configuracion, idCliente, nombreEntidad) {
            listador.listar(idCliente).toList().asSequence() // Hay que consumir a memoria resultado completo antes de terminar transaccion o se cierra el cursor
        }
    }
}