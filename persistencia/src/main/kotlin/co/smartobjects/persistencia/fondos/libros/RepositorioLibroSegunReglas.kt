package co.smartobjects.persistencia.fondos.libros

import co.smartobjects.entidades.fondos.libros.*
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.EntidadNoExiste
import co.smartobjects.persistencia.excepciones.ErrorActualizacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.ErrorCreacionViolacionDeRestriccion
import com.j256.ormlite.field.SqlType


interface RepositorioLibrosSegunReglas
    : CreadorRepositorio<LibroSegunReglas>,
      Creable<LibroSegunReglas>,
      Listable<LibroSegunReglas>,
      Buscable<LibroSegunReglas, Long>,
      Actualizable<LibroSegunReglas, Long>,
      EliminablePorId<LibroSegunReglas, Long>


internal typealias LibroSegunReglasEnBD = EntidadRelacionUnoAMuchos<LibroSegunReglasDAO, ReglaDAO>

private val transformadorDeLibroPreciosARelacionDaoLibroYReglasCreacion =
        object : TransformadorEntidadCliente<LibroSegunReglas, LibroSegunReglasEnBD>
        {
            override fun transformar(idCliente: Long, origen: LibroSegunReglas): LibroSegunReglasEnBD
            {
                val reglasDeIdUbicacion = origen.reglasIdUbicacion.asSequence().map { ReglaDAO(origen, it) }
                val reglasDeIdGrupoDeClientes = origen.reglasIdGrupoDeClientes.asSequence().map { ReglaDAO(origen, it) }
                val reglasDeIdPaquete = origen.reglasIdPaquete.asSequence().map { ReglaDAO(origen, it) }

                return EntidadRelacionUnoAMuchos(
                        LibroSegunReglasDAO(origen),
                        (reglasDeIdUbicacion + reglasDeIdGrupoDeClientes + reglasDeIdPaquete).toList()
                                                )
            }
        }

private val transformadorDeRelacionDaoLibroYReglasALibroSegunReglas =
        object : TransformadorEntidadCliente<LibroSegunReglasEnBD, LibroSegunReglas>
        {
            override fun transformar(idCliente: Long, origen: LibroSegunReglasEnBD): LibroSegunReglas
            {
                return origen.entidadOrigen.aEntidadDeNegocioConReglas(idCliente, origen.entidadDestino.toList())
            }
        }

private val extractorIdPrecioSegunReglasDAO = object : Transformador<LibroSegunReglasDAO, Long?>
{
    override fun transformar(origen: LibroSegunReglasDAO): Long?
    {
        return origen.id
    }
}

private val verificadorIdReglaDAONulo = object : Transformador<ReglaDAO, Boolean>
{
    override fun transformar(origen: ReglaDAO): Boolean
    {
        return origen.id == null
    }
}

private val transformadorDeLibrosYReglasALibroSegunReglas = object : TransformadorEntidadCliente<LibroSegunReglasEnBD, LibroSegunReglas>
{
    override fun transformar(idCliente: Long, origen: LibroSegunReglasEnBD): LibroSegunReglas
    {
        val libroSegunReglasDAO = origen.entidadOrigen
        val reglasDao = origen.entidadDestino

        return libroSegunReglasDAO.aEntidadDeNegocioConReglas(idCliente, reglasDao)
    }
}

private val extractorLibroSegunReglasDAODeRelacionLibroReglas = object : Transformador<LibroSegunReglasEnBD, LibroSegunReglasDAO>
{
    override fun transformar(origen: LibroSegunReglasEnBD): LibroSegunReglasDAO
    {
        return origen.entidadOrigen
    }
}

private val transformarLibroSegunReglasDaoARelacionLibroReglas = object : TransformadorEntidadCliente<LibroSegunReglasDAO, LibroSegunReglasEnBD>
{
    override fun transformar(idCliente: Long, origen: LibroSegunReglasDAO): LibroSegunReglasEnBD
    {
        return EntidadRelacionUnoAMuchos(origen, emptyList())
    }
}

