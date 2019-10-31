package co.smartobjects.persistencia.ubicaciones.consumibles

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.entidades.ubicaciones.consumibles.ConsumibleEnPuntoDeVenta
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.acceso.AccesoDAO
import co.smartobjects.persistencia.fondos.categoriaskus.CategoriaSkuDAO
import co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos.todos.darCombinadorListableParaFondoCompletoConParametros
import co.smartobjects.persistencia.fondos.skus.SkuDAO
import com.j256.ormlite.field.SqlType

interface RepositorioConsumibleEnPuntoDeVenta
    : CreadorRepositorio<ConsumibleEnPuntoDeVenta>,
      CreableConDiferenteSalida<ListaConsumiblesEnPuntoDeVentaUbicaciones, List<ConsumibleEnPuntoDeVenta>>,
      ListableConParametros<ConsumibleEnPuntoDeVenta, IdUbicacionConsultaConsumibles>

interface RepositorioFondosEnPuntoDeVenta
    : ListableConParametros<Fondo<*>, IdUbicacionConsultaConsumibles>

private val campoFKUbicacion = CampoTabla(ConsumibleEnPuntoDeVentaDAO.TABLA, ConsumibleEnPuntoDeVentaDAO.COLUMNA_ID_UBICACION)

private val transformadorConsumibleEnPuntoDeVentaAConsumibleEnPuntoDeVentaDao =
        object : TransformadorEntidadCliente<ConsumibleEnPuntoDeVenta, ConsumibleEnPuntoDeVentaDAO>
        {
            override fun transformar(idCliente: Long, origen: ConsumibleEnPuntoDeVenta): ConsumibleEnPuntoDeVentaDAO
            {
                return ConsumibleEnPuntoDeVentaDAO(origen)
            }
        }


private val transformadorListaConsumiblesEnPuntoDeVentaUbicacionesAListaConsumiblesEnPuntoDeVenta =
        object : TransformadorEntidadCliente<ListaConsumiblesEnPuntoDeVentaUbicaciones, List<ConsumibleEnPuntoDeVenta>>
        {
            override fun transformar(idCliente: Long, origen: ListaConsumiblesEnPuntoDeVentaUbicaciones): List<ConsumibleEnPuntoDeVenta>
            {
                return origen.consumiblesEnPuntoDeVentaUbicaciones.map { it.copiar(idUbicacion = origen.idUbicacion) }
            }
        }

private val extractorParametroIdUbicacion = object : Transformador<ListaConsumiblesEnPuntoDeVentaUbicaciones, IdUbicacionConsultaConsumibles>
{
    override fun transformar(origen: ListaConsumiblesEnPuntoDeVentaUbicaciones): IdUbicacionConsultaConsumibles
    {
        return IdUbicacionConsultaConsumibles(origen.idUbicacion)
    }
}

class RepositorioConsumibleEnPuntoDeVentaSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<ConsumibleEnPuntoDeVenta>,
        private val creador: CreableConDiferenteSalida<ListaConsumiblesEnPuntoDeVentaUbicaciones, List<ConsumibleEnPuntoDeVenta>>,
        private val listador: ListableConParametros<ConsumibleEnPuntoDeVenta, IdUbicacionConsultaConsumibles>)
    : CreadorRepositorio<ConsumibleEnPuntoDeVenta> by creadorRepositorio,
      CreableConDiferenteSalida<ListaConsumiblesEnPuntoDeVentaUbicaciones, List<ConsumibleEnPuntoDeVenta>> by creador,
      ListableConParametros<ConsumibleEnPuntoDeVenta, IdUbicacionConsultaConsumibles> by listador,
      RepositorioConsumibleEnPuntoDeVenta
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = ConsumibleEnPuntoDeVenta.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<ConsumibleEnPuntoDeVenta>,
            listador: ListableConParametros<ConsumibleEnPuntoDeVenta, IdUbicacionConsultaConsumibles>,
            parametrosConsumiblesDAO: ParametrosParaDAOEntidadDeCliente<ConsumibleEnPuntoDeVentaDAO, Long>,
            parametrosFondoDAO: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>
                       )
            : this(
            creadorRepositorio,
            CreableEnTransaccionSQLParaDiferenteSalida<ListaConsumiblesEnPuntoDeVentaUbicaciones, List<ConsumibleEnPuntoDeVenta>>
            (
                    parametrosConsumiblesDAO.configuracion,
                    CreableValidandoSeCrearonNumeroEntidadesCorrectasConDiferenteSalida<ListaConsumiblesEnPuntoDeVentaUbicaciones, ConsumibleEnPuntoDeVenta, IdUbicacionConsultaConsumibles>
                    (
                            CreableEliminandoMultiplesEntidadesConDiferenteSalida<ListaConsumiblesEnPuntoDeVentaUbicaciones, List<ConsumibleEnPuntoDeVenta>, ConsumibleEnPuntoDeVentaDAO>
                            (
                                    CreableConTransformacionConDiferenteSalida<ListaConsumiblesEnPuntoDeVentaUbicaciones, List<ConsumibleEnPuntoDeVenta>>
                                    (
                                            CreableSimpleLista(
                                                    CreableDAOMultiples(parametrosConsumiblesDAO, NOMBRE_ENTIDAD),
                                                    transformadorConsumibleEnPuntoDeVentaAConsumibleEnPuntoDeVentaDao
                                                              ),
                                            transformadorListaConsumiblesEnPuntoDeVentaUbicacionesAListaConsumiblesEnPuntoDeVenta
                                    ),
                                    EliminableDao(NOMBRE_ENTIDAD, parametrosConsumiblesDAO)
                            ),

                            ContableConParametrosSegunContableEntidadPrincipal<ConsumibleEnPuntoDeVenta, EntidadRelacionUnoAUno<ConsumibleEnPuntoDeVentaDAO, FondoDAO>, IdUbicacionConsultaConsumibles>
                            (
                                    ContableConParametrosSegunListableConParametrosSQL<EntidadRelacionUnoAUno<ConsumibleEnPuntoDeVentaDAO, FondoDAO>, IdUbicacionConsultaConsumibles>
                                    (
                                            ListableConParametrosSegunListableSQL
                                            (
                                                    ListableInnerJoin<ConsumibleEnPuntoDeVentaDAO, FondoDAO>
                                                    (
                                                            ListableDAO(listOf(ConsumibleEnPuntoDeVentaDAO.COLUMNA_ID), parametrosConsumiblesDAO),
                                                            ListableSQLConFiltrosSQL
                                                            (
                                                                    ListableDAO(listOf(FondoDAO.COLUMNA_ID), parametrosFondoDAO),
                                                                    listOf(
                                                                            FiltroIn(
                                                                                    CampoTabla(FondoDAO.TABLA, FondoDAO.COLUMNA_TIPO_DE_FONDO),
                                                                                    FondoDAO.TipoDeFondoEnBD.TIPO_FONDOS_CONSUMIBLES,
                                                                                    SqlType.STRING
                                                                                    )
                                                                          )
                                                            )
                                                    )
                                            )

                                    )
                            ),
                            extractorParametroIdUbicacion,
                            { ErrorDeLlaveForanea(null as String?, NOMBRE_ENTIDAD) }
                    )
            ),
            listador
                  )

    private constructor(
            parametrosConsumiblesDAO: ParametrosParaDAOEntidadDeCliente<ConsumibleEnPuntoDeVentaDAO, Long>,
            parametrosFondoDAO: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>
                       )
            : this(
            CreadorUnicaVez(
                    CreadorRepositorioSimple(parametrosConsumiblesDAO, NOMBRE_ENTIDAD)
                           ),
            ListableConParametrosSimple
            (
                    ListableConParametrosDAO(listOf(ConsumibleEnPuntoDeVentaDAO.COLUMNA_ID), parametrosConsumiblesDAO)
            ),
            parametrosConsumiblesDAO,
            parametrosFondoDAO
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, ConsumibleEnPuntoDeVentaDAO.TABLA, ConsumibleEnPuntoDeVentaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, FondoDAO.TABLA, FondoDAO::class.java)
                  )
}

