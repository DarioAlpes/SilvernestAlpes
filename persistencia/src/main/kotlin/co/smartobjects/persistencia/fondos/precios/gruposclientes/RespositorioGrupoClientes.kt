package co.smartobjects.persistencia.fondos.precios.gruposclientes

import co.smartobjects.entidades.fondos.precios.GrupoClientes
import co.smartobjects.entidades.fondos.precios.SegmentoClientes
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.CampoActualizableDesconocido
import co.smartobjects.persistencia.excepciones.ErrorCreacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.fondos.precios.SegmentoClientesDAO
import co.smartobjects.persistencia.personas.valorgrupoedad.ValorGrupoEdadDAO
import com.j256.ormlite.field.SqlType

interface RepositorioGrupoClientes
    : CreadorRepositorio<GrupoClientes>,
      Creable<GrupoClientes>,
      Listable<GrupoClientes>,
      Buscable<GrupoClientes, Long>,
      ActualizablePorCamposIndividuales<GrupoClientes, Long>,
      EliminablePorId<GrupoClientes, Long>

private val transformadorCamposNegocioACamposDaoGrupoClientes = object : Transformador<CamposDeEntidad<GrupoClientes>, CamposDeEntidadDAO<GrupoClientesDAO>>
{
    override fun transformar(origen: CamposDeEntidad<GrupoClientes>): CamposDeEntidadDAO<GrupoClientesDAO>
    {
        return origen.map {
            if (it.key == GrupoClientes.Campos.NOMBRE)
            {
                GrupoClientesDAO.COLUMNA_NOMBRE to CampoModificableEntidadDao<GrupoClientesDAO, Any?>(it.value.valor, GrupoClientesDAO.COLUMNA_NOMBRE)
            }
            else
            {
                throw CampoActualizableDesconocido(it.key, GrupoClientes.NOMBRE_ENTIDAD)
            }
        }.toMap()
    }

}

private val extractorIdGrupoClientesDao = object : Transformador<GrupoClientesDAO, Long?>
{
    override fun transformar(origen: GrupoClientesDAO): Long?
    {
        return origen.id!!
    }
}

private val transformadorDeGrupoClientesARelacionUnoAMuchos = object
    : TransformadorEntidadCliente<GrupoClientes, EntidadRelacionUnoAMuchos<GrupoClientesDAO, SegmentoClientesDAO>>
{
    override fun transformar(idCliente: Long, origen: GrupoClientes): EntidadRelacionUnoAMuchos<GrupoClientesDAO, SegmentoClientesDAO>
    {
        return EntidadRelacionUnoAMuchos(GrupoClientesDAO(origen), origen.segmentosClientes.map { SegmentoClientesDAO(it) })
    }
}

private val transformadorDeRelacionUnoAMuchosAGrupoClientes = object
    : TransformadorEntidadCliente<EntidadRelacionUnoAMuchos<GrupoClientesDAO, SegmentoClientesDAO>, GrupoClientes>
{
    override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAMuchos<GrupoClientesDAO, SegmentoClientesDAO>): GrupoClientes
    {
        return origen.entidadOrigen.aEntidadDeNegocio(idCliente, origen.entidadDestino.toList())
    }
}

private val transformadorAsignarRelacionSegmentoAGrupoClientes = object
    : TransformadorEntidadesRelacionadas<GrupoClientesDAO, SegmentoClientesDAO>
{
    override fun asignarCampoRelacionAEntidadDestino(entidadOrigen: GrupoClientesDAO, entidadDestino: SegmentoClientesDAO): SegmentoClientesDAO
    {
        return entidadDestino.copy(grupoClientesDAO = entidadOrigen)
    }
}

