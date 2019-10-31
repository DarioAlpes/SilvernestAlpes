package co.smartobjects.persistencia.personas.valorgrupoedad

import co.smartobjects.entidades.personas.ValorGrupoEdad
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.ErrorActualizacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.ErrorCreacionActualizacionPorDuplicidad
import co.smartobjects.persistencia.excepciones.ErrorCreacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.fondos.precios.SegmentoClientesDAO
import com.j256.ormlite.stmt.Where

interface RepositorioValoresGruposEdad
    : CreadorRepositorio<ValorGrupoEdad>,
      Creable<ValorGrupoEdad>,
      Listable<ValorGrupoEdad>,
      Buscable<ValorGrupoEdad, String>,
      Actualizable<ValorGrupoEdad, String>,
      EliminablePorId<ValorGrupoEdad, String>

private val transformadorValorGrupoEdadAValorGrupoEdadDao = object : TransformadorEntidadCliente<ValorGrupoEdad, ValorGrupoEdadDAO>
{
    override fun transformar(idCliente: Long, origen: ValorGrupoEdad): ValorGrupoEdadDAO
    {
        return ValorGrupoEdadDAO(origen)
    }
}
private val transformadorValorGrupoEdadAValorGrupoEdadDaoSinIdCliente = object : Transformador<ValorGrupoEdad, ValorGrupoEdadDAO>
{
    override fun transformar(origen: ValorGrupoEdad): ValorGrupoEdadDAO
    {
        return ValorGrupoEdadDAO(origen)
    }
}

private val asignadorIdValorGrupoEdadDao = object : AsignadorParametro<ValorGrupoEdadDAO, String>
{
    override fun asignarParametro(entidad: ValorGrupoEdadDAO, parametro: String): ValorGrupoEdadDAO
    {
        return entidad.copy(valor = parametro)
    }
}

private fun crearWhereClauseQueryRangosIntersectadosValoresGrupoEdad(
        whereRangos: Where<ValorGrupoEdadDAO, String>,
        valorGrupoEdad: ValorGrupoEdad)
        : Where<ValorGrupoEdadDAO, String>
{
    if (valorGrupoEdad.edadMinima != null)
    {
        whereRangos.ge(ValorGrupoEdadDAO.COLUMNA_EDAD_MAXIMA, valorGrupoEdad.edadMinima)
        whereRangos.isNull(ValorGrupoEdadDAO.COLUMNA_EDAD_MAXIMA)
        whereRangos.or(2)

        if (valorGrupoEdad.edadMaxima != null)
        {
            whereRangos.le(ValorGrupoEdadDAO.COLUMNA_EDAD_MINIMA, valorGrupoEdad.edadMaxima)
            whereRangos.isNull(ValorGrupoEdadDAO.COLUMNA_EDAD_MINIMA)
            whereRangos.or(2)
            whereRangos.and(2)
        }
    }
    else
    {
        if (valorGrupoEdad.edadMaxima != null)
        {
            whereRangos.le(ValorGrupoEdadDAO.COLUMNA_EDAD_MINIMA, valorGrupoEdad.edadMaxima)
            whereRangos.isNull(ValorGrupoEdadDAO.COLUMNA_EDAD_MINIMA)
            whereRangos.or(2)
        }
        else
        {
            whereRangos.raw("1 = 1")
        }
    }

    return whereRangos
}

private class ValidadorValorGrupoEdadEsDisyuntoParaCreacion(
        private val parametrosDAO: ParametrosParaDAOEntidadDeCliente<ValorGrupoEdadDAO, String>
                                                           ) : ValidadorRestriccionCreacion<ValorGrupoEdad>
{
    override fun validar(idCliente: Long, entidadACrear: ValorGrupoEdad)
    {
        val queryRangos = parametrosDAO[idCliente].dao.queryBuilder()

        crearWhereClauseQueryRangosIntersectadosValoresGrupoEdad(queryRangos.where(), entidadACrear)

        val seInterceptaroRangos = queryRangos.limit(1).countOf() > 0

        if (seInterceptaroRangos)
        {
            throw ErrorCreacionViolacionDeRestriccion(
                    ValorGrupoEdad.NOMBRE_ENTIDAD,
                    "El rango de edad debe de ser disyunto de los existentes",
                    arrayOf(entidadACrear.edadMinima.toString(), entidadACrear.edadMaxima.toString())
                                                     )
        }

    }
}