class RepositorioFondosEnPuntoDeVentaSQL private constructor
(
        private val listador: ListableConParametros<Fondo<*>, IdUbicacionConsultaConsumibles>
) : ListableConParametros<Fondo<*>, IdUbicacionConsultaConsumibles> by listador,
    RepositorioFondosEnPuntoDeVenta
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = Fondo.NOMBRE_ENTIDAD
    }

    private constructor(
            parametrosConsumiblesDAO: ParametrosParaDAOEntidadDeCliente<ConsumibleEnPuntoDeVentaDAO, Long>,
            parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
            parametrosDaoSku: ParametrosParaDAOEntidadDeCliente<SkuDAO, Long>,
            parametrosDaoCategoriaSkuDAO: ParametrosParaDAOEntidadDeCliente<CategoriaSkuDAO, Long>,
            parametrosDaoAcceso: ParametrosParaDAOEntidadDeCliente<AccesoDAO, Long>
                       )
            : this(
            darCombinadorListableParaFondoCompletoConParametros<IdUbicacionConsultaConsumibles>
            (
                    ListableIgnorandoEntidadDestino
                    (
                            ListableInnerJoin<FondoDAO, ConsumibleEnPuntoDeVentaDAO>
                            (
                                    ListableDAO(listOf(FondoDAO.COLUMNA_ID), parametrosDaoFondo),
                                    ListableDAO(listOf(ConsumibleEnPuntoDeVentaDAO.COLUMNA_ID), parametrosConsumiblesDAO)
                            ),
                            1
                    ),
                    parametrosDaoSku,
                    parametrosDaoCategoriaSkuDAO,
                    parametrosDaoAcceso,
                    NOMBRE_ENTIDAD
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, ConsumibleEnPuntoDeVentaDAO.TABLA, ConsumibleEnPuntoDeVentaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, FondoDAO.TABLA, FondoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, SkuDAO.TABLA, SkuDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CategoriaSkuDAO.TABLA, CategoriaSkuDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, AccesoDAO.TABLA, AccesoDAO::class.java)
                  )
}

data class ListaConsumiblesEnPuntoDeVentaUbicaciones(
        val consumiblesEnPuntoDeVentaUbicaciones: Set<ConsumibleEnPuntoDeVenta>,
        val idUbicacion: Long
                                                    ) : EntidadConFiltrosIgualdadEliminacion()
{
    override val filtrosEliminacion: List<FiltroIgualdad<*>> = IdUbicacionConsultaConsumibles(idUbicacion).filtrosIgualdad
}


data class IdUbicacionConsultaConsumibles(private val idUbicacion: Long)
    : ParametrosConsulta
{
    internal val filtrosIgualdad: List<FiltroIgualdad<*>> =
            listOf(FiltroIgualdad(campoFKUbicacion, idUbicacion, SqlType.LONG))

    override val filtrosSQL: List<FiltroSQL> = filtrosIgualdad
}