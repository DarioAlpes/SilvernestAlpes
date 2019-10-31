package co.smartobjects.persistencia.personas.camposdepersona

import co.smartobjects.entidades.personas.CampoDePersona
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.Transformador
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.basederepositorios.*


interface RepositorioCamposDePersonas
    : CreadorRepositorio<CampoDePersona>,
      CreableConDiferenteSalida<RepositorioCamposDePersonas.CamposACrear, List<CampoDePersona>>,
      Listable<CampoDePersona>,
      Buscable<CampoDePersona, CampoDePersona.Predeterminado>,
      Actualizable<CampoDePersona, CampoDePersona.Predeterminado>
{
    abstract class CamposACrear(val campos: Set<CampoDePersona>)
    {
        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CamposACrear

            if (campos != other.campos) return false

            return true
        }

        override fun hashCode(): Int
        {
            return campos.hashCode()
        }

        override fun toString(): String
        {
            return "CamposACrear(campos=$campos)"
        }
    }
}

private val transformadorPredeterminadoDeNegocioADao = object : Transformador<CampoDePersona.Predeterminado, CampoDePersonaDAO.Predeterminado>
{
    override fun transformar(origen: CampoDePersona.Predeterminado): CampoDePersonaDAO.Predeterminado
    {
        return CampoDePersonaDAO.Predeterminado.desdeNegocio(origen)
    }
}

private val transformadorCampoPersonaACampoPersonaDao = object : Transformador<CampoDePersona, CampoDePersonaDAO>
{
    override fun transformar(origen: CampoDePersona): CampoDePersonaDAO
    {
        return CampoDePersonaDAO(origen)
    }
}

class RepositorioCampoDePersonasSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<CampoDePersona>,
        private val creador: CreableConDiferenteSalida<RepositorioCamposDePersonas.CamposACrear, List<CampoDePersona>>,
        private val listador: Listable<CampoDePersona>,
        private val buscador: Buscable<CampoDePersona, CampoDePersona.Predeterminado>,
        private val actualizador: Actualizable<CampoDePersona, CampoDePersona.Predeterminado>)
    : CreadorRepositorio<CampoDePersona> by creadorRepositorio,
      CreableConDiferenteSalida<RepositorioCamposDePersonas.CamposACrear, List<CampoDePersona>> by creador,
      Listable<CampoDePersona> by listador,
      Buscable<CampoDePersona, CampoDePersona.Predeterminado> by buscador,
      Actualizable<CampoDePersona, CampoDePersona.Predeterminado> by actualizador,
      RepositorioCamposDePersonas
{
    companion object
    {
        @JvmField
        val NOMBRE_ENTIDAD: String = CampoDePersona.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(parametrosDAO: ParametrosParaDAOEntidadDeCliente<CampoDePersonaDAO, CampoDePersonaDAO.Predeterminado>)
            : this(
            CreadorUnicaVez
            (
                    CreadorRepositorioSimple<CampoDePersona, CampoDePersonaDAO, CampoDePersona.Predeterminado>
                    (
                            CreadorRepositorioDAO(parametrosDAO, NOMBRE_ENTIDAD)
                    )
            ),
            CreableEnTransaccionSQLParaDiferenteSalida<RepositorioCamposDePersonas.CamposACrear, List<CampoDePersona>>
            (
                    parametrosDAO.configuracion,
                    CreableConTransformacionConDiferenteSalida
                    (
                            CreableSimpleLista
                            (
                                    CreableDAOMultiples(parametrosDAO, NOMBRE_ENTIDAD),
                                    object : TransformadorEntidadCliente<CampoDePersona, CampoDePersonaDAO>
                                    {
                                        override fun transformar(idCliente: Long, origen: CampoDePersona): CampoDePersonaDAO
                                        {
                                            return CampoDePersonaDAO(origen)
                                        }
                                    }
                            ),
                            object : TransformadorEntidadCliente<RepositorioCamposDePersonas.CamposACrear, List<CampoDePersona>>
                            {
                                override fun transformar(idCliente: Long, origen: RepositorioCamposDePersonas.CamposACrear): List<CampoDePersona>
                                {
                                    return origen.campos.toList()
                                }
                            }
                    )
            ),
            ListableSimple<CampoDePersona, CampoDePersonaDAO, CampoDePersonaDAO.Predeterminado>
            (
                    ListableDAO(listOf(CampoDePersonaDAO.COLUMNA_CAMPO), parametrosDAO)
            ),
            BuscableSimple<CampoDePersona, CampoDePersonaDAO, CampoDePersona.Predeterminado, CampoDePersonaDAO.Predeterminado>
            (
                    transformadorPredeterminadoDeNegocioADao,
                    BuscableDao(parametrosDAO)
            ),
            ActualizableEnTransaccionSQL
            (
                    parametrosDAO.configuracion,
                    ActualizableSimple(
                            ActualizableDAO(parametrosDAO, NOMBRE_ENTIDAD),
                            transformadorPredeterminadoDeNegocioADao,
                            transformadorCampoPersonaACampoPersonaDao
                                      )
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(ParametrosParaDAOEntidadDeCliente(configuracion, CampoDePersonaDAO.TABLA, CampoDePersonaDAO::class.java))
}

class ListadoCamposPredeterminados
    : RepositorioCamposDePersonas
      .CamposACrear(
        CampoDePersona.Predeterminado.values().map { CampoDePersona(it, false) }.toSet()
                   )