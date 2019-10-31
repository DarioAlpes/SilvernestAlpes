package co.smartobjects.persistencia.ubicaciones.contabilizables

import co.smartobjects.entidades.ubicaciones.contabilizables.UbicacionesContabilizables
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.basederepositorios.*


interface RepositorioUbicacionesContabilizables
    : CreadorRepositorio<UbicacionesContabilizables>,
      CreableConDiferenteSalida<UbicacionesContabilizables, List<Long>>,
      Listable<Long>


class RepositorioUbicacionesContabilizablesSQL private constructor
(
        private val creador: CreadorRepositorio<UbicacionesContabilizables>,
        private val creable: CreableConDiferenteSalida<UbicacionesContabilizables, List<Long>>,
        private val listable: Listable<Long>
) : CreadorRepositorio<UbicacionesContabilizables> by creador,
    CreableConDiferenteSalida<UbicacionesContabilizables, List<Long>> by creable,
    Listable<Long> by listable,
    RepositorioUbicacionesContabilizables
{
    companion object
    {
        private val NOMBRE_ENTIDAD = UbicacionesContabilizables.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD


    private constructor(parametrosDao: ParametrosParaDAOEntidadDeCliente<UbicacionContabilizableDAO, Long?>)
            :
            this(
                    CreadorUnicaVez<UbicacionesContabilizables>
                    (
                            CreadorRepositorioSimple(parametrosDao, NOMBRE_ENTIDAD)
                    ),
                    CreableEnTransaccionSQLParaDiferenteSalida<UbicacionesContabilizables, List<Long>>
                    (
                            parametrosDao.configuracion,
                            CreableEliminandoMultiplesConDiferenteSalidaYParametros
                            (
                                    CreableConTransformacionIntermediaConDiferenteSalida<UbicacionesContabilizables, List<UbicacionContabilizableDAO>, List<Long>>
                                    (
                                            CreableDAOMultiples(parametrosDao, NOMBRE_ENTIDAD),
                                            object : TransformadorEntidadCliente<UbicacionesContabilizables, List<UbicacionContabilizableDAO>>
                                            {
                                                override fun transformar(idCliente: Long, origen: UbicacionesContabilizables): List<UbicacionContabilizableDAO>
                                                {
                                                    return origen.idsUbicaciones.map { UbicacionContabilizableDAO(it) }
                                                }
                                            },
                                            object : TransformadorEntidadCliente<List<UbicacionContabilizableDAO>, List<Long>>
                                            {
                                                override fun transformar(idCliente: Long, origen: List<UbicacionContabilizableDAO>): List<Long>
                                                {
                                                    return origen.map { it.ubicacionDAO.id!! }
                                                }
                                            }
                                    ),
                                    object : ParametrosConsulta
                                    {
                                        // Lista vacía indica borrar todas las tuplas de la tabla
                                        override val filtrosSQL: List<FiltroSQL> = emptyList()
                                    },
                                    EliminablePorParametrosDao(NOMBRE_ENTIDAD, parametrosDao)
                            )
                    ),
                    ListableConTransaccion
                    (
                            parametrosDao.configuracion,
                            NOMBRE_ENTIDAD,
                            ListableConTransformacion<UbicacionContabilizableDAO, Long>
                            (
                                    ListableDAO
                                    (
                                            listOf(UbicacionContabilizableDAO.COLUMNA_ID),
                                            parametrosDao
                                    ),
                                    object : TransformadorEntidadCliente<UbicacionContabilizableDAO, Long>
                                    {
                                        override fun transformar(idCliente: Long, origen: UbicacionContabilizableDAO): Long
                                        {
                                            return origen.ubicacionDAO.id!!
                                        }
                                    }
                            )
                    )
                )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, UbicacionContabilizableDAO.TABLA, UbicacionContabilizableDAO::class.java)
                  )
}