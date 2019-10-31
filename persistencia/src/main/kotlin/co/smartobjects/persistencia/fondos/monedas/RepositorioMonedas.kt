package co.smartobjects.persistencia.fondos.monedas

import co.smartobjects.entidades.fondos.Dinero
import co.smartobjects.persistencia.CampoTabla
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.FondoDAO
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.TransformadorCamposNegocioACamposFondo
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.crearCombinadorActualizarFondoPorCamposIndividuales
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.crearFiltroIgualdadFondoEspecifico
import co.smartobjects.persistencia.fondos.combinadores.fondos.compartidos.darCombinadorEliminableParaFondoEspecifico
import co.smartobjects.persistencia.fondos.combinadores.fondos.simples.crearCombinadorDeActualizacionDeFondoSimple
import co.smartobjects.persistencia.fondos.combinadores.fondos.simples.darCombinadorCreableParaFondoSimple
import co.smartobjects.persistencia.fondos.combinadores.fondos.simples.darCombinadorListableParaFondoSimple
import co.smartobjects.persistencia.fondos.combinadores.fondos.simples.darCreadorRepositorioFondoSimple
import com.j256.ormlite.field.SqlType

interface RepositorioMonedas
    : CreadorRepositorio<Dinero>,
      Creable<Dinero>,
      Listable<Dinero>,
      Buscable<Dinero, Long>,
      Actualizable<Dinero, Long>,
      ActualizablePorCamposIndividuales<Dinero, Long>,
      EliminablePorId<Dinero, Long>

private val filtroIgualdadDinero = crearFiltroIgualdadFondoEspecifico(FondoDAO.TipoDeFondoEnBD.DINERO)

class RepositorioMonedasSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Dinero>,
        private val creador: Creable<Dinero>,
        private val listador: Listable<Dinero>,
        private val buscador: Buscable<Dinero, Long>,
        private val actualizador: Actualizable<Dinero, Long>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Dinero, Long>,
        private val eliminador: EliminablePorId<Dinero, Long>)
    : CreadorRepositorio<Dinero> by creadorRepositorio,
      Creable<Dinero> by creador,
      Listable<Dinero> by listador,
      Buscable<Dinero, Long> by buscador,
      Actualizable<Dinero, Long> by actualizador,
      ActualizablePorCamposIndividuales<Dinero, Long> by actualizadorCamposIndividuales,
      EliminablePorId<Dinero, Long> by eliminador,
      RepositorioMonedas
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Dinero.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<Dinero>,
            creador: Creable<Dinero>,
            listador: ListableFiltrableOrdenable<Dinero>,
            actualizador: Actualizable<Dinero, Long>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Dinero, Long>,
            eliminador: EliminablePorId<Dinero, Long>
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

    private constructor(parametrosDaoFondo: ParametrosParaDAOEntidadDeCliente<FondoDAO, Long>)
            : this(
            darCreadorRepositorioFondoSimple
            (
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD
            ),
            darCombinadorCreableParaFondoSimple(
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD
                                               ),
            darCombinadorListableParaFondoSimple<Dinero>
            (
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadDinero
            ),
            crearCombinadorDeActualizacionDeFondoSimple(
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadDinero
                                                       ),
            crearCombinadorActualizarFondoPorCamposIndividuales(
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    listOf(filtroIgualdadDinero),
                    TransformadorCamposNegocioACamposFondo()
                                                               ),
            darCombinadorEliminableParaFondoEspecifico
            (
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadDinero
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(ParametrosParaDAOEntidadDeCliente(configuracion, FondoDAO.TABLA, FondoDAO::class.java))
}