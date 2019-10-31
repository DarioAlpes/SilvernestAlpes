package co.smartobjects.persistencia.basederepositorios

import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.excepciones.ErrorDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking


interface ParametrosConsulta
{
    val filtrosSQL: List<FiltroSQL>
}

class UnitParametrosConsulta private constructor() : ParametrosConsulta
{
    companion object
    {
        @JvmField
        val INSTANCIA = UnitParametrosConsulta()
    }

    override val filtrosSQL: List<FiltroSQL> = listOf()
}

interface ListableConParametros<out EntidadDeNegocio, in Parametros : ParametrosConsulta>
{
    @Throws(ErrorDAO::class)
    fun listarSegunParametros(idCliente: Long, parametros: Parametros): Sequence<EntidadDeNegocio>
}

internal interface ListableConParametrosFiltrableOrdenable<out EntidadDeNegocio, in Parametros : ParametrosConsulta>
    : ListableConParametros<EntidadDeNegocio, Parametros>
{
    fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<EntidadDeNegocio, Parametros>
    fun listarOrdenado(idCliente: Long, parametros: Parametros): Sequence<EntidadDeNegocio>
}

internal class ListableConParametrosFiltrableOrdenableSQL<EntidadDeNegocio, in Parametros : ParametrosConsulta>
(
        private val listableSQL: ListableSQL<EntidadDeNegocio>,
        private val camposDeOrdenamiento: List<CampoOrdenamiento>
) : ListableConParametrosFiltrableOrdenable<EntidadDeNegocio, Parametros>
{
    private fun darConstructorQueryOrdenandoPorParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<EntidadDeNegocio>
    {
        return listableSQL.darConstructorQuery(idCliente).conFiltrosSQL(parametros.filtrosSQL).ordenarPorCampos(camposDeOrdenamiento)
    }

    override fun listarSegunParametros(idCliente: Long, parametros: Parametros): Sequence<EntidadDeNegocio>
    {   
        return darConstructorQueryOrdenandoPorParametros(idCliente, parametros).ejecutarQuery()
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<EntidadDeNegocio, Parametros>
    {
        return ListableConParametrosFiltrableOrdenableSQL(ListableSQLConFiltrosSQL(listableSQL, filtrosSQL), camposDeOrdenamiento)
    }

    override fun listarOrdenado(idCliente: Long, parametros: Parametros): Sequence<EntidadDeNegocio>
    {   print("listarOrdenado")
        return darConstructorQueryOrdenandoPorParametros(idCliente, parametros).ejecutarQuery()
    }
}

internal interface ListableConParametrosSQL<EntidadDeNegocio, in Parametros : ParametrosConsulta>
    : ListableConParametrosFiltrableOrdenable<EntidadDeNegocio, Parametros>
{
    val camposDeOrdenamiento: List<CampoTabla>
    fun darConstructorQueryConParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<EntidadDeNegocio>

    fun darConstructorQueryOrdenandoPorId(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<EntidadDeNegocio>
    {
        return darConstructorQueryConParametros(idCliente, parametros)
            .ordenarPorCampos(camposDeOrdenamiento.map { CampoOrdenamiento(it, CampoOrdenamiento.Orden.ASC) })
    }

    override fun listarSegunParametros(idCliente: Long, parametros: Parametros): Sequence<EntidadDeNegocio>
    {
        return darConstructorQueryConParametros(idCliente, parametros).ejecutarQuery()
    }

    override fun listarOrdenado(idCliente: Long, parametros: Parametros): Sequence<EntidadDeNegocio>
    {
        return darConstructorQueryOrdenandoPorId(idCliente, parametros).ejecutarQuery()
    }
}

internal class ListableConParametrosSegunListableSQL<EntidadDeNegocio, in Parametros : ParametrosConsulta>(
        private val listableSQL: ListableSQL<EntidadDeNegocio>)
    : ListableConParametrosSQL<EntidadDeNegocio, Parametros>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listableSQL.camposDeOrdenamiento

    override fun darConstructorQueryConParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<EntidadDeNegocio>
    {
        return listableSQL.darConstructorQuery(idCliente).conFiltrosSQL(parametros.filtrosSQL)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<EntidadDeNegocio, Parametros>
    {
        return ListableConParametrosSegunListableSQL(ListableSQLConFiltrosSQL(listableSQL, filtrosSQL))
    }
}

@Suppress("unused")
interface ContableConParametros<out EntidadDeNegocio, in Parametros : ParametrosConsulta>
{
    @Throws(ErrorDAO::class)
    fun contar(idCliente: Long, parametros: Parametros): Long
}

internal class ContableConParametrosSegunListableConParametrosSQL<EntidadNegocio, in Parametros : ParametrosConsulta>(
        val listable: ListableConParametrosSQL<EntidadNegocio, Parametros>)
    : ContableConParametros<EntidadNegocio, Parametros>
{
    override fun contar(idCliente: Long, parametros: Parametros): Long
    {
        return listable.darConstructorQueryConParametros(idCliente, parametros).contar()
    }
}

internal class ContableConParametrosSegunContableEntidadPrincipal<out EntidadNegocio, out EntidadDAO, in Parametros : ParametrosConsulta>(
        private val contablePrincipal: ContableConParametros<EntidadDAO, Parametros>)
    : ContableConParametros<EntidadNegocio, Parametros>
{
    override fun contar(idCliente: Long, parametros: Parametros): Long
    {
        return contablePrincipal.contar(idCliente, parametros)
    }
}


internal class MaximoStringConParametrosSegunListableConParametrosSQL<EntidadNegocio, in Parametros : ParametrosConsulta>
(
        private val listableDelMaximo: ListableConParametrosSQL<EntidadNegocio, Parametros>,
        private val columnaParaMaximo: CampoTabla
) : ListableConParametrosSQL<String, Parametros>
{
    override val camposDeOrdenamiento = listableDelMaximo.camposDeOrdenamiento

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<String, Parametros>
    {
        return ListableProyectandoAColumnaStringConParametros(listableDelMaximo, columnaParaMaximo).conFiltrosSQL(filtrosSQL)
    }

    override fun darConstructorQueryConParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<String>
    {
        return listableDelMaximo.darConstructorQueryConParametros(idCliente, parametros).maximoComoString(columnaParaMaximo)
    }
}


internal class ListableConParametrosDAO<EntidadDao, TipoId, in Parametros : ParametrosConsulta>(
        nombresCamposIds: List<String>,
        private val parametrosDAO: ParametrosParaDAOEntidadDeCliente<EntidadDao, TipoId>)
    : ListableConParametrosSQL<EntidadDao, Parametros>, Contable<EntidadDao>
{
    override val camposDeOrdenamiento: List<CampoTabla> = nombresCamposIds.map { CampoTabla(parametrosDAO.nombreTabla, it) }

    override fun darConstructorQueryConParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<EntidadDao>
    {
        return ConstructorQueryORMLite(idCliente, parametrosDAO).conFiltrosSQL(parametros.filtrosSQL)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<EntidadDao, Parametros>
    {
        return ListableSQLConParametrosConFiltrosSQL(this, filtrosSQL)
    }

    override fun contar(idCliente: Long): Long
    {
        return parametrosDAO[idCliente].dao.queryBuilder().countOf()
    }
}

internal class ListableSegunListableConParametrosUnit<out Entidad>(
        private val listableConParametrosConsulta: ListableConParametrosFiltrableOrdenable<Entidad, UnitParametrosConsulta>)
    : ListableFiltrableOrdenable<Entidad>
{
    override fun listar(idCliente: Long): Sequence<Entidad>
    {
        return listableConParametrosConsulta.listarSegunParametros(idCliente, UnitParametrosConsulta.INSTANCIA)
    }

    override fun listarOrdenado(idCliente: Long): Sequence<Entidad>
    {
        return listableConParametrosConsulta.listarOrdenado(idCliente, UnitParametrosConsulta.INSTANCIA)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<Entidad>
    {
        return ListableSegunListableConParametrosUnit(listableConParametrosConsulta.conFiltrosSQL(filtrosSQL))
    }
}

internal class ListableConParametrosUnitSegunListable<out Entidad>(
        private val listableConParametros: ListableFiltrableOrdenable<Entidad>)
    : ListableConParametrosFiltrableOrdenable<Entidad, UnitParametrosConsulta>
{
    override fun listarSegunParametros(idCliente: Long, parametros: UnitParametrosConsulta): Sequence<Entidad>
    {
        return listableConParametros.listar(idCliente)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<Entidad, UnitParametrosConsulta>
    {
        return ListableConParametrosUnitSegunListable(listableConParametros.conFiltrosSQL(filtrosSQL))
    }

    override fun listarOrdenado(idCliente: Long, parametros: UnitParametrosConsulta): Sequence<Entidad>
    {
        return listableConParametros.listarOrdenado(idCliente)
    }
}

internal class ListableSQLConParametrosConFiltrosSQL<EntidadDao, in Parametros : ParametrosConsulta>
(
        private val listableSQL: ListableConParametrosSQL<EntidadDao, Parametros>,
        private val filtrosSQL: List<FiltroSQL>
) : ListableConParametrosSQL<EntidadDao, Parametros>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listableSQL.camposDeOrdenamiento

    override fun darConstructorQueryConParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<EntidadDao>
    {
        return listableSQL.darConstructorQueryConParametros(idCliente, parametros).conFiltrosSQL(filtrosSQL)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<EntidadDao, Parametros>
    {
        return ListableSQLConParametrosConFiltrosSQL(listableSQL, this.filtrosSQL + filtrosSQL)
    }
}

internal class ListableSQLConParametrosInyectadosUsandoOr<EntidadDao, in Parametros : ParametrosConsulta>
(
        private val listableSQL: ListableConParametrosSQL<EntidadDao, Parametros>
) : ListableConParametrosSQL<EntidadDao, Parametros>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listableSQL.camposDeOrdenamiento

    override fun darConstructorQueryConParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<EntidadDao>
    {
        return listableSQL
            .darConstructorQueryConParametros(idCliente, parametros)
            .conFiltrosSQLUsandoOr(parametros.filtrosSQL)
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<EntidadDao, Parametros>
    {
        return ListableSQLConParametrosInyectadosUsandoOr(listableSQL).conFiltrosSQL(filtrosSQL)
    }
}

internal class ListableSimple<out EntidadDeNegocio, in EntidadDao : EntidadDAO<EntidadDeNegocio>, TipoIdDao>(
        listableConTransformacion: ListableConTransformacion<EntidadDao, EntidadDeNegocio>)
    : ListableFiltrableOrdenable<EntidadDeNegocio> by listableConTransformacion
{
    constructor(listableDAO: ListableDAO<EntidadDao, TipoIdDao>) :
            this(ListableConTransformacion(listableDAO, TransformadorEntidadDao()))
}

internal class ListableConParametrosSimple<out EntidadDeNegocio, in EntidadDao : EntidadDAO<EntidadDeNegocio>, TipoIdDao, in Parametros : ParametrosConsulta> private constructor(
        listableConTransformacion: ListableConTransformacionYParametros<EntidadDao, EntidadDeNegocio, Parametros>)
    : ListableConParametrosFiltrableOrdenable<EntidadDeNegocio, Parametros> by listableConTransformacion
{
    constructor(listableDAO: ListableConParametrosDAO<EntidadDao, TipoIdDao, Parametros>) :
            this(ListableConTransformacionYParametros(listableDAO, TransformadorEntidadDao()))
}

internal class ListableConTransformacionYParametros<in EntidadOrigen, out EntidadDestino, in Parametros : ParametrosConsulta>(
        private val listableEntidadDestino: ListableConParametrosFiltrableOrdenable<EntidadOrigen, Parametros>,
        private val transformador: TransformadorEntidadCliente<EntidadOrigen, EntidadDestino>)
    : ListableConParametrosFiltrableOrdenable<EntidadDestino, Parametros>
{
    override fun listarOrdenado(idCliente: Long, parametros: Parametros): Sequence<EntidadDestino>
    {
        return listableEntidadDestino.listarOrdenado(idCliente, parametros).map { transformador.transformar(idCliente, it) }
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<EntidadDestino, Parametros>
    {
        return ListableConTransformacionYParametros(listableEntidadDestino.conFiltrosSQL(filtrosSQL), transformador)
    }

    override fun listarSegunParametros(idCliente: Long, parametros: Parametros): Sequence<EntidadDestino>
    {   print("\n5\n "+parametros.toString())
        return listableEntidadDestino.listarSegunParametros(idCliente, parametros).map { transformador.transformar(idCliente, it) }
    }
}

internal class ListableUnoAMuchosConParametros<EntidadOrigen, EntidadDestino : Any, out TipoIdOrigen, in Parametros : ParametrosConsulta>
(
        private val listadorJoin: ListableConParametrosFiltrableOrdenable<EntidadRelacionUnoAUno<EntidadOrigen, EntidadDestino?>, Parametros>,
        private val extractorIdOrigen: Transformador<EntidadOrigen, TipoIdOrigen>
) : ListableConParametrosFiltrableOrdenable<EntidadRelacionUnoAMuchos<EntidadOrigen, EntidadDestino>, Parametros>
{
    override fun listarOrdenado(idCliente: Long, parametros: Parametros): Sequence<EntidadRelacionUnoAMuchos<EntidadOrigen, EntidadDestino>>
    {   print("6")
        return listarSegunParametros(idCliente, parametros)
    }

    override fun listarSegunParametros(idCliente: Long, parametros: Parametros): Sequence<EntidadRelacionUnoAMuchos<EntidadOrigen, EntidadDestino>>
    {   print("7")
        return listadorJoin
            .listarOrdenado(idCliente, parametros)
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

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<EntidadRelacionUnoAMuchos<EntidadOrigen, EntidadDestino>, Parametros>
    {
        return ListableUnoAMuchosConParametros(listadorJoin.conFiltrosSQL(filtrosSQL), extractorIdOrigen)
    }
}

internal class ListableInSubqueryConParametrosEnSubquery<Entidad, in Parametros : ParametrosConsulta>(
        private val listableEntidad: ListableSQL<Entidad>,
        private val listadorSubquery: ListableConParametrosSQL<*, Parametros>,
        private val campoTablaFiltroIn: CampoTabla)
    : ListableConParametrosSQL<Entidad, Parametros>
{

    override val camposDeOrdenamiento: List<CampoTabla> = listableEntidad.camposDeOrdenamiento
    override fun darConstructorQueryConParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<Entidad>
    {
        val filtroDeSubquery =
                FiltroInSubQuery(campoTablaFiltroIn, listadorSubquery.darConstructorQueryConParametros(idCliente, parametros))

        return listableEntidad.darConstructorQuery(idCliente).conFiltrosSQL(listOf(filtroDeSubquery))
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<Entidad, Parametros>
    {
        return ListableInSubqueryConParametrosEnSubquery(
                ListableSQLConFiltrosSQL(listableEntidad, filtrosSQL),
                listadorSubquery,
                campoTablaFiltroIn
                                                        )
    }
}

internal class ListableAgrupandoConsultasConParametros<out OrigenIzquierda, out DestinoIzquierda, out OrigenDerecha, out DestinoDerecha, in Parametros : ParametrosConsulta>
(
        private val listadorIzquierda: ListableConParametrosFiltrableOrdenable<EntidadRelacionUnoAUno<OrigenIzquierda, DestinoIzquierda>, Parametros>,
        private val listadorDerecha: ListableConParametrosFiltrableOrdenable<EntidadRelacionUnoAUno<OrigenDerecha, DestinoDerecha>, Parametros>,
        private val comparadorEntidades: ComparadorEntidades<EntidadRelacionUnoAUno<OrigenIzquierda, DestinoIzquierda>, EntidadRelacionUnoAUno<OrigenDerecha, DestinoDerecha>>
) : ListableConParametrosFiltrableOrdenable<AgrupacionDosResultados<OrigenIzquierda, DestinoIzquierda, OrigenDerecha, DestinoDerecha>, Parametros>
{
    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>)
            : ListableConParametrosFiltrableOrdenable<AgrupacionDosResultados<OrigenIzquierda, DestinoIzquierda, OrigenDerecha, DestinoDerecha>, Parametros>
    {
        return ListableAgrupandoConsultasConParametros(
                listadorIzquierda.conFiltrosSQL(filtrosSQL),
                listadorDerecha.conFiltrosSQL(filtrosSQL),
                comparadorEntidades
                                                      )
    }

    override fun listarSegunParametros(idCliente: Long, parametros: Parametros): Sequence<AgrupacionDosResultados<OrigenIzquierda, DestinoIzquierda, OrigenDerecha, DestinoDerecha>>
    {   print("8")
        return runBlocking {
            val resultadoIzquierda = async(coroutineContext + Dispatchers.IO) {
                listadorIzquierda
                    .listarOrdenado(idCliente, parametros)
                    .map { relacion ->
                        ElementoDeAgrupacion(relacion) { it }
                    }
            }
            val resultadoDerecha = async(coroutineContext + Dispatchers.IO) {
                listadorDerecha
                    .listarOrdenado(idCliente, parametros)
                    .map { relacion ->
                        ElementoDeAgrupacion(relacion) { it }
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

    override fun listarOrdenado(idCliente: Long, parametros: Parametros)
            : Sequence<AgrupacionDosResultados<OrigenIzquierda, DestinoIzquierda, OrigenDerecha, DestinoDerecha>>
    {
        return listarSegunParametros(idCliente, parametros)
    }
}

internal class ListableProyectandoAColumnaStringConParametros<EntidadOrigen, in Parametros : ParametrosConsulta>(
        private val listadorOrigen: ListableConParametrosSQL<EntidadOrigen, Parametros>,
        private val campoTablaColumnaString: CampoTabla)
    : ListableConParametrosSQL<String, Parametros>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listadorOrigen.camposDeOrdenamiento

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<String, Parametros>
    {
        return ListableSQLConParametrosConFiltrosSQL(this, filtrosSQL)
    }

    override fun darConstructorQueryConParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<String>
    {
        return listadorOrigen.darConstructorQueryConParametros(idCliente, parametros).reemplazandoProyeccion(
                InformacionProyeccion(listOf(campoTablaColumnaString), MapeadorResultadoParaColumnaString(0))
                                                                                                            )
    }
}

internal class ListableProyectandoAColumnaLongConParametros<EntidadOrigen, in Parametros : ParametrosConsulta>(
        private val listadorOrigen: ListableConParametrosSQL<EntidadOrigen, Parametros>,
        private val campoTablaColumnaLong: CampoTabla)
    : ListableConParametrosSQL<Long, Parametros>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listadorOrigen.camposDeOrdenamiento

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<Long, Parametros>
    {
        return ListableSQLConParametrosConFiltrosSQL(this, filtrosSQL)
    }

    override fun darConstructorQueryConParametros(idCliente: Long, parametros: Parametros): ConstructorQueryORMLite<Long>
    {
        return listadorOrigen.darConstructorQueryConParametros(idCliente, parametros).reemplazandoProyeccion(
                InformacionProyeccion(listOf(campoTablaColumnaLong), MapeadorResultadoParaColumnaLong(0))
                                                                                                            )
    }
}

internal class ListableConTransaccionYParametros<out Entidad, in Parametros : ParametrosConsulta>
(
        private val configuracion: ConfiguracionRepositorios,
        private val nombreEntidad: String,
        private val listador: ListableConParametrosFiltrableOrdenable<Entidad, Parametros>
) : ListableConParametrosFiltrableOrdenable<Entidad, Parametros>
{
    override fun listarOrdenado(idCliente: Long, parametros: Parametros): Sequence<Entidad>
    {
        return transaccionEnEsquemaClienteYProcesarErroresParaConsulta(configuracion, idCliente, nombreEntidad) {
            listador.listarOrdenado(idCliente, parametros).toList().asSequence() // Hay que consumir a memoria resultado completo antes de terminar transaccion o se cierra el cursor
        }
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<Entidad, Parametros>
    {
        return ListableConTransaccionYParametros(configuracion, nombreEntidad, listador.conFiltrosSQL(filtrosSQL))
    }

    override fun listarSegunParametros(idCliente: Long, parametros: Parametros): Sequence<Entidad>
    {   print("\n9 "+parametros.toString()+"\n")
        return transaccionEnEsquemaClienteYProcesarErroresParaConsulta(configuracion, idCliente, nombreEntidad) {
            listador.listarSegunParametros(idCliente, parametros).toList().asSequence() // Hay que consumir a memoria resultado completo antes de terminar transaccion o se cierra el cursor
        }
    }
}