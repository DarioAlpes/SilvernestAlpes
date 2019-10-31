package co.smartobjects.persistencia.fondos.categoriaskus

import co.smartobjects.entidades.fondos.CategoriaSku
import co.smartobjects.persistencia.CampoTabla
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.TransformadorCamposNegocioACamposFondo
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.crearCombinadorActualizarFondoPorCamposIndividuales
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.crearFiltroIgualdadFondoEspecifico
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.darCombinadorEliminableParaFondoEspecifico
import co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos.*
import com.j256.ormlite.field.SqlType

interface RepositorioCategoriasSkus
    : CreadorRepositorio<CategoriaSku>,
      Creable<CategoriaSku>,
      Listable<CategoriaSku>,
      Buscable<CategoriaSku, Long>,
      Actualizable<CategoriaSku, Long>,
      ActualizablePorCamposIndividuales<CategoriaSku, Long>,
      EliminablePorId<CategoriaSku, Long>

private val filtroIgualdadCategoriaSku = crearFiltroIgualdadFondoEspecifico(FondoDAO.TipoDeFondoEnBD.CATEGORIA_SKU)

class RepositorioCategoriasSkusSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<CategoriaSku>,
        private val creador: Creable<CategoriaSku>,
        private val listador: Listable<CategoriaSku>,
        private val buscador: Buscable<CategoriaSku, Long>,
        private val actualizador: Actualizable<CategoriaSku, Long>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<CategoriaSku, Long>,
        private val eliminador: EliminablePorId<CategoriaSku, Long>
                                                      ) : CreadorRepositorio<CategoriaSku> by creadorRepositorio,
                                                          Creable<CategoriaSku> by creador,
                                                          Listable<CategoriaSku> by listador,
                                                          Buscable<CategoriaSku, Long> by buscador,
                                                          Actualizable<CategoriaSku, Long> by actualizador,
                                                          ActualizablePorCamposIndividuales<CategoriaSku, Long> by actualizadorCamposIndividuales,
                                                          EliminablePorId<CategoriaSku, Long> by eliminador,
                                                          RepositorioCategoriasSkus
{
    companion object
    {
        private val NOMBRE_ENTIDAD = CategoriaSku.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<CategoriaSku>,
            creador: Creable<CategoriaSku>,
            listador: ListableFiltrableOrdenable<CategoriaSku>,
            actualizador: Actualizable<CategoriaSku, Long>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<CategoriaSku, Long>,
            eliminador: EliminablePorId<CategoriaSku, Long>
                       )
            : this(
            creadorRepositorio,
            creador,
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(FondoDAO.TABLA, FondoDAO.COLUMNA_ID), SqlType.LONG),
            actualizador,
            actualizadorCamposIndividuales,
            eliminador
                  )

    private constructor(
            parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
            parametrosDaoCategoriaSku: ParametrosParaDAOEntidadDeCliente<CategoriaSkuDAO, Long>
                       )
            : this(
            darCreadorRepositorioFondoCompuesto
            (
                    parametrosDaoFondo,
                    parametrosDaoCategoriaSku,
                    NOMBRE_ENTIDAD
            ),
            darCombinadorCreableParaFondoCompuesto<CategoriaSku, CategoriaSkuDAO>(
                    parametrosDaoFondo,
                    darCombinadorCreableFondoCompuestoSegunCreadorFondoEspecifico(
                            parametrosDaoFondo,
                            CreableJerarquicoDAO(
                                    parametrosDaoCategoriaSku,
                                    CreableDAO(parametrosDaoCategoriaSku, NOMBRE_ENTIDAD)
                                                ),
                            NOMBRE_ENTIDAD,
                            { categoriaSkuDAO, fondoDAO, id -> categoriaSkuDAO.copy(fondoDAO = fondoDAO, id = id) }
                                                                                 ),
                    ::CategoriaSkuDAO
                                                                                 ),
            darCombinadorListableParaFondoCompuesto<CategoriaSku, CategoriaSkuDAO>
            (
                    parametrosDaoFondo,
                    parametrosDaoCategoriaSku,
                    CategoriaSku.NOMBRE_ENTIDAD,
                    filtroIgualdadCategoriaSku
            ),
            crearCombinadorDeActualizacionDeFondoCompuestoJerarquico(
                    parametrosDaoFondo,
                    parametrosDaoCategoriaSku,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadCategoriaSku,
                    { CategoriaSkuDAO(it) },
                    { fondoCompuesto, idsAncestros -> fondoCompuesto.copiar(idsDeAncestros = idsAncestros) }
                                                                    ),
            crearCombinadorActualizarFondoPorCamposIndividuales(
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    listOf(filtroIgualdadCategoriaSku),
                    TransformadorCamposNegocioACamposFondo()
                                                               ),
            darCombinadorEliminableParaFondoEspecifico
            (
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadCategoriaSku
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, FondoDAO.TABLA, FondoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CategoriaSkuDAO.TABLA, CategoriaSkuDAO::class.java)
                  )
}