private val extraerReglasDeRelacionLibroReglas = object : Transformador<LibroSegunReglasEnBD, List<ReglaDAO>>
{
    override fun transformar(origen: LibroSegunReglasEnBD): List<ReglaDAO>
    {
        return origen.entidadDestino
    }
}

private val asignadorReglasARelcionLibroReglas = object : AsignadorParametro<LibroSegunReglasEnBD, List<ReglaDAO>>
{
    override fun asignarParametro(entidad: LibroSegunReglasEnBD, parametro: List<ReglaDAO>): LibroSegunReglasEnBD
    {
        return entidad.copy(entidadDestino = parametro)
    }
}

private val transformarLibroSegunReglasARelacionLibroReglas = object : Transformador<LibroSegunReglas, LibroSegunReglasEnBD>
{
    override fun transformar(origen: LibroSegunReglas): LibroSegunReglasEnBD
    {
        val reglasDao =
                origen.reglasIdUbicacion.asSequence().map { ReglaDAO(origen, it) } +
                origen.reglasIdGrupoDeClientes.asSequence().map { ReglaDAO(origen, it) } +
                origen.reglasIdPaquete.asSequence().map { ReglaDAO(origen, it) }

        return EntidadRelacionUnoAMuchos(LibroSegunReglasDAO(origen), reglasDao.toList())
    }
}

private fun existeLibroSegunReglasConMismasReglasParaMismoLibro(
        parametrosLibroSegunReglasDAO: ParametrosParaDAOEntidadDeCliente<LibroSegunReglasDAO, Long?>,
        parametrosReglaDAO: ParametrosParaDAOEntidadDeCliente<ReglaDAO, Long?>,
        idCliente: Long,
        idLibro: Long,
        reglas: List<Regla<*>>,
        idLibroSegunReglas: Long?): Boolean
{
    val queryLibrosConReglas = parametrosLibroSegunReglasDAO[idCliente].dao.queryBuilder()
    val queryDeReglas = parametrosReglaDAO[idCliente].dao.queryBuilder()

    val columnaIdLibroSegunReglas = CampoTabla(LibroSegunReglasDAO.TABLA, LibroSegunReglasDAO.COLUMNA_ID)

    val columnaFkLibroSegunReglas = CampoTabla(ReglaDAO.TABLA, ReglaDAO.COLUMNA_ID_LIBRO_SEGUN_REGLAS)

    queryLibrosConReglas
        .selectRaw(columnaIdLibroSegunReglas.nombreColumna)
        .selectRaw("COUNT(${columnaFkLibroSegunReglas.nombreColumna}) AS numero_de_reglas_por_libro")
        .leftJoin(queryDeReglas)

    if (reglas.isNotEmpty())
    {
        val whereReglas = queryDeReglas.where()
        reglas.forEach {
            val a = when (it)
            {
                is ReglaDeIdUbicacion       ->
                {
                    whereReglas.eq(ReglaDAO.COLUMNA_ID_UBICACION, it.restriccion)
                }
                is ReglaDeIdGrupoDeClientes ->
                {
                    whereReglas.eq(ReglaDAO.COLUMNA_ID_GRUPO_CLIENTES, it.restriccion)
                }
                is ReglaDeIdPaquete         ->
                {
                    whereReglas.eq(ReglaDAO.COLUMNA_ID_PAQUETE, it.restriccion)
                }
            }
        }
        if (reglas.size > 1)
        {
            whereReglas.or(reglas.size)
        }
    }

    val whereLibrosSegunReglas = queryLibrosConReglas.where()
    whereLibrosSegunReglas.eq(LibroSegunReglasDAO.COLUMNA_ID_LIBRO, idLibro)
    if (idLibroSegunReglas != null)
    {
        whereLibrosSegunReglas.and().ne(LibroSegunReglasDAO.COLUMNA_ID, idLibroSegunReglas)
    }

    queryLibrosConReglas.groupBy(LibroSegunReglasDAO.COLUMNA_ID)
    queryLibrosConReglas.having("COUNT(${columnaFkLibroSegunReglas.nombreColumna}) = ${reglas.size}")
    queryLibrosConReglas.limit(1)

    return queryLibrosConReglas.queryRaw().firstOrNull() != null
}

