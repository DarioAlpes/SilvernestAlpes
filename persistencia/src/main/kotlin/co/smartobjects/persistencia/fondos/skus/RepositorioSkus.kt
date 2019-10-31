package co.smartobjects.persistencia.fondos.skus

import co.smartobjects.entidades.fondos.Sku
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

interface RepositorioSkus
    : CreadorRepositorio<Sku>,
      Creable<Sku>,
      Listable<Sku>,
      Buscable<Sku, Long>,
      Actualizable<Sku, Long>,
      ActualizablePorCamposIndividuales<Sku, Long>,
      EliminablePorId<Sku, Long>

private val filtroIgualdadSku = crearFiltroIgualdadFondoEspecifico(FondoDAO.TipoDeFondoEnBD.SKU)

class RepositorioSkusSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Sku>,
        private val creador: Creable<Sku>,
        private val listador: Listable<Sku>,
        private val buscador: Buscable<Sku, Long>,
        private val actualizador: Actualizable<Sku, Long>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Sku, Long>,
        private val eliminador: EliminablePorId<Sku, Long>
                                            ) : CreadorRepositorio<Sku> by creadorRepositorio,
                                                Creable<Sku> by creador,
                                                Listable<Sku> by listador,
                                                Buscable<Sku, Long> by buscador,
                                                Actualizable<Sku, Long> by actualizador,
                                                ActualizablePorCamposIndividuales<Sku, Long> by actualizadorCamposIndividuales,
                                                EliminablePorId<Sku, Long> by eliminador,
                                                RepositorioSkus
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Sku.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<Sku>,
            creador: Creable<Sku>,
            listador: ListableFiltrableOrdenable<Sku>,
            actualizador: Actualizable<Sku, Long>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Sku, Long>,
            eliminador: EliminablePorId<Sku, Long>
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
            parametrosDaoSku: ParametrosParaDAOEntidadDeCliente<SkuDAO, Long>
                       )
            : this(
            darCreadorRepositorioFondoCompuesto
            (
                    parametrosDaoFondo,
                    parametrosDaoSku,
                    NOMBRE_ENTIDAD
            ),
            darCombinadorCreableParaFondoCompuesto<Sku, SkuDAO>(
                    parametrosDaoFondo,
                    darCombinadorCreableFondoCompuesto(
                            parametrosDaoFondo,
                            parametrosDaoSku,
                            NOMBRE_ENTIDAD,
                            { skuDAO, fondoDAO, id -> skuDAO.copy(fondoDAO = fondoDAO, id = id) }
                                                      ),
                    ::SkuDAO
                                                               ),
            darCombinadorListableParaFondoCompuesto<Sku, SkuDAO>
            (
                    parametrosDaoFondo,
                    parametrosDaoSku,
                    Sku.NOMBRE_ENTIDAD,
                    filtroIgualdadSku
            ),
            crearCombinadorDeActualizacionDeFondoCompuestoNoJerarquico(
                    parametrosDaoFondo,
                    parametrosDaoSku,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadSku,
                    ::SkuDAO
                                                                      ),
            crearCombinadorActualizarFondoPorCamposIndividuales(
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    listOf(filtroIgualdadSku),
                    TransformadorCamposNegocioACamposFondo()
                                                               ),
            darCombinadorEliminableParaFondoEspecifico
            (
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadSku
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, FondoDAO.TABLA, FondoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, SkuDAO.TABLA, SkuDAO::class.java)
                  )
}