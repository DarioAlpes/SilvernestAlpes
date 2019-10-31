package co.smartobjects.persistencia.personas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import com.j256.ormlite.field.SqlType


interface RepositorioPersonas
    : CreadorRepositorio<Persona>,
      Creable<Persona>,
      Listable<Persona>,
      Buscable<Persona, Long>,
      BuscableConParametros<Persona, DocumentoCompletoDAO>,
      Actualizable<Persona, Long>,
      EliminablePorId<Persona, Long>

private val transformadorPersonaAPersonaDaoCreacion = object : TransformadorEntidadCliente<Persona, PersonaDAO>
{
    override fun transformar(idCliente: Long, origen: Persona): PersonaDAO
    {
        return PersonaDAO(origen)
    }
}

private val transformadorPersonaAPersonaDaoActualizacion = object : Transformador<Persona, PersonaDAO>
{
    override fun transformar(origen: Persona): PersonaDAO
    {
        return PersonaDAO(origen)
    }
}

class RepositorioPersonasSQL private constructor(
        private val creadorRepositorio: CreadorRepositorio<Persona>,
        private val creador: Creable<Persona>,
        private val listador: Listable<Persona>,
        private val buscador: Buscable<Persona, Long>,
        private val buscadorConParametros: BuscableConParametros<Persona, DocumentoCompletoDAO>,
        private val actualizador: Actualizable<Persona, Long>,
        private val eliminador: EliminablePorId<Persona, Long>)
    : CreadorRepositorio<Persona> by creadorRepositorio,
      Creable<Persona> by creador,
      Listable<Persona> by listador,
      Buscable<Persona, Long> by buscador,
      BuscableConParametros<Persona, DocumentoCompletoDAO> by buscadorConParametros,
      Actualizable<Persona, Long> by actualizador,
      EliminablePorId<Persona, Long> by eliminador,
      RepositorioPersonas
{
    companion object
    {
        private val NOMBRE_ENTIDAD = Persona.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD

    private constructor(parametrosDAO: ParametrosParaDAOEntidadDeCliente<PersonaDAO, Long?>)
            : this(
            CreadorUnicaVez(
                    CreadorRepositorioSimple(parametrosDAO, NOMBRE_ENTIDAD)
                           ),
            CreableEnTransaccionSQL(
                    parametrosDAO.configuracion,
                    CreableSimple(
                            CreableDAO(
                                    parametrosDAO,
                                    Persona.NOMBRE_ENTIDAD
                                      ),
                            transformadorPersonaAPersonaDaoCreacion
                                 )
                                   ),
            ListableSimple<Persona, PersonaDAO, Long?>(ListableDAO(listOf(PersonaDAO.COLUMNA_ID), parametrosDAO)),
            BuscableSimple(
                    TransformadorIdentidad(),
                    BuscableDao(parametrosDAO)
                          ),
            BuscableConParametrosSegunListableFiltrableConParametros(
                    ListableConParametrosSimple(
                            ListableConParametrosDAO(listOf(PersonaDAO.COLUMNA_ID), parametrosDAO)
                                               )
                                                                    ),
            ActualizableEnTransaccionSQL(
                    parametrosDAO.configuracion,
                    ActualizableSimple(
                            ActualizableDAO(parametrosDAO, NOMBRE_ENTIDAD),
                            TransformadorIdentidad(),
                            transformadorPersonaAPersonaDaoActualizacion
                                      )
                                        ),
            EliminablePorIdEnTransaccionSQL(
                    parametrosDAO.configuracion,
                    EliminableSimple(
                            EliminableDao(NOMBRE_ENTIDAD, parametrosDAO),
                            TransformadorIdentidad()
                                    )
                                           )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(ParametrosParaDAOEntidadDeCliente(configuracion, PersonaDAO.TABLA, PersonaDAO::class.java))
}

data class DocumentoCompletoDAO private constructor
(
        private val numeroDocumento: String,
        private val tipoDocumento: PersonaDAO.TipoDocumento
) : ParametrosConsulta
{
    override val filtrosSQL: List<FiltroSQL> = listOf(
            FiltroIgualdad(CampoTabla(PersonaDAO.TABLA, PersonaDAO.COLUMNA_NUMERO_DOCUMENTO), numeroDocumento, SqlType.STRING),
            FiltroIgualdad(CampoTabla(PersonaDAO.TABLA, PersonaDAO.COLUMNA_TIPO_DOCUMENTO), tipoDocumento, SqlType.STRING)
                                                     )

    constructor(documentoCompleto: DocumentoCompleto)
            : this(documentoCompleto.numeroDocumento, PersonaDAO.TipoDocumento.desdeNegocio(documentoCompleto.tipoDocumento))
}