private class ValidadorReglasDiferenteParaLibroAsociadoParaCreacion(
        private val parametrosLibroDAO: ParametrosParaDAOEntidadDeCliente<LibroDAO, Long?>,
        private val parametrosLibroSegunReglasDAO: ParametrosParaDAOEntidadDeCliente<LibroSegunReglasDAO, Long?>,
        private val parametrosReglaDAO: ParametrosParaDAOEntidadDeCliente<ReglaDAO, Long?>)
    : ValidadorRestriccionCreacion<LibroSegunReglas>
{
    override fun validar(idCliente: Long, entidadACrear: LibroSegunReglas)
    {
        val reglasComoLista = entidadACrear.reglas.toList()
        val existeLibro =
                existeLibroSegunReglasConMismasReglasParaMismoLibro(
                        parametrosLibroSegunReglasDAO,
                        parametrosReglaDAO,
                        idCliente,
                        entidadACrear.idLibro,
                        reglasComoLista,
                        null
                                                                   )
        if (existeLibro)
        {
            val libroDaoAsociado = parametrosLibroDAO[idCliente].dao.queryForId(entidadACrear.idLibro)

            val nombreTipoDeLibroAsociado =
                    when (libroDaoAsociado.tipo)
                    {
                        LibroDAO.TipoEnBD.PRECIOS       -> LibroDePrecios.NOMBRE_ENTIDAD
                        LibroDAO.TipoEnBD.PROHIBICIONES -> LibroDeProhibiciones.NOMBRE_ENTIDAD
                        LibroDAO.TipoEnBD.DESCONOCIDO   -> throw IllegalStateException("Estado en base de datos inconsistente para libros")
                    }

            throw ErrorCreacionViolacionDeRestriccion(
                    LibroSegunReglas.NOMBRE_ENTIDAD,
                    "Ya existe otro $nombreTipoDeLibroAsociado con las mismas reglas",
                    reglasComoLista.map { it.toString() }.toTypedArray()
                                                     )
        }
    }
}

private class ValidadorReglasDiferenteParaLibroAsociadoParaActualizacion(
        private val parametrosLibroDAO: ParametrosParaDAOEntidadDeCliente<LibroDAO, Long?>,
        private val parametrosLibroSegunReglasDAO: ParametrosParaDAOEntidadDeCliente<LibroSegunReglasDAO, Long?>,
        private val parametrosReglaDAO: ParametrosParaDAOEntidadDeCliente<ReglaDAO, Long?>)
    : ValidadorRestriccionActualizacion<LibroSegunReglas, Long?>
{
    override fun validar(idCliente: Long, id: Long?, entidadAActualizar: LibroSegunReglas)
    {
        val reglasComoLista = entidadAActualizar.reglas.toList()
        val existeLibro =
                existeLibroSegunReglasConMismasReglasParaMismoLibro(
                        parametrosLibroSegunReglasDAO,
                        parametrosReglaDAO,
                        idCliente,
                        entidadAActualizar.idLibro,
                        reglasComoLista,
                        id
                                                                   )
        if (existeLibro)
        {
            val libroDaoAsociado = parametrosLibroDAO[idCliente].dao.queryForId(entidadAActualizar.idLibro)

            val nombreTipoDeLibroAsociado =
                    when (libroDaoAsociado.tipo)
                    {
                        LibroDAO.TipoEnBD.PRECIOS       -> LibroDePrecios.NOMBRE_ENTIDAD
                        LibroDAO.TipoEnBD.PROHIBICIONES -> LibroDeProhibiciones.NOMBRE_ENTIDAD
                        LibroDAO.TipoEnBD.DESCONOCIDO   -> throw IllegalStateException("Estado en base de datos inconsistente para libros")
                    }

            throw ErrorActualizacionViolacionDeRestriccion(
                    LibroSegunReglas.NOMBRE_ENTIDAD,
                    id.toString(),
                    "Ya existe otro $nombreTipoDeLibroAsociado con las mismas reglas",
                    reglasComoLista.map { it.toString() }.toTypedArray()
                                                          )
        }
    }
}

