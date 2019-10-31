package co.smartobjects.persistencia.fondos.libros


import co.smartobjects.entidades.fondos.libros.LibroDeProhibiciones
import co.smartobjects.entidades.fondos.libros.Prohibicion
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.combinadores.fondos.libros.crearFiltroSoloLibrosDeProhibiciones
import co.smartobjects.persistencia.fondos.combinadores.fondos.libros.darFiltroSobreTipoDeLibro
import com.j256.ormlite.field.SqlType

interface RepositorioLibrosDeProhibiciones
    : CreadorRepositorio<LibroDeProhibiciones>,
      Creable<LibroDeProhibiciones>,
      Listable<LibroDeProhibiciones>,
      Buscable<LibroDeProhibiciones, Long>,
      Actualizable<LibroDeProhibiciones, Long>,
      EliminablePorId<LibroDeProhibiciones, Long>



private typealias RelacionDaoLibroYProhibiciones = EntidadRelacionUnoAMuchos<LibroDAO, ProhibicionDAO>

private val extractorIdLibroDao = object : Transformador<LibroDAO, Long?>
{
    override fun transformar(origen: LibroDAO): Long?
    {
        return origen.id!!
    }
}

private val transformadorDeLibroPreciosARelacionDaoLibroYProhibicionesCreacion = object
    : TransformadorEntidadCliente<LibroDeProhibiciones, RelacionDaoLibroYProhibiciones>
{
    override fun transformar(idCliente: Long, origen: LibroDeProhibiciones): RelacionDaoLibroYProhibiciones
    {
        return EntidadRelacionUnoAMuchos(
                LibroDAO(origen),
                origen.prohibicionesDeFondo.map { ProhibicionDAO(null, it) }
                +
                origen.prohibicionesDePaquete.map { ProhibicionDAO(null, it) }
                                        )
    }
}

private val transformadorDeRelacionDaoLibroYProhibicionesALibroDeProhibiciones = object
    : TransformadorEntidadCliente<RelacionDaoLibroYProhibiciones, LibroDeProhibiciones>
{
    override fun transformar(idCliente: Long, origen: RelacionDaoLibroYProhibiciones): LibroDeProhibiciones
    {
        return origen.entidadOrigen.aEntidadDeNegocio(idCliente, origen.entidadDestino.toSet())
    }
}

private val transformadorAsignarLibroAProhibicion = object
    : TransformadorEntidadesRelacionadas<LibroDAO, ProhibicionDAO>
{
    override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: LibroDAO, entidadDestino: ProhibicionDAO): ProhibicionDAO
    {
        return entidadDestino.copy(libro = entidadOrigen)
    }
}

private val transformadorDeLibroPreciosARelacionDaoLibroYProhibicionesActualizacion =
        object : Transformador<LibroDeProhibiciones, RelacionDaoLibroYProhibiciones>
        {
            override fun transformar(origen: LibroDeProhibiciones): RelacionDaoLibroYProhibiciones
            {
                return EntidadRelacionUnoAMuchos(
                        LibroDAO(origen),
                        origen.prohibicionesDeFondo.map { ProhibicionDAO(origen.id, it) }
                        +
                        origen.prohibicionesDePaquete.map { ProhibicionDAO(origen.id, it) }
                                                )
            }
        }

private val extractorLibroDaoDeRelacionDaoLibroYProhibiciones = object : Transformador<RelacionDaoLibroYProhibiciones, LibroDAO>
{
    override fun transformar(origen: RelacionDaoLibroYProhibiciones): LibroDAO
    {
        return origen.entidadOrigen
    }
}

private val transformadorLibroDaoARelacionDaoLibroSinProhibiciones = object : TransformadorEntidadCliente<LibroDAO, RelacionDaoLibroYProhibiciones>
{
    override fun transformar(idCliente: Long, origen: LibroDAO): RelacionDaoLibroYProhibiciones
    {
        // Los precios que se pongan acá no importan porque se completan con ActualizableParcialCompuesto
        return EntidadRelacionUnoAMuchos(origen, emptyList())
    }
}

private val extractorDePreciosDeRelacionDaoLibroYProhibiciones = object : Transformador<RelacionDaoLibroYProhibiciones, List<ProhibicionDAO>>
{
    override fun transformar(origen: RelacionDaoLibroYProhibiciones): List<ProhibicionDAO>
    {
        return origen.entidadDestino
    }
}

private val asignadorDePreciosARelacionDaoLibroYProhibiciones = object : AsignadorParametro<RelacionDaoLibroYProhibiciones, List<ProhibicionDAO>>
{
    override fun asignarParametro(entidad: RelacionDaoLibroYProhibiciones, parametro: List<ProhibicionDAO>): RelacionDaoLibroYProhibiciones
    {
        return entidad.copy(entidadDestino = parametro)
    }
}

class RepositorioLibrosDeProhibicionesSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<LibroDeProhibiciones>,
        private val creador: Creable<LibroDeProhibiciones>,
        private val listador: Listable<LibroDeProhibiciones>,
        private val buscador: Buscable<LibroDeProhibiciones, Long>,
        private val actualizador: Actualizable<LibroDeProhibiciones, Long>,
        private val eliminador: EliminablePorId<LibroDeProhibiciones, Long>)
    : CreadorRepositorio<LibroDeProhibiciones> by creadorRepositorio,
      Creable<LibroDeProhibiciones> by creador,
      Listable<LibroDeProhibiciones> by listador,
      Buscable<LibroDeProhibiciones, Long> by buscador,
      Actualizable<LibroDeProhibiciones, Long> by actualizador,
      EliminablePorId<LibroDeProhibiciones, Long> by eliminador,
      RepositorioLibrosDeProhibiciones
{
    companion object
    {
        private val NOMBRE_ENTIDAD = LibroDeProhibiciones.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<LibroDeProhibiciones>,
            creador: Creable<LibroDeProhibiciones>,
            listador: ListableFiltrableOrdenable<LibroDeProhibiciones>,
            actualizador: Actualizable<LibroDeProhibiciones, Long>,
            eliminador: EliminablePorId<LibroDeProhibiciones, Long>
                       )
            : this(
            creadorRepositorio,
            creador,
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(LibroDAO.TABLA, LibroDAO.COLUMNA_ID), SqlType.LONG),
            actualizador,
            eliminador
                  )

    private constructor(
            parametrosLibroDAO: ParametrosParaDAOEntidadDeCliente<LibroDAO, Long?>,
            parametrosProhibicionDAO: ParametrosParaDAOEntidadDeCliente<ProhibicionDAO, Long?>
                       )
            : this(
            CreadorUnicaVez
            (
                    CreadorRepositorioCompuesto
                    (
                            listOf(
                                    CreadorRepositorioDAO(parametrosLibroDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosProhibicionDAO, NOMBRE_ENTIDAD)
                                  ),
                            NOMBRE_ENTIDAD
                    )
            ),
            CreableEnTransaccionSQL<LibroDeProhibiciones>
            (
                    parametrosLibroDAO.configuracion,
                    CreableConTransformacion<LibroDeProhibiciones, RelacionDaoLibroYProhibiciones>
                    (
                            CreableDAORelacionUnoAMuchos(
                                    CreableDAO(parametrosLibroDAO, NOMBRE_ENTIDAD),
                                    CreableDAOMultiples(parametrosProhibicionDAO, NOMBRE_ENTIDAD),
                                    transformadorAsignarLibroAProhibicion
                                                        ),
                            transformadorDeLibroPreciosARelacionDaoLibroYProhibicionesCreacion,
                            transformadorDeRelacionDaoLibroYProhibicionesALibroDeProhibiciones
                    )
            ),
            ListableConTransaccion
            (
                    parametrosLibroDAO.configuracion,
                    NOMBRE_ENTIDAD,
                    ListableConTransformacion<RelacionDaoLibroYProhibiciones, LibroDeProhibiciones>
                    (
                            ListableUnoAMuchos
                            (
                                    ListableInnerJoin
                                    (
                                            ListableSQLConFiltrosSQL
                                            (
                                                    ListableDAO(listOf(LibroDAO.COLUMNA_ID), parametrosLibroDAO),
                                                    darFiltroSobreTipoDeLibro(LibroDAO.TipoEnBD.PROHIBICIONES)
                                            ),
                                            ListableDAO(listOf(ProhibicionDAO.COLUMNA_ID), parametrosProhibicionDAO)
                                    ),
                                    extractorIdLibroDao
                            ),
                            transformadorDeRelacionDaoLibroYProhibicionesALibroDeProhibiciones
                    )
            ),
            ActualizableEnTransaccionSQL
            (
                    parametrosLibroDAO.configuracion,
                    ActualizableConTransformacion<LibroDeProhibiciones, Long?, RelacionDaoLibroYProhibiciones, Long?>
                    (
                            ActualizableEntidadRelacionUnoAMuchosClonandoHijos
                            (
                                    ActualizableParcialCompuesto<RelacionDaoLibroYProhibiciones, Long?, List<ProhibicionDAO>>
                                    (
                                            ActualizableConTransformacion<RelacionDaoLibroYProhibiciones, Long?, LibroDAO, Long?>
                                            (
                                                    ActualizableDAOConFiltrosIgualdad(parametrosLibroDAO, NOMBRE_ENTIDAD, crearFiltroSoloLibrosDeProhibiciones()),
                                                    TransformadorIdentidad(),
                                                    extractorLibroDaoDeRelacionDaoLibroYProhibiciones,
                                                    transformadorLibroDaoARelacionDaoLibroSinProhibiciones
                                            ),
                                            extractorDePreciosDeRelacionDaoLibroYProhibiciones,
                                            asignadorDePreciosARelacionDaoLibroYProhibiciones
                                    ),
                                    ProhibicionDAO.COLUMNA_ID_LIBRO,
                                    ActualizableEntidadRelacionUnoAMuchosClonandoHijos.HijoClonable
                                    (
                                            ProhibicionDAO.TABLA,
                                            EliminableDao(Prohibicion.NOMBRE_ENTIDAD, parametrosProhibicionDAO),
                                            SqlType.LONG,
                                            CreableDAOMultiples(parametrosProhibicionDAO, Prohibicion.NOMBRE_ENTIDAD),
                                            extractorDePreciosDeRelacionDaoLibroYProhibiciones,
                                            asignadorDePreciosARelacionDaoLibroYProhibiciones
                                    )
                            ),
                            TransformadorIdentidad(),
                            transformadorDeLibroPreciosARelacionDaoLibroYProhibicionesActualizacion,
                            transformadorDeRelacionDaoLibroYProhibicionesALibroDeProhibiciones
                    )
            ),
            EliminablePorIdEnTransaccionSQL
            (
                    parametrosLibroDAO.configuracion,
                    EliminableSimple
                    (
                            EliminableDao(LibroDeProhibiciones.NOMBRE_ENTIDAD, parametrosLibroDAO),
                            TransformadorIdentidad()
                    )
            )
                  )

    constructor (configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, LibroDAO.TABLA, LibroDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, ProhibicionDAO.TABLA, ProhibicionDAO::class.java)
                  )
}