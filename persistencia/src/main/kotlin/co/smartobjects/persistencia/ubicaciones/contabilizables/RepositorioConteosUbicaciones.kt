package co.smartobjects.persistencia.ubicaciones.contabilizables

import co.smartobjects.entidades.operativas.reservas.SesionDeManilla
import co.smartobjects.entidades.ubicaciones.contabilizables.ConteoUbicacion
import co.smartobjects.persistencia.ConfiguracionRepositorios
import co.smartobjects.persistencia.TransformadorEntidadCliente
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.excepciones.ErrorCreacionViolacionDeRestriccion
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.operativas.reservas.SesionDeManillaDAO


interface RepositorioConteosUbicaciones
    : CreadorRepositorio<ConteoUbicacion>,
      Creable<ConteoUbicacion>,
      ListableConParametros<ConteoUbicacion, FiltroConteosUbicaciones>,
      EliminablePorParametros<ConteoUbicacion, FiltroConteosUbicaciones>


private class ValidadorSesionDeManillaActiva
(
        private val parametrosDaoSesionDeManilla: ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long>
) : ValidadorRestriccionCreacion<ConteoUbicacion>
{
    override fun validar(idCliente: Long, entidadACrear: ConteoUbicacion)
    {
        val sesionDeManilla = parametrosDaoSesionDeManilla[idCliente].dao.queryForId(entidadACrear.idSesionDeManilla)

        if (sesionDeManilla == null)
        {
            if (parametrosDaoSesionDeManilla.configuracion.llavesForaneasActivadas)
            {
                throw ErrorDeLlaveForanea(entidadACrear.idSesionDeManilla.toString(), SesionDeManilla.NOMBRE_ENTIDAD)
            }
        }
        else if (sesionDeManilla.uuidTag == null)
        {
            throw ErrorCreacionViolacionDeRestriccion(ConteoUbicacion.NOMBRE_ENTIDAD, "La sesión de manilla debe de estar activa", null)
        }
    }
}

class RepositorioConteosUbicacionesSQL private constructor
(
        private val creadorRepositorio: CreadorRepositorio<ConteoUbicacion>,
        private val creable: Creable<ConteoUbicacion>,
        private val listable: ListableConParametros<ConteoUbicacion, FiltroConteosUbicaciones>,
        private val eliminable: EliminablePorParametros<ConteoUbicacion, FiltroConteosUbicaciones>
) : CreadorRepositorio<ConteoUbicacion> by creadorRepositorio,
    Creable<ConteoUbicacion> by creable,
    ListableConParametros<ConteoUbicacion, FiltroConteosUbicaciones> by listable,
    EliminablePorParametros<ConteoUbicacion, FiltroConteosUbicaciones> by eliminable,
    RepositorioConteosUbicaciones
{
    companion object
    {
        private val NOMBRE_ENTIDAD = ConteoUbicacion.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD


    private constructor(
            parametrosDaoConteoUbicacion: ParametrosParaDAOEntidadDeCliente<ConteoUbicacionDAO, Long>,
            parametrosDaoSesionDeManilla: ParametrosParaDAOEntidadDeCliente<SesionDeManillaDAO, Long>
                       )
            :
            this(
                    CreadorUnicaVez<ConteoUbicacion>
                    (
                            CreadorRepositorioSimple(parametrosDaoConteoUbicacion, NOMBRE_ENTIDAD)
                    ),
                    CreableEnTransaccionSQL
                    (
                            parametrosDaoConteoUbicacion.configuracion,
                            CreableConRestriccion
                            (
                                    CreableSimple
                                    (
                                            CreableDAO(parametrosDaoConteoUbicacion, NOMBRE_ENTIDAD),
                                            object : TransformadorEntidadCliente<ConteoUbicacion, ConteoUbicacionDAO>
                                            {
                                                override fun transformar(idCliente: Long, origen: ConteoUbicacion): ConteoUbicacionDAO
                                                {
                                                    return ConteoUbicacionDAO(origen)
                                                }
                                            }
                                    ),
                                    ValidadorSesionDeManillaActiva(parametrosDaoSesionDeManilla)
                            )
                    ),
                    ListableConTransaccionYParametros
                    (
                            parametrosDaoConteoUbicacion.configuracion,
                            NOMBRE_ENTIDAD,
                            ListableConParametrosSimple
                            (
                                    ListableConParametrosDAO<ConteoUbicacionDAO, Long, FiltroConteosUbicaciones>
                                    (
                                            listOf(ConteoUbicacionDAO.COLUMNA_ID),
                                            parametrosDaoConteoUbicacion
                                    )
                            )
                    ),
                    EliminablePorParametrosEnTransaccionSQL<ConteoUbicacion, FiltroConteosUbicaciones>
                    (
                            parametrosDaoConteoUbicacion.configuracion,
                            EliminablePorParametrosSimple<ConteoUbicacion, ConteoUbicacionDAO, Long, FiltroConteosUbicaciones>
                            (
                                    EliminablePorParametrosDao(NOMBRE_ENTIDAD, parametrosDaoConteoUbicacion)
                            )
                    )
                )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, ConteoUbicacionDAO.TABLA, ConteoUbicacionDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, SesionDeManillaDAO.TABLA, SesionDeManillaDAO::class.java)
                  )
}


sealed class FiltroConteosUbicaciones : ParametrosConsulta
{
    final override val filtrosSQL by lazy {
        filtrosSQLAdicionales
    }

    protected abstract val filtrosSQLAdicionales: List<FiltroSQL>

    object Todos : FiltroConteosUbicaciones()
    {
        override val filtrosSQLAdicionales: List<FiltroSQL> = emptyList()
    }
}