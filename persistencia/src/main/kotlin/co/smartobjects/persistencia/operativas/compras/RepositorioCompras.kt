@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.operativas.compras

import co.smartobjects.entidades.operativas.EntidadTransaccional
import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.CampoActualizableDesconocido
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import co.smartobjects.persistencia.operativas.RepositorioTransaccional
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.persistencia.personas.PersonaDAO
import com.j256.ormlite.field.SqlType
import org.threeten.bp.ZonedDateTime


interface RepositorioCompras
    : CreadorRepositorio<Compra>,
      Creable<Compra>,
      Listable<Compra>,
      Buscable<Compra, String>,
      ActualizablePorCamposIndividuales<Compra, String>,
      EliminablePorId<Compra, String>

interface RepositorioComprasDeUnaPersona
    : ListableConParametros<Compra, FiltroComprasPersona>

interface RepositorioPersonasDeUnaCompra
    : ListableConParametros<Persona, NumeroTransaccionPago>

private val asignarCompraDAOAPagoDAO =
        object : TransformadorEntidadesRelacionadas<CompraDAO, PagoDAO>
        {
            override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: CompraDAO, entidadDestino: PagoDAO): PagoDAO
            {
                return entidadDestino.copy(compraDAO = entidadDestino.compraDAO.copy(id = entidadOrigen.id))
            }
        }

private val asignarCompraDAOACreditosDAO =
        object : TransformadorEntidadesRelacionadas<EntidadRelacionUnoAMuchos<CompraDAO, PagoDAO>, EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>
        {
            override fun asignarCampoRelacionAEntidadDestino(
                    entidadOrigen: EntidadRelacionUnoAMuchos<CompraDAO, PagoDAO>,
                    entidadDestino: EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>
                                                            ): EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>
            {
                return EntidadRelacionUnoAMuchos(
                        entidadDestino.entidadOrigen?.copy(compraDAO = entidadOrigen.entidadOrigen),
                        entidadDestino.entidadDestino.map { it.copy(compraDAO = entidadOrigen.entidadOrigen) }
                                                )
            }
        }

private val transformarCreditosAgrupadosPorPaqueteAListaCreditosPaquete =
        object : Transformador<List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>, List<CreditoPaqueteDAO>>
        {
            override fun transformar(origen: List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>): List<CreditoPaqueteDAO>
            {
                return origen.mapNotNull { it.entidadOrigen }
            }
        }

private val transformarCreditosAgrupadosPorPaqueteAListaCreditosFondos =
        object : Transformador<List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>, List<CreditoFondoDAO>>
        {
            override fun transformar(origen: List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>): List<CreditoFondoDAO>
            {
                return origen.flatMap { it.entidadDestino }
            }
        }

private val asignarCreditoPaqueteDAOACreditosFondoDAOCorrespondientes =
        object : AsignadorParametro<List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>, List<CreditoPaqueteDAO>>
        {
            override fun asignarParametro(entidad: List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>, parametro: List<CreditoPaqueteDAO>): List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>
            {
                var indiceActual = 0
                return entidad.map {
                    if (it.entidadOrigen == null)
                    {
                        it
                    }
                    else
                    {
                        val creditoCreado = parametro[indiceActual++]
                        EntidadRelacionUnoAMuchos(creditoCreado, it.entidadDestino.map { it.copy(creditoPaqueteDAO = creditoCreado) })
                    }
                }
            }
        }

private val asignarCreditoFondoDAOAEntidadResultadoCorrespondientes =
        object : AsignadorParametro<List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>, List<CreditoFondoDAO>>
        {
            override fun asignarParametro(entidad: List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>, parametro: List<CreditoFondoDAO>): List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>
            {
                var indiceActual = 0
                return entidad.map {
                    val resultado = EntidadRelacionUnoAMuchos(it.entidadOrigen, parametro.subList(indiceActual, indiceActual + it.entidadDestino.size))
                    indiceActual += it.entidadDestino.size
                    resultado
                }
            }
        }

