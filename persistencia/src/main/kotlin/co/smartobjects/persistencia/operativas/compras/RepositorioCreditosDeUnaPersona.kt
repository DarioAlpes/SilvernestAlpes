package co.smartobjects.persistencia.operativas.compras

import co.smartobjects.entidades.operativas.compras.Compra
import co.smartobjects.entidades.operativas.compras.CreditosDeUnaPersona
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import co.smartobjects.persistencia.personas.PersonaDAO
import com.j256.ormlite.field.SqlType
import org.threeten.bp.ZonedDateTime

interface RepositorioCreditosDeUnaPersona
    : BuscableConParametros<CreditosDeUnaPersona, FiltroCreditosPersona>


internal class ListableProyectandoSoloCreditosFondoYPaquete
(
        private val parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>,
        private val parametrosDaoCreditoPaquete: ParametrosParaDAOEntidadDeCliente<CreditoPaqueteDAO, Long>,
        private val listadorOrigen: ListableSQL<EntidadRelacionUnoAUno<CompraDAO, EntidadRelacionUnoAUno<CreditoFondoDAO, CreditoPaqueteDAO?>>>
) : ListableSQL<EntidadRelacionUnoAUno<CreditoFondoDAO, CreditoPaqueteDAO?>>
{
    override val camposDeOrdenamiento: List<CampoTabla> = listadorOrigen.camposDeOrdenamiento

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableFiltrableOrdenable<EntidadRelacionUnoAUno<CreditoFondoDAO, CreditoPaqueteDAO?>>
    {
        return ListableSQLConFiltrosSQL(this, filtrosSQL)
    }

    override fun darConstructorQuery(idCliente: Long): ConstructorQueryORMLite<EntidadRelacionUnoAUno<CreditoFondoDAO, CreditoPaqueteDAO?>>
    {
        return listadorOrigen.darConstructorQuery(idCliente).reemplazandoProyeccion(
                InformacionProyeccion(
                        listOf(ProyeccionTodos(CreditoFondoDAO.TABLA), ProyeccionTodos(CreditoPaqueteDAO.TABLA)),
                        MapeadorResultadoORMLiteParaJoin(
                                MapeadorResultadoORMLiteParaRango(parametrosDaoCreditoFondo[idCliente], 0),
                                MapeadorResultadoORMLiteParaRango(parametrosDaoCreditoPaquete[idCliente], 0)
                                                        )
                                     )
                                                                                   )
    }
}

internal class ListableDeCreditosFondosYPaquetesACreditosDeUnaPersonaSinIdPersona
(
        private val listableEntidadDestino: ListableConParametrosFiltrableOrdenable<EntidadRelacionUnoAUno<CreditoFondoDAO, CreditoPaqueteDAO?>, FiltroCreditosPersona>
) : ListableConParametrosFiltrableOrdenable<CreditosDeUnaPersona, FiltroCreditosPersona>
{
    private fun convertirACreditosDeUnaPersonaSinIdPersona(
            idCliente: Long,
            idPersona: Long,
            secuencia: Sequence<EntidadRelacionUnoAUno<CreditoFondoDAO, CreditoPaqueteDAO?>>
                                                          )
            : Sequence<CreditosDeUnaPersona>
    {
        val creditoPaqueteVsCreditosFondo = mutableMapOf<Long, Pair<CreditoPaqueteDAO, MutableList<CreditoFondoDAO>>>()
        val creditosFondoSinPaquete = mutableListOf<CreditoFondoDAO>()

        for (relacionUnoAUno in secuencia)
        {
            val creditoFondoDao = relacionUnoAUno.entidadOrigen
            val creditoPaqueteDao = relacionUnoAUno.entidadDestino
            val idCreditoPaquete = creditoPaqueteDao?.id
            if (idCreditoPaquete != null)
            {
                if (!creditoPaqueteVsCreditosFondo.containsKey(idCreditoPaquete))
                {
                    creditoPaqueteVsCreditosFondo[idCreditoPaquete] = Pair(creditoPaqueteDao, mutableListOf())
                }
                creditoPaqueteVsCreditosFondo[idCreditoPaquete]!!.second.add(creditoFondoDao)
            }
            else
            {
                creditosFondoSinPaquete.add(creditoFondoDao)
            }
        }

        return sequenceOf(
                CreditosDeUnaPersona(
                        idCliente,
                        idPersona,
                        creditosFondoSinPaquete.map { it.aEntidadDeNegocio(idCliente) },
                        creditoPaqueteVsCreditosFondo.map {
                            it.value.first.aEntidadDeNegocio(idCliente, it.value.second)
                        }
                                    )
                         )
    }

    override fun listarOrdenado(idCliente: Long, parametros: FiltroCreditosPersona): Sequence<CreditosDeUnaPersona>
    {
        return convertirACreditosDeUnaPersonaSinIdPersona(
                idCliente,
                parametros.idPersona,
                listableEntidadDestino.listarOrdenado(idCliente, parametros)
                                                         )
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>): ListableConParametrosFiltrableOrdenable<CreditosDeUnaPersona, FiltroCreditosPersona>
    {
        return ListableDeCreditosFondosYPaquetesACreditosDeUnaPersonaSinIdPersona(listableEntidadDestino.conFiltrosSQL(filtrosSQL))
    }

    override fun listarSegunParametros(idCliente: Long, parametros: FiltroCreditosPersona): Sequence<CreditosDeUnaPersona>
    {
        return convertirACreditosDeUnaPersonaSinIdPersona(
                idCliente,
                parametros.idPersona,
                listableEntidadDestino.listarSegunParametros(idCliente, parametros)
                                                         )
    }
}

