package co.smartobjects.persistencia.fondos.precios.impuestos

import co.smartobjects.entidades.fondos.precios.Impuesto
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.TransformadorIdentidad
import co.smartobjects.persistencia.basederepositorios.*

interface RepositorioImpuestos
    : CreadorRepositorio<Impuesto>,
      Creable<Impuesto>,
      Listable<Impuesto>,
      Buscable<Impuesto, Long>,
      Actualizable<Impuesto, Long>,
      EliminablePorId<Impuesto, Long>

private val transformadorImpuestoAImpuestoDaoCreacion = object : TransformadorEntidadCliente<Impuesto, ImpuestoDAO>
{
    override fun transformar(idCliente: Long, origen: Impuesto): ImpuestoDAO
    {
        return ImpuestoDAO(origen)
    }
}

private val transformadorImpuestoAImpuestoDaoActualizacion = object : Transformador<Impuesto, ImpuestoDAO>
{
    override fun transformar(origen: Impuesto): ImpuestoDAO
    {
        return ImpuestoDAO(origen)
    }
}

class RepositorioImpuestosSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Impuesto>,
        private val creador: Creable<Impuesto>,
        private val listador: Listable<Impuesto>,
        private val buscador: Buscable<Impuesto, Long>,
        private val actualizador: Actualizable<Impuesto, Long>,
        private val eliminador: EliminablePorId<Impuesto, Long>)
    : CreadorRepositorio<Impuesto> by creadorRepositorio,
      Creable<Impuesto> by creador,
      Listable<Impuesto> by listador,
      Buscable<Impuesto, Long> by buscador,
      Actualizable<Impuesto, Long> by actualizador,
      EliminablePorId<Impuesto, Long> by eliminador,
      RepositorioImpuestos
{
    override val nombreEntidad: String = Impuesto.NOMBRE_ENTIDAD

    private constructor(parametrosDAO: ParametrosParaDAOEntidadDeCliente<ImpuestoDAO, Long?>)
            : this(
            CreadorUnicaVez(
                    CreadorRepositorioSimple(parametrosDAO, Impuesto.NOMBRE_ENTIDAD)
                           ),
            CreableEnTransaccionSQL(
                    parametrosDAO.configuracion,
                    CreableSimple(
                            CreableDAO(
                                    parametrosDAO,
                                    Impuesto.NOMBRE_ENTIDAD
                                      ),
                            transformadorImpuestoAImpuestoDaoCreacion
                                 )
                                   ),
            ListableSimple<Impuesto, ImpuestoDAO, Long?>(ListableDAO(listOf(ImpuestoDAO.COLUMNA_ID), parametrosDAO)),
            BuscableSimple(
                    TransformadorIdentidad(),
                    BuscableDao(parametrosDAO)
                          ),
            ActualizableEnTransaccionSQL(
                    parametrosDAO.configuracion,
                    ActualizableSimple(
                            ActualizableDAO(parametrosDAO, Impuesto.NOMBRE_ENTIDAD),
                            TransformadorIdentidad(),
                            transformadorImpuestoAImpuestoDaoActualizacion
                                      )
                                        ),
            EliminablePorIdEnTransaccionSQL(
                    parametrosDAO.configuracion,
                    EliminableSimple(
                            EliminableDao(Impuesto.NOMBRE_ENTIDAD, parametrosDAO),
                            TransformadorIdentidad()
                                    )
                                           )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(ParametrosParaDAOEntidadDeCliente(configuracion, ImpuestoDAO.TABLA, ImpuestoDAO::class.java))
}