private val deCompraAARelacionesEnDaoParaCreacion =
        object : TransformadorEntidadCliente<Compra, EntidadRelacionUnoAMuchos<EntidadRelacionUnoAMuchos<CompraDAO, PagoDAO>, EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>>
        {
            override fun transformar(idCliente: Long, origen: Compra): EntidadRelacionUnoAMuchos<EntidadRelacionUnoAMuchos<CompraDAO, PagoDAO>, EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>
            {
                val compraDAO = CompraDAO(origen, true)
                val pagosDAO = origen.pagos.map { PagoDAO(it, origen.id) }
                val creditosDeFondosDAOIndividuales: EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO> = EntidadRelacionUnoAMuchos(null, origen.creditosFondos.map { CreditoFondoDAO(it, origen.id, true) })
                val creditosDePaquetesDAO = origen.creditosPaquetes.map { CreditoPaqueteDAO(it, origen.id) }

                val creditosDeFondoDAODePaquetes: List<EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>> = origen.creditosPaquetes.zip(creditosDePaquetesDAO).map {
                    EntidadRelacionUnoAMuchos(it.second, it.first.creditosFondos.map { CreditoFondoDAO(it, origen.id, true) })
                }

                return EntidadRelacionUnoAMuchos(EntidadRelacionUnoAMuchos(compraDAO, pagosDAO), listOf(creditosDeFondosDAOIndividuales) + creditosDeFondoDAODePaquetes)
            }
        }


private val deRelacionesEnDaoACompra =
        object : TransformadorEntidadCliente<EntidadRelacionUnoAMuchos<EntidadRelacionUnoAMuchos<CompraDAO, PagoDAO>, EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>, Compra>
        {
            override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAMuchos<EntidadRelacionUnoAMuchos<CompraDAO, PagoDAO>, EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>): Compra
            {
                val compraDAO = origen.entidadOrigen.entidadOrigen
                val pagosDAO = origen.entidadOrigen.entidadDestino
                val creditosDAOAgrupados = origen.entidadDestino

                return compraDAO.aEntidadDeNegocio(idCliente, creditosDAOAgrupados, pagosDAO)
            }
        }

private val transformadorCreditoPaqueteDAOEsNull =
        object : Transformador<CreditoPaqueteDAO, Boolean>
        {
            override fun transformar(origen: CreditoPaqueteDAO): Boolean
            {
                return origen.id == null
            }
        }

private val transformadorPagoDAOEsNull =
        object : Transformador<PagoDAO, Boolean>
        {
            override fun transformar(origen: PagoDAO): Boolean
            {
                return origen.id == null
            }
        }

private val extractorIdCompraDao =
        object : Transformador<CompraDAO, String>
        {
            override fun transformar(origen: CompraDAO): String
            {
                return origen.id
            }
        }

private val extractorIdCompraDaoConCreditoPaqueteDAO =
        object : Transformador<EntidadRelacionUnoAUno<CompraDAO, CreditoPaqueteDAO?>, EntidadRelacionUnoAUno<String, Long?>>
        {
            override fun transformar(origen: EntidadRelacionUnoAUno<CompraDAO, CreditoPaqueteDAO?>): EntidadRelacionUnoAUno<String, Long?>
            {
                return EntidadRelacionUnoAUno(origen.entidadOrigen.id, origen.entidadDestino?.id)
            }
        }

private val comparadorCompraConPagosContraCompraConCreditos =
        object : ComparadorEntidades<
                EntidadRelacionUnoAMuchos<CompraDAO, Any?>,
                EntidadRelacionUnoAMuchos<CompraDAO, Any?>
                >
        {
            override fun comparar(
                    origenIzquierda: EntidadRelacionUnoAMuchos<CompraDAO, Any?>,
                    origenDerecha: EntidadRelacionUnoAMuchos<CompraDAO, Any?>)
                    : Int
            {
                return origenIzquierda.entidadOrigen.id.compareTo(origenDerecha.entidadOrigen.id)
            }
        }

