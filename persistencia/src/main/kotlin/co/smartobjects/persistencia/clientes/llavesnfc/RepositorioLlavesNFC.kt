package co.smartobjects.persistencia.clientes.llavesnfc

import co.smartobjects.entidades.clientes.Cliente
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import co.smartobjects.persistencia.persistoresormlite.ZonedDateTimeThreeTenType
import org.threeten.bp.ZonedDateTime


interface RepositorioLlavesNFC
    : CreadorRepositorio<Cliente.LlaveNFC>,
      Creable<Cliente.LlaveNFC>,
      BuscableConParametros<Cliente.LlaveNFC, FiltroLlavesNFC>,
      EliminablePorParametros<Cliente.LlaveNFC, FiltroLlavesNFC>


class RepositorioLlavesNFCSQL
private constructor(
        private val creadorRepositorio: CreadorRepositorio<Cliente.LlaveNFC>,
        private val creador: Creable<Cliente.LlaveNFC>,
        private val buscador: BuscableConParametros<Cliente.LlaveNFC, FiltroLlavesNFC>,
        private val eliminador: EliminablePorParametros<Cliente.LlaveNFC, FiltroLlavesNFC>
                   ) : CreadorRepositorio<Cliente.LlaveNFC> by creadorRepositorio,
                       Creable<Cliente.LlaveNFC> by creador,
                       BuscableConParametros<Cliente.LlaveNFC, FiltroLlavesNFC> by buscador,
                       EliminablePorParametros<Cliente.LlaveNFC, FiltroLlavesNFC> by eliminador,
                       RepositorioLlavesNFC
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Cliente.LlaveNFC.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD


    private constructor(
            creadorRepositorio: CreadorRepositorio<Cliente.LlaveNFC>,
            creador: Creable<Cliente.LlaveNFC>,
            listableSinParametros: ListableDAO<LlaveNFCDAO, Long>,
            configuracion: ConfiguracionRepositorios,
            eliminador: EliminablePorParametros<Cliente.LlaveNFC, FiltroLlavesNFC>
                       )
            : this(
            creadorRepositorio,
            creador,
            BuscableConParametrosSegunListableFiltrableConParametros
            (
                    ListableConTransaccionYParametros
                    (
                            configuracion,
                            NOMBRE_ENTIDAD,
                            ListableConTransformacionYParametros
                            (
                                    ListableInSubqueryConParametrosEnSubquery
                                    (
                                            listableSinParametros,
                                            MaximoStringConParametrosSegunListableConParametrosSQL
                                            (
                                                    ListableConParametrosSegunListableSQL(listableSinParametros),
                                                    CampoTabla(LlaveNFCDAO.TABLA, LlaveNFCDAO.COLUMNA_FECHA_CREACION)
                                            ),
                                            CampoTabla(LlaveNFCDAO.TABLA, LlaveNFCDAO.COLUMNA_FECHA_CREACION)
                                    ),
                                    TransformadorEntidadDao()
                            )
                    )
            ),
            eliminador
                  )

    private constructor(parametrosDAO: ParametrosParaDAOEntidadDeCliente<LlaveNFCDAO, Long>)
            : this(
            CreadorUnicaVez(CreadorRepositorioSimple<Cliente.LlaveNFC, LlaveNFCDAO, Long>(parametrosDAO, NOMBRE_ENTIDAD)),
            CreableEnTransaccionSQL
            (
                    parametrosDAO.configuracion,
                    CreableConTransformacion<Cliente.LlaveNFC, Cliente.LlaveNFC>
                    (
                            CreableSimple
                            (
                                    CreableDAO(
                                            parametrosDAO,
                                            NOMBRE_ENTIDAD
                                              ),
                                    object : TransformadorEntidadCliente<Cliente.LlaveNFC, LlaveNFCDAO>
                                    {
                                        override fun transformar(idCliente: Long, origen: Cliente.LlaveNFC): LlaveNFCDAO
                                        {
                                            val entidadDao = LlaveNFCDAO(origen)
                                            origen.limpiar()
                                            return entidadDao
                                        }
                                    }
                            ),
                            TransformadorIdentidadCliente(),
                            object : TransformadorEntidadCliente<Cliente.LlaveNFC, Cliente.LlaveNFC>
                            {
                                override fun transformar(idCliente: Long, origen: Cliente.LlaveNFC): Cliente.LlaveNFC
                                {
                                    return origen.also { it.limpiar() }
                                }
                            }
                    )
            ),
            repositorioEntidadDao(parametrosDAO, LlaveNFCDAO.COLUMNA_ID),
            parametrosDAO.configuracion,
            EliminablePorParametrosEnTransaccionSQL<Cliente.LlaveNFC, FiltroLlavesNFC>
            (
                    parametrosDAO.configuracion,
                    EliminablePorParametrosSimple<Cliente.LlaveNFC, LlaveNFCDAO, Long, FiltroLlavesNFC>(EliminablePorParametrosDao(NOMBRE_ENTIDAD, parametrosDAO))
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(ParametrosParaDAOEntidadDeCliente(configuracion, LlaveNFCDAO.TABLA, LlaveNFCDAO::class.java))
}

sealed class FiltroLlavesNFC : ParametrosConsulta
{
    final override val filtrosSQL by lazy {
        filtrosSQLAdicionales
    }

    protected abstract val filtrosSQLAdicionales: List<FiltroSQL>

    data class ValidaEnFecha(private val fechaHoraCorte: ZonedDateTime) : FiltroLlavesNFC()
    {
        override val filtrosSQLAdicionales: List<FiltroSQL> =
                listOf(
                        FiltroMenorOIgualQue(
                                CampoTabla(LlaveNFCDAO.TABLA, LlaveNFCDAO.COLUMNA_FECHA_CREACION),
                                ZonedDateTimeThreeTenType.deNegocio(fechaHoraCorte),
                                ZonedDateTimeThreeTenType.getSingleton().sqlType
                                            )
                      )
    }
}