private class ValidadorGrupoClienteNoExiste(
        private val parametrosDAOValorGrupoEdad: ParametrosParaDAOEntidadDeCliente<ValorGrupoEdadDAO, String?>,
        private val parametrosSegmentosClientesDAO: ParametrosParaDAOEntidadDeCliente<SegmentoClientesDAO, Long?>
                                           ) : ValidadorRestriccionCreacion<GrupoClientes>
{
    private fun existeGrupoConSegmentosDados(idCliente: Long, segmentosClientes: List<SegmentoClientes>): Boolean
    {
        val columnaGrupoClientesFk = CampoTabla(SegmentoClientesDAO.TABLA, SegmentoClientesDAO.COLUMNA_ID_GRUPO_CLIENTES)

        val querySegmentosMatch = parametrosSegmentosClientesDAO[idCliente].dao.queryBuilder()

        querySegmentosMatch
            .selectRaw(
                    "${columnaGrupoClientesFk.nombreColumna} AS id_grupo_match",
                    "COUNT(${columnaGrupoClientesFk.nombreColumna}) AS numero_segmentos_match"
                      )

        val where = querySegmentosMatch.where()
        segmentosClientes.forEach {
            where.eq(SegmentoClientesDAO.COLUMNA_NOMBRE_CAMPO, it.campo)
                .and().eq(SegmentoClientesDAO.COLUMNA_VALOR_CAMPO, it.valor)
        }
        where.or(segmentosClientes.size)

        querySegmentosMatch.groupBy(SegmentoClientesDAO.COLUMNA_ID_GRUPO_CLIENTES)
        querySegmentosMatch.having("COUNT(${columnaGrupoClientesFk.nombreColumna}) = ${segmentosClientes.size}")


        val querySegmentosTodos = parametrosSegmentosClientesDAO[idCliente].dao.queryBuilder()

        querySegmentosTodos.selectRaw(
                "${columnaGrupoClientesFk.nombreColumna} AS id_grupo_total",
                "COUNT(${columnaGrupoClientesFk.nombreColumna}) AS numero_segmentos_total"
                                     )
        querySegmentosTodos.groupBy(SegmentoClientesDAO.COLUMNA_ID_GRUPO_CLIENTES)

        val sql = """
            SELECT
                numero_segmentos_que_hacen_match.id_grupo_match
            FROM
                (${querySegmentosMatch.prepareStatementString()}) AS numero_segmentos_que_hacen_match
                JOIN (${querySegmentosTodos.prepareStatementString()}) AS numero_segmentos_totales ON
                    numero_segmentos_que_hacen_match.id_grupo_match = numero_segmentos_totales.id_grupo_total
                    AND numero_segmentos_que_hacen_match.numero_segmentos_match = numero_segmentos_totales.numero_segmentos_total
            LIMIT 1""".trimIndent()

        return parametrosSegmentosClientesDAO[idCliente].dao.queryRaw(sql).results.firstOrNull() != null
    }

    override fun validar(idCliente: Long, entidadACrear: GrupoClientes)
    {
        if (parametrosDAOValorGrupoEdad.configuracion.llavesForaneasActivadas)
        {
            entidadACrear.segmentosClientes.forEach {
                if (it.campo == SegmentoClientes.NombreCampo.GRUPO_DE_EDAD
                    && !parametrosDAOValorGrupoEdad[idCliente].dao.idExists(it.valor))
                {
                    throw ErrorDeLlaveForanea(it.valor, GrupoClientes.NOMBRE_ENTIDAD)
                }
            }
        }

        if (existeGrupoConSegmentosDados(idCliente, entidadACrear.segmentosClientes))
        {
            throw ErrorCreacionViolacionDeRestriccion(
                    GrupoClientes.NOMBRE_ENTIDAD,
                    "Ya existe otro ${GrupoClientes.NOMBRE_ENTIDAD} con los mismos segmentos",
                    entidadACrear.segmentosClientes.map { it.toString() }.toTypedArray()
                                                     )
        }
    }
}

class RepositorioGrupoClientesSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<GrupoClientes>,
        private val creador: Creable<GrupoClientes>,
        private val listador: Listable<GrupoClientes>,
        private val buscador: Buscable<GrupoClientes, Long>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<GrupoClientes, Long>,
        private val eliminador: EliminablePorId<GrupoClientes, Long>)
    : CreadorRepositorio<GrupoClientes> by creadorRepositorio,
      Creable<GrupoClientes> by creador,
      Listable<GrupoClientes> by listador,
      Buscable<GrupoClientes, Long> by buscador,
      ActualizablePorCamposIndividuales<GrupoClientes, Long> by actualizadorCamposIndividuales,
      EliminablePorId<GrupoClientes, Long> by eliminador,
      RepositorioGrupoClientes
{
    override val nombreEntidad: String = GrupoClientes.NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<GrupoClientes>,
            creador: Creable<GrupoClientes>,
            listador: ListableFiltrableOrdenable<GrupoClientes>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<GrupoClientes, Long>,
            eliminador: EliminablePorId<GrupoClientes, Long>
                       )
            : this(
            creadorRepositorio,
            creador,
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(GrupoClientesDAO.TABLA, GrupoClientesDAO.COLUMNA_ID), SqlType.LONG),
            actualizadorCamposIndividuales,
            eliminador
                  )

    private constructor(
            parametrosDAOValorGrupoEdad: ParametrosParaDAOEntidadDeCliente<ValorGrupoEdadDAO, String?>,
            parametrosGrupoClientesDAO: ParametrosParaDAOEntidadDeCliente<GrupoClientesDAO, Long?>,
            parametrosSegmentosClientesDAO: ParametrosParaDAOEntidadDeCliente<SegmentoClientesDAO, Long?>
                       )
            : this(
            CreadorUnicaVez(
                    CreadorRepositorioCompuesto(
                            listOf(
                                    CreadorRepositorioDAO(parametrosGrupoClientesDAO, GrupoClientes.NOMBRE_ENTIDAD),
                                    CreadorRepositorioDAO(parametrosSegmentosClientesDAO, GrupoClientes.NOMBRE_ENTIDAD)
                                  ),
                            GrupoClientes.NOMBRE_ENTIDAD
                                               )
                           ),
            CreableEnTransaccionSQL<GrupoClientes>(
                    parametrosGrupoClientesDAO.configuracion,
                    CreableConRestriccion(
                            CreableConTransformacion<GrupoClientes, EntidadRelacionUnoAMuchos<GrupoClientesDAO, SegmentoClientesDAO>>(
                                    CreableDAORelacionUnoAMuchos(
                                            CreableDAO(parametrosGrupoClientesDAO, GrupoClientes.NOMBRE_ENTIDAD),
                                            CreableDAOMultiples(parametrosSegmentosClientesDAO, GrupoClientes.NOMBRE_ENTIDAD),
                                            transformadorAsignarRelacionSegmentoAGrupoClientes
                                                                ),
                                    transformadorDeGrupoClientesARelacionUnoAMuchos,
                                    transformadorDeRelacionUnoAMuchosAGrupoClientes
                                                                                                                                     ),
                            ValidadorGrupoClienteNoExiste(parametrosDAOValorGrupoEdad, parametrosSegmentosClientesDAO)
                                         )
                                                  ),
            ListableConTransformacion<EntidadRelacionUnoAMuchos<GrupoClientesDAO, SegmentoClientesDAO>, GrupoClientes>(
                    ListableUnoAMuchos
                    (
                            ListableInnerJoin(
                                    ListableDAO(listOf(GrupoClientesDAO.COLUMNA_ID), parametrosGrupoClientesDAO),
                                    ListableDAO(listOf(SegmentoClientesDAO.COLUMNA_ID), parametrosSegmentosClientesDAO)
                                             ),
                            extractorIdGrupoClientesDao
                    ),
                    transformadorDeRelacionUnoAMuchosAGrupoClientes
                                                                                                                      ),
            ActualizablePorCamposIndividualesEnTransaccionSQL(
                    parametrosGrupoClientesDAO.configuracion,
                    ActualizablePorCamposIndividualesSimple(
                            ActualizablePorCamposIndividualesDao(
                                    GrupoClientes.NOMBRE_ENTIDAD,
                                    parametrosGrupoClientesDAO
                                                                ),
                            TransformadorIdentidad(),
                            transformadorCamposNegocioACamposDaoGrupoClientes
                                                           )
                                                             ),
            EliminablePorIdEnTransaccionSQL(
                    parametrosGrupoClientesDAO.configuracion,
                    EliminableSimple(
                            EliminableDao(GrupoClientes.NOMBRE_ENTIDAD, parametrosGrupoClientesDAO),
                            TransformadorIdentidad()
                                    )
                                           )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, ValorGrupoEdadDAO.TABLA, ValorGrupoEdadDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, GrupoClientesDAO.TABLA, GrupoClientesDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, SegmentoClientesDAO.TABLA, SegmentoClientesDAO::class.java)
                  )
}