package co.smartobjects.persistencia.fondos.acceso

import co.smartobjects.entidades.fondos.AccesoBase
import co.smartobjects.entidades.fondos.Entrada
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

interface RepositorioEntradas
    : CreadorRepositorio<Entrada>,
      Creable<Entrada>,
      Listable<Entrada>,
      Buscable<Entrada, Long>,
      Actualizable<Entrada, Long>,
      ActualizablePorCamposIndividuales<Entrada, Long>,
      EliminablePorId<Entrada, Long>

private val filtroIgualdadEntrada = crearFiltroIgualdadFondoEspecifico(FondoDAO.TipoDeFondoEnBD.ENTRADA)

class RepositorioEntradasSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Entrada>,
        private val creador: Creable<Entrada>,
        private val listador: Listable<Entrada>,
        private val buscador: Buscable<Entrada, Long>,
        private val actualizador: Actualizable<Entrada, Long>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Entrada, Long>,
        private val eliminador: EliminablePorId<Entrada, Long>
                                                ) : CreadorRepositorio<Entrada> by creadorRepositorio,
                                                    Creable<Entrada> by creador,
                                                    Listable<Entrada> by listador,
                                                    Buscable<Entrada, Long> by buscador,
                                                    Actualizable<Entrada, Long> by actualizador,
                                                    ActualizablePorCamposIndividuales<Entrada, Long> by actualizadorCamposIndividuales,
                                                    EliminablePorId<Entrada, Long> by eliminador,
                                                    RepositorioEntradas
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Entrada.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<Entrada>,
            creador: Creable<Entrada>,
            listador: ListableFiltrableOrdenable<Entrada>,
            actualizador: Actualizable<Entrada, Long>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Entrada, Long>,
            eliminador: EliminablePorId<Entrada, Long>
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
            parametrosDaoEntrada: ParametrosParaDAOEntidadDeCliente<AccesoDAO, Long>
                       )
            : this(
            darCreadorRepositorioFondoCompuesto
            (
                    parametrosDaoFondo,
                    parametrosDaoEntrada,
                    NOMBRE_ENTIDAD
            ),
            darCombinadorCreableParaFondoCompuestoConEntidadIntermedia<AccesoBase<*>, Entrada, AccesoDAO>(
                    parametrosDaoFondo,
                    darCombinadorCreableFondoCompuesto(
                            parametrosDaoFondo,
                            parametrosDaoEntrada,
                            NOMBRE_ENTIDAD,
                            { accesoDAO, fondoDAO, id -> accesoDAO.copy(fondoDAO = fondoDAO, id = id) }
                                                      ),
                    ::AccesoDAO
                                                                                                         ),
            darCombinadorListableParaFondoCompuestoConEntidadIntermedia<AccesoBase<*>, Entrada, AccesoDAO>
            (
                    parametrosDaoFondo,
                    parametrosDaoEntrada,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadEntrada
            ),
            crearCombinadorDeActualizacionDeFondoCompuestoNoJerarquico(
                    parametrosDaoFondo,
                    parametrosDaoEntrada,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadEntrada,
                    ::AccesoDAO
                                                                      ),
            crearCombinadorActualizarFondoPorCamposIndividuales(
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    listOf(filtroIgualdadEntrada),
                    TransformadorCamposNegocioACamposFondo()
                                                               ),
            darCombinadorEliminableParaFondoEspecifico
            (
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadEntrada
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, FondoDAO.TABLA, FondoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, AccesoDAO.TABLA, AccesoDAO::class.java)
                  )
}