private val transformadorResultadoSecuenciaACompra =
        object : TransformadorEntidadCliente<EntidadRelacionUnoAUno<EntidadRelacionUnoAMuchos<CompraDAO, PagoDAO>?, EntidadRelacionUnoAMuchos<CompraDAO, EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>?>, Compra>
        {
            override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAUno<EntidadRelacionUnoAMuchos<CompraDAO, PagoDAO>?, EntidadRelacionUnoAMuchos<CompraDAO, EntidadRelacionUnoAMuchos<CreditoPaqueteDAO?, CreditoFondoDAO>>?>): Compra
            {
                val compraDAO = origen.entidadOrigen!!.entidadOrigen
                return compraDAO.aEntidadDeNegocio(idCliente, origen.entidadDestino!!.entidadDestino, origen.entidadOrigen.entidadDestino)
            }
        }

private val transformadorCamposNegocioACamposDaoCompra =
        object : Transformador<CamposDeEntidad<Compra>, CamposDeEntidadDAO<CompraDAO>>
        {
            override fun transformar(origen: CamposDeEntidad<Compra>): CamposDeEntidadDAO<CompraDAO>
            {
                return origen.map {
                    when (it.key)
                    {
                        EntidadTransaccional.Campos.CREACION_TERMINADA ->
                        {
                            CompraDAO.COLUMNA_CREACION_TERMINADA to CampoModificableEntidadDao<CompraDAO, Any?>(it.value.valor, CompraDAO.COLUMNA_CREACION_TERMINADA)
                        }
                        else                                           ->
                        {
                            throw CampoActualizableDesconocido(it.key, Compra.NOMBRE_ENTIDAD)
                        }
                    }
                }.toMap()
            }
        }

private fun darListableSQLCompraConPagos(
        parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
        parametrosDaoPago: ParametrosParaDAOEntidadDeCliente<PagoDAO, Long>
                                        ): ListableSQL<EntidadRelacionUnoAUno<CompraDAO, PagoDAO?>>
{
    return ListableLeftJoin(
            repositorioEntidadDao(parametrosDaoCompra, CompraDAO.COLUMNA_ID),
            repositorioEntidadDao(parametrosDaoPago, PagoDAO.COLUMNA_ID),
            transformadorPagoDAOEsNull
                           )
}

private fun darListableSQLCompraConCreditos(
        parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
        parametrosDaoCreditoPaquete: ParametrosParaDAOEntidadDeCliente<CreditoPaqueteDAO, Long>,
        parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>
                                           ): ListableSQL<EntidadRelacionUnoAUno<CompraDAO, EntidadRelacionUnoAUno<CreditoPaqueteDAO?, CreditoFondoDAO>>>
{
    return ListableInnerJoin(
            repositorioEntidadDao(parametrosDaoCompra, CompraDAO.COLUMNA_ID),
            ListableRightJoin
            (
                    repositorioEntidadDao(parametrosDaoCreditoPaquete, CreditoPaqueteDAO.COLUMNA_ID),
                    repositorioEntidadDao(parametrosDaoCreditoFondo, CreditoFondoDAO.COLUMNA_ID),
                    transformadorCreditoPaqueteDAOEsNull
            )
                            )
}

private fun <TipoParametro : ParametrosConsulta> deListablesCompraConPagosDAOYCompraConCreditosDAOAListableCompras(
        parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
        listableCompraConPagoDAO: ListableConParametrosFiltrableOrdenable<EntidadRelacionUnoAUno<CompraDAO, PagoDAO?>, TipoParametro>,
        listableCompraConCreditosDAO: ListableConParametrosFiltrableOrdenable<EntidadRelacionUnoAUno<CompraDAO, EntidadRelacionUnoAUno<CreditoPaqueteDAO?, CreditoFondoDAO>>, TipoParametro>)
        : ListableConParametrosFiltrableOrdenable<Compra, TipoParametro>
{
    return ListableConTransaccionYParametros(
            parametrosDaoCompra.configuracion,
            Compra.NOMBRE_ENTIDAD,
            ListableConTransformacionYParametros
            (
                    ListableAgrupandoConsultasConParametros
                    (
                            ListableUnoAMuchosConParametros
                            (
                                    listableCompraConPagoDAO,
                                    extractorIdCompraDao
                            ),
                            ListableUnoAMuchosConParametros
                            (
                                    ListableConTransformacionYParametros
                                    (
                                            ListableUnoAMuchosConParametros
                                            (
                                                    ListableConTransformacionYParametros
                                                    (
                                                            listableCompraConCreditosDAO,
                                                            shiftIzquierdaEntidadRelacionUnoAUno()
                                                    ),
                                                    extractorIdCompraDaoConCreditoPaqueteDAO
                                            ),
                                            shiftDerechaEntidadRelacionUnoAUno()
                                    ),
                                    extractorIdCompraDao
                            ),
                            comparadorCompraConPagosContraCompraConCreditos
                    ),
                    transformadorResultadoSecuenciaACompra
            )
                                            )
}

class RepositorioComprasSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Compra>,
        private val repositorioTransaccional: RepositorioTransaccional<Compra, CompraDAO>,
        private val listador: Listable<Compra>,
        private val buscador: Buscable<Compra, String>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Compra, String>)
    : CreadorRepositorio<Compra> by creadorRepositorio,
      Creable<Compra> by repositorioTransaccional,
      Listable<Compra> by listador,
      Buscable<Compra, String> by buscador,
      ActualizablePorCamposIndividuales<Compra, String> by actualizadorCamposIndividuales,
      EliminablePorId<Compra, String> by repositorioTransaccional,
      RepositorioCompras
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Compra.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<Compra>,
            creadorSinTransaccion: Creable<Compra>,
            listador: ListableFiltrableOrdenable<Compra>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Compra, String>,
            eliminadorSinTransaccion: EliminablePorIdFiltrable<Compra, String>,
            parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>
                       )
            : this(
            creadorRepositorio,
            RepositorioTransaccional<Compra, CompraDAO>(CompraDAO.TABLA, NOMBRE_ENTIDAD, parametrosDaoCompra, creadorSinTransaccion, eliminadorSinTransaccion),
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(CompraDAO.TABLA, CompraDAO.COLUMNA_ID), SqlType.STRING),
            actualizadorCamposIndividuales
                  )

    private constructor(
            parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
            parametrosDaoPago: ParametrosParaDAOEntidadDeCliente<PagoDAO, Long>,
            parametrosDaoCreditoPaquete: ParametrosParaDAOEntidadDeCliente<CreditoPaqueteDAO, Long>,
            parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>
                       )
            : this(
            CreadorUnicaVez(
                    CreadorRepositorioCompuesto(
                            listOf(
                                    CreadorRepositorioDAO(parametrosDaoCompra, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosDaoPago, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosDaoCreditoPaquete, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosDaoCreditoFondo, NOMBRE_ENTIDAD)
                                  ),
                            NOMBRE_ENTIDAD
                                               )
                           ),

            CreableConTransformacion
            (
                    CreableDAORelacionUnoAMuchos
                    (
                            CreableDAORelacionUnoAMuchos
                            (
                                    CreableDAO(parametrosDaoCompra, NOMBRE_ENTIDAD),
                                    CreableDAOMultiples(parametrosDaoPago, NOMBRE_ENTIDAD),
                                    asignarCompraDAOAPagoDAO
                            ),
                            CreableMultiplesEntidadesParciales
                            (
                                    listOf
                                    (
                                            CreableConEntidadParcial
                                            (
                                                    CreableDAOMultiples(parametrosDaoCreditoPaquete, NOMBRE_ENTIDAD),
                                                    transformarCreditosAgrupadosPorPaqueteAListaCreditosPaquete,
                                                    asignarCreditoPaqueteDAOACreditosFondoDAOCorrespondientes
                                            ),
                                            CreableConEntidadParcial
                                            (
                                                    CreableDAOMultiples(parametrosDaoCreditoFondo, NOMBRE_ENTIDAD),
                                                    transformarCreditosAgrupadosPorPaqueteAListaCreditosFondos,
                                                    asignarCreditoFondoDAOAEntidadResultadoCorrespondientes
                                            )
                                    )
                            ),
                            asignarCompraDAOACreditosDAO
                    ),
                    deCompraAARelacionesEnDaoParaCreacion,
                    deRelacionesEnDaoACompra
            ),
            ListableSegunListableConParametrosUnit
            (
                    deListablesCompraConPagosDAOYCompraConCreditosDAOAListableCompras<UnitParametrosConsulta>
                    (
                            parametrosDaoCompra,
                            ListableConParametrosUnitSegunListable(darListableSQLCompraConPagos(parametrosDaoCompra, parametrosDaoPago)),
                            ListableConParametrosUnitSegunListable(darListableSQLCompraConCreditos(parametrosDaoCompra, parametrosDaoCreditoPaquete, parametrosDaoCreditoFondo))
                    )
            ),
            ActualizablePorCamposIndividualesEnTransaccionSQL
            (
                    parametrosDaoCompra.configuracion,
                    ActualizablePorCamposIndividualesSimple
                    (
                            ActualizablePorCamposIndividualesDao(NOMBRE_ENTIDAD, parametrosDaoCompra),
                            TransformadorIdentidad(),
                            transformadorCamposNegocioACamposDaoCompra
                    )
            ),
            EliminableSimple
            (
                    EliminableDao(NOMBRE_ENTIDAD, parametrosDaoCompra),
                    TransformadorIdentidad()
            ),
            parametrosDaoCompra
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, CompraDAO.TABLA, CompraDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PagoDAO.TABLA, PagoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoPaqueteDAO.TABLA, CreditoPaqueteDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoFondoDAO.TABLA, CreditoFondoDAO::class.java)
                  )
}

