package co.smartobjects.persistencia.personas.relacionesdepersonas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.persistencia.*
import co.smartobjects.persistencia.basederepositorios.*
import co.smartobjects.persistencia.fondos.combinadores.repositorioEntidadDao
import co.smartobjects.persistencia.personas.PersonaDAO
import com.j256.ormlite.field.SqlType


interface RepositorioRelacionesPersonas
    : CreadorRepositorio<PersonaConFamiliares>,
      CreableConDiferenteSalida<PersonaConFamiliares, Unit>,
      BuscableConParametros<PersonaConFamiliares, FiltroRelacionesPersonas.PorDocumentoCompleto>


private class ListableAgrupandoPersonasEnFamiliares
(
        private val listadorJoin: ListableConParametrosFiltrableOrdenable<PersonaDAO, FiltroRelacionesPersonas.PorDocumentoCompleto>
) : ListableConParametrosFiltrableOrdenable<EntidadRelacionUnoAMuchos<PersonaDAO, PersonaDAO>, FiltroRelacionesPersonas.PorDocumentoCompleto>
{
    override fun listarOrdenado(idCliente: Long, parametros: FiltroRelacionesPersonas.PorDocumentoCompleto)
            : Sequence<EntidadRelacionUnoAMuchos<PersonaDAO, PersonaDAO>>
    {
        return listarSegunParametros(idCliente, parametros)
    }

    override fun listarSegunParametros(idCliente: Long, parametros: FiltroRelacionesPersonas.PorDocumentoCompleto)
            : Sequence<EntidadRelacionUnoAMuchos<PersonaDAO, PersonaDAO>>
    {
        val listado = listadorJoin.listarOrdenado(idCliente, parametros)
        var padre: PersonaDAO? = null
        val familiares = mutableListOf<PersonaDAO>()

        listado.forEach {
            if (it.tipoDocumento == parametros.tipoDocumento && it.numeroDocumento == parametros.numeroDocumento)
            {
                padre = it
            }
            else
            {
                familiares.add(it)
            }
        }

        return if (padre != null)
        {
            sequenceOf(EntidadRelacionUnoAMuchos(padre!!, familiares))
        }
        else
        {
            sequenceOf()
        }
    }

    override fun conFiltrosSQL(filtrosSQL: List<FiltroSQL>)
            : ListableConParametrosFiltrableOrdenable<
            EntidadRelacionUnoAMuchos<PersonaDAO, PersonaDAO>, FiltroRelacionesPersonas.PorDocumentoCompleto
            >
    {
        return ListableAgrupandoPersonasEnFamiliares(listadorJoin.conFiltrosSQL(filtrosSQL))
    }
}


class RepositorioRelacionesPersonasSQL
private constructor
(
        private val creadorRepositorio: CreadorRepositorio<PersonaConFamiliares>,
        private val creador: CreableConDiferenteSalida<PersonaConFamiliares, Unit>,
        private val buscador: BuscableConParametros<PersonaConFamiliares, FiltroRelacionesPersonas.PorDocumentoCompleto>
) : CreadorRepositorio<PersonaConFamiliares> by creadorRepositorio,
    CreableConDiferenteSalida<PersonaConFamiliares, Unit> by creador,
    BuscableConParametros<PersonaConFamiliares, FiltroRelacionesPersonas.PorDocumentoCompleto> by buscador,
    RepositorioRelacionesPersonas
{
    companion object
    {
        private val NOMBRE_ENTIDAD = PersonaConFamiliares.NOMBRE_ENTIDAD
    }

    override val nombreEntidad: String = NOMBRE_ENTIDAD


    private constructor(
            parametrosDaoRelacionPersona: ParametrosParaDAOEntidadDeCliente<RelacionPersonaDAO, Long>,
            parametrosDaoPersona: ParametrosParaDAOEntidadDeCliente<PersonaDAO, Long>
                       )
            : this(
            CreadorUnicaVez
            (
                    CreadorRepositorioSimple<PersonaConFamiliares, RelacionPersonaDAO, Long>(parametrosDaoRelacionPersona, NOMBRE_ENTIDAD)
            ),
            CreableEnTransaccionSQLParaDiferenteSalida<PersonaConFamiliares, Unit>
            (
                    parametrosDaoRelacionPersona.configuracion,
                    CreableEliminandoSegunParametrosConDiferenteSalidaYParametros<
                            PersonaConFamiliares,
                            FiltroRelacionesPersonas,
                            Unit,
                            RelacionPersonaDAO
                            >
                    (
                            CreableConTransformacionIntermediaConDiferenteSalida<PersonaConFamiliares, List<RelacionPersonaDAO>, Unit>
                            (
                                    CreableDAOMultiples(parametrosDaoRelacionPersona, NOMBRE_ENTIDAD),
                                    object : TransformadorEntidadCliente<PersonaConFamiliares, List<RelacionPersonaDAO>>
                                    {
                                        override fun transformar(idCliente: Long, origen: PersonaConFamiliares): List<RelacionPersonaDAO>
                                        {
                                            return origen.familiares.asSequence().flatMap {
                                                sequenceOf(
                                                        RelacionPersonaDAO(origen.persona, it),
                                                        RelacionPersonaDAO(it, origen.persona)
                                                          )
                                            }.toList()
                                        }
                                    },
                                    object : TransformadorEntidadCliente<List<RelacionPersonaDAO>, Unit>
                                    {
                                        override fun transformar(idCliente: Long, origen: List<RelacionPersonaDAO>): Unit
                                        {
                                            return Unit
                                        }
                                    }
                            ),
                            { personaConFamiliares ->
                                FiltroRelacionesPersonas.IdPersonaEstaEnRelacion(personaConFamiliares.persona.id!!)
                            },
                            EliminablePorParametrosDao(NOMBRE_ENTIDAD, parametrosDaoRelacionPersona)
                    )
            ),
            BuscableConParametrosSegunListableFiltrableConParametros
            (
                    ListableConTransaccionYParametros
                    (
                            parametrosDaoRelacionPersona.configuracion,
                            NOMBRE_ENTIDAD,
                            ListableConTransformacionYParametros
                            (
                                    ListableAgrupandoPersonasEnFamiliares
                                    (
                                            ListableSQLConParametrosInyectadosUsandoOr
                                            (
                                                    ListableInSubqueryConParametrosEnSubquery
                                                    (
                                                            ListableIgnorandoEntidadDestino
                                                            (
                                                                    ListableLeftJoinSegunColumnas
                                                                    (
                                                                            repositorioEntidadDao(parametrosDaoPersona, PersonaDAO.COLUMNA_ID),
                                                                            PersonaDAO.COLUMNA_ID,
                                                                            repositorioEntidadDao(parametrosDaoRelacionPersona, RelacionPersonaDAO.COLUMNA_ID_HIJO),
                                                                            RelacionPersonaDAO.COLUMNA_ID_HIJO,
                                                                            object : Transformador<RelacionPersonaDAO, Boolean>
                                                                            {
                                                                                override fun transformar(origen: RelacionPersonaDAO): Boolean
                                                                                {
                                                                                    return origen.id == null
                                                                                }
                                                                            }
                                                                    ),
                                                                    1
                                                            ),
                                                            ListableProyectandoAColumnaLongConParametros<PersonaDAO, FiltroRelacionesPersonas.PorDocumentoCompleto>
                                                            (
                                                                    ListableConParametrosDAO(listOf(PersonaDAO.COLUMNA_ID), parametrosDaoPersona),
                                                                    CampoTabla(PersonaDAO.TABLA, PersonaDAO.COLUMNA_ID)
                                                            ),
                                                            CampoTabla(RelacionPersonaDAO.TABLA, RelacionPersonaDAO.COLUMNA_ID_PADRE)
                                                    )
                                            )
                                    ),
                                    object : TransformadorEntidadCliente<EntidadRelacionUnoAMuchos<PersonaDAO, PersonaDAO>, PersonaConFamiliares>
                                    {
                                        override fun transformar(idCliente: Long, origen: EntidadRelacionUnoAMuchos<PersonaDAO, PersonaDAO>): PersonaConFamiliares
                                        {
                                            return PersonaConFamiliares(
                                                    origen.entidadOrigen.aEntidadDeNegocio(idCliente),
                                                    origen.entidadDestino.filter { it != origen.entidadOrigen }.map { it.aEntidadDeNegocio(idCliente) }.toSet()
                                                                       )
                                        }
                                    }
                            )
                    )
            )
                  )

    constructor(configuracion: ConfiguracionRepositorios)
            : this(
            ParametrosParaDAOEntidadDeCliente(configuracion, RelacionPersonaDAO.TABLA, RelacionPersonaDAO::class.java),
            ParametrosParaDAOEntidadDeCliente(configuracion, PersonaDAO.TABLA, PersonaDAO::class.java)
                  )
}

sealed class FiltroRelacionesPersonas : ParametrosConsulta
{
    final override val filtrosSQL by lazy {
        filtrosSQLAdicionales
    }

    protected abstract val filtrosSQLAdicionales: List<FiltroSQL>

    class IdPersonaEstaEnRelacion(val idPersona: Long) : FiltroRelacionesPersonas()
    {
        override val filtrosSQLAdicionales: List<FiltroSQL> =
                listOf(
                        FiltroIgualdad(
                                CampoTabla(RelacionPersonaDAO.TABLA, RelacionPersonaDAO.COLUMNA_ID_PADRE),
                                idPersona,
                                SqlType.LONG
                                      )
                            .or(
                                    FiltroIgualdad(
                                            CampoTabla(RelacionPersonaDAO.TABLA, RelacionPersonaDAO.COLUMNA_ID_HIJO),
                                            idPersona,
                                            SqlType.LONG
                                                  )
                               )
                      )
    }

    class PorDocumentoCompleto private constructor
    (
            val tipoDocumento: PersonaDAO.TipoDocumento,
            val numeroDocumento: String
    ) : ParametrosConsulta
    {
        override val filtrosSQL: List<FiltroSQL> = listOf(
                FiltroIgualdad(CampoTabla(PersonaDAO.TABLA, PersonaDAO.COLUMNA_NUMERO_DOCUMENTO), numeroDocumento, SqlType.STRING),
                FiltroIgualdad(CampoTabla(PersonaDAO.TABLA, PersonaDAO.COLUMNA_TIPO_DOCUMENTO), tipoDocumento, SqlType.STRING)
                                                         )

        constructor(documentoCompleto: DocumentoCompleto)
                : this(
                PersonaDAO.TipoDocumento.desdeNegocio(documentoCompleto.tipoDocumento),
                documentoCompleto.numeroDocumento
                      )

        override fun equals(other: Any?): Boolean
        {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PorDocumentoCompleto

            if (tipoDocumento != other.tipoDocumento) return false
            if (numeroDocumento != other.numeroDocumento) return false

            return true
        }

        override fun hashCode(): Int
        {
            var result = tipoDocumento.hashCode()
            result = 31 * result + numeroDocumento.hashCode()
            return result
        }

        override fun toString(): String
        {
            return "PorDocumentoCompleto(tipoDocumento=$tipoDocumento, numeroDocumento='$numeroDocumento')"
        }
    }
}