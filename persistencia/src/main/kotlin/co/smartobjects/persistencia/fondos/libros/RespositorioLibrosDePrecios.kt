@file:Suppress("NOTHING_TO_INLINE")

package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroDePrecios
import co.smartobjects.entidades.fondos.libros.PrecioEnLibro
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.combinadores.fondos.libros.crearFiltroSoloLibrosDePrecio
import co.smartobjects.persistencia.fondos.combinadores.fondos.libros.darFiltroSobreTipoDeLibro
import com.j256.ormlite.field.SqlType

interface RepositorioLibrosDePrecios
    : CreadorRepositorio<LibroDePrecios>,
      Creable<LibroDePrecios>,
      Listable<LibroDePrecios>,
      Buscable<LibroDePrecios, Long>,
      Actualizable<LibroDePrecios, Long>,
      EliminablePorId<LibroDePrecios, Long>



private typealias RelacionDaoLibroYPrecios = EntidadRelacionUnoAMuchos<LibroDAO, PrecioDeFondoDAO>

private val extractorIdLibroDao = object : Transformador<LibroDAO, Long?>
{
    override fun transformar(origen: LibroDAO): Long?
    {
        return origen.id!!
    }
}

private val transformadorDeLibroPreciosARelacionDaoLibroYPreciosCreacion = object
    : TransformadorEntidadCliente<LibroDePrecios, RelacionDaoLibroYPrecios>
{
    override fun transformar(idCliente: Long, origen: LibroDePrecios): RelacionDaoLibroYPrecios
    {
        return EntidadRelacionUnoAMuchos(LibroDAO(origen), origen.precios.map { PrecioDeFondoDAO(null, it) })
    }
}

private val transformadorDeRelacionDaoLibroYPreciosALibroDePrecios = object
    : TransformadorEntidadCliente<RelacionDaoLibroYPrecios, LibroDePrecios>
{
    override fun transformar(idCliente: Long, origen: RelacionDaoLibroYPrecios): LibroDePrecios
    {
        return origen.entidadOrigen.aEntidadDeNegocio(idCliente, origen.entidadDestino.toSet())
    }
}

private val transformadorAsignarRelacionLibroAPrecioParaFondo = object
    : TransformadorEntidadesRelacionadas<LibroDAO, PrecioDeFondoDAO>
{
    override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: LibroDAO, entidadDestino: PrecioDeFondoDAO): PrecioDeFondoDAO
    {
        return entidadDestino.copy(libro = entidadOrigen)
    }
}

private val transformadorDeLibroPreciosARelacionDaoLibroYPreciosActualizacion =
        object : Transformador<LibroDePrecios, RelacionDaoLibroYPrecios>
        {
            override fun transformar(origen: LibroDePrecios): RelacionDaoLibroYPrecios
            {
                return EntidadRelacionUnoAMuchos(LibroDAO(origen), origen.precios.map { PrecioDeFondoDAO(origen.id, it) })
            }
        }

private val extractorLibroDaoDeRelacionDaoLibroYPrecios = object : Transformador<RelacionDaoLibroYPrecios, LibroDAO>
{
    override fun transformar(origen: RelacionDaoLibroYPrecios): LibroDAO
    {
        return origen.entidadOrigen
    }
}

private val transformadorLibroDaoARelacionDaoLibroSinPrecios = object : TransformadorEntidadCliente<LibroDAO, RelacionDaoLibroYPrecios>
{
    override fun transformar(idCliente: Long, origen: LibroDAO): RelacionDaoLibroYPrecios
    {
        // Los precios que se pongan acá no importan porque se completan con ActualizableParcialCompuesto
        return EntidadRelacionUnoAMuchos(origen, emptyList())
    }
}

private val extractorDePreciosDeRelacionDaoLibroYPrecios = object : Transformador<RelacionDaoLibroYPrecios, List<PrecioDeFondoDAO>>
{
    override fun transformar(origen: RelacionDaoLibroYPrecios): List<PrecioDeFondoDAO>
    {
        return origen.entidadDestino
    }
}

private val asignadorDePreciosARelacionDaoLibroYPrecios = object : AsignadorParametro<RelacionDaoLibroYPrecios, List<PrecioDeFondoDAO>>
{
    override fun asignarParametro(entidad: RelacionDaoLibroYPrecios, parametro: List<PrecioDeFondoDAO>): RelacionDaoLibroYPrecios
    {
        return entidad.copy(entidadDestino = parametro)
    }
}

class RepositorioLibrosDePreciosSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<LibroDePrecios>,
        private val creador: Creable<LibroDePrecios>,
        private val listador: Listable<LibroDePrecios>,
        private val buscador: Buscable<LibroDePrecios, Long>,
        private val actualizador: Actualizable<LibroDePrecios, Long>,
        private val eliminador: EliminablePorId<LibroDePrecios, Long>)
    : CreadorRepositorio<LibroDePrecios> by creadorRepositorio,
      Creable<LibroDePrecios> by creador,
      Listable<LibroDePrecios> by listador,
      Buscable<LibroDePrecios, Long> by buscador,
      Actualizable<LibroDePrecios, Long> by actualizador,
      EliminablePorId<LibroDePrecios, Long> by eliminador,
      RepositorioLibrosDePrecios
{
    companion object
    {
        private val NOMBRE_ENTIDAD = LibroDePrecios.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<LibroDePrecios>,
            creador: Creable<LibroDePrecios>,
            listador: ListableFiltrableOrdenable<LibroDePrecios>,
            actualizador: Actualizable<LibroDePrecios, Long>,
            eliminador: EliminablePorId<LibroDePrecios, Long>
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
            parametrosPrecioDeFondoDAO: ParametrosParaDAOEntidadDeCliente<PrecioDeFondoDAO, Long?>
                       )
            : this(
            CreadorUnicaVez
            (
                    CreadorRepositorioCompuesto
                    (
                            listOf(
                                    CreadorRepositorioDAO(parametrosLibroDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosPrecioDeFondoDAO, NOMBRE_ENTIDAD)
                                  ),
                            NOMBRE_ENTIDAD
                    )
            ),
            CreableEnTransaccionSQL<LibroDePrecios>
            (
                    parametrosLibroDAO.configuracion,
                    CreableConTransformacion<LibroDePrecios, RelacionDaoLibroYPrecios>
                    (
                            CreableDAORelacionUnoAMuchos(
                                    CreableDAO(parametrosLibroDAO, NOMBRE_ENTIDAD),
                                    CreableDAOMultiples(parametrosPrecioDeFondoDAO, NOMBRE_ENTIDAD),
                                    transformadorAsignarRelacionLibroAPrecioParaFondo
                                                        ),
                            transformadorDeLibroPreciosARelacionDaoLibroYPreciosCreacion,
                            transformadorDeRelacionDaoLibroYPreciosALibroDePrecios
                    )
            ),
            ListableConTransaccion
            (
                    parametrosLibroDAO.configuracion,
                    NOMBRE_ENTIDAD,
                    ListableConTransformacion<RelacionDaoLibroYPrecios, LibroDePrecios>
                    (
                            ListableUnoAMuchos
                            (
                                    ListableInnerJoin
                                    (
                                            ListableSQLConFiltrosSQL
                                            (
                                                    ListableDAO(listOf(LibroDAO.COLUMNA_ID), parametrosLibroDAO),
                                                    darFiltroSobreTipoDeLibro(LibroDAO.TipoEnBD.PRECIOS)
                                            ),
                                            ListableDAO(listOf(PrecioDeFondoDAO.COLUMNA_ID), parametrosPrecioDeFondoDAO)
                                    ),
                                    extractorIdLibroDao
                            ),
                            transformadorDeRelacionDaoLibroYPreciosALibroDePrecios
                    )
            ),
            ActualizableEnTransaccionSQL
            (
                    parametrosLibroDAO.configuracion,
                    ActualizableConTransformacion<LibroDePrecios, Long?, RelacionDaoLibroYPrecios, Long?>
                    (
                            ActualizableEntidadRelacionUnoAMuchosClonandoHijos
                            (
                                    ActualizableParcialCompuesto<RelacionDaoLibroYPrecios, Long?, List<PrecioDeFondoDAO>>
                                    (
                                            ActualizableConTransformacion<RelacionDaoLibroYPrecios, Long?, LibroDAO, Long?>
                                            (
                                                    ActualizableDAOConFiltrosIgualdad(parametrosLibroDAO, NOMBRE_ENTIDAD, crearFiltroSoloLibrosDePrecio()),
                                                    TransformadorIdentidad(),
                                                    extractorLibroDaoDeRelacionDaoLibroYPrecios,
                                                    transformadorLibroDaoARelacionDaoLibroSinPrecios
                                            ),
                                            extractorDePreciosDeRelacionDaoLibroYPrecios,
                                            asignadorDePreciosARelacionDaoLibroYPrecios
                                    ),
                                    PrecioDeFondoDAO.COLUMNA_ID_LIBRO,
                                    ActualizableEntidadRelacionUnoAMuchosClonandoHijos.HijoClonable
                                    (
                                            PrecioDeFondoDAO.TABLA,
                                            EliminableDao(PrecioEnLibro.NOMBRE_ENTIDAD, parametrosPrecioDeFondoDAO),
                                            SqlType.LONG,
                                            CreableDAOMultiples(parametrosPrecioDeFondoDAO, PrecioEnLibro.NOMBRE_ENTIDAD),
                                            extractorDePreciosDeRelacionDaoLibroYPrecios,
                                            asignadorDePreciosARelacionDaoLibroYPrecios
                                    )
                            ),
                            TransformadorIdentidad(),
                            transformadorDeLibroPreciosARelacionDaoLibroYPreciosActualizacion,
                            transformadorDeRelacionDaoLibroYPreciosALibroDePrecios
                    )
            ),
            EliminablePorIdEnTransaccionSQL
            (
                    parametrosLibroDAO.configuracion,
                    EliminableSimple
                    (
                            EliminableDao(LibroDePrecios.NOMBRE_ENTIDAD, parametrosLibroDAO),
                            TransformadorIdentidad()
                    )
            )
                  )

    constructor (configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, LibroDAO.TABLA, LibroDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PrecioDeFondoDAO.TABLA, PrecioDeFondoDAO::class.java)
                  )
}