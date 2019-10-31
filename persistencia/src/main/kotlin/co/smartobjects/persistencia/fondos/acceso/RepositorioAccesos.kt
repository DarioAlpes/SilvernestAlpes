package co.smartobjects.persistencia.fondos.acceso

import co.smartobjects.entidades.fondos.Acceso
import co.smartobjects.entidades.fondos.AccesoBase
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

interface RepositorioAccesos
    : CreadorRepositorio<Acceso>,
      Creable<Acceso>,
      Listable<Acceso>,
      Buscable<Acceso, Long>,
      Actualizable<Acceso, Long>,
      ActualizablePorCamposIndividuales<Acceso, Long>,
      EliminablePorId<Acceso, Long>

private val filtroIgualdadAcceso = crearFiltroIgualdadFondoEspecifico(FondoDAO.TipoDeFondoEnBD.ACCESO)

class RepositorioAccesosSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Acceso>,
        private val creador: Creable<Acceso>,
        private val listador: Listable<Acceso>,
        private val buscador: Buscable<Acceso, Long>,
        private val actualizador: Actualizable<Acceso, Long>,
        private val actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Acceso, Long>,
        private val eliminador: EliminablePorId<Acceso, Long>
                                               ) : CreadorRepositorio<Acceso> by creadorRepositorio,
                                                   Creable<Acceso> by creador,
                                                   Listable<Acceso> by listador,
                                                   Buscable<Acceso, Long> by buscador,
                                                   Actualizable<Acceso, Long> by actualizador,
                                                   ActualizablePorCamposIndividuales<Acceso, Long> by actualizadorCamposIndividuales,
                                                   EliminablePorId<Acceso, Long> by eliminador,
                                                   RepositorioAccesos
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Acceso.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(
            creadorRepositorio: CreadorRepositorio<Acceso>,
            creador: Creable<Acceso>,
            listador: ListableFiltrableOrdenable<Acceso>,
            actualizador: Actualizable<Acceso, Long>,
            actualizadorCamposIndividuales: ActualizablePorCamposIndividuales<Acceso, Long>,
            eliminador: EliminablePorId<Acceso, Long>
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
            parametrosDaoAcceso: ParametrosParaDAOEntidadDeCliente<AccesoDAO, Long>
                       )
            : this(
            darCreadorRepositorioFondoCompuesto
            (
                    parametrosDaoFondo,
                    parametrosDaoAcceso,
                    NOMBRE_ENTIDAD
            ),
            darCombinadorCreableParaFondoCompuestoConEntidadIntermedia<AccesoBase<*>, Acceso, AccesoDAO>(
                    parametrosDaoFondo,
                    darCombinadorCreableFondoCompuesto(
                            parametrosDaoFondo,
                            parametrosDaoAcceso,
                            NOMBRE_ENTIDAD,
                            { accesoDAO, fondoDAO, id -> accesoDAO.copy(fondoDAO = fondoDAO, id = id) }
                                                      ),
                    ::AccesoDAO
                                                                                                        ),
            darCombinadorListableParaFondoCompuestoConEntidadIntermedia<AccesoBase<*>, Acceso, AccesoDAO>
            (
                    parametrosDaoFondo,
                    parametrosDaoAcceso,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadAcceso
            ),
            crearCombinadorDeActualizacionDeFondoCompuestoNoJerarquico(
                    parametrosDaoFondo,
                    parametrosDaoAcceso,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadAcceso,
                    ::AccesoDAO
                                                                      ),
            crearCombinadorActualizarFondoPorCamposIndividuales(
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    listOf(filtroIgualdadAcceso),
                    TransformadorCamposNegocioACamposFondo()
                                                               ),
            darCombinadorEliminableParaFondoEspecifico
            (
                    parametrosDaoFondo,
                    NOMBRE_ENTIDAD,
                    filtroIgualdadAcceso
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, FondoDAO.TABLA, FondoDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, AccesoDAO.TABLA, AccesoDAO::class.java)
                  )
}