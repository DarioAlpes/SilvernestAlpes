package co.smartobjects.persistencia.fondos

import co.smartobjects.entidades.fondos.Fondo
import co.smartobjects.persistencia.CampoTabla
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.acceso.AccesoDAO
import co.smartobjects.persistencia.fondos.categoriaskus.CategoriaSkuDAO
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.TransformadorCamposNegocioACamposFondo
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.crearCombinadorActualizarFondoPorCamposIndividuales
import co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos.todos.darCombinadorEliminableParaFondoCompleto
import co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos.todos.darCombinadorListableParaFondoCompleto
import co.smartobjects.persistencia.fondos.combinadores.fondos.compuestos.todos.darCreadorRepositorioFondoCompleto
import co.smartobjects.persistencia.fondos.skus.SkuDAO
import com.j256.ormlite.field.SqlType

interface RepositorioFondos
    : CreadorRepositorio<Fondo<*>>,
      Listable<Fondo<*>>,
      Buscable<Fondo<*>, Long>,
      ActualizablePorCamposIndividuales<Fondo<*>, Long>,
      EliminablePorId<Fondo<*>, Long>

class RepositorioFondosSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Fondo<*>>,
        private val listador: Listable<Fondo<*>>,
        private val buscador: Buscable<Fondo<*>, Long>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Fondo<*>, Long>,
        private val eliminador: EliminablePorId<Fondo<*>, Long>)
    : CreadorRepositorio<Fondo<*>> by creadorRepositorio,
      Listable<Fondo<*>> by listador,
      Buscable<Fondo<*>, Long> by buscador,
      ActualizablePorCamposIndividuales<Fondo<*>, Long> by actualizadorCamposIndividuales,
      EliminablePorId<Fondo<*>, Long> by eliminador,
      RepositorioFondos
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Fondo.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<Fondo<*>>,
            listador: ListableFiltrableOrdenable<Fondo<*>>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Fondo<*>, Long>,
            eliminador: EliminablePorId<Fondo<*>, Long>
                       )
            : this(
            creadorRepositorio,
            listador,
            BuscableSegunListableFiltrable(listador, CampoTabla(FondoDAO.TABLA, FondoDAO.COLUMNA_ID), SqlType.LONG),
            actualizadorCamposIndividuales,
            eliminador
                  )

    private constructor(
            parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>,
            parametrosDaoSku: ParametrosParaDAOEntidadDeCliente<SkuDAO, Long>,
            parametrosDaoCategoriaSkuDAO: ParametrosParaDAOEntidadDeCliente<CategoriaSkuDAO, Long>,
            parametrosDaoAcceso: ParametrosParaDAOEntidadDeCliente<AccesoDAO, Long>
                       )
            : this(
            darCreadorRepositorioFondoCompleto
            (
                    parametrosDaoFondo,
                    parametrosDaoSku,
                    parametrosDaoCategoriaSkuDAO,
                    parametrosDaoAcceso,
                    NOMBRE_ENTIDAD
            ),
            darCombinadorListableParaFondoCompleto
            (
                    parametrosDaoFondo,
                    parametrosDaoSku,
                    parametrosDaoCategoriaSkuDAO,
                    parametrosDaoAcceso,
                    NOMBRE_ENTIDAD
            ),
            crearCombinadorActualizarFondoPorCamposIndividuales(
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    listOf(),
                    TransformadorCamposNegocioACamposFondo()
                                                               ),
            darCombinadorEliminableParaFondoCompleto
            (
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, FondoDAO.TABLA, FondoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, SkuDAO.TABLA, SkuDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, CategoriaSkuDAO.TABLA, CategoriaSkuDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, AccesoDAO.TABLA, AccesoDAO::class.java)
                  )
}