package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.LibroSegunReglasCompleto
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import com.j256.ormlite.field.SqlType


interface RepositorioLibrosSegunReglasCompleto
    : CreadorRepositorio<LibroSegunReglasCompleto<*>>,
      Listable<LibroSegunReglasCompleto<*>>,
      Buscable<LibroSegunReglasCompleto<*>, Long>

private typealias RelacionLibroYPrecios = EntidadRelacionUnoAUno<LibroDAO, PrecioDeFondoDAO?>
private typealias RelacionLibroYPreciosOProhibiciones = EntidadRelacionUnoAUno<RelacionLibroYPrecios, ProhibicionDAO?>
private typealias RelacionLibroSegunReglasYLibros = EntidadRelacionUnoAMuchos<LibroSegunReglasDAO, RelacionLibroYPreciosOProhibiciones>
private typealias RelacionAgrupacionLibrosYReglas = EntidadRelacionUnoAUno<RelacionLibroSegunReglasYLibros?, LibroSegunReglasEnBD?>

private val verificadorIdPrecioDeFondoDAONulo = object : Transformador<PrecioDeFondoDAO, Boolean>
{
    override fun transformar(origen: PrecioDeFondoDAO): Boolean
    {
        return origen.id == null
    }
}

private val verificadorIdProhibicionDAONulo = object : Transformador<ProhibicionDAO, Boolean>
{
    override fun transformar(origen: ProhibicionDAO): Boolean
    {
        return origen.id == null
    }
}


private val extractorIdPrecioSegunReglasDAO = object : Transformador<LibroSegunReglasDAO, Long?>
{
    override fun transformar(origen: LibroSegunReglasDAO): Long?
    {
        return origen.id
    }
}

private val comparadorIdsLibrosSegunReglasDAO =
        object : ComparadorEntidades<EntidadRelacionUnoAUno<LibroSegunReglasDAO, List<RelacionLibroYPreciosOProhibiciones>>, EntidadRelacionUnoAUno<LibroSegunReglasDAO, List<ReglaDAO>>>
        {
            override fun comparar(origenIzquierda: EntidadRelacionUnoAUno<LibroSegunReglasDAO, List<RelacionLibroYPreciosOProhibiciones>>, origenDerecha: EntidadRelacionUnoAUno<LibroSegunReglasDAO, List<ReglaDAO>>): Int
            {
                val libroIzquierdo = origenIzquierda.entidadOrigen
                val libroDerecho = origenDerecha.entidadOrigen

                return libroIzquierdo.id!!.compareTo(libroDerecho.id!!)
            }
        }

private val verificadorIdReglaDAONulo = object : Transformador<ReglaDAO, Boolean>
{
    override fun transformar(origen: ReglaDAO): Boolean
    {
        return origen.id == null
    }
}

private val transformadorDeAgrupacionLibrosYReglasALibroSegunReglas = object : TransformadorEntidadCliente<RelacionAgrupacionLibrosYReglas, LibroSegunReglasCompleto<*>>
{
    override fun transformar(idCliente: Long, origen: RelacionAgrupacionLibrosYReglas): LibroSegunReglasCompleto<*>
    {
        val relacionLibrosSegunReglasYLibros = origen.entidadOrigen!!
        val libroSegunReglasDAO = relacionLibrosSegunReglasYLibros.entidadOrigen

        val libroDao = relacionLibrosSegunReglasYLibros.entidadDestino.firstOrNull()?.entidadOrigen?.entidadOrigen
                       ?: throw IllegalStateException("Estado en base de datos inconsistente para libros")

        val caracteristicasPropiasDeLibros =
                relacionLibrosSegunReglasYLibros.entidadDestino
                    .asSequence()
                    .map {
                        Pair(it.entidadOrigen.entidadDestino, it.entidadDestino)
                    }

        val reglasDao = origen.entidadDestino?.entidadDestino ?: emptyList()
        val libroSegunReglas = libroSegunReglasDAO.aEntidadDeNegocioConReglas(idCliente, reglasDao)

        return when (libroDao.tipo)
        {
            LibroDAO.TipoEnBD.PRECIOS       ->
            {
                val libroDePrecios = libroDao.aEntidadDeNegocio(idCliente, caracteristicasPropiasDeLibros.map { it.first!! }.toSet())
                LibroSegunReglasCompleto(libroSegunReglas, libroDePrecios)
            }
            LibroDAO.TipoEnBD.PROHIBICIONES ->
            {
                val libroDeProhibiciones = libroDao.aEntidadDeNegocio(idCliente, caracteristicasPropiasDeLibros.map { it.second!! }.toSet())
                LibroSegunReglasCompleto(libroSegunReglas, libroDeProhibiciones)
            }
            LibroDAO.TipoEnBD.DESCONOCIDO   ->
            {
                throw IllegalStateException("Estado en base de datos inconsistente para libros")
            }
        }
    }
}

