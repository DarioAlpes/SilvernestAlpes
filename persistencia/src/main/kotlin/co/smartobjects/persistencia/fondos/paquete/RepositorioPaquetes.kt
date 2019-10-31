@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.paquete

import co.smartobjects.entidades.fondos.Paquete
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.CampoActualizableDesconocido
import co.smartobjects.persistencia.fondos.combinadores.fondos.paquetes.extractorIdPaqueteDao
import co.smartobjects.persistencia.fondos.combinadores.fondos.paquetes.repositorioPaqueteDao
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import com.j256.ormlite.field.SqlType


interface RepositorioPaquetes
    : CreadorRepositorio<Paquete>,
      Creable<Paquete>,
      Listable<Paquete>,
      Buscable<Paquete, Long>,
      Actualizable<Paquete, Long>,
      ActualizablePorCamposIndividuales<Paquete, Long>,
      EliminablePorId<Paquete, Long>


private typealias RelacionPaqueteConFondos = EntidadRelacionUnoAMuchos<PaqueteDAO, FondoPaqueteDAO>

private val asignarIdPaqueteCreadoAFondoPaquete =
        object : TransformadorEntidadesRelacionadas<PaqueteDAO, FondoPaqueteDAO>
        {
            override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: PaqueteDAO, entidadDestino: FondoPaqueteDAO): FondoPaqueteDAO
            {
                return entidadDestino.copy(paqueteDAO = entidadOrigen)
            }
        }

private val dePaqueteACraerARelacionesEnDao =
        object : TransformadorEntidadCliente<Paquete, RelacionPaqueteConFondos>
        {
            override fun transformar(idCliente: Long, origen: Paquete): RelacionPaqueteConFondos
            {
                val paqueteDao = PaqueteDAO(origen)

                return RelacionPaqueteConFondos(
                        paqueteDao,
                        origen
                            .fondosIncluidos
                            .map { FondoPaqueteDAO(0, it) }
                                               )
            }
        }

private val deRelacionesEnDaoAPaqueteCreado =
        object : TransformadorEntidadCliente<RelacionPaqueteConFondos, Paquete>
        {
            override fun transformar(idCliente: Long, origen: RelacionPaqueteConFondos): Paquete
            {
                val paqueteDao = origen.entidadOrigen
                val fondosIncluidos = origen.entidadDestino.toList()

                return paqueteDao.aEntidadDeNegocioConPrecio(idCliente, fondosIncluidos)
            }
        }

private inline fun repositorioFondoPaqueteDao(
        parametrosDao: ParametrosParaDAOEntidadDeCliente<FondoPaqueteDAO, Long>
                                             ) = repositorioEntidadDao(parametrosDao, listOf())

private val transformadorFondoPaqueteDAOEsNull =
        object : Transformador<FondoPaqueteDAO, Boolean>
        {
            override fun transformar(origen: FondoPaqueteDAO): Boolean
            {
                return origen.id == null
            }
        }

private val transformadorCamposNegocioACamposDaoPaquete =
        object : Transformador<CamposDeEntidad<Paquete>, CamposDeEntidadDAO<PaqueteDAO>>
        {
            override fun transformar(origen: CamposDeEntidad<Paquete>): CamposDeEntidadDAO<PaqueteDAO>
            {
                return origen.map {
                    when (it.key)
                    {
                        Paquete.Campos.NOMBRE                   ->
                        {
                            PaqueteDAO.COLUMNA_NOMBRE to CampoModificableEntidadDao<PaqueteDAO, Any?>(it.value.valor, PaqueteDAO.COLUMNA_NOMBRE)
                        }
                        Paquete.Campos.DESCRIPCION              ->
                        {
                            PaqueteDAO.COLUMNA_DESCRIPCION to CampoModificableEntidadDao(it.value.valor, PaqueteDAO.COLUMNA_DESCRIPCION)
                        }
                        Paquete.Campos.DISPONIBLE_PARA_LA_VENTA ->
                        {
                            PaqueteDAO.COLUMNA_DISPONIBLE_PARA_LA_VENTA to CampoModificableEntidadDao(it.value.valor, PaqueteDAO.COLUMNA_DISPONIBLE_PARA_LA_VENTA)
                        }
                        else                                    ->
                        {
                            throw CampoActualizableDesconocido(it.key, Paquete.NOMBRE_ENTIDAD)
                        }
                    }
                }.toMap()
            }
        }

class RepositorioPaquetesSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Paquete>,
        private val creador: Creable<Paquete>,
        private val listador: Listable<Paquete>,
        private val buscador: Buscable<Paquete, Long>,
        private val actualizador: Actualizable<Paquete, Long>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Paquete, Long>,
        private val eliminador: EliminablePorId<Paquete, Long>)
    : CreadorRepositorio<Paquete> by creadorRepositorio,
      Creable<Paquete> by creador,
      Listable<Paquete> by listador,
      Buscable<Paquete, Long> by buscador,
      Actualizable<Paquete, Long> by actualizador,
      ActualizablePorCamposIndividuales<Paquete, Long> by actualizadorCamposIndividuales,
      EliminablePorId<Paquete, Long> by eliminador,
      RepositorioPaquetes
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Paquete.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<Paquete>,
            creador: Creable<Paquete>,
            listador: ListableFiltrableOrdenable<Paquete>,
            actualizador: Actualizable<Paquete, Long>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Paquete, Long>,
            eliminador: EliminablePorId<Paquete, Long>
                       )
            : this(
            creadorRepositorio,
            creador,
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(PaqueteDAO.TABLA, PaqueteDAO.COLUMNA_ID), SqlType.LONG),
            actualizador,
            actualizadorCamposIndividuales,
            eliminador
                  )

    private constructor(
            parametrosDaoPaquete: ParametrosParaDAOEntidadDeCliente<PaqueteDAO, Long>,
            parametrosDaoFondoPaquete: ParametrosParaDAOEntidadDeCliente<FondoPaqueteDAO, Long>
                       )
            : this(
            CreadorUnicaVez(
                    CreadorRepositorioCompuesto(
                            listOf(
                                    CreadorRepositorioDAO(parametrosDaoPaquete, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosDaoFondoPaquete, NOMBRE_ENTIDAD)
                                  ),
                            NOMBRE_ENTIDAD
                                               )
                           ),
            CreableEnTransaccionSQL<Paquete>
            (
                    parametrosDaoPaquete.configuracion,
                    CreableConTransformacion
                    (
                            CreableDAORelacionUnoAMuchos<PaqueteDAO, FondoPaqueteDAO>
                            (
                                    CreableDAO(parametrosDaoPaquete, NOMBRE_ENTIDAD),
                                    CreableDAOMultiples(parametrosDaoFondoPaquete, Paquete.FondoIncluido.NOMBRE_ENTIDAD),
                                    asignarIdPaqueteCreadoAFondoPaquete
                            ),
                            dePaqueteACraerARelacionesEnDao,
                            deRelacionesEnDaoAPaqueteCreado
                    )
            ),
            ListableConTransaccion
            (
                    parametrosDaoPaquete.configuracion,
                    NOMBRE_ENTIDAD,
                    ListableConTransformacion<RelacionPaqueteConFondos, Paquete>
                    (
                            ListableUnoAMuchos
                            (
                                    ListableLeftJoin
                                    (
                                            repositorioPaqueteDao(parametrosDaoPaquete),
                                            repositorioFondoPaqueteDao(parametrosDaoFondoPaquete),
                                            transformadorFondoPaqueteDAOEsNull
                                    ),
                                    extractorIdPaqueteDao
                            ),
                            object : TransformadorEntidadCliente<RelacionPaqueteConFondos, Paquete>
                            {
                                override fun transformar(idCliente: Long, origen: RelacionPaqueteConFondos): Paquete
                                {
                                    val paqueteDao = origen.entidadOrigen
                                    val fondosIncluidos = origen.entidadDestino

                                    return paqueteDao.aEntidadDeNegocioConPrecio(idCliente, fondosIncluidos)
                                }
                            }
                    )
            ),
            ActualizableEnTransaccionSQL
            (
                    parametrosDaoPaquete.configuracion,
                    ActualizableConTransformacion<Paquete, Long, RelacionPaqueteConFondos, Long>
                    (
                            ActualizableEntidadRelacionUnoAMuchosClonandoHijos<RelacionPaqueteConFondos, Long, Long>
                            (
                                    ActualizableParcialCompuesto<RelacionPaqueteConFondos, Long, List<FondoPaqueteDAO>>
                                    (
                                            ActualizableConTransformacion<RelacionPaqueteConFondos, Long, PaqueteDAO, Long>
                                            (
                                                    ActualizableDAO(parametrosDaoPaquete, NOMBRE_ENTIDAD),
                                                    TransformadorIdentidad(),
                                                    object : Transformador<RelacionPaqueteConFondos, PaqueteDAO>
                                                    {
                                                        override fun transformar(origen: RelacionPaqueteConFondos): PaqueteDAO
                                                        {
                                                            return origen.entidadOrigen
                                                        }
                                                    },
                                                    object : TransformadorEntidadCliente<PaqueteDAO, RelacionPaqueteConFondos>
                                                    {
                                                        override fun transformar(idCliente: Long, origen: PaqueteDAO): RelacionPaqueteConFondos
                                                        {
                                                            return RelacionPaqueteConFondos(origen, emptyList())
                                                        }
                                                    }
                                            ),
                                            object : Transformador<RelacionPaqueteConFondos, List<FondoPaqueteDAO>>
                                            {
                                                override fun transformar(origen: RelacionPaqueteConFondos): List<FondoPaqueteDAO>
                                                {
                                                    return origen.entidadDestino
                                                }
                                            },
                                            object : AsignadorParametro<RelacionPaqueteConFondos, List<FondoPaqueteDAO>>
                                            {
                                                override fun asignarParametro(entidad: RelacionPaqueteConFondos, parametro: List<FondoPaqueteDAO>): RelacionPaqueteConFondos
                                                {
                                                    return entidad.copy(entidadDestino = parametro)
                                                }
                                            }
                                    ),
                                    FondoPaqueteDAO.COLUMNA_ID_PAQUETE,
                                    ActualizableEntidadRelacionUnoAMuchosClonandoHijos.HijoClonable<RelacionPaqueteConFondos, FondoPaqueteDAO, Long>
                                    (
                                            FondoPaqueteDAO.TABLA,
                                            EliminableDao(Paquete.FondoIncluido.NOMBRE_ENTIDAD, parametrosDaoFondoPaquete),
                                            SqlType.LONG,
                                            CreableDAOMultiples(parametrosDaoFondoPaquete, Paquete.FondoIncluido.NOMBRE_ENTIDAD),
                                            object : Transformador<RelacionPaqueteConFondos, List<FondoPaqueteDAO>>
                                            {
                                                override fun transformar(origen: RelacionPaqueteConFondos): List<FondoPaqueteDAO>
                                                {
                                                    return origen.entidadDestino
                                                }
                                            },
                                            object : AsignadorParametro<RelacionPaqueteConFondos, List<FondoPaqueteDAO>>
                                            {
                                                override fun asignarParametro(entidad: RelacionPaqueteConFondos, parametro: List<FondoPaqueteDAO>): RelacionPaqueteConFondos
                                                {
                                                    return entidad.copy(entidadDestino = parametro)
                                                }
                                            }
                                    )
                            ),
                            TransformadorIdentidad(),
                            object : Transformador<Paquete, RelacionPaqueteConFondos>
                            {
                                override fun transformar(origen: Paquete): RelacionPaqueteConFondos
                                {
                                    return RelacionPaqueteConFondos(
                                            PaqueteDAO(origen),
                                            origen.fondosIncluidos.map { FondoPaqueteDAO(origen.id, it) }
                                                                   )
                                }
                            },
                            deRelacionesEnDaoAPaqueteCreado
                    )

            ),
            ActualizablePorCamposIndividualesEnTransaccionSQL
            (
                    parametrosDaoPaquete.configuracion,
                    ActualizablePorCamposIndividualesSimple
                    (
                            ActualizablePorCamposIndividualesDao(Paquete.NOMBRE_ENTIDAD, parametrosDaoPaquete),
                            TransformadorIdentidad(),
                            transformadorCamposNegocioACamposDaoPaquete
                    )
            ),
            EliminablePorIdEnTransaccionSQL
            (
                    parametrosDaoPaquete.configuracion,
                    EliminableSimple(
                            EliminableDao
                            (
                                    Paquete.NOMBRE_ENTIDAD,
                                    parametrosDaoPaquete
                            ),
                            TransformadorIdentidad()
                                    )
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, PaqueteDAO.TABLA, PaqueteDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, FondoPaqueteDAO.TABLA, FondoPaqueteDAO::class.java)
                  )
}