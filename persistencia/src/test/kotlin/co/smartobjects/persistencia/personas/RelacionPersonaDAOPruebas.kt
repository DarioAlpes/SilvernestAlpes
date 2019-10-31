package co.smartobjects.persistencia.personas

import co.smartobjects.entidades.personas.DocumentoCompleto
import co.smartobjects.entidades.personas.Persona
import co.smartobjects.entidades.personas.PersonaConFamiliares
import co.smartobjects.persistencia.EntidadDAOBasePruebas
import co.smartobjects.persistencia.TestConMultiplesDAO
import co.smartobjects.persistencia.basederepositorios.CreadorRepositorio
import co.smartobjects.persistencia.excepciones.ErrorDeLlaveForanea
import co.smartobjects.persistencia.excepciones.EsquemaNoExiste
import co.smartobjects.persistencia.listarTodasLasRelacionesPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.FiltroRelacionesPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.RelacionPersonaDAO
import co.smartobjects.persistencia.personas.relacionesdepersonas.RepositorioRelacionesPersonas
import co.smartobjects.persistencia.personas.relacionesdepersonas.RepositorioRelacionesPersonasSQL
import co.smartobjects.utilidades.ZONA_HORARIA_POR_DEFECTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


internal class RelacionPersonaDAOPruebas : EntidadDAOBasePruebas()
{
    private val repositorioPersonas: RepositorioPersonas by lazy { RepositorioPersonasSQL(configuracionRepositorios) }
    private val repositorio: RepositorioRelacionesPersonas by lazy { RepositorioRelacionesPersonasSQL(configuracionRepositorios) }

    override val creadoresRepositoriosUsados: List<CreadorRepositorio<*>> by lazy {
        listOf<CreadorRepositorio<*>>(repositorioPersonas, repositorio)
    }

    private fun darInstanciaDePersona(indice: Int): Persona
    {
        assert(indice >= 0)
        return Persona(
                idClientePruebas,
                null,
                "Persona prueba $indice",
                Persona.TipoDocumento.values().let { it[indice % it.size] },
                "documento-$indice",
                Persona.Genero.values().let { it[indice % it.size] },
                LocalDate.now(ZONA_HORARIA_POR_DEFECTO).minusDays(indice.toLong()),
                Persona.Categoria.values().let { it[indice % it.size] },
                Persona.Afiliacion.values().let { it[indice % it.size] },
                null,
                "empresa",
                "0",
                Persona.Tipo.NO_AFILIADO
                      )
    }

    private fun crearPersona(persona: Persona) = repositorioPersonas.crear(idClientePruebas, persona)

    private fun darInstanciaEntidadValida(numeroFamiliares: Int = 3, indice: Int = 0): PersonaConFamiliares
    {
        assert(numeroFamiliares >= 0)
        assert(indice >= 0)

        val numeroPersonaQueNoSeRelacionaran = 5
        for (i in indice until indice + numeroPersonaQueNoSeRelacionaran)
        {
            crearPersona(darInstanciaDePersona(i))
        }

        val indiceExcluyendoPersonasNoRelacionadas = numeroPersonaQueNoSeRelacionaran + indice
        val persona = crearPersona(darInstanciaDePersona(indiceExcluyendoPersonasNoRelacionadas))
        val familiares =
                if (numeroFamiliares == 0)
                {
                    setOf()
                }
                else
                {
                    (1..numeroFamiliares).map {
                        crearPersona(darInstanciaDePersona(indiceExcluyendoPersonasNoRelacionadas + it))
                    }.toSet()
                }

        return PersonaConFamiliares(persona, familiares)
    }

