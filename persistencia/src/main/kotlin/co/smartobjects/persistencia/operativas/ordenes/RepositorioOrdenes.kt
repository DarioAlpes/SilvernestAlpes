package co.smartobjects.persistencia.operativas.ordenes

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.ordenes.LoteDeOrdenes
import co.smartobjects.entidades.operativas.ordenes.Orden
import co.smartobjects.entidades.operativas.ordenes.Transaccion
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.CampoActualizableDesconocido
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorEliminacionViolacionDeRestriccion
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import co.smartobjects.persistencia.operativas.ColumnasTransaccionales
import com.j256.ormlite.field.SqlType

interface RepositorioOrdenes
    : CreadorRepositorio<Orden>,
      CreableConDiferenteSalida<LoteDeOrdenes, List<Orden>>,
      Listable<Orden>,
      Buscable<Orden, Long>,
      ActualizablePorCamposIndividualesParaTabla<LoteDeOrdenes, IdTransaccionActualizacionTerminacionOrden>,
      EliminablePorId<Orden, Long>

internal fun darCreableDeOrdenesDesdeLoteDeOrdenesSinTransaccion(
        parametrosDaoOrden: ParametrosParaDAOEntidadDeCliente<OrdenDAO, Long>,
        parametrosDaoTransaccion: ParametrosParaDAOEntidadDeCliente<TransaccionDAO, Long>)
        : CreableConDiferenteSalida<LoteDeOrdenesConParametrosEliminacion, List<Orden>>
{
    return CreableConRestriccionConDiferenteSalida(
            CreableConTransformacionIntermediaConDiferenteSalida
            (
                    CreableMultiplesUnoAUno
                    (
                            CreableDAORelacionUnoAMuchos
                            (
                                    CreableDAO(parametrosDaoOrden, Orden.NOMBRE_ENTIDAD),
                                    CreableDAOMultiples(parametrosDaoTransaccion, Orden.NOMBRE_ENTIDAD),
                                    object : TransformadorEntidadesRelacionadas<OrdenDAO, TransaccionDAO>
                                    {
                                        override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: OrdenDAO, entidadDestino: TransaccionDAO): TransaccionDAO
                                        {
                                            return entidadDestino.copy(ordenDAO = entidadDestino.ordenDAO.copy(id = entidadOrigen.id))
                                        }
                                    }
                            )
                    ),
                    object : TransformadorEntidadCliente<
                            LoteDeOrdenesConParametrosEliminacion,
                            List<EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>>
                            >
                    {
                        override fun transformar(idCliente: Long, origen: LoteDeOrdenesConParametrosEliminacion)
                                : List<EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>>
                        {
                            val nuevoOrigen = EntidadRelacionUnoAUno(origen.loteDeOrdenes, origen.loteDeOrdenes.ordenes)

                            val loteDeOrdenes = nuevoOrigen.entidadOrigen

                            return nuevoOrigen.entidadDestino.map {
                                val ordenDao = OrdenDAO(loteDeOrdenes, it, true)
                                val transaccionesDao = it.transacciones.map {
                                    when (it)
                                    {
                                        is Transaccion.Debito  -> TransaccionDAO(it, ordenDao.id, true)
                                        is Transaccion.Credito -> TransaccionDAO(it, ordenDao.id, true)
                                    }
                                }

                                EntidadRelacionUnoAUno(ordenDao, transaccionesDao)
                            }
                        }
                    },
                    object : TransformadorEntidadCliente<List<EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>>, List<Orden>>
                    {
                        override fun transformar(idCliente: Long, origen: List<EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>>): List<Orden>
                        {
                            return origen.map { it.entidadOrigen.aEntidadDeNegocio(idCliente, it.entidadDestino) }
                        }
                    }
            ),
            object : ValidadorRestriccionCreacion<LoteDeOrdenesConParametrosEliminacion>
            {
                override fun validar(idCliente: Long, entidadACrear: LoteDeOrdenesConParametrosEliminacion)
                {
                    val existeAlmenosUnaOrdenTerminada =
                            parametrosDaoOrden[idCliente].dao.queryBuilder().apply {
                                where()
                                    .eq(OrdenDAO.COLUMNA_ID_TRANSACCION, entidadACrear.loteDeOrdenes.id)
                                    .and()
                                    .eq(OrdenDAO.COLUMNA_CREACION_TERMINADA, true)
                            }.countOf() > 0

                    if (existeAlmenosUnaOrdenTerminada)
                    {
                        throw ErrorCreacionActualizacionPorDuplicidad(Orden.NOMBRE_ENTIDAD)
                    }
                }
            }
                                                  )
}

class RepositorioOrdenesSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Orden>,
        private val creador: CreableConDiferenteSalida<LoteDeOrdenes, List<Orden>>,
        private val listador: Listable<Orden>,
        private val buscador: Buscable<Orden, Long>,
        private val actualizadorPorCamposIndividualesParaTabla: ActualizablePorCamposIndividualesParaTabla<LoteDeOrdenes, IdTransaccionActualizacionTerminacionOrden>,
        private val eliminador: EliminablePorId<Orden, Long>)
    : CreadorRepositorio<Orden> by creadorRepositorio,
      CreableConDiferenteSalida<LoteDeOrdenes, List<Orden>> by creador,
      Listable<Orden> by listador,
      Buscable<Orden, Long> by buscador,
      ActualizablePorCamposIndividualesParaTabla<LoteDeOrdenes, IdTransaccionActualizacionTerminacionOrden> by actualizadorPorCamposIndividualesParaTabla,
      EliminablePorId<Orden, Long> by eliminador,
      RepositorioOrdenes
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Orden.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD


    private constructor(
            creadorRepositorio: CreadorRepositorio<Orden>,
            creador: CreableConDiferenteSalida<LoteDeOrdenes, List<Orden>>,
            listador: ListableFiltrableOrdenable<Orden>,
            actualizadorPorCamposIndividualesParaTabla: ActualizablePorCamposIndividualesParaTabla<LoteDeOrdenes, IdTransaccionActualizacionTerminacionOrden>,
            eliminador: EliminablePorId<Orden, Long>
                       )
            : this(
            creadorRepositorio,
            creador,
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(OrdenDAO.TABLA, OrdenDAO.COLUMNA_ID), SqlType.LONG),
            actualizadorPorCamposIndividualesParaTabla,
            eliminador
                  )

    private constructor(
            parametrosDaoOrden: ParametrosParaDAOEntidadDeCliente<OrdenDAO, Long>,
            parametrosDaoTransaccion: ParametrosParaDAOEntidadDeCliente<TransaccionDAO, Long>
                       )
            :
            this(
                    CreadorUnicaVez<Orden>
                    (
                            CreadorRepositorioCompuesto
                            (
                                    listOf(
                                            CreadorRepositorioDAO(parametrosDaoOrden, NOMBRE_ENTIDAD),
                                            CreadorRepositorioDAO(parametrosDaoTransaccion, NOMBRE_ENTIDAD)
                                          ),
                                    NOMBRE_ENTIDAD
                            )
                    ),
                    CreableEnTransaccionSQLParaDiferenteSalida<LoteDeOrdenes, List<Orden>>
                    (
                            parametrosDaoOrden.configuracion,
                            CreableConTransformacionIntermediaTemporalConDiferenteSalida<LoteDeOrdenes, LoteDeOrdenesConParametrosEliminacion, List<Orden>>
                            (
                                    CreableEliminandoMultiplesEntidadesConDiferenteSalida<LoteDeOrdenesConParametrosEliminacion, List<Orden>, OrdenDAO>
                                    (
                                            darCreableDeOrdenesDesdeLoteDeOrdenesSinTransaccion(parametrosDaoOrden, parametrosDaoTransaccion),
                                            EliminableDao(NOMBRE_ENTIDAD, parametrosDaoOrden)
                                    ),
                                    object : TransformadorEntidadCliente<LoteDeOrdenes, LoteDeOrdenesConParametrosEliminacion>
                                    {
                                        override fun transformar(idCliente: Long, origen: LoteDeOrdenes): LoteDeOrdenesConParametrosEliminacion
                                        {
                                            return LoteDeOrdenesConParametrosEliminacion(origen)
                                        }
                                    }
                            )
                    ),
                    ListableConTransaccion
                    (
                            parametrosDaoOrden.configuracion,
                            NOMBRE_ENTIDAD,
                            ListableConTransformacion<EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>, Orden>
                            (
                                    ListableUnoAMuchos<OrdenDAO, TransaccionDAO, Long>
                                    (
                                            ListableInnerJoin<OrdenDAO, TransaccionDAO>
                                            (
                                                    repositorioEntidadDao(parametrosDaoOrden, OrdenDAO.COLUMNA_ID),
                                                    repositorioEntidadDao(parametrosDaoTransaccion, TransaccionDAO.COLUMNA_ID)
                                            ),
                                            object : Transformador<OrdenDAO, Long>
                                            {
                                                override fun transformar(origen: OrdenDAO): Long
                                                {
                                                    return origen.id!!
                                                }
                                            }
                                    ),
                                    object : TransformadorEntidadCliente<EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>, Orden>
                                    {
                                        override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>): Orden
                                        {
                                            return origen.entidadOrigen.aEntidadDeNegocio(idCliente, origen.entidadDestino)
                                        }
                                    }
                            )
                    ),
                    ActualizablePorCamposIndividualesParaTablaEnTransaccionSQL
                    (
                            parametrosDaoOrden.configuracion,
                            ActualizablePorCamposIndividualesParaTablaDao<LoteDeOrdenes, OrdenDAO, IdTransaccionActualizacionTerminacionOrden>
                            (
                                    parametrosDaoOrden,
                                    NOMBRE_ENTIDAD,
                                    object : Transformador<CamposDeEntidad<LoteDeOrdenes>, CamposDeEntidadDAO<OrdenDAO>>
                                    {
                                        override fun transformar(origen: CamposDeEntidad<LoteDeOrdenes>): CamposDeEntidadDAO<OrdenDAO>
                                        {
                                            return origen.map {
                                                if (it.key == EntidadTransaccional.Campos.CREACION_TERMINADA)
                                                {
                                                    OrdenDAO.COLUMNA_CREACION_TERMINADA to CampoModificableEntidadDao<OrdenDAO, Any?>(it.value.valor, OrdenDAO.COLUMNA_CREACION_TERMINADA)
                                                }
                                                else
                                                {
                                                    throw CampoActualizableDesconocido(it.key, Orden.NOMBRE_ENTIDAD)
                                                }
                                            }.toMap()
                                        }
                                    }
                            )
                    ),
                    EliminablePorIdEnTransaccionSQL
                    (
                            parametrosDaoOrden.configuracion,
                            EliminableSimple
                            (
                                    EliminableConRestriccion
                                    (
                                            EliminableDao(NOMBRE_ENTIDAD, parametrosDaoOrden)
                                                .conFiltrosSQL(
                                                        sequenceOf(FiltroIgualdad(CampoTabla(OrdenDAO.TABLA, ColumnasTransaccionales.COLUMNA_CREACION_TERMINADA), false, SqlType.BOOLEAN))
                                                              ),
                                            object : ValidadorRestriccionEliminacion<Long>
                                            {
                                                override fun validarRestriccion(idCliente: Long, idAEliminar: Long)
                                                {
                                                    val ordenAEliminar = parametrosDaoOrden[idCliente].dao.queryForId(idAEliminar)

                                                    if (ordenAEliminar != null && ordenAEliminar.creacionTerminada)
                                                    {
                                                        throw ErrorEliminacionViolacionDeRestriccion(
                                                                NOMBRE_ENTIDAD,
                                                                idAEliminar.toString(),
                                                                "La orden se encuentra marcada como terminada",
                                                                null
                                                                                                    )
                                                    }
                                                }
                                            }
                                    ),
                                    TransformadorIdentidad()
                            )
                    )
                )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, OrdenDAO.TABLA, OrdenDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, TransaccionDAO.TABLA, TransaccionDAO::class.java)
                  )
}