class RepositorioLibrosSegunReglasCompletoSQL(
        private val creadorRepositorio: CreadorRepositorio<LibroSegunReglasCompleto<*>>,
        private val listador: Listable<LibroSegunReglasCompleto<*>>,
        private val buscador: Buscable<LibroSegunReglasCompleto<*>, Long>)
    : CreadorRepositorio<LibroSegunReglasCompleto<*>> by creadorRepositorio,
      Listable<LibroSegunReglasCompleto<*>> by listador,
      Buscable<LibroSegunReglasCompleto<*>, Long> by buscador,
      RepositorioLibrosSegunReglasCompleto
{
    companion object
    {
        private val NOMBRE_ENTIDAD = LibroSegunReglasCompleto.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<LibroSegunReglasCompleto<*>>,
            listador: ListableFiltrableOrdenable<LibroSegunReglasCompleto<*>>
                       )
            : this(
            creadorRepositorio,
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(LibroSegunReglasDAO.TABLA, LibroSegunReglasDAO.COLUMNA_ID), SqlType.LONG)
                  )

    private constructor(
            parametrosLibroDAO: ParametrosParaDAOEntidadDeCliente<LibroDAO, Long?>,
            parametrosLibroSegunReglasCompletoDAO: ParametrosParaDAOEntidadDeCliente<LibroSegunReglasDAO, Long?>,
            parametrosReglaDAO: ParametrosParaDAOEntidadDeCliente<ReglaDAO, Long?>,
            parametrosPrecioDeFondoDAO: ParametrosParaDAOEntidadDeCliente<PrecioDeFondoDAO, Long?>,
            parametrosProhibicionDAO: ParametrosParaDAOEntidadDeCliente<ProhibicionDAO, Long?>
                       )
            : this(
            CreadorUnicaVez<LibroSegunReglasCompleto<*>>
            (
                    CreadorRepositorioCompuesto<LibroSegunReglasCompleto<*>>
                    (
                            listOf(
                                    CreadorRepositorioDAO(parametrosLibroDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosLibroSegunReglasCompletoDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosReglaDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosPrecioDeFondoDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosProhibicionDAO, NOMBRE_ENTIDAD)
                                  ),
                            NOMBRE_ENTIDAD
                    )
            ),
            ListableConTransaccion
            (
                    parametrosLibroSegunReglasCompletoDAO.configuracion,
                    NOMBRE_ENTIDAD,
                    ListableConTransformacion<
                            EntidadRelacionUnoAUno<
                                    RelacionLibroSegunReglasYLibros?,
                                    LibroSegunReglasEnBD?
                                    >
                            , LibroSegunReglasCompleto<*>
                            >
                    (
                            ListableAgrupandoConsultas<
                                    LibroSegunReglasDAO
                                    , List<RelacionLibroYPreciosOProhibiciones>
                                    , LibroSegunReglasDAO
                                    , List<ReglaDAO>
                                    >
                            (
                                    ListableUnoAMuchos<LibroSegunReglasDAO, RelacionLibroYPreciosOProhibiciones, Long?>
                                    (
                                            ListableInnerJoin<LibroSegunReglasDAO, RelacionLibroYPreciosOProhibiciones>
                                            (
                                                    ListableDAO(listOf(LibroSegunReglasDAO.COLUMNA_ID), parametrosLibroSegunReglasCompletoDAO),
                                                    ListableLeftJoin<RelacionLibroYPrecios, ProhibicionDAO>
                                                    (
                                                            ListableLeftJoin<LibroDAO, PrecioDeFondoDAO>
                                                            (
                                                                    ListableDAO(listOf(LibroDAO.COLUMNA_ID), parametrosLibroDAO),
                                                                    ListableDAO(listOf(PrecioDeFondoDAO.COLUMNA_ID), parametrosPrecioDeFondoDAO),
                                                                    verificadorIdPrecioDeFondoDAONulo
                                                            ),
                                                            ListableDAO(listOf(ProhibicionDAO.COLUMNA_ID), parametrosProhibicionDAO),
                                                            verificadorIdProhibicionDAONulo
                                                    )
                                            ),
                                            extractorIdPrecioSegunReglasDAO
                                    ),
                                    ListableUnoAMuchos<LibroSegunReglasDAO, ReglaDAO, Long?>
                                    (
                                            ListableLeftJoin<LibroSegunReglasDAO, ReglaDAO>
                                            (
                                                    ListableDAO(listOf(LibroSegunReglasDAO.COLUMNA_ID), parametrosLibroSegunReglasCompletoDAO),
                                                    ListableDAO(listOf(ReglaDAO.COLUMNA_ID), parametrosReglaDAO),
                                                    verificadorIdReglaDAONulo
                                            ),
                                            extractorIdPrecioSegunReglasDAO
                                    ),
                                    comparadorIdsLibrosSegunReglasDAO
                            ),
                            transformadorDeAgrupacionLibrosYReglasALibroSegunReglas
                    )
            )
                  )


    constructor (configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, LibroDAO.TABLA, LibroDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, LibroSegunReglasDAO.TABLA, LibroSegunReglasDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, ReglaDAO.TABLA, ReglaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PrecioDeFondoDAO.TABLA, PrecioDeFondoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, ProhibicionDAO.TABLA, ProhibicionDAO::class.java)
                  )
}