class RepositorioCreditosDeUnaPersonaSQL private constructor(
        private val buscador: BuscableConParametros<CreditosDeUnaPersona, FiltroCreditosPersona>
                                                            ) :
        BuscableConParametros<CreditosDeUnaPersona, FiltroCreditosPersona> by buscador,
        RepositorioCreditosDeUnaPersona
{
    private constructor(
            parametrosDaoPersona: ParametrosParaDAOEntidadDeCliente<PersonaDAO, Long>,
            parametrosDaoCompra: ParametrosParaDAOEntidadDeCliente<CompraDAO, String>,
            parametrosDaoCreditoPaquete: ParametrosParaDAOEntidadDeCliente<CreditoPaqueteDAO, Long>,
            parametrosDaoCreditoFondo: ParametrosParaDAOEntidadDeCliente<CreditoFondoDAO, Long>
                       )
            : this(
            BuscableConParametrosConRestriccion(
                    BuscableConParametrosSegunListableFiltrableConParametros
                    (
                            ListableConTransaccionYParametros
                            (
                                    parametrosDaoCompra.configuracion,
                                    Compra.NOMBRE_ENTIDAD,
                                    ListableDeCreditosFondosYPaquetesACreditosDeUnaPersonaSinIdPersona
                                    (
                                            ListableConParametrosSegunListableSQL
                                            (
                                                    ListableProyectandoSoloCreditosFondoYPaquete
                                                    (
                                                            parametrosDaoCreditoFondo,
                                                            parametrosDaoCreditoPaquete,
                                                            ListableInnerJoin
                                                            (
                                                                    ListableSQLConFiltrosSQL
                                                                    (
                                                                            repositorioEntidadDao(parametrosDaoCompra, CompraDAO.COLUMNA_ID),
                                                                            listOf(FiltroCampoBooleano(CampoTabla(CompraDAO.TABLA, CompraDAO.COLUMNA_CREACION_TERMINADA), true))
                                                                    ),
                                                                    ListableLeftJoin
                                                                    (
                                                                            repositorioEntidadDao(parametrosDaoCreditoFondo, CreditoFondoDAO.COLUMNA_ID),
                                                                            repositorioEntidadDao(parametrosDaoCreditoPaquete, CreditoPaqueteDAO.COLUMNA_ID),
                                                                            object : Transformador<CreditoPaqueteDAO, Boolean>
                                                                            {
                                                                                override fun transformar(origen: CreditoPaqueteDAO): Boolean
                                                                                {
                                                                                    return origen.id == null
                                                                                }
                                                                            }
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
                    ),
                    object : ValidadorRestriccionBuscableParametros<CreditosDeUnaPersona, FiltroCreditosPersona>
                    {
                        override fun validar(idCliente: Long, parametros: FiltroCreditosPersona)
                        {
                            if (!parametrosDaoPersona[idCliente].dao.idExists(parametros.idPersona))
                            {
                                throw EntidadNoExiste(parametros.idPersona, Persona.NOMBRE_ENTIDAD)
                            }
                        }
                    }
                                               )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, PersonaDAO.TABLA, PersonaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CompraDAO.TABLA, CompraDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoPaqueteDAO.TABLA, CreditoPaqueteDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CreditoFondoDAO.TABLA, CreditoFondoDAO::class.java)
                  )
}

sealed class FiltroCreditosPersona(val idPersona: Long) : ParametrosConsulta
{
    final override val filtrosSQL by lazy {
        listOf(FiltroIgualdad(CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_ID_PERSONA), idPersona, SqlType.LONG)) +
        filtrosSQLAdicionales
    }

    protected abstract val filtrosSQLAdicionales: List<FiltroSQL>

    class NoConsumidosValidosParaDia(idPersona: Long, fechaHoraCorte: ZonedDateTime) : FiltroCreditosPersona(idPersona)
    {
        override val filtrosSQLAdicionales: List<FiltroSQL> =
                listOf(
                        FiltroCampoBooleano(CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_CONSUMIDO), false),
                        FiltroMenorOIgualQue(
                                CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_VALIDO_DESDE),
                                ZonedDateTimeThreeTenType.deNegocio(fechaHoraCorte),
                                ZonedDateTimeThreeTenType.getSingleton().sqlType
                                            )
                            .or(
                                    FiltroIgualdad(
                                            CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_VALIDO_DESDE),
                                            null,
                                            ZonedDateTimeThreeTenType.getSingleton().sqlType
                                                  )
                               )
                        ,
                        FiltroMayorOIgualQue(
                                CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_VALIDO_HASTA),
                                ZonedDateTimeThreeTenType.deNegocio(fechaHoraCorte),
                                ZonedDateTimeThreeTenType.getSingleton().sqlType
                                            )
                            .or(
                                    FiltroIgualdad(
                                            CampoTabla(CreditoFondoDAO.TABLA, CreditoFondoDAO.COLUMNA_VALIDO_HASTA),
                                            null,
                                            ZonedDateTimeThreeTenType.getSingleton().sqlType
                                                  )
                               )
                      )
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FiltroCreditosPersona

        if (idPersona != other.idPersona) return false

        return true
    }

    override fun hashCode(): Int
    {
        return idPersona.hashCode()
    }
}