class RepositorioComprasDeUnaPersonaSQL private constructor(
        private val listador: ListableConParametros<Compra, FiltroComprasPersona>
                                                           ) :
        ListableConParametros<Compra, FiltroComprasPersona> by listador,
        RepositorioComprasDeUnaPersona
{
    private constructor(
            parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
            parametrosDaoPago: ParametrosParaDAOEntidadDeCliente<PagoDAO, Long>,
            parametrosDaoCreditoPaquete: ParametrosParaDAOEntidadDeCliente<CreditoPaqueteDAO, Long>,
            parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>,
            listableIdsComprasFiltradas: ListableConParametrosSegunListableSQL<String, FiltroComprasPersona>
                       )
            : this(
            deListablesCompraConPagosDAOYCompraConCreditosDAOAListableCompras
            (
                    parametrosDaoCompra,
                    ListableInSubqueryConParametrosEnSubquery
                    (
                            darListableSQLCompraConPagos(parametrosDaoCompra, parametrosDaoPago),
                            listableIdsComprasFiltradas,
                            CampoTabla(CompraDAO.TABLA, CompraDAO.COLUMNA_ID)
                    ),
                    ListableInSubqueryConParametrosEnSubquery
                    (
                            darListableSQLCompraConCreditos
                            (
                                    parametrosDaoCompra,
                                    parametrosDaoCreditoPaquete,
                                    parametrosDaoCreditoFondo
                            ),
                            listableIdsComprasFiltradas,
                            CampoTabla(CompraDAO.TABLA, CompraDAO.COLUMNA_ID)
                    )
            )
                  )

    private constructor(
            parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
            parametrosDaoPago: ParametrosParaDAOEntidadDeCliente<PagoDAO, Long>,
            parametrosDaoCreditoPaquete: ParametrosParaDAOEntidadDeCliente<CreditoPaqueteDAO, Long>,
            parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>
                       )
            : this(
            parametrosDaoCompra,
            parametrosDaoPago,
            parametrosDaoCreditoPaquete,
            parametrosDaoCreditoFondo,
            ListableConParametrosSegunListableSQL
            (
                    ListableProyectandoAColumnaString<EntidadRelacionUnoAUno<CompraDAO, CreditoFondoDAO>>
                    (
                            ListableInnerJoin<CompraDAO, CreditoFondoDAO>
                            (
                                    ListableSQLConFiltrosSQL
                                    (
                                            repositorioEntidadDao(parametrosDaoCompra, CompraDAO.COLUMNA_ID),
                                            listOf(FiltroCampoBooleano(CampoTabla(CompraDAO.TABLA, CompraDAO.COLUMNA_CREACION_TERMINADA), true))
                                    ),
                                    ListableSQLConFiltrosSQL
                                    (
                                            repositorioEntidadDao(parametrosDaoCreditoFondo, CreditoFondoDAO.COLUMNA_ID),
                                            listOf(
                                                    FiltroCampoBooleano(CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_CONSUMIDO), false)
                                                  )
                                    )
                            ),
                            CampoTabla(CompraDAO.TABLA, CompraDAO.COLUMNA_ID)
                    )
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, CompraDAO.TABLA, CompraDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PagoDAO.TABLA, PagoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoPaqueteDAO.TABLA, CreditoPaqueteDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoFondoDAO.TABLA, CreditoFondoDAO::class.java)
                  )
}

