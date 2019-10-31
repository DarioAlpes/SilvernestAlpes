package co.smartobjects.persistencia.ubicaciones

import co.smartobjects.entidades.ubicaciones.Ubicacion
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.TransformadorIdentidad
import co.smartobjects.persistencia.basederepositorios.*

interface RepositorioUbicaciones
    : CreadorRepositorio<Ubicacion>,
      Creable<Ubicacion>,
      Listable<Ubicacion>,
      Buscable<Ubicacion, Long>,
      Actualizable<Ubicacion, Long>,
      EliminablePorId<Ubicacion, Long>

private val transformadorUbicacionAUbicacionDaoCreacion = object : TransformadorEntidadCliente<Ubicacion, UbicacionDAO>
{
    override fun transformar(idCliente: Long, origen: Ubicacion): UbicacionDAO
    {
        return UbicacionDAO(origen)
    }
}

private val transformadorUbicacionAUbicacionDaoActualizacion = object : Transformador<Ubicacion, UbicacionDAO>
{
    override fun transformar(origen: Ubicacion): UbicacionDAO
    {
        return UbicacionDAO(origen)
    }
}

class RepositorioUbicacionesSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Ubicacion>,
        private val creador: Creable<Ubicacion>,
        private val listador: Listable<Ubicacion>,
        private val buscador: Buscable<Ubicacion, Long>,
        private val actualizador: Actualizable<Ubicacion, Long>,
        private val eliminador: EliminablePorId<Ubicacion, Long>)
    : CreadorRepositorio<Ubicacion> by creadorRepositorio,
      Creable<Ubicacion> by creador,
      Listable<Ubicacion> by listador,
      Buscable<Ubicacion, Long> by buscador,
      Actualizable<Ubicacion, Long> by actualizador,
      EliminablePorId<Ubicacion, Long> by eliminador,
      RepositorioUbicaciones
{
    override val nombreEntidad: String = Ubicacion.NOMBRE_ENTIDAD

    private constructor(parametrosDAO: ParametrosParaDAOEntidadDeCliente<UbicacionDAO, Long>)
            : this(
            CreadorUnicaVez(
                    CreadorRepositorioSimple(parametrosDAO, Ubicacion.NOMBRE_ENTIDAD)
                           ),
            CreableEnTransaccionSQL(
                    parametrosDAO.configuracion,
                    CreableSimple(
                            CreableJerarquicoDAO(
                                    parametrosDAO,
                                    CreableDAO(parametrosDAO, Ubicacion.NOMBRE_ENTIDAD)
                                                ),
                            transformadorUbicacionAUbicacionDaoCreacion
                                 )
                                   ),
            ListableSimple(ListableDAO(listOf(UbicacionDAO.COLUMNA_ID), parametrosDAO)),
            BuscableSimple(
                    TransformadorIdentidad(),
                    BuscableDao(parametrosDAO)
                          ),
            ActualizableEnTransaccionSQL(
                    parametrosDAO.configuracion,
                    ActualizableSimple(
                            JerarquiaActualizableDAO(
                                    parametrosDAO,
                                    ActualizableDAO(parametrosDAO, Ubicacion.NOMBRE_ENTIDAD)
                                                    ),
                            TransformadorIdentidad(),
                            transformadorUbicacionAUbicacionDaoActualizacion
                                      )
                                        ),
            EliminablePorIdEnTransaccionSQL(
                    parametrosDAO.configuracion,
                    EliminableSimple(
                            EliminableDao(Ubicacion.NOMBRE_ENTIDAD, parametrosDAO),
                            TransformadorIdentidad()
                                    )
                                           )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(ParametrosParaDAOEntidadDeCliente(configuracion, UbicacionDAO.TABLA, UbicacionDAO::class.java))
}