private class ValidarQueEntidadExiste(private val parametrosLibroSegunReglasDAO: ParametrosParaDAOEntidadDeCliente<LibroSegunReglasDAO, Long?>)
    : ValidadorRestriccionActualizacion<LibroSegunReglas, Long?>
{
    override fun validar(idCliente: Long, id: Long?, entidadAActualizar: LibroSegunReglas)
    {
        if (!parametrosLibroSegunReglasDAO[idCliente].dao.idExists(id))
        {
            throw EntidadNoExiste(id, LibroSegunReglas.NOMBRE_ENTIDAD)
        }
    }
}

class RepositorioLibrosSegunReglasSQL(
        private val creadorRepositorio: CreadorRepositorio<LibroSegunReglas>,
        private val creador: Creable<LibroSegunReglas>,
        private val listador: Listable<LibroSegunReglas>,
        private val buscador: Buscable<LibroSegunReglas, Long>,
        private val actualizador: Actualizable<LibroSegunReglas, Long>,
        private val eliminador: EliminablePorId<LibroSegunReglas, Long>)
    : CreadorRepositorio<LibroSegunReglas> by creadorRepositorio,
      Creable<LibroSegunReglas> by creador,
      Listable<LibroSegunReglas> by listador,
      Buscable<LibroSegunReglas, Long> by buscador,
      Actualizable<LibroSegunReglas, Long> by actualizador,
      EliminablePorId<LibroSegunReglas, Long> by eliminador,
      RepositorioLibrosSegunReglas
{
    companion object
    {
        private val NOMBRE_ENTIDAD = LibroSegunReglas.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<LibroSegunReglas>,
            creador: Creable<LibroSegunReglas>,
            listador: ListableFiltrableOrdenable<LibroSegunReglas>,
            actualizador: Actualizable<LibroSegunReglas, Long?>,
            eliminador: EliminablePorId<LibroSegunReglas, Long>
                       )
            : this(
            creadorRepositorio,
            creador,
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(LibroSegunReglasDAO.TABLA, LibroSegunReglasDAO.COLUMNA_ID), SqlType.LONG),
            actualizador,
            eliminador
                  )

    private constructor(
            parametrosLibroDAO: ParametrosParaDAOEntidadDeCliente<LibroDAO, Long?>,
            parametrosLibroSegunReglasDAO: ParametrosParaDAOEntidadDeCliente<LibroSegunReglasDAO, Long?>,
            parametrosReglaDAO: ParametrosParaDAOEntidadDeCliente<ReglaDAO, Long?>,
            parametrosPrecioDeFondoDAO: ParametrosParaDAOEntidadDeCliente<PrecioDeFondoDAO, Long?>,
            parametrosProhibicionDAO: ParametrosParaDAOEntidadDeCliente<ProhibicionDAO, Long?>
                       )
            : this(
            CreadorUnicaVez<LibroSegunReglas>
            (
                    CreadorRepositorioCompuesto<LibroSegunReglas>
                    (
                            listOf(
                                    CreadorRepositorioDAO(parametrosLibroDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosLibroSegunReglasDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosReglaDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosPrecioDeFondoDAO, NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosProhibicionDAO, NOMBRE_ENTIDAD)
                                  ),
                            NOMBRE_ENTIDAD
                    )
            ),
            CreableEnTransaccionSQL<LibroSegunReglas>
            (
                    parametrosLibroDAO.configuracion,
                    CreableConRestriccion
                    (
                            CreableConTransformacion
                            (
                                    CreableDAORelacionUnoAMuchos(
                                            CreableDAO(parametrosLibroSegunReglasDAO, NOMBRE_ENTIDAD),
                                            CreableDAOMultiples(parametrosReglaDAO, NOMBRE_ENTIDAD),
                                            object : TransformadorEntidadesRelacionadas<LibroSegunReglasDAO, ReglaDAO>
                                            {
                                                override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: LibroSegunReglasDAO, entidadDestino: ReglaDAO): ReglaDAO
                                                {
                                                    return entidadDestino.copy(libroSegunReglas = entidadOrigen)
                                                }
                                            }
                                                                ),
                                    transformadorDeLibroPreciosARelacionDaoLibroYReglasCreacion,
                                    transformadorDeRelacionDaoLibroYReglasALibroSegunReglas
                            ),
                            ValidadorReglasDiferenteParaLibroAsociadoParaCreacion(parametrosLibroDAO, parametrosLibroSegunReglasDAO, parametrosReglaDAO)
                    )
            ),
            ListableConTransaccion
            (
                    parametrosLibroSegunReglasDAO.configuracion,
                    NOMBRE_ENTIDAD,
                    ListableConTransformacion
                    (
                            ListableUnoAMuchos
                            (
                                    ListableLeftJoin
                                    (
                                            ListableDAO(listOf(LibroSegunReglasDAO.COLUMNA_ID), parametrosLibroSegunReglasDAO),
                                            ListableDAO(listOf(ReglaDAO.COLUMNA_ID), parametrosReglaDAO),
                                            verificadorIdReglaDAONulo
                                    ),
                                    extractorIdPrecioSegunReglasDAO
                            ),
                            transformadorDeLibrosYReglasALibroSegunReglas
                    )
            ),
            ActualizableEnTransaccionSQL
            (
                    parametrosLibroSegunReglasDAO.configuracion,
                    ActualizableConRestriccion
                    (
                            ActualizableConRestriccion
                            (
                                    ActualizableConTransformacion<LibroSegunReglas, Long?, LibroSegunReglasEnBD, Long?>
                                    (
                                            ActualizableEntidadRelacionUnoAMuchosClonandoHijos
                                            (
                                                    ActualizableParcialCompuesto<LibroSegunReglasEnBD, Long?, List<ReglaDAO>>
                                                    (
                                                            ActualizableConTransformacion<LibroSegunReglasEnBD, Long?, LibroSegunReglasDAO, Long?>
                                                            (
                                                                    ActualizableDAO(parametrosLibroSegunReglasDAO, NOMBRE_ENTIDAD),
                                                                    TransformadorIdentidad(),
                                                                    extractorLibroSegunReglasDAODeRelacionLibroReglas,
                                                                    transformarLibroSegunReglasDaoARelacionLibroReglas
                                                            ),
                                                            extraerReglasDeRelacionLibroReglas,
                                                            asignadorReglasARelcionLibroReglas
                                                    ),
                                                    ReglaDAO.COLUMNA_ID_LIBRO_SEGUN_REGLAS,
                                                    ActualizableEntidadRelacionUnoAMuchosClonandoHijos.HijoClonable
                                                    (
                                                            ReglaDAO.TABLA,
                                                            EliminableDao(Regla.NOMBRE_ENTIDAD, parametrosReglaDAO),
                                                            SqlType.LONG,
                                                            CreableDAOMultiples(parametrosReglaDAO, Regla.NOMBRE_ENTIDAD),
                                                            extraerReglasDeRelacionLibroReglas,
                                                            asignadorReglasARelcionLibroReglas
                                                    )
                                            ),
                                            TransformadorIdentidad(),
                                            transformarLibroSegunReglasARelacionLibroReglas,
                                            transformadorDeRelacionDaoLibroYReglasALibroSegunReglas
                                    ),
                                    ValidadorReglasDiferenteParaLibroAsociadoParaActualizacion(
                                            parametrosLibroDAO, parametrosLibroSegunReglasDAO, parametrosReglaDAO
                                                                                              )
                            ),
                            ValidarQueEntidadExiste(parametrosLibroSegunReglasDAO)
                    )
            ),
            EliminablePorIdEnTransaccionSQL
            (
                    parametrosLibroSegunReglasDAO.configuracion,
                    EliminableSimple
                    (
                            EliminableDao(LibroSegunReglas.NOMBRE_ENTIDAD, parametrosLibroSegunReglasDAO),
                            TransformadorIdentidad()
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