class RepositorioPersonasDeUnaCompraSQL private constructor(
        private val listador: ListableConParametros<Persona, NumeroTransaccionPago>
                                                           ) :
        ListableConParametros<Persona, NumeroTransaccionPago> by listador,
        RepositorioPersonasDeUnaCompra
{
    private constructor(
            parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
            parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, String>,
            parametrosDaoPago: ParametrosParaDAOEntidadDeCliente<PagoDAO, Long>,
            parametrosDaoPersona: ParametrosParaDAOEntidadDeCliente<PersonaDAO, Long>
                       )
            : this(
            ListableConTransformacionYParametros
            (
                    ListableConParametrosSegunListableSQL
                    (
                            ListableDistintos
                            (
                                    ListableIgnorandoEntidadDestino
                                    (
                                            ListableInnerJoin
                                            (
                                                    repositorioEntidadDao(parametrosDaoPersona, PersonaDAO.COLUMNA_ID),
                                                    ListableInnerJoin
                                                    (
                                                            repositorioEntidadDao(parametrosDaoCreditoFondo, CreditoFondoDAO.COLUMNA_ID),
                                                            ListableInnerJoin
                                                            (
                                                                    repositorioEntidadDao(parametrosDaoCompra, CompraDAO.COLUMNA_ID),
                                                                    repositorioEntidadDao(parametrosDaoPago, PagoDAO.COLUMNA_ID)
                                                            )
                                                    )
                                            ),
                                            1
                                    )
                            )
                    ),
                    TransformadorEntidadDao()
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, CompraDAO.TABLA, CompraDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoFondoDAO.TABLA, CreditoFondoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PagoDAO.TABLA, PagoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PersonaDAO.TABLA, PersonaDAO::class.java)
                  )
}

abstract class FiltroComprasPersona(private val idPersona: Long) : ParametrosConsulta
{
    override val filtrosSQL: List<FiltroSQL> = listOf(
            FiltroIgualdad(CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_ID_PERSONA), idPersona, SqlType.LONG)
                                                     )

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FiltroComprasPersona

        if (idPersona != other.idPersona) return false

        return true
    }

    override fun hashCode(): Int
    {
        return idPersona.hashCode()
    }
}

class FiltroComprasPersonaConCreditosPresentesOFuturos(idPersona: Long, private val fechaCorte: ZonedDateTime) : FiltroComprasPersona(idPersona)
{
    override val filtrosSQL: List<FiltroSQL> =
            super.filtrosSQL +
            FiltroMayorOIgualQue(
                    CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_VALIDO_HASTA),
                    ZonedDateTimeThreeTenType.deNegocio(fechaCorte),
                    ZonedDateTimeThreeTenType.getSingleton().sqlType
                                )

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as FiltroComprasPersonaConCreditosPresentesOFuturos

        if (fechaCorte != other.fechaCorte) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = super.hashCode()
        result = 31 * result + fechaCorte.hashCode()
        return result
    }
}

data class NumeroTransaccionPago(private val numeroTransaccion: String) : ParametrosConsulta
{
    override val filtrosSQL: List<FiltroSQL> = listOf(
            FiltroIgualdad(CampoTabla(PagoDAO.TABLA, PagoDAO.COLUMNA_NUMERO_TRANSACCION_POS), numeroTransaccion, SqlType.STRING)
                                                     )
}