private class ValidadorValorGrupoEdadEsDisyuntoYNoEstaEnUsoParaActualizacion(
        private val parametrosValorGrupoEdadDAO: ParametrosParaDAOEntidadDeCliente<ValorGrupoEdadDAO, String>,
        private val parametrosSegmentosClientesDAO: ParametrosParaDAOEntidadDeCliente<SegmentoClientesDAO, Long>
                                                                            ) : ValidadorRestriccionActualizacion<ValorGrupoEdad, String>
{
    override fun validar(idCliente: Long, id: String, entidadAActualizar: ValorGrupoEdad)
    {
        if (id != entidadAActualizar.valor)
        {
            if (parametrosSegmentosClientesDAO.configuracion.llavesForaneasActivadas)
            {
                if (valorGrupoEdadEstaEnUso(parametrosSegmentosClientesDAO, idCliente, id))
                {
                    throw ErrorDeLlaveForanea(id, ValorGrupoEdad.NOMBRE_ENTIDAD)
                }
            }
            if (parametrosValorGrupoEdadDAO[idCliente].dao.idExists(entidadAActualizar.valor))
            {
                throw ErrorCreacionActualizacionPorDuplicidad(ValorGrupoEdad.NOMBRE_ENTIDAD)
            }
        }

        val queryRangos = parametrosValorGrupoEdadDAO[idCliente].dao.queryBuilder()
        val whereRangos = queryRangos.where()
        whereRangos.ne(ValorGrupoEdadDAO.COLUMNA_VALOR, id)
        crearWhereClauseQueryRangosIntersectadosValoresGrupoEdad(whereRangos, entidadAActualizar)
        whereRangos.and(2)
        val seInterceptaroRangos = queryRangos.limit(1).countOf() > 0
        if (seInterceptaroRangos)
        {
            throw ErrorActualizacionViolacionDeRestriccion(
                    ValorGrupoEdad.NOMBRE_ENTIDAD,
                    id,
                    "El rango de edad debe de ser disyunto de los existentes",
                    arrayOf(entidadAActualizar.edadMinima.toString(), entidadAActualizar.edadMaxima.toString())
                                                          )
        }
    }
}

private fun valorGrupoEdadEstaEnUso(parametrosSegmentosClientesDAO: ParametrosParaDAOEntidadDeCliente<SegmentoClientesDAO, Long>, idCliente: Long, valor: String): Boolean
{
    return parametrosSegmentosClientesDAO[idCliente].dao.queryBuilder()
               .where()
               .eq(SegmentoClientesDAO.COLUMNA_VALOR_CAMPO, valor)
               .and()
               .eq(SegmentoClientesDAO.COLUMNA_NOMBRE_CAMPO, SegmentoClientesDAO.NombreCampoEnDAO.GRUPO_DE_EDAD)
               .countOf() > 0
}

private class ValidadorGrupoEdadNoEstaEnUso(private val parametrosSegmentosClientesDAO: ParametrosParaDAOEntidadDeCliente<SegmentoClientesDAO, Long>)
    : ValidadorRestriccionEliminacion<String>
{
    override fun validarRestriccion(idCliente: Long, idAEliminar: String)
    {
        if (parametrosSegmentosClientesDAO.configuracion.llavesForaneasActivadas)
        {
            if (valorGrupoEdadEstaEnUso(parametrosSegmentosClientesDAO, idCliente, idAEliminar))
            {
                throw ErrorDeLlaveForanea(idAEliminar, ValorGrupoEdad.NOMBRE_ENTIDAD)
            }
        }
    }
}

class RepositorioValoresGruposEdadSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<ValorGrupoEdad>,
        private val creador: Creable<ValorGrupoEdad>,
        private val listador: Listable<ValorGrupoEdad>,
        private val buscador: Buscable<ValorGrupoEdad, String>,
        private val actualizador: Actualizable<ValorGrupoEdad, String>,
        private val eliminador: EliminablePorId<ValorGrupoEdad, String>)
    : CreadorRepositorio<ValorGrupoEdad> by creadorRepositorio,
      Creable<ValorGrupoEdad> by creador,
      Listable<ValorGrupoEdad> by listador,
      Buscable<ValorGrupoEdad, String> by buscador,
      Actualizable<ValorGrupoEdad, String> by actualizador,
      EliminablePorId<ValorGrupoEdad, String> by eliminador,
      RepositorioValoresGruposEdad
{
    override val nombreEntidad: String = ValorGrupoEdad.NOMBRE_ENTIDAD

    private constructor(
            parametrosValorGrupoEdadDAO: ParametrosParaDAOEntidadDeCliente<ValorGrupoEdadDAO, String>,
            parametrosSegmentosClientesDAO: ParametrosParaDAOEntidadDeCliente<SegmentoClientesDAO, Long>)
            : this(
            CreadorUnicaVez(
                    CreadorRepositorioSimple(parametrosValorGrupoEdadDAO, ValorGrupoEdad.NOMBRE_ENTIDAD)
                           ),
            CreableEnTransaccionSQL(
                    parametrosValorGrupoEdadDAO.configuracion,
                    CreableConRestriccion(
                            CreableSimple(
                                    CreableDAO(
                                            parametrosValorGrupoEdadDAO,
                                            ValorGrupoEdad.NOMBRE_ENTIDAD
                                              ),
                                    transformadorValorGrupoEdadAValorGrupoEdadDao
                                         ),
                            ValidadorValorGrupoEdadEsDisyuntoParaCreacion(parametrosValorGrupoEdadDAO)
                                         )
                                   ),
            ListableSimple<ValorGrupoEdad, ValorGrupoEdadDAO, String>(ListableDAO(listOf(ValorGrupoEdadDAO.COLUMNA_VALOR), parametrosValorGrupoEdadDAO)),
            BuscableSimple(
                    TransformadorIdentidad(),
                    BuscableDao(parametrosValorGrupoEdadDAO)
                          ),
            ActualizableEnTransaccionSQL(
                    parametrosValorGrupoEdadDAO.configuracion,
                    ActualizableConRestriccion(
                            ActualizableSimple(
                                    ActualizableConIdMutable(
                                            parametrosValorGrupoEdadDAO,
                                            ValorGrupoEdadDAO.COLUMNA_VALOR,
                                            ActualizableDAO(parametrosValorGrupoEdadDAO, ValorGrupoEdad.NOMBRE_ENTIDAD),
                                            asignadorIdValorGrupoEdadDao
                                                            ),
                                    TransformadorIdentidad(),
                                    transformadorValorGrupoEdadAValorGrupoEdadDaoSinIdCliente
                                              ),
                            ValidadorValorGrupoEdadEsDisyuntoYNoEstaEnUsoParaActualizacion(
                                    parametrosValorGrupoEdadDAO,
                                    parametrosSegmentosClientesDAO
                                                                                          )
                                              )
                                        ),
            EliminablePorIdEnTransaccionSQL(
                    parametrosValorGrupoEdadDAO.configuracion,
                    EliminableSimple(
                            EliminableConRestriccion(
                                    EliminableDao(ValorGrupoEdad.NOMBRE_ENTIDAD, parametrosValorGrupoEdadDAO),
                                    ValidadorGrupoEdadNoEstaEnUso(parametrosSegmentosClientesDAO)
                                                    ),
                            TransformadorIdentidad()
                                    )
                                           )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            :
            this(
                    ParametrosParaDAOEntidadDeCliente(configuracion, ValorGrupoEdadDAO.TABLA, ValorGrupoEdadDAO::class.java),
                    ParametrosParaDAOEntidadDeCliente(configuracion, SegmentoClientesDAO.TABLA, SegmentoClientesDAO::class.java)
                )
}