internal data class LoteDeOrdenesConParametrosEliminacion(val loteDeOrdenes: LoteDeOrdenes) : EntidadConFiltrosIgualdadEliminacion()
{
    override val filtrosEliminacion: List<FiltroIgualdad<*>> =
            listOf(
                    FiltroIgualdad(CampoTabla(OrdenDAO.TABLA, OrdenDAO.COLUMNA_ID_TRANSACCION), loteDeOrdenes.id, SqlType.STRING),
                    FiltroIgualdad(CampoTabla(OrdenDAO.TABLA, OrdenDAO.COLUMNA_CREACION_TERMINADA), false, SqlType.BOOLEAN)
                  )
}

data class IdTransaccionActualizacionTerminacionOrden(private val idTransaccion: String) : ParametrosConsulta
{
    override val filtrosSQL: List<FiltroSQL> = listOf(FiltroIgualdad(CampoTabla(OrdenDAO.TABLA, OrdenDAO.COLUMNA_ID_TRANSACCION), idTransaccion, SqlType.STRING))
}


interface RepositorioOrdenesDeUnaSesionDeManilla
    : ListableConParametros<Orden, IdSesionDeManillaParaConsultaOrdenes>

class RepositorioOrdenesDeUnaSesionDeManillaSQL private constructor(
        private val listador: ListableConParametros<Orden, IdSesionDeManillaParaConsultaOrdenes>
                                                                   ) :
        ListableConParametros<Orden, IdSesionDeManillaParaConsultaOrdenes> by listador,
        RepositorioOrdenesDeUnaSesionDeManilla
{
    private constructor(
            parametrosDaoOrden: ParametrosParaDAOEntidadDeCliente<OrdenDAO, Long>,
            parametrosDaoTransaccion: ParametrosParaDAOEntidadDeCliente<TransaccionDAO, Long>
                       )
            : this(
            ListableConTransaccionYParametros
            (
                    parametrosDaoOrden.configuracion,
                    Orden.NOMBRE_ENTIDAD,
                    ListableConTransformacionYParametros<EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>, Orden, IdSesionDeManillaParaConsultaOrdenes>
                    (
                            ListableUnoAMuchosConParametros<OrdenDAO, TransaccionDAO, Long?, IdSesionDeManillaParaConsultaOrdenes>
                            (
                                    ListableConParametrosSegunListableSQL
                                    (
                                            ListableInnerJoin<OrdenDAO, TransaccionDAO>
                                            (
                                                    ListableSQLConFiltrosSQL
                                                    (
                                                            repositorioEntidadDao(parametrosDaoOrden, OrdenDAO.COLUMNA_ID),
                                                            listOf(FiltroCampoBooleano(CampoTabla(OrdenDAO.TABLA, OrdenDAO.COLUMNA_CREACION_TERMINADA), true))
                                                    ),
                                                    repositorioEntidadDao(parametrosDaoTransaccion, TransaccionDAO.COLUMNA_ID)
                                            )
                                    ),
                                    object : Transformador<OrdenDAO, Long?>
                                    {
                                        override fun transformar(origen: OrdenDAO): Long?
                                        {
                                            return origen.id
                                        }
                                    }
                            ),
                            object : TransformadorEntidadCliente<EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>, Orden>
                            {
                                override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAMuchos<OrdenDAO, TransaccionDAO>): Orden
                                {
                                    return origen.entidadOrigen.aEntidadDeNegocio(idCliente, origen.entidadDestino)
                                }
                            }
                    )
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, OrdenDAO.TABLA, OrdenDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, TransaccionDAO.TABLA, TransaccionDAO::class.java)
                  )
}

data class IdSesionDeManillaParaConsultaOrdenes(private val idSesionDeManilla: Long)
    : ParametrosConsulta
{
    override val filtrosSQL: List<FiltroSQL> = listOf(
            FiltroIgualdad(CampoTabla(OrdenDAO.TABLA, OrdenDAO.COLUMNA_ID_SESION_DE_MANILLA), idSesionDeManilla, SqlType.LONG)
                                                     )
}