    @Nested
    inner class Crear
    {
        @Nested
        inner class SinFamiliaresEnBD
        {
            @TestConMultiplesDAO
            fun con_familiares_se_crea_correctamente_en_BD()
            {
                val entidadAInsertar = darInstanciaEntidadValida()
                val relacionesEsperadas =
                        entidadAInsertar.familiares.asSequence().flatMap {
                            sequenceOf(
                                    RelacionPersonaDAO(
                                            personaPadreDao = PersonaDAO(id = entidadAInsertar.persona.id),
                                            personaHijaDao = PersonaDAO(id = it.id)
                                                      ),
                                    RelacionPersonaDAO(
                                            personaPadreDao = PersonaDAO(id = it.id),
                                            personaHijaDao = PersonaDAO(id = entidadAInsertar.persona.id)
                                                      )
                                      )
                        }.toList().sortedBy { it.personaPadreDao.id }

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val relacionesEnBd =
                        listarTodasLasRelacionesPersonas(idClientePruebas, configuracionRepositorios)
                            .map { it.copy(id = null) }
                            .sortedBy { it.personaPadreDao.id }

                assertEquals(relacionesEsperadas, relacionesEnBd)
            }

            @TestConMultiplesDAO
            fun sin_familiares_no_crea_nada_en_BD()
            {
                val entidadAInsertar = darInstanciaEntidadValida(0)

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val relacionesEnBd = listarTodasLasRelacionesPersonas(idClientePruebas, configuracionRepositorios)

                assertTrue(relacionesEnBd.isEmpty())
            }
        }

        @Nested
        inner class ConFamiliaresEnBD
        {
            private lateinit var entidadDePrueba: PersonaConFamiliares

            @BeforeEach
            fun crearFamiliares()
            {
                entidadDePrueba = darInstanciaEntidadValida()
                repositorio.crear(idClientePruebas, entidadDePrueba)
            }

            @TestConMultiplesDAO
            fun sin_familiares_a_crear_borra_los_existentes_y_no_crea_nada()
            {
                val entidadAInsertar = entidadDePrueba.copiar(familiares = setOf())

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val relacionesEnBd = listarTodasLasRelacionesPersonas(idClientePruebas, configuracionRepositorios)

                assertTrue(relacionesEnBd.isEmpty())
            }

            @TestConMultiplesDAO
            fun con_familiares_diferentes_a_crear_borra_los_existentes_y_crea_nuevos()
            {
                val entidadAInsertar =
                        darInstanciaEntidadValida(indice = 1 + 2000 * entidadDePrueba.familiares.size)
                            .copiar(persona = entidadDePrueba.persona)

                val relacionesEsperadas =
                        entidadAInsertar.familiares.asSequence().flatMap {
                            sequenceOf(
                                    RelacionPersonaDAO(
                                            personaPadreDao = PersonaDAO(id = entidadAInsertar.persona.id),
                                            personaHijaDao = PersonaDAO(id = it.id)
                                                      ),
                                    RelacionPersonaDAO(
                                            personaPadreDao = PersonaDAO(id = it.id),
                                            personaHijaDao = PersonaDAO(id = entidadAInsertar.persona.id)
                                                      )
                                      )
                        }.toList().sortedBy { it.personaPadreDao.id }

                repositorio.crear(idClientePruebas, entidadAInsertar)

                val relacionesEnBd =
                        listarTodasLasRelacionesPersonas(idClientePruebas, configuracionRepositorios)
                            .map { it.copy(id = null) }
                            .sortedBy { it.personaPadreDao.id }

                assertEquals(relacionesEsperadas, relacionesEnBd)
            }
        }

        @[Nested DisplayName("lanza LanzaErrorDeLlaveForanea si")]
        inner class LanzaErrorDeLlaveForanea
        {
            @TestConMultiplesDAO
            fun la_persona_no_existe()
            {
                val entidadCrear =
                        darInstanciaEntidadValida().let {
                            it.copiar(persona = it.persona.copiar(id = 111222333))
                        }

                assertThrows<ErrorDeLlaveForanea> {
                    repositorio.crear(idClientePruebas, entidadCrear)
                }
            }

            @TestConMultiplesDAO
            fun algun_familiar_no_existe()
            {
                val entidadCrear =
                        darInstanciaEntidadValida().let {
                            it.copiar(familiares = setOf(it.familiares.last().copiar(id = 1894647)))
                        }

                assertThrows<ErrorDeLlaveForanea> {
                    repositorio.crear(idClientePruebas, entidadCrear)
                }
            }
        }

        @TestConMultiplesDAO
        fun lanza_excepcion_EsquemaNoExiste_si_cliente_con_id_dado_no_existe()
        {
            val entidadAInsertar = darInstanciaEntidadValida()

            assertThrows<EsquemaNoExiste> { repositorio.crear(idClientePruebas + 100, entidadAInsertar) }
        }
    }

    @Nested
    inner class Consultar
    {
        @Nested
        inner class PorDocumentoCompleto
        {
            @TestConMultiplesDAO
            fun para_persona_inexistente_retorna_null()
            {
                val documentoInexistente = DocumentoCompleto(Persona.TipoDocumento.CC, "233g93g9j34")
                val entidadConsultada =
                        repositorio
                            .buscarSegunParametros(
                                    idClientePruebas,
                                    FiltroRelacionesPersonas.PorDocumentoCompleto(documentoInexistente)
                                                  )

                assertNull(entidadConsultada)
            }

            @TestConMultiplesDAO
            fun para_persona_existente_pero_sin_relaciones_retorna_persona_sin_familiares()
            {
                val entidadEsperada = darInstanciaEntidadValida().copiar(familiares = setOf())

                val entidadConsultada =
                        repositorio
                            .buscarSegunParametros(
                                    idClientePruebas,
                                    FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(entidadEsperada.persona))
                                                  )

                assertEquals(entidadEsperada, entidadConsultada)
            }

            @Nested
            inner class ConFamiliaresCreados
            {
                private lateinit var entidadDePrueba: PersonaConFamiliares


                @BeforeEach
                fun prepararPrueba()
                {
                    entidadDePrueba = darInstanciaEntidadValida()
                    repositorio.crear(idClientePruebas, entidadDePrueba)
                }


                @TestConMultiplesDAO
                fun retorna_persona_con_familiares_correctos()
                {
                    val entidadConsultada =
                            repositorio
                                .buscarSegunParametros(
                                        idClientePruebas,
                                        FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(entidadDePrueba.persona))
                                                      )

                    assertEquals(entidadDePrueba, entidadConsultada)
                }

                @TestConMultiplesDAO
                fun lanza_excepcion_EsquemaNoExiste_si_se_consulta_por_id_con_id_cliente_inexistente()
                {
                    assertThrows<EsquemaNoExiste> {
                        repositorio
                            .buscarSegunParametros(
                                    idClientePruebas + 100,
                                    FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(entidadDePrueba.persona))
                                                  )
                    }
                }
            }

            @TestConMultiplesDAO
            fun por_documento_existente_en_otro_cliente_retorna_null()
            {
                val entidadEsperada = darInstanciaEntidadValida()

                ejecutarConClienteAlternativo {
                    val entidadConsultada = repositorio.buscarSegunParametros(
                            it.id!!,
                            FiltroRelacionesPersonas.PorDocumentoCompleto(DocumentoCompleto(entidadEsperada.persona))
                                                                             )
                    assertNull(entidadConsultada)
                